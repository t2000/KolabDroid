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

import android.content.Context;
import at.dasz.KolabDroid.R;

public abstract class BaseWorker
{
	private final static String	SYNC_ROOT	= "SYNC_BaseWorker";

	private static boolean		isRunning	= false;
	private static boolean		isStopping	= false;

	public static boolean isRunning()
	{
		synchronized (SYNC_ROOT)
		{
			return isRunning;
		}
	}

	public static boolean isStopping()
	{
		synchronized (SYNC_ROOT)
		{
			return isStopping;
		}
	}

	protected abstract void runWorker();

	public void start()
	{
		try
		{
			synchronized (SYNC_ROOT)
			{
				if(isRunning) return;
				isRunning = true;
				isStopping = false;
			}
			runWorker();
		}
		finally
		{
			synchronized (SYNC_ROOT)
			{
				isRunning = false;
			}
		}
	}

	public static void stopWorker()
	{
		synchronized (SYNC_ROOT)
		{
			isStopping = true;
		}
	}

	private static int	runningMessageResID	= 0;

	public static int getRunningMessageResID()
	{
		synchronized (SYNC_ROOT)
		{
			if (runningMessageResID == 0) return R.string.workerisrunning;
			return runningMessageResID;
		}
	}

	protected void setRunningMessage(int resid)
	{
		synchronized (SYNC_ROOT)
		{
			runningMessageResID = resid;
		}
	}

	protected Context	context;

	public BaseWorker(Context context)
	{
		this.context = context;
	}
}
