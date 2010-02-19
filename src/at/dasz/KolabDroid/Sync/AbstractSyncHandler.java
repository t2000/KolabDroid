package at.dasz.KolabDroid.Sync;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MultipartDataSource;
import javax.mail.Session;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import at.dasz.KolabDroid.Utils;

public abstract class AbstractSyncHandler implements SyncHandler
{
	protected AbstractSyncHandler(Context context)
	{
		this.context = context;
		status = new StatusEntry();
		Time t = new Time();
		t.setToNow();
		status.setTime(t);
	}

	protected StatusEntry	status;
	protected Context 		context;
	
	protected abstract String getMimeType();
	
	protected abstract String writeXml(SyncContext sync) throws ParserConfigurationException;
	protected abstract String getMessageBodyText(SyncContext sync);	

	protected abstract void updateLocalItemFromServer(SyncContext sync, Document xml);
	protected abstract void updateServerItemFromLocal(SyncContext sync, Document xml);
	
	public abstract void deleteLocalItem(int localId);

	public StatusEntry getStatus()
	{
		return status;
	}
	
	public void createLocalItemFromServer(SyncContext sync)
			throws MessagingException, ParserConfigurationException, SAXException, IOException
	{
		Log.d("sync", "Downloading item ...");
		InputStream xmlinput = extractXml(sync.getMessage());
		Document doc = Utils.getDocument(xmlinput);
		updateLocalItemFromServer(sync, doc);
		updateCacheEntryFromMessage(sync);
	}

	public void updateLocalItemFromServer(SyncContext sync) throws MessagingException, ParserConfigurationException, SAXException, IOException
	{
		if (hasLocalItem(sync))
		{
			Log.d("sync", "Updating without conflict check: "
					+ sync.getCacheEntry().getLocalId());
			InputStream xmlinput = extractXml(sync.getMessage());
			Document doc = Utils.getDocument(xmlinput);
			updateLocalItemFromServer(sync, doc);
			updateCacheEntryFromMessage(sync);
		}
	}

	private void updateCacheEntryFromMessage(SyncContext sync) throws MessagingException
	{
		CacheEntry c = sync.getCacheEntry();
		Message m = sync.getMessage();
		Date dt = m.getReceivedDate();
		if(dt == null) dt = m.getSentDate();
		c.setRemoteChangedDate(dt); 
		c.setRemoteId(m.getSubject());
		c.setRemoteSize(m.getSize());
		getLocalCacheProvider().saveEntry(c);
	}

	public void createServerItemFromLocal(Session session,
			Folder targetFolder, SyncContext sync, int localId) throws MessagingException, ParserConfigurationException
	{
		Log.d("sync", "Uploading: #" + localId);

		// initialize cache entry with values that should go
		// into the new server item
		CacheEntry entry = new CacheEntry();
		sync.setCacheEntry(entry);
		entry.setLocalId(localId);

		String xml = writeXml(sync);
		Message m = wrapXmlInMessage(session, sync, xml);
		targetFolder.appendMessages(new Message[] { m });
		m.saveChanges();
		sync.setMessage(m);
		updateCacheEntryFromMessage(sync);
	}
	

	public void updateServerItemFromLocal(Session session,
			Folder targetFolder, SyncContext sync)
			throws MessagingException, IOException, SAXException
	{
		Log.d("sync", "Update item on Server: #" + sync.getCacheEntry().getLocalId());
		
		InputStream xmlinput = extractXml(sync.getMessage());
		try
		{
			// Parse XML 
			Document doc = Utils.getDocument(xmlinput);
			
			// Update
			updateServerItemFromLocal(sync, doc);

			// Create & Upload new Message  
			// IMAP needs a new Message uploaded
			String xml = Utils.getXml(doc);
			Message newMessage = wrapXmlInMessage(session, sync, xml);
			targetFolder.appendMessages(new Message[] { newMessage });
			newMessage.saveChanges();
			
			sync.getMessage().setFlag(Flag.DELETED, true);
			sync.setMessage(newMessage);
			
			updateCacheEntryFromMessage(sync);			
		}
		catch (ParserConfigurationException ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			if (xmlinput != null) xmlinput.close();
		}
	}

	
	public void deleteLocalItem(SyncContext sync)
	{
		Log.d("sync", "Deleting locally: " + sync.getCacheEntry().getLocalHash());
		deleteLocalItem(sync.getCacheEntry().getLocalId());
		getLocalCacheProvider().deleteEntry(sync.getCacheEntry());
	}

	public void deleteServerItem(SyncContext sync)
			throws MessagingException
	{
		Log.d("sync", "Deleting from server: " + sync.getMessage().getSubject());
		sync.getMessage().setFlag(Flag.DELETED, true);
		// remove contents too, to avoid confusing the butchered JAF
		// message.setContent("", "text/plain");
		// message.saveChanges();
		getLocalCacheProvider().deleteEntry(sync.getCacheEntry());
	}

	private InputStream extractXml(Message message)
	{
		try
		{
			DataSource mainDataSource = message.getDataHandler()
					.getDataSource();
			if (!(mainDataSource instanceof MultipartDataSource)) { return null; }

			MultipartDataSource multipart = (MultipartDataSource) mainDataSource;
			for (int idx = 0; idx < multipart.getCount(); idx++)
			{
				BodyPart p = multipart.getBodyPart(idx);

				if (p.isMimeType(getMimeType())) { return p.getInputStream(); }
			}
		}
		catch (MessagingException ex)
		{
			ex.printStackTrace();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	

	private Message wrapXmlInMessage(Session session, 
			SyncContext sync, String xml) throws MessagingException
	{
		Message result = new MimeMessage(session);
		result.setSubject(sync.getCacheEntry().getRemoteId());
		result.setSentDate(sync.getCacheEntry().getRemoteChangedDate()); // TODO:
		MimeMultipart mp = new MimeMultipart();
		MimeBodyPart txt = new MimeBodyPart();
		txt.setText(getMessageBodyText(sync), "utf8");
		mp.addBodyPart(txt);

		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source;
		try
		{
			source = new ByteArrayDataSource(xml.getBytes("UTF-8"),
					getMimeType());
		}
		catch (UnsupportedEncodingException ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName("kolab.xml");
		mp.addBodyPart(messageBodyPart);

		result.setContent(mp);

		// avoid later change in timestamp when the SEEN flag would be updated
		result.setFlag(Flags.Flag.SEEN, true);

		return result;
	}
}
