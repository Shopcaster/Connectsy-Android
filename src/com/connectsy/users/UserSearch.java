package com.connectsy.users;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.connectsy.R;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.users.UserManager.User;

public class UserSearch extends Activity implements OnClickListener, 
		ApiRequestListener, DataUpdateListener {
	private static final int REFRESH_USER = 1;
	private static final int SEARCH_USERS = 2;
	private static final String TAG = "UserSearch";
	UserAdapter adapter;
	String lastResponse;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_search);
        
        ImageView search = (ImageView)findViewById(R.id.ab_user_search);
        search.setOnClickListener(this);
    }

    private void updateDisplay(String response){
    	if (response != null) lastResponse = response;
    	Log.d(TAG, "displaying: "+lastResponse);
		try {
			ArrayList<User> users = new ArrayList<User>();
			JSONArray usersJson = new JSONObject(lastResponse)
					.getJSONArray("results");
			for (int i=0;i<usersJson.length();i++){
				JSONObject userJson = usersJson.getJSONObject(i);
				User u = manager(userJson.getString("username")).getUser();
				if (u == null){
					manager(userJson.getString("username")).refreshUser(REFRESH_USER);
					Log.d(TAG, userJson.getString("username")+" refreshing");
				}else{
					users.add(u);
					Log.d(TAG, u.username+" added");
				}
			}
			
			if (adapter == null){
				adapter = new UserAdapter(this, R.id.user_search_results, users, false);
				ListView result_list = (ListView)findViewById(R.id.user_search_results);
				result_list.setAdapter(adapter);
			}else{
	        	adapter.clear();
	        	for (int i = 0;i < users.size();i++)
	        		adapter.add(users.get(i));
	    		adapter.notifyDataSetChanged();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
	public void onClick(View v) {
        EditText search = (EditText)findViewById(R.id.user_search_box);
		ApiRequest r = new ApiRequest(this, this, Method.GET, "/users/", true, SEARCH_USERS);
		r.addGetArg("q", search.getText().toString());
		r.execute();
	}

	public void onApiRequestFinish(int status, String response, int code) {
		Log.d(TAG, "onApiRequestFinish: "+response);
		updateDisplay(response);
	}

	public void onDataUpdate(int code, String response) {
		updateDisplay(null);
	}

	private UserManager manager(String username){
		return new UserManager(this, this, username);
	}
	
	public void onApiRequestError(int httpStatus, int retCode) {
		Log.d(TAG, "onApiRequestError: "+httpStatus);
		// TODO Auto-generated method stub
	}
	public void onRemoteError(int httpStatus, int code) {
	}
}
