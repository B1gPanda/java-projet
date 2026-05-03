import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Classe principale de la simulation d'une scène de sauvetage en mer.
 * Gère la grille, les agents (requins et bateaux), les ressources marines
 * et les interactions entre tous ces éléments.
 */
public class Simulation {
    private static final int NB_LIGNES = 10;
    private static final int NB_COLONNES = 10;
    private static final int NOMBRE_ETAPES = 25;
    private static final int TEMPS_PAUSE_MS = 2000;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    private static final Random RNG = new Random();

    private final Terrain terrain;
    private final List<Agent> agents;
    private int humainsSauves = 0;
    private int humainsManges = 0;
    private int poissonsPeches = 0;
    private int poissonsPerdus = 0;
    private Bateau.Strategie strategieChoisie;

    /**
     * Crée et initialise une nouvelle simulation.
     * Initialise le terrain, sélectionne la stratégie, place les ressources et crée les agents.
     */
    public Simulation() {
        terrain = new Terrain(NB_LIGNES, NB_COLONNES);
        agents = new ArrayList<>();
        try {
            selectionnerStrategie();
        } catch (SimulationException e) {
            System.out.println(e.getMessage());
            strategieChoisie = Bateau.Strategie.PROTECTEUR;
        }
        initialiserRessources();
        initialiserAgents();
        terrain.verifierPositionRessources();
    }

    /**
     * Point d'entrée de l'application de simulation.
     * @param args Paramètres de ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        Simulation sim = new Simulation();
        SimulationManager.getInstance().lancer(sim);
    }

    /**
     * Demande à l'utilisateur de choisir la stratégie des bateaux.
     *
     * @throws SimulationException si le choix n'est pas valide.
     */
    private void selectionnerStrategie() throws SimulationException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Stratégie des bateaux ===");
        System.out.println("1. PROTECTEUR - Les bateaux sauvent en priorité les naufragés");
        System.out.println("2. PECHEUR - Les bateaux pêchent en priorité les poissons");
        System.out.println("3. CHASSEUR - Les bateaux chassent en priorité les requins");
        System.out.print("Choisissez (1, 2 ou 3, ou tapez 'protecteur', 'pecheur' ou 'chasseur') : ");

