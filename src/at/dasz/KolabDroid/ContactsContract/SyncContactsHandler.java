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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Flags.Flag;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import at.dasz.KolabDroid.Utils;
import at.dasz.KolabDroid.Provider.LocalCacheProvider;
import at.dasz.KolabDroid.Settings.Settings;
import at.dasz.KolabDroid.Sync.AbstractSyncHandler;
import at.dasz.KolabDroid.Sync.CacheEntry;
import at.dasz.KolabDroid.Sync.SyncContext;
import at.dasz.KolabDroid.Sync.SyncException;

public class SyncContactsHandler extends AbstractSyncHandler
{
	//private static final String[]		PEOPLE_ID_PROJECTION	= new String[] { People._ID };
	
	/*
	private static final String[]		PHONE_PROJECTION		= new String[] {
			Contacts.Phones.TYPE, Contacts.Phones.NUMBER		};
	private static final String[]		EMAIL_PROJECTION		= new String[] {
			Contacts.ContactMethods.TYPE, Contacts.ContactMethods.DATA };
	//private static final String[]		PEOPLE_NAME_PROJECTION	= new String[] { People.NAME };
	
	private static final String[]	CONTACT_NAME_PROJECTION = new String[] { ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME };
	
	
	private static final String[]		ID_PROJECTION			= new String[] { "_id" };
	private static final String			EMAIL_FILTER			= Contacts.ContactMethods.KIND
																		+ "="
																		+ Contacts.KIND_EMAIL;
*/
	
	private final String				defaultFolderName;
	private final LocalCacheProvider	cacheProvider;
	private final ContentResolver		cr;

	public SyncContactsHandler(Context context)
	{
		super(context);
		Settings s = new Settings(context);
		settings = s;
		defaultFolderName = s.getContactsFolder();
		cacheProvider = new LocalCacheProvider.ContactsCacheProvider(context);
		cr = context.getContentResolver();
		status.setTask("Contacts");
	}

	public String getDefaultFolderName()
	{
		return defaultFolderName;
	}
	
	public boolean shouldProcess()
	{
		boolean hasFolder = (defaultFolderName != null && !"".equals(defaultFolderName));
		return settings.getSyncContacts() && hasFolder;
	}

	public LocalCacheProvider getLocalCacheProvider()
	{
		return cacheProvider;
	}

	public Cursor getAllLocalItemsCursor()
	{
		//return only those which are not deleted by other programs
		//String where = ContactsContract.RawContacts.DELETED+"='0'";
		
		//return all again
		return cr.query(ContactsContract.RawContacts.CONTENT_URI,
				new String[]{ContactsContract.RawContacts._ID}, null, null, null);
	}

	public int getIdColumnIndex(Cursor c)
	{
		return c.getColumnIndex(ContactsContract.RawContacts._ID);
	}
	
	@Override
	public void createLocalItemFromServer(Session session, Folder targetFolder, SyncContext sync)
			throws MessagingException, ParserConfigurationException,
			IOException, SyncException
	{
		Log.d("sync", "Downloading item ...");
		try
		{
			InputStream xmlinput = extractXml(sync.getMessage());
			Document doc = Utils.getDocument(xmlinput);
			updateLocalItemFromServer(sync, doc);
			updateCacheEntryFromMessage(sync);
			
			if(this.settings.getMergeContactsByName())
			{
				Log.d("ConH", "Preparing upload of Contact after merge");
				sync.setLocalItem(null);
				getLocalItem(sync); //fetch updates which were just done
				
				Log.d("ConH", "Fetched data after merge for " + ((Contact)sync.getLocalItem()).getFullName());
				
				updateServerItemFromLocal(sync, doc);
				
				Log.d("ConH", "Server item updated after merge");
				
				// Create & Upload new Message
				// IMAP needs a new Message uploaded
				String xml = Utils.getXml(doc);
				Message newMessage = wrapXmlInMessage(session, sync, xml);				
				targetFolder.appendMessages(new Message[] { newMessage });
				newMessage.saveChanges();

				// Delete old message
				sync.getMessage().setFlag(Flag.DELETED, true);
				// Replace sync context with new message
				sync.setMessage(newMessage);
				
				Log.d("ConH", "IMAP Message replaced after merge");

				updateCacheEntryFromMessage(sync);
			}
			
		}
		catch (SAXException ex)
		{
			throw new SyncException(getItemText(sync),
					"Unable to extract XML Document", ex);
		}
	}

