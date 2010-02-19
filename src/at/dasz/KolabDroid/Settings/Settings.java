package at.dasz.KolabDroid.Settings;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
	public static final String SETTINGS = "SETTINGS";
	
	private SharedPreferences pref;
	private SharedPreferences.Editor edit;
	
	public Settings(Context ctx) {
		pref = ctx.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);		
	}
	
	public void edit() {
		edit = pref.edit();
	}
	
	public void save() {
		edit.commit();
	}

	public void cancel() {
		edit = null;
	}
	
	public String getHost() {
		return pref.getString("HOST", "");
	}

	public void setHost(String value) {
		edit.putString("HOST", value);
	}

	public int getPort() {
		return pref.getInt("PORT", 993);
	}

	public void setPort(int value) {
		edit.putInt("PORT", value);
	}

	public boolean getUseSSL() {
		return pref.getBoolean("USESSL", true);
	}

	public void setUseSSL(boolean value) {
		edit.putBoolean("USESSL", value);
	}

	public String getUsername() {
		return pref.getString("USERNAME", "");
	}

	public void setUsername(String value) {
		edit.putString("USERNAME", value);
	}
	
	public String getPassword() {
		return pref.getString("PASSWORD", "");
	}

	public void setPassword(String value) {
		edit.putString("PASSWORD", value);
	}

	public String getContactsFolder()
	{
		return pref.getString("FOLDER_CONTACTS", "");
	}

	public void setContactsFolder(String value) {
		edit.putString("FOLDER_CONTACTS", value);
	}
	
	public String getCalendarFolder()
	{
		return pref.getString("FOLDER_CALENDAR", "");
	}

	public void setCalendarFolder(String value) {
		edit.putString("FOLDER_CALENDAR", value);
	}
}
