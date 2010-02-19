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
