package at.dasz.KolabDroid.Contacts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import corinis.util.xml.ChildElementAdapter;

import android.content.ContentValues;
import android.provider.Contacts;
import android.provider.Contacts.People;
import at.dasz.KolabDroid.Utils;

public class ContactEntry implements Comparable<ContactEntry>, Serializable
{

	/**
	 * for serialization
	 */
	private static final long	serialVersionUID	= 857231805984141277L;

	/**
	 * holds the data as StringBuffer
	 */
	StringBuffer data;
	
	/**
	 * same as data, but buffers the converted xml
	 */ 
	Document xmlData;
	
	/**
	 *  uid to identify this contact
	 */
	String uid;
	
	/**
	 * Array of all known fields. These will be used for comparison as well (see fieldnumber below)
	 */
	public String[] fields = new String[50];
	
	public ArrayList<String> additonalEmail = new ArrayList<String>();
	
	private static final int	DisplayName=0;
	private static final int	FirstName = 1;
	private static final int	LastName = 2;
	private static final int	PreferMailFormat	=3;
	private static final int	JobTitle	= 4;
	private static final int	NickName	= 5;
	private static final int	PrimaryEmail	= 6;
	private static final int	SecondEmail	= 7;
	private static final int	Category	= 8;
	private static final int	Company	= 9;
	private static final int	CellularNumber	= 10;
	private static final int	HomePhone	= 11;
	private static final int	FaxNumber	= 12;
	private static final int	WorkPhone	= 13;
	private static final int	PagerNumber	= 14;
	private static final int	BirthYear	= 15;
	private static final int	BirthMonth	= 16;
	private static final int	BirthDay	= 17;
	private static final int	AnniversaryYear	= 18;
	private static final int	AnniversaryMonth	= 19;
	private static final int	AnniversaryDay	= 20;
	private static final int	PhotoName	= 21;
	private static final int	Notes	= 22;
	private static final int	Department	= 23;
	private static final int	WebPage1	= 24;
	private static final int	WebPage2	= 25;
	private static final int	Custom1	= 26;
	private static final int	Custom2	= 27;
	private static final int	Custom3	= 28;
	private static final int	Custom4	= 29;
	private static final int	AimScreenName	= 30;
	private static final int	AllowRemoteContent	= 31;
	private static final int	HomeAddress	= 32;
	private static final int	HomeAddress2	= 33;
	private static final int	HomeCity	= 34;
	private static final int	HomeState	= 35;
	private static final int	HomeZipCode	= 36;
	private static final int	HomeCountry	= 37;
	private static final int	WorkAddress	= 38;
	private static final int	WorkAddress2	= 39;
	private static final int	WorkCity	= 40;
	private static final int	WorkState	= 41;
	private static final int	WorkZipCode	= 42;
	private static final int	WorkCountry	= 43;

	
	private static final String MAIL_FORMAT_UNKNOWN = "0";
	private static final String MAIL_FORMAT_PLAINTEXT = "1";
	private static final String MAIL_FORMAT_HTML = "2";
	
	
	// index holders for android urls TODO: extend this to all thats required to save as much as possible
	private String contactURI;
	private String emailURI;
	
	public ContactEntry (StringBuffer data, Document xmlData) {
		this.data = data;
		this.xmlData = xmlData;
		for (int i =0; i< this.fields.length; i++)
			fields[i] = null;
	}
	
	/**
	 * checks if a property exists
	 * @param prop the property index to check
	 * @return true if the property is valid
	 */
	private boolean haveProperty(int prop) {
		return this.fields[prop]!=null && !this.fields[prop].equals("");
	}
	

	/**
	 * create a xml subnode in a stringbuffer
	 * @param xml the stringbuffer to write into
	 * @param node the name
	 * @param content the content
	 * @param b TODO: why to we need this?
	 */
	private void nodeWithContent (StringBuffer xml, String node, String content, boolean b) {
		// skip empty nodes
		if (node == null || content == null)
			return;
		
		xml.append('<');
		xml.append(node);
		xml.append('>');
		xml.append(content);
		xml.append('<');
		xml.append('/');
		xml.append(node);
		xml.append('>');
		// add a newline
		xml.append('\n'); 
	}
	

