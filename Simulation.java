import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe principale de la simulation d'écosystème marin.
 * Gère l'initialisation, les étapes de simulation et l'affichage.
 */
public class Simulation {
    private static final int NB_LIGNES = 10;
    private static final int NB_COLONNES = 10;
    private static final int NOMBRE_ETAPES = 12;
    private static final Random RNG = new Random();
    private final Terrain terrain;
    private final List<Agent> agents;
    private int humainsSauves = 0;
    private int poissonsPeches = 0;
    private int humainsManges = 0;

    /**
     * Constructeur de la simulation : initialise le terrain, les ressources et les agents.
     */
    public Simulation() {
        terrain = new Terrain(NB_LIGNES, NB_COLONNES);
        agents = new ArrayList<>();
        initialiserRessources();
        initialiserAgents();
        terrain.verifierPositionRessources();
    }

    /**
     * Méthode principale pour lancer la simulation.
     * @param args Arguments de ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        simulation.lancer();
    }

    /**
     * Initialise les ressources sur le terrain : poissons, sacs de plastique, humains naufragés.
     */
    private void initialiserRessources() {
        for (int i = 0; i < 20; i++) {
            placerRessourceAleatoire(new Poisson(3 + RNG.nextInt(3)));
        }
        for (int i = 0; i < 2; i++) {
            placerRessourceAleatoire(new SacPlastique());
        }
        for (int i = 0; i < 6; i++) {
            placerRessourceAleatoire(new HumainNaufrage());
        }
    }

    /**
     * Initialise les agents : requins et bateaux à positions aléatoires.
     */
    private void initialiserAgents() {
        agents.add(new Requin(1 + RNG.nextInt(NB_LIGNES), 1 + RNG.nextInt(NB_COLONNES), NB_LIGNES, NB_COLONNES));
        agents.add(new Requin(1 + RNG.nextInt(NB_LIGNES), 1 + RNG.nextInt(NB_COLONNES), NB_LIGNES, NB_COLONNES));
        agents.add(new Bateau(1 + RNG.nextInt(NB_LIGNES), 1 + RNG.nextInt(NB_COLONNES), NB_LIGNES, NB_COLONNES));
    }

    /**
     * Lance la simulation : affiche l'état initial, boucle sur les étapes, affiche la conclusion.
     */
    private void lancer() {
        afficherEtat(0);
        for (int etape = 1; etape <= NOMBRE_ETAPES; etape++) {
            mettreAJourLesEtapes(etape);
            afficherEtat(etape);
        }
        afficherConclusion();
    }

    /**
     * Met à jour les étapes de simulation : croissance des poissons, actions des agents, interactions, nettoyage.
     * @param etape Le numéro de l'étape actuelle.
     */
    private void mettreAJourLesEtapes(int etape) {
        croitreLesPoissons();
        List<Agent> snapshot = new ArrayList<>(agents);
        for (Agent agent : snapshot) {
            if (agent.isAlive()) {
                agent.agir(terrain, snapshot);
            }
        }
        resoudreInteractions();
        nettoyerMortEtRessources();
    }

    /**
     * Fait croître tous les poissons sur le terrain.
     */
    private void croitreLesPoissons() {
        for (Ressource r : terrain.lesRessources()) {
            if (r instanceof Poisson) {
                ((Poisson) r).croit();
            }
        }
    }


    /**
     * Résout les interactions entre agents et ressources.
     */
    private void resoudreInteractions() {
        for (Agent agent : new ArrayList<>(agents)) {
            if (!agent.isAlive()) {
                continue;
            }
            if (agent instanceof Requin) {
                appliquerEffetPlastique((Requin) agent);
                mangerRessource((Requin) agent);
            }
            if (agent instanceof Bateau) {
                pecherEtSauver((Bateau) agent);
            }
        }
        resoudreConflitsRequinBateau();
    }

    /**
     * Applique l'effet du plastique sur le requin : perte d'énergie.
     * @param requin Le requin affecté.
     */
    private void appliquerEffetPlastique(Requin requin) {
        ResourceMarine ressource = ressourcesSurCase(requin.getLigne(), requin.getColonne());
        if (ressource instanceof SacPlastique) {
            requin.energie = Math.max(0, requin.energie - 10);
        }
    }

    /**
     * Permet au requin de manger une ressource sur sa case.
     * @param requin Le requin qui mange.
     */
    private void mangerRessource(Requin requin) {
        ResourceMarine ressource = ressourcesSurCase(requin.getLigne(), requin.getColonne());
        if (ressource instanceof Poisson) {
            Poisson poisson = (Poisson) ressource;
            int prise = Math.min(2, poisson.getQuantite());
            poisson.setQuantite(poisson.getQuantite() - prise);
            requin.energie += prise;
        } else if (ressource instanceof HumainNaufrage) {
            requin.energie += 6;
            humainsManges++;
            terrain.viderCase(ressource.getLigne(), ressource.getColonne());
        }
    }

