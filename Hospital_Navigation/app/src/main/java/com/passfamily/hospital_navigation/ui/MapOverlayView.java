package com.passfamily.hospital_navigation.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.passfamily.hospital_navigation.model.Node;

import java.util.List;

/**
 * MapOverlayView — transparent View drawn on top of the hospital floor map PhotoView.
 * This version uses a Matrix passed from PhotoView to lock the route and markers 
 * to the map during zoom and pan operations.
 */
public class MapOverlayView extends View {

    private static final float STROKE_DP  = 6f;
    private static final float CIRCLE_DP  = 14f;
    private static final float BORDER_DP  = 3f;

    private List<Node> routePath = null;
    private final Matrix displayMatrix = new Matrix();

    private final Paint routePaint       = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint startFillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint endFillPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MapOverlayView(Context context) { this(context, null); }
    public MapOverlayView(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public MapOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints();
    }

    private void initPaints() {
        final float dp = getContext().getResources().getDisplayMetrics().density;
        routePaint.setColor(Color.parseColor("#1565C0"));
        routePaint.setStyle(Paint.Style.STROKE);
        routePaint.setStrokeWidth(STROKE_DP * dp);
        routePaint.setStrokeJoin(Paint.Join.ROUND);
        routePaint.setStrokeCap(Paint.Cap.ROUND);

        startFillPaint.setColor(Color.parseColor("#2E7D32"));
        startFillPaint.setStyle(Paint.Style.FILL);

        endFillPaint.setColor(Color.parseColor("#C62828"));
        endFillPaint.setStyle(Paint.Style.FILL);

        markerBorderPaint.setColor(Color.WHITE);
        markerBorderPaint.setStyle(Paint.Style.STROKE);
        markerBorderPaint.setStrokeWidth(BORDER_DP * dp);
    }

    public void setRoute(List<Node> path) {
        this.routePath = path;
        invalidate();
    }

    public void setTransformationMatrix(Matrix matrix) {
        this.displayMatrix.set(matrix);
        invalidate();
    }

    public void clearRoute() {
        routePath = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (routePath == null || routePath.isEmpty()) return;

        final float density = getContext().getResources().getDisplayMetrics().density;
        float[] pts = new float[2];

        // 1. Draw route line
        Path polyline = new Path();
        for (int i = 0; i < routePath.size(); i++) {
            Node node = routePath.get(i);
            pts[0] = node.getX() * density;
            pts[1] = node.getY() * density;
            displayMatrix.mapPoints(pts);

            if (i == 0) polyline.moveTo(pts[0], pts[1]);
            else polyline.lineTo(pts[0], pts[1]);
        }
        canvas.drawPath(polyline, routePaint);

        // 2. Draw markers
        final float radius = CIRCLE_DP * density;

        // Start (Green)
        Node startNode = routePath.get(0);
        pts[0] = startNode.getX() * density;
        pts[1] = startNode.getY() * density;
        displayMatrix.mapPoints(pts);
        canvas.drawCircle(pts[0], pts[1], radius, startFillPaint);
        canvas.drawCircle(pts[0], pts[1], radius, markerBorderPaint);

        // Destination (Red)
        if (routePath.size() > 1) {
            Node endNode = routePath.get(routePath.size() - 1);
            pts[0] = endNode.getX() * density;
            pts[1] = endNode.getY() * density;
            displayMatrix.mapPoints(pts);
            canvas.drawCircle(pts[0], pts[1], radius, endFillPaint);
            canvas.drawCircle(pts[0], pts[1], radius, markerBorderPaint);
        }
    }
}
