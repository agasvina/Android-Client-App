package com.harry.storq;

import android.app.Application;

import com.harry.storq.ui.Gesture2Activity;
import com.harry.storq.ui.MainActivity;
import com.harry.storq.utils.ParseConstants;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;

public class StorqApplication extends Application {
	
	
	public static  String[] color;
	/*
	 * Sky Blue: 8ab6ff
Purple: d38aff
Pink: ff89b9
Red: fe8a8a
White: ffffff
Orange: fed289
Green: 90ff89
Turquoise: 89f8ff
Yellow: fff38a
	 * 
	 * */
	
	@Override
	public void onCreate() { 
		super.onCreate();
		
	    Parse.initialize(this, 
	    	"kq2ROIwn7vWNTNqhGuZE635S3fQKGgDKKmhkoj4W", 
	    	"qzwy4MG2oopzc34Nbq6sY40Mml5OqJ0YzZPv72b3");
	    
	    ParseFacebookUtils.initialize(""+R.string.facebook_app_id);

	    
	    //PushService.setDefaultPushCallback(this, MainActivity.class);
	    PushService.setDefaultPushCallback(this, MainActivity.class, R.drawable.ic_launcher);
	    ParseInstallation.getCurrentInstallation().saveInBackground();
	}
	
	public static void updateParseInstallation(ParseUser user) {
		ParseInstallation installation = ParseInstallation.getCurrentInstallation();
		installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
		installation.saveInBackground();
	}
}