package pl.wsiz.rzeszowlocator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public DBHelper(Context context, String db_name) {
		super(context, db_name, null, 1);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table itemStore ("
				+ "id integer primary key autoincrement," + "name text,"
				+ "description text," + "desc text," + "img text,"
				+ "lat text," + "lon text" +  ");" );
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
