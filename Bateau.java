import java.util.List;

/**
 * Classe représentant un bateau, agent sauveteur dans la simulation.
 * Le bateau cherche les ressources (poissons, humains) et les requins proches pour se déplacer.
 */
public class Bateau extends Agent {
    private int humainsSauves = 0;
    private int poissonsPris = 0;

    /**
     * Constructeur pour créer un bateau.
     * @param ligne La ligne initiale.
     * @param colonne La colonne initiale.
     * @param nbLignes Nombre total de lignes.
     * @param nbColonnes Nombre total de colonnes.
     */
    public Bateau(int vie, int ligne, int colonne, int nbLignes, int nbColonnes) {
        super("Bateau", vie, ligne, colonne, nbLignes, nbColonnes);
    }

    /**
     * Définit l'action du bateau à chaque tour : se déplacer vers la ressource la plus proche.
     * Le bateau défend les naufragés si un requin s'approche (distance <= 3).
     * @param terrain Le terrain de la simulation.
     * @param allAgents La liste de tous les agents.
     */
    @Override
    public void agir(Terrain terrain, List<Agent> allAgents) {
        ResourceMarine cible = findNearestRessource(terrain);
        ResourceMarine humainProche = findNearestHumain(terrain);
        Agent cibleRequin = findNearestAgent(allAgents, Requin.class);
        
        // Si un naufragé est proche et un requin aussi s'approche, défendre
        if (humainProche != null && cibleRequin != null && distance(cibleRequin) <= 3) {
            moveTowards(cibleRequin.getLigne(), cibleRequin.getColonne());
        } else if (cible != null) {
            moveTowards(cible.getLigne(), cible.getColonne());
        } else {
            moveRandomly();
        }
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
