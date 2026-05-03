/**
 * Exception levée lors d'erreurs dans la simulation.
 * Utilisée pour signaler des problèmes de validation ou d'exécution spécifiques à la simulation.
 */
public class SimulationException extends Exception {

    /**
     * Crée une SimulationException avec un message d'erreur.
     * @param message Le message décrivant l'erreur.
     */
    public SimulationException(String message) {
        super(message);
    }
}