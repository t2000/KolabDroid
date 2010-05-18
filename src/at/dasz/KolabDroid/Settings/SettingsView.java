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

package at.dasz.KolabDroid.Settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.CompoundButton.OnCheckedChangeListener;
import at.dasz.KolabDroid.R;

public class SettingsView extends Activity {
	public static final String EDIT_SETTINGS_ACTION = "at.dasz.KolabDroid.Settings.action.EDIT_TITLE";

	private EditText txtHost;
	private EditText txtPort;
	private CheckBox cbUseSSL;
	private EditText txtUsername;
	private EditText txtPassword;
	private EditText txtFolderContact;
	private EditText txtFolderCalendar;
	private CheckBox cbCreateRemoteHash;
	private CheckBox cbMergeContactsByName;
	private Spinner spAccount;
		
	private Settings pref;
	private boolean isInitializing = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        txtHost = (EditText)findViewById(R.id.edithost);
        txtPort = (EditText)findViewById(R.id.editport);
        cbUseSSL = (CheckBox)findViewById(R.id.usessl);
        txtUsername = (EditText)findViewById(R.id.editusername);
        txtPassword = (EditText)findViewById(R.id.editpassword);
        txtFolderContact = (EditText)findViewById(R.id.editfoldercontact);
        txtFolderCalendar = (EditText)findViewById(R.id.editfoldercalendar);
        cbCreateRemoteHash = (CheckBox)findViewById(R.id.createRemoteHash);
        cbMergeContactsByName = (CheckBox)findViewById(R.id.mergeContactsByName);
        spAccount = (Spinner)findViewById(R.id.selectAccount);
        
        pref = new Settings(this);
        
        cbUseSSL.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isInitializing) return;
				if(isChecked && "143".equals(txtPort.getText().toString())) {
					txtPort.setText("993");
				}
				else if(!isChecked && "993".equals(txtPort.getText().toString())) {
					txtPort.setText("143");
				}
			}
		});
			
		isInitializing = true;
		
		txtHost.setText(pref.getHost());
		txtPort.setText(Integer.toString(pref.getPort()));
		cbUseSSL.setChecked(pref.getUseSSL());
		txtUsername.setText(pref.getUsername());
		txtPassword.setText(pref.getPassword());
		txtFolderContact.setText(pref.getContactsFolder());
		txtFolderCalendar.setText(pref.getCalendarFolder());
		cbCreateRemoteHash.setChecked(pref.getCreateRemoteHash());
		cbMergeContactsByName.setChecked(pref.getMergeContactsByName());
		
		//TODO: adjust account spinner to show configured account
		//setFirstAccount();
		
		isInitializing = false;
	}

	@Override
	protected void onPause() {
        pref.edit();
		pref.setHost(txtHost.getText().toString());
		pref.setPort(Integer.parseInt(txtPort.getText().toString()));
		pref.setUseSSL(cbUseSSL.isChecked());
		pref.setUsername(txtUsername.getText().toString());
		pref.setPassword(txtPassword.getText().toString());
		pref.setContactsFolder(txtFolderContact.getText().toString());
		pref.setCalendarFolder(txtFolderCalendar.getText().toString());
		pref.setCreateRemoteHash(cbCreateRemoteHash.isChecked());
		pref.setMergeContactsByName(cbMergeContactsByName.isChecked());
		
		//TODO: adjust account spinner to show configured account
		setFirstAccount();
		
		pref.save();

		super.onPause();
	}
	
	private void setFirstAccount()
	{
		 // Get account data from system
		
        Account[] accounts = AccountManager.get(this).getAccounts();
        
        if(accounts.length >0)
        {
        	pref.setAccountName(accounts[0].name);
        	pref.setAccountType(accounts[0].type);
        }
        else
        {
        	pref.setAccountName("");
        	pref.setAccountType("");
        }
	}
	
}
