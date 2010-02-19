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
