package at.dasz.KolabDroid.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentValues;
import android.provider.ContactsContract;
import at.dasz.KolabDroid.Utils;

public class Contact
{
	private int					id;
	//private String				fullName, uid;
	private String				uid;
	private String				givenName, familyName;
	private String				birthday; //string as in android for now
	
	public String getBirthday()
	{
		return birthday;
	}

	public void setBirthday(String birthday)
	{
		this.birthday = birthday;
	}

	public String getGivenName()
	{
		return givenName;
	}

	public void setGivenName(String givenName)
	{
		this.givenName = givenName;
	}

	public String getFamilyName()
	{
		return familyName;
	}

	public void setFamilyName(String familyName)
	{
		this.familyName = familyName;		
	}

	private List<ContactMethod>	contactMethods	= new ArrayList<ContactMethod>();
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}

	//@Deprecated
	/*
	public void setFullName(String fullName)
	{
		//this.fullName = fullName;
		Log.e("EEEE", "setFullname not suported anymore");
	}
	*/
	
	public String getFullName()
	{
		return givenName + " " + familyName;
		//return fullName;
	}

	public List<ContactMethod> getContactMethods()
	{
		return contactMethods;
	}

	public ContentValues toContentValues()
	{
		ContentValues result = new ContentValues();
		result.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, getFullName());
		//result.put(People.NAME, this.fullName);
		return result;
	}

	@Override
	public String toString()
	{
		return getFullName() + " with " + getContactMethods().size()
				+ " contact methods";
	}

	public String getLocalHash()
	{
		ArrayList<String> contents = new ArrayList<String>(contactMethods
				.size() + 1);
		contents.add(getFullName() == null ? "no name" : getFullName());

		Collections.sort(contactMethods, new Comparator<ContactMethod>() {
			public int compare(ContactMethod cm1, ContactMethod cm2)
			{
				return cm1.toString().compareTo(cm2.toString());
			}
		});

		for (ContactMethod cm : contactMethods)
		{
			contents.add(cm.getData());
		}
		
		contents.add(birthday);

		return Utils.join("|", contents.toArray());
	}
}
