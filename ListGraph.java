
/**
 * @author Dan Jensen
 *
 * **/

import java.io.Serializable;
import java.util.*;


public class ListGraph<T> implements Graph<T>, Serializable {

    private final Map<T, Set<Edge<T>>> nodes = new HashMap<>();

    private final List<Edge<T>> edges = new ArrayList<>();

    public ListGraph()
    {}

    @Override
    public String toString() {
        List<T> nodeList = new ArrayList<>(getNodes());
        List<String> finalList = new ArrayList<>();
        for (int i = 0; i < getNodes().size(); i++) {
            List<String> stringList = new ArrayList<>();
            Set<Edge<T>> edgeSet = new HashSet<>(nodes.get(nodeList.get(i)));
            edgeSet.forEach(e -> stringList.add(e.toString()));
            finalList.add("Nodes: "+i+" Name: "+nodeList.get(i).toString()+" Edges: "+ stringList);
        }
        return finalList.toString();
    }

    @Override
    public void add(T node) {
        nodes.putIfAbsent(node, new HashSet<>());
    }

 //removes node and all edges from node
    @Override
    public void remove(T node) {
        if (!nodes.containsKey(node)) throw new NoSuchElementException("Nodes does not exist");
        else edges.removeIf(edge -> edge.getSrc() == node || edge.getDestination() == node);
        getNodes().forEach(n -> nodes.get(n).removeAll(getEdgesFrom(node)));
        edges.removeAll(getEdgesFrom(node));
        getNodes().forEach(n -> nodes.get(n).removeIf(e -> getEdgesFrom(n).size() == 1 && e.getDestination() == node));
        nodes.remove(node);
    }

    @Override
    public void connect (T node1, T node2, String name, int weight) {
        Set<Edge<T>> set1 = nodes.get(node1);
        Set<Edge<T>> set2 = nodes.get(node2);
        Edge<T> e1 = new Edge<>(name, node1, node2, weight);
        Edge<T> e2 = new Edge<>(name, node2, node1, weight);
        if (weight < 0) throw new IllegalArgumentException("Negative weight");
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            throw new NoSuchElementException("Node doesn't exist");
        }
        for (Edge<T> e : set1) if (e.getDestination() == node1 && e.getSrc() == node2)
            throw new IllegalStateException("Edge already exists");
        for (Edge<T> e : set2) if (e.getDestination() == node2 && e.getSrc() == node1)
            throw new IllegalStateException("Edge already exists");
        set1.add(e1);
        set2.add(e2);
        nodes.put(node1, set1);
        nodes.put(node2, set2);
        edges.add(e1);
        edges.add(e2);
    }

    @Override
    public void setConnectionWeight(T node1, T node2, int weight) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2))
            throw new NoSuchElementException("Nodes does not exist");
            getEdgeBetween(node1, node2).setWeight(weight);
            getEdgeBetween(node2, node1).setWeight(weight);
    }

    @Override
    public Set<T> getNodes() {
        return nodes.keySet();
    }

    //all Edges connected to specific node
    @Override
    public Collection<Edge<T>> getEdgesFrom(T node) {
        if (!nodes.containsKey(node)) throw new NoSuchElementException("Nodes does not exist");
        Set<Edge<T>> returnSet = new HashSet<>();
        for (Edge<T> e : edges) {
            if (e.getSrc() == node) returnSet.add(e);
        }
        returnSet.addAll(nodes.get(node));
        return Collections.unmodifiableCollection(returnSet);
    }

    //produces edges between nodes regardless of direction
    @Override
    public Edge<T> getEdgeBetween(T node1, T node2) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) throw new NoSuchElementException("Nodes does not exist");
        for (Edge<T> edge: nodes.get(node1)) {
            if ((edge.getSrc() == node1 && edge.getDestination() == node2) ||
                    (edge.getSrc() == node2 && edge.getDestination() == node1)) {
                return edge;
            }
        }
        for (Edge<T> edge: nodes.get(node2)) {
            if ((edge.getSrc() == node1 && edge.getDestination() == node2) ||
                    (edge.getSrc() == node2 && edge.getDestination() == node1)) {
                return edge;
            }
        }
        return null;
    }

    @Override
    public void disconnect(T node1, T node2) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2))
            throw new NoSuchElementException("Nodes does not exist");
        if (getEdgeBetween(node1, node2) == null) throw new IllegalStateException("Edge does not exist");
        else
            edges.remove(getEdgeBetween(node1, node2));
            edges.remove(getEdgeBetween(node2, node1));
            nodes.get(node1).remove(getEdgeBetween(node1, node2));
            nodes.get(node2).remove(getEdgeBetween(node2, node1));
    }

    @Override
    public boolean pathExists(T from, T to) {
        Set<T> visited = new HashSet<>();
        Map<T, T> via = new HashMap<>();
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) return false;
        else depthFirstSearch(from, to, visited, via);
        return visited.contains(to);
    }

    //collects path using depthFirstSearch which adds nodes to via Map
    @Override
    public List<Edge<T>> getPath(T from, T to) {
        Set<T> visited = new HashSet<>();
        Map<T, T> via = new HashMap<>();
        if (!pathExists(from, to)) return null;
        else depthFirstSearch(to, from, visited, via);
            return gatherPath(to, from, via);
    }

    //provides list of Edges from via Map created in DepthFirstSearch
    private List<Edge<T>> gatherPath(T from, T to, Map<T, T> via){
        List<Edge<T>> path = new ArrayList<>();
        T where = to;
        while (where != from){
            T node = via.get(where);
            Edge<T> e = getEdgeBetween(node, where);
            path.add(e);
            where = node;
        }
        Collections.reverse(path);
        return path;
    }

    private void depthFirstSearch(T where, T fromWhere, Set<T> visited, Map<T, T> via){
        visited.add(where);
        via.put(where, fromWhere);
        for(Edge<T> e : nodes.get(where))
            if (!visited.contains(e.getDestination()))
                depthFirstSearch(e.getDestination(), where, visited, via);
    }

}