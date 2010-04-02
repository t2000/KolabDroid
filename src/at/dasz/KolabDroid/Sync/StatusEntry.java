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

package at.dasz.KolabDroid.Sync;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.format.Time;
import at.dasz.KolabDroid.Provider.DatabaseHelper;
import at.dasz.KolabDroid.Provider.StatusProvider;

/**
 * Describes a log entry.
 */
public class StatusEntry
{
	private long	id;

	private Time	time;
	private String	task;

	private int		items;

	private int		localChanged;
	private int		remoteChanged;

	private int		localNew;
	private int		remoteNew;

	private int		localDeleted;
	private int		remoteDeleted;
	
	private int		conflicted;
	private int		errors;

	private String	fatalErrorMsg;
	
	public StatusEntry()
	{
	}

	public StatusEntry(Cursor c)
	{
		setId(c.getLong(DatabaseHelper.COL_IDX_ID));
		
		Time t = new Time();
		t.set(c.getLong(StatusProvider.COL_IDX_time));
		setTime(t);
		
		setTask(c.getString(StatusProvider.COL_IDX_task));
		setItems(c.getInt(StatusProvider.COL_IDX_items));

		setLocalChanged(c.getInt(StatusProvider.COL_IDX_localChanged));
		setRemoteChanged(c.getInt(StatusProvider.COL_IDX_remoteChanged));
		
		setLocalNew(c.getInt(StatusProvider.COL_IDX_localNew));
		setRemoteNew(c.getInt(StatusProvider.COL_IDX_remoteNew));
		
		setLocalDeleted(c.getInt(StatusProvider.COL_IDX_localDeleted));
		setRemoteDeleted(c.getInt(StatusProvider.COL_IDX_remoteDeleted));
		
		setConflicted(c.getInt(StatusProvider.COL_IDX_conflicted));
		setErrors(c.getInt(StatusProvider.COL_IDX_errors));
		setFatalErrorMsg(c.getString(StatusProvider.COL_IDX_fatalErrorMsg));
	}
	
	public ContentValues toContentValues()
	{
		ContentValues result = new ContentValues();
		if (getId() != 0)
		{
			result.put(DatabaseHelper.COL_ID, getId());
		}
		result.put(StatusProvider.COL_time, getTime().toMillis(true));
		result.put(StatusProvider.COL_task, getTask());
		result.put(StatusProvider.COL_items, getItems());

		result.put(StatusProvider.COL_localChanged, getLocalChanged());
		result.put(StatusProvider.COL_remoteChanged, getRemoteChanged());

		result.put(StatusProvider.COL_localNew, getLocalNew());
		result.put(StatusProvider.COL_remoteNew, getRemoteNew());

		result.put(StatusProvider.COL_localDeleted, getLocalDeleted());
		result.put(StatusProvider.COL_remoteDeleted, getRemoteDeleted());
		
		result.put(StatusProvider.COL_conflicted, getConflicted());
		result.put(StatusProvider.COL_errors, getErrors());
		result.put(StatusProvider.COL_fatalErrorMsg, getFatalErrorMsg());

		return result;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Time getTime()
	{
		return time;
	}

	public void setTime(Time time)
	{
		this.time = time;
	}

	public String getTask()
	{
		return task;
	}

	public void setTask(String task)
	{
		this.task = task;
	}

	public int getItems()
	{
		return items;
	}

	public void setItems(int items)
	{
		this.items = items;
	}
	
	public int incrementItems()
	{
		return ++items;
	}

	public int getLocalChanged()
	{
		return localChanged;
	}

	public void setLocalChanged(int localChanged)
	{
		this.localChanged = localChanged;
	}

	public int incrementLocalChanged()
	{
		return ++localChanged;
	}

	public int getRemoteChanged()
	{
		return remoteChanged;
	}

	public void setRemoteChanged(int remoteChanged)
	{
		this.remoteChanged = remoteChanged;
	}
	public int incrementRemoteChanged()
	{
		return ++remoteChanged;
	}
	
	public int getLocalNew()
	{
		return localNew;
	}

	public void setLocalNew(int localNew)
	{
		this.localNew = localNew;
	}
	public int incrementLocalNew()
	{
		return ++localNew;
	}

	public int getRemoteNew()
	{
		return remoteNew;
	}

	public void setRemoteNew(int remoteNew)
	{
		this.remoteNew = remoteNew;
	}
	public int incrementRemoteNew()
	{
		return ++remoteNew;
	}

	public int getLocalDeleted()
	{
		return localDeleted;
	}

	public void setLocalDeleted(int localDeleted)
	{
		this.localDeleted = localDeleted;
	}
	public int incrementLocalDeleted()
	{
		return ++localDeleted;
	}

	public int getRemoteDeleted()
	{
		return remoteDeleted;
	}

	public void setRemoteDeleted(int remoteDeleted)
	{
		this.remoteDeleted = remoteDeleted;
	}
	public int incrementRemoteDeleted()
	{
		return ++remoteDeleted;
	}
	
	public int getConflicted()
	{
		return conflicted;
	}

	public void setConflicted(int conflicted)
	{
		this.conflicted = conflicted;
	}

	public int incrementConflicted()
	{
		return ++conflicted;
	}
	
	public int getErrors()
	{
		return errors;
	}

	public void setErrors(int errors)
	{
		this.errors = errors;
	}

	public int incrementErrors()
	{
		return ++errors;
	}
	
	public String getFatalErrorMsg()
	{
		return fatalErrorMsg;
	}

	public void setFatalErrorMsg(String fatalErrorMsg)
	{
		this.fatalErrorMsg = fatalErrorMsg;
	}
}
