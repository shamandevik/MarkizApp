package app.devlens;
import android.app.*;import android.content.*;import android.os.*;import android.hardware.Camera;import android.view.*;import android.widget.*;import com.google.zxing.*;import com.google.zxing.common.HybridBinarizer;
@SuppressWarnings("deprecation") public class ScanActivity extends Activity implements SurfaceHolder.Callback,Camera.PreviewCallback{
 Camera camera;boolean busy;long last;TextView tip;
 @Override public void onCreate(Bundle b){super.onCreate(b);android.widget.FrameLayout root=new android.widget.FrameLayout(this);SurfaceView surface=new SurfaceView(this);root.addView(surface);tip=new TextView(this);tip.setText("DEVLENS / SCAN\n\nНаведите камеру на QR-код\nСодержимое не выполняется автоматически");tip.setTextColor(0xffb5f56b);tip.setTextSize(20);tip.setPadding(30,50,30,30);tip.setBackgroundColor(0x880b1017);root.addView(tip);setContentView(root);surface.getHolder().addCallback(this);}
 public void surfaceCreated(SurfaceHolder h){try{camera=Camera.open();camera.setDisplayOrientation(90);Camera.Parameters p=camera.getParameters();if(p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);camera.setParameters(p);camera.setPreviewDisplay(h);camera.setPreviewCallback(this);camera.startPreview();}catch(Exception e){tip.setText("Камера недоступна: "+e.getMessage());}}
 public void surfaceChanged(SurfaceHolder h,int f,int w,int height){}public void surfaceDestroyed(SurfaceHolder h){close();}
 void close(){if(camera!=null){camera.setPreviewCallback(null);camera.stopPreview();camera.release();camera=null;}}
 public void onPreviewFrame(byte[] data,Camera c){if(busy||System.currentTimeMillis()-last<300)return;last=System.currentTimeMillis();busy=true;try{Camera.Size s=c.getParameters().getPreviewSize();Result r=new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new PlanarYUVLuminanceSource(data,s.width,s.height,0,0,s.width,s.height,false))));setResult(RESULT_OK,new Intent().putExtra("text",r.getText()));finish();}catch(Exception ignored){}finally{busy=false;}}
 @Override protected void onPause(){close();super.onPause();}
}
