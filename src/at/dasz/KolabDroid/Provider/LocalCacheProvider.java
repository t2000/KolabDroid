package at.dasz.KolabDroid.Provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import at.dasz.KolabDroid.Sync.CacheEntry;

public abstract class LocalCacheProvider
{
	public final static String		CONTACT_TABLE_NAME			= "Contacts";
	public final static String		CALENDAR_TABLE_NAME			= "CalendarEntries";

	public final static String				COL_LOCAL_ID				= "localid";
	public final static String				COL_LOCAL_HASH				= "localhash";
	public final static String				COL_REMOTE_ID				= "remoteid";
	public final static String				COL_REMOTE_IMAP_UID			= "remoteimapuid";
	public final static String				COL_REMOTE_CHANGEDDATE		= "remotechangeddate";
	public final static String				COL_REMOTE_SIZE				= "remotesize";

	public final static int				COL_IDX_LOCAL_ID			= 1;
	public final static int				COL_IDX_LOCAL_HASH			= 2;
	public final static int				COL_IDX_REMOTE_ID			= 3;
	public final static int				COL_IDX_REMOTE_IMAP_UID		= 4;
	public final static int				COL_IDX_REMOTE_CHANGEDDATE	= 5;
	public final static int				COL_IDX_REMOTE_SIZE			= 6;
	
	public static final String[]			DEFAULT_PROJECTION			= new String[] {
		DatabaseHelper.COL_ID, // 0
			COL_LOCAL_ID, // 1
			COL_LOCAL_HASH, // 2
			COL_REMOTE_ID, // 3
			COL_REMOTE_IMAP_UID, // 4
			COL_REMOTE_CHANGEDDATE, // 5
			COL_REMOTE_SIZE, // 6
																};

	public static class CalendarCacheProvider extends LocalCacheProvider
	{
		public CalendarCacheProvider(Context ctx)
		{
			super(ctx, LocalCacheProvider.CALENDAR_TABLE_NAME);
		}
	}

	public static class ContactsCacheProvider extends LocalCacheProvider
	{
		public ContactsCacheProvider(Context ctx)
		{
			super(ctx, LocalCacheProvider.CONTACT_TABLE_NAME);
		}
	}

	private DatabaseHelper	dbHelper;
	private final String	tableName;

	protected LocalCacheProvider(Context ctx, String tableName)
	{
		dbHelper = new DatabaseHelper(ctx);
		this.tableName = tableName;
	}
	
	private Cursor getCursor(SQLiteDatabase db, String[] projectionIn,
			String selection, String[] selectionArgs, String groupBy,
			String having, String sortOrder)
	{
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(tableName);
		return qb.query(db, projectionIn, selection, selectionArgs, groupBy,
				having, sortOrder);
	}

	public static void resetDatabase(Context ctx)
	{
		DatabaseHelper dbHelper = new DatabaseHelper(ctx);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try
		{
			dbHelper.cleanDb(db);
		}
		finally
		{
			if (db != null) db.close();
		}
	}

	public CacheEntry getEntryFromRemoteId(String remoteId)
	{
		return getEntry(COL_REMOTE_ID + " = ?", new String[] { remoteId });
	}

	public CacheEntry getEntryFromLocalId(int localId)
	{
		return getEntry(COL_LOCAL_ID + " = " + Integer.toString(localId), null);
	}

	

	private CacheEntry getEntry(String selection, String[] selectionArgs)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		try
		{
			c = getCursor(db, DEFAULT_PROJECTION, selection, selectionArgs // COL_REMOTE_ID
					// + " = ?",
					/* new String[] { id } */, null, null, null);
			switch (c.getCount())
			{
			case 0:
				return null;
			case 1:
				c.moveToFirst();
				return new CacheEntry(c);
			default:
				throw new RuntimeException(
						"Multiple cache entries for the same remote ID found");
			}
		}
		finally
		{
			if (c != null) c.close();
			if (db != null) db.close();
		}
	}

	public void deleteEntry(CacheEntry entry)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try
		{
			db.delete(this.tableName, DatabaseHelper.COL_ID + " = " + entry.getId(), null);
		}
		finally
		{
			if (db != null) db.close();
		}
	}

	public void saveEntry(CacheEntry newlyCreated)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = null;
		try
		{
			c = getCursor(db, DatabaseHelper.ID_PROJECTION, COL_LOCAL_ID + " = "
					+ newlyCreated.getLocalId(), null, null, null, null);
			if (c.moveToFirst())
			{
				newlyCreated.setId(c.getInt(DatabaseHelper.COL_IDX_ID));
				db.update(tableName, newlyCreated.toContentValues(), DatabaseHelper.COL_ID
						+ " = " + newlyCreated.getId(), null);
			}
			else
			{
				long rowId = db.insertOrThrow(tableName, null, newlyCreated
						.toContentValues());
				newlyCreated.setId(rowId);
			}
		}
		finally
		{
			if (c != null) c.close();
			if (db != null) db.close();
		}
	}



}
