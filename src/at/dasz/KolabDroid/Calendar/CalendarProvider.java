package at.dasz.KolabDroid.Calendar;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateUtils;
import android.text.format.Time;

public class CalendarProvider
{
	public static final Uri			CALENDAR_URI	= Uri
															.parse("content://calendar/events");
	public static final String		_ID				= "_id";

	private static final String[]	projection		= new String[] { "_id",
			"calendar_id", "title", "allDay", "dtstart", "dtend",
			"description", "eventLocation", "visibility", "hasAlarm", "rrule" };
	private ContentResolver			cr;

	public CalendarProvider(ContentResolver cr)
	{
		this.cr = cr;
	}

	public List<CalendarEntry> loadAllCalendarEntries(int calendar_id)
	{
		List<CalendarEntry> result = new ArrayList<CalendarEntry>();

		Cursor cur = cr.query(CALENDAR_URI, projection, null, null, null);
		try
		{
			if (cur.moveToFirst())
			{
				do
				{
					result.add(loadCalendarEntry(cur));
				} while (cur.moveToNext());
			}
			return result;
		}
		finally
		{
			cur.close();
		}
	}

	public CalendarEntry loadCalendarEntry(int id)
	{
		if (id == 0) return null;
		Uri uri = ContentUris.withAppendedId(CALENDAR_URI, id);
		Cursor cur = cr.query(uri, projection, null, null, null);
		try
		{
			if (cur.moveToFirst()) { return loadCalendarEntry(cur); }
			return null;
		}
		finally
		{
			cur.close();
		}
	}

	private CalendarEntry loadCalendarEntry(Cursor cur)
	{
		CalendarEntry e = new CalendarEntry();
		e.setId(cur.getInt(0));
		e.setCalendar_id(cur.getInt(1));
		e.setTitle(cur.getString(2));
		e.setAllDay(cur.getInt(3) != 0);

		Time start = new Time();
		start.set(cur.getLong(4));
		e.setDtstart(start);

		Time end = new Time();
		end.set(cur.getLong(5));
		e.setDtend(end);

		e.setDescription(cur.getString(6));
		e.setEventLocation(cur.getString(7));
		e.setVisibility(cur.getInt(8));
		e.setHasAlarm(cur.getInt(9));
		e.setrRule(cur.getString(10));

		return e;
	}

	public void delete(CalendarEntry e)
	{
		if (e.getId() == 0) return;
		Uri uri = ContentUris.withAppendedId(CALENDAR_URI, e.getId());
		cr.delete(uri, null, null);
	}

	public void delete(int id)
	{
		if (id == 0) return;
		Uri uri = ContentUris.withAppendedId(CALENDAR_URI, id);
		cr.delete(uri, null, null);
	}

	public void save(CalendarEntry e)
	{
		ContentValues values = new ContentValues();
		long start = e.getDtstart().toMillis(true);
		long end = e.getDtend().toMillis(true);

		String duration;
		if (e.getAllDay())
		{
			long days = (end - start + DateUtils.DAY_IN_MILLIS - 1)
					/ DateUtils.DAY_IN_MILLIS;
			duration = "P" + days + "D";
		}
		else
		{
			long seconds = (end - start) / DateUtils.SECOND_IN_MILLIS;
			duration = "P" + seconds + "S";
		}

		values.put("calendar_id", e.getCalendar_id());
		values.put("title", e.getTitle());
		values.put("allDay", e.getAllDay() ? 1 : 0);
		values.put("dtstart", start);
		values.put("dtend", end);
		values.put("duration", duration);
		values.put("description", e.getDescription());
		values.put("eventLocation", e.getEventLocation());
		values.put("visibility", e.getVisibility());
		values.put("hasAlarm", e.getHasAlarm());
		values.put("rrule", e.getrRule());

		if (e.getId() == 0)
		{
			Uri newUri = cr.insert(CALENDAR_URI, values);
			e.setId((int) ContentUris.parseId(newUri));
		}
		else
		{
			Uri uri = ContentUris.withAppendedId(CALENDAR_URI, e.getId());
			cr.update(uri, values, null, null);
		}
	}
}
