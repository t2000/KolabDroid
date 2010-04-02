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

package at.dasz.KolabDroid.Sync;

import java.io.IOException;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.xml.parsers.ParserConfigurationException;

import android.database.Cursor;
import at.dasz.KolabDroid.Provider.LocalCacheProvider;

/**
 * This interface defines the necessary operations to be able to synchronize a 
 * local data store with a kolab imap folder. Common tasks are already 
 * implemented in the AbstractSyncHandler, which is the recommended base class
 * for all implementors of this interface.
 */
public interface SyncHandler
{
	/**
	 * Retrieves the current status of this data store.
	 * @return
	 */
	public StatusEntry getStatus();
	
	/**
	 * Retrieves a list of all local items.
	 * @return
	 */
	public abstract Cursor getAllLocalItemsCursor();

	/**
	 * Returns the local ID column index of the specified cursor 
	 * @param c
	 * @return
	 */
	public abstract int getIdColumnIndex(Cursor c);

	/**
	 * Returns the folder name where the items are stored in the IMAP server.
	 * @return
	 */
	public abstract String getDefaultFolderName();

	/**
	 * Returns the local cache provider for this data store.
	 * @return
	 */
	public abstract LocalCacheProvider getLocalCacheProvider();

	/**
	 * Returns true, if the specified item exists in the local data store.
	 * @param sync the SyncContext, the CacheEntry has to be set
	 * @return
	 * @throws SyncException
	 * @throws MessagingException
	 */
	public abstract boolean hasLocalItem(SyncContext sync) throws SyncException, MessagingException;

	/**
	 * Returns true if there are local changes with respect to the specified
	 * cache entry.
	 * 
	 * @param entry
	 * @return
	 * @throws MessagingException 
	 * @throws SyncException 
	 */
	public abstract boolean hasLocalChanges(SyncContext sync) throws SyncException, MessagingException;

	/**
	 * Create a local item and cache entry from the message.
	 * 
	 * @param message
	 * @return
	 * @throws MessagingException
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SyncException 
	 */
	public abstract void createLocalItemFromServer(SyncContext sync)
			throws MessagingException, ParserConfigurationException, IOException, SyncException;

	/**
	 * Upload the item specified by the cache entry to the targetFolder.
	 * 
	 * @param session
	 * @param targetFolder
	 * @param localId
	 * @return
	 * @throws MessagingException
	 * @throws ParserConfigurationException 
	 * @throws SyncException 
	 */
	public abstract void createServerItemFromLocal(Session session,
			Folder targetFolder, SyncContext sync, int localId) throws MessagingException, ParserConfigurationException, SyncException;

	/**
	 * Update the local item and cache entry with the data from the server.
	 * 
	 * @param entry
	 * @param message
	 * @return
	 * @throws MessagingException
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SyncException 
	 */
	public abstract void updateLocalItemFromServer(SyncContext sync) throws MessagingException, ParserConfigurationException, IOException, SyncException;

	/**
	 * Update the message and cache entry with the local data.
	 * 
	 * @param entry
	 * @param message
	 * @return
	 * @throws MessagingException
	 * @throws IOException 
	 * @throws SyncException 
	 * @throws ParserConfigurationException 
	 */
	public abstract void updateServerItemFromLocal(Session session,
			Folder targetFolder,SyncContext sync) throws MessagingException, IOException, SyncException, ParserConfigurationException;

	/**
	 * Delete the local item and cache entry.
	 * 
	 * @param entry
	 * @throws SyncException 
	 */
	public abstract void deleteLocalItem(SyncContext sync) throws SyncException;

	/**
	 * Delete the message and cache entry.
	 * 
	 * @param entry
	 * @param message
	 * @throws MessagingException
	 * @throws SyncException 
	 */
	public abstract void deleteServerItem(SyncContext sync)
			throws MessagingException, SyncException;
}
