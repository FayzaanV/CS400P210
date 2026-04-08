import java.util.PriorityQueue;
import java.util.List;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class extends the BaseGraph data structure with additional methods for
 * computing the total cost and list of node data along the shortest path
 * connecting a provided starting to ending nodes. This class makes use of
 * Dijkstra's shortest path algorithm.
 */
public class DijkstraGraph<NodeType, EdgeType extends Number>
        extends BaseGraph<NodeType, EdgeType>
        implements GraphADT<NodeType, EdgeType> {

    /**
     * While searching for the shortest path between two nodes, a SearchNode
     * contains data about one specific path between the start node and another
     * node in the graph. The final node in this path is stored in its node
     * field. The total cost of this path is stored in its cost field. And the
     * predecessor SearchNode within this path is referenced by the predecessor
     * field (this field is null within the SearchNode containing the starting
     * node in its node field).
     *
     * SearchNodes are Comparable and are sorted by cost so that the lowest cost
     * SearchNode has the highest priority within a java.util.PriorityQueue.
     */
    protected class SearchNode implements Comparable<SearchNode> {
        public Node node;
        public double cost;
        public SearchNode pred;

        public SearchNode(Node startNode) {
            this.node = startNode;
            this.cost = 0;
            this.pred = null;
        }

        public SearchNode(SearchNode pred, Edge newEdge) {
            this.node = newEdge.succ;
            this.cost = pred.cost + newEdge.data.doubleValue();
            this.pred = pred;
        }

        public int compareTo(SearchNode other) {
            if (cost > other.cost)
                return +1;
            if (cost < other.cost)
                return -1;
            return 0;
        }
    }

    /**
     * Constructor that sets the map that the graph uses.
     */
    public DijkstraGraph() {
        super(new PlaceholderMap<>());
    }

    /**
     * Insert a new directed edge with a non-negative weight into the graph. If 
     * an edge between pred and succ already exists, update the data stored in 
     * that edge to the new weight.
     * 
     * @param pred is the data contained in the new edge's predecesor node
     * @param succ is the data contained in the new edge's succ node
     * @param weight is the non-negative data to be stored in the new edge
     * @return true if the edge could be inserted or updated, or false if the 
     * pred or succ data are not found in any graph nodes or the weight 
     * specified is negative.
     */
    @Override
    public boolean insertEdge(NodeType pred, NodeType succ, EdgeType weight) {
        if (weight.doubleValue() < 0)
            return false;
        return super.insertEdge(pred, succ, weight);
    }

    /**
     * This helper method creates a network of SearchNodes while computing the
     * shortest path between the provided start and end locations. The
     * SearchNode that is returned by this method represents the end of the
     * shortest path that is found: it's cost is the cost of that shortest path,
     * and the nodes linked together through predecessor references represent
     * all of the nodes along that shortest path (ordered from end to start).
     *
     * @param start the starting node for the path
     * @param end   the destination node for the path
     * @return SearchNode for the final end node within the shortest path
     * @throws NoSuchElementException if either the start or the end node
     * cannot be found, or there is no path from start node to end node
     * @throws NullPointerException if the start or end node are null
     */
    protected SearchNode computeShortestPath(Node start, Node end) {
        // Exception handling
        if (start == null || end == null) {
            throw new NullPointerException("Neither start nor end node can be null");
        }
        if (!nodes.containsKey(start.data)) {
            throw new NoSuchElementException("Start node cannot be found in the graph");
        }
        if (!nodes.containsKey(end.data)) {
            throw new NoSuchElementException("End node cannot be found in the graph");
        }
        // Make an List of all unvisited nodes
        List<NodeType> unvisited = getAllNodes();
        // Make a priority queue that will store SearchNode objects
        PriorityQueue<SearchNode> pq = new PriorityQueue<>();
        // Make a SearchNode with start as the destination node using the first constructor
        SearchNode currNode = new SearchNode(start);
        unvisited.remove(currNode.node);
        // while loop
        while(!pq.isEmpty()) {
            // Pop the min search node off of the priority queue
            SearchNode path = pq.poll();
            // If the min destination is the end node, make a return that searchnode object
            if (path.node.equals(end)) {
                return path;
            }
            // If SearchNode destination node is in the arrayList (it is unvisited), should i store?
            if (unvisited.contains(path.node)) {
                // Remove node from the unvisited list
                unvisited.remove(path.node);
                List<Edge> edgesOut = path.node.edgesLeaving;
                // add each univisited neighbor
                for (Edge e : edgesOut) {
                    if (unvisited.contains(e.succ)) {
                        pq.offer(new SearchNode(path, e));
                    }
                }
            }
        }
        throw new NoSuchElementException("There is no path from the start node to the end node");
    }

    /**
     * Returns the list of data values from nodes along the shortest path
     * from the node with the provided start value through the node with the
     * provided end value. This list of data values starts with the start
     * value, ends with the end value, and contains intermediary values in the
     * order they are encountered while traversing this shortest path. This
     * method uses Dijkstra's shortest path algorithm to find this solution.
     *
     * @param start the data item in the starting node for the path
     * @param end   the data item in the destination node for the path
     * @return list of data item from nodes along this shortest path
     * @throws NoSuchElementException if either the start or the end node
     * cannot be found, or there is no path from start node to end node
     * @throws NullPointerException if the start or end node are null
     */
    public List<NodeType> shortestPathData(NodeType start, NodeType end) {
        // Create node objects with the data type passed in using the MapADT get() method. If the either value does not exist in the graph,
        // the get() method will throw a NoSuchElementException. Since we don't catch it, it will continue to bubble up the method call stack.
        Node startNode = nodes.get(start);
        Node endNode = nodes.get(end);
        // Create a linked list to store all the nodes
        LinkedList<NodeType> values = new LinkedList<>();
        // Call the computeShortestPath() method. All the nodes on the path will be stored as the predecessor references.
        SearchNode ending = computeShortestPath(startNode, endNode);
        // Go through each predecessor value and add it to the start of the LinkedList
        while (ending != null) {
            values.addFirst(ending.node.data);
            ending = ending.pred;
        }
        return values;
    }

    /**
     * Returns the cost of the path (sum over edge weights) of the shortest
     * path from the node containing the start data to the node containing the
     * end data. This method uses Dijkstra's shortest path algorithm to find
     * this solution.
     *
     * @param start the data item in the starting node for the path
     * @param end   the data item in the destination node for the path
     * @return the cost of the shortest path between these nodes
     * @throws NoSuchElementException if either the start or the end node
     * cannot be found, or there is no path from start node to end node
     * @throws NullPointerException if the start or end node are null
     */
    public double shortestPathCost(NodeType start, NodeType end) {
        // Create node objects with the data type passed in using the MapADT get() method. If the either value does not exist in the graph,
        // the get() method will throw a NoSuchElementException. Since we don't catch it, it will continue to bubble up the method call stack.
        Node startNode = nodes.get(start);
        Node endNode = nodes.get(end);
        // Call the computeShortestPath() method. All the nodes on the path will be stored as the predecessor references.
        SearchNode ending = computeShortestPath(startNode, endNode);
        return ending.cost;
    }

    @Test
    public void test1() {
        DijkstraGraph<String, Double> test = new DijkstraGraph<>();
        test.insertNode("A");
        test.insertNode("B");
        test.insertNode("C");
        test.insertNode("D");
        test.insertNode("E");
        test.insertNode("F");
        test.insertNode("G");
        test.insertNode("H");
        test.insertEdge("A", "B", 4.0);
        test.insertEdge("A", "C", 2.0);
        test.insertEdge("C", "D", 5.0);
        test.insertEdge("B", "D", 1.0);
        test.insertEdge("A", "E", 15.0);
        test.insertEdge("B", "E", 10.0);
        test.insertEdge("D", "E", 3.0);
        test.insertEdge("D", "F", 0.0);
        test.insertEdge("F", "D", 2.0);
        test.insertEdge("F", "H", 4.0);
        test.insertEdge("G", "H", 4.0);
        System.out.println(test.getNodeCount());
        assertEquals(test.getNodeCount(), 8);
    }
}
