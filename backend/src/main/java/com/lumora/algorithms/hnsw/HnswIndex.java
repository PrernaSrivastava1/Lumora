package com.lumora.algorithms.hnsw;

import com.lumora.algorithms.common.vector.DistanceCalculator;
import com.lumora.algorithms.common.vector.Vector;
import com.lumora.model.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom Hierarchical Navigable Small World (HNSW) index implementation.
 */
public class HnswIndex {

    public static class HnswNode {
        Vector vector;
        // Level index -> list of neighbor nodes
        List<List<HnswNode>> connections;
        int level;

        public HnswNode(Vector vector, int level) {
            this.vector = vector;
            this.level = level;
            this.connections = new ArrayList<>(level + 1);
            for (int i = 0; i <= level; i++) {
                this.connections.add(new ArrayList<>());
            }
        }
    }

    private static class NodeDistance {
        HnswNode node;
        double distance;

        NodeDistance(HnswNode node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    private final int M = 16;
    private final int M0 = 32;
    private final int efConstruction = 64;
    private final int efSearch = 32;
    private final double mL = 1.0 / Math.log(M);

    private final Map<Long, HnswNode> nodes = new ConcurrentHashMap<>();
    private HnswNode enterPoint = null;
    private int maxLevel = -1;
    private final Random random = new Random();

    public HnswNode getEnterPoint() {
        return enterPoint;
    }

    public Map<Long, HnswNode> getNodes() {
        return nodes;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int count() {
        return nodes.size();
    }

    /**
     * Builds the HNSW index from a list of vectors.
     */
    public void build(List<Vector> vectors, DistanceCalculator calculator) {
        nodes.clear();
        enterPoint = null;
        maxLevel = -1;

        if (vectors == null) {
            return;
        }
        for (Vector v : vectors) {
            insert(v, calculator);
        }
    }

    /**
     * Inserts a vector dynamically into the HNSW index.
     */
    public void insert(Vector vector, DistanceCalculator calculator) {
        if (vector == null || vector.getId() == null) {
            return;
        }

        // Generate level for the new node
        double randVal = random.nextDouble();
        if (randVal == 0.0) {
            randVal = 0.0000001; // Avoid Math.log(0.0)
        }
        int level = (int) (-Math.log(randVal) * mL);

        HnswNode newNode = new HnswNode(vector, level);
        nodes.put(vector.getId(), newNode);

        if (enterPoint == null) {
            enterPoint = newNode;
            maxLevel = level;
            return;
        }

        HnswNode currEnterPoint = enterPoint;
        float[] query = vector.getValues();

        // 1. Greedy search from top level down to level + 1
        double curDist = calculator.calculate(vector, currEnterPoint.vector);
        for (int l = maxLevel; l > level; l--) {
            boolean changed = true;
            while (changed) {
                changed = false;
                for (HnswNode neighbor : currEnterPoint.connections.get(l)) {
                    double d = calculator.calculate(vector, neighbor.vector);
                    if (d < curDist) {
                        curDist = d;
                        currEnterPoint = neighbor;
                        changed = true;
                    }
                }
            }
        }

        // 2. Connect at level down to 0
        int startLevel = Math.min(level, maxLevel);
        for (int l = startLevel; l >= 0; l--) {
            List<NodeDistance> candidates = searchLayer(currEnterPoint, query, efConstruction, l, calculator);
            
            // Connect to nearest neighbors
            int limit = (l == 0) ? M0 : M;
            candidates.sort(Comparator.comparingDouble(c -> c.distance));
            List<NodeDistance> selected = candidates.subList(0, Math.min(candidates.size(), limit));

            for (NodeDistance nd : selected) {
                HnswNode neighbor = nd.node;
                newNode.connections.get(l).add(neighbor);
                neighbor.connections.get(l).add(newNode);

                // Shrink neighbor connections if limit exceeded
                shrinkConnections(neighbor, l, calculator);
            }
            
            // Set enter point for the next lower layer search
            if (!selected.isEmpty()) {
                currEnterPoint = selected.get(0).node;
            }
        }

        // Update global entry point if new node exceeds maxLevel
        if (level > maxLevel) {
            maxLevel = level;
            enterPoint = newNode;
        }
    }

    /**
     * Deletes a vector dynamically from the HNSW index.
     */
    public void delete(Vector vector, DistanceCalculator calculator) {
        if (vector == null || vector.getId() == null) {
            return;
        }

        HnswNode targetNode = nodes.remove(vector.getId());
        if (targetNode == null) {
            return;
        }

        // Disconnect targetNode from all neighbors
        for (int l = 0; l <= targetNode.level; l++) {
            List<HnswNode> neighbors = new ArrayList<>(targetNode.connections.get(l));
            for (HnswNode neighbor : neighbors) {
                neighbor.connections.get(l).remove(targetNode);
                
                // Perform healing to maintain navigable path
                healConnections(neighbor, l, calculator);
            }
        }

        // Re-evaluate enterPoint and maxLevel
        if (enterPoint == targetNode) {
            if (nodes.isEmpty()) {
                enterPoint = null;
                maxLevel = -1;
            } else {
                // Pick an arbitrary node that has the highest level
                HnswNode newEnterPoint = nodes.values().iterator().next();
                for (HnswNode node : nodes.values()) {
                    if (node.level > newEnterPoint.level) {
                        newEnterPoint = node;
                    }
                }
                enterPoint = newEnterPoint;
                maxLevel = newEnterPoint.level;
            }
        }
    }

    private void healConnections(HnswNode node, int level, DistanceCalculator calculator) {
        int limit = (level == 0) ? M0 : M;
        List<HnswNode> connList = node.connections.get(level);
        if (connList.size() < limit / 2 && !nodes.isEmpty()) {
            // Find nearby candidates to reconnect
            List<NodeDistance> candidates = searchLayer(enterPoint, node.vector.getValues(), efConstruction, level, calculator);
            candidates.sort(Comparator.comparingDouble(c -> c.distance));
            for (NodeDistance nd : candidates) {
                if (connList.size() >= limit) {
                    break;
                }
                HnswNode candidate = nd.node;
                if (!nodes.containsKey(candidate.vector.getId())) {
                    continue;
                }
                if (candidate != node && !connList.contains(candidate)) {
                    connList.add(candidate);
                    candidate.connections.get(level).add(node);
                }
            }
        }
    }

    private void shrinkConnections(HnswNode node, int level, DistanceCalculator calculator) {
        int limit = (level == 0) ? M0 : M;
        List<HnswNode> connList = node.connections.get(level);
        if (connList.size() > limit) {
            List<NodeDistance> candidates = new ArrayList<>();
            for (HnswNode neighbor : connList) {
                double d = calculator.calculate(node.vector, neighbor.vector);
                candidates.add(new NodeDistance(neighbor, d));
            }
            candidates.sort(Comparator.comparingDouble(c -> c.distance));

            connList.clear();
            for (int i = 0; i < limit; i++) {
                connList.add(candidates.get(i).node);
            }
        }
    }

    /**
     * Searches the HNSW graph using greedy routing and candidates queue.
     */
    public List<SearchResult> search(float[] query, int k, DistanceCalculator calculator) {
        if (enterPoint == null || k <= 0) {
            return Collections.emptyList();
        }

        HnswNode currEnterPoint = enterPoint;
        Vector queryVec = new Vector(null, query, null);
        double curDist = calculator.calculate(queryVec, currEnterPoint.vector);

        // 1. Greedy search down to layer 1
        for (int l = maxLevel; l > 0; l--) {
            boolean changed = true;
            while (changed) {
                changed = false;
                for (HnswNode neighbor : currEnterPoint.connections.get(l)) {
                    double d = calculator.calculate(queryVec, neighbor.vector);
                    if (d < curDist) {
                        curDist = d;
                        currEnterPoint = neighbor;
                        changed = true;
                    }
                }
            }
        }

        // 2. Multi-candidate layer search on level 0
        List<NodeDistance> candidates = searchLayer(currEnterPoint, query, Math.max(efSearch, k), 0, calculator);
        candidates.sort(Comparator.comparingDouble(c -> c.distance));

        List<SearchResult> results = new ArrayList<>();
        int count = Math.min(candidates.size(), k);
        for (int i = 0; i < count; i++) {
            NodeDistance nd = candidates.get(i);
            results.add(SearchResult.builder()
                    .chunkId(nd.node.vector.getId())
                    .score(nd.distance)
                    .explanation("Approximate similarity calculated via HNSW")
                    .build());
        }

        return results;
    }

    private List<NodeDistance> searchLayer(HnswNode startNode, float[] query, int ef, int level, DistanceCalculator calculator) {
        Set<Long> visited = new HashSet<>();
        PriorityQueue<NodeDistance> candidates = new PriorityQueue<>(Comparator.comparingDouble(c -> c.distance));
        PriorityQueue<NodeDistance> found = new PriorityQueue<>(Comparator.comparingDouble((NodeDistance c) -> c.distance).reversed());

        Vector queryVec = new Vector(null, query, null);
        double dist = calculator.calculate(queryVec, startNode.vector);
        NodeDistance start = new NodeDistance(startNode, dist);
        visited.add(startNode.vector.getId());
        candidates.offer(start);
        found.offer(start);

        while (!candidates.isEmpty()) {
            NodeDistance curr = candidates.poll();
            NodeDistance worstFound = found.peek();
            if (curr.distance > worstFound.distance) {
                break;
            }

            for (HnswNode neighbor : curr.node.connections.get(level)) {
                Long nid = neighbor.vector.getId();
                if (!visited.contains(nid)) {
                    visited.add(nid);
                    double d = calculator.calculate(queryVec, neighbor.vector);
                    worstFound = found.peek();
                    if (d < worstFound.distance || found.size() < ef) {
                        NodeDistance candidate = new NodeDistance(neighbor, d);
                        candidates.offer(candidate);
                        found.offer(candidate);
                        if (found.size() > ef) {
                            found.poll();
                        }
                    }
                }
            }
        }

        return new ArrayList<>(found);
    }
}
