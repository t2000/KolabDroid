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

import java.util.Date;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;
import at.dasz.KolabDroid.Utils;
import at.dasz.KolabDroid.Provider.LocalCacheProvider;
import at.dasz.KolabDroid.Settings.Settings;
import at.dasz.KolabDroid.Sync.AbstractSyncHandler;
import at.dasz.KolabDroid.Sync.CacheEntry;
import at.dasz.KolabDroid.Sync.SyncContext;
import at.dasz.KolabDroid.Sync.SyncException;

public class SyncContactsHandler extends AbstractSyncHandler
{
	private static final String[]		PHONE_PROJECTION		= new String[] {
			Contacts.Phones.TYPE, Contacts.Phones.NUMBER		};
	private static final String[]		EMAIL_PROJECTION		= new String[] {
			Contacts.ContactMethods.TYPE, Contacts.ContactMethods.DATA };
	private static final String[]		PEOPLE_NAME_PROJECTION	= new String[] { People.NAME };
	private static final String[]		PEOPLE_ID_PROJECTION	= new String[] { People._ID };
	private static final String[]		ID_PROJECTION			= new String[] { "_id" };
	private static final String			EMAIL_FILTER			= Contacts.ContactMethods.KIND
																		+ "="
																		+ Contacts.KIND_EMAIL;

	private final String				defaultFolderName;
	private final LocalCacheProvider	cacheProvider;
	private final ContentResolver		cr;

	public SyncContactsHandler(Context context)
	{
		super(context);
		Settings s = new Settings(context);
		defaultFolderName = s.getContactsFolder();
		cacheProvider = new LocalCacheProvider.ContactsCacheProvider(context);
		cr = context.getContentResolver();
		status.setTask("Contacts");
	}

	public String getDefaultFolderName()
	{
		return defaultFolderName;
	}

	public LocalCacheProvider getLocalCacheProvider()
	{
		return cacheProvider;
	}

	public Cursor getAllLocalItemsCursor()
	{
		return cr.query(People.CONTENT_URI, PEOPLE_ID_PROJECTION, null, null,
				null);
	}

	public int getIdColumnIndex(Cursor c)
	{
		return c.getColumnIndex(People._ID);
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

		Element name = Utils.getXmlElement(root, "name");
		if (name != null)
		{
			contact.setFullName(Utils.getXmlElementString(name, "full-name"));
		}
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

		Utils.setXmlElementValue(xml, root, "last-modification-date", Utils
				.toUtc(lastChanged));

		Element name = Utils.getOrCreateXmlElement(xml, root, "name");
		Utils.setXmlElementValue(xml, name, "full-name", source.getFullName());

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
		Uri uri = ContentUris.withAppendedId(People.CONTENT_URI, localId);
		cr.delete(uri, null, null);
	}

	private CacheEntry saveContact(Contact contact) throws SyncException
	{
		Uri uri;
		if (contact.getId() == 0)
		{
			uri = Contacts.People.createPersonInMyContactsGroup(cr, contact
					.toContentValues());
		}
		else
		{
			uri = ContentUris.withAppendedId(People.CONTENT_URI, contact
					.getId());
			cr.update(uri, contact.toContentValues(), null, null);

			Cursor phoneCursor = null, emailCursor = null;
			try
			{
				Uri phoneDirectoryUri = Uri.withAppendedPath(uri,
						Contacts.People.Phones.CONTENT_DIRECTORY);
				phoneCursor = cr.query(phoneDirectoryUri, ID_PROJECTION, null,
						null, null);
				if (phoneCursor == null) throw new SyncException(contact.toString(), "cr.query returned null");
				while (phoneCursor.moveToNext())
				{
					Uri delUri = ContentUris.withAppendedId(phoneDirectoryUri,
							phoneCursor.getInt(0));
					cr.delete(delUri, null, null);
				}

				Uri emailDirectoryUri = Uri.withAppendedPath(uri,
						Contacts.People.ContactMethods.CONTENT_DIRECTORY);
				emailCursor = cr.query(emailDirectoryUri, ID_PROJECTION,
						EMAIL_FILTER, null, null);
				if (emailCursor == null) throw new SyncException(contact.toString(), "cr.query returned null");
				while (emailCursor.moveToNext())
				{
					Uri delUri = ContentUris.withAppendedId(emailDirectoryUri,
							emailCursor.getInt(0));
					cr.delete(delUri, null, null);
				}
			}
			finally
			{
				if (phoneCursor != null) phoneCursor.close();
				if (emailCursor != null) emailCursor.close();
			}

		}
		for (ContactMethod method : contact.getContactMethods())
		{
			Uri methodUri = Uri.withAppendedPath(uri, method
					.getContentDirectory());
			cr.insert(methodUri, method.toContentValues());
		}
		CacheEntry result = new CacheEntry();
		result.setLocalId((int) ContentUris.parseId(uri));
		result.setLocalHash(contact.getLocalHash());
		return result;
	}

