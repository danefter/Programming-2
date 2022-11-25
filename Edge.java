/**
 * @author Dan Jensen
 *
 * **/
import java.io.Serializable;
import java.util.Objects;



public class Edge<T> implements Serializable, Comparable<Edge<T>> {
    private final String name;
    private final T src;
    private final T dest;
    private int weight;

    public Edge(String name, T src, T dest, int weight) {
        this.name = name;
        this.src = src;
        this.dest = dest;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public T getSrc() {
        return src;
    }

    public T getDestination() {
        return dest;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) throws IllegalArgumentException {
        if (weight < 0) throw new IllegalArgumentException("Weight must be a positive integer.");
        else this.weight = weight;
    }


    @Override
    public String toString() {
        return "to " + ((Place)dest).getName() + " by " + name + " takes " + weight;
    }

    @Override
    public int compareTo(Edge edge) {
        return Integer.compare(weight, edge.weight);
    }

    //equals overridden to maintain directed edges
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge<?> edge = (Edge<?>) o;
        return Objects.equals(name, edge.name) && Objects.equals(src, edge.src) && Objects.equals(dest, edge.dest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, src, dest, weight);
    }
}