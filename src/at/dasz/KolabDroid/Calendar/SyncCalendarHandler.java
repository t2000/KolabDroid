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

package at.dasz.KolabDroid.Calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;

import javax.mail.MessagingException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.format.Time;
import android.util.Log;
import at.dasz.KolabDroid.Utils;
import at.dasz.KolabDroid.Provider.LocalCacheProvider;
import at.dasz.KolabDroid.Settings.Settings;
import at.dasz.KolabDroid.Sync.AbstractSyncHandler;
import at.dasz.KolabDroid.Sync.CacheEntry;
import at.dasz.KolabDroid.Sync.SyncContext;
import at.dasz.KolabDroid.Sync.SyncException;

public class SyncCalendarHandler extends AbstractSyncHandler
{
	private final String				defaultFolderName;
	private final LocalCacheProvider	cacheProvider;

	private final CalendarProvider		calendarProvider;
	private final ContentResolver		cr;

	public SyncCalendarHandler(Context context)
	{
		super(context);
		Settings s = new Settings(context);
		defaultFolderName = s.getCalendarFolder();
		cacheProvider = new LocalCacheProvider.CalendarCacheProvider(context);
		calendarProvider = new CalendarProvider(context.getContentResolver());
		cr = context.getContentResolver();
		status.setTask("Calendar");
	}

	public String getDefaultFolderName()
	{
		return defaultFolderName;
	}

	public LocalCacheProvider getLocalCacheProvider()
	{
		return cacheProvider;
	}

	public int getIdColumnIndex(Cursor c)
	{
		return c.getColumnIndex(CalendarProvider._ID);
	}

	public Cursor getAllLocalItemsCursor()
	{
		return cr.query(CalendarProvider.CALENDAR_URI,
				new String[] { CalendarProvider._ID }, null, null, null);
	}

	@Override
	public void deleteLocalItem(int localId)
	{
		calendarProvider.delete(localId);
	}

	@Override
	protected String getMimeType()
	{
		return "application/x-vnd.kolab.event";
	}

	public boolean hasLocalItem(SyncContext sync) throws SyncException
	{
		return getLocalItem(sync) != null;
	}

	public boolean hasLocalChanges(SyncContext sync) throws SyncException
	{
		CacheEntry e = sync.getCacheEntry();
		CalendarEntry cal = getLocalItem(sync);
		String entryHash = e.getLocalHash();
		String calHash = cal != null ? cal.getLocalHash() : "";
		return !entryHash.equals(calHash);
	}

	@Override
	protected void updateLocalItemFromServer(SyncContext sync, Document xml)
	{
		CalendarEntry cal = (CalendarEntry) sync.getLocalItem();
		if (cal == null)
		{
			cal = new CalendarEntry();
		}
		Element root = xml.getDocumentElement();

		cal.setUid(Utils.getXmlElementString(root, "uid"));
		cal.setDescription(Utils.getXmlElementString(root, "body"));
		cal.setTitle(Utils.getXmlElementString(root, "summary"));
		cal.setEventLocation(Utils.getXmlElementString(root, "location"));

		Time start = Utils.getXmlElementTime(root, "start-date");
		Time end = Utils.getXmlElementTime(root, "end-date");

		cal.setDtstart(start);
		cal.setDtend(end);

		cal.setAllDay(start.hour == 0 && end.hour == 0 && start.minute == 0
				&& end.minute == 0 && start.second == 0 && end.second == 0);

		Element recurrence = Utils.getXmlElement(root, "recurrence");
		if (recurrence != null)
		{
			StringBuilder sb = new StringBuilder();
			String cycle = Utils.getXmlAttributeString(recurrence, "cycle")
					.toUpperCase();
			sb.append("FREQ=");
			sb.append(cycle);

			sb.append(";WKST=");
			int firstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
			switch (firstDayOfWeek)
			{
			case Calendar.MONDAY:
				sb.append("MO");
				break;
			case Calendar.TUESDAY:
				sb.append("TU");
				break;
			case Calendar.WEDNESDAY:
				sb.append("WE");
				break;
			case Calendar.THURSDAY:
				sb.append("TH");
				break;
			case Calendar.FRIDAY:
				sb.append("FR");
				break;
			case Calendar.SATURDAY:
				sb.append("SA");
				break;
			case Calendar.SUNDAY:
				sb.append("SU");
				break;
			}

			int daynumber = Utils.getXmlElementInt(recurrence, "daynumber", 0);
			NodeList days = Utils.getXmlElements(recurrence, "day");
			int daysLength = days.getLength();
			if (daysLength > 0)
			{
				sb.append(";BYDAY=");
				for (int i = 0; i < daysLength; i++)
				{
					if (daynumber > 1) sb.append(daynumber);

					Element day = (Element) days.item(i);
					String d = Utils.getXmlElementString(day);
					sb.append(CalendarEntry.kolabWeekDayToWeekDay(d));

					if ((i + 1) < daysLength) sb.append(",");
				}

				if (CalendarEntry.YEARLY.equals(cycle))
				{
					String month = Utils.getXmlElementString(recurrence,
							"month");
					if (month != null && !"".equals(month))
					{
						sb.append(";BYMONTH=");
						sb.append(CalendarEntry.kolabMonthToMonth(month));
					}
				}
			}
			else if (daynumber != 0 && CalendarEntry.MONTHLY.equals(cycle))
			{
				sb.append(";BYMONTHDAY=" + daynumber);
			}

			int interval = Utils.getXmlElementInt(recurrence, "interval", 0);
			if (interval > 1)
			{
				sb.append(";INTERVAL=" + interval);
			}

			cal.setrRule(sb.toString());
			Log.d("sync", "RRule = " + cal.getrRule());
		}

		sync.setCacheEntry(saveCalender(cal));
	}

