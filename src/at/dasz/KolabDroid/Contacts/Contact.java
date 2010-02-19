package at.dasz.KolabDroid.Contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentValues;
import android.provider.Contacts.People;
import at.dasz.KolabDroid.Utils;

public class Contact
{
	private int					id;
	private String				fullName;

	private List<ContactMethod>	contactMethods	= new ArrayList<ContactMethod>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}


	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	public String getFullName()
	{
		return fullName;
	}

	public List<ContactMethod> getContactMethods()
	{
		return contactMethods;
	}

	public ContentValues toContentValues()
	{
		ContentValues result = new ContentValues();
		result.put(People.NAME, this.fullName);
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

		return Utils.join("|", contents.toArray());
	}
}
