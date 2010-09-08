package com.connectsy.events;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.connectsy.R;
import com.connectsy.categories.CategoryManager;
import com.connectsy.categories.CategoryManager.Category;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.events.EventManager.Event;
import com.connectsy.settings.MainMenu;

public class EventNew extends Activity implements OnClickListener, DataUpdateListener {
	private final String TAG = "NewEvent";
	private ProgressDialog loadingDialog;
    private EventManager eventManager;
    private Category category;
	
    // where we display the selected date and time
    private TextView mDateDisplay;
    private TextView mTimeDisplay;
	
    static final int TIME_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;
    
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_new);

        mDateDisplay = (TextView) findViewById(R.id.events_new_date);
        mTimeDisplay = (TextView) findViewById(R.id.events_new_time);
        
        Button dateButton = (Button)findViewById(R.id.events_new_date_change);
        dateButton.setOnClickListener(this);
        Button timeButton = (Button)findViewById(R.id.events_new_time_change);
        timeButton.setOnClickListener(this);
        
        Button everyone = (Button)findViewById(R.id.events_new_who_everyone);
        everyone.setOnClickListener(this);
        Button friends = (Button)findViewById(R.id.events_new_who_friends);
        friends.setOnClickListener(this);
        friends.setSelected(true);
        LinearLayout category = (LinearLayout)findViewById(R.id.events_new_cat);
        category.setOnClickListener(this);
        
        Button submitButton = (Button)findViewById(R.id.events_new_submit);
        submitButton.setOnClickListener(this);
        
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        
        updateTimeDisplay();
        
        CategoryManager.precacheCategories(this);
    }

	public void onClick(View v) {
		int id = v.getId();
        if (id == R.id.events_new_time_change){
        	showDialog(TIME_DIALOG_ID);
        }else if (id == R.id.events_new_date_change){
        	showDialog(DATE_DIALOG_ID);
	    }else if (id == R.id.events_new_who_everyone){
        	setWho("everyone");
	    }else if (id == R.id.events_new_who_friends){
        	setWho("friends");
	    }else if (id == R.id.events_new_cat){
        	getCategory();
	    }else if (id == R.id.events_new_submit){
        	submitData();
	    }else{
	    	Log.d("events", "bad view is for button");
	    }
	}
	
	private void getCategory(){
		Intent i = new Intent(Intent.ACTION_CHOOSER);
		i.setType("vnd.android.cursor.item/vnd.connectsy.category");
		//ArrayList<Category> categories = new CategoryManager(this, this).getCategories();
		//i.putExtra("com.connectsy.categories", Category.serializeList(categories));
		startActivityForResult(i, 0);
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == RESULT_OK){
			try {
				category = new Category(data.getExtras().getString("com.connectsy.category"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			TextView title = (TextView)findViewById(R.id.events_new_category_title);
			title.setText(category.name);
		}
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, mHour, mMinute, false);
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener, mYear, mMonth, mDay);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    
                    updateTimeDisplay();
                }
            };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour = hourOfDay;
                    mMinute = minute;
                    
                    updateTimeDisplay();
                }
            };
            
    private Calendar getCal(){
    	Calendar c = Calendar.getInstance();
    	c.set(mYear, mMonth, mDay, mHour, mMinute);
    	return c;
    }
            
    private void updateTimeDisplay() {
    	Calendar selected = getCal();
    	Calendar today = Calendar.getInstance();
    	SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
    	String timeString = timeFormat.format(selected.getTime());
        mTimeDisplay.setText(timeString);
        
    	String dateString;
    	
    	if (selected.get(Calendar.MONTH) == today.get(Calendar.MONTH)
    			&& selected.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
    		if (selected.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH))
    			dateString = "Today";
    		else if (selected.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)+1)
    			dateString = "Tomorrow";
    		else{
    			dateString = new SimpleDateFormat("E the d").format(selected.getTime());
    			// This is here because apparently Java is a shitty programming
    			// language...
    			String[] thArray = new String[] {
    					"st","nd","rd","th","th","th","th","th","th","th",
    					"th","th","th","th","th","th","th","th","th","th",
    					"st","nd","rd","th","th","th","th","th","th","th",
    					"st" };
    			dateString = dateString+thArray[selected.get(Calendar.DAY_OF_MONTH)];
    		}
    	}else{
    		dateString = new SimpleDateFormat("MMM d, yyyy").format(selected.getTime());
    	}
        
        mDateDisplay.setText(dateString);
    }
    
    private void setEnabled(Button enabled, Button disabled){
		enabled.setSelected(true);
		disabled.setSelected(false);
	}
    
    private void setWho(String who){
    	Button ev = (Button)findViewById(R.id.events_new_who_everyone);
    	Button fr = (Button)findViewById(R.id.events_new_who_friends);
    	if (who == "everyone"){
	        findViewById(R.id.events_new_cat).setVisibility(View.VISIBLE);
	        setEnabled(ev, fr);
    	}else{
	        findViewById(R.id.events_new_cat).setVisibility(View.GONE);
	        setEnabled(fr, ev);
    	}
    }

    private void submitData() {
    	Log.d(TAG, "creating event");
    	
        EditText desc = (EditText) findViewById(R.id.events_new_desc);
        EditText where = (EditText) findViewById(R.id.events_new_where);
        String strDesc = desc.getText().toString();
        String strWhere = where.getText().toString();
        Button bcast = (Button)findViewById(R.id.events_new_who_everyone);
        Button friends = (Button)findViewById(R.id.events_new_who_friends);
        
        SharedPreferences data = getSharedPreferences("consy", 0);
        String username = data.getString("username", "username_fail");

        int when = (int) new Timestamp(mYear, mMonth, mDay, mHour, mMinute, 0, 0).getTime();
        
        eventManager = new EventManager(this, this, null, null);
        Event event = eventManager.new Event();
        event.description = strDesc;
        event.where = strWhere;
        event.when = when;
        event.creator = username;
        event.broadcast = bcast.isSelected();
        if (event.broadcast && category != null)
        	event.category = category.name;
        event.friends = friends.isSelected();
        eventManager.createEvent(event, 0);
        loadingDialog = ProgressDialog.show(this, "", "Posting event...", true);
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        return MainMenu.onCreateOptionsMenu(menu);
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {
        return MainMenu.onOptionsItemSelected(this, item);
    }

	public void onDataUpdate(int code, String response) {
		loadingDialog.dismiss();
		try {
			JSONObject e = new JSONObject(response);
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.event");
			i.putExtra("com.connectsy.events.revision", e.getString("revision"));
			startActivity(i);
			this.finish();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void onRemoteError(int httpStatus, int code) {
		loadingDialog.dismiss();
	}
}