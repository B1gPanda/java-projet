import java.util.List;

/**
 * Classe représentant un requin, agent prédateur dans la simulation.
 * Le requin cherche les ressources (poissons, humains) et les bateaux proches pour se déplacer.
 */
public class Requin extends Agent implements Cloneable{
    /**
     * Constructeur pour créer un requin.
     * @param vie L'énergie initiale du requin.
     * @param ligne La ligne initiale.
     * @param colonne La colonne initiale.
     * @param nbLignes Nombre total de lignes.
     * @param nbColonnes Nombre total de colonnes.
     */
    public Requin(int vie, int ligne, int colonne, int nbLignes, int nbColonnes) {
        super("Requin", vie, ligne, colonne, nbLignes, nbColonnes);
    }

    /**
     * Définit l'action du requin à chaque tour : se déplacer vers la ressource la plus proche.
     * Les requins ne poursuivent pas les bateaux, ils cherchent uniquement la nourriture.
     * @param terrain Le terrain de la simulation.
     * @param allAgents La liste de tous les agents.
     */
    @Override
    public void agir(Terrain terrain, List<Agent> allAgents) {
        ResourceMarine cible = findNearestRessource(terrain);
        if (cible != null) {
            moveTowards(cible.getLigne(), cible.getColonne());
        } else {
            moveRandomly();
        }
        energie = Math.max(0, energie);
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

    /**
     * Clone le requin avec une position différente pour éviter les doublons au même endroit.
     * @return Un clone du requin avec une nouvelle position aléatoire.
     * @throws AssertionError si le clonage échoue.
     */
    @Override
    public Requin clone() {
        try {
            Requin copie = (Requin) super.clone();
            int newLigne;
            int newColonne;
            do {
                newLigne = 1 + Utils.random(nbLignes);
                newColonne = 1 + Utils.random(nbColonnes);
            } while (newLigne == ligne && newColonne == colonne);
            copie.setPosition(newLigne, newColonne);
            return copie;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clonage impossible");
        }
    }

}
