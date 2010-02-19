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

package at.dasz.KolabDroid.Imap;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class ImapClient
{

	public static Store openServer(Session session, String hostname,
			String username, String password) throws MessagingException
	{
		Store store = session.getStore("imap");
		store.connect(hostname, username, password);
		return store;
	}

	public static Session getDefaultImapSession(int port, boolean useSsl)
	{
		java.util.Properties props = new java.util.Properties();
		if (useSsl)
		{
			props.setProperty("mail.imap.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
		}
		props.setProperty("mail.imap.socketFactory.fallback", "false");
		props.setProperty("mail.imap.socketFactory.port", Integer
				.toString(port));

		Session session = Session.getDefaultInstance(props);
		return session;
	}
}
