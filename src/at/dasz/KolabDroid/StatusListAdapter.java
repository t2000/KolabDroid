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

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import at.dasz.KolabDroid.Provider.StatusProvider;
import at.dasz.KolabDroid.Sync.StatusEntry;

public class StatusListAdapter extends BaseExpandableListAdapter
{
	private ArrayList<StatusEntry>	statusList = new ArrayList<StatusEntry>();
	private Activity				context;

	public StatusListAdapter(Activity context)
	{
		this.context = context;
	}
	
	public void refresh()
	{
		StatusProvider db = new StatusProvider(context);
		try
		{
			statusList = db.getLastStatusEntries();
		}
		finally
		{
			db.close();
		}
		notifyDataSetChanged();
	}

	public Object getChild(int groupPosition, int childPosition)
	{
		return null;
	}

	public long getChildId(int groupPosition, int childPosition)
	{
		return 0;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent)
	{
		View v = convertView;
		if (v == null)
		{
			v = context.getLayoutInflater().inflate(R.layout.statuslist_item,
					null);
		}

		StatusEntry e = statusList.get(groupPosition);

		TextView msg = (TextView) v.findViewById(R.id.msg);
		TextView txt = (TextView) v.findViewById(R.id.text);

		switch (childPosition)
		{
		case 0:
			msg.setText("Items");
			txt.setText(Integer.toString((e.getItems())));
			break;
		case 1:
			msg.setText("Local changed");
			txt.setText(Integer.toString((e.getLocalChanged())));
			break;
		case 2:
			msg.setText("Remote changed");
			txt.setText(Integer.toString((e.getRemoteChanged())));
			break;
		case 3:
			msg.setText("Local added");
			txt.setText(Integer.toString((e.getLocalNew())));
			break;
		case 4:
			msg.setText("Remote added");
			txt.setText(Integer.toString((e.getRemoteNew())));
			break;
		case 5:
			msg.setText("Local deleted");
			txt.setText(Integer.toString((e.getLocalDeleted())));
			break;
		case 6:
			msg.setText("Remote deleted");
			txt.setText(Integer.toString((e.getRemoteDeleted())));
			break;
		case 7:
			msg.setText("Confliced");
			txt.setText(Integer.toString((e.getConflicted())));
			break;
		}
		return v;
	}

	public int getChildrenCount(int groupPosition)
	{
		return 8;
	}

	public Object getGroup(int groupPosition)
	{
		return statusList.get(groupPosition);
	}

	public int getGroupCount()
	{
		return statusList.size();
	}

	public long getGroupId(int groupPosition)
	{
		return 0;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent)
	{
		View v = convertView;
		if (v == null)
		{
			v = context.getLayoutInflater().inflate(R.layout.statuslist_header,
					null);
		}

		StatusEntry e = statusList.get(groupPosition);

		TextView txt = (TextView) v.findViewById(R.id.headertext);
		TextView time = (TextView) v.findViewById(R.id.headertime);
		txt.setText(e.getTask());
		time.setText(e.getTime().format("%c"));
		return v;
	}

	public boolean hasStableIds()
	{
		return false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return false;
	}
}
