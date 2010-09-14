package com.connectsy.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class DateUtils{
	public static String formatDate(Date date){
		Calendar formatting = Calendar.getInstance();
		formatting.setTime(date);
    	Calendar today = Calendar.getInstance();
		String dateString;
    	
    	if (formatting.get(Calendar.MONTH) == today.get(Calendar.MONTH)
    			&& formatting.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
    		if (formatting.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH))
    			dateString = "Today";
    		else if (formatting.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)+1)
    			dateString = "Tomorrow";
    		else{
    			dateString = new SimpleDateFormat("E the d").format(date);
    			// This is here because apparently Java is a shitty programming
    			// language...
    			String[] thArray = new String[] {
    					"st","nd","rd","th","th","th","th","th","th","th",
    					"th","th","th","th","th","th","th","th","th","th",
    					"st","nd","rd","th","th","th","th","th","th","th",
    					"st" };
    			dateString = dateString+thArray[formatting.get(Calendar.DAY_OF_MONTH)];
    		}
    	}else{
    		dateString = new SimpleDateFormat("MMM d, yyyy").format(date);
    	}
    	return dateString;
	}
	
	public static String formatTime(Date date){
    	SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
    	return timeFormat.format(date);
	}
	
	public static String formatTimestamp(int timestamp){
		Log.d("DateUtils", Integer.toString(timestamp));
		Date date = (Date)new Timestamp(timestamp);
    	String timeString = formatTime(date);
    	String dateString = formatDate(date);
		return dateString+" "+timeString;
	}
}