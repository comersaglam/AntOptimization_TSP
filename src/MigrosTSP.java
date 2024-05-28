/**
 * This program is an AngryBullet game. just like AngryBirds.
 * @author Celaleddin Ömer Sağlam, Student ID:2023400348
 * @since Date: 13.05.2024
 */

import java.util.*;
import java.util.stream.IntStream;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * This class implements two approaches to solve the Traveling Salesman Problem (TSP):
 * Brute Force and Ant Colony Optimization (ACO).
 */
public class MigrosTSP {
    // Matrices to store distances and pheromone levels between nodes
    private static double[][] edgeMatrixDistance;
    private static double[][] edgeMatrixPheromone;

    // Variables to track the shortest path found
    private static double minDistance = Double.MAX_VALUE;
    private static int[] bestPath;

    // ACO parameters
    private static final int iterationCount = 100;
    private static final int antCount = 50;
    private static final double alpha = 0.8; // Importance of pheromone in path selection
    private static final double beta = 5; // Importance of distance in path selection
    private static final double evaporation = 0.9; // Rate at which pheromone evaporates
    private static final double Q = 0.1; // Pheromone deposit factor

    // Choosing the method to solve the TSP
    private static int chosenMethod = 1; // 1 for ACO, 0 for brute force

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        List<Pair> nodes = null;
        try {
            nodes = readTXT("input05.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        edgeMatrixDistance = createDistanceMatrix(nodes);
        edgeMatrixPheromone = createPheromoneMatrix(nodes);

        if (chosenMethod == 0) {
            bestPath = bruteForceTSP(nodes);
        } else if (chosenMethod == 1) {
            bestPath = antColonyOptimization();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time to calculate path is: " + (endTime - startTime) / 1000.0 + " seconds");

        drawGraph(nodes, edgeMatrixDistance, edgeMatrixPheromone, bestPath);
    }

    /**
     * Draws the graph of nodes, edges, and path using StdDraw.
     * @param nodes List of node locations (pairs of coordinates).
     * @param edgeMatrixDistance Matrix of distances between each pair of nodes.
     * @param edgeMatrixPheromone Matrix of pheromone levels between each pair of nodes.
     * @param path Array representing the best path found.
     */
    public static void drawGraph(List<Pair> nodes, double[][] edgeMatrixDistance, double[][] edgeMatrixPheromone, int[] path) {
        int canvasHeight = 700;
        int canvasWidth = 1400;
        StdDraw.setCanvasSize(canvasWidth, canvasHeight);
        StdDraw.setXscale(0, canvasWidth);
        StdDraw.setYscale(0, canvasHeight);
        StdDraw.enableDoubleBuffering();

        // Draw edges with varying thickness based on pheromone levels
        StdDraw.setPenColor(StdDraw.BLACK);
        for (int i = 0; i < edgeMatrixPheromone.length; i++) {
            for (int j = i + 1; j < edgeMatrixPheromone[i].length; j++) {
                double thickness = edgeMatrixPheromone[i][j] / 500.0;  // Scale pheromone visualization
                StdDraw.setPenRadius(thickness);
                double scaledX1 = nodes.get(i).getX() * canvasWidth;
                double scaledY1 = nodes.get(i).getY() * canvasHeight;
                double scaledX2 = nodes.get(j).getX() * canvasWidth;
                double scaledY2 = nodes.get(j).getY() * canvasHeight;
                StdDraw.line(scaledX1, scaledY1, scaledX2, scaledY2);
            }
        }

        // Highlight the best path found in blue
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.setPenRadius(0.008);
        for (int i = 0; i < path.length - 1; i++) {
            double scaledX1 = nodes.get(path[i]).getX() * canvasWidth;
            double scaledY1 = nodes.get(path[i]).getY() * canvasHeight;
            double scaledX2 = nodes.get(path[i + 1]).getX() * canvasWidth;
            double scaledY2 = nodes.get(path[i + 1]).getY() * canvasHeight;
            StdDraw.line(scaledX1, scaledY1, scaledX2, scaledY2);
        }

        // Draw nodes, with the starting node in orange and others in gray
        for (int i = 0; i < nodes.size(); i++) {
            Pair node = nodes.get(i);
            double scaledX = node.getX() * canvasWidth;
            double scaledY = node.getY() * canvasHeight;

            StdDraw.setPenColor(i == 0 ? StdDraw.ORANGE : StdDraw.GRAY);
            StdDraw.filledCircle(scaledX, scaledY, 20);
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.text(scaledX, scaledY, String.valueOf(i));
        }

        StdDraw.show();
        StdDraw.setPenRadius();
    }

    /**
     * Executes the Ant Colony Optimization (ACO) algorithm to find the shortest path for the TSP problem.
     * @return An array of integers representing the best path found.
     */
    public static int[] antColonyOptimization(){
        double bestPathLengthOverall = Double.MAX_VALUE;
        ArrayList<Integer> bestPathTakenOverall = new ArrayList<>();

        int nodeCount = edgeMatrixPheromone.length; // Number of nodes in the graph
        List<Ant> ants = new ArrayList<>();

        // Loop through each iteration as specified by iterationCount
        for (int i = 0; i < iterationCount; i++) {
            ants.clear(); // Clear previous list of ants
            // Create and process each ant in the colony
            for (int j = 0; j < antCount; j++) {
                Ant myAnt = new Ant(0, nodeCount); 
                while (!myAnt.allNodesVisited()) { 
                    myAnt.selectNextNode(edgeMatrixPheromone, edgeMatrixDistance, alpha, beta);
                }
                myAnt.moveToNextNode(0, edgeMatrixDistance); // Move ant back to the starting node
                ants.add(myAnt);
            }

            // Pheromone update phase
            for (Ant ant : ants) {
                ant.updatePheromones(edgeMatrixPheromone, Q, edgeMatrixDistance);
            }

            // Evaporation of pheromones
            for (int j = 0; j < nodeCount; j++) {
                for (int k = 0; k < nodeCount; k++) {
                    edgeMatrixPheromone[j][k] *= evaporation;
                }
            }

            // Check if the newly found path is the best one
            ArrayList<Integer> bestPathTaken = findBestPath(ants);
            double bestPathLength = calculateTotalDistance(bestPathTaken.stream().mapToInt(Integer::intValue).toArray());

            if (bestPathLength < bestPathLengthOverall) {
                bestPathLengthOverall = bestPathLength;
                bestPathTakenOverall = new ArrayList<>(bestPathTaken);
            }
        }
        System.out.println("" + bestPathLengthOverall);
        return bestPathTakenOverall.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Implements the brute force approach to solving the Traveling Salesman Problem.
     * @param nodes A list of node locations used for generating permutations of paths.
     * @return An array representing the shortest path found.
     */
    public static int[] bruteForceTSP(List<Pair> nodes) {
        int[] numbers = IntStream.rangeClosed(1, nodes.size() - 1).toArray();
        int[] path = new int[numbers.length + 2];
        path[0] = 0;
        path[path.length - 1] = 0;
        generatePermutations(numbers, 0, path);

        System.out.println("Brute force Method:");
        System.out.println("Shortest Distance: " + minDistance);
        System.out.println("Shortest Path: " + Arrays.toString(bestPath));
        return bestPath;
    }

    /**
     * Identifies the best path taken by any ant in a given iteration.
     * @param ants A list of ants after completing their path finding.
     * @return An ArrayList representing the path of the shortest route found.
     */
    public static ArrayList<Integer> findBestPath(List<Ant> ants) {
        double bestPathLength = Double.MAX_VALUE;
        ArrayList<Integer> bestPathTaken = new ArrayList<>();
        for (Ant ant : ants) {
            if (ant.getPathLength() < bestPathLength) {
                bestPathLength = ant.getPathLength();
                bestPathTaken = ant.getPathTaken();
            }
        }
        return bestPathTaken;
    }

    /**
     * Creates a matrix of pheromone levels for each edge between nodes, initializing with a default value.
     * @param nodes A list of nodes used in the TSP.
     * @return A 2D array of doubles representing the initial pheromone levels.
     */
    public static double[][] createPheromoneMatrix(List<Pair> nodes) {
        double[][] edgeMatrixPheromone = new double[nodes.size()][nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            Arrays.fill(edgeMatrixPheromone[i], 1.0);
        }
        return edgeMatrixPheromone;
    }

    /**
     * Creates a matrix of distances between each pair of nodes.
     * @param nodes A list of nodes used in the TSP.
     * @return A 2D array of doubles representing the distances between each node pair.
     */
    public static double[][] createDistanceMatrix(List<Pair> nodes) {
        double[][] edgeMatrixDistance = new double[nodes.size()][nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                edgeMatrixDistance[i][j] = calculateDistance(nodes.get(i), nodes.get(j));
            }
        }
        return edgeMatrixDistance;
    }

    /**
     * Calculates the total distance of a given path.
     * @param path An array of integers representing a path through nodes.
     * @return The total distance traveled in the given path.
     */
    public static double calculateTotalDistance(int[] path) {
        double distance = 0;
        for (int i = 0; i < path.length - 1; i++) {
            distance += edgeMatrixDistance[path[i]][path[i + 1]];
        }
        return distance;
    }

    /**
     * Reads a file to extract node coordinates, returning a list of node pairs.
     * @param txtname The name of the file to read from.
     * @return A List of Pair objects representing node coordinates.
     * @throws FileNotFoundException If the specified file is not found.
     */
    public static List<Pair> readTXT(String txtname) throws FileNotFoundException {
        List<Pair> nodes = new ArrayList<>();
        File file = new File(txtname);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(",");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            nodes.add(new Pair(x, y));
        }
        scanner.close();
        return nodes;
    }

    /**
     * Calculates the Euclidean distance between two nodes.
     * @param node1 The first node.
     * @param node2 The second node.
     * @return The distance between node1 and node2.
     */
    public static double calculateDistance(Pair node1, Pair node2){
        double xDistance = Math.abs(node1.getX() - node2.getX());
        double yDistance = Math.abs(node1.getY() - node2.getY());
        return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
    }

    /**
     * Recursive method to generate all permutations of path configurations.
     * @param numbers An array of integers representing node indices.
     * @param index The current index to consider for swapping.
     * @param path The current path configuration being built.
     */
    private static void generatePermutations(int[] numbers, int index, int[] path) {
        if (index == numbers.length) {
            System.arraycopy(numbers, 0, path, 1, numbers.length);
            double currentDistance = calculateTotalDistance(path);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                bestPath = Arrays.copyOf(path, path.length);
            }
        } else {
            for (int i = index; i < numbers.length; i++) {
                swap(numbers, index, i);
                generatePermutations(numbers, index + 1, path);
                swap(numbers, index, i); // Backtrack to try another permutation
            }
        }
    }

    /**
     * Swaps two elements in an array of integers.
     * @param numbers The array containing the elements to swap.
     * @param i The index of the first element.
     * @param j The index of the second element.
     */
    private static void swap(int[] numbers, int i, int j) {
        int temp = numbers[i];
        numbers[i] = numbers[j];
        numbers[j] = temp;
    }
}