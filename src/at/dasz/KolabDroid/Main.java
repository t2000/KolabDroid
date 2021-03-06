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

import javax.activation.DataHandler;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import at.dasz.KolabDroid.Imap.DchFactory;
import at.dasz.KolabDroid.Provider.StatusProvider;
import at.dasz.KolabDroid.Settings.Settings;
import at.dasz.KolabDroid.Sync.BaseWorker;
import at.dasz.KolabDroid.Sync.ResetService;
import at.dasz.KolabDroid.Sync.ResetSoftService;
import at.dasz.KolabDroid.Sync.SyncService;
import at.dasz.KolabDroid.Sync.SyncServiceManager;

public class Main extends Activity implements MainActivity {
	
	private StatusListAdapter statusAdapter = null;
	private TextView status = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if (!DataHandler.hasDataContentHandlerFactory())
		{
			DataHandler.setDataContentHandlerFactory(new DchFactory());
		}
		
		setContentView(R.layout.main);

		Button btnSync = (Button) findViewById(R.id.sync);
		btnSync.setOnClickListener(btnSyncListener);
		
		Button btnSyncCal = (Button) findViewById(R.id.BtnSyncCalendar);
		btnSyncCal.setOnClickListener(btnSyncCalListener);
		
		status = (TextView) findViewById(R.id.status);
		StatusHandler.load(this);
		StatusHandler.writeStatus("");
		
		statusAdapter = new StatusListAdapter(this);
		
		ExpandableListView statusListView = (ExpandableListView)findViewById(R.id.statusList);
		statusListView.setAdapter(statusAdapter);
		
		bindStatus();
		
		// Ensure that the Alarm Manager is active
		SyncServiceManager.initAlarmManager(this);
	}
	
    @Override
    protected void onResume()
    {
    	status = (TextView) findViewById(R.id.status);
		StatusHandler.load(this);
    	super.onResume();
    }
    
    @Override
    protected void onPause()
    {
    	StatusHandler.unload();
    	super.onPause();
    }
    
    private final static int MENU_SETTINGS = 1;
    private final static int MENU_RESET = 2;
    private final static int MENU_REFRESH = 3;
    private final static int MENU_STOP_SYNC = 5;
    private final static int MENU_CLEAR_LOG = 6;
    private final static int MENU_RESET_SOFT = 7;
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SETTINGS, 0, R.string.settings);
        menu.add(0, MENU_REFRESH, 0, R.string.refreshstatus);
        menu.add(0, MENU_STOP_SYNC, 0, R.string.stopsync);
        menu.add(0, MENU_CLEAR_LOG, 0, R.string.clearlog);
        menu.add(0, MENU_RESET, 0, R.string.reset);
        menu.add(0, MENU_RESET_SOFT, 0, R.string.resetSoft);
        return true;
    }

    /* Handles item selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_SETTINGS:
    		showSettings();
            return true;
        case MENU_RESET:
    		resetData();
    		bindStatus();
            return true;
        case MENU_RESET_SOFT:
    		resetDataSoft();
    		bindStatus();
            return true;
        case MENU_REFRESH:
        	bindStatus();
            return true;
        case MENU_CLEAR_LOG:
        	StatusProvider statProvider = new StatusProvider(Main.this);
        	statProvider.clearAllEntries();
        	bindStatus();
            return true;
        case MENU_STOP_SYNC:
			Intent s = new Intent(this, SyncService.class);
		    this.stopService(s);		
		    bindStatus();
            return true;
        }
        return false;
    }

	private void resetData()
	{
		if(BaseWorker.isRunning()) {
			NotificationDialog.show(this, BaseWorker.getRunningMessageResID());
		} else {
			NotificationDialog.showYesNo(this, R.string.really_reset, 
					dlgResetListener, NotificationDialog.closeDlg);
		}
	}
	
	private void resetDataSoft()
	{
		if(BaseWorker.isRunning()) {
			NotificationDialog.show(this, BaseWorker.getRunningMessageResID());
		} else {
			NotificationDialog.showYesNo(this, R.string.really_reset_soft, 
					dlgResetSoftListener, NotificationDialog.closeDlg);
		}
	}

	private void showSettings()
	{
		startActivity(new Intent(at.dasz.KolabDroid.Settings.SettingsView.EDIT_SETTINGS_ACTION));
	}
    
    private final OnClickListener btnSyncListener = new OnClickListener() {
    	public void onClick(View v) {
    		if(BaseWorker.isRunning()) {
    			NotificationDialog.show(v.getContext(), BaseWorker.getRunningMessageResID());
    		} else {
    			Settings s = new Settings(v.getContext());
    			s.edit();
    			s.setSyncContacts(true);
    			s.setSyncCalendar(true);
    			s.save();
    			SyncService.startSync(Main.this);
    		}
    	}
    };
    
    private final OnClickListener btnSyncCalListener = new OnClickListener() {
    	public void onClick(View v) {
    		if(BaseWorker.isRunning()) {
    			NotificationDialog.show(v.getContext(), BaseWorker.getRunningMessageResID());
    		} else {    			
    			Settings s = new Settings(v.getContext());
    			s.edit();
    			s.setSyncContacts(false);
    			s.setSyncCalendar(true);
    			s.save();
    			SyncService.startSync(Main.this);
    		}
    	}
    };

    private final DialogInterface.OnClickListener dlgResetListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			ResetService.startReset(Main.this);
			dialog.cancel();
		}
    };
    
    private final DialogInterface.OnClickListener dlgResetSoftListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			ResetSoftService.startReset(Main.this);
			dialog.cancel();
		}
    };

	public void setStatusText(int resid)
	{
		status.setText(resid);
	}

	public void setStatusText(String text)
	{
		status.setText(text);		
	}
	
	public void bindStatus()
	{
		statusAdapter.refresh();
	}
}
