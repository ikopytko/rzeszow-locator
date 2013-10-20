package pl.wsiz.rzeszowlocator;

import java.io.File;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class ActivityDetails extends SherlockActivity implements OnClickListener{
	ImageView iv;
	TextView title;
	TextView desc;
	Button btnToMap;
	
	String name;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		iv = (ImageView) findViewById(R.id.imageView1);
		title = (TextView) findViewById(R.id.tvTitle);
		desc = (TextView) findViewById(R.id.tvDescr);
		btnToMap = (Button) findViewById(R.id.btnShowOnMap);
		
		btnToMap.setOnClickListener(this);
		
		Intent intent = this.getIntent();
		name = intent.getStringExtra("name");
		Log.i("TAG",  "Name: "+name);
		
		DBHelper dbHelper = null;
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			dbHelper = new DBHelper(this, "itemStore");
			db = dbHelper.getWritableDatabase();
			String[] selectionArgs = new String[] { name };
			c = db.query("itemStore", null, "name = ?", selectionArgs, null, null, null);
			
			if (c == null || !c.moveToFirst()) {
				Toast.makeText(ActivityDetails.this, "DB error",
						Toast.LENGTH_LONG).show();
				return;
			}
			
			File imgFile = new  File(ActivityMain.locations + c.getString(c.getColumnIndex("img")));
			if(imgFile.exists()){
				Bitmap bitmap  = BitmapFactory.decodeFile(ActivityMain.locations
					+ c.getString(c.getColumnIndex("img")));
			
				iv.setImageBitmap(bitmap);
			} else {
				iv.setImageResource(R.drawable.ic_launcher);
			}
			title.setText(c.getString(c.getColumnIndex("name")));
			desc.setText(c.getString(c.getColumnIndex("description")));
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

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnShowOnMap) {
			Intent intent = new Intent(this, ActivityMap.class);
			intent.putExtra("name", name);
			startActivity(intent);
		}
	}
}
