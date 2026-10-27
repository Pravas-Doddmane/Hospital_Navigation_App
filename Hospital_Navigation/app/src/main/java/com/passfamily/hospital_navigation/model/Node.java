package com.passfamily.hospital_navigation.model;

import java.util.List;

/**
 * Represents a navigation node (waypoint) in the hospital floor map graph.
 * Each node has a unique ID, coordinates on the 2000x2000 map, and connections to adjacent nodes.
 */
public class Node {

    private String id;
    private float x;
    private float y;
    private List<String> connectedTo;

    public Node() {
        // Required for JSON deserialization
    }

    public Node(String id, float x, float y, List<String> connectedTo) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.connectedTo = connectedTo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public List<String> getConnectedTo() {
        return connectedTo;
    }

    public void setConnectedTo(List<String> connectedTo) {
        this.connectedTo = connectedTo;
    }

    @Override
    public String toString() {
        return "Node{id='" + id + "', x=" + x + ", y=" + y + "}";
    }
}