import java.util.*;

public class EvolutionProblem {

    private static char[] genes = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static String solution;
    private static int optimalFitness;
    private static HashMap<String, Integer> population;
    private static float mutationRate = 0.05F;
    private static float crossoverRate = 0.5F;



    public static void main(String[] args) {
        if (args.length < 1) {
            args = new String[1];
            args[0] = "Hello world";
        }
        run(args[0]);
    }

    private static void run(String arg) {
        solution = arg;
        optimalFitness = solution.length(); //require to guess the whole word
        // start GA
        int initialPopulation = 10;
        for (int i = 0; i < initialPopulation; i++) {
            String chromosome = randomGeneSet();
            population.put(chromosome, fitness(chromosome));
        }
        System.out.println("Created " + initialPopulation + "pseudorandom chromosomes as starting population.");
        population.forEach((chr, fit) -> System.out.println("Chromosome " + chr + " with fitness " + fit));
        int maxEpochs = 500;
        int ep = 1;
        while (ep < maxEpochs) {
            mutate(population);
            crossover(population);
            population = select(population);
            if (!solutionAchieved(population)) ep++;
        }
        System.out.println("Couldn't reach answer in " + maxEpochs + " epochs!");
    }

    private static void crossover(HashMap<String, Integer> population) {
        HashMap<String, Integer> kids = new HashMap<>();
        for (int i = 0; i < ((int) (population.size() * crossoverRate * 0.5)); i++) {
            String c1 = selectRandomChr(population);
            String c2 = String.valueOf(c1);
            while (c1.contentEquals(c2))
                c2 = selectRandomChr(population);
            String child = mixGametes(c1, c2);
            kids.put(child, fitness(child));
        }
        population.putAll(kids);
    }

    private static String mixGametes(String c1, String c2) {
        if (c1.length() != c2.length())
            throw new IllegalArgumentException("Crossing two not equally long chromosomes!");
        Random r = new Random();
        int pos = r.nextInt(c1.length());
        String[] gametes = new String[]{c1.substring(0, pos), c1.substring(pos),
                                        c2.substring(0, pos), c2.substring(pos)};
        String g1 = gametes[r.nextInt(gametes.length)];
        String g2 = String.valueOf(c1);
        while (g1.contentEquals(g2)) g2 = gametes[r.nextInt(gametes.length)];
        return g1 + g2;
    }

    private static String selectRandomChr(HashMap<String, Integer> population) {
        Random       random    = new Random();
        List<String> keys      = new ArrayList<String>(population.keySet());
        String       randomKey = keys.get( random.nextInt(keys.size()) );
        return randomKey;
    }

    private static void mutate(HashMap<String, Integer> population) {
        Random random = new Random();
        population.forEach((chr, val) -> {
            if (random.nextInt(100) // a 100% cointoss
                    < ((int) (mutationRate * 100))) {
                int pos = random.nextInt(chr.length()); // random position
                String mutatedChr = chr.substring(0, pos - 1) + genes[random.nextInt(genes.length)]
                        + chr.substring(pos);
                System.out.println("Mutated " + chr + " to " + mutatedChr);
                population.remove(chr);
                population.put(mutatedChr, fitness(mutatedChr));
            }
        });
    }




    private static String randomGeneSet() {
        String out = "";
        Random r = new Random();
        for (int i = 0; i < solution.length(); i++) {
            out += genes[r.nextInt(solution.length())];
        }
        return out;
    }

    private static int fitness(String chromosome) {
        int d = solution.length();
        for (int j = 0; j < chromosome.length(); j++) {
            if (solution.charAt(j) != chromosome.charAt(j)) d--;
        }
        return d;
    }
}
