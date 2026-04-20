/**
 * Classe représentant un poisson, ressource marine qui peut croître.
 */
public class Poisson extends ResourceMarine {
    /**
     * Constructeur pour créer un poisson.
     * @param quantite La quantité initiale de poissons.
     */
    public Poisson(int quantite) {
        super("Poisson", quantite);
    }

    /**
     * Fait croître le poisson en augmentant sa quantité de 1.
     */
    public void croit() {
        setQuantite(getQuantite() + 1);
    }
}
