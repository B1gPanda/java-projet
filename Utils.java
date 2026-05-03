/**
 * Classe utilitaire fournissant des méthodes mathématiques et de génération aléatoire.
 * Offre des fonctions pour générer des nombres aléatoires et limiter les valeurs.
 */
public class Utils {

    /**
     * Génère un nombre entier aléatoire entre 0 (inclus) et max (exclus).
     * @param max La limite supérieure (exclusive).
     * @return Un entier aléatoire dans [0, max[.
     */
    public static int random(int max) {
        return (int)(Math.random() * max);
    }

    /**
     * Limite une valeur entre un minimum et un maximum.
     * @param value La valeur à limiter.
     * @param min La limite inférieure.
     * @param max La limite supérieure.
     * @return La valeur limitée dans [min, max].
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}