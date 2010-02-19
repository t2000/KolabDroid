package at.dasz.KolabDroid.Settings;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
		pref.save();

		super.onPause();
	}
}
