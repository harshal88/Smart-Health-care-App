package com.unique.smarthealthcare;

import java.util.HashMap;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class LoginDataBaseAdapter {

	static final String DATABASE_NAME = "login.db";
	static final int DATABASE_VERSION = 1;
	public static final int NAME_COLUMN = 3;
	
	
	

	static final String DATABASE_CREATE = "create table " + "LOGIN" + "( "
			+ "ID integer primary key autoincrement," + "PASSWORD text,"
			+ "SECURITYHINT text," +"AGE integer," + "FREQUENCY text," + "HOME_ZIP text," +"WORK_ZIP text," +"SCHOOL_ZIP text," +"EMAIL text) ";

	public SQLiteDatabase db;
	private final Context context;
	private DataBaseHelper dbHelper;

	public LoginDataBaseAdapter(Context _context) {
		context = _context;
		dbHelper = new DataBaseHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);

	}

	public LoginDataBaseAdapter open() throws SQLException {
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		db.close();
	}

	public SQLiteDatabase getDatabaseInstance() {
		return db;
	}

	public void insertEntry(String password,
			String securityhint) {
		ContentValues newValues = new ContentValues();
		newValues.put("PASSWORD", password);
	//	newValues.put("REPASSWORD", repassword);
		newValues.put("SECURITYHINT", securityhint);

		db.insert("LOGIN", null, newValues);
		db.close();
	}

	public int deleteEntry(String password) {
		String where = "PASSWORD=?";
		int numberOFEntriesDeleted = db.delete("LOGIN", where,
				new String[] { password });
		return numberOFEntriesDeleted;
	}

	public String getSinlgeEntry(String password) {
		Cursor cursor = db.query("LOGIN", null, " PASSWORD=?",
				new String[] { password }, null, null, null);
		if (cursor.getCount() < 1) // UserName Not Exist
		{
			cursor.close();
			return "NOT EXIST";
		}
		cursor.moveToFirst();
		String password1 = cursor.getString(cursor
				.getColumnIndex("PASSWORD"));
		cursor.close();
		return password1;
	}

	public String getAllTags(String sec) {

		String selectQuery = "Select * FROM " + " LOGIN " +  " WHERE "  + " SECURITYHINT " + "= '" + sec
				+ "' ";
		db = dbHelper.getWritableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);
		String str = null;
	
		if (c.moveToFirst()) {
		do {
		str = c.getString(c.getColumnIndex("PASSWORD"));
		} while (c.moveToNext());
		}
		
		return str;
		
		}

	public int updateEntry(String password, String repassword) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put("PASSWORD", repassword);
		//updatedValues.put("REPASSWORD", repassword);
		updatedValues.put("SECURITYHINT", repassword);

		String where = "PASSWORD = ?";
		return db.update("LOGIN", updatedValues, where, new String[] { password });
		
	}

	public void updatepref(int age, String gender, String frequency, String home_zip, String work_zip, String school_zip, String email , String password)
	
	{
		ContentValues updatedValues = new ContentValues();
		updatedValues.put("AGE", age);
		updatedValues.put("Gender", gender);
		updatedValues.put("FREQUENCY", frequency);
		updatedValues.put("HOMEZIP", home_zip);
		updatedValues.put("WORKZIP", work_zip);
		updatedValues.put("SCHOOLZIP", school_zip);
		updatedValues.put("EMAIL", email);
		String where = "PASSWORD = ?";
		
		 db.update("LOGIN", updatedValues, where, new String[] { password });
		 
		 db.close();
		
	}
	
	
	
	
	public HashMap<String, String> getlogininfo(String id) {
		HashMap<String, String> wordList = new HashMap<String, String>();
		String selectQuery = "SELECT * FROM LOGIN where SECURITYHINT='" + id
				+ "'";
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				wordList.put("PASSWORD", cursor.getString(1));
			} while (cursor.moveToNext());
		}
		return wordList;
	}
}