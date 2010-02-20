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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import at.dasz.KolabDroid.StatusHandler;

public class SyncService extends Service implements BaseWorkerFinishedListener
{
	// private static final long ONE_SECOND = 1000;
	// private static final long ONE_MINUTE = ONE_SECOND * 60;
	// private static final long ONE_HOUR = ONE_MINUTE * 60;

	// private static final long SYNC_INTERVAL = ONE_HOUR * 12;
	// private static final long START_DELAY = ONE_SECOND * 5;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.i("Service", "Service is starting");
		StatusHandler.writeStatus("Service started");
		Log.i("Service", "Service started");
		_startSync(this);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		BaseWorker.stopWorker();
		Log.i("Service", "Service stopped");
	}

	public static void startSync(Context context)
	{
		Log.i("Service", "starting service -> sync will start");
		// Start the service
		// No need to call _startSync - this is done by the alarm manager
		Intent s = new Intent(context, SyncService.class);
		context.startService(s);
	}

	private SyncWorker	s	= null;

	private void _startSync(Context context)
	{
		if (!BaseWorker.isRunning())
		{
			Log.i("Service", "starting sync");
			if (context == null) context = this;
			s = new SyncWorker(context);
			s.setFinishedListener(this);
			s.start();
		}
		else
		{
			Log.i("Service", "a worker is running - do nothing");
		}
	}

	public void onFinished()
	{
		this.stopSelf();
		s = null;
	}
}
