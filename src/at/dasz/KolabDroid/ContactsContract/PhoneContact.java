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
import at.dasz.KolabDroid.Utils;

public class PhoneContact extends ContactMethod
{

	public PhoneContact()
	{
		//setKind(Contacts.KIND_PHONE);
		//setKind(ContactsContract.CommonDataKinds.Phone.TYPE)
		
		//setType(Contacts.Phones.TYPE_OTHER);
		setType(ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
	}

	/*
	@Override
	public ContentValues toContentValues()
	{
		ContentValues result = new ContentValues();
		result.put(Contacts.Phones.TYPE, this.getType());
		result.put(Contacts.Phones.NUMBER, this.getData());
		return result;
	}
	*/

	/*
	@Override
	public String getContentDirectory()
	{
		return Contacts.People.Phones.CONTENT_DIRECTORY;
	}
	*/

	@Override
	public void toXml(Document xml, Element parent, String fullName)
	{
		//Element phone = Utils.getOrCreateXmlElement(xml, parent, "phone");
		Element phone = Utils.createXmlElement(xml, parent, "phone");
		switch (this.getType())
		{
		//TODO: we only support 1 home and 1 business number for now		
		case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
			Utils.setXmlElementValue(xml, phone, "type", "home1");
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
			Utils.setXmlElementValue(xml, phone, "type", "business1");
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
			Utils.setXmlElementValue(xml, phone, "type", "mobile");
			break;
		default:
			break;
		}
		
		//strip "-" out of phone numbers
		String tmp = getData();
		String stripped = tmp.replaceAll("-", "");
		
		Utils.setXmlElementValue(xml, phone, "number", stripped);
	}
	
	@Override
	public void fromXml(Element parent)
	{
		this.setData(Utils.getXmlElementString(parent, "number"));
		String type = Utils.getXmlElementString(parent, "type");
		/*
		if("home".equals(type)) setType(Contacts.Phones.TYPE_HOME);
		if("business".equals(type)) setType(Contacts.Phones.TYPE_WORK);
		if("mobile".equals(type)) setType(Contacts.Phones.TYPE_MOBILE);
		*/
		if(type != null)
		{
			if(type.startsWith("home")) setType(ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
			if(type.startsWith("business")) setType(ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
			if(type.startsWith("mobile")) setType(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
		}
	}
}
