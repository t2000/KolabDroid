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
import android.content.Intent;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class ResetService extends WakefulIntentService
{
	public ResetService()
	{
		super("ResetService");
	}

	public static void startReset(Context context)
	{
		if(!BaseWorker.isRunning())
		{
			Log.i("Service", "starting service");
			WakefulIntentService.acquireStaticLock(context);
			context.startService(new Intent(context, ResetService.class));		
		}
		else
		{
			Log.i("Service", "another service is already running");			
		}
	}

	@Override
	protected void doWakefulWork(Intent intent)
	{
		Log.i("Service", "starting reset");
		try
		{
			ResetWorker r = new ResetWorker(this);
			r.start();
			Log.i("Service", "reset finished");
		}
		catch (Exception ex)
		{
			Log.i("Service", "reset failed: " + ex.toString());
		}
	}
}
