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

package at.dasz.KolabDroid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class StatusHandler extends Handler
{
	private final static String			MESSAGE		= "message";
	private final static String			RESOURCE	= "resource";
	private final static String			SYNC_FINISHED	= "sync_finished";
	private final static String			SYNC_ROOT		= "SYNC";
	private MainActivity				activity;
	private static StatusHandler	current;

	private StatusHandler(MainActivity a)
	{
		activity = a;		
	}
	
	public static void load(MainActivity a) {
		synchronized (SYNC_ROOT)
		{
			current = new StatusHandler(a);
		}
	}
	
	public static void unload() {
		synchronized (SYNC_ROOT)
		{
			current = null;
		}
	}

	@Override
	public void handleMessage(android.os.Message msg)
	{
		synchronized (SYNC_ROOT)
		{
			Bundle data = msg.getData();
			if (data.containsKey(MESSAGE))
			{
				activity.setStatusText(data.getString(MESSAGE));
			}
			else if (data.containsKey(RESOURCE))
			{
				activity.setStatusText(data.getInt(RESOURCE));
			}
			else if (data.containsKey(SYNC_FINISHED))
			{
				activity.bindStatus();
			}
		}
	};

	public static void writeStatus(String text)
	{
		synchronized (SYNC_ROOT)
		{
			if (current == null) return;
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString(MESSAGE, text);
			msg.setData(data);
			current.sendMessage(msg);
		}
	}

	public static void writeStatus(int resource)
	{
		synchronized (SYNC_ROOT)
		{
			if (current == null) return;
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putInt(RESOURCE, resource);
			msg.setData(data);
			current.sendMessage(msg);
		}
	}

	public static void notifySyncFinished()
	{
		synchronized (SYNC_ROOT)
		{
			if (current == null) return;
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putBoolean(SYNC_FINISHED, true);
			msg.setData(data);
			current.sendMessage(msg);
		}
	}
}