	private void fillContentValues(ContentValues values, String key,
			int field)
	{
		if (this.haveProperty(field))
			values.put(key, this.getProperty(field));
	}

	public void readFromAndroid () {
		// TODO: update the fields from android internally
	}
	
	public void writeToAndroid () {
		// default content
		ContentValues contact = new ContentValues();
		fillContentValues(contact, People.NAME, DisplayName);
		//People.CONTENT_URI
		//People.PRIMARY_EMAIL_ID
		//People.PRIMARY_ORGANIZATION_ID
		//People.PRIMARY_PHONE_ID
		//People.IM_ACCOUNT
		//People.NAME
		//People.NOTES
		//People.NUMBER
		//People.TYPE_FAX_HOME
		//People.TYPE_FAX_WORK
		//People.TYPE_HOME
		//People.TYPE_HOME
		//People.TYPE_MOBILE
		//People.TYPE_WORK
		//People.TYPE_PAGER
		//People.TYPE_OTHER
		
		//Contacts.KIND_POSTAL
		
		// emails
		ContentValues data = new ContentValues();
		data.put(Contacts.ContactMethods.KIND, Contacts.KIND_EMAIL);
		data.put(Contacts.ContactMethods.TYPE, People.ContactMethods.TYPE_OTHER);
		fillContentValues(data, Contacts.ContactMethods.DATA, PrimaryEmail);

		data = new ContentValues();
		data.put(Contacts.ContactMethods.KIND, Contacts.KIND_EMAIL);
		data.put(Contacts.ContactMethods.TYPE, People.ContactMethods.TYPE_OTHER);
		fillContentValues(data, Contacts.ContactMethods.DATA, SecondEmail);

		// phone
		data = new ContentValues();
		data.put(Contacts.ContactMethods.KIND, Contacts.KIND_PHONE);
		data.put(Contacts.ContactMethods.TYPE, People.ContactMethods.TYPE_HOME);
		fillContentValues(data, Contacts.Phones.NUMBER, HomePhone);
		

		data = new ContentValues();
		data.put(Contacts.ContactMethods.KIND, Contacts.KIND_PHONE);
		data.put(Contacts.ContactMethods.TYPE, People.ContactMethods.TYPE_WORK);
		fillContentValues(data, Contacts.Phones.NUMBER, WorkPhone);

		data = new ContentValues();
		data.put(Contacts.ContactMethods.KIND, Contacts.KIND_PHONE);
		data.put(Contacts.ContactMethods.TYPE, People.ContactMethods.TYPE_OTHER);
		fillContentValues(data, Contacts.Phones.NUMBER, CellularNumber);
		
		// postal is one string - max 4 lines in 1.6

	}
	