	private CacheEntry saveCalender(CalendarEntry cal)
	{
		cal.setCalendar_id(1);
		calendarProvider.save(cal);
		CacheEntry result = new CacheEntry();
		result.setLocalId(cal.getId());
		result.setLocalHash(cal.getLocalHash());
		result.setRemoteId(cal.getUid());
		return result;
	}

	private String getNewUid()
	{
		// Create Application and Type specific id
		// kd == Kolab Droid, ev == event
		return "kd-ev-" + UUID.randomUUID().toString();
	}

	@Override
	protected void updateServerItemFromLocal(SyncContext sync, Document xml) throws SyncException
	{
		CalendarEntry source = getLocalItem(sync);
		CacheEntry entry = sync.getCacheEntry();

		entry.setLocalHash(source.getLocalHash());
		final Date lastChanged = new Date();
		entry.setRemoteChangedDate(lastChanged);

		writeXml(xml, source, lastChanged);

	}

	private final static java.util.regex.Pattern	regFREQ				= java.util.regex.Pattern
																				.compile("FREQ=(\\w*);.*");
	// private final static java.util.regex.Pattern regUntil =
	// java.util.regex.Pattern
	// .compile(";UNTIL=(\\w*)");
	// private final static java.util.regex.Pattern regWKST =
	// java.util.regex.Pattern
	// .compile(";WKST=(\\w*)");
	private final static java.util.regex.Pattern	regBYDAY			= java.util.regex.Pattern
																				.compile(".*;BYDAY=([\\+\\-\\,0-9A-Z]*);?.*");
	private final static java.util.regex.Pattern	regBYDAYSubPattern	= java.util.regex.Pattern
																				.compile("(?:([+-]?)([\\d]*)([A-Z]{2}),?)");
	private final static java.util.regex.Pattern	regINTERVAL			= java.util.regex.Pattern
																				.compile(".*;INTERVAL=(\\d*);?.*");
	private final static java.util.regex.Pattern	regBYMONTHDAY		= java.util.regex.Pattern
																				.compile(".*;BYMONTHDAY=(\\d*);?.*");
	private final static java.util.regex.Pattern	regBYMONTH			= java.util.regex.Pattern
																				.compile(".*;BYMONTH=(\\d*);?.*");

