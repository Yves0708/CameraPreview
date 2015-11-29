package com.xcodeon.android.camerapreview;

import java.io.IOException;
import android.hardware.Camera;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements SurfaceHolder.Callback{

	private SurfaceHolder surfaceHolder;
	private Camera myCamera;
	private SurfaceView surfaceView;
	private int cameraId=0; //自動校正顯示角度 背鏡頭cameraID=0；前鏡頭cameraID=1
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//把標題列關掉
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		//定向
		this.setRequestedOrientation(1);
        //在AndroidManifest.xml中設定或是用下面的setRequestedOrientation(0)設定也可以
        //0代表橫向、1代表縱向
		precessView();
	}

	private void precessView(){
		surfaceView = (SurfaceView) findViewById(R.id.cameraSurfaceView);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);//this表示是實作callback的物件實體
		surfaceHolder.setType(surfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//推到surfaceView
		surfaceHolder.setKeepScreenOn(true);//讓螢幕常亮
		surfaceView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				if(Camera.getNumberOfCameras()>1){
					cameraId = cameraId==0? 1 : 0;
					initialCamera();
					myCamera.startPreview();//開始呈現畫面
					return true;
				}
				
				return true;
			}
		});
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		initialCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		if(myCamera!=null){
			Camera.Parameters parameters = myCamera.getParameters();//得到initial參數
			myCamera.setParameters(parameters);
			myCamera.startPreview();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if(myCamera!=null){
			myCamera.stopPreview();
			myCamera.release();//釋放相機
			myCamera=null;
		}
	}
	
	void initialCamera(){
		if(myCamera!=null){
			myCamera.release();
			myCamera=null;
		}
		try{
			myCamera = Camera.open(cameraId);
			if(myCamera!=null){
				Camera.Parameters parameters = myCamera.getParameters();
				parameters.setZoom(0);//拉到最近
				myCamera.setParameters(parameters);
				myCamera.setPreviewDisplay(surfaceHolder);
				setCameraDisplayOrientation(MainActivity.this, cameraId, myCamera);
			}
		}catch(IOException e){
			if(myCamera!=null){
				myCamera.release();
				myCamera = null;
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Camera.Parameters parameters = myCamera.getParameters();
		switch(keyCode){
		case KeyEvent.KEYCODE_VOLUME_UP:
			if(parameters.getZoom()<parameters.getMaxZoom()){
				parameters.setZoom(parameters.getZoom()+1);
				myCamera.setParameters(parameters);
			}else{
				Toast.makeText(this, "MAX Zoom IN", Toast.LENGTH_SHORT).show();
			}
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if(parameters.getZoom()>0){
				parameters.setZoom(parameters.getZoom()-1);
				myCamera.setParameters(parameters);
			}else{
				Toast.makeText(this, "MAX Zoom OUT", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public static void setCameraDisplayOrientation(Activity activity,
	         int cameraId, Camera camera) {
		//取得相機資訊
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		//取得螢幕顯示角度
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch(rotation){
		case Surface.ROTATION_0: degrees = 0; break;
		case Surface.ROTATION_90: degrees = 90; break;
		case Surface.ROTATION_180: degrees = 180; break;
		case Surface.ROTATION_270: degrees = 270; break;
		}
		int result;
		//修改相機資訊
		if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
			result = (info.orientation+degrees)%360;
			result = (360-result)%360;
		}else{//背鏡頭
			result = (info.orientation-degrees+360)%360;
		}
		camera.setDisplayOrientation(result);//更改相機的旋轉角度
	 }
}
