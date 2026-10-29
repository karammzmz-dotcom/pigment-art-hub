
package com.pigmentarthub;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.BlurMaskFilter;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(24, 26, 30);
    private final int PANEL = Color.rgb(37, 40, 45);
    private final int PANEL2 = Color.rgb(49, 53, 59);
    private final int ACCENT = Color.rgb(218, 171, 111);
    private final int FG = Color.rgb(242, 239, 231);
    private final int MUTED = Color.rgb(163, 166, 171);
    private final int PAPER = Color.rgb(249, 246, 237);

    private FrameLayout root;
    private CanvasView canvasView;
    private int selectedColor = Color.rgb(45, 42, 39);
    private float brushSize = 18f;
    private Tool selectedTool = Tool.OIL;
    private TextView brushLabel;
    private final List<Integer> palette = new ArrayList<>();

    private enum Tool {
        OIL, WATERCOLOR, PENCIL, BLEND, ERASER
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        palette.add(Color.rgb(45, 42, 39));
        palette.add(Color.rgb(249, 246, 237));
        palette.add(Color.rgb(166, 52, 43));
        palette.add(Color.rgb(218, 111, 48));
        palette.add(Color.rgb(226, 184, 75));
        palette.add(Color.rgb(91, 126, 76));
        palette.add(Color.rgb(65, 128, 139));
        palette.add(Color.rgb(51, 76, 145));
        palette.add(Color.rgb(125, 78, 132));
        palette.add(Color.rgb(144, 94, 64));

        showCanvasPicker();
    }

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout.LayoutParams size(int w, int h) {
        return new LinearLayout.LayoutParams(
                w < 0 ? w : dp(w),
                h < 0 ? h : dp(h));
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView text(String value, float sp, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private TextView action(String value, boolean active, Runnable callback) {
        TextView t = text(value, 13, active ? BG : FG);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(13), 0, dp(13), 0);
        t.setBackground(rounded(active ? ACCENT : PANEL2, 12));
        t.setOnClickListener(v -> callback.run());
        return t;
    }

    private void newRoot() {
        root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        setContentView(root);
    }

    private void showCanvasPicker() {
        newRoot();

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(24), dp(24), dp(20));

        root.addView(page, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout brand = new LinearLayout(this);
        brand.setGravity(Gravity.CENTER_VERTICAL);

        TextView icon = text("P", 23, BG);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(ACCENT, 12));
        brand.addView(icon, size(46, 46));

        LinearLayout brandName = new LinearLayout(this);
        brandName.setOrientation(LinearLayout.VERTICAL);
        brandName.setPadding(dp(12), 0, 0, 0);
        brandName.addView(text("PIGMENT ART HUB", 17, FG));
        brandName.addView(text("THE DIGITAL PAINT STUDIO", 10, MUTED));
        brand.addView(brandName, new LinearLayout.LayoutParams(0, -2, 1));
        page.addView(brand, size(-1, 52));

        TextView heading = text("A fresh sheet.", 32, FG);
        heading.setPadding(0, dp(32), 0, dp(5));
        page.addView(heading, size(-1, -2));

        page.addView(text("Choose a canvas for your next painting.",
                14, MUTED), size(-1, -2));

        TextView section = text("CANVAS PRESETS", 11, ACCENT);
        section.setPadding(0, dp(28), 0, dp(12));
        page.addView(section, size(-1, -2));

        preset(page, "01", "Square", "1024 × 1024 px", 1024, 1024);
        preset(page, "02", "Portrait", "900 × 1200 px", 900, 1200);
        preset(page, "03", "Landscape", "1200 × 900 px", 1200, 900);
        preset(page, "04", "Wide canvas", "1600 × 900 px", 1600, 900);

        TextView footer = text(
                "COLOR  ·  TEXTURE  ·  EXPRESSION", 10, MUTED);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fp = size(-1, 48);
        fp.topMargin = dp(8);
        page.addView(footer, fp);
    }

    private void preset(LinearLayout page, String number, String name,
                        String dimensions, int w, int h) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), 0, dp(14), 0);
        card.setBackground(rounded(PANEL, 15));

        card.addView(text(number, 12, ACCENT), size(34, -2));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.addView(text(name, 16, FG), size(-1, -2));
        details.addView(text(dimensions, 12, MUTED), size(-1, -2));
        card.addView(details, new LinearLayout.LayoutParams(0, -2, 1));

        card.addView(text("↗", 20, ACCENT), size(28, 40));

        LinearLayout.LayoutParams p = size(-1, 68);
        p.bottomMargin = dp(10);
        page.addView(card, p);

        card.setOnClickListener(v -> showEditor(w, h));
    }

    private void showEditor(int canvasWidth, int canvasHeight) {
        newRoot();

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(10), dp(8), dp(10), dp(8));
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        top.addView(action("‹", false, this::showCanvasPicker),
                size(42, 40));

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        heading.setPadding(dp(10), 0, 0, 0);
        heading.addView(text("Untitled painting", 14, FG));
        heading.addView(text(canvasWidth + " × " + canvasHeight + " px",
                10, MUTED));

        top.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));

        top.addView(action("Undo", false, () -> {
            if (canvasView != null) canvasView.undo();
        }), size(62, 36));

        page.addView(top, size(-1, 46));

        LinearLayout toolRow = new LinearLayout(this);
        toolRow.setGravity(Gravity.CENTER_VERTICAL);

        addTool(toolRow, "Oil", Tool.OIL);
        addTool(toolRow, "Water", Tool.WATERCOLOR);
        addTool(toolRow, "Pencil", Tool.PENCIL);
        addTool(toolRow, "Blend", Tool.BLEND);
        addTool(toolRow, "Erase", Tool.ERASER);

        HorizontalScrollView toolScroll = new HorizontalScrollView(this);
        toolScroll.setHorizontalScrollBarEnabled(false);
        toolScroll.addView(toolRow);
        page.addView(toolScroll, size(-1, 50));

        FrameLayout paperFrame = new FrameLayout(this);
        paperFrame.setPadding(dp(7), dp(7), dp(7), dp(7));
        paperFrame.setBackground(rounded(Color.rgb(17, 18, 20), 12));

        canvasView = new CanvasView();
        paperFrame.addView(canvasView,
                new FrameLayout.LayoutParams(-1, -1));

        LinearLayout.LayoutParams canvasParams =
                new LinearLayout.LayoutParams(-1, 0, 1f);
        canvasParams.topMargin = dp(4);
        canvasParams.bottomMargin = dp(10);
        page.addView(paperFrame, canvasParams);

        LinearLayout paletteHeading = new LinearLayout(this);
        paletteHeading.setGravity(Gravity.CENTER_VERTICAL);
        paletteHeading.addView(text("PIGMENTS", 11, MUTED),
                new LinearLayout.LayoutParams(0, dp(32), 1));
        paletteHeading.addView(action("Mix color +", false,
                this::showColorMixer), size(112, 32));
        page.addView(paletteHeading, size(-1, 34));

        LinearLayout swatches = new LinearLayout(this);
        swatches.setGravity(Gravity.CENTER_VERTICAL);

        for (int color : palette) {
            View swatch = new View(this);
            swatch.setBackground(rounded(color, 20));
            LinearLayout.LayoutParams sp = size(30, 30);
            sp.setMargins(dp(5), dp(4), dp(5), dp(4));
            swatches.addView(swatch, sp);

            swatch.setOnClickListener(v -> {
                selectedColor = color;
                selectedTool = Tool.OIL;
                refreshToolSelection();
            });
        }

        HorizontalScrollView paletteScroll = new HorizontalScrollView(this);
        paletteScroll.setHorizontalScrollBarEnabled(false);
        paletteScroll.addView(swatches);
        page.addView(paletteScroll, size(-1, 42));

        LinearLayout brushRow = new LinearLayout(this);
        brushRow.setGravity(Gravity.CENTER_VERTICAL);

        brushLabel = text("SIZE  " + (int) brushSize, 11, MUTED);
        brushRow.addView(brushLabel, size(85, 32));

        SeekBar slider = new SeekBar(this);
        slider.setMax(99);
        slider.setProgress((int) brushSize - 1);
        slider.setProgressTintList(ColorStateList.valueOf(ACCENT));
        slider.setThumbTintList(ColorStateList.valueOf(ACCENT));

        slider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(
                            SeekBar s, int progress, boolean fromUser) {
                        brushSize = progress + 1;
                        brushLabel.setText("SIZE  " + (int) brushSize);
                    }

                    @Override public void onStartTrackingTouch(SeekBar s) {}
                    @Override public void onStopTrackingTouch(SeekBar s) {}
                });

        brushRow.addView(slider,
                new LinearLayout.LayoutParams(0, dp(32), 1));
        page.addView(brushRow, size(-1, 34));

        page.addView(text("Build color with layered strokes.", 10, MUTED),
                size(-1, 22));
    }

    private void addTool(LinearLayout row, String title, Tool tool) {
        TextView button = action(title, selectedTool == tool, () -> {
            selectedTool = tool;
            refreshToolSelection();
        });

        LinearLayout.LayoutParams p = size(-2, 36);
        p.setMargins(dp(3), 0, dp(3), 0);
        row.addView(button, p);
    }

    private void refreshToolSelection() {
        // Rebuild only the tool controls without resetting the painting.
        if (canvasView != null) canvasView.invalidate();
        showToolToast();
    }

    private void showToolToast() {
        String name = selectedTool.name().toLowerCase();
        Toast.makeText(this, name + " selected", Toast.LENGTH_SHORT).show();
    }

    private void showColorMixer() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(8), dp(16), dp(8));

        SeekBar[] channels = new SeekBar[3];
        String[] names = {"Red", "Green", "Blue"};
        int[] values = {
                Color.red(selectedColor),
                Color.green(selectedColor),
                Color.blue(selectedColor)
        };

        TextView preview = text("Mixed pigment", 14, Color.WHITE);
        preview.setGravity(Gravity.CENTER);
        preview.setBackground(rounded(selectedColor, 10));
        box.addView(preview, size(-1, 48));

        for (int i = 0; i < 3; i++) {
            box.addView(text(names[i], 12, Color.DKGRAY), size(-1, 24));
            channels[i] = new SeekBar(this);
            channels[i].setMax(255);
            channels[i].setProgress(values[i]);
            box.addView(channels[i], size(-1, 36));
        }

        Runnable update = () -> {
            preview.setBackground(rounded(Color.rgb(
                    channels[0].getProgress(),
                    channels[1].getProgress(),
                    channels[2].getProgress()), 10));
        };

        for (SeekBar channel : channels) {
            channel.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(
                                SeekBar s, int p, boolean fromUser) {
                            update.run();
                        }

                        @Override public void onStartTrackingTouch(SeekBar s) {}
                        @Override public void onStopTrackingTouch(SeekBar s) {}
                    });
        }

        new AlertDialog.Builder(this)
                .setTitle("Mix a color")
                .setView(box)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Use color", (dialog, which) -> {
                    selectedColor = Color.rgb(
                            channels[0].getProgress(),
                            channels[1].getProgress(),
                            channels[2].getProgress());

                    if (!palette.contains(selectedColor)) {
                        palette.add(selectedColor);
                    }

                    if (canvasView != null) canvasView.invalidate();
                })
                .show();
    }

    private class CanvasView extends View {
        private Bitmap bitmap;
        private Canvas bitmapCanvas;
        private Bitmap undoBitmap;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float lastX, lastY;

        CanvasView() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setBackgroundColor(PAPER);
        }

        private void ensureBitmap() {
            if (getWidth() <= 0 || getHeight() <= 0) return;

            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                        Bitmap.Config.ARGB_8888);
                bitmapCanvas = new Canvas(bitmap);
                bitmapCanvas.drawColor(PAPER);
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
            if (bitmapCanvas == null) return true;

            float x = event.getX();
            float y = event.getY();
            float pressure = Math.max(0.12f, event.getPressure());

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (undoBitmap != null) undoBitmap.recycle();
                    undoBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                    lastX = x;
                    lastY = y;
                    drawStroke(x, y, pressure);
                    invalidate();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    int history = event.getHistorySize();

                    for (int i = 0; i < history; i++) {
                        float hx = event.getHistoricalX(i);
                        float hy = event.getHistoricalY(i);
                        drawSegment(lastX, lastY, hx, hy,
                                event.getHistoricalPressure(i));
                        lastX = hx;
                        lastY = hy;
                    }

                    drawSegment(lastX, lastY, x, y, pressure);
                    lastX = x;
                    lastY = y;
                    invalidate();
                    return true;

                case MotionEvent.ACTION_UP:
                    drawSegment(lastX, lastY, x, y, pressure);
                    invalidate();
                    performClick();
                    return true;

                default:
                    return true;
            }
        }

        private void drawSegment(float x1, float y1, float x2, float y2,
                                 float pressure) {
            float distance = (float) Math.hypot(x2 - x1, y2 - y1);
            int steps = Math.max(1,
                    (int) (distance / Math.max(1f, brushSize * 0.2f)));

            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                drawStroke(x1 + (x2 - x1) * t,
                        y1 + (y2 - y1) * t, pressure);
            }
        }

        private void drawStroke(float x, float y, float pressure) {
            float radius = Math.max(1f, brushSize * pressure);
            int color = selectedTool == Tool.ERASER ? PAPER : selectedColor;

            paint.setColor(color);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(255);
            paint.setMaskFilter(null);

            switch (selectedTool) {
                case PENCIL:
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(Math.max(1f, radius * 0.45f));
                    paint.setAlpha(210);
                    bitmapCanvas.drawPoint(x, y, paint);
                    break;

                case WATERCOLOR:
                    paint.setAlpha(30);
                    paint.setMaskFilter(new BlurMaskFilter(
                            Math.max(1f, radius * 0.5f),
                            BlurMaskFilter.Blur.NORMAL));
                    bitmapCanvas.drawCircle(x, y, radius, paint);
                    break;

                case BLEND:
                    paint.setAlpha(35);
                    paint.setMaskFilter(new BlurMaskFilter(
                            Math.max(1f, radius * 0.3f),
                            BlurMaskFilter.Blur.NORMAL));
                    bitmapCanvas.drawCircle(x, y, radius, paint);
                    break;

                case OIL:
                    paint.setAlpha(225);
                    bitmapCanvas.drawCircle(x, y, radius * 0.5f, paint);
                    if (radius > 5) {
                        paint.setColor(Color.WHITE);
                        paint.setAlpha(25);
                        bitmapCanvas.drawCircle(
                                x - radius * 0.12f,
                                y - radius * 0.12f,
                                radius * 0.08f, paint);
                    }
                    break;

                case ERASER:
                    bitmapCanvas.drawCircle(x, y, radius, paint);
                    break;
            }

            paint.setMaskFilter(null);
            paint.setAlpha(255);
            paint.setStyle(Paint.Style.FILL);
        }

        void undo() {
            if (undoBitmap == null || bitmapCanvas == null) {
                Toast.makeText(MainActivity.this,
                        "Nothing to undo", Toast.LENGTH_SHORT).show();
                return;
            }

            bitmapCanvas.drawColor(PAPER);
            bitmapCanvas.drawBitmap(undoBitmap, 0, 0, null);
            undoBitmap.recycle();
            undoBitmap = null;
            invalidate();
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }
    }
}
