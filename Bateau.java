import java.util.List;

/**
 * Classe représentant un bateau, agent sauveteur dans la simulation.
 * Le bateau cherche les ressources (poissons, humains) et les requins proches pour se déplacer.
 */
public class Bateau extends Agent {
    /**
     * Énumération des stratégies possibles pour un bateau.
     */
    public enum Strategie {
        /** Priorité aux naufragés. */
        PROTECTEUR,
        /** Priorité à la pêche. */
        PECHEUR,
        /** Priorité à la chasse aux requins. */
        CHASSEUR
    }

    private int humainsSauves = 0;
    private int poissonsPris = 0;
    private final Strategie strategie;

    /**
     * Constructeur pour créer un bateau avec une stratégie.
     * @param vie L'énergie initiale.
     * @param ligne La ligne initiale.
     * @param colonne La colonne initiale.
     * @param nbLignes Nombre total de lignes.
     * @param nbColonnes Nombre total de colonnes.
     * @param strategie La stratégie du bateau (PROTECTEUR ou PECHEUR).
     */
    public Bateau(int vie, int ligne, int colonne, int nbLignes, int nbColonnes, Strategie strategie) {
        super("Bateau", vie, ligne, colonne, nbLignes, nbColonnes);
        this.strategie = strategie;
    }

    /**
     * Définit l'action du bateau à chaque tour selon sa stratégie.
     * @param terrain Le terrain de la simulation.
     * @param allAgents La liste de tous les agents.
     */
    @Override
    public void agir(Terrain terrain, List<Agent> allAgents) {
        ResourceMarine humainProche = findNearestHumain(terrain);
        ResourceMarine poissonProche = findNearestPoisson(terrain);
        Agent cibleRequin = findNearestAgent(allAgents, Requin.class);

        if (strategie == Strategie.PROTECTEUR) {
            // Mode protecteur : priorité aux naufragés
            if (humainProche != null) {
                moveTowards(humainProche.getLigne(), humainProche.getColonne());
            } else if (poissonProche != null) {
                moveTowards(poissonProche.getLigne(), poissonProche.getColonne());
            } else {
                moveRandomly();
            }
        } else if (strategie == Strategie.PECHEUR) {
            // Mode pêcheur : priorité aux poissons
            if (poissonProche != null) {
                moveTowards(poissonProche.getLigne(), poissonProche.getColonne());
            } else if (humainProche != null) {
                moveTowards(humainProche.getLigne(), humainProche.getColonne());
            } else {
                moveRandomly();
            }
        } else if (strategie == Strategie.CHASSEUR) {
            // Mode chasseur : priorité aux requins
            if (cibleRequin != null && distance(cibleRequin) <= 2) {
                // Attaque le requin à portée (distance 2)
                cibleRequin.energie = Math.max(0, cibleRequin.energie - 5);
            } else if (cibleRequin != null) {
                moveTowards(cibleRequin.getLigne(), cibleRequin.getColonne());
            } else if (humainProche != null) {
                moveTowards(humainProche.getLigne(), humainProche.getColonne());
            } else if (poissonProche != null) {
                moveTowards(poissonProche.getLigne(), poissonProche.getColonne());
            } else {
                moveRandomly();
            }
        }
    }

    /**
     * Retourne la stratégie du bateau.
     * @return La stratégie (PROTECTEUR ou PECHEUR).
     */
    public Strategie getStrategie() {
        return strategie;
    }

    /**
     * Trouve le poisson le plus proche.
     * @param terrain Le terrain contenant les ressources.
     * @return Le poisson le plus proche, ou null si aucun.
     */
    private ResourceMarine findNearestPoisson(Terrain terrain) {
        ResourceMarine meilleur = null;
        int distanceMin = Integer.MAX_VALUE;
        for (Ressource r : terrain.lesRessources()) {
            if (r instanceof Poisson) {
                ResourceMarine rm = (ResourceMarine) r;
                int dist = distance(rm.getLigne(), rm.getColonne());
                if (dist < distanceMin) {
                    distanceMin = dist;
                    meilleur = rm;
                }
            }
        }
        return meilleur;
    }

    /**
     * Trouve le naufragé le plus proche.
     * @param terrain Le terrain contenant les ressources.
     * @return Le naufragé le plus proche, ou null si aucun.
     */
    private ResourceMarine findNearestHumain(Terrain terrain) {
        ResourceMarine meilleur = null;
        int distanceMin = Integer.MAX_VALUE;
        for (Ressource r : terrain.lesRessources()) {
            if (r instanceof HumainNaufrage) {
                ResourceMarine rm = (ResourceMarine) r;
                int dist = distance(rm.getLigne(), rm.getColonne());
                if (dist < distanceMin) {
                    distanceMin = dist;
                    meilleur = rm;
                }
            }
        }
        return meilleur;
    }

    /**
     * Incrémente le nombre d'humains sauvés par ce bateau.
     */
    public void humainSauve() {
        humainsSauves++;
    }

    /**
     * Ajoute le nombre de poissons pris par ce bateau.
     * @param quantite La quantité de poissons pris.
     */
    public void poissonsPrises(int quantite) {
        poissonsPris += quantite;
    }

    /**
     * Retourne le nombre de poissons pris par ce bateau.
     * @return Le nombre de poissons pris.
     */
    public int poissonsPris() {
        return poissonsPris;
    }

    /**
     * Retourne le nombre d'humains sauvés par ce bateau.
     * @return Le nombre d'humains sauvés.
     */
    public int getHumainsSauves() {
        return humainsSauves;
    }

    /**
     * Trouve la ressource marine (poisson ou humain) la plus proche.
     * @param terrain Le terrain contenant les ressources.
     * @return La ressource la plus proche, ou null si aucune.
     */
    private ResourceMarine findNearestRessource(Terrain terrain) {
        ResourceMarine meilleur = null;
        int distanceMin = Integer.MAX_VALUE;
        for (Ressource r : terrain.lesRessources()) {
            if (r instanceof Poisson || r instanceof HumainNaufrage) {
                ResourceMarine rm = (ResourceMarine) r;
                int dist = distance(rm.getLigne(), rm.getColonne());
                if (dist < distanceMin) {
                    distanceMin = dist;
                    meilleur = rm;
                }
            }
        }
        return meilleur;
    }

    /**
     * Trouve l'agent du type spécifié le plus proche.
     * @param allAgents La liste de tous les agents.
     * @param typeRecherche La classe du type d'agent à rechercher (ex: Requin.class).
     * @return L'agent le plus proche du type spécifié, ou null si aucun.
     */
    private Agent findNearestAgent(List<Agent> allAgents, Class<? extends Agent> typeRecherche) {
        Agent meilleur = null;
        int distanceMin = Integer.MAX_VALUE;
        for (Agent other : allAgents) {
            if (other == this || !typeRecherche.isInstance(other)) {
                continue;
            }
            int dist = distance(other);
            if (dist < distanceMin) {
                distanceMin = dist;
                meilleur = other;
            }
        }
        return meilleur;
    }
}
