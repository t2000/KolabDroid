/*
 * Copyright 2010 Arthur Zaczek <arthur@dasz.at>, dasz.at OG; All rights reserved.
 * Copyright 2010 David Schmitt <david@dasz.at>, dasz.at OG; All rights reserved.
 *
 *  This file is part of Kolab Sync for Android.

 *  Kolab Sync for Android is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.

 *  Kolab Sync for Android is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with Kolab Sync for Android.
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package at.dasz.KolabDroid.Provider;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import at.dasz.KolabDroid.Sync.StatusEntry;

public class StatusProvider
{
	public final static String		STATUS_TABLE_NAME			= "Status";

	public final static String				COL_time = "time";
	public final static String				COL_task = "task";

	public final static String				COL_items = "items";

	public final static String				COL_localChanged = "localChanged";
	public final static String				COL_remoteChanged = "remoteChanged";

	public final static String				COL_localNew = "localNew";
	public final static String				COL_remoteNew = "remoteNew";

	public final static String				COL_localDeleted = "localDeleted";
	public final static String				COL_remoteDeleted = "remoteDeleted";

	public final static String				COL_conflicted = "conflicted";
	
	public final static int				COL_IDX_time = 1;
	public final static int				COL_IDX_task = 2;

	public final static int				COL_IDX_items = 3;

	public final static int				COL_IDX_localChanged = 4;
	public final static int				COL_IDX_remoteChanged = 5;

	public final static int				COL_IDX_localNew = 6;
	public final static int				COL_IDX_remoteNew = 7;

	public final static int				COL_IDX_localDeleted = 8;
	public final static int				COL_IDX_remoteDeleted = 9;

	public final static int				COL_IDX_conflicted = 10;

	public static final String[]			DEFAULT_PROJECTION			= new String[] {
		DatabaseHelper.COL_ID, // 0
		COL_time, // 1
		COL_task, // 2
		COL_items, // 3
		COL_localChanged, // 4
		COL_remoteChanged, // 5
		COL_localNew, // 6
		COL_remoteNew, // 7
		COL_localDeleted, // 8
		COL_remoteDeleted, // 9
		COL_conflicted, // 10
															};
	
	private DatabaseHelper	dbHelper;

	public StatusProvider(Context ctx)
	{
		dbHelper = new DatabaseHelper(ctx);
	}
	
	public void close()
	{
		dbHelper.close();
	}
	
	private Cursor getCursor(SQLiteDatabase db, String[] projectionIn,
			String selection, String[] selectionArgs, String groupBy,
			String having, String sortOrder)
	{
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(STATUS_TABLE_NAME);
		return qb.query(db, projectionIn, selection, selectionArgs, groupBy,
				having, sortOrder);
	}
	
	public StatusEntry getStatusEntry(int id) 
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		try
		{
			c = getCursor(db, DEFAULT_PROJECTION, DatabaseHelper.COL_ID + " = ?", new String[] { Integer.toString(id) }, null, null, null);
			if(c.moveToFirst()) return new StatusEntry(c);
			return null;
		}
		finally
		{
			if (c != null) c.close();
			if (db != null) db.close();
		}
	}
	
	public StatusEntry getLastStatusEntry(String task) 
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		try
		{
			c = getCursor(db, DatabaseHelper.ID_PROJECTION, "MAX(" + COL_time + ") AND " + COL_task + "= ?", new String[] { task }, null, null, null);
			if(c.moveToFirst()) return getStatusEntry(c.getInt(DatabaseHelper.COL_IDX_ID));
			return null;
		}
		finally
		{
			if (c != null) c.close();
			if (db != null) db.close();
		}
	}
	
	public ArrayList<StatusEntry> getLastStatusEntries()
	{
		ArrayList<StatusEntry> result = new ArrayList<StatusEntry>();

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		try
		{
			c = getCursor(db, DEFAULT_PROJECTION, null, null, null, null, COL_time + " DESC");
			while(c.moveToNext()) 
			{
				result.add(new StatusEntry(c));
			}
		}
		finally
		{
			if (c != null) c.close();
			if (db != null) db.close();
		}
		
		return result;
	}
	
	public void saveStatusEntry(StatusEntry entry) 
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try
		{
			if (entry.getId() != 0)
			{
				db.update(STATUS_TABLE_NAME, entry.toContentValues(), DatabaseHelper.COL_ID
						+ " = " + entry.getId(), null);
			}
			else
			{
				long rowId = db.insertOrThrow(STATUS_TABLE_NAME, null, entry
						.toContentValues());
				entry.setId(rowId);
			}
		}
		finally
		{
			if (db != null) db.close();
		}
	}
	
	public void deleteStatusEntry(StatusEntry entry)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try
		{
			db.delete(STATUS_TABLE_NAME, DatabaseHelper.COL_ID + " = " + entry.getId(), null);
		}
		finally
		{
			if (db != null) db.close();
		}
	}

	public void clearAllEntries()
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try
		{
			db.execSQL("DELETE FROM " + StatusProvider.STATUS_TABLE_NAME);
		}
		finally
		{
			if (db != null) db.close();
		}
	}	
}