    /**
     * Permet au bateau de pêcher ou sauver sur sa case.
     * @param bateau Le bateau qui agit.
     */
    private void pecherEtSauver(Bateau bateau) {
        ResourceMarine ressource = ressourcesSurCase(bateau.getLigne(), bateau.getColonne());
        if (ressource instanceof Poisson) {
            Poisson poisson = (Poisson) ressource;
            int prise = Math.min(2, poisson.getQuantite());
            poisson.setQuantite(poisson.getQuantite() - prise);
            bateau.poissonsPrises(prise);
            poissonsPeches += prise;
        } else if (ressource instanceof HumainNaufrage) {
            bateau.humainSauve();
            humainsSauves++;
            terrain.viderCase(ressource.getLigne(), ressource.getColonne());
        }
    }

    /**
     * Résout les conflits entre requins et bateaux sur la même case.
     */
    private void resoudreConflitsRequinBateau() {
        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                Agent a = agents.get(i);
                Agent b = agents.get(j);
                if (a.isAlive() && b.isAlive() && memeCase(a, b)) {
                    if (a instanceof Requin && b instanceof Bateau) {
                        combattre((Requin) a, (Bateau) b);
                    } else if (a instanceof Bateau && b instanceof Requin) {
                        combattre((Requin) b, (Bateau) a);
                    }
                }
            }
        }
    }

    /**
     * Gère le combat entre un requin et un bateau.
     * @param requin Le requin.
     * @param bateau Le bateau.
     */
    private void combattre(Requin requin, Bateau bateau) {
        if (bateau.energie >= requin.energie) {
            requin.energie = Math.max(0, requin.energie - 1);
            bateau.energie = Math.max(0, bateau.energie - 2);
        } else {
            bateau.energie = 0;
        }
    }

    /**
     * Nettoie les agents morts et les ressources épuisées.
     */
    private void nettoyerMortEtRessources() {
        agents.removeIf(agent -> !agent.isAlive());
        for (Ressource r : new ArrayList<>(terrain.lesRessources())) {
            if (r.getQuantite() <= 0) {
                terrain.viderCase(r.getLigne(), r.getColonne());
            }
        }
    }

    /**
     * Vérifie si deux entités sont sur la même case.
     * @param a Première entité.
     * @param b Seconde entité.
     * @return true si même case.
     */
    private boolean memeCase(Entite a, Entite b) {
        return a.getLigne() == b.getLigne() && a.getColonne() == b.getColonne();
    }

    /**
     * Retourne la ressource marine sur une case donnée.
     * @param ligne La ligne.
     * @param colonne La colonne.
     * @return La ressource, ou null.
     */
    private ResourceMarine ressourcesSurCase(int ligne, int colonne) {
        Ressource contenu = terrain.getCase(ligne, colonne);
        if (contenu instanceof ResourceMarine) {
            return (ResourceMarine) contenu;
        }
        return null;
    }

    /**
     * Place une ressource aléatoirement sur une case vide.
     * @param ressource La ressource à placer.
     */
    private void placerRessourceAleatoire(Ressource ressource) {
        while (true) {
            int lig = 1 + RNG.nextInt(NB_LIGNES);
            int col = 1 + RNG.nextInt(NB_COLONNES);
            if (terrain.caseEstVide(lig, col)) {
                ressource.setPosition(lig, col);
                terrain.setCase(lig, col, ressource);
                return;
            }
        }
    }

    /**
     * Affiche l'état de la simulation à une étape donnée.
     * @param etape Le numéro de l'étape.
     */
    private void afficherEtat(int etape) {
        System.out.println("\n--- Étape " + etape + " ---");
        terrain.afficher(6);
        System.out.println("Agents en vie :");
        for (Agent agent : agents) {
            System.out.println("- " + agent);
        }
        System.out.println("Humains sauvés : " + humainsSauves + " | Humains mangés : " + humainsManges + " | Poissons pêchés : " + poissonsPeches);
    }

    /**
     * Affiche le bilan final de la simulation.
     */
    private void afficherConclusion() {
        System.out.println("\n--- Bilan final ---");
        System.out.println("Agents restants : " + agents.size());
        System.out.println("Humains sauvés : " + humainsSauves);
        System.out.println("Humains mangés : " + humainsManges);
        System.out.println("Poissons pêchés : " + poissonsPeches);
        System.out.println("Ressources restantes : " + terrain.compterRessources());
    }
}

