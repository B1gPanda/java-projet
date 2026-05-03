public class SimulationManager {

    private static SimulationManager instance;

    private SimulationManager() {}

    public static SimulationManager getInstance() {
        if (instance == null) {
            instance = new SimulationManager();
        }
        return instance;
    }

    public void lancer(Simulation sim) {

        System.out.println("=== Lancement via SimulationManager ===");

        long debut = System.currentTimeMillis();

        sim.lancer();

        long fin = System.currentTimeMillis();

        System.out.println("Temps d'exécution : " + (fin - debut) + " ms");
    }
}