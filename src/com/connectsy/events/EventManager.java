package com.connectsy.events;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.connectsy.LocManager;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager;

public class EventManager extends DataManager {
	private static final String TAG = "EventManager";
	
	private static final int CREATE_EVENT = 0;
	private static final int GET_EVENTS = 1;
	private static final int GET_EVENT = 2;
	
	private String category;
	private Filter filter;
	private ArrayList<Event> events;
	private LocManager locManager;
	
	public enum Filter{ALL, CATEGORY, FRIENDS}
	
	public class Event{
		public String ID;
		public String revision;
		public String creator;
		public String description;
		public String where;
		public int when;
		public String category;
		public String location;
		public boolean broadcast;
		public boolean friends;
		public ArrayList<Integer> someFriends;
		public int created;

		public Event(){}
		
		public Event(JSONObject response) throws JSONException{
			JSONObject event = response.getJSONObject("event");
			ID = event.getString("id");
			revision = event.getString("revision");
			created = event.getInt("created");
			creator = event.getString("creator");
			description = event.getString("desc");
			where = event.getString("where");
			when = event.getInt("when");
			category = event.getString("category");
			// TODO: broadcast isn't optional, I just don't want to pitch my data 
			// right now.
			if (event.has("broadcast"))
				broadcast = event.getBoolean("broadcast");
			if (event.has("location"))
				location = event.getString("location");
		}
	}
	
	public EventManager(Context c, DataUpdateListener l, Filter f, String cat) {
		super(c, l);
		filter = f;
		category = cat;
		locManager = new LocManager(c);
	}
	
	private ApiRequest getEventsRequest(){
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(); 
		if (filter == Filter.FRIENDS)
			args.add(new BasicNameValuePair("friends", "true"));
		else if (filter == Filter.CATEGORY)
			args.add(new BasicNameValuePair("category", category));
		
		Location loc = locManager.getLocation();
		if (loc != null){
			args.add(new BasicNameValuePair("lat", Double.toString(loc.getLatitude())));
			args.add(new BasicNameValuePair("lng", Double.toString(loc.getLongitude())));
		}
		
		ApiRequest request = new ApiRequest(this, context, Method.GET, 
				"/events/", true, GET_EVENTS);
		request.setGetArgs(args);
		return request;
	}
	
	public ArrayList<Event> getEvents(){
		events = new ArrayList<Event>();
		String eventsCache = getEventsRequest().getCached();
		if (eventsCache == null) 
			return events;
		try {
			JSONArray revisions = new JSONObject(eventsCache)
					.getJSONArray("events");
			for(int i=0;i<revisions.length();i++){
				Event event = getEvent(revisions.getString(i));
				if (event != null) events.add(event);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return events;
	}
	
	public void refreshEvents(int sentReturnCode){
		returnCode = sentReturnCode;
		getEventsRequest().execute();
		pendingUpdates++;
	}
	
	private void refreshEventsReturn(String response){
		try {
			JSONArray revisions = new JSONObject(response)
					.getJSONArray("events");
			for(int i=0;i<revisions.length();i++)
				refreshEvent(revisions.getString(i), returnCode);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private ApiRequest getEventRequest(String revision){
		return new ApiRequest(this, context, Method.GET, 
				"/events/"+revision+"/", true, GET_EVENT);
	}
	
	public Event getEvent(String revision){
		ApiRequest eventRequest = getEventRequest(revision);
		String eventString = eventRequest.getCached();
		if (eventString == null) return null;
		try {
			return new Event(new JSONObject(eventString));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void refreshEvent(String revision, int passedReturnCode){
		ApiRequest eventRequest = getEventRequest(revision);
		String eventString = eventRequest.getCached();
		if (eventString == null){
			returnCode = passedReturnCode;
			pendingUpdates++;
			eventRequest.execute();
		}
	}

	public void createEvent(Event event, int passedReturnCode) {
		returnCode = passedReturnCode;
		
		JSONObject json = new JSONObject();
		try {
			json.put("where", event.where);
			json.put("when", event.when);
			json.put("desc", event.description);
			json.put("category", event.category);
			json.put("broadcast", event.broadcast);
			json.put("friends", event.friends);
			json.put("client", "Connectsy for Android");
			Location loc = locManager.getLocation();
			if (loc != null){
				JSONArray jLoc = new JSONArray();
				jLoc.put(loc.getLatitude());
				jLoc.put(loc.getLongitude());
				json.put("posted_from", jLoc);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ApiRequest r = new ApiRequest(this, context, Method.POST, 
				"/events/", true, CREATE_EVENT);
		r.setBodyString(json.toString());
		r.execute();
		pendingUpdates++;
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		if (code == GET_EVENTS)
			refreshEventsReturn(response);
		pendingUpdates--;
		if (pendingUpdates == 0)
			listener.onDataUpdate(returnCode, response);
	}
}