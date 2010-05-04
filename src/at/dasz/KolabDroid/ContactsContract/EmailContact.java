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
//import android.provider.Contacts;
import android.provider.ContactsContract;
//import android.provider.Contacts.People;
import at.dasz.KolabDroid.Utils;

public class EmailContact extends ContactMethod {
	
	public EmailContact() {		
		//setType(People.ContactMethods.TYPE_OTHER);
		setType(ContactsContract.CommonDataKinds.Email.TYPE_HOME);
	}
	
	/*
	@Override
	public ContentValues toContentValues() {
		ContentValues result = new ContentValues();
		result.put(Contacts.ContactMethods.KIND, this.getKind());
		result.put(Contacts.ContactMethods.TYPE, this.getType());
		result.put(Contacts.ContactMethods.DATA, this.getData());
		return result;
	}
	*/

	/*
	@Override
	public String getContentDirectory() {
		return Contacts.People.ContactMethods.CONTENT_DIRECTORY;
	}
	*/

	@Override
	public void toXml(Document xml, Element parent, String fullName)
	{
		//Element email = Utils.getOrCreateXmlElement(xml, parent, "email");
		Element email = Utils.createXmlElement(xml, parent, "email");
		Utils.setXmlElementValue(xml, email, "display-name", fullName);
		Utils.setXmlElementValue(xml, email, "smtp-address", getData());
	}
	
	@Override
	public void fromXml(Element parent)
	{
		this.setData(Utils.getXmlElementString(parent, "smtp-address"));
	}
}
