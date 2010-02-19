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

package at.dasz.KolabDroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class NotificationDialog {
	public static void show(Context ctx, int msg) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    	builder.setMessage(msg);
    	builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
    	
    	AlertDialog alert = builder.create();
    	alert.show();		
	}
	
    public static final DialogInterface.OnClickListener closeDlg = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();
		}
	};
	
	public static void showYesNo(Context ctx, int msg, DialogInterface.OnClickListener yes, DialogInterface.OnClickListener no) {

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    	builder.setMessage(msg);
    	
    	builder.setPositiveButton(R.string.yes, yes);
    	builder.setNegativeButton(R.string.no, no);
    	
    	AlertDialog alert = builder.create();
    	alert.show();	
	}
}