	private Contact getLocalItem(SyncContext sync) throws SyncException,
			MessagingException
	{
		if (sync.getLocalItem() != null) return (Contact) sync.getLocalItem();

		Uri uri = ContentUris.withAppendedId(People.CONTENT_URI, sync
				.getCacheEntry().getLocalId());
		Cursor personCursor = null, phoneCursor = null, emailCursor = null;
		try
		{
			personCursor = cr.query(uri, PEOPLE_NAME_PROJECTION, null, null,
					null);
			if (personCursor == null) throw new SyncException(
					getItemText(sync), "cr.query returned null");
			if (!personCursor.moveToFirst()) return null;
			Contact result = new Contact();
			result.setId(sync.getCacheEntry().getLocalId());
			final int nameIdx = personCursor.getColumnIndex(People.NAME);
			result.setFullName(personCursor.getString(nameIdx));

			{
				Uri phoneDirectoryUri = Uri.withAppendedPath(uri,
						Contacts.People.Phones.CONTENT_DIRECTORY);
				phoneCursor = cr.query(phoneDirectoryUri, PHONE_PROJECTION,
						null, null, Contacts.Phones.NUMBER);
				if (phoneCursor == null) throw new SyncException(
						getItemText(sync), "cr.query returned null");

				final int typeIdx = phoneCursor
						.getColumnIndex(Contacts.Phones.TYPE);
				final int numberIdx = phoneCursor
						.getColumnIndex(Contacts.Phones.NUMBER);
				while (phoneCursor.moveToNext())
				{
					PhoneContact pc = new PhoneContact();
					pc.setData(phoneCursor.getString(numberIdx));
					pc.setType(phoneCursor.getInt(typeIdx));
					result.getContactMethods().add(pc);
				}
			}
			{
				Uri emailDirectoryUri = Uri.withAppendedPath(uri,
						Contacts.People.ContactMethods.CONTENT_DIRECTORY);
				emailCursor = cr.query(emailDirectoryUri, EMAIL_PROJECTION,
						EMAIL_FILTER, null, Contacts.ContactMethods.DATA);
				if (emailCursor == null) throw new SyncException(
						getItemText(sync), "cr.query returned null");
				final int typeIdx = emailCursor
						.getColumnIndex(Contacts.ContactMethods.TYPE);
				final int dataIdx = emailCursor
						.getColumnIndex(Contacts.ContactMethods.DATA);
				while (emailCursor.moveToNext())
				{
					EmailContact pc = new EmailContact();
					pc.setData(emailCursor.getString(dataIdx));
					pc.setType(emailCursor.getInt(typeIdx));
					result.getContactMethods().add(pc);
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
		// kd == Kolab Droid
		return "kd-ct-" + UUID.randomUUID().toString();
	}

	@Override
	protected String getMessageBodyText(SyncContext sync) throws SyncException, MessagingException
	{
		Contact contact = getLocalItem(sync);
		StringBuilder sb = new StringBuilder();

		sb.append(contact.getFullName());
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
