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
	
	public boolean getCreateRemoteHash() {
		return pref.getBoolean("CREATE_REMOTE_HASH", false);
	}

	public void setCreateRemoteHash(boolean value) {
		edit.putBoolean("CREATE_REMOTE_HASH", value);
	}
}
