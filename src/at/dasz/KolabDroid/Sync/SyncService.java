package at.dasz.KolabDroid.Sync;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import at.dasz.KolabDroid.StatusHandler;

public class SyncService extends Service
{
	private static final long	ONE_SECOND = 1000;
	private static final long	ONE_MINUTE = ONE_SECOND * 60;
	private static final long	ONE_HOUR = ONE_MINUTE * 60;

	private static final long	SYNC_INTERVAL	= ONE_HOUR * 12;
	private static final long	START_DELAY		= ONE_SECOND * 5;

	private Timer				timer			= null;
	private static SyncService	current			= null;

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
		current = this;
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run()
			{
				_startSync(null);
			}
		}, START_DELAY, SYNC_INTERVAL);
		StatusHandler.writeStatus("Service started");
		Log.i("Service", "Service started");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (timer != null) timer.cancel();
		BaseWorker.stopWorker();
		StatusHandler.writeStatus("Service stopped");
		Log.i("Service", "Service stopped");
	}

	public static void startSync(Context context)
	{
		if (current != null)
		{
			Log.i("Service", "starting sync on current service");
			current._startSync(context);
		}
		else
		{
			Log.i("Service", "starting service -> sync will start");
			// Start the service
			// No need to call _startSync - this is done by the timer
			Intent s = new Intent(context, SyncService.class);
		    context.startService(s);		
		}
	}

	private void _startSync(Context context)
	{
		if (!BaseWorker.isRunning())
		{
			Log.i("Service", "starting sync");
			if (context == null) context = this;
			SyncWorker s = new SyncWorker(context);
			s.start();
		}
		else
		{
			Log.i("Service", "sync is running - do nothing");
		}		
	}
}
