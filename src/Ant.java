import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents an ant used in the Ant Colony Optimization algorithm.
 * An ant simulates a path through a graph, collecting path lengths and contributing to the pheromone levels.
 */
public class Ant {
    private int currentPosition;  // Current position of the ant on the graph
    private ArrayList<Integer> pathTaken;  // List of nodes visited in order
    private double pathLength;  // Total distance of the path taken
    private boolean[] visited;  // Tracks whether each node has been visited
    private int startNode;  // The starting node of the path

    /**
     * Constructs an Ant with a specified start node and the total number of nodes.
     * @param startNode the starting node index for the ant
     * @param nodeCount the total number of nodes in the graph
     */
    public Ant(int startNode, int nodeCount) {
        this.startNode = startNode;
        this.currentPosition = startNode;
        this.pathTaken = new ArrayList<>();
        this.pathTaken.add(startNode);
        this.visited = new boolean[nodeCount];
        this.visited[startNode] = true;
        this.pathLength = 0;
    }

    /**
     * Checks if all nodes have been visited by the ant.
     * @return true if all nodes have been visited, false otherwise
     */
    public boolean allNodesVisited() {
        for (int i = 1; i < visited.length; i++) {
            if (!visited[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Selects the next node to visit based on pheromone levels and distances using a probabilistic approach.
     * @param pheromoneMatrix matrix representing pheromone levels between nodes
     * @param distanceMatrix matrix representing distances between nodes
     * @param alpha controls the influence of pheromone in probability calculation
     * @param beta controls the influence of distance in probability calculation
     */
    public void selectNextNode(double[][] pheromoneMatrix, double[][] distanceMatrix, double alpha, double beta) {
        double[] probabilities = new double[visited.length];
        double probabilitySum = 0;

        // Calculate probability for each non-visited node
        for (int i = 0; i < probabilities.length; i++) {
            if (!visited[i]) {
                probabilities[i] = Math.pow(pheromoneMatrix[currentPosition][i], alpha) *
                                   Math.pow(1.0 / distanceMatrix[currentPosition][i], beta);
                probabilitySum += probabilities[i];
            }
        }

        // Normalize probabilities
        for (int i = 0; i < probabilities.length; i++) {
            if (!visited[i]) {
                probabilities[i] /= probabilitySum;
            }
        }

        // Choose next node based on probabilities
        double randomChoice = Math.random();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulativeProbability += probabilities[i];
            if (randomChoice <= cumulativeProbability) {
                moveToNextNode(i, distanceMatrix);
                break;
            }
        }
    }

    /**
     * Moves the ant to the next node and updates the path length.
     * @param nextNode the next node to move to
     * @param distanceMatrix matrix representing distances between nodes
     */
    public void moveToNextNode(int nextNode, double[][] distanceMatrix) {
        pathTaken.add(nextNode);
        visited[nextNode] = true;
        currentPosition = nextNode;
        pathLength += distanceMatrix[currentPosition][nextNode];
    }

    /**
     * Calculates the total distance for a given path.
     * @param pathTaken list of node indices that form the path
     * @param distanceMatrix matrix of distances between nodes
     * @return the total distance of the path
     */
    public double calculateTotalPathDistance(ArrayList<Integer> pathTaken, double[][] distanceMatrix) {
        double totalDistance = 0;
        for (int i = 0; i < pathTaken.size() - 1; i++) {
            totalDistance += distanceMatrix[pathTaken.get(i)][pathTaken.get(i + 1)];
        }
        return totalDistance;
    }

    /**
     * Updates pheromone levels on the paths taken by the ant.
     * @param pheromoneMatrix matrix representing pheromone levels between nodes
     * @param Q the amount of pheromone to deposit
     * @param distanceMatrix matrix of distances between nodes
     */
    public void updatePheromones(double[][] pheromoneMatrix, double Q, double[][] distanceMatrix) {
        double totalCycleDistance = calculateTotalPathDistance(pathTaken, distanceMatrix);
        double delta = Q / totalCycleDistance;

        // Apply pheromone deposit symmetrically for each edge in the path
        for (int i = 0; i < pathTaken.size() - 1; i++) {
            int node1 = pathTaken.get(i);
            int node2 = pathTaken.get(i + 1);
            pheromoneMatrix[node1][node2] += delta;
            pheromoneMatrix[node2][node1] += delta;
        }
    }

    /**
     * Retrieves the total path length traveled by the ant.
     * This is the sum of the distances of all edges the ant has traversed in its current tour.
     * 
     * @return The total distance of the path taken by the ant.
     */
    public double getPathLength() {
        return pathLength;
    }

    /**
     * Returns the path taken by the ant.
     * @return a list of node indices representing the path taken
     */
    public ArrayList<Integer> getPathTaken() {
        return pathTaken;
    }

    /**
     * Returns a string representation of the ant's path and path length.
     * @return a string describing the ant's current path and length
     */
    @Override
    public String toString() {
        return "Ant{" +
                "pathTaken=" + pathTaken +
                ", pathLength=" + pathLength +
                '}';
    }
}

