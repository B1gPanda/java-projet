/**
 * Interface pour les entités positionnables sur la grille de la simulation.
 * Définit les méthodes de base pour obtenir et définir la position, le type et une description.
 */
public interface Entite {
    /**
     * Retourne la ligne actuelle de l'entité.
     * @return La ligne.
     */
    int getLigne();

    /**
     * Retourne la colonne actuelle de l'entité.
     * @return La colonne.
     */
    int getColonne();

    /**
     * Retourne le type de l'entité.
     * @return Le type sous forme de chaîne.
     */
    String getType();

    /**
     * Définit la position de l'entité.
     * @param ligne La nouvelle ligne.
     * @param colonne La nouvelle colonne.
     */
    void setPosition(int ligne, int colonne);

    /**
     * Retourne une description textuelle de l'entité.
     * @return La description.
     */
    String description();
}