	private void writeXml(Document xml, CalendarEntry source,
			final Date lastChanged)
	{
		Element root = xml.getDocumentElement();

		Utils.setXmlElementValue(xml, root, "uid", source.getUid());
		Utils.setXmlElementValue(xml, root, "body", source.getDescription());
		Utils.setXmlElementValue(xml, root, "last-modification-date", Utils
				.toUtc(lastChanged));
		Utils.setXmlElementValue(xml, root, "summary", source.getTitle());
		Utils.setXmlElementValue(xml, root, "location", source
				.getEventLocation());
		
		//times have to be in UTC, according to
		//http://www.kolab.org/doc/kolabformat-2.0rc7-html/x123.html
		Time startTime = source.getDtstart();
		startTime.switchTimezone("UTC");
		
		Utils.setXmlElementValue(xml, root, "start-date", startTime
				.format3339(source.getAllDay()));
		
		Time endTime = source.getDtend();
		endTime.switchTimezone("UTC");
		
		Utils.setXmlElementValue(xml, root, "end-date", endTime
				.format3339(source.getAllDay()));

		String rrule = source.getrRule();
		if (rrule != null && !"".equals(rrule))
		{
			Element recurrence = Utils.getOrCreateXmlElement(xml, root,
					"recurrence");
			Utils.deleteXmlElements(recurrence, "day");

			Matcher result;

			// /////////// Frequency /////////////
			result = regFREQ.matcher(rrule);
			String cycle = "";
			if (result.matches())
			{
				cycle = result.group(1);
				Utils.setXmlAttributeValue(xml, recurrence, "cycle", cycle
						.toLowerCase());
			}

			// /////////// weekday recurrence /////////////
			String daynumber = "";
			result = regBYDAY.matcher(rrule);
			if (result.matches())
			{
				if (CalendarEntry.MONTHLY.equals(cycle)
						|| CalendarEntry.YEARLY.equals(cycle))
				{
					Utils.setXmlAttributeValue(xml, recurrence, "type",
							"weekday");
				}

				final String group = result.group(1);
				Matcher grpResult = regBYDAYSubPattern.matcher(group);
				while (grpResult.find())
				{
					String plusMinus = grpResult.group(1);
					String d = grpResult.group(2);
					if (d != null && !"".equals(d))
					{
						if ("-".equals(plusMinus))
						{
							daynumber = "4";
						}
						else
						{
							daynumber = d;
						}
					}
					String day = CalendarEntry.weekDayToKolabWeekDay(grpResult
							.group(3));
					if (!"".equals(day)) Utils.addXmlElementValue(xml,
							recurrence, "day", day);
				}
			}
			// Not a weekday recurrence - must be daynumber or monthday
			if ("".equals(daynumber))
			{
				if (CalendarEntry.MONTHLY.equals(cycle))
				{
					Utils.setXmlAttributeValue(xml, recurrence, "type",
							"daynumber");
				}
				if (CalendarEntry.YEARLY.equals(cycle))
				{
					Utils.setXmlAttributeValue(xml, recurrence, "type",
							"monthday");
				}
			}

			// /////////// Monthday /////////////
			result = regBYMONTHDAY.matcher(rrule);
			if (result.matches())
			{
				daynumber = result.group(1);
			}

			// If daynumber is empty, get the daynumber from startdate for
			// MONTHLY and YEARLY recurrences
			if ("".equals(daynumber)
					&& (CalendarEntry.MONTHLY.equals(cycle) || CalendarEntry.YEARLY
							.equals(cycle)))
			{
				daynumber = Integer.toString(source.getDtstart().monthDay);
			}

			Utils.setXmlElementValue(xml, recurrence, "daynumber", daynumber);

			// /////////// Month /////////////
			result = regBYMONTH.matcher(rrule);
			String month = "";
			if (result.matches())
			{
				month = CalendarEntry.monthToKolabMonth(result.group(1));
			}
			// Get month only for YEARLY recurrences from startdate
			if (CalendarEntry.YEARLY.equals(cycle) && "".equals(month))
			{
				month = CalendarEntry.monthToKolabMonth(Integer.toString(source
						.getDtstart().month + 1)); // 0-11
			}
			Utils.setXmlElementValue(xml, recurrence, "month", month);

			// /////////// Interval /////////////
			result = regINTERVAL.matcher(rrule);
			if (result.matches())
			{
				String f = result.group(1);
				Utils.setXmlElementValue(xml, recurrence, "interval", f);
			}

			// TODO: Android does not know until?
		}
	}

	@Override
	protected String writeXml(SyncContext sync)
			throws ParserConfigurationException, SyncException
	{
		CalendarEntry source = getLocalItem(sync);
		CacheEntry entry = sync.getCacheEntry();

		entry.setLocalHash(source.getLocalHash());
		final Date lastChanged = new Date();
		entry.setRemoteChangedDate(lastChanged);
		final String newUid = getNewUid();
		entry.setRemoteId(newUid);
		source.setUid(newUid);

		Document xml = Utils.newDocument("event");
		writeXml(xml, source, lastChanged);

		return Utils.getXml(xml);
	}

	private CalendarEntry getLocalItem(SyncContext sync) throws SyncException
	{
		if (sync.getLocalItem() != null) return (CalendarEntry) sync
				.getLocalItem();
		CalendarEntry c = calendarProvider.loadCalendarEntry(sync
				.getCacheEntry().getLocalId(), sync.getCacheEntry().getRemoteId());
		sync.setLocalItem(c);
		return c;
	}

	@Override
	protected String getMessageBodyText(SyncContext sync) throws SyncException
	{
		CalendarEntry cal = getLocalItem(sync);
		StringBuilder sb = new StringBuilder();

		sb.append(cal.getTitle());
		sb.append("\n");

		sb.append("Location: ");
		sb.append(cal.getEventLocation());
		sb.append("\n");

		sb.append("Start: ");
		sb.append(cal.getDtstart().format("%c")); // TODO: Change format for
													// allDay events
		sb.append("\n");

		sb.append("End: ");
		sb.append(cal.getDtend().format("%c"));// TODO: Change format for allDay
												// events
		sb.append("\n");

		sb.append("Recurrence: ");
		sb.append(cal.getrRule());
		sb.append("\n");

		sb.append("-----\n");
		sb.append(cal.getDescription());
		return sb.toString();
	}

	@Override
	public String getItemText(SyncContext sync) throws MessagingException
	{
		if (sync.getLocalItem() != null)
		{
			CalendarEntry item = (CalendarEntry) sync.getLocalItem();
			return item.getTitle() + ": " + item.getDtstart().toString();
		}
		else
		{
			return sync.getMessage().getSubject();
		}
	}
}
