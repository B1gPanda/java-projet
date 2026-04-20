import java.util.List;

/**
 * Classe représentant un requin, agent prédateur dans la simulation.
 * Le requin cherche les ressources (poissons, humains) et les bateaux proches pour se déplacer.
 */
public class Requin extends Agent {
    /**
     * Constructeur pour créer un requin.
     * @param ligne La ligne initiale.
     * @param colonne La colonne initiale.
     * @param nbLignes Nombre total de lignes.
     * @param nbColonnes Nombre total de colonnes.
     */
    public Requin(int ligne, int colonne, int nbLignes, int nbColonnes) {
        super("Requin", 18, ligne, colonne, nbLignes, nbColonnes);
    }

    /**
     * Définit l'action du requin à chaque tour : se déplacer vers la ressource ou bateau le plus proche, puis perdre 1 énergie.
     * @param terrain Le terrain de la simulation.
     * @param allAgents La liste de tous les agents.
     */
    @Override
    public void agir(Terrain terrain, List<Agent> allAgents) {
        ResourceMarine cible = findNearestRessource(terrain);
        Agent cibleBateau = findNearestAgent(allAgents, Bateau.class);
        if (cible != null) {
            moveTowards(cible.getLigne(), cible.getColonne());
        } else if (cibleBateau != null && distance(cibleBateau) <= 5) {
            moveTowards(cibleBateau.getLigne(), cibleBateau.getColonne());
        } else {
            moveRandomly();
        }
        energie = Math.max(0, energie - 1);
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
     * @param typeRecherche La classe du type d'agent à rechercher (ex: Bateau.class).
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
