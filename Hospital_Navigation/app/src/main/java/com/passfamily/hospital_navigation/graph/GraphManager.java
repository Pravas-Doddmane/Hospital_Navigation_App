package com.passfamily.hospital_navigation.graph;


import android.content.Context;

import com.passfamily.hospital_navigation.model.Node;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * GraphManager is responsible for:
 * 1. Loading Nodes.json from assets
 * 2. Building an adjacency list graph from the nodes
 * 3. Running BFS to find the shortest path between two nodes
 */
public class GraphManager {

    // Map from node ID -> Node object
    private final Map<String, Node> nodeMap = new HashMap<>();

    // Adjacency list: node ID -> list of neighbor node IDs
    private final Map<String, List<String>> adjacencyList = new HashMap<>();

    /**
     * Loads Nodes.json from assets and builds the graph.
     *
     * @param context Android context (used to open assets)
     * @throws Exception if the file cannot be read or parsed
     */
    public void loadGraph(Context context) throws Exception {
        String json = readAssetFile(context, "Nodes.json");
        JSONArray jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);

            String id = obj.getString("id");
            float x = (float) obj.getDouble("x");
            float y = (float) obj.getDouble("y");

            JSONArray connArray = obj.getJSONArray("connectedTo");
            List<String> connections = new ArrayList<>();
            for (int j = 0; j < connArray.length(); j++) {
                connections.add(connArray.getString(j));
            }

            Node node = new Node(id, x, y, connections);
            nodeMap.put(id, node);
            adjacencyList.put(id, connections);
        }
    }

    /**
     * BFS shortest path between two node IDs.
     *
     * @return Ordered List<Node> from start to end, or empty list if no path exists.
     */
    public List<Node> findShortestPath(String startNodeId, String endNodeId) {
        if (startNodeId == null || endNodeId == null) return Collections.emptyList();
        if (!nodeMap.containsKey(startNodeId) || !nodeMap.containsKey(endNodeId)) {
            return Collections.emptyList();
        }
        if (startNodeId.equals(endNodeId)) {
            List<Node> single = new ArrayList<>();
            single.add(nodeMap.get(startNodeId));
            return single;
        }

        Map<String, String> parentMap = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(startNodeId);
        parentMap.put(startNodeId, null);

        boolean found = false;

        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<String> neighbors = adjacencyList.get(current);
            if (neighbors == null) continue;

            for (String neighbor : neighbors) {
                if (!parentMap.containsKey(neighbor)) {
                    parentMap.put(neighbor, current);
                    if (neighbor.equals(endNodeId)) {
                        found = true;
                        break;
                    }
                    queue.add(neighbor);
                }
            }
            if (found) break;
        }

        if (!found) return Collections.emptyList();

        // Reconstruct path
        List<String> pathIds = new ArrayList<>();
        String current = endNodeId;
        while (current != null) {
            pathIds.add(current);
            current = parentMap.get(current);
        }
        Collections.reverse(pathIds);

        List<Node> path = new ArrayList<>();
        for (String id : pathIds) {
            Node node = nodeMap.get(id);
            if (node != null) path.add(node);
        }
        return path;
    }

    public Node getNode(String nodeId) {
        return nodeMap.get(nodeId);
    }

    public Map<String, Node> getAllNodes() {
        return nodeMap;
    }

    /**
     * Returns the graph node whose coordinates are closest to (x, y).
     */
    public Node findNearestNode(float x, float y) {
        Node nearest = null;
        float minDist = Float.MAX_VALUE;
        for (Node node : nodeMap.values()) {
            float dx = node.getX() - x;
            float dy = node.getY() - y;
            float dist = dx * dx + dy * dy;
            if (dist < minDist) {
                minDist = dist;
                nearest = node;
            }
        }
        return nearest;
    }

    /**
     * Reads an asset file line-by-line via BufferedReader.
     *
     * WHY NOT is.available():
     * Android's AssetInputStream.available() frequently returns 0 or an
     * incorrect byte count, causing new byte[is.available()] to allocate a
     * zero-length buffer. The subsequent is.read() stores nothing, the JSON
     * string is empty, and JSONArray() throws "End of input at character 0"
     * — surfaced to the user as "Failed to load map data: Nodes.json".
     *
     * BufferedReader.readLine() reads until true EOF and is always reliable.
     */
    private String readAssetFile(Context context, String filename) throws Exception {
        InputStream is = context.getAssets().open(filename);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        reader.close();
        return sb.toString();
    }
}