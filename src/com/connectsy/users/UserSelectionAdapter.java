package com.connectsy.users;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.connectsy.R;
import com.connectsy.data.AvatarFetcher;
import com.connectsy.users.ContactCursor.Contact;

public class UserSelectionAdapter extends BaseAdapter implements ListAdapter {

	private static String TAG = "UserSelectionAdapter";
	private Activity context;
	private ArrayList<Object> objects = new ArrayList<Object>();
	private ArrayList<String> friends;
	private HashMap<String, Boolean> friendsSelected = new HashMap<String, Boolean>();

	private ContactCursor contactsCursor;
	private HashMap<String, Pair<Boolean, Contact>> contactsSelected = 
		new HashMap<String, Pair<Boolean, Contact>>();

	public static final int FRIENDS = 1;
	public static final int CONTACTS = 1;
	
	public UserSelectionAdapter(Activity activity, ArrayList<String> users){
		this.context = activity;
//		this.contactsCursor = new ContactCursor(activity);
		update(users);
	}

	public void update(ArrayList<String> friends) {
		this.friends = friends;
		
		objects.clear();
		// Nothing in 0 since it'll be "select all friends".
		objects.add(null);
		objects.addAll(friends);
		if (contactsCursor != null && contactsCursor.getCount() > 0)
			objects.add("Select From Contacts");
	}
	
	public int getCount() {
		int c = objects.size();
		if (contactsCursor != null)
			c += contactsCursor.getCount();
		return c;
	}

	public Object getItem(int position) {
		if (objects.size() <= position){
			int contactPos = position - objects.size();
			return contactsCursor.getAt(contactPos);
		}else{
			return objects.get(position);
		}
	}

	public long getItemId(int position) {
		return position;
	}
	
	public boolean isAllSelected(int type){
		if (type == FRIENDS){
			if (friends.size() == friendsSelected.size()){
				int selected = 0;
				for (HashMap.Entry<String, Boolean> entry : friendsSelected.entrySet())
					if (entry.getValue()) selected++;
				if (selected == friends.size())
					return true;
			}
		}
		return false;
	}
	
	public void selectAll(int type){
		if (type == FRIENDS){
			for (String user: friends)
				friendsSelected.put(user, true);
			this.notifyDataSetChanged();
		}
	}
	
	public void deselectAll(int type){
		if (type == FRIENDS){
			for (String user: friends)
				friendsSelected.put(user, false);
			this.notifyDataSetChanged();
		}
	}
	
	public ArrayList<String> getSelectedFriends(){
		ArrayList<String> users = new ArrayList<String>();
		for (String u: friends)
			if (friendsSelected.containsKey(u) 
					&& friendsSelected.get(u))
				users.add(u);
		return users;
	}
	
	public ArrayList<Contact> getSelectedContacts(){
		ArrayList<Contact> selContacts = new ArrayList<Contact>();
		for (Pair<Boolean, Contact> pair: contactsSelected.values())
			if (pair.first) selContacts.add(pair.second);
		return selContacts;
	}
	
	public void setSelectedFriends(ArrayList<String> friends){
		for (String u: friends)
			friendsSelected.put(u, true);
	}
	
	public void setSelectedContacts(ArrayList<Contact> contacts){
		for (Contact c: contacts)
			contactsSelected.put(c.number, new Pair<Boolean, Contact>(true, c));
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		Object obj = getItem(position);
		LayoutInflater inflater = LayoutInflater.from(context);
		
		if (position == 0){
			View view;
			if (convertView != null && convertView.getId() == R.layout.user_list_item)
				view = convertView;
			else
				view = inflater.inflate(R.layout.user_list_item, parent, false);

			CheckBox check = (CheckBox)view.findViewById(R.id.user_list_item_select);
			check.setVisibility(CheckBox.VISIBLE);
			check.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			    	if (isChecked) selectAll(FRIENDS);
			    	else deselectAll(FRIENDS);
			    }
			});
			check.setChecked(isAllSelected(FRIENDS));
			
	        TextView name = (TextView)view.findViewById(R.id.user_list_item_username);
	        name.setText("All Followers");
	        name.setTextColor(context.getResources().getColor(R.color.text_grey));

	        view.findViewById(R.id.user_list_item_avatar).setVisibility(View.GONE);
	        
			return view;
//		}else if (obj instanceof String){
//			String header = (String) obj;
//			View view;
//			if (convertView != null && convertView.getId() == R.layout.user_list_header)
//				view = convertView;
//			else
//				view = inflater.inflate(R.layout.user_list_header, parent, false);
//			((TextView)view.findViewById(R.id.user_list_header_text))
//					.setText(header);
//			return view;
		}else if (obj instanceof String){
			final String user = (String) obj;
			View view;
			if (convertView != null && convertView.getId() == R.layout.user_list_item)
				view = convertView;
			else
				view = inflater.inflate(R.layout.user_list_item, parent, false);
			
			CheckBox sel = (CheckBox)view.findViewById(R.id.user_list_item_select);
			sel.setVisibility(CheckBox.VISIBLE);
			sel.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			    	friendsSelected.put(user, isChecked);
			    	notifyDataSetChanged();
			    }
			});
			if (friendsSelected.containsKey(user))
				sel.setChecked(friendsSelected.get(user));
			
	        TextView username = (TextView)view.findViewById(R.id.user_list_item_username);
	        username.setText(user);
	        username.setOnClickListener(new TextView.OnClickListener(){
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setType("vnd.android.cursor.item/vnd.connectsy.user");
					i.putExtra("com.connectsy.user.username", user);
		    		context.startActivity(i);
				}
	        });
	        
	        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
	        new AvatarFetcher(user, avatar, false);

	        return view;
		}else if (obj instanceof Contact){
			final Contact contact = (Contact) obj;
			
			View view;
			if (convertView != null && convertView.getId() == R.layout.user_list_item)
				view = convertView;
			else
				view = inflater.inflate(R.layout.user_list_item, parent, false);
			
			CheckBox sel = (CheckBox)view.findViewById(R.id.user_list_item_select);
			sel.setVisibility(CheckBox.VISIBLE);
			sel.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			    	contactsSelected.put(contact.number, 
			    			new Pair<Boolean, Contact>(isChecked, contact));
			    }
			});
			if (contactsSelected.containsKey(contact.number))
				sel.setChecked(contactsSelected.get(contact.number).first);
			
	        TextView name = (TextView)view.findViewById(R.id.user_list_item_username);
	        name.setText(contact.displayName);
	        name.setTextColor(context.getResources().getColor(R.color.text_grey));
	        
	        if (contact.starred)
	        	view.findViewById(R.id.user_list_item_star)
	        			.setVisibility(View.VISIBLE);
	    	
	        TextView number = (TextView)view.findViewById(R.id.user_list_item_detail);
	        number.setText(contact.displayType +": "+ contact.displayNumber);
	        number.setVisibility(TextView.VISIBLE);
	        
	        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
	        ContentResolver cr = context.getContentResolver();
	        Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, 
	        		contact.lookupKey);
	        Log.d(TAG, "lookup uri:"+lookupUri);
	        Uri uri = Contacts.lookupContact(cr, lookupUri);
	        InputStream input = Contacts.openContactPhotoInputStream(cr, uri);
	        if (input != null) 
	             avatar.setImageBitmap(BitmapFactory.decodeStream(input));

	        return view;
		}else{
			// This should never happen!
			// If it does, it means you've added a bad Object type to the 
			// adapter. User, String (for a heading), and Contact are acceptable.
			return null;
		}
	}

}