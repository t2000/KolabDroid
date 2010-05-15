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

import java.util.ArrayList;

import android.text.format.Time;
import at.dasz.KolabDroid.Utils;

public class CalendarEntry
{

	public static final String	YEARLY	= "YEARLY";
	public static final String	MONTHLY	= "MONTHLY";
	public static final String	WEEKLY	= "WEEKLY";
	public static final String	DAILY	= "DAILY";

	private int					id;
	private int					calendar_id;
	private String				title;
	private boolean				allDay;
	private Time				dtstart;
	private Time				dtend;
	private String				description;
	private String				eventLocation;
	private int					visibility;
	private int					hasAlarm;
	private String				rRule;
	private String				uid;
	private int					reminderTime = -1;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int getCalendar_id()
	{
		return calendar_id;
	}

	public void setCalendar_id(int calendarId)
	{
		calendar_id = calendarId;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public boolean getAllDay()
	{
		return allDay;
	}

	public void setAllDay(boolean allDay)
	{
		this.allDay = allDay;
		if(allDay) {
			if(dtstart != null) {
				dtstart.hour = 0;
				dtstart.minute = 0;
				dtstart.second = 0;
				dtstart.timezone = Time.TIMEZONE_UTC;
				dtstart.normalize(true);
			}
			if(dtend != null) {
				dtend.hour = 0;
				dtend.minute = 0;
				dtend.second = 0;
				dtend.timezone = Time.TIMEZONE_UTC;
				dtend.normalize(true);
			}
		}
	}

	public Time getDtstart()
	{
		return dtstart;
	}

	public void setDtstart(Time dtstart)
	{
		this.dtstart = dtstart;
	}

	public Time getDtend()
	{
		return dtend;
	}

	public void setDtend(Time dtend)
	{
		this.dtend = dtend;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getEventLocation()
	{
		return eventLocation;
	}

	public void setEventLocation(String eventLocation)
	{
		this.eventLocation = eventLocation;
	}

	public int getVisibility()
	{
		return visibility;
	}

	public void setVisibility(int visibility)
	{
		this.visibility = visibility;
	}

	public int getHasAlarm()
	{
		return hasAlarm;
	}

	public void setHasAlarm(int hasAlarm)
	{
		this.hasAlarm = hasAlarm;
	}

	public String getrRule()
	{
		return rRule;
	}

	public void setrRule(String rRule)
	{
		this.rRule = rRule;
	}

	public int getReminderTime()
	{
		return reminderTime;
	}

	public void setReminderTime(int reminderTime)
	{
		this.reminderTime = reminderTime;
	}

	@Override
	public String toString()
	{
		return this.title + ": " + this.dtstart;
	}

	public String getLocalHash()
	{
		ArrayList<String> contents = new ArrayList<String>(4);
		contents.add(getTitle() == null ? "no title" : getTitle());
		contents.add(getDtstart() == null ? "no start" : "start "
				+ getDtstart().toMillis(false));
		contents.add(getDtend() == null ? "no end" : "end "
				+ getDtend().toMillis(false));
		contents.add(getAllDay() ? "AllDay" : "Not AllDay");
		contents.add(getDescription() == null ? "no Description"
				: getDescription());
		contents.add(getEventLocation() == null ? "no EventLocation"
				: getEventLocation());
		contents.add(getrRule() == null ? "no rRule" : getrRule());
		contents.add(getReminderTime() == -1 ? "no Reminder" : Integer.toString(getReminderTime()));
		return Utils.join("|", contents.toArray());
	}

	public static String monthToKolabMonth(String m)
	{
		if ("1".equals(m)) return "january";
		else if ("2".equals(m)) return "february";
		else if ("3".equals(m)) return "march";
		else if ("4".equals(m)) return "april";
		else if ("5".equals(m)) return "may";
		else if ("6".equals(m)) return "june";
		else if ("7".equals(m)) return "july";
		else if ("8".equals(m)) return "august";
		else if ("9".equals(m)) return "september";
		else if ("10".equals(m)) return "october";
		else if ("11".equals(m)) return "november";
		else if ("12".equals(m)) return "december";
		return "";
	}

	public static String kolabMonthToMonth(String m)
	{
		if ("january".equals(m)) return "1";
		else if ("february".equals(m)) return "2";
		else if ("march".equals(m)) return "3";
		else if ("april".equals(m)) return "4";
		else if ("may".equals(m)) return "5";
		else if ("june".equals(m)) return "6";
		else if ("july".equals(m)) return "7";
		else if ("august".equals(m)) return "8";
		else if ("september".equals(m)) return "9";
		else if ("october".equals(m)) return "10";
		else if ("november".equals(m)) return "11";
		else if ("december".equals(m)) return "12";
		return "";
	}

	public static String weekDayToKolabWeekDay(String d)
	{
		if ("MO".equals(d)) return "monday";
		else if ("TU".equals(d)) return "tuesday";
		else if ("WE".equals(d)) return "wednesday";
		else if ("TH".equals(d)) return "thursday";
		else if ("FR".equals(d)) return "friday";
		else if ("SA".equals(d)) return "saturday";
		else if ("SU".equals(d)) return "sunday";
		return "";
	}

	public static String kolabWeekDayToWeekDay(String d)
	{
		if ("monday".equals(d)) return "MO";
		else if ("tuesday".equals(d)) return "TU";
		else if ("wednesday".equals(d)) return "WE";
		else if ("thursday".equals(d)) return "TH";
		else if ("friday".equals(d)) return "FR";
		else if ("saturday".equals(d)) return "SA";
		else if ("sunday".equals(d)) return "SU";
		return "";
	}
}
