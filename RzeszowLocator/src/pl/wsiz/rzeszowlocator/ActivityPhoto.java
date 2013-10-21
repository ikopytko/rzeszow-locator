package pl.wsiz.rzeszowlocator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class ActivityPhoto extends SherlockActivity implements OnClickListener {
	Button btnTakePic;
	SurfaceView surfaceView;
	Camera camera;
	File photoFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		btnTakePic = (Button) findViewById(R.id.btnTakePicture);
		btnTakePic.setOnClickListener(this);

		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

		SurfaceHolder holder = surfaceView.getHolder();
		holder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				try {
					camera.setPreviewDisplay(holder);
					camera.startPreview();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
			}
		});
		
		Random rnd = new Random();
		photoFile = new File(ActivityMain.locations +(1000+rnd.nextInt(9000))+".jpg");
	}

	@Override
	public void onClick(View v) {
		camera.takePicture(null, null, new PictureCallback() {
		      @Override
		      public void onPictureTaken(byte[] data, Camera camera) {
		        try {
		          FileOutputStream fos = new FileOutputStream(photoFile);
		          fos.write(data);
		          fos.close();
		          Intent intent = new Intent();
		  		  intent.putExtra("path", photoFile.getPath());
		  		  setResult(RESULT_OK, intent);
		  		  finish();
		        } catch (Exception e) {
		          e.printStackTrace();
		        }
		      }
		    });
	}

	@Override
	protected void onResume() {
		super.onResume();
		camera = Camera.open();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (camera != null)
			camera.release();
		camera = null;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
