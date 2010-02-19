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
