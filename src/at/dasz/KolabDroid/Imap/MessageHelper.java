package at.dasz.KolabDroid.Imap;

import javax.mail.Message;
import javax.mail.MessagingException;

public final class MessageHelper {

	public static String calculateHash(Message message) throws MessagingException {
		return message.getSubject() + "|" + message.getSize() + "|" + message.getHeader("Date");
	}

}
