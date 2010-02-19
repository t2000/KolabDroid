package at.dasz.KolabDroid.Contacts;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.ContentValues;
import android.provider.Contacts;
import at.dasz.KolabDroid.Utils;

public class PhoneContact extends ContactMethod
{

	public PhoneContact()
	{
		setKind(Contacts.KIND_PHONE);
		setType(Contacts.Phones.TYPE_OTHER);
	}

	@Override
	public ContentValues toContentValues()
	{
		ContentValues result = new ContentValues();
		result.put(Contacts.Phones.TYPE, this.getType());
		result.put(Contacts.Phones.NUMBER, this.getData());
		return result;
	}

	@Override
	public String getContentDirectory()
	{
		return Contacts.People.Phones.CONTENT_DIRECTORY;
	}

	@Override
	public void toXml(Document xml, Element parent, String fullName)
	{
		Element email = Utils.getOrCreateXmlElement(xml, parent, "phone");
		switch (this.getType())
		{
		case Contacts.Phones.TYPE_HOME:
			Utils.setXmlElementValue(xml, email, "type", "home");
			break;
		case Contacts.Phones.TYPE_WORK:
			Utils.setXmlElementValue(xml, email, "type", "business");
			break;
		case Contacts.Phones.TYPE_MOBILE:
			Utils.setXmlElementValue(xml, email, "type", "mobile");
			break;
		default:
			break;
		}
		Utils.setXmlElementValue(xml, email, "number", getData());
	}
	
	@Override
	public void fromXml(Element parent)
	{
		this.setData(Utils.getXmlElementString(parent, "number"));
		String type = Utils.getXmlElementString(parent, "type");
		if("home".equals(type)) setType(Contacts.Phones.TYPE_HOME);
		if("business".equals(type)) setType(Contacts.Phones.TYPE_WORK);
		if("mobile".equals(type)) setType(Contacts.Phones.TYPE_MOBILE);
	}
}
