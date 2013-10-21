package pl.wsiz.rzeszowlocator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class ActivityAdd extends SherlockActivity implements OnClickListener {
	private Bitmap bitmap;
	private EditText etName;
	private EditText etDesc;
	private Button btnAdd;
	private Button btnCoords;
	private Button btnPhoto;
	private TextView tvCoordsOk;
	private TextView tvPhotoOk;
	
	private double lat = 0;
	private double lon = 0;
	private String photo = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		etDesc = (EditText) findViewById(R.id.etDesc);
		etName = (EditText) findViewById(R.id.etTitle);
		
		tvCoordsOk = (TextView) findViewById(R.id.tvCoordsOk);
		tvPhotoOk = (TextView) findViewById(R.id.tvPhotoOk);
		
		btnAdd = (Button) findViewById(R.id.btnAdd);
		btnCoords = (Button) findViewById(R.id.btnCoords);
		btnPhoto = (Button) findViewById(R.id.btnPhoto);
		
		btnAdd.setOnClickListener(this);
		btnCoords.setOnClickListener(this);
		btnPhoto.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.btnAdd:
			if (lat==0 || lon==0) {
				Toast.makeText(this, "Enter coordinates", Toast.LENGTH_SHORT).show();
				break;
			}
			if (etName.getText().toString().equalsIgnoreCase("")) {
				Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
				break;
			}
			if (etDesc.getText().toString().equalsIgnoreCase("")) {
				Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show();
				break;
			}
			if (photo.equalsIgnoreCase("")) {
				Toast.makeText(this, "Take a photo", Toast.LENGTH_SHORT).show();
				break;
			}
			new ImageUploadTask().execute();
			break;
			
		case R.id.btnCoords:
			tvCoordsOk.setVisibility(View.INVISIBLE);
			intent = new Intent(this, ActivityMap.class);
			intent.putExtra("serv", true);
			startActivityForResult(intent, 100);
			break;
			
		case R.id.btnPhoto:
			tvPhotoOk.setVisibility(View.INVISIBLE);
			intent = new Intent(this, ActivityPhoto.class);
			startActivityForResult(intent, 200);
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 100:
				tvCoordsOk.setVisibility(View.VISIBLE);
				lat = data.getDoubleExtra("lat", 0);
				lon = data.getDoubleExtra("lon", 0);
				break;
			case 200:
				tvPhotoOk.setVisibility(View.VISIBLE);
				photo = data.getStringExtra("path");
				bitmap = BitmapFactory.decodeFile(photo);
				break;
			}
		} else {
			Toast.makeText(this, "Error! Please try again!", Toast.LENGTH_SHORT).show();
		}
	}

	class ImageUploadTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... unsued) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();
				HttpPost httpPost = new HttpPost("http://locator.byethost7.com/upload.php");

				MultipartEntityBuilder entity = MultipartEntityBuilder.create();

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.JPEG, 100, bos);
				byte[] data = bos.toByteArray();
				entity.addTextBody("name", etName.getText().toString());
				entity.addTextBody("descr", etDesc.getText().toString());
				entity.addTextBody("lat", ""+lat);
				entity.addTextBody("lon", ""+lon);
				entity.addPart("userfile", new ByteArrayBody(data,photo.substring(photo.lastIndexOf("/"))));
				
				httpPost.setEntity(entity.build());
				HttpResponse response = httpClient.execute(httpPost,
						localContext);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));

				String sResponse = reader.readLine();
				return sResponse;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(String sResponse) {
			try {
				if (sResponse != null) {
					Log.i(ActivityMain.TAG,	sResponse);

					if (sResponse.charAt(0) == '0') {
						Toast.makeText(getApplicationContext(), "Added! Now you must update menu to see your place",
								Toast.LENGTH_LONG).show();
						finish();
					} else {
						Toast.makeText(getApplicationContext(),
								"Uploading error! Please, try again.",
								Toast.LENGTH_SHORT).show();
						etDesc.setText("");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
