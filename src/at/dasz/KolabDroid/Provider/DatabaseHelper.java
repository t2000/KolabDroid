package at.dasz.KolabDroid.Provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import at.dasz.KolabDroid.Utils;

public class DatabaseHelper extends SQLiteOpenHelper
{
	private final static String		DATABASE_NAME				= "KolabDroid.db";
	private final static int		DATABASE_VERSION			= 3;
	
	public final static String			COL_ID						= "_id";
	public final static int				COL_IDX_ID					= 0;
	
	public static final String[]	ID_PROJECTION				= new String[] { COL_ID };

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
		// TODO: create proper upgrade path, so not to loose available
		// sync-info
		if (oldVersion != newVersion)
		{
			dropDb(db);
			createDb(db);
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
				StatusProvider.COL_conflicted + " INTEGER",});

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