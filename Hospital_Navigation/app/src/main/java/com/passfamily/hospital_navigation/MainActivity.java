package com.passfamily.hospital_navigation;

import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.passfamily.hospital_navigation.graph.GraphManager;
import com.passfamily.hospital_navigation.graph.RoomManager;
import com.passfamily.hospital_navigation.model.Node;
import com.passfamily.hospital_navigation.ui.MapOverlayView;

import java.util.List;

/**
 * MainActivity hosts the hospital floor map and the search UI.
 */
public class MainActivity extends AppCompatActivity {

    // ---- UI ----
    private PhotoView mapImageView;
    private MapOverlayView overlayView;
    private AutoCompleteTextView etStart;
    private AutoCompleteTextView etDestination;
    private TextInputLayout tilStart;
    private TextInputLayout tilDestination;
    private MaterialButton btnFindRoute;
    private MaterialButton btnSearch;
    private View expandableContent;
    private View loadingIndicator;

    // ---- Navigation graph ----
    private GraphManager graphManager;
    private RoomManager roomManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        // Enable modern edge-to-edge display
        EdgeToEdge.enable(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ensure system bar icons are visible (Dark icons for Light mode)
        WindowInsetsControllerCompat insetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(true);
            insetsController.setAppearanceLightNavigationBars(true);
        }

        // Bind views
        mapImageView    = findViewById(R.id.mapImageView);
        overlayView     = findViewById(R.id.overlayView);
        etStart         = findViewById(R.id.etStart);
        etDestination   = findViewById(R.id.etDestination);
        tilStart        = findViewById(R.id.tilStart);
        tilDestination  = findViewById(R.id.tilDestination);
        btnFindRoute    = findViewById(R.id.btnFindRoute);
        btnSearch       = findViewById(R.id.btnSearch);
        expandableContent = findViewById(R.id.expandableContent);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Adjust padding for system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.searchPanel), (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), topInset + 8, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // Load map
        mapImageView.setImageResource(R.drawable.floor_map);

        // Set initial zoom
        mapImageView.post(() -> {
            float maxScale = mapImageView.getMaximumScale();
            mapImageView.setScale(maxScale, true);
        });

        // SYNC: Keep overlay locked to map during zoom and pan
        mapImageView.setOnMatrixChangeListener(rect -> {
            Matrix m = new Matrix();
            mapImageView.getDisplayMatrix(m);
            overlayView.setTransformationMatrix(m);
        });

        // Load graph data
        loadGraphData();

        // Search button toggle
        btnSearch.setOnClickListener(v -> toggleSearchPanel());

        // Find Route button
        btnFindRoute.setOnClickListener(v -> handleFindRoute());
    }

    private void loadGraphData() {
        loadingIndicator.setVisibility(View.VISIBLE);
        btnFindRoute.setEnabled(false);

        new Thread(() -> {
            try {
                graphManager = new GraphManager();
                graphManager.loadGraph(MainActivity.this);

                roomManager = new RoomManager(graphManager);
                roomManager.loadRooms(MainActivity.this);

                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    btnFindRoute.setEnabled(true);
                    setupLocationDropdowns();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this,
                            "Failed to load map data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void toggleSearchPanel() {
        if (expandableContent.getVisibility() == View.GONE) {
            expandableContent.setVisibility(View.VISIBLE);
            btnSearch.setVisibility(View.GONE);
        } else {
            expandableContent.setVisibility(View.GONE);
            btnSearch.setVisibility(View.VISIBLE);
        }
    }

    private void collapseSearchPanel() {
        expandableContent.setVisibility(View.GONE);
        btnSearch.setVisibility(View.VISIBLE);
    }

    private void setupLocationDropdowns() {
        if (roomManager == null) return;

        List<String> roomNames = roomManager.getAllRoomNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                roomNames
        );

        etStart.setAdapter(adapter);
        etDestination.setAdapter(adapter);

        etStart.setOnClickListener(v -> etStart.showDropDown());
        etDestination.setOnClickListener(v -> etDestination.showDropDown());

        etStart.setOnItemClickListener((parent, view, position, id) -> tilStart.setError(null));
        etDestination.setOnItemClickListener((parent, view, position, id) -> tilDestination.setError(null));
    }

    private void handleFindRoute() {
        tilStart.setError(null);
        tilDestination.setError(null);
        overlayView.clearRoute();

        String startInput = etStart.getText() != null ? etStart.getText().toString().trim() : "";
        String destInput  = etDestination.getText() != null ? etDestination.getText().toString().trim() : "";

        if (startInput.isEmpty()) {
            tilStart.setError("Please enter a starting location");
            return;
        }
        if (destInput.isEmpty()) {
            tilDestination.setError("Please enter a destination");
            return;
        }

        if (graphManager == null || roomManager == null) {
            Toast.makeText(this, "Map data is still loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        Node startNode = roomManager.getRoomNode(startInput);
        Node endNode   = roomManager.getRoomNode(destInput);

        if (startNode == null) {
            tilStart.setError("Room not found");
            return;
        }
        if (endNode == null) {
            tilDestination.setError("Room not found");
            return;
        }

        List<Node> path = graphManager.findShortestPath(startNode.getId(), endNode.getId());

        if (path.isEmpty()) {
            Toast.makeText(this, "No route found", Toast.LENGTH_LONG).show();
            return;
        }

        // Set route and force an initial matrix sync using the total display matrix
        overlayView.setRoute(path);
        Matrix m = new Matrix();
        mapImageView.getDisplayMatrix(m);
        overlayView.setTransformationMatrix(m);

        Toast.makeText(this, "Route found!", Toast.LENGTH_SHORT).show();
        collapseSearchPanel();
    }
}
