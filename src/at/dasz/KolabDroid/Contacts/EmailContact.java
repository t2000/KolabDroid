package at.dasz.KolabDroid.Contacts;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.ContentValues;
import android.provider.Contacts;
import android.provider.Contacts.People;
import at.dasz.KolabDroid.Utils;

public class EmailContact extends ContactMethod {
	
	public EmailContact() {
		setKind(Contacts.KIND_EMAIL);
		setType(People.ContactMethods.TYPE_OTHER);
	}
	
	@Override
	public ContentValues toContentValues() {
		ContentValues result = new ContentValues();
		result.put(Contacts.ContactMethods.KIND, this.getKind());
		result.put(Contacts.ContactMethods.TYPE, this.getType());
		result.put(Contacts.ContactMethods.DATA, this.getData());
		return result;
	}

	@Override
	public String getContentDirectory() {
		return Contacts.People.ContactMethods.CONTENT_DIRECTORY;
	}

	@Override
	public void toXml(Document xml, Element parent, String fullName)
	{
		Element email = Utils.getOrCreateXmlElement(xml, parent, "email");
		Utils.setXmlElementValue(xml, email, "display-name", fullName);
		Utils.setXmlElementValue(xml, email, "smtp-address", getData());
	}
	
	@Override
	public void fromXml(Element parent)
	{
		this.setData(Utils.getXmlElementString(parent, "smtp-address"));
	}
}
