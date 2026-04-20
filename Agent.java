import java.util.List;
import java.util.Random;

/**
 * Classe abstraite représentant un agent mobile dans la simulation.
 * Les agents ont de l'énergie, une position sur la grille et peuvent se déplacer.
 */
public abstract class Agent implements Entite {
    protected static final Random RNG = new Random();
    protected final String type;
    protected int energie;
    protected int ligne;
    protected int colonne;
    protected final int nbLignes;
    protected final int nbColonnes;

    /**
     * Constructeur pour créer un agent.
     * @param type Le type de l'agent (ex: "Requin", "Bateau").
     * @param energie L'énergie initiale de l'agent.
     * @param ligne La ligne initiale sur la grille.
     * @param colonne La colonne initiale sur la grille.
     * @param nbLignes Le nombre total de lignes de la grille.
     * @param nbColonnes Le nombre total de colonnes de la grille.
     */
    public Agent(String type, int energie, int ligne, int colonne, int nbLignes, int nbColonnes) {
        this.type = type;
        this.energie = energie;
        this.ligne = Math.max(1, Math.min(nbLignes, ligne));
        this.colonne = Math.max(1, Math.min(nbColonnes, colonne));
        this.nbLignes = nbLignes;
        this.nbColonnes = nbColonnes;
    }

    /**
     * Retourne le type de l'agent.
     * @return Le type sous forme de chaîne.
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Retourne la ligne actuelle de l'agent.
     * @return La ligne.
     */
    @Override
    public int getLigne() {
        return ligne;
    }

    /**
     * Retourne la colonne actuelle de l'agent.
     * @return La colonne.
     */
    @Override
    public int getColonne() {
        return colonne;
    }

    /**
     * Définit la position de l'agent sur la grille, en s'assurant qu'elle reste dans les limites.
     * @param ligne La nouvelle ligne.
     * @param colonne La nouvelle colonne.
     */
    @Override
    public void setPosition(int ligne, int colonne) {
        this.ligne = Math.max(1, Math.min(nbLignes, ligne));
        this.colonne = Math.max(1, Math.min(nbColonnes, colonne));
    }

    /**
     * Calcule la distance de Manhattan entre la position actuelle et une case donnée.
     * @param lig La ligne cible.
     * @param col La colonne cible.
     * @return La distance.
     */
    protected int distance(int lig, int col) {
        return Math.abs(ligne - lig) + Math.abs(colonne - col);
    }

    /**
     * Calcule la distance de Manhattan entre cet agent et une autre entité.
     * @param other L'autre entité.
     * @return La distance.
     */
    protected int distance(Entite other) {
        return distance(other.getLigne(), other.getColonne());
    }

    /**
     * Déplace l'agent d'un pas vers la cible donnée.
     * @param targetLigne La ligne cible.
     * @param targetColonne La colonne cible.
     */
    protected void moveTowards(int targetLigne, int targetColonne) {
        int dL = Integer.compare(targetLigne, ligne);
        int dC = Integer.compare(targetColonne, colonne);
        setPosition(ligne + dL, colonne + dC);
    }

    /**
     * Déplace l'agent de manière aléatoire (dans une direction aléatoire ou reste sur place).
     */
    protected void moveRandomly() {
        int dL = RNG.nextInt(3) - 1;
        int dC = RNG.nextInt(3) - 1;
        setPosition(ligne + dL, colonne + dC);
    }

    /**
     * Vérifie si l'agent est encore en vie (énergie > 0).
     * @return true si vivant, false sinon.
     */
    public boolean isAlive() {
        return energie > 0;
    }

    /**
     * Méthode abstraite définissant l'action de l'agent à chaque tour.
     * @param terrain Le terrain de la simulation.
     * @param allAgents La liste de tous les agents.
     */
    public abstract void agir(Terrain terrain, List<Agent> allAgents);

    /**
     * Retourne une description textuelle de l'agent.
     * @return La description.
     */
    @Override
    public String description() {
        return type + "(" + ligne + "," + colonne + ") énergie=" + energie;
    }

    /**
     * Retourne la description de l'agent (même que description()).
     * @return La chaîne de description.
     */
    @Override
    public String toString() {
        return description();
    }
}
