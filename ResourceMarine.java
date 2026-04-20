/**
 * Classe abstraite pour les ressources marines, étendant Ressource et implémentant Entity.
 * Permet aux ressources d'avoir une position sur la grille.
 */
public abstract class ResourceMarine extends Ressource implements Entity {
    /**
     * Constructeur pour créer une ressource marine.
     * @param type Le type de la ressource (ex: "Poisson").
     * @param quantite La quantité initiale.
     */
    public ResourceMarine(String type, int quantite) {
        super(type, quantite);
    }

    /**
     * Retourne la ligne de la ressource.
     * @return La ligne.
     */
    @Override
    public int getLigne() {
        return super.getLigne();
    }

    /**
     * Retourne la colonne de la ressource.
     * @return La colonne.
     */
    @Override
    public int getColonne() {
        return super.getColonne();
    }

    /**
     * Définit la position de la ressource.
     * @param ligne La nouvelle ligne.
     * @param colonne La nouvelle colonne.
     */
    @Override
    public void setPosition(int ligne, int colonne) {
        super.setPosition(ligne, colonne);
    }

    /**
     * Retourne le type de la ressource.
     * @return Le type.
     */
    @Override
    public String getType() {
        return super.type;
    }

    /**
     * Retourne une description de la ressource.
     * @return La description.
     */
    @Override
    public String description() {
        return getType() + "(" + getQuantite() + ") @" + getLigne() + "," + getColonne();
    }

    /**
     * Retourne la description de la ressource.
     * @return La chaîne de description.
     */
    @Override
    public String toString() {
        return description();
    }
}
