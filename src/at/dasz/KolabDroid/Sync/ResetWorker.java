package at.dasz.KolabDroid.Sync;

import android.content.Context;
import android.database.Cursor;
import at.dasz.KolabDroid.R;
import at.dasz.KolabDroid.StatusHandler;
import at.dasz.KolabDroid.Calendar.SyncCalendarHandler;
import at.dasz.KolabDroid.Contacts.SyncContactsHandler;
import at.dasz.KolabDroid.Provider.LocalCacheProvider;

public class ResetWorker extends BaseWorker
{
	public ResetWorker(Context context)
	{
		super(context);
	}

	@Override
	protected void runWorker()
	{
		setRunningMessage(R.string.resetisrunning);
		try
		{
			StatusHandler.writeStatus(R.string.resetting_start);

			final String deleteMessageFormat = this.context.getResources()
					.getString(R.string.delete_message_format);
			int currentItemNo = 0;

			LocalCacheProvider.resetDatabase(context);

			currentItemNo = resetContacts(deleteMessageFormat, currentItemNo);
			currentItemNo = resetCalendar(deleteMessageFormat, currentItemNo);

			StatusHandler.writeStatus(R.string.reseting_finished);
		}
		catch (Exception ex)
		{
			final String errorFormat = this.context.getResources().getString(
					R.string.reset_error_format);

			StatusHandler
					.writeStatus(String.format(errorFormat, ex.toString()));

			ex.printStackTrace();
		}
	}

	private int resetCalendar(final String deleteMessageFormat,
			int currentItemNo)
	{
		SyncCalendarHandler calendar = new SyncCalendarHandler(context);
		Cursor c = calendar.getAllLocalItemsCursor();
		if (c != null)
		{
			try
			{
				final int idIdx = calendar.getIdColumnIndex(c);
				while (c.moveToNext())
				{
					calendar.deleteLocalItem(c.getInt(idIdx));
					if (++currentItemNo % 10 == 0)
					{
						StatusHandler.writeStatus(String.format(
								deleteMessageFormat, currentItemNo));
					}
				}
			}
			finally
			{
				if (c != null) c.close();
			}
		}
		return currentItemNo;
	}

	private int resetContacts(final String deleteMessageFormat,
			int currentItemNo)
	{
		SyncContactsHandler contacts = new SyncContactsHandler(context);
		Cursor c = contacts.getAllLocalItemsCursor();
		if (c != null)
		{
			try
			{
				final int idIdx = contacts.getIdColumnIndex(c);
				while (c.moveToNext())
				{
					contacts.deleteLocalItem(c.getInt(idIdx));

					if (++currentItemNo % 10 == 0)
					{
						StatusHandler.writeStatus(String.format(
								deleteMessageFormat, currentItemNo));
					}
				}
			}
			finally
			{
				if (c != null) c.close();
			}
		}
		return currentItemNo;
	}
}
