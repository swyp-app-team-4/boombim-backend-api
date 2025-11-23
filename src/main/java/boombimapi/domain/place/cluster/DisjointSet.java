package boombimapi.domain.place.cluster;

public final class DisjointSet {

    private final int[] parent;

    public DisjointSet(
        int count
    ) {
        if (count <= 0) {
            throw new IllegalArgumentException("DisjointSet size must be positive. count:" + count);
        }

        this.parent = new int[count];

        for (int i = 0; i < count; i++) {
            parent[i] = i;
        }
    }

    public int findRoot(
        int index
    ) {
        if (parent[index] != index) {
            parent[index] = findRoot(parent[index]);
        }

        return parent[index];
    }

    public void union(
        int first,
        int second
    ) {
        int rootFirst = findRoot(first);
        int rootSecond = findRoot(second);

        if (rootFirst == rootSecond) {
            return;
        }

        parent[rootSecond] = rootFirst;
    }

}