	@Override
	protected void updateLocalItemFromServer(SyncContext sync, Document xml) throws SyncException
	{
		Contact contact = (Contact) sync.getLocalItem();
		if (contact == null)
		{
			contact = new Contact();
		}
		Element root = xml.getDocumentElement();

		contact.setUid(Utils.getXmlElementString(root, "uid"));

		Element name = Utils.getXmlElement(root, "name");
		if (name != null)
		{
			//contact.setFullName(Utils.getXmlElementString(name, "full-name"));
			String fullName = Utils.getXmlElementString(name, "full-name");
			if(fullName != null)
			{
				String[] names = fullName.split(" ");
				if(names.length == 2)
				{
					contact.setGivenName(names[0]);
					contact.setFamilyName(names[1]);
				}
			}
		}
		
		contact.setBirthday(Utils.getXmlElementString(root, "birthday"));
		
		contact.getContactMethods().clear();
		NodeList nl = Utils.getXmlElements(root, "phone");
		for (int i = 0; i < nl.getLength(); i++)
		{
			ContactMethod cm = new PhoneContact();
			cm.fromXml((Element) nl.item(i));
			contact.getContactMethods().add(cm);
		}
		nl = Utils.getXmlElements(root, "email");
		for (int i = 0; i < nl.getLength(); i++)
		{
			ContactMethod cm = new EmailContact();
			cm.fromXml((Element) nl.item(i));
			contact.getContactMethods().add(cm);
		}
		sync.setCacheEntry(saveContact(contact));
	}

	@Override
	protected void updateServerItemFromLocal(SyncContext sync, Document xml) throws SyncException, MessagingException
	{
		Contact source = getLocalItem(sync);
		CacheEntry entry = sync.getCacheEntry();

		entry.setLocalHash(source.getLocalHash());
		final Date lastChanged = new Date();
		entry.setRemoteChangedDate(lastChanged);

		writeXml(xml, source, lastChanged);
	}

	private void writeXml(Document xml, Contact source, final Date lastChanged)
	{
		Element root = xml.getDocumentElement();
		
		//TODO: needs to be above contact information (Kmail bug?)
		//Kmail seems to be picky about <phone> and <email> elements they should be right after each other
		
		//remove it for now
		Utils.deleteXmlElements(root, "last-modification-date");
		//we do not need this one for now
		//if we need it, put below contact methods (otherwise kmail complains)...
		//TODO: what shall we do with this entry? :)
		Utils.deleteXmlElements(root, "preferred-address");

		/*
		Utils.setXmlElementValue(xml, root, "last-modification-date", Utils
				.toUtc(lastChanged));
		*/
		Utils.setXmlElementValue(xml, root, "uid", source.getUid());

		Element name = Utils.getOrCreateXmlElement(xml, root, "name");
		Utils.setXmlElementValue(xml, name, "full-name", source.getFullName());
		Utils.setXmlElementValue(xml, name, "given-name", source.getGivenName());
		Utils.setXmlElementValue(xml, name, "last-name", source.getFamilyName());
		
		Utils.setXmlElementValue(xml, root, "birthday", source.getBirthday());

		Utils.deleteXmlElements(root, "phone");
		Utils.deleteXmlElements(root, "email");

		for (ContactMethod cm : source.getContactMethods())
		{
			cm.toXml(xml, root, source.getFullName());
		}
	}

