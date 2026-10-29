package com.pigmentarthub;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private PaintView paintView;
    private int selectedColor = Color.rgb(32, 100, 210);
    private float brushSize = 14f, opacity = 0.92f;
    private String brush = "Round";
    private final List<Integer> palette = new ArrayList<>();
    private LinearLayout root;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        palette.add(Color.BLACK); palette.add(Color.WHITE); palette.add(Color.RED);
        palette.add(Color.rgb(255, 150, 20)); palette.add(Color.YELLOW);
        palette.add(Color.GREEN); palette.add(Color.CYAN); palette.add(Color.BLUE);
        palette.add(Color.MAGENTA); palette.add(Color.rgb(130, 75, 40));
        showCanvasPicker();
    }

    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView text(String s, int size) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(Color.rgb(35, 35, 40)); t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(8), dp(4), dp(8), dp(4)); return t;
    }
    private Button button(String s, Runnable r) {
        Button b = new Button(this); b.setText(s); b.setAllCaps(false);
        b.setOnClickListener(v -> r.run()); return b;
    }
    private void showCanvasPicker() {
        String[] options = {"Square · 1024 × 1024", "Portrait · 900 × 1200",
                "Landscape · 1200 × 900", "HD · 1600 × 900"};
        new AlertDialog.Builder(this).setTitle("Start a new canvas")
            .setMessage("Choose your paper size. You can draw with a finger or compatible stylus.")
            .setItems(options, (d, which) -> {
                int w = which == 0 ? 1024 : which == 1 ? 900 : 1200;
                int h = which == 0 ? 1024 : which == 1 ? 1200 : which == 2 ? 900 : 900;
                buildEditor(w, h);
            }).setCancelable(false).show();
    }

    private void buildEditor(int w, int h) {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(242, 241, 237));
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(text("PIGMENT  /  ART HUB", 17), new LinearLayout.LayoutParams(0, dp(52), 1));
        top.addView(button("New", this::showCanvasPicker));
        top.addView(button("Undo", () -> { if (paintView != null) paintView.undo(); }));
        top.addView(button("Clear", () -> { if (paintView != null) paintView.clear(); }));
        top.addView(button("Export", this::exportPng));
        root.addView(top);

        HorizontalScrollView toolsScroll = new HorizontalScrollView(this);
        LinearLayout tools = new LinearLayout(this);
        tools.setGravity(Gravity.CENTER_VERTICAL);
        String[] brushNames = {"Round", "Pencil", "Ink", "Flat", "Wide", "Marker", "Airbrush", "Watercolor", "Eraser"};
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brushNames));
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                brush = brushNames[pos]; if (paintView != null) paintView.setBrush(brush);
            }
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        tools.addView(text("Brush", 13)); tools.addView(spinner, new LinearLayout.LayoutParams(dp(145), dp(48)));
        tools.addView(button("−", () -> { brushSize = Math.max(1, brushSize - 2); updateBrushInfo(); }));
        tools.addView(button("+", () -> { brushSize = Math.min(100, brushSize + 2); updateBrushInfo(); }));
        tools.addView(button("Palette +", this::addColorDialog));
        tools.addView(button("Mix", this::mixCurrentColor));
        toolsScroll.addView(tools); root.addView(toolsScroll);

        TextView info = text("Size: 14 px · Pressure controls stroke width", 13);
        info.setTag("brushInfo"); root.addView(info);
        SeekBar opacityBar = new SeekBar(this); opacityBar.setMax(100); opacityBar.setProgress(92);
        opacityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int value, boolean user) {
                opacity = Math.max(0.05f, value / 100f); if (paintView != null) paintView.setOpacity(opacity);
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });
        root.addView(text("Paint opacity", 12)); root.addView(opacityBar, new LinearLayout.LayoutParams(-1, dp(30)));

        HorizontalScrollView paletteScroll = new HorizontalScrollView(this);
        LinearLayout swatches = new LinearLayout(this); swatches.setPadding(dp(6), dp(3), dp(6), dp(3));
        for (int c : palette) addSwatch(swatches, c);
        paletteScroll.addView(swatches); root.addView(paletteScroll);
        paintView = new PaintView(this, w, h); paintView.setColor(selectedColor);
        root.addView(paintView, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(text("Lenovo pen: pressure/tilt are used when reported by your device. Two-finger gestures are reserved for Android navigation.", 11));
        setContentView(root);
    }

    private void updateBrushInfo() {
        if (root == null) return;
        for (int i=0;i<root.getChildCount();i++) {
            View v=root.getChildAt(i);
            if (v instanceof TextView && "brushInfo".equals(v.getTag())) {
                ((TextView)v).setText("Size: " + Math.round(brushSize) + " px · Pressure controls stroke width"); break;
            }
        }
        if (paintView != null) paintView.setBrushSize(brushSize);
    }
    private void addSwatch(LinearLayout row, int color) {
        Button b = new Button(this); b.setText("●"); b.setTextColor(color);
        b.setTextSize(22); b.setMinWidth(dp(42)); b.setPadding(0,0,0,0);
        b.setOnClickListener(v -> { selectedColor=color; if(paintView!=null)paintView.setColor(color); });
        row.addView(b, new LinearLayout.LayoutParams(dp(46), dp(46)));
    }
    private void addColorDialog() {
        final String[] names={"Red","Orange","Yellow","Green","Teal","Blue","Purple","Pink","Brown","Black","White","Custom…"};
        final int[] colors={Color.RED,0xffff8a00,Color.YELLOW,0xff35a853,0xff00a99d,Color.BLUE,0xff8e44ad,0xffff69b4,0xff795548,Color.BLACK,Color.WHITE,0xff3366cc};
        new AlertDialog.Builder(this).setTitle("Add a palette color").setItems(names,(d,which)->{
            if(which==11) {
                LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
                SeekBar r=bar(0),g=bar(100),b=bar(210);
                box.addView(text("Red",12));box.addView(r);box.addView(text("Green",12));box.addView(g);box.addView(text("Blue",12));box.addView(b);
                new AlertDialog.Builder(this).setTitle("Custom RGB color").setView(box).setPositiveButton("Add",(dd,ww)->{
                    int c=Color.rgb(r.getProgress(),g.getProgress(),b.getProgress()); palette.add(c); selectedColor=c;
                    if(paintView!=null)paintView.setColor(c); refreshPalette();
                }).setNegativeButton("Cancel",null).show();
            } else { palette.add(colors[which]); selectedColor=colors[which]; if(paintView!=null)paintView.setColor(selectedColor); refreshPalette(); }
        }).show();
    }
    private SeekBar bar(int val){SeekBar s=new SeekBar(this);s.setMax(255);s.setProgress(val);return s;}
    private void refreshPalette() {
        if(root==null)return;
        HorizontalScrollView hs=null;
        for(int i=0;i<root.getChildCount();i++) if(root.getChildAt(i) instanceof HorizontalScrollView && i>1){hs=(HorizontalScrollView)root.getChildAt(i);}
        if(hs!=null){LinearLayout row=new LinearLayout(this);for(int c:palette)addSwatch(row,c);hs.removeAllViews();hs.addView(row);}
    }
    private void mixCurrentColor() {
        int mixed = PaintView.mixPigments(selectedColor, Color.rgb(255, 210, 70));
        selectedColor=mixed; if(paintView!=null)paintView.setColor(mixed);
        Toast.makeText(this,"Mixed with warm yellow pigment",Toast.LENGTH_SHORT).show();
    }
    private void exportPng() {
        if(paintView==null)return;
        try {
            Bitmap bitmap=paintView.exportBitmap();
            File dir=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if(dir==null)throw new Exception("Storage unavailable");
            if(!dir.exists())dir.mkdirs();
            File out=new File(dir,"Pigment-"+System.currentTimeMillis()+".png");
            FileOutputStream stream=new FileOutputStream(out); bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);stream.close();
            Toast.makeText(this,"PNG saved: "+out.getAbsolutePath(),Toast.LENGTH_LONG).show();
        } catch(Exception e){Toast.makeText(this,"Export failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

    private class PaintView extends View {
        private final int canvasW, canvasH;
        private Bitmap layer;
        private Canvas layerCanvas;
        private Paint p = new Paint(3);
        private Path path = new Path();
        private float lastX,lastY, lastPressure=0.5f, tilt=0;
        private int color=Color.BLUE;
        private float size=14, alpha=0.92f;
        private String kind="Round";
        private final ArrayList<Bitmap> undo = new ArrayList<>();
        private boolean drawing=false;
        PaintView(Activity c,int w,int h){super(c);canvasW=w;canvasH=h;setLayerType(View.LAYER_TYPE_SOFTWARE,null);initLayer();}
        private void initLayer(){layer=Bitmap.createBitmap(canvasW,canvasH,Bitmap.Config.ARGB_8888);layer.eraseColor(Color.WHITE);layerCanvas=new Canvas(layer);}
        void setColor(int c){color=c;}
        void setBrush(String b){kind=b;}
        void setBrushSize(float s){size=s;}
        void setOpacity(float a){alpha=a;}
        void clear(){saveUndo();layer.eraseColor(Color.WHITE);invalidate();}
        void undo(){if(undo.size()>0){layer=undo.remove(undo.size()-1);layerCanvas=new Canvas(layer);invalidate();}}
        private void saveUndo(){
            if(undo.size()>=8)undo.remove(0);
            undo.add(layer.copy(Bitmap.Config.ARGB_8888,true));
        }
        Bitmap exportBitmap(){return layer.copy(Bitmap.Config.ARGB_8888,false);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            canvas.drawColor(0xffd7d5cf);
            float scale=Math.min((getWidth()-dp(16))/(float)canvasW,(getHeight()-dp(16))/(float)canvasH);
            float dw=canvasW*scale,dh=canvasH*scale;
            float left=(getWidth()-dw)/2f, top=(getHeight()-dh)/2f;
            canvas.save();canvas.translate(left,top);canvas.scale(scale,scale);
            Paint shadow=new Paint(3);shadow.setColor(Color.WHITE);
            canvas.drawRect(0,0,canvasW,canvasH,shadow);canvas.drawBitmap(layer,0,0,null);
            canvas.restore();
            // Keep view-space transform available to touch handlers.
        }
        private float scale(){return Math.min((getWidth()-dp(16))/(float)canvasW,(getHeight()-dp(16))/(float)canvasH);}
        private float offX(){return (getWidth()-canvasW*scale())/2f;}
        private float offY(){return (getHeight()-canvasH*scale())/2f;}
        @Override public boolean onTouchEvent(MotionEvent e){
            int action=e.getActionMasked();
            if(e.getPointerCount()>1)return true;
            float sc=scale();
            float x=(e.getX()-offX())/sc, y=(e.getY()-offY())/sc;
            if(x<0||y<0||x>canvasW||y>canvasH)return true;
            float pressure=e.getPressure();
            if(pressure<=0)pressure=0.5f;
            int tool=e.getToolType(0);
            if(action==MotionEvent.ACTION_DOWN){
                saveUndo();drawing=true;lastX=x;lastY=y;lastPressure=pressure;
                path.reset();path.moveTo(x,y);drawDab(x,y,pressure,e);invalidate();return true;
            } else if(action==MotionEvent.ACTION_MOVE && drawing){
                int hist=e.getHistorySize();
                for(int i=0;i<hist;i++){
                    float hx=(e.getHistoricalX(0,i)-offX())/sc,hy=(e.getHistoricalY(0,i)-offY())/sc;
                    float hp=e.getHistoricalPressure(0,i);if(hp<=0)hp=0.5f;
                    segment(hx,hy,hp,e.getHistoricalAxisValue(MotionEvent.AXIS_TILT,0,i));
                }
                segment(x,y,pressure,e.getAxisValue(MotionEvent.AXIS_TILT));
                invalidate();return true;
            } else if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){
                drawing=false;invalidate();return true;
            }
            return true;
        }
        private void segment(float x,float y,float pressure,float t){
            float dx=x-lastX,dy=y-lastY,dist=(float)Math.hypot(dx,dy);
            int steps=Math.max(1,(int)(dist/Math.max(1,size*0.16f)));
            for(int i=1;i<=steps;i++){
                float f=i/(float)steps;
                drawDab(lastX+dx*f,lastY+dy*f,lastPressure+(pressure-lastPressure)*f,t);
            }
            lastX=x;lastY=y;lastPressure=pressure;tilt=t;
        }
        private void drawDab(float x,float y,float pressure,MotionEvent e){drawDab(x,y,pressure,e.getAxisValue(MotionEvent.AXIS_TILT));}
        private void drawDab(float x,float y,float pressure,float t){
            float pressureFactor=0.18f+Math.min(1f,Math.max(0f,pressure))*1.45f;
            float radius=Math.max(0.5f,size*pressureFactor);
            p.reset();p.setAntiAlias(true);p.setDither(true);
            int a=(int)(255*alpha);
            p.setColor((color&0x00ffffff)|(a<<24));
            p.setAlpha(a);
            if(kind.equals("Eraser")){p.setColor(Color.WHITE);p.setAlpha(255);}
            if(kind.equals("Pencil")){p.setAlpha((int)(a*0.55f));p.setStrokeWidth(Math.max(1,radius*0.6f));p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);layerCanvas.drawPoint(x,y,p);}
            else if(kind.equals("Flat")||kind.equals("Wide")){
                float angle=(float)(t>0.02 ? t*1.4 : 0.35);
                float halfW=kind.equals("Wide")?radius*1.6f:radius*0.7f;
                float halfH=kind.equals("Wide")?radius*0.42f:radius*0.25f;
                p.setStyle(Paint.Style.FILL);
                layerCanvas.save();layerCanvas.rotate((float)Math.toDegrees(angle),x,y);
                layerCanvas.drawRoundRect(new RectF(x-halfW,y-halfH,x+halfW,y+halfH),halfH*0.18f,halfH*0.18f,p);
                layerCanvas.restore();
            } else if(kind.equals("Airbrush")){
                p.setAlpha(Math.max(1,a/12));p.setMaskFilter(new android.graphics.BlurMaskFilter(radius*1.8f,android.graphics.BlurMaskFilter.Blur.NORMAL));
                layerCanvas.drawCircle(x,y,radius*1.7f,p);p.setMaskFilter(null);
            } else if(kind.equals("Watercolor")){
                p.setAlpha(Math.max(1,a/4));p.setMaskFilter(new android.graphics.BlurMaskFilter(radius*0.55f,android.graphics.BlurMaskFilter.Blur.NORMAL));
                layerCanvas.drawCircle(x,y,radius*1.1f,p);p.setMaskFilter(null);
            } else {
                if(kind.equals("Marker"))p.setAlpha((int)(a*0.65f));
                if(kind.equals("Ink"))radius*=Math.max(0.45f,pressure);
                p.setStyle(Paint.Style.FILL);layerCanvas.drawCircle(x,y,radius,p);
            }
        }
        static int mixPigments(int c1,int c2){
            // Kubelka-Munk-inspired approximation: combine optical absorption in linear RGB.
            float[] a=toLinear(c1), b=toLinear(c2);
            float r=combine(a[0],b[0]),g=combine(a[1],b[1]),bl=combine(a[2],b[2]);
            return Color.rgb(toSrgb(r),toSrgb(g),toSrgb(bl));
        }
        private static float[] toLinear(int c){return new float[]{linear(Color.red(c)/255f),linear(Color.green(c)/255f),linear(Color.blue(c)/255f)};}
        private static float linear(float x){return x<=0.04045f?x/12.92f:(float)Math.pow((x+0.055f)/1.055f,2.4);}
        private static int toSrgb(float x){float s=x<=0.0031308f?12.92f*x:1.055f*(float)Math.pow(x,1/2.4)-0.055f;return Math.max(0,Math.min(255,Math.round(s*255)));}
        private static float combine(float a,float b){float ka=(1-a)*(1-a)/(2*Math.max(0.02f,a));float kb=(1-b)*(1-b)/(2*Math.max(0.02f,b));float k=(ka+kb)*0.5f;return 1f/(1f+k+(float)Math.sqrt(k*k+2*k));}
    }
}
