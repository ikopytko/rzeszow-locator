package pl.wsiz.rzeszowlocator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ActivityMain extends SherlockActivity {

	final static String TAG = "rzeszowLocator";
	public final static String locations = Environment.getExternalStorageDirectory()
			+ File.separator + "RzeszowLocator" + File.separator;
	ArrayList<HashMap<String, Object>> items;
	HashMap<String, Object> hm;
	
	DBHelper dbHelper;
	SQLiteDatabase db;
	Cursor c = null;
	
	ListView listView;
	TextView pbTitle;
	ProgressBar pb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		items = new ArrayList<HashMap<String, Object>>();
		listView = (ListView) findViewById(R.id.listView1);
		pb = (ProgressBar) findViewById(R.id.progressBar1);
		pbTitle = (TextView) findViewById(R.id.pbTitle);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			   @SuppressWarnings("unchecked")
			@Override
			   public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				   Intent intent1 = new Intent(ActivityMain.this, ActivityDetails.class);
					HashMap<String, Object> hm /*= new HashMap<String, Object>()*/;
					hm = (HashMap<String, Object>) listView.getItemAtPosition(position);
					intent1.putExtra("name", hm.get("name").toString());
					startActivity(intent1);
			   }
			 });
		
		updateList();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuinf = getSupportMenuInflater();
		menuinf.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuUpd:
			DBHelper dbHelper = new DBHelper(this, "itemStore");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete("itemStore", null, null);
			dbHelper.close();
			db.close();
			items.clear();
			File file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "RzeszowLocator", "");
		    if (file != null && file.isDirectory()) {
		        File[] files = file.listFiles();
		        if(files != null) {
		            for(File f : files) {   
		                f.delete();
		            }
		        }
		    }
			Download sc = new Download(pb, pbTitle, this);
			sc.execute();
			break;
			
		case R.id.menuShowAll:
			startActivity(new Intent(this, ActivityMap.class));
			break;
			
		case R.id.menuExit:
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	void updateList() {
		try {
			dbHelper = new DBHelper(this, "itemStore");
			db = dbHelper.getWritableDatabase();
			c = db.query("itemStore", null, null, null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.main_list,
				new String[] {
					"img",
					"name",
					"desc"
				}, new int[] {
					R.id.ivImg,
					R.id.tvTitle,
					R.id.tvDesc});
		listView.setAdapter(adapter);
		
		if (c != null && c.moveToFirst()) { // if table isnt empty
			do {
				hm = new HashMap<String, Object>();
				hm.put("name", c.getString(c.getColumnIndex("name")));
				hm.put("desc", c.getString(c.getColumnIndex("description")).substring(0, 25)+"...");
				
				File imgFile = new  File(locations + c.getString(c.getColumnIndex("img")));
				if(imgFile.exists()){
					hm.put("img", locations + c.getString(c.getColumnIndex("img")));
				} else {
					hm.put("img", R.drawable.ic_launcher);
				}
				
				items.add(hm);
				adapter.notifyDataSetChanged();
			} while (c.moveToNext());
			
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		} else {
			Download sc = new Download(pb, pbTitle, this);
			sc.execute();
		}
		dbHelper.close();
		db.close();
		c.close();
	}
}
