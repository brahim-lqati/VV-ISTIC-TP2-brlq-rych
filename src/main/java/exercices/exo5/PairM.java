package exercices.exo5;

/**
 * class used to facilitate the creation of dependency graph
 * node: represent the method name
 * label: represent the shared variable names between 2 nodes
 */
public class PairM {
    public PairM(String node1, String node2, String label) {
        this.node1 = node1;
        this.node2 = node2;
        this.label = label;
    }

    private String node1;
    private String node2;
    private String label;

    public String getNode1() {
        return node1;
    }

    public String getNode2() {
        return node2;
    }

    public String getLabel() {
        return label;
    }
}