	/**
	 * @param xml the stringbuffer to create the xml into
	 * @return a kolab xml representation in a stringbuffer
	 * TODO: dont we actually want a writer for this?
	 */
	public void toStringBuffer (StringBuffer xml) {
		
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xml.append("<contact version=\"1.0\" >\n");
		xml.append(" <product-id>SyncKolab, Kolab resource</product-id>\n");
		nodeWithContent(xml, "uid", uid, false);
		nodeWithContent(xml, "categories", this.getProperty(Category), false);
		// TODO do we have/need these?
		//xml.append(" <creation-date>"+date2String(new Date(this.getProperty(LastModifiedDate")*1000))+"T"+time2String(new Date(this.getProperty(LastModifiedDate")*1000))+"Z</creation-date>\n";
		//xml.append(" <last-modification-date>"+date2String(new Date(this.getProperty(LastModifiedDate)*1000))+"T"+time2String(new Date(this.getProperty(LastModifiedDate)*1000))+"Z</last-modification-date>\n");
		// ??
		xml.append(" <sensitivity>public</sensitivity>\n");
		if (this.haveProperty(Notes))
			nodeWithContent(xml, "body", this.getProperty(Notes), false);

		if (this.haveProperty(FirstName) || this.haveProperty(LastName) ||this.haveProperty(DisplayName) ||
			this.haveProperty(NickName))
		{
			xml.append(" <name>\n");
			if (this.haveProperty(FirstName))
				nodeWithContent(xml, "given-name", getProperty(FirstName), false);
//				xml.append("  <middle-names>"+this.getProperty(NickName")+"</middle-names>\n"; // not really correct...
			if (this.haveProperty(LastName))
				nodeWithContent(xml, "last-name", getProperty(LastName), false);
			if (this.haveProperty(DisplayName))
				nodeWithContent(xml, "full-name", getProperty(DisplayName), false);
			else
			if (this.haveProperty(FirstName) || this.haveProperty(LastName))
			{
				String displayName = getProperty(FirstName) + " " + getProperty(LastName);
				nodeWithContent(xml, "full-name", displayName, false);
			}
			xml.append(" </name>\n");
		}
		nodeWithContent(xml, "organization", getProperty(Company), false);
		nodeWithContent(xml, "web-page", getProperty(WebPage1), false);
		// not really kolab.. but we need that somewhere
		nodeWithContent(xml, "business-web-page", getProperty(WebPage2), false);
		nodeWithContent(xml, "im-address", getProperty(AimScreenName), false);
		nodeWithContent(xml, "department", getProperty(Department), false);
	//" <office-location>zuhaus</office-location>\n";
	//" <profession>programmierer</profession>\n";
		nodeWithContent(xml, "job-title", getProperty(JobTitle), false);
		nodeWithContent(xml, "nick-name", getProperty(NickName), false);
		
		if (this.haveProperty(BirthYear) && this.haveProperty(BirthMonth) && this.haveProperty(BirthDay))
		{
			String adate = getProperty(BirthYear) + "-" + getProperty(BirthMonth) + "-" + getProperty(BirthDay);
			nodeWithContent(xml, "birthday", adate, false);
		}
		if(this.haveProperty(AnniversaryYear) && this.haveProperty(AnniversaryMonth) && this.haveProperty(AnniversaryDay))
		{
			String adate = getProperty(AnniversaryYear) + "-" + getProperty(AnniversaryMonth) + "-" + getProperty(AnniversaryDay);
			nodeWithContent(xml, "anniversary", adate, false);
		}
		if (this.haveProperty(HomePhone))
		{	
			xml.append(" <phone>\n");
			xml.append("  <type>home1</type>\n");
			nodeWithContent(xml, "number", getProperty(HomePhone), false);
			xml.append(" </phone>\n");
		}
		if (this.haveProperty(WorkPhone))
		{	
			xml.append(" <phone>\n");
			xml.append("  <type>business1</type>\n");
			nodeWithContent(xml, "number", getProperty(WorkPhone), false);
			xml.append(" </phone>\n");
		}
		if (this.haveProperty(FaxNumber))
		{	
			xml.append(" <phone>\n");
			xml.append("  <type>fax</type>\n");
			nodeWithContent(xml, "number", getProperty(FaxNumber), false);
			xml.append(" </phone>\n");
		}
		if (this.haveProperty(CellularNumber))
		{	
			xml.append(" <phone>\n");
			xml.append("  <type>mobile</type>\n");
			nodeWithContent(xml, "number", getProperty(CellularNumber), false);
			xml.append(" </phone>\n");
		}
		if (this.haveProperty(PagerNumber))
		{	
			xml.append(" <phone>\n");
			xml.append("  <type>page</type>\n");
			nodeWithContent(xml, "number", getProperty(PagerNumber), false);
			xml.append(" </phone>\n");
		}
		
		if (this.haveProperty(PrimaryEmail))
		{
			xml.append(" <email type=\"primary\">\n");
			nodeWithContent(xml, "display-name", getProperty(DisplayName), false);
			nodeWithContent(xml, "smtp-address", getProperty(PrimaryEmail), false);
			xml.append(" </email>\n");
		}
		
		if (this.haveProperty(SecondEmail))
		{
			xml.append(" <email>\n");
			nodeWithContent(xml, "display-name", getProperty(DisplayName), false);
			nodeWithContent(xml, "smtp-address", getProperty(SecondEmail), false);
			xml.append(" </email>\n");
		}

		// if the mail format is set... 
		if (!this.getProperty(PreferMailFormat).equals(ContactEntry.MAIL_FORMAT_UNKNOWN))
		{
			if (this.getProperty(PreferMailFormat).equals(ContactEntry.MAIL_FORMAT_PLAINTEXT))
			{
				nodeWithContent(xml, "prefer-mail-format", "text", false);
			}
			else
			{
				nodeWithContent(xml, "prefer-mail-format", "html", false);
			}
		}

		if (this.haveProperty(HomeAddress) || this.haveProperty(HomeAddress2) ||
			this.haveProperty(HomeCity) || this.haveProperty(HomeState) ||
			this.haveProperty(HomeZipCode) || this.haveProperty(HomeCountry))
		{
			xml.append(" <address>\n");
			xml.append("  <type>home</type>\n");
			nodeWithContent(xml, "street", this.getProperty(HomeAddress), false);
			nodeWithContent(xml, "street2", this.getProperty(HomeAddress2), false);
			nodeWithContent(xml, "locality", this.getProperty(HomeCity), false);
			nodeWithContent(xml, "region", this.getProperty(HomeState), false);
			nodeWithContent(xml, "postal-code", this.getProperty(HomeZipCode), false);
			nodeWithContent(xml, "country", this.getProperty(HomeCountry), false);
			xml.append(" </address>\n");
		}

		if (this.haveProperty(WorkAddress) || this.haveProperty(WorkAddress2) ||
			this.haveProperty(WorkCity) || this.haveProperty(WorkState) ||
			this.haveProperty(WorkZipCode) || this.haveProperty(WorkCountry))
		{
			xml.append(" <address>\n");
			xml.append("  <type>business</type>\n");
			nodeWithContent(xml, "street", this.getProperty(WorkAddress), false);
			nodeWithContent(xml, "street2", this.getProperty(WorkAddress2), false);
			nodeWithContent(xml, "locality", this.getProperty(WorkCity), false);
			nodeWithContent(xml, "region", this.getProperty(WorkState), false);
			nodeWithContent(xml, "postal-code", this.getProperty(WorkZipCode), false);
			nodeWithContent(xml, "country", this.getProperty(WorkCountry), false);
			xml.append(" </address>\n");
		}
		
		// photo name = photo - this is an attachment (handled outside)
		// TODO handle this
		nodeWithContent(xml, "picture", this.getProperty(PhotoName), false); 

		
		//nodeWithContent(xml, "preferred-address", this.getProperty(DefaultAddress"), false); @deprecated
		nodeWithContent(xml, "custom1", this.getProperty(Custom1), false);
		nodeWithContent(xml, "custom2", this.getProperty(Custom2), false);
		nodeWithContent(xml, "custom3", this.getProperty(Custom3), false);
		nodeWithContent(xml, "custom4", this.getProperty(Custom4), false);
		if (this.getProperty(AllowRemoteContent).equals("true"))
			nodeWithContent(xml, "allow-remote-content", "true", false);
		else
			nodeWithContent(xml, "allow-remote-content", "false", false);
			
		// add extra/missing fields
		/*
		 * TODO: there shoudlnt be any extra fields
		if (fields != null)
		{
			xml.append(fields.toXmlString());
		}
		*/
		xml.append("</contact>\n");
	}
	
