package at.dasz.KolabDroid.Imap;

import java.util.Locale;

import javax.activation.DataContentHandler;
import javax.activation.DataContentHandlerFactory;

import com.sun.mail.handlers.multipart_mixed;
import com.sun.mail.handlers.text_html;
import com.sun.mail.handlers.text_plain;

public class DchFactory implements DataContentHandlerFactory
{
	public DataContentHandler createDataContentHandler(String mimeType)
	{
		mimeType = mimeType.toLowerCase(Locale.ENGLISH);
		if ("text/plain".equals(mimeType))
		{
			return new text_plain();
		}
		else if ("multipart/mixed".equals(mimeType))
		{
			return new multipart_mixed();
		}
		else if ("text/html".equals(mimeType))
		{
			return new text_html();
		}
		else
		{
			return null;
		}
	}
}
