package at.dasz.KolabDroid.Calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import at.dasz.KolabDroid.Utils;
import at.dasz.KolabDroid.Provider.LocalCacheProvider;
import at.dasz.KolabDroid.Settings.Settings;
import at.dasz.KolabDroid.Sync.AbstractSyncHandler;
import at.dasz.KolabDroid.Sync.CacheEntry;
import at.dasz.KolabDroid.Sync.SyncContext;

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
		System.out.println("Deleting Calendar#" + localId);
		calendarProvider.delete(localId);
	}

	@Override
	protected String getMimeType()
	{
		return "application/x-vnd.kolab.event";
	}

	public boolean hasLocalItem(SyncContext sync)
	{
		return getLocalItem(sync) != null;
	}

	public boolean hasLocalChanges(SyncContext sync)
	{
		CacheEntry e = sync.getCacheEntry();
		System.out.println("Checking for local changes: #" + e.getLocalId());
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

		cal.setDescription(Utils.getXmlElementString(root, "body"));
		cal.setTitle(Utils.getXmlElementString(root, "summary"));
		cal.setEventLocation(Utils.getXmlElementString(root, "location"));
		cal.setDtstart(Utils.getXmlElementTime(root, "start-date"));
		cal.setDtend(Utils.getXmlElementTime(root, "end-date"));

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
			int length = days.getLength();
			if (length > 0)
			{
				sb.append(";BYDAY=");
				for (int i = 0; i < length; i++)
				{
					if (daynumber > 1) sb.append(daynumber);

					Element day = (Element) days.item(i);
					String d = Utils.getXmlElementString(day);
					if ("monday".equals(d)) sb.append("MO");
					if ("tuesday".equals(d)) sb.append("TU");
					if ("wednesday".equals(d)) sb.append("WE");
					if ("thursday".equals(d)) sb.append("TH");
					if ("friday".equals(d)) sb.append("FR");
					if ("saturday".equals(d)) sb.append("SA");
					if ("sunday".equals(d)) sb.append("SU");

					if ((i + 1) < length) sb.append(",");
				}
			}

			if (daynumber != 0 && "MONTHLY".equals(cycle))
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
		return result;
	}

	private String getNewUid()
	{
		// Create Application and Type specific id
		// kd == Kolab Droid
		return "kd-ev-" + UUID.randomUUID().toString();
	}

	@Override
	protected void updateServerItemFromLocal(SyncContext sync, Document xml)
	{
		CalendarEntry source = getLocalItem(sync);
		CacheEntry entry = sync.getCacheEntry();

		entry.setLocalHash(source.getLocalHash());
		final Date lastChanged = new Date();
		entry.setRemoteChangedDate(lastChanged);

		writeXml(xml, source, lastChanged);

	}

	private final static java.util.regex.Pattern	regFREQ			= java.util.regex.Pattern
																			.compile("FREQ=(\\w*);.*");
	// private final static java.util.regex.Pattern regUntil =
	// java.util.regex.Pattern
	// .compile(";UNTIL=(\\w*)");
	// private final static java.util.regex.Pattern regWKST =
	// java.util.regex.Pattern
	// .compile(";WKST=(\\w*)");
	private final static java.util.regex.Pattern	regBYDAY		= java.util.regex.Pattern
																			.compile(".*;BYDAY=(.*);?");
	private final static java.util.regex.Pattern	regINTERVAL		= java.util.regex.Pattern
																			.compile(".*;INTERVAL=(\\d*);?");
	private final static java.util.regex.Pattern	regBYDAYWeekDay	= java.util.regex.Pattern
																			.compile("(?:([+-]?)([0-9]*)([A-Z]{2}),?)");

	private void writeXml(Document xml, CalendarEntry source,
			final Date lastChanged)
	{
		Element root = xml.getDocumentElement();

		Utils.setXmlElementValue(xml, root, "body", source.getDescription());
		Utils.setXmlElementValue(xml, root, "last-modification-date", Utils
				.toUtc(lastChanged));
		Utils.setXmlElementValue(xml, root, "summary", source.getTitle());
		Utils.setXmlElementValue(xml, root, "location", source
				.getEventLocation());
		Utils.setXmlElementValue(xml, root, "start-date", source.getDtstart()
				.format3339(source.getAllDay()));
		Utils.setXmlElementValue(xml, root, "end-date", source.getDtend()
				.format3339(source.getAllDay()));

		String rrule = source.getrRule();
		if (rrule != null && !"".equals(rrule))
		{
			Element recurrence = Utils.getOrCreateXmlElement(xml, root,
					"recurrence");
			Utils.deleteXmlElements(recurrence, "day");

			Matcher result;

			result = regFREQ.matcher(rrule);
			if (result.matches())
			{
				String f = result.group(1);
				Utils.setXmlAttributeValue(xml, recurrence, "cycle", f
						.toLowerCase());
			}

			result = regBYDAY.matcher(rrule);
			String daynumber = "";
			if (result.matches())
			{
				final String group = result.group(1);
				Matcher grpResult = regBYDAYWeekDay.matcher(group);
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
					String g = grpResult.group(3);
					String day = "";
					if ("MO".equals(g)) day = "monday";
					if ("TU".equals(g)) day = "tuesday";
					if ("WE".equals(g)) day = "wednesday";
					if ("TH".equals(g)) day = "thursday";
					if ("FR".equals(g)) day = "friday";
					if ("SA".equals(g)) day = "saturday";
					if ("SU".equals(g)) day = "sunday";
					Utils.addXmlElementValue(xml, recurrence, "day", day);
				}
			}
			Utils.setXmlElementValue(xml, recurrence, "daynumber", daynumber);

			result = regINTERVAL.matcher(rrule);
			if (result.matches())
			{
				String f = result.group(1);
				Utils.setXmlElementValue(xml, recurrence, "interval", f);
			}

			// Android does not know until

		}
	}

	@Override
	protected String writeXml(SyncContext sync)
			throws ParserConfigurationException
	{
		CalendarEntry source = getLocalItem(sync);
		CacheEntry entry = sync.getCacheEntry();

		entry.setLocalHash(source.getLocalHash());
		final Date lastChanged = new Date();
		entry.setRemoteChangedDate(lastChanged);
		final String newUid = getNewUid();
		entry.setRemoteId(newUid);

		Document xml = Utils.newDocument("event");
		writeXml(xml, source, lastChanged);

		return Utils.getXml(xml);
	}

	private CalendarEntry getLocalItem(SyncContext sync)
	{
		if (sync.getLocalItem() != null) return (CalendarEntry) sync
				.getLocalItem();
		CalendarEntry c = calendarProvider.loadCalendarEntry(sync
				.getCacheEntry().getLocalId());
		sync.setLocalItem(c);
		return c;
	}

	@Override
	protected String getMessageBodyText(SyncContext sync)
	{
		CalendarEntry cal = getLocalItem(sync);
		StringBuilder sb = new StringBuilder();

		sb.append(cal.getTitle());
		sb.append("\n");

		sb.append("Location: ");
		sb.append(cal.getEventLocation());
		sb.append("\n");

		sb.append("Start: ");
		sb.append(cal.getDtstart().format("%c"));
		sb.append("\n");

		sb.append("End: ");
		sb.append(cal.getDtend().format("%c"));
		sb.append("\n");

		sb.append("Recurrence: ");
		sb.append(cal.getrRule());
		sb.append("\n");

		sb.append("-----\n");
		sb.append(cal.getDescription());
		return sb.toString();
	}

}
