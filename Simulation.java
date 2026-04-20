import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public Simulation() {
        terrain = new Terrain(NB_LIGNES, NB_COLONNES);
        agents = new ArrayList<>();
        initialiserRessources();
        initialiserAgents();
        terrain.verifierPositionRessources();
    }

    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        simulation.lancer();
    }

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

    private void initialiserAgents() {
        agents.add(new Requin(1 + RNG.nextInt(NB_LIGNES), 1 + RNG.nextInt(NB_COLONNES), NB_LIGNES, NB_COLONNES));
        agents.add(new Requin(1 + RNG.nextInt(NB_LIGNES), 1 + RNG.nextInt(NB_COLONNES), NB_LIGNES, NB_COLONNES));
        agents.add(new Bateau(1 + RNG.nextInt(NB_LIGNES), 1 + RNG.nextInt(NB_COLONNES), NB_LIGNES, NB_COLONNES));
    }

    private void lancer() {
        afficherEtat(0);
        attendre();
        for (int etape = 1; etape <= NOMBRE_ETAPES; etape++) {
            mettreAJourLesEtapes(etape);
            afficherEtat(etape);
            attendre();
        }
        afficherConclusion();
    }

    private void attendre() {
        try {
            Thread.sleep(TEMPS_PAUSE_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String coloriserAgent(Agent agent) {
        if (agent instanceof Requin) {
            return ANSI_RED + agent + ANSI_RESET;
        }
        if (agent instanceof Bateau) {
            return ANSI_BLUE + agent + ANSI_RESET;
        }
        return agent.toString();
    }

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
        gererNouvelleProductionDePoissons();
    }

    private void croitreLesPoissons() {
        for (Ressource r : terrain.lesRessources()) {
            if (r instanceof Poisson) {
                ((Poisson) r).croit();
            }
        }
    }

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

    private void appliquerEffetPlastique(Requin requin) {
        ResourceMarine ressource = ressourcesSurCase(requin.getLigne(), requin.getColonne());
        if (ressource instanceof SacPlastique) {
            requin.energie = Math.max(0, requin.energie - 10);
        }
    }

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

    private void pecherEtSauver(Bateau bateau) {
        ResourceMarine ressource = ressourcesSurCase(bateau.getLigne(), bateau.getColonne());
        if (ressource instanceof Poisson) {
            Poisson poisson = (Poisson) ressource;
            int prise = poisson.getQuantite();
            bateau.poissonsPrises(prise);
            poissonsPeches += prise;
            terrain.viderCase(ressource.getLigne(), ressource.getColonne());
        } else if (ressource instanceof HumainNaufrage) {
            bateau.humainSauve();
            humainsSauves++;
            terrain.viderCase(ressource.getLigne(), ressource.getColonne());
        }
    }

    private void resoudreConflitsRequinBateau() {
        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                Agent a = agents.get(i);
                Agent b = agents.get(j);
                if (a.isAlive() && b.isAlive() && memeCase(a, b)) {
                    if (a instanceof Requin && b instanceof Bateau) {
                        b.energie = Math.max(0, b.energie - 2);
                        a.energie = Math.max(0, a.energie - 3);
                    } else if (a instanceof Bateau && b instanceof Requin) {
                        a.energie = Math.max(0, a.energie - 2);
                        b.energie = Math.max(0, b.energie - 3);
                    } else if (a instanceof Requin && b instanceof Requin) {
                        a.energie = Math.max(0, a.energie - 2);
                        b.energie = Math.max(0, b.energie - 2);
                        if (a.energie == 0) {
                            b.energie += 5;
                        } else if (b.energie == 0) {
                            a.energie += 5;
                        }
                    }
                }
            }
        }
    }

    private void nettoyerMortEtRessources() {
        for (Agent agent : new ArrayList<>(agents)) {
            if (!agent.isAlive() && agent instanceof Bateau) {
                Bateau bateauMort = (Bateau) agent;
                poissonsPerdus = bateauMort.poissonsPris();
                humainsSauves -= bateauMort.getHumainsSauves();
                humainsManges += bateauMort.getHumainsSauves();
                poissonsPeches -= poissonsPerdus;
            }
        }
        agents.removeIf(agent -> !agent.isAlive());
        for (Ressource r : new ArrayList<>(terrain.lesRessources())) {
            if (r.getQuantite() <= 0) {
                terrain.viderCase(r.getLigne(), r.getColonne());
            }
        }
    }

    private boolean memeCase(Entite a, Entite b) {
        return a.getLigne() == b.getLigne() && a.getColonne() == b.getColonne();
    }

    private ResourceMarine ressourcesSurCase(int ligne, int colonne) {
        Ressource contenu = terrain.getCase(ligne, colonne);
        if (contenu instanceof ResourceMarine) {
            return (ResourceMarine) contenu;
        }
        return null;
    }

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

    private List<Agent> agentsSurCase(int lig, int col) {
        List<Agent> result = new ArrayList<>();
        for (Agent agent : agents) {
            if (agent.getLigne() == lig && agent.getColonne() == col) {
                result.add(agent);
            }
        }
        return result;
    }

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
     * Chaque étape a 50% de chance d'ajouter un groupe de 2-3 poissons.
     */
    private void gererNouvelleProductionDePoissons() {
        if (RNG.nextDouble() < 0.5) {
            Poisson poisson = new Poisson(2 + RNG.nextInt(2));
            placerRessourceAleatoire(poisson);
        }
    }

    private void afficherEtat(int etape) {
        System.out.println("\n--- Étape " + etape + " ---");
        afficherGrilleAgentsEtRessources();
        System.out.println("Agents en vie :");
        for (Agent agent : agents) {
            System.out.println("- " + coloriserAgent(agent));
        }
        System.out.println("Humains sauvés : " + humainsSauves + " | Humains mangés : " + humainsManges + " | Poissons pêchés : " + poissonsPeches + " | Poissons perdus : " + poissonsPerdus);
    }

    private void afficherConclusion() {
        System.out.println("\n--- Bilan final ---");
        System.out.println("Agents restants : " + agents.size());
        System.out.println("Humains sauvés : " + humainsSauves);
        System.out.println("Humains mangés : " + humainsManges);
        System.out.println("Poissons pêchés : " + poissonsPeches);
        System.out.println("Poissons perdus : " + poissonsPerdus);
        System.out.println("Ressources restantes : " + terrain.compterRessources());
        if humainsSauves > humainsManges && poissonsPeches > poissonsPerdus) {
            System.out.println("Bilan positif : les humains ont été majoritairement sauvés et les poissons pêchés dépassent les pertes.");
        } else if (humainsManges > humainsSauves && poissonsPerdus > poissonsPeches) {
            System.out.println("Bilan négatif : les humains ont été majoritairement mangés et les pertes de poissons dépassent les prises.");
        } else {
            System.out.println("Bilan mitigé : un équilibre fragile entre sauvetage, prédation, pêche et pertes.");
        }
    }
}
