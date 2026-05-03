/**
 * Gestionnaire de simulation utilisant le pattern Singleton.
 * Responsable du lancement et du suivi des simulations, y compris la mesure du temps d'exécution.
 */
public class SimulationManager {

    private static SimulationManager instance;

    /**
     * Constructeur privé pour empêcher l'instanciation directe (Singleton).
     */
    private SimulationManager() {}

    /**
     * Retourne l'instance unique du SimulationManager (Singleton).
     * @return L'instance unique de SimulationManager.
     */
    public static SimulationManager getInstance() {
        if (instance == null) {
            instance = new SimulationManager();
        }
        return instance;
    }

    /**
     * Lance une simulation et affiche le temps d'exécution.
     * @param sim La simulation à lancer.
     */
    public void lancer(Simulation sim) {

        System.out.println("=== Lancement via SimulationManager ===");

        long debut = System.currentTimeMillis();

        sim.lancer();

        long fin = System.currentTimeMillis();

        System.out.println("Temps d'exécution : " + (fin - debut) + " ms");
    }
}