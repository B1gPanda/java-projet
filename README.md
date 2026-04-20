# Simulation d'Écosystème Marin

## Description

Ce projet implémente une simulation d'écosystème marin où des agents (requins et bateaux) interagissent avec des ressources marines (poissons, sacs de plastique, humains naufragés) sur une grille. La simulation évolue par étapes, avec des règles d'énergie, de reproduction et d'interactions.

## Classes Principales

### Interfaces et Classes Abstraites
- **Entity** : Interface pour les entités positionnables sur la grille (ligne, colonne, type, description).
- **Agent** : Classe abstraite étendant Entity, représentant les agents mobiles avec de l'énergie. Méthodes clés : `agir()`, `moveTowards()`, `moveRandomly()`, `isAlive()`.
- **ResourceMarine** : Classe abstraite pour les ressources marines, implémentant Entity.

### Agents
- **Requin** : Agent prédateur qui cherche les poissons et humains les plus proches. Perd 1 énergie par tour. Gagne +1 énergie par poisson mangé, +2 par humain, perd -10 par sac de plastique.
- **Bateau** : Agent sauveteur qui cherche les poissons et humains les plus proches. Perd 1 énergie par tour seulement s'il est sur la même case qu'un requin. Ne regagne pas d'énergie une fois perdue. Sauve des humains et pêche des poissons.

### Ressources
- **Poisson** : Ressource qui croît en quantité chaque tour. Mangée par les requins et pêchée par les bateaux.
- **SacPlastique** : Déchet qui pénalise les requins (-10 énergie).
- **HumainNaufrage** : Humain à sauver par les bateaux ou à manger par les requins.

### Simulation
- **Simulation** : Classe principale qui initialise le terrain, les agents et les ressources, puis lance la boucle de simulation. Gère les interactions, nettoie les morts et affiche l'état à chaque étape.
- **Terrain** : Classe fournie pour gérer la grille (non modifiable).

## Règles de Simulation

- **Grille** : 10x10 cases.
- **Énergie** :
  - Requins : Démarrent à 18, perdent 1/tour, gains comme ci-dessus.
  - Bateaux : Démarrent à 16, ne perdent que si sur même case qu'un requin (-1), ne regagnent pas d'énergie.
- **Interactions** :
  - Requins mangent poissons (réduisent quantité de 2, gagnent +1) ou humains (+2, suppriment la ressource).
  - Bateaux pêchent poissons (réduisent quantité de 2, comptent comme pêchés) ou sauvent humains (comptent comme sauvés, suppriment la ressource).
  - Si requin et bateau sur même case, bateau perd 1 énergie.
  - Si bateau énergie = 0, perd ses humains sauvés.
- **Croissance** : Poissons croissent chaque tour.
- **Nouvelle production** : Chance de 50% d'ajouter un nouveau poisson par tour.
- **Durée** : 12 étapes.

## Comment Lancer

1. Compiler : `javac *.java`
2. Exécuter : `java Simulation`

La simulation affiche la grille avec les agents colorés (rouge pour requins, bleu pour bateaux, magenta si multiples), les statistiques d'humains sauvés/mangés et poissons pêchés.

## Documentation Générée

La documentation Javadoc est disponible dans le dossier `doc/`. Ouvrez `doc/index.html` dans un navigateur pour consulter les détails des classes et méthodes.