	public void setProperty(int field, String value) {
		fields[field] = value;
	}
	
	private String getProperty(int field) {
		return fields[field];
	}
	
	public boolean parse () throws Exception {
		if (xmlData == null && data == null) {
			throw new Exception("No data available");
		}
		
		if (xmlData == null) {
			InputStream xmlinput = new ByteArrayInputStream(data.toString().getBytes());
			// Parse XML
			this.xmlData = Utils.getDocument(xmlinput);
		}
		
		
		/**
		 * <contact>
		 * 	<uid>
		 *  <sensitivity>
		 *  <name>
		 *   <given-name>
		 *   <last-name>
		 *   <full-name>
		 *  </name>
		 *  ...
		 * </contact>
		 */
		boolean found = false; // set to true when valid data has been found;
		int email = 0;
		for(Element cur : new ChildElementAdapter(xmlData.getFirstChild())) {
			
			String name = cur.getNodeName().toUpperCase();
			
			if(name.equals("LAST-MODIFICATION-DATE")) {
				// ignore
				continue;
			}
			
			if (name.equals("NAME")) {
				this.setProperty(DisplayName, Utils.getXmlElementString(cur, "FULL-NAME"));
				// additionaly save given 
				this.setProperty(FirstName, Utils.getXmlElementString(cur, "GIVEN-NAME"));
				this.setProperty(LastName, Utils.getXmlElementString(cur, "LAST-NAME"));
				found = true;
				continue;
			}
			if (name.equals("PREFER-MAIL-FORMAT")) {
				String format = cur.getNodeValue();
				if (format == null)
					continue;
				format = format.toUpperCase();
				this.setProperty(PreferMailFormat, ContactEntry.MAIL_FORMAT_UNKNOWN);
				if (format.equals("PLAINTEXT") || format.equals("TEXT") || format.equals("TXT") || format.equals("PLAIN") || format.equals("1"))
					this.setProperty(PreferMailFormat, ContactEntry.MAIL_FORMAT_PLAINTEXT);
				if (format.equals("HTML") || format.equals("RUCHTEXT") || format.equals("RICH") || format.equals("2"))
					this.setProperty(PreferMailFormat, ContactEntry.MAIL_FORMAT_HTML);
			}

			if (name.equals("JOB-TITLE")) {
				this.setProperty(JobTitle, cur.getNodeValue());
				found = true;
				continue;
			}

			if (name.equals("NICK-NAME")) {
				this.setProperty(NickName, cur.getNodeValue());
				found = true;
				continue;
			}

			if (name.equals("EMAIL")) {
				//com.synckolab.tools.logMessage("email: " + email + " - " + cur.getXmlResult("SMTP-ADDRESS", ""), this.global.LOG_DEBUG + this.global.LOG_AB);
				switch (email)
				{
					case 0:
						this.setProperty(PrimaryEmail, Utils.getXmlElementString(cur, "SMTP-ADDRESS"));
						break;
					case 1:
						this.setProperty(SecondEmail, Utils.getXmlElementString(cur, "SMTP-ADDRESS"));
						break;
					default:
						// remember other emails
						// TODO: show some error (what to do?)
						//extraFields.addField("EMAIL", Utils.getXmlElementString(cur, "SMTP-ADDRESS"));
						break;
						
				}
				email++;
				found = true;
				continue;
			}
			
			if (name.equals("CATEGORIES")) {
				this.setProperty(Category, cur.getNodeValue());
				continue;
			}

			if (name.equals("ORGANIZATION")) {
				this.setProperty(Company, cur.getNodeValue());
				found = true;
				continue;
			}
	
	// these two are the same
			if (name.equals("PHONE")) {
				String num = Utils.getXmlElementString(cur, "NUMBER");
				String type = Utils.getXmlElementString(cur, "TYPE");
				if (type == null)
				{
					this.setProperty(CellularNumber, num);
					found = true;
					continue;
				}
				type = type.toUpperCase();
				if (type.equals("MOBILE") || type.equals("CELLULAR")){
					this.setProperty(CellularNumber, num);
				}
				else
				if (type.equals("HOME") || type.equals("HOME1")){
					this.setProperty(HomePhone, num);
				}
				if (type.equals("FAX") || type.equals("BUSINESSFAX")){
					this.setProperty(FaxNumber, num);
				}
				if (type.equals("BUSINESS") || type.equals("BUSINESS1")){
					this.setProperty(WorkPhone, num);
				}
				if (type.equals("PAGE")){
					this.setProperty(PagerNumber, num);
				}
				else
				{
					//TODO show some error
					continue;
				}
				
				found = true;
			}

			if (name.equals("BIRTHDAY")) {
				if (cur.getNodeValue() == null)
					continue;
				String[] tok = cur.getNodeValue().split("-");
				this.setProperty(BirthYear, tok[0]);
				this.setProperty(BirthMonth, tok[1]);
				// BDAY: 1987-09-27
				this.setProperty(BirthDay, tok[2]);
				found = true;
				continue;
			}
			
			// anniversary - not in vcard rfc??
			if (name.equals("ANNIVERSARY")) {
				if (cur.getNodeValue() == null)
					continue;
				String tok[] = cur.getNodeValue().split("-");
		
				this.setProperty(AnniversaryYear, tok[0]);
				this.setProperty(AnniversaryMonth, tok[1]);
				// BDAY:1987-09-27T08:30:00-06:00
				this.setProperty(AnniversaryDay, tok[2]);
				found = true;
				continue;
			}
			
			if (name.equals("ADDRESS")) {
				String type = Utils.getXmlElementString(cur, "TYPE");
				if ("HOME".equalsIgnoreCase(type)) {
					this.setProperty(HomeAddress, Utils.getXmlElementString(cur, "STREET"));
					this.setProperty(HomeAddress2, Utils.getXmlElementString(cur, "STREET2"));
					this.setProperty(HomeCity, Utils.getXmlElementString(cur, "LOCALITY"));
					this.setProperty(HomeState, Utils.getXmlElementString(cur, "REGION"));
					this.setProperty(HomeZipCode, Utils.getXmlElementString(cur, "POSTAL-CODE"));
					this.setProperty(HomeCountry, Utils.getXmlElementString(cur, "COUNTRY"));
				}
				else
				{
					this.setProperty(WorkAddress, Utils.getXmlElementString(cur, "STREET"));
					this.setProperty(WorkAddress2, Utils.getXmlElementString(cur, "STREET2"));
					this.setProperty(WorkCity, Utils.getXmlElementString(cur, "LOCALITY"));
					this.setProperty(WorkState, Utils.getXmlElementString(cur, "REGION"));
					this.setProperty(WorkZipCode, Utils.getXmlElementString(cur, "POSTAL-CODE"));
					this.setProperty(WorkCountry, Utils.getXmlElementString(cur, "COUNTRY"));
				}
				found = true;
			}
			if (name.equals("PICTURE")) {
				// TODO we should have a picture named /tmp/synckolab.img - this will be moved if we keep this contact
				this.setProperty(PhotoName, cur.getNodeValue());
				continue;
			}
			if (name.equals("BODY")) {
				String cnotes = cur.getNodeValue();
				// bugfix for invalid client data
				this.setProperty(Notes, cnotes.replaceAll("\\n", "\n"));
				found = true;
				continue;
			}
			if (name.equals("DEPARTMENT")) {
				this.setProperty(Department, cur.getNodeValue());
				found = true;
				continue;
			}

			if (name.equals("WEB-PAGE")) {
				this.setProperty(WebPage1, cur.getNodeValue());
				found = true;
				continue;
			}
		
			if (name.equals("BUSINESS-WEB-PAGE")) {
				this.setProperty(WebPage2, cur.getNodeValue());
				found = true;
				continue;
			}

			if (name.equals("UID")) {
				uid = cur.getNodeValue();
				continue;
			}

			if (name.equals("CUSTOM1")) {
				this.setProperty(Custom1, cur.getNodeValue());
				continue;
			}

			if (name.equals("CUSTOM2")) {
				this.setProperty(Custom2, cur.getNodeValue());
				continue;
			}

			if (name.equals("CUSTOM3")) {
				this.setProperty(Custom3, cur.getNodeValue());
				continue;
			}

			if (name.equals("CUSTOM4")) {
				this.setProperty(Custom4, cur.getNodeValue());
				continue;
			}
			
			if (name.equals("IM-ADDRESS")) {
				this.setProperty(AimScreenName, cur.getNodeValue());
				continue;
			}

			if (name.equals("ALLOW-REMOTE-CONTENT")) {
				if ("TRUE".equalsIgnoreCase(cur.getNodeValue()))
					this.setProperty(AllowRemoteContent, "true");
				else
					this.setProperty(AllowRemoteContent, "false");
				continue;
			}
	
			// end parsing
		}
		return found;
	}

	/**
	 * compares two sets of contacts
	 * @return 0 if both are the same; != 0 is the index+1 thats not equals 
	 */
	public int compareTo(ContactEntry another)
	{
		for(int i=0; i < this.fields.length; i++) {
			if (this.fields[i] == null && another.fields[i] == null)
				continue;
			if (this.fields[i] == null && another.fields[i] != null)
				return i+1;
			if (this.fields[i] != null && another.fields[i] == null)
				return i+1;
			if (!this.fields[i].equals(another.fields[i]))
				return i+1;
		}
		return 0;
	}
	
	/**
	 * @param another the other contact entry
	 * @return true if the same (see: compareTo() )
	 */
	public boolean equals(ContactEntry another) {
		return compareTo(another) == 0;
	}
	
	
}
