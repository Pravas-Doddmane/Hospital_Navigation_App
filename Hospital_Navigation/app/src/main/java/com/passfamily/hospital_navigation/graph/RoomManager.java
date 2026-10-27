package com.passfamily.hospital_navigation.graph;
import android.content.Context;

import com.passfamily.hospital_navigation.model.Node;
import com.passfamily.hospital_navigation.model.Room;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomManager is responsible for:
 * 1. Loading Rooms.json from assets
 * 2. Providing case-insensitive room name lookup
 * 3. Mapping a room name to the nearest graph node via GraphManager
 */
public class RoomManager {

    private final List<Room> rooms = new ArrayList<>();
    private final GraphManager graphManager;

    public RoomManager(GraphManager graphManager) {
        this.graphManager = graphManager;
    }

    /**
     * Loads Rooms.json from assets.
     * Must be called before any lookup methods.
     */
    public void loadRooms(Context context) throws Exception {
        String json = readAssetFile(context, "Rooms.json");
        JSONArray jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String name   = obj.getString("name");
            float x       = (float) obj.getDouble("x");
            float y       = (float) obj.getDouble("y");
            float width   = (float) obj.getDouble("width");
            float height  = (float) obj.getDouble("height");
            rooms.add(new Room(name, x, y, width, height));
        }
    }

    /**
     * Case-insensitive room name lookup.
     * Tries exact match first, then substring match as fallback.
     */
    public Room findRoomByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String query = name.trim().toUpperCase();

        for (Room room : rooms) {
            if (room.getName().toUpperCase().equals(query)) return room;
        }
        for (Room room : rooms) {
            if (room.getName().toUpperCase().contains(query)) return room;
        }
        return null;
    }

    /**
     * Maps a room name to the nearest graph node.
     */
    public Node getRoomNode(String roomName) {
        Room room = findRoomByName(roomName);
        if (room == null) return null;
        return graphManager.findNearestNode(room.getCenterX(), room.getCenterY());
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    /**
     * Returns a sorted list of all room names for dropdown selection.
     */
    public List<String> getAllRoomNames() {
        List<String> names = new ArrayList<>();
        for (Room room : rooms) {
            names.add(room.getName());
        }
        // Sort alphabetically for better user experience
        java.util.Collections.sort(names);
        return names;
    }

    /**
     * Reads an asset file line-by-line via BufferedReader.
     *
     * WHY NOT is.available():
     * Android's AssetInputStream.available() frequently returns 0, making
     * new byte[is.available()] a zero-length buffer. BufferedReader is reliable.
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