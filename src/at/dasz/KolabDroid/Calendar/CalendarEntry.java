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
