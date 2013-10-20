package pl.wsiz.rzeszowlocator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

class Download extends AsyncTask<Void, Integer, Void> {

	boolean downloadBig = false;
	private ProgressBar pb;
	private TextView tv;
	Context context;
	
	public Download(ProgressBar pb, TextView tv, Context context) {
		this.pb = pb;
		this.tv = tv;
		this.context = context;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pb.setIndeterminate(false);
		pb.setVisibility(View.VISIBLE);
		tv.setVisibility(View.VISIBLE);
		tv.setText("Downloaded 0 items from %");
		
		getNetworkState();
		
		boolean success = (new File(ActivityMain.locations)).mkdir(); 
        if (!success)
            Log.w(ActivityMain.TAG, "directory not created");
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		Log.w(ActivityMain.TAG, "doInBackground");
		String readPlaces = readPlaces();
		
		DBHelper dbHelper = null;
		SQLiteDatabase db = null;
		
		int progress = 0;
		
		try {
			dbHelper = new DBHelper(context, "itemStore");
			db = dbHelper.getWritableDatabase();
			
			JSONArray jsonArray = new JSONArray(readPlaces);
			ContentValues cv = new ContentValues();
			String pic;
			int length = jsonArray.length();
			
			for (int i = 0; i < length; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				if (downloadBig)
					pic = downloadPic(jsonObject.getString("bimg"));
				else 
					pic = downloadPic(jsonObject.getString("simg"));
				
				cv.put("name", jsonObject.getString("name"));
				cv.put("description", jsonObject.getString("description"));
				cv.put("img", pic);
				cv.put("lat", jsonObject.getDouble("lat"));
				cv.put("lon", jsonObject.getDouble("lon"));
				
				db.insert("itemStore", null, cv);
				cv.clear();
				progress = (100/length)*i;
				publishProgress(progress, i, length);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dbHelper!=null)
				dbHelper.close();
			if (db!=null)
				db.close();
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		pb.setProgress(values[0]);
		tv.setText("Downloaded " + values[1] + " items from " + values[2]);
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		Log.w(ActivityMain.TAG, "onPostExecute");
		pb.setVisibility(View.GONE);
		tv.setVisibility(View.GONE);
		tv.setText("");
		((ActivityMain) context).updateList();
	}
	
	protected void getNetworkState() {
		ConnectivityManager cm =
		        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		
		if( activeNetwork == null ||
                !activeNetwork.isConnectedOrConnecting()) {
			Toast.makeText(context, "Check your Internet access",
					Toast.LENGTH_LONG).show();
			return;
		}
		
		downloadBig = (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) ||
				(activeNetwork.getType() == ConnectivityManager.TYPE_WIMAX);
	}
	
	public String readPlaces() {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(
				"http://locator.byethost7.com/a.json");
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(ActivityMain.TAG, "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}
	
	public String downloadPic(String picName) {
		
        try
        {
        	Log.w(ActivityMain.TAG, "http://locator.byethost7.com/"+picName);
            URL url = new URL("http://locator.byethost7.com/"+picName);
            
            URLConnection ucon = url.openConnection();

            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            File file = new File(ActivityMain.locations, picName);

            ByteArrayBuffer baf = new ByteArrayBuffer(5000);
            int current = 0;
            while ((current = bis.read()) != -1) {
               baf.append((byte) current);
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.flush();
            fos.close();
            
            
            // Create small bitmap for listView
            
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize=2; //try to decrease decoded image 

            Bitmap baseBitmap = BitmapFactory.decodeFile(ActivityMain.locations + picName, options);
            Bitmap resized = Bitmap.createScaledBitmap(baseBitmap, 120, 120, true);
            
            File fileT = new File(ActivityMain.locations, "_"+picName);
            fileT.createNewFile();
            FileOutputStream ostream = new FileOutputStream(fileT);
            resized.compress(CompressFormat.PNG, 65, ostream);
            ostream.close(); 
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return picName;
        
	}
}