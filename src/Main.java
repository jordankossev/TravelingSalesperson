import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static final int ITERATIONS = 1_000_000;
    private static final int POPULATION_SIZE = 10;
    private static final int DIMENSIONS = 2;
    private static final double WORLD_SIZE = 500;
    private static final double MUTATION_RATE = 0.45;
    private static int cities[];
    private static double distances[][];
    private static Chromosome generation[];

    private static double totalFitness;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();

        setup(n);
        for (int i = 0; i < 10_000; i++) {
            makeNewGeneration();
        }

//        setup(n);
    }

    private static void setup(int size) {
        cities = new int[size];
        generation = new Chromosome[POPULATION_SIZE];
        distances = new double[size][size];
        double citiesCoordinates[][] = new double[size][DIMENSIONS];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                citiesCoordinates[i][j] = new Random().nextDouble(0, WORLD_SIZE);
            }
        }

        calculateDistances(citiesCoordinates);
        populate();
    }

    private static void calculateDistances(double coordinates[][]) {
        for (int i = 0; i < distances.length; i++) {
            for (int j = i; j < distances.length; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                    continue;
                }

                double x1 = coordinates[i][0];
                double x2 = coordinates[j][0];
                double y1 = coordinates[i][1];
                double y2 = coordinates[j][1];
                double distance = Math.hypot(x1 - x2, y1 - y2);

                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }
    }

    private static void populate() {
        totalFitness = 0;
        Random rng = new Random();

        for (int i = 0; i < generation.length; i++) {
            int[] genes = new int[cities.length];

            for (int j = 0; j < genes.length; j++) {
                genes[j] = j;
            }
            // shuffling
            int a = 10;
            for (int j = 0; j < genes.length; j++) {
                int index = rng.nextInt(genes.length);

                // swap values
                int temp = genes[j];
                genes[j] = genes[index];
                genes[index] = temp;
            }

            // the lower the length of the route is, the higher the fitness score will be
            double fitnessScore = 1 / (calculateFitness(genes)); // it can't be 0 but just in case
            generation[i] = new Chromosome(genes, fitnessScore);
            totalFitness += fitnessScore;
        }
    }

    private static double calculateFitness(int[] route) {
        double sum = 0;
        for (int i = 0; i < route.length - 1; i++) {
            sum += distances[route[i]][route[i + 1]];
        }

        return sum;
    }

    private static void makeNewGeneration() {
        Chromosome[] newGeneration = new Chromosome[POPULATION_SIZE];
        double fittest = 0;
        double newTotalFitness = 0;
        for (int i = 0; i < generation.length; i++) {
            Chromosome parent1 = selectOne();
            Chromosome parent2 = selectOne();
            while (parent1 == parent2) {
                parent2 = selectOne();
            }

            newGeneration[i] = crossOverAndMutate(parent1, parent2);
            newTotalFitness += newGeneration[i].fitnessScore();
            if (newGeneration[i].fitnessScore() > fittest) {
                fittest = newGeneration[i].fitnessScore();
            }
        }

        totalFitness = newTotalFitness;
        generation = newGeneration;
        System.out.println("Fittest " + fittest * 10000);
    }

    private static Chromosome selectOne() {
        int i = 0;
        double r = new Random().nextDouble() * totalFitness;
        while (r > 0) {
            r -= generation[i].fitnessScore();
            i++;
        }
        i--;

        return generation[i];
    }

    private static Chromosome crossOverAndMutate(Chromosome parent1, Chromosome parent2) {
        Random rng = new Random();
        int start = rng.nextInt(parent1.genes().length);
        int end = rng.nextInt(start, parent1.genes().length);
        int[] childGenes = new int[parent1.genes().length];

        Set<Integer> used = new HashSet<>();
        int j = 0;
        for (int i = start; i <= end; i++) {
            childGenes[i - start] = parent1.genes()[i];
            used.add(childGenes[i - start]);
        }

        // parent 1: [...<p1 part>...]
        // parent 2: [...<p2 first>...<p2 second>]
        // child: [<p2 second> + <p1 part> + <p2 first>]
        int indexInChild = end - start + 1;
        for (int i = 0; i < parent2.genes().length; i++) {
            if (used.contains(parent2.genes()[i])) {
                continue;
            }
//            if (indexInChild == start) {
//                indexInChild = end + 1;
//            }
            if (indexInChild >= childGenes.length) {
                indexInChild = 0;
            }

            childGenes[indexInChild] = parent2.genes()[i];
            indexInChild++;
        }

        mutateGenes(childGenes, MUTATION_RATE);
        double fitnessScore = 1 / (calculateFitness(childGenes));
        return new Chromosome(childGenes, fitnessScore);
    }

    private static void mutateGenes(int[] genes, double mutationRate) {
        Random rng = new Random();
        double rand = rng.nextDouble();
        if (rand > mutationRate) {
            return;
        }

        int index1 = rng.nextInt(genes.length);
        int index2 = rng.nextInt(genes.length);
        while (index2 == index1) {
            index2 = rng.nextInt(genes.length);
        }

        // swap
        int temp = genes[index1];
        genes[index1] = genes[index2];
        genes[index2] = temp;
    }
}
