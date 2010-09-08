package com.connectsy.users;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.connectsy.R;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.settings.Settings;
import com.connectsy.users.UserManager.User;
import com.wilson.android.library.DrawableManager;

public class UserAdapter extends ArrayAdapter<User> {
	private boolean multi;
	
	public UserAdapter(Context context, int viewResourceId,
			ArrayList<User> users, boolean selectMultiple) {
		super(context, viewResourceId, users);
		multi = selectMultiple;
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		final Context context = getContext();
		final User user = getItem(position);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.user_list_item, parent, false);
        
		if (multi){
			CheckBox sel = (CheckBox)view.findViewById(R.id.user_list_item_select);
			sel.setVisibility(CheckBox.VISIBLE);
		}
		
        TextView username = (TextView)view.findViewById(R.id.user_list_item_username);
        username.setText(user.username);
        username.setOnClickListener(new TextView.OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
				i.putExtra("com.connectsy.user.username", user.username);
	    		context.startActivity(i);
			}
        });
        
        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
        DrawableManager dm = new DrawableManager();
        String avyUrl = Settings.API_DOMAIN+"/users/"+user.username+"/avatar/";
        dm.fetchDrawableOnThread(avyUrl, avatar);
        
        if (user.friendStatusPending){
        	Button confirm = (Button)view.findViewById(R.id.user_list_item_confirm);
        	confirm.setVisibility(Button.VISIBLE);
        	confirm.setOnClickListener(new Button.OnClickListener(){
        		private DataUpdateListener l = new DataUpdateListener() {
					public void onRemoteError(int httpStatus, int code) {}
					public void onDataUpdate(int code, String response) {}
				};
        		
    			public void onClick(View v) {
    				UserManager manager = new UserManager(context, l, user.username);
    				manager.befriend(0);
    			}
            });
        }
        
        return view;
	}
}
