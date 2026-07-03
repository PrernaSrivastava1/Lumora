package com.lumora.algorithms.kdtree;

import com.lumora.algorithms.common.vector.DistanceCalculator;
import com.lumora.algorithms.common.vector.Vector;
import com.lumora.model.DistanceMetric;
import com.lumora.model.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Custom K-Dimensional Tree (KD-Tree) index for similarity search.
 */
public class KdTree {

    public static class Node {
        Vector vector;
        Node left;
        Node right;
        int axis;

        public Node(Vector vector, int axis) {
            this.vector = vector;
            this.axis = axis;
        }
    }

    private Node root;

    public Node getRoot() {
        return root;
    }

    /**
     * Builds the KD-Tree index from a list of vectors.
     */
    public void build(List<Vector> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            this.root = null;
            return;
        }
        this.root = buildRecursive(new ArrayList<>(vectors), 0);
    }

    private Node buildRecursive(List<Vector> vectors, int depth) {
        if (vectors.isEmpty()) {
            return null;
        }

        int k = vectors.get(0).getDimension();
        int axis = depth % k;

        vectors.sort((v1, v2) -> Float.compare(v1.getValues()[axis], v2.getValues()[axis]));

        int medianIndex = vectors.size() / 2;
        Node node = new Node(vectors.get(medianIndex), axis);

        node.left = buildRecursive(vectors.subList(0, medianIndex), depth + 1);
        node.right = buildRecursive(vectors.subList(medianIndex + 1, vectors.size()), depth + 1);

        return node;
    }

    /**
     * Inserts a vector dynamically into the KD-Tree.
     */
    public void insert(Vector vector) {
        if (vector == null) {
            return;
        }
        this.root = insertRecursive(this.root, vector, 0);
    }

    private Node insertRecursive(Node node, Vector vector, int depth) {
        if (node == null) {
            int k = vector.getDimension();
            return new Node(vector, depth % k);
        }

        int axis = node.axis;
        if (vector.getValues()[axis] < node.vector.getValues()[axis]) {
            node.left = insertRecursive(node.left, vector, depth + 1);
        } else {
            node.right = insertRecursive(node.right, vector, depth + 1);
        }

        return node;
    }

    /**
     * Deletes a vector dynamically from the KD-Tree.
     */
    public void delete(Vector vector) {
        if (vector == null) {
            return;
        }
        this.root = deleteRecursive(this.root, vector, 0);
    }

    private Node deleteRecursive(Node node, Vector vector, int depth) {
        if (node == null) {
            return null;
        }

        int axis = node.axis;

        if (node.vector.getId().equals(vector.getId())) {
            if (node.right != null) {
                Node minNode = findMin(node.right, axis);
                node.vector = minNode.vector;
                node.right = deleteRecursive(node.right, minNode.vector, depth + 1);
            } else if (node.left != null) {
                Node minNode = findMin(node.left, axis);
                node.vector = minNode.vector;
                node.right = deleteRecursive(node.left, minNode.vector, depth + 1);
                node.left = null;
            } else {
                return null; // Leaf node deleted
            }
        } else if (vector.getValues()[axis] < node.vector.getValues()[axis]) {
            node.left = deleteRecursive(node.left, vector, depth + 1);
        } else {
            node.right = deleteRecursive(node.right, vector, depth + 1);
        }

        return node;
    }

    private Node findMin(Node node, int axis) {
        if (node == null) {
            return null;
        }

        if (node.axis == axis) {
            if (node.left == null) {
                return node;
            }
            return findMin(node.left, axis);
        }

        Node res = node;
        Node leftMin = findMin(node.left, axis);
        Node rightMin = findMin(node.right, axis);

        if (leftMin != null && leftMin.vector.getValues()[axis] < res.vector.getValues()[axis]) {
            res = leftMin;
        }
        if (rightMin != null && rightMin.vector.getValues()[axis] < res.vector.getValues()[axis]) {
            res = rightMin;
        }

        return res;
    }

    /**
     * Finds K-Nearest Neighbors for the query vector.
     */
    public List<SearchResult> knnSearch(float[] query, int k, DistanceCalculator calculator, DistanceMetric metric) {
        if (this.root == null || k <= 0) {
            return Collections.emptyList();
        }

        // Max heap to keep top K nearest neighbors (largest distance on top)
        PriorityQueue<SearchResult> queue = new PriorityQueue<>(k, Comparator.comparingDouble(SearchResult::getScore).reversed());

        knnSearchRecursive(this.root, query, k, calculator, metric, queue);

        List<SearchResult> results = new ArrayList<>(queue);
        results.sort(Comparator.comparingDouble(SearchResult::getScore));
        return results;
    }

    private void knnSearchRecursive(Node node, float[] query, int k, DistanceCalculator calculator, DistanceMetric metric, PriorityQueue<SearchResult> queue) {
        if (node == null) {
            return;
        }

        double distance = calculator.calculate(new Vector(null, query, null), node.vector);
        SearchResult hit = SearchResult.builder()
                .chunkId(node.vector.getId())
                .score(distance)
                .explanation("Exact distance calculated via KD-Tree search")
                .build();

        if (queue.size() < k) {
            queue.offer(hit);
        } else if (distance < queue.peek().getScore()) {
            queue.poll();
            queue.offer(hit);
        }

        int axis = node.axis;
        double diff = query[axis] - node.vector.getValues()[axis];

        Node first = diff < 0 ? node.left : node.right;
        Node second = diff < 0 ? node.right : node.left;

        knnSearchRecursive(first, query, k, calculator, metric, queue);

        boolean searchOther = false;
        if (queue.size() < k) {
            searchOther = true;
        } else {
            double maxScore = queue.peek().getScore();
            if (metric == DistanceMetric.EUCLIDEAN || metric == DistanceMetric.MANHATTAN) {
                searchOther = Math.abs(diff) < maxScore;
            } else {
                // Cosine metric lacks simple single-axis projection lower bounds
                searchOther = true;
            }
        }

        if (searchOther) {
            knnSearchRecursive(second, query, k, calculator, metric, queue);
        }
    }
}
