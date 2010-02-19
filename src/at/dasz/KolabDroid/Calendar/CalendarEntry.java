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

public class CalendarEntry {
	private int id;
	private int calendar_id;
	private String title;
	private boolean allDay;
	private Time dtstart;
	private Time dtend;
	private String description;
	private String eventLocation;
	private int visibility;
	private int hasAlarm;
	private String rRule;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCalendar_id() {
		return calendar_id;
	}
	public void setCalendar_id(int calendarId) {
		calendar_id = calendarId;
	}	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean getAllDay() {
		return allDay;
	}
	public void setAllDay(boolean allDay) {
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
	public Time getDtstart() {
		return dtstart;
	}
	public void setDtstart(Time dtstart) {
		this.dtstart = dtstart;
	}
	public Time getDtend() {
		return dtend;
	}
	public void setDtend(Time dtend) {
		this.dtend = dtend;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getEventLocation() {
		return eventLocation;
	}
	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}
	public int getVisibility() {
		return visibility;
	}
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}
	public int getHasAlarm() {
		return hasAlarm;
	}
	public void setHasAlarm(int hasAlarm) {
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
	
	@Override
	public String toString() {
		return this.title + ": " + this.dtstart;
	}
	public String getLocalHash()
	{
		ArrayList<String> contents = new ArrayList<String>(4);
		contents.add(getTitle() == null ? "no title" : getTitle());
		contents.add(getDtstart() == null ? "no start" : "start " + getDtstart().toMillis(false));
		contents.add(getDtend() == null ? "no end" : "end " + getDtend().toMillis(false));
		contents.add(getAllDay() ? "AllDay" : "Not AllDay");
		contents.add(getDescription() == null ? "no Description" : getDescription());
		contents.add(getEventLocation() == null ? "no EventLocation" : getEventLocation());
		contents.add(getrRule() == null ? "no rRule" : getrRule());
		return Utils.join("|", contents.toArray());	
	}
}
