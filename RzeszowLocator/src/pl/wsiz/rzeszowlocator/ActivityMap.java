package pl.wsiz.rzeszowlocator;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ActivityMap extends SherlockFragmentActivity {
	
	class MyInfoWindowAdapter implements InfoWindowAdapter{

		private final View myContentsView;
		
		MyInfoWindowAdapter(){
			myContentsView = getLayoutInflater().inflate(R.layout.info_window, null);
		}
		
		@Override
		public View getInfoContents(Marker marker) {
			TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            //TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
            //tvSnippet.setText(marker.getSnippet());
            ImageView ivIcon = ((ImageView)myContentsView.findViewById(R.id.imgPw));
            //ivIcon.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_gallery));
            
            Bitmap bitmap  = BitmapFactory.decodeFile(marker.getSnippet());
            ivIcon.setImageBitmap(bitmap);
            return myContentsView;
		}

		@Override
		public View getInfoWindow(Marker marker) {
			return null;
		}
	}
	
	GoogleMap mMap;
	String name = "";

	@Override
	public void onCreate(Bundle savedInstanceData) {
		super.onCreate(savedInstanceData);
		setContentView(R.layout.activity_map);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = this.getIntent();
		if (intent.hasExtra("name"))
			name = intent.getStringExtra("name");
		
		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		
		//mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());

		addMarkers();
	}
	
	void addMarkers() {
		DBHelper dbHelper = null;
		SQLiteDatabase db = null;
		Cursor c = null;
		
		try {
			dbHelper = new DBHelper(this, "itemStore");
			db = dbHelper.getWritableDatabase();
			
			if (name.equals("")) { // all
				c = db.query("itemStore", null, null, null, null, null, null);
				Log.i("TAG", "Count" + c.getCount());
			} else {
				String[] selectionArgs = new String[] { name };
				c = db.query("itemStore", null, "name = ?", selectionArgs, null, null, null);
			}
			
			if (c == null || !c.moveToFirst()) {
				Toast.makeText(ActivityMap.this, "DB error",
						Toast.LENGTH_LONG).show();
				return;
			}
			
			LatLng RZESZOW;
			do {
				RZESZOW = new LatLng(Double.parseDouble(c.getString(c.getColumnIndex("lat"))),
						Double.parseDouble(c.getString(c.getColumnIndex("lon"))));
				mMap.addMarker(new MarkerOptions().position(RZESZOW).snippet(ActivityMain.locations + c.getString(c.getColumnIndex("img")))).
					setTitle(c.getString(c.getColumnIndex("name")));
			} while (c.moveToNext());
			
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(RZESZOW, 13));
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dbHelper != null)
				dbHelper.close();
			if (db != null)
				db.close();
			if (c != null)
				c.close();
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