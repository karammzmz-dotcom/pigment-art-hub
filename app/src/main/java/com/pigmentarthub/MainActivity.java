
package com.pigmentarthub;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private LinearLayout root;
    private DrawingView drawingView;
    private int selectedColor = Color.BLACK;
    private float brushSize = 12f;
    private boolean eraserEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showCanvasPicker();
    }

    private void showCanvasPicker() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(24, 32, 24, 24);
        root.setBackgroundColor(Color.rgb(242, 241, 237));

        TextView title = new TextView(this);
        title.setText("PIGMENT ART HUB");
        title.setTextSize(26);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Start a new canvas");
        subtitle.setTextSize(20);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 24, 0, 24);
        root.addView(subtitle, matchWrap());

        addCanvasButton("Square — 1024 × 1024", 1024, 1024);
        addCanvasButton("Portrait — 900 × 1200", 900, 1200);
        addCanvasButton("Landscape — 1200 × 900", 1200, 900);
        addCanvasButton("HD Landscape — 1600 × 900", 1600, 900);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private void addCanvasButton(String label, int width, int height) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(16);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 64);
        params.setMargins(0, 8, 0, 8);

        root.addView(button, params);

        button.setOnClickListener(v -> showEditor(width, height));
        setContentView(root);
    }

    private void showEditor(int canvasWidth, int canvasHeight) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(235, 233, 228));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(4, 4, 4, 4);

        Button backButton = new Button(this);
        backButton.setText("Back");
        backButton.setAllCaps(false);
        backButton.setOnClickListener(v -> showCanvasPicker());
        toolbar.addView(backButton, new LinearLayout.LayoutParams(0, 52, 1));

        Button eraserButton = new Button(this);
        eraserButton.setText("Eraser");
        eraserButton.setAllCaps(false);
        eraserButton.setOnClickListener(v -> {
            eraserEnabled = !eraserEnabled;
            Toast.makeText(
                    this,
                    eraserEnabled ? "Eraser enabled" : "Brush enabled",
                    Toast.LENGTH_SHORT
            ).show();
        });
        toolbar.addView(eraserButton, new LinearLayout.LayoutParams(0, 52, 1));

        Button clearButton = new Button(this);
        clearButton.setText("Clear");
        clearButton.setAllCaps(false);
        clearButton.setOnClickListener(v -> drawingView.clearCanvas());
        toolbar.addView(clearButton, new LinearLayout.LayoutParams(0, 52, 1));

        root.addView(toolbar, matchWrap());

        TextView canvasInfo = new TextView(this);
        canvasInfo.setText("Canvas: " + canvasWidth + " × " + canvasHeight);
        canvasInfo.setTextColor(Color.DKGRAY);
        canvasInfo.setGravity(Gravity.CENTER);
        canvasInfo.setPadding(4, 8, 4, 8);
        root.addView(canvasInfo, matchWrap());

        drawingView = new DrawingView();
        drawingView.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams drawingParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        drawingParams.setMargins(8, 8, 8, 8);
        root.addView(drawingView, drawingParams);

        LinearLayout colors = new LinearLayout(this);
        colors.setOrientation(LinearLayout.HORIZONTAL);
        colors.setGravity(Gravity.CENTER);
        colors.setPadding(4, 4, 4, 4);

        int[] palette = {
                Color.BLACK, Color.WHITE, Color.RED, Color.rgb(255, 128, 0),
                Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE,
                Color.MAGENTA, Color.rgb(130, 70, 30), Color.GRAY
        };

        for (int color : palette) {
            Button colorButton = new Button(this);
            colorButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(color));
            colorButton.setText("");
            colorButton.setContentDescription("Choose color");

            LinearLayout.LayoutParams colorParams =
                    new LinearLayout.LayoutParams(0, 44, 1);
            colorParams.setMargins(2, 2, 2, 2);
            colors.addView(colorButton, colorParams);

            colorButton.setOnClickListener(v -> {
                selectedColor = color;
                eraserEnabled = false;
                drawingView.invalidate();
            });
        }

        root.addView(colors, matchWrap());

        TextView brushLabel = new TextView(this);
        brushLabel.setText("Brush size");
        brushLabel.setTextColor(Color.BLACK);
        brushLabel.setGravity(Gravity.CENTER);
        root.addView(brushLabel, matchWrap());

        SeekBar brushSeekBar = new SeekBar(this);
        brushSeekBar.setMax(99);
        brushSeekBar.setProgress(11);
        root.addView(brushSeekBar, matchWrap());

        brushSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar, int progress, boolean fromUser) {
                        brushSize = progress + 1;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        setContentView(root);
    }

    private class DrawingView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private Bitmap bitmap;
        private Canvas bitmapCanvas;
        private float lastX;
        private float lastY;

        DrawingView() {
            super(MainActivity.this);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(brushSize);
            paint.setColor(selectedColor);
        }

        private void ensureBitmap() {
            if (getWidth() <= 0 || getHeight() <= 0) {
                return;
            }

            if (bitmap == null
                    || bitmap.getWidth() != getWidth()
                    || bitmap.getHeight() != getHeight()) {
                Bitmap oldBitmap = bitmap;
                bitmap = Bitmap.createBitmap(
                        getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                bitmapCanvas = new Canvas(bitmap);
                bitmapCanvas.drawColor(Color.WHITE);

                if (oldBitmap != null) {
                    bitmapCanvas.drawBitmap(oldBitmap, 0, 0, null);
                    oldBitmap.recycle();
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            ensureBitmap();

            if (bitmap != null) {
                canvas.drawBitmap(bitmap, 0, 0, null);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            ensureBitmap();

            if (bitmapCanvas == null) {
                return true;
            }

            float x = event.getX();
            float y = event.getY();

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = x;
                    lastY = y;
                    path.reset();
                    path.moveTo(x, y);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    paint.setColor(
                            eraserEnabled ? Color.WHITE : selectedColor);
                    paint.setStrokeWidth(brushSize);
                    bitmapCanvas.drawLine(lastX, lastY, x, y, paint);
                    lastX = x;
                    lastY = y;
                    invalidate();
                    return true;

                case MotionEvent.ACTION_UP:
                    paint.setColor(
                            eraserEnabled ? Color.WHITE : selectedColor);
                    paint.setStrokeWidth(brushSize);
                    bitmapCanvas.drawLine(lastX, lastY, x, y, paint);
                    invalidate();
                    performClick();
                    return true;

                default:
                    return true;
            }
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        void clearCanvas() {
            ensureBitmap();
            if (bitmapCanvas != null) {
                bitmapCanvas.drawColor(Color.WHITE);
                invalidate();
            }
        }
    }
}
