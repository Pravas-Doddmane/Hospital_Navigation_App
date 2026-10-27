package com.passfamily.hospital_navigation.model;

/**
 * Represents a named room in the hospital with its position and dimensions on the floor map.
 */
public class Room {

    private String name;
    private float x;
    private float y;
    private float width;
    private float height;

    public Room() {
        // Required for JSON deserialization
    }

    public Room(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Returns the center X coordinate of the room on the 2000x2000 map. */
    public float getCenterX() {
        return x + width / 2f;
    }

    /** Returns the center Y coordinate of the room on the 2000x2000 map. */
    public float getCenterY() {
        return y + height / 2f;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "Room{name='" + name + "', x=" + x + ", y=" + y + "}";
    }
}