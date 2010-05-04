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

package at.dasz.KolabDroid.ContactsContract;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

//import android.content.ContentValues;

public abstract class ContactMethod {
	private int kind, type;

	private String data;

	public final void setData(String data) {
		this.data = data;
	}

	public final String getData() {
		return data;
	}

	protected final void setKind(int kind) {
		this.kind = kind;
	}

	public final int getKind() {
		return kind;
	}

	public final void setType(int type) {
		this.type = type;
	}

	public final int getType() {
		return type;
	}
	
	@Override
	public String toString()
	{
		return getData();
	}

	//public abstract ContentValues toContentValues();
	
	//public abstract String getContentDirectory();

	public abstract void toXml(Document xml, Element parent, String fullName);
	public abstract void fromXml(Element parent);
}