	@Override
	protected String writeXml(SyncContext sync)
			throws ParserConfigurationException, SyncException, MessagingException
	{
		Contact source = getLocalItem(sync);
		CacheEntry entry = sync.getCacheEntry();

		entry.setLocalHash(source.getLocalHash());
		final Date lastChanged = new Date();
		entry.setRemoteChangedDate(lastChanged);
		final String newUid = getNewUid();
		entry.setRemoteId(newUid);
		source.setUid(newUid);
		
		Document xml = Utils.newDocument("contact");
		writeXml(xml, source, lastChanged);

		return Utils.getXml(xml);
	}

	@Override
	protected String getMimeType()
	{
		return "application/x-vnd.kolab.contact";
	}

	public boolean hasLocalItem(SyncContext sync) throws SyncException, MessagingException
	{
		return getLocalItem(sync) != null;
	}

	public boolean hasLocalChanges(SyncContext sync) throws SyncException, MessagingException
	{
		CacheEntry e = sync.getCacheEntry();
		Contact contact = getLocalItem(sync);;
		String entryHash = e.getLocalHash();
		String contactHash = contact != null ? contact.getLocalHash() : "";
		return !entryHash.equals(contactHash);
	}

	@Override
	public void deleteLocalItem(int localId)
	{
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		
		//normal delete first, then with syncadapter flag		
		Uri rawUri = ContactsContract.RawContacts.CONTENT_URI;
		ops.add(ContentProviderOperation.newDelete(rawUri).
    	withSelection(ContactsContract.RawContacts._ID + "=?", new String[]{String.valueOf(localId)}).
    	build());
		
		//remove contact from raw_contact table (this time with syncadapter flag set)		
		rawUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
		ops.add(ContentProviderOperation.newDelete(rawUri).
    	withSelection(ContactsContract.RawContacts._ID + "=?", new String[]{String.valueOf(localId)}).
    	build());		
		
		try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
        	Log.e("EE", e.toString());
        }
		
	}
	
	private void deleteLocalItemFinally(int localId)
	{
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		
		//remove contact from raw_contact table (with syncadapter flag set)		
		Uri rawUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
		ops.add(ContentProviderOperation.newDelete(rawUri).
    	withSelection(ContactsContract.RawContacts._ID + "=?", new String[]{String.valueOf(localId)}).
    	build());		
		
		try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
        	Log.e("EE", e.toString());
        }
		
	}
	
	@Override
	public void deleteServerItem(SyncContext sync) throws MessagingException, SyncException
	{
		Log.d("sync", "Deleting from server: " + sync.getMessage().getSubject());
		sync.getMessage().setFlag(Flag.DELETED, true);
		// remove contents too, to avoid confusing the butchered JAF
		// message.setContent("", "text/plain");
		// message.saveChanges();
		getLocalCacheProvider().deleteEntry(sync.getCacheEntry());
		
		//make sure it gets flushed from the raw_contacts table on the phone as well
		deleteLocalItemFinally(sync.getCacheEntry().getLocalId());
	}

	private CacheEntry saveContact(Contact contact) throws SyncException
	{
		Uri uri = null;
		
		String name = contact.getFullName();
		String firstName = contact.getGivenName();
		String lastName = contact.getFamilyName();
		
		String email = "";
		String phone = "";
		
		Log.d("ConH", "Saving Contact: \"" + name + "\"");
		
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		
		boolean doMerge = false;
		
		if (contact.getId() == 0 && this.settings.getMergeContactsByName())
		{
			//find raw_contact by name
			String w = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME +"='"+name+"'";
			
			//Cursor c = cr.query(ContactsContract.RawContacts.CONTENT_URI, null, w, null, null);
			Cursor c = cr.query(ContactsContract.Data.CONTENT_URI, null, w, null, null);
			
			if(c == null)
			{
				Log.d("ConH", "SC: faild to query for merge with contact: " + name);
			}
			
			if(c.getCount()>0)
			{
				c.moveToFirst();
				//int nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
				//String c.getString(nameIdx);
				//int rawIdIdx = c.getColumnIndex(ContactsContract.RawContacts._ID);
				int rawIdIdx = c.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID);
				int rawID = c.getInt(rawIdIdx);
				contact.setId(rawID);
				doMerge = true;
				
				Log.d("ConH", "SC: Found Entry ID: " + rawID + " for contact: " + name + " -> will merge now");
			}
			
			if(c != null) c.close();
		}
		
		
		if (contact.getId() == 0)
		{
			Log.d("ConH", "SC: Contact " + name + " is NEW -> insert");
			
			String accountName = settings.getAccountName();
			if("".equals(accountName)) accountName = null;
			String accountType = settings.getAccountType();
			if("".equals(accountType)) accountType = null;
			
			ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
	                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
	                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
	                .build());
	        
			ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
	                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
	                .withValue(ContactsContract.Data.MIMETYPE,
	                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
	                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
	                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
	                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
	                .build());
			
			ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
	                .withValue(ContactsContract.Data.MIMETYPE,
	                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)	                        
	                .withValue(ContactsContract.CommonDataKinds.Event.START_DATE, contact.getBirthday())
	                .withValue(ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
	                .build());

			for (ContactMethod cm : contact.getContactMethods())
			{
				if(cm instanceof EmailContact)
				{
					email = cm.getData();
					
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			                .withValue(ContactsContract.Data.MIMETYPE,
			                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
			                .withValue(ContactsContract.CommonDataKinds.Email.DATA, email).
			                withValue(ContactsContract.CommonDataKinds.Email.TYPE, cm.getType()).build());
				}
				
				if(cm instanceof PhoneContact)
				{
					phone = cm.getData();
					
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			                .withValue(ContactsContract.Data.MIMETYPE,
			                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
			                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
			                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, cm.getType())
			                .build());
				}
			}
		}
		else
		{
			Log.d("ConH", "SC. Contact " +name+" already in Android book, MergeFlag: " + doMerge);
			
			Uri updateUri = ContactsContract.Data.CONTENT_URI;
			
			List<ContactMethod> cms = null;
			List<ContactMethod> mergedCms = new ArrayList<ContactMethod>();
			
			//first remove stuff that is in addressbook
			Cursor phoneCursor = null, emailCursor = null, birthdayCursor = null;
			
			//update name (broken at the moment :()
			/*
			ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
	                .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.getId())
	                .withValue(ContactsContract.Data.MIMETYPE,
	                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
	                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
	                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
	                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
	                .build());
			*/
			
			//birthday
			{
				String w = ContactsContract.Data.RAW_CONTACT_ID+"='"+contact.getId()+"' AND " +
				ContactsContract.Contacts.Data.MIMETYPE + " = '"+ ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE+"'";
				
				//Log.i("II", "w: " + w);
				
				birthdayCursor = cr.query(updateUri, null, w, null, null);
				
				if (birthdayCursor == null) throw new SyncException(
						"EE", "cr.query returned null");
				
				if(birthdayCursor.getCount() > 0) // otherwise no events
				{				
					if (!birthdayCursor.moveToFirst()) return null;
					int idCol = birthdayCursor.getColumnIndex(ContactsContract.Data._ID);
					//int dateCol = birthdayCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
					int typeCol = birthdayCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE);					
					
					if(birthdayCursor.getInt(typeCol) == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
					{
						ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI).
							withSelection(ContactsContract.Data._ID + "=?", new String[]{String.valueOf(birthdayCursor.getInt(idCol))}).
							build());
					 }
				}
				
				if(! "".equals(contact.getBirthday()))
				{				
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
		                .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.getId())
		                .withValue(ContactsContract.Data.MIMETYPE,
		                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
		                .withValue(ContactsContract.CommonDataKinds.Event.START_DATE, contact.getBirthday())
		                .withValue(ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
		                .build());
					
					Log.d("ConH", "Writing birthday: " + contact.getBirthday() + " for contact " + name);
				}
			}
			
			//phone
			{
				String w = ContactsContract.Data.RAW_CONTACT_ID+"='"+contact.getId()+"' AND " +
				ContactsContract.Contacts.Data.MIMETYPE + " = '"+ ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE+"'";
				
				//Log.i("II", "w: " + w);
				
				phoneCursor = cr.query(updateUri, null, w, null, null);
				
				if (phoneCursor == null) throw new SyncException(
						"EE", "cr.query returned null");
				
				if(phoneCursor.getCount() > 0) // otherwise no phone numbers
				{				
					if (!phoneCursor.moveToFirst()) return null;
					int idCol = phoneCursor.getColumnIndex(ContactsContract.Data._ID);
					int numberCol = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					int typeCol = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);					
					
					if(!doMerge)
					{
						do
						{
							ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI).
								withSelection(ContactsContract.Data._ID + "=?", new String[]{String.valueOf(phoneCursor.getInt(idCol))}).
								build());
						 }while (phoneCursor.moveToNext());
					}
					else
					{
						for(ContactMethod cm : contact.getContactMethods())
						{							
							if(! (cm instanceof PhoneContact)) continue;
							
							boolean found = false;							
							String newNumber = cm.getData();
							int newType = cm.getType();
							
							do {
								String numberIn = phoneCursor.getString(numberCol);
								int typeIn = phoneCursor.getInt(typeCol);
								
								if(typeIn == newType && numberIn.equals(newNumber))
								{
									Log.d("ConH", "SC: Found phone: " + numberIn + " for contact " + name + " -> wont add");
									found = true;
									break;
								}
								
							}while(phoneCursor.moveToNext());
							
							if(!found)
							{
								mergedCms.add(cm);
							}
						}
					}
				}
				else
				{
					if(doMerge)
					{
						Log.d("ConH", "SC: No numbers in android for contact " + name + " -> adding all");
						//we can add all new Numbers
						for(ContactMethod cm : contact.getContactMethods())
						{							
							if(! (cm instanceof PhoneContact)) continue;							
							mergedCms.add(cm);
						}
					}
				}
			}
			
			//mail
			{
				String w = ContactsContract.Data.RAW_CONTACT_ID+"='"+contact.getId()+"' AND " +
				ContactsContract.Contacts.Data.MIMETYPE + " = '"+ ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE+"'";
				
				emailCursor = cr.query(updateUri, null, w, null, null);
				
				if (emailCursor == null) throw new SyncException(
						"EE", "cr.query returned null");
				
				if(emailCursor.getCount() > 0) // otherwise no email addresses
				{
					if (!emailCursor.moveToFirst()) return null;								
					int idCol = emailCursor.getColumnIndex(ContactsContract.Data._ID);
					int mailCol = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
					int typeCol = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);
					
					if(!doMerge)
					{
						do
						{
							ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI).
									withSelection(ContactsContract.Data._ID + "=?", new String[]{String.valueOf(emailCursor.getInt(idCol))}).
									build());
						}while (emailCursor.moveToNext());
					}
					else
					{
						for(ContactMethod cm : contact.getContactMethods())
						{							
							if(! (cm instanceof EmailContact)) continue;
							
							boolean found = false;							
							String newMail = cm.getData();
							int newType = cm.getType();
							
							do {
								String emailIn = emailCursor.getString(mailCol);
								int typeIn = emailCursor.getInt(typeCol);
								
								if(typeIn == newType && emailIn.equals(newMail))
								{
									Log.d("ConH", "SC. Found email: " + emailIn + " for contact " + name + " -> wont add");
									found = true;
									break;
								}
								
							}while(emailCursor.moveToNext());
							
							if(!found)
							{
								mergedCms.add(cm);
							}
						}
					}					
				}
				else
				{
					if(doMerge)
					{
						Log.d("ConH", "SC: No email in android for contact " + name + " -> adding all");
						//we can add all new Numbers
						for(ContactMethod cm : contact.getContactMethods())
						{							
							if(! (cm instanceof EmailContact)) continue;							
							mergedCms.add(cm);
						}
					}
				}
			}
			
			//insert again
			if(doMerge)
			{
				cms = mergedCms;
			}
			else
			{
				cms = contact.getContactMethods();
			}
			
			//for (ContactMethod cm : contact.getContactMethods())
			for (ContactMethod cm : cms)
			{
				if(cm instanceof EmailContact)
				{
					email = cm.getData();
					Log.d("ConH", "SC: Writing mail: " + email + " for contact " + name);
					
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			                .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.getId())
			                .withValue(ContactsContract.Data.MIMETYPE,
			                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
			                .withValue(ContactsContract.CommonDataKinds.Email.DATA, email).
			                withValue(ContactsContract.CommonDataKinds.Email.TYPE, cm.getType()).build());				
				}
				
				if(cm instanceof PhoneContact)
				{
					phone = cm.getData();
					Log.d("ConH", "Writing phone: " + phone + " for contact " + name);
					
					ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			                .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.getId())
			                .withValue(ContactsContract.Data.MIMETYPE,
			                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
			                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
			                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, cm.getType())
			                .build());
				}
			}
			
			if (phoneCursor != null) phoneCursor.close();
			if (emailCursor != null) emailCursor.close();
		}
		
		//Log.i("II", "Creating contact: " + firstName + " " + lastName);
        try {
            ContentProviderResult[] results = cr.applyBatch(ContactsContract.AUTHORITY, ops);
            //store the first result: it contains the uri of the raw contact with its ID
            if(contact.getId() == 0)
            {
            	uri = results[0].uri;
            }
            else
            {
            	uri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, contact.getId());
            }
            Log.d("ConH", "SC: Affected Uri was: " + uri);
            
        } catch (Exception e) {	
            // Log exception
            Log.e("EE","Exception encountered while inserting contact: " + e.getMessage() + e.getStackTrace());
        }

		CacheEntry result = new CacheEntry();
		result.setLocalId((int) ContentUris.parseId(uri));
		result.setLocalHash(contact.getLocalHash());
		result.setRemoteId(contact.getUid());		
		return result;
	}

	private Contact getLocalItem(SyncContext sync) throws SyncException,
			MessagingException
	{
		if (sync.getLocalItem() != null) return (Contact) sync.getLocalItem();

		Uri uri = ContactsContract.Data.CONTENT_URI;

		Cursor personCursor = null, phoneCursor = null, emailCursor = null, birthdayCursor = null;
		try
		{			
			String where = ContactsContract.Data.RAW_CONTACT_ID+"='"+sync.getCacheEntry().getLocalId()+"' AND " +
			ContactsContract.Contacts.Data.MIMETYPE + " = '"+ ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE+"'";
			
			//Log.i("II", "where: " + where);
			
			personCursor = cr.query(uri, null, where, null, null);
			
			if (personCursor == null) throw new SyncException(
					getItemText(sync), "cr.query returned null");
			if (!personCursor.moveToFirst()) return null;
			
			//int idx = personCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
			int idxFirst = personCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
			int idxLast = personCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
			
			String firstName = personCursor.getString(idxFirst);
			String lastName = personCursor.getString(idxLast);
			
			//String name = firstName + " " + lastName;
			
			//Log.i("II", "Cursor Line" +name);
						
			Contact result = new Contact();
			result.setId(sync.getCacheEntry().getLocalId());
			result.setUid(sync.getCacheEntry().getRemoteId());
					
			//result.setFullName(personCursor.getString(nameIdx));
			result.setGivenName(firstName);
			result.setFamilyName(lastName);

			//phone
			{
				String w = ContactsContract.Data.RAW_CONTACT_ID+"='"+sync.getCacheEntry().getLocalId()+"' AND " +
				ContactsContract.Contacts.Data.MIMETYPE + " = '"+ ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE+"'";
				
				phoneCursor = cr.query(uri, null, w, null, null);
				
				if (phoneCursor == null) throw new SyncException(
						getItemText(sync), "cr.query returned null");
				
				if(phoneCursor.getCount() > 0) // otherwise no phone numbers
				{
				
					if (!phoneCursor.moveToFirst()) return null;
					
					int numberIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					int typeIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);			
					
					do
					{
						PhoneContact pc = new PhoneContact();
						pc.setData(phoneCursor.getString(numberIdx));
						pc.setType(phoneCursor.getInt(typeIdx));
						result.getContactMethods().add(pc);
					 }while (phoneCursor.moveToNext());
				}
			}
			
			//mail
			{
				String w = ContactsContract.Data.RAW_CONTACT_ID+"='"+sync.getCacheEntry().getLocalId()+"' AND " +
				ContactsContract.Contacts.Data.MIMETYPE + " = '"+ ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE+"'";
				
				emailCursor = cr.query(uri, null, w, null, null);
				
				if (emailCursor == null) throw new SyncException(
						getItemText(sync), "cr.query returned null");
				
				if(emailCursor.getCount() > 0) // otherwise no email addresses
				{
					if (!emailCursor.moveToFirst()) return null;
					
					int dataIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
					//int typeIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);			
					
					do
					{
						EmailContact pc = new EmailContact();
						pc.setData(emailCursor.getString(dataIdx));
						//pc.setType(emailCursor.getInt(typeIdx));
						result.getContactMethods().add(pc);
					}while (emailCursor.moveToNext());
				}
			}
			
			//birthday
			{
				String w = ContactsContract.Data.RAW_CONTACT_ID+"='"+sync.getCacheEntry().getLocalId()+"' AND " +
				ContactsContract.Contacts.Data.MIMETYPE + " = '"+ ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE +"'";
				
				birthdayCursor = cr.query(uri, null, w, null, null);
				
				if (birthdayCursor == null) throw new SyncException(
						getItemText(sync), "cr.query returned null");
				
				if(birthdayCursor.getCount() > 0) // otherwise no birthday
				{
					if (!birthdayCursor.moveToFirst()) return null;
					
					int dateIdx = birthdayCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
					int typeIdx = birthdayCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE);
					//int typeIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);			
					
					//do
					//{
						int typeIn = birthdayCursor.getInt(typeIdx);
						if(typeIn == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
						{
							String bday = birthdayCursor.getString(dateIdx);
							result.setBirthday(bday);
						}
					//}while (birthdayCursor.moveToNext());
				}
			}

			sync.setLocalItem(result);
			return result;
		}
		finally
		{
			if (personCursor != null) personCursor.close();
			if (phoneCursor != null) phoneCursor.close();
			if (emailCursor != null) emailCursor.close();
		}
	}

	private String getNewUid()
	{
		// Create Application and Type specific id
		// kd == Kolab Droid, ct = contact
		return "kd-ct-" + UUID.randomUUID().toString();
	}

	@Override
	protected String getMessageBodyText(SyncContext sync) throws SyncException, MessagingException
	{
		Contact contact = getLocalItem(sync);
		StringBuilder sb = new StringBuilder();

		String fullName =contact.getFullName(); 
		sb.append(fullName == null ? "(no name)" : fullName);
		sb.append("\n");
		sb.append("----- Contact Methods -----\n");
		for (ContactMethod cm : contact.getContactMethods())
		{
			sb.append(cm.getData());
			sb.append("\n");
		}

		return sb.toString();
	}

	@Override
	public String getItemText(SyncContext sync) throws MessagingException
	{
		if (sync.getLocalItem() != null)
		{
			Contact item = (Contact) sync.getLocalItem();
			return item.getFullName();
		}
		else
		{
			return sync.getMessage().getSubject();
		}
	}
}
