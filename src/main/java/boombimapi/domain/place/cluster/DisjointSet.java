package boombimapi.domain.place.cluster;

public final class DisjointSet {

    private final int[] parent;
    private final int[] size;

    public DisjointSet(
        int n
    ) {
        if (n <= 0) {
            throw new IllegalArgumentException("DisjointSet size must be positive. n:" + n);
        }

        this.parent = new int[n];
        this.size = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int findRoot(
        int x
    ) {
        if (parent[x] != x) {
            parent[x] = findRoot(parent[x]);
        }

        return parent[x];
    }

    public void union(
        int a,
        int b
    ) {
        int rootA = findRoot(a);
        int rootB = findRoot(b);

        if (rootA == rootB) {
            return;
        }

        parent[rootB] = rootA;
        size[rootA] += size[rootB];
    }

}
