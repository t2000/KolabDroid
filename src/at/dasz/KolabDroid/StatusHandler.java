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