        String choix = scanner.nextLine().trim().toLowerCase();
        if (choix.equals("1") || choix.equals("protecteur")) {
            strategieChoisie = Bateau.Strategie.PROTECTEUR;
            System.out.println("=> Mode PROTECTEUR : Les bateaux à sauver les humains !");
        } else if (choix.equals("2") || choix.equals("pecheur")) {
            strategieChoisie = Bateau.Strategie.PECHEUR;
            System.out.println("=> Mode PÈCHEUR : Les bateaux en quête de poissons !");
        } else if (choix.equals("3") || choix.equals("chasseur")) {
            strategieChoisie = Bateau.Strategie.CHASSEUR;
            System.out.println("=> Mode CHASSEUR : Les bateaux en chasse des requins !");
        } else {
            scanner.close();
            throw new SimulationException("Choix invalide, mode PROTECTEUR par défaut.");
        }
        System.out.println();
        scanner.close();
    }

    /**
     * Initialise les ressources marines sur le terrain (poissons, humains, plastiques).
     */
    private void initialiserRessources() {
        for (int i = 0; i < 20; i++) {
            placerRessourceAleatoire(new Poisson(3 + RNG.nextInt(3)));
        }
        for (int i = 0; i < 3; i++) {
            placerRessourceAleatoire(new SacPlastique());
        }
        for (int i = 0; i < 6; i++) {
            placerRessourceAleatoire(new HumainNaufrage());
        }
    }

    /**
     * Initialise les agents de la simulation (requins et bateau).
     */
    private void initialiserAgents() {
        Requin r1 = new Requin(16, 1 + RNG.nextInt(NB_LIGNES), 1 + RNG.nextInt(NB_COLONNES), NB_LIGNES, NB_COLONNES);
        Requin r2 =  r1.clone();

        agents.add(r1);
        agents.add(r2);
        agents.add(new Bateau(50, 1 + RNG.nextInt(NB_LIGNES), 1 + RNG.nextInt(NB_COLONNES), NB_LIGNES, NB_COLONNES, strategieChoisie));
    }

    /**
     * Lance la simulation pour un nombre défini d'étapes.
     * Affiche l'état initial, puis exécute chaque étape avec un délai.
     */
    public void lancer() {
        afficherEtat(0);
        attendre();
        for (int etape = 1; etape <= NOMBRE_ETAPES; etape++) {
            mettreAJourLesEtapes(etape);
            afficherEtat(etape);
            attendre();
        }
        afficherConclusion();
    }

    /**
     * Met en pause la simulation pendant TEMPS_PAUSE_MS millisecondes.
     */
    private void attendre() {
        try {
            Thread.sleep(TEMPS_PAUSE_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Applique la couleur ANSI appropriée à un agent selon son type.
     * @param agent L'agent à coloriser.
     * @return La chaîne de l'agent avec codes ANSI de couleur.
     */
    private String coloriserAgent(Agent agent) {
        if (agent instanceof Requin) {
            return ANSI_RED + agent + ANSI_RESET;
        }
        if (agent instanceof Bateau) {
            return ANSI_BLUE + agent + ANSI_RESET;
        }
        return agent.toString();
    }

    /**
     * Effectue toutes les mises à jour pour une étape de la simulation.
     * @param etape Le numéro de l'étape en cours.
     */
    private void mettreAJourLesEtapes(int etape) {
        try {
            if (terrain.lesRessources().isEmpty()) {
                throw new SimulationException("Plus aucune ressource !");
            }
        } catch (SimulationException e) {
            System.out.println(e.getMessage());
            return; // ⛔ stop le tour
        }
        
        croitreLesPoissons();
        List<Agent> snapshot = new ArrayList<>(agents);
        for (Agent agent : snapshot) {
            if (agent.isAlive()) {
                agent.agir(terrain, snapshot);
            }
        }
        resoudreInteractions();
        nettoyerMortEtRessources();
        gererNouvelleProductionDePoissons();
    }

    /**
     * Fait croître les poissons en augmentant leur quantité.
     */
    private void croitreLesPoissons() {
        for (Ressource r : terrain.lesRessources()) {
            if (r instanceof Poisson) {
                ((Poisson) r).croit();
            }
        }
    }

    /**
     * Résout toutes les interactions entre les agents et les ressources de manière cyclique.
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
     * Applique l'effet du sac plastique sur l'énergie d'un requin.
     * @param requin Le requin affecté.
     */
    private void appliquerEffetPlastique(Requin requin) {
        ResourceMarine ressource = ressourcesSurCase(requin.getLigne(), requin.getColonne());
        if (ressource instanceof SacPlastique) {
            requin.energie = Math.max(0, requin.energie - 10);
        }
    }

    /**
     * Gère la consommation de ressources marines par un requin.
     * @param requin Le requin mangeant la ressource.
     */
    private void mangerRessource(Requin requin) {
        ResourceMarine ressource = ressourcesSurCase(requin.getLigne(), requin.getColonne());
        if (ressource instanceof Poisson) {
            Poisson poisson = (Poisson) ressource;
            int prise = poisson.getQuantite();
            poisson.setQuantite(0);
            requin.energie += 1;
            terrain.viderCase(ressource.getLigne(), ressource.getColonne());
        } else if (ressource instanceof HumainNaufrage) {
            requin.energie += 2;
            humainsManges++;
            terrain.viderCase(ressource.getLigne(), ressource.getColonne());
        }
    }

    /**
     * Gère la pêche et le sauvetage d'humains par un bateau.
     * @param bateau Le bateau effectuant les actions.
     */
    private void pecherEtSauver(Bateau bateau) {
        ResourceMarine ressource = ressourcesSurCase(bateau.getLigne(), bateau.getColonne());
        if (ressource instanceof Poisson) {
            Poisson poisson = (Poisson) ressource;
            int prise = poisson.getQuantite();
            bateau.poissonsPrises(prise);
            if (RNG.nextDouble() < 0.05) {
                poissonsPeches = Math.max(0, poissonsPeches - 10);
                poissonsPerdus += 10;
            } else {
                poissonsPeches += prise;
            }
            // Si bateau en mode pêche, donne plus d'énergie
            int energieBonus = (bateau.getStrategie() == Bateau.Strategie.PECHEUR) ? (prise + 2) : prise;
            bateau.energie = Math.min(50, bateau.energie + energieBonus);
            terrain.viderCase(ressource.getLigne(), ressource.getColonne());
        } else if (ressource instanceof HumainNaufrage) {
            bateau.humainSauve();
            humainsSauves++;
            terrain.viderCase(ressource.getLigne(), ressource.getColonne());
        }
    }

    /**
     * Résout les conflits entre requins et bateaux utilisant les positions communes.
     */
    private void resoudreConflitsRequinBateau() {
        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                Agent a = agents.get(i);
                Agent b = agents.get(j);
                if (a.isAlive() && b.isAlive() && memeCase(a, b)) {
                    if (a instanceof Requin && b instanceof Bateau) {
                        b.energie = Math.max(0, b.energie - 2);
                        a.energie = Math.max(0, a.energie - 3);
                        a.separer();
                        b.separer();
                    } else if (a instanceof Bateau && b instanceof Requin) {
                        a.energie = Math.max(0, a.energie - 2);
                        b.energie = Math.max(0, b.energie - 3);
                        a.separer();
                        b.separer();
                    } else if (a instanceof Requin && b instanceof Requin) {
                        a.energie = Math.max(0, a.energie - 2);
                        b.energie = Math.max(0, b.energie - 2);
                        if (a.energie == 0) {
                            b.energie += 5;
                        } else if (b.energie == 0) {
                            a.energie += 5;
                        }
                        a.separer();
                        b.separer();
                    }
                }
            }
        }
    }

    /**
     * Nettoie les morts et les ressources vides du terrain et de la liste des agents.
     */
    private void nettoyerMortEtRessources() {
        for (Agent agent : new ArrayList<>(agents)) {
            if (!agent.isAlive() && agent instanceof Bateau) {
                Bateau bateauMort = (Bateau) agent;
                poissonsPerdus = bateauMort.poissonsPris();
                humainsSauves -= bateauMort.getHumainsSauves();
                humainsManges += bateauMort.getHumainsSauves();
                poissonsPeches = Math.max(0, poissonsPeches - poissonsPerdus);
            }
        }
        agents.removeIf(agent -> !agent.isAlive());
        for (Ressource r : new ArrayList<>(terrain.lesRessources())) {
            if (r.getQuantite() <= 0) {
                terrain.viderCase(r.getLigne(), r.getColonne());
            }
        }
    }

    /**
     * Vérifie si deux entités occupent la même case sur la grille.
     * @param a La première entité.
     * @param b La deuxième entité.
     * @return true si elles sont sur la même case, false sinon.
     */
    private boolean memeCase(Entite a, Entite b) {
        return a.getLigne() == b.getLigne() && a.getColonne() == b.getColonne();
    }

    /**
     * Retourne la ressource marine à la position spécifiée, si elle existe.
     * @param ligne La ligne de la case.
     * @param colonne La colonne de la case.
     * @return La ressource marine ou null.
     */
    private ResourceMarine ressourcesSurCase(int ligne, int colonne) {
        Ressource contenu = terrain.getCase(ligne, colonne);
        if (contenu instanceof ResourceMarine) {
            return (ResourceMarine) contenu;
        }
        return null;
    }

    /**
     * Place une ressource aléatoirement sur le terrain en trouvant une case vide.
     * @param ressource La ressource à placer.
     */
    private void placerRessourceAleatoire(Ressource ressource) {
        while (true) {
            int lig = 1 + Utils.random(NB_LIGNES);
            int col = 1 + Utils.random(NB_COLONNES);
            if (terrain.caseEstVide(lig, col)) {
                ressource.setPosition(lig, col);
                terrain.setCase(lig, col, ressource);
                return;
            }
        }
    }

    /**
     * Affiche la grille du terrain avec les agents et ressources colorés.
     */
    private void afficherGrilleAgentsEtRessources() {
        System.out.println("+------+------+------+------+------+------+------+------+------+------+");
        for (int lig = 1; lig <= NB_LIGNES; lig++) {
            for (int col = 1; col <= NB_COLONNES; col++) {
                System.out.print("|");
            System.out.print(coloriserCellule(padderCellule(getCelluleTexte(lig, col)), lig, col));
        }
            System.out.println("|");
            System.out.println("+------+------+------+------+------+------+------+------+------+------+");
        }
    }

    /**
     * Formate un contenu de cellule en le complétant avec des espaces.
     * @param contenu Le contenu à formater.
     * @return Le contenu formaté sur largeur fixe.
     */
    private String padderCellule(String contenu) {
        int taille = 6;
        if (contenu.length() >= taille) {
            return contenu.substring(0, taille);
        }
        StringBuilder sb = new StringBuilder(contenu);
        while (sb.length() < taille) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Retourne le texte à afficher pour une cellule de la grille.
     * @param lig La ligne.
     * @param col La colonne.
     * @return Le texte de la cellule (agent et/ou ressource).
     */
    private String getCelluleTexte(int lig, int col) {
        StringBuilder affichage = new StringBuilder();
        Ressource ressource = terrain.getCase(lig, col);
        if (ressource instanceof ResourceMarine) {
            affichage.append(shortResource((ResourceMarine) ressource));
        }
        List<Agent> agentsCase = agentsSurCase(lig, col);
        if (!agentsCase.isEmpty()) {
            if (affichage.length() > 0) {
                affichage.append('/');
            }
            affichage.append(shortAgent(agentsCase));
        }
        if (affichage.length() == 0) {
            return "";
        }
        return affichage.toString();
    }

    /**
     * Retourne l'abréviation pour afficher une ressource marine.
     * @param ressource La ressource marine.
     * @return L'abréviation (ex: "Po" pour poisson).
     */
    private String shortResource(ResourceMarine ressource) {
        if (ressource instanceof Poisson) {
            return "Po";
        }
        if (ressource instanceof SacPlastique) {
            return "SP";
        }
        if (ressource instanceof HumainNaufrage) {
            return "HN";
        }
        return "Poule";
    }

    /**
     * Retourne la liste des agents situés sur une case précise.
     * @param lig La ligne.
     * @param col La colonne.
     * @return Liste des agents à cette position.
     */
    private List<Agent> agentsSurCase(int lig, int col) {
        List<Agent> result = new ArrayList<>();
        for (Agent agent : agents) {
            if (agent.getLigne() == lig && agent.getColonne() == col) {
                result.add(agent);
            }
        }
        return result;
    }

    /**
     * Applique la couleur ANSI appropriate à une cellule selon les agents présents.
     * @param contenu Le contenu de la cellule.
     * @param lig La ligne.
     * @param col La colonne.
     * @return La cellule avec ses codes ANSI de couleur.
     */
    private String coloriserCellule(String contenu, int lig, int col) {
        List<Agent> agentsCase = agentsSurCase(lig, col);
        if (agentsCase.isEmpty()) {
            return contenu;
        }
        String color;
        if (agentsCase.size() == 1) {
            Agent agent = agentsCase.get(0);
            if (agent instanceof Requin) {
                color = ANSI_RED;
            } else if (agent instanceof Bateau) {
                color = ANSI_BLUE;
            } else {
                color = ANSI_MAGENTA;
            }
        } else {
            color = ANSI_MAGENTA;
        }
        return color + contenu + ANSI_RESET;
    }

    /**
     * Retourne l'abréviation pour afficher un ensemble d'agents.
     * @param agentsCase La liste des agents sur une case.
     * @return L'abréviation (ex: "Bat" pour bateau, "Bagarre3" pour 3 agents).
     */
    private String shortAgent(List<Agent> agentsCase) {
        if (agentsCase.size() == 1) {
            Agent agent = agentsCase.get(0);
            if (agent instanceof Requin) {
                return "Rq";
            }
            if (agent instanceof Bateau) {
                return "Bat";
            }
            return "Ag";
        }
        return "Bagarre" + agentsCase.size();
    }

    /**
     * Ajoute aléatoirement de nouveaux poissons sur la grille pendant la simulation.
     * Chaque étape a 50% de chance d'ajouter un groupe de 2-4 poissons.
     */
    /**
     * Gère la génération aléatoire de nouveaux poissons chaque étape.
     * Avec 50% de probabilité, ajoute entre 2 et 4 poissons aléatoirement positionnés.
     */
    private void gererNouvelleProductionDePoissons() {
        if (Utils.random(100) < 50) {
            Poisson poisson = new Poisson(2 + Utils.random(3));
            placerRessourceAleatoire(poisson);
        }
    }

    /**
     * Affiche l'état complet de la simulation pour une étape donnée.
     * @param etape Le numéro de l'étape à afficher.
     */
    private void afficherEtat(int etape) {
        System.out.println("\n--- Étape " + etape + " ---");
        afficherGrilleAgentsEtRessources();
        System.out.println("Agents en vie :");
        for (Agent agent : agents) {
            System.out.println("- " + coloriserAgent(agent));
        }
        System.out.println("Humains sauvés : " + humainsSauves + " | Humains mangés : " + humainsManges + " | Poissons pêchés : " + poissonsPeches + " | Poissons perdus : " + poissonsPerdus);
    }

    /**
     * Affiche le bilan final de la simulation avec toutes les statistiques.
     */
    private void afficherConclusion() {
        System.out.println("\n--- Bilan final ---");
        System.out.println("Agents restants : " + agents.size());
        System.out.println("Humains sauvés : " + humainsSauves);
        System.out.println("Humains mangés : " + humainsManges);
        System.out.println("Poissons pêchés : " + poissonsPeches);
        System.out.println("Poissons perdus : " + poissonsPerdus);
        System.out.println("Ressources restantes : " + terrain.compterRessources());
        if (humainsSauves > humainsManges && poissonsPeches > poissonsPerdus) {
            System.out.println("Bilan positif : les humains ont été majoritairement sauvés et les poissons pêchés dépassent les pertes.");
        } else if (humainsManges > humainsSauves && poissonsPerdus > poissonsPeches) {
            System.out.println("Bilan négatif : les humains ont été majoritairement mangés et les pertes de poissons dépassent les prises.");
        } else {
            System.out.println("Bilan mitigé : un équilibre fragile entre sauvetage, prédation, pêche et pertes.");
        }
    }
}
