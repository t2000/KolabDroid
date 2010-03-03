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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import at.dasz.KolabDroid.Utils;

/**
 * @author arthur
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
	private final static String		DATABASE_NAME				= "KolabDroid.db";
	private final static int		DATABASE_VERSION			= 5;
	
	public final static String			COL_ID						= "_id";
	public final static int				COL_IDX_ID					= 0;
	
	public static final String[]	ID_PROJECTION				= new String[] { COL_ID };

	/**
	 * @param context
	 */
	DatabaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		createDb(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// sync-info
		if (oldVersion != newVersion)
		{
			Log.i("Database", "Upgrading Database from " + oldVersion + " to " + newVersion);
			if(oldVersion < 4)
			{
				// Old development version, no upgrade path
				dropDb(db);
				createDb(db);
			}
			if(oldVersion == 4)
			{
				// Add alter statements for version upgrade 4 to 5
				// Add error message column
				db.execSQL("ALTER TABLE " + StatusProvider.STATUS_TABLE_NAME + " ADD " + StatusProvider.COL_fatalErrorMsg  + " TEXT");
				// Next version, continue upgrade
				oldVersion = 5;
			}
			if(oldVersion == 5)
			{
				// Add alter statements for version upgrade 5 to 6
				// ...
				// Next version, continue upgrade
				oldVersion = 6;
			}
		}
		else
		{
			Log.i("Database", "No need to upgrade Database");			
		}
	}

	private void createDb(SQLiteDatabase db)
	{
		final String columns = Utils.join(", ", new String[] {
				COL_ID + " INTEGER PRIMARY KEY",
				LocalCacheProvider.COL_LOCAL_ID + " INTEGER UNIQUE", 
				LocalCacheProvider.COL_LOCAL_HASH + " TEXT",
				LocalCacheProvider.COL_REMOTE_ID + " TEXT UNIQUE",
				LocalCacheProvider.COL_REMOTE_IMAP_UID + " TEXT",
				LocalCacheProvider.COL_REMOTE_CHANGEDDATE + " INTEGER",
				LocalCacheProvider.COL_REMOTE_SIZE + " INTEGER" });

		db.execSQL("CREATE TABLE " + LocalCacheProvider.CONTACT_TABLE_NAME + " (" + columns
				+ ");");
		db.execSQL("CREATE TABLE " + LocalCacheProvider.CALENDAR_TABLE_NAME + " (" + columns
				+ ");");
		
		final String stat_columns = Utils.join(", ", new String[] {
				COL_ID + " INTEGER PRIMARY KEY",
				StatusProvider.COL_time + " INTEGER", 
				StatusProvider.COL_task + " TEXT",
				StatusProvider.COL_items + " INTEGER",
				StatusProvider.COL_localChanged + " INTEGER",
				StatusProvider.COL_remoteChanged + " INTEGER",
				StatusProvider.COL_localNew + " INTEGER",
				StatusProvider.COL_remoteNew + " INTEGER",
				StatusProvider.COL_localDeleted + " INTEGER",
				StatusProvider.COL_remoteDeleted + " INTEGER",
				StatusProvider.COL_conflicted + " INTEGER",
				StatusProvider.COL_errors + " INTEGER",});

		db.execSQL("CREATE TABLE " + StatusProvider.STATUS_TABLE_NAME + " (" + stat_columns
				+ ");");
	}

	private void dropDb(SQLiteDatabase db)
	{
		db.execSQL("DROP TABLE IF EXISTS " + LocalCacheProvider.CONTACT_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + LocalCacheProvider.CALENDAR_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + StatusProvider.STATUS_TABLE_NAME);
	}

	public void cleanDb(SQLiteDatabase db)
	{
		db.execSQL("DELETE FROM " + LocalCacheProvider.CONTACT_TABLE_NAME);
		db.execSQL("DELETE FROM " + LocalCacheProvider.CALENDAR_TABLE_NAME);
		db.execSQL("DELETE FROM " + StatusProvider.STATUS_TABLE_NAME);
	}
}
