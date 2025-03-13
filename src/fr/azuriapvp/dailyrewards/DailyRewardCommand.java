package fr.azuriapvp.dailyrewards;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

public class DailyRewardCommand implements CommandExecutor {

    private AzuriaDailyRewards plugin;

    public DailyRewardCommand(AzuriaDailyRewards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("azuriapvp.dailyrewards.admin")) {
                sender.sendMessage("§b[DailyRewards] §cVous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }
            plugin.reloadConfig();
            plugin.reloadMessages();
            sender.sendMessage("§b[DailyRewards] §aDailyRewards a été rechargé avec succès.");
            return true;
        }
        
        if (args.length > 0 && args[0].equalsIgnoreCase("resetall")) {
            if (!sender.hasPermission("azuriapvp.dailyrewards.admin")) {
                sender.sendMessage("§b[DailyRewards] §cVous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }
            try {
                Connection conn = plugin.getDatabaseManager().getConnection();
                PreparedStatement ps = conn.prepareStatement("UPDATE cooldowns SET cooldown = 0");
                int count = ps.executeUpdate();
                ps.close();
                sender.sendMessage("§aCooldown réinitialisé pour " + count + " joueurs.");
            } catch (SQLException e) {
                sender.sendMessage("§b[DailyRewards] §cErreur lors de la réinitialisation des cooldowns.");
                e.printStackTrace();
            }
            return true;
        }
        
        if (args.length > 0 && args[0].equalsIgnoreCase("reset") && args.length > 1) {
            if (!sender.hasPermission("azuriapvp.dailyrewards.admin")) {
                sender.sendMessage("§b[DailyRewards] §cVous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }
            String targetName = args[1];
            @SuppressWarnings("deprecation")
			OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            try {
                Connection conn = plugin.getDatabaseManager().getConnection();
                PreparedStatement ps = conn.prepareStatement("UPDATE cooldowns SET cooldown = 0 WHERE uuid = ?");
                ps.setString(1, target.getUniqueId().toString());
                int count = ps.executeUpdate();
                ps.close();
                if (count > 0) {
                    sender.sendMessage("§b[DailyRewards] §aCooldown réinitialisé pour " + target.getName() + ".");
                } else {
                    sender.sendMessage("§b[DailyRewards] §cAucun cooldown trouvé pour " + target.getName() + ".");
                }
            } catch (SQLException e) {
                sender.sendMessage("§b[DailyRewards] §cErreur lors de la réinitialisation du cooldown de " + target.getName() + ".");
                e.printStackTrace();
            }
            return true;
        }
        
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }
        
        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            sendInfo(sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessages().getString("command.onlyPlayers", "Seuls les joueurs peuvent utiliser cette commande."));
            return true;
        }
        Player player = (Player) sender;

        String guiTitle = plugin.getMessages().getString("gui.title", "§6Daily Rewards");
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);

        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, blackGlass);
        }

        boolean eligible = checkPlayerEligibility(player);
        Material woolType = eligible ? Material.GREEN_WOOL : Material.RED_WOOL;
        ItemStack claimItem = new ItemStack(woolType);
        ItemMeta claimMeta = claimItem.getItemMeta();
        if (eligible) {
            claimMeta.setDisplayName(plugin.getMessages().getString("message.recoverReward", "§aRécupèrez votre récompense !"));
        } else {
            claimMeta.setDisplayName(plugin.getMessages().getString("message.alreadyClaimed", "§cVous avez déjà récupéré votre récompense !"));
        }
        claimItem.setItemMeta(claimMeta);
        gui.setItem(11, claimItem);

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
        headMeta.setOwningPlayer(player);
        headMeta.setDisplayName(player.getName());

        long cooldown = plugin.getCooldown(player.getUniqueId().toString());
        long currentTime = System.currentTimeMillis();
        long remainingMillis = Math.max(0, cooldown - currentTime);
        long remainingSeconds = remainingMillis / 1000;
        long hours = remainingSeconds / 3600;
        long minutes = (remainingSeconds % 3600) / 60;
        long seconds = remainingSeconds % 60;
        String lore = plugin.getMessages().getString("gui.timeRemaining", "§bRevenez dans : §a{hours}§bh §a{minutes}§bm §a{seconds}§bs")
                        .replace("{hours}", String.valueOf(hours))
                        .replace("{minutes}", String.valueOf(minutes))
                        .replace("{seconds}", String.valueOf(seconds));
        headMeta.setLore(Collections.singletonList(lore));
        playerHead.setItemMeta(headMeta);
        gui.setItem(15, playerHead);

        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(plugin.getMessages().getString("gui.closeMenu", "§cFermer le Menu"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(16, closeItem);

        player.openInventory(gui);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        FileConfiguration config = plugin.getConfig();
        int delay = config.getInt("reward-delay");
        int hours = delay / 3600;
        int minutes = (delay % 3600) / 60;
        int seconds = delay % 60;
        String helpMessage = plugin.getMessages().getString("message.helpMessage",
                "§m§l§e========== §bDailyRewards §m§l§e===========\n\n" +
                "§6Utilise la commande : §a/dailyrewards\n" +
                "§6Vous pouvez récupérer une récompense tous les {hours}h {minutes}m {seconds}s.\n\n" +
                "§m§l§e========== §bDailyRewards §m§l§e===========");
        helpMessage = helpMessage.replace("{hours}", String.valueOf(hours))
                                 .replace("{minutes}", String.valueOf(minutes))
                                 .replace("{seconds}", String.valueOf(seconds));
        sender.sendMessage(helpMessage);
    }

    private void sendInfo(CommandSender sender) {
        String infoMessage = "§m§l§e----------- §bDailyRewards §m§l§e-----------\n\n" +
                             "§6Plugin : AzuriaDailyRewards\n" +
                             "§6Développeur : AzZoXSky\n" +
                             "§6Version : 1.0\n" +
                             "§6GitHub : https://github.com/AzZoXSky\n\n" +
                             "§m§l§e----------- §bDailyRewards §m§l§e-----------";
        sender.sendMessage(infoMessage);
    }

    private boolean checkPlayerEligibility(Player player) {
        long currentTime = System.currentTimeMillis();
        String uuid = player.getUniqueId().toString();
        long cooldown = plugin.getCooldown(uuid);
        return currentTime >= cooldown;
    }
}
