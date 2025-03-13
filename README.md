# AzuriaDailyRewards 

DailyRewards est un plugin Minecraft 1.20 qui permet aux joueurs de réclamer une récompense quotidienne en un simple clic ! 

- Système de récompenses configurable (items, argent, clés, etc.)
- Compatibilité avec Vault pour gérer l’économie
- Interface intuitive et personnalisable
- Suivi du temps entre chaque récompense



### Commandes joueur

- /dailyrewards : pour ouvrir votre GUI de récompense

- /dailyrewards help : Pour savoir le temps d'attente avant de récupérer une récompense 

- /dailyrewards info : Pour avoir des informations sur le plugin !



### Commandes admin

- /dailyrewards reload : pour reload les configs du plugin

- /dailyrewards resetall : Pour reset le temps d'attente de tout les joueurs

- /dailyrewards reset {player} : Pour reset le temps d'attente d'un joueur



# Config.yml : 
![image](https://github.com/user-attachments/assets/0f23134f-c1f4-4f50-9b9d-04036850a0f5)

Si vous souhaitez donner simplement un items en récompense : 
 - item: STONE (nom de l'item)
  amount: 19 (nombre à donner)
  chance: 20 (chance d'obtenir la récompense)

Pour definir le joueur utilisé : {player}
Si vous voulez simplement utiliser une commande vous pouvez utiliser :
 - commands : eco give {player} 500 (votre commande)
  chance : 30 (chance d'obtenir la récompense)

Pour definir le joueur utilisé : {player}
Si vous voulez utiliser une commande + un itemss vous pouvez utiliser :
 - item: GOLD_INGOT (nom de l'item)
  amount: 1 (nombre à donner)
  commands: eco give {player} 100 (votre commande)
  chance: 50 (chance d'obtenir la récompense)

