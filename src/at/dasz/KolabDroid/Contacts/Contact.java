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

package at.dasz.KolabDroid.Contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentValues;
import android.provider.Contacts.People;
import at.dasz.KolabDroid.Utils;

public class Contact
{
	private int					id;
	private String				fullName, uid;

	private List<ContactMethod>	contactMethods	= new ArrayList<ContactMethod>();
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}

	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}
	
	public String getFullName()
	{
		return fullName;
	}

	public List<ContactMethod> getContactMethods()
	{
		return contactMethods;
	}

	public ContentValues toContentValues()
	{
		ContentValues result = new ContentValues();
		result.put(People.NAME, this.fullName);
		return result;
	}

	@Override
	public String toString()
	{
		return getFullName() + " with " + getContactMethods().size()
				+ " contact methods";
	}

	public String getLocalHash()
	{
		ArrayList<String> contents = new ArrayList<String>(contactMethods
				.size() + 1);
		contents.add(getFullName() == null ? "no name" : getFullName());

		Collections.sort(contactMethods, new Comparator<ContactMethod>() {
			public int compare(ContactMethod cm1, ContactMethod cm2)
			{
				return cm1.toString().compareTo(cm2.toString());
			}
		});

		for (ContactMethod cm : contactMethods)
		{
			contents.add(cm.getData());
		}

		return Utils.join("|", contents.toArray());
	}
}
