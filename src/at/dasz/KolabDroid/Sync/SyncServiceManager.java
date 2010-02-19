package at.dasz.KolabDroid.Sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SyncServiceManager extends BroadcastReceiver
{
	public static final String	TAG	= "SyncServiceManager";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
		{
			SyncService.startSync(context);
		}
		else
		{
			Log.e(TAG, "Received unexpected intent " + intent.toString());
		}
	}
}
