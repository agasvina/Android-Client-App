package com.harry.storq.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.harry.storq.R;
import com.harry.storq.utils.ParseConstants;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class SendStorqActivity extends Activity {


	protected Uri mMediaUri;
	protected String mFileType;
	
	//for sending text message
	protected boolean text;
	protected boolean forward;
	protected String msg;
	protected String newMsg;
	protected String Location;
	protected int total = 1;

	protected List<ParseUser> mUsers;
	protected String senders;
	protected ArrayList<String> recipientId;

	
	//added progress bar.. just in case:
	protected ProgressBar mProgressBar;
	protected boolean proceed = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_send_storq);
		mUsers = new ArrayList<ParseUser>();
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        mProgressBar.setVisibility(View.VISIBLE);

        Random random = new Random();
        
        //getting the recipient ID
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
		query.setLimit(1000);
		int totalSkip = 1;
		try {
			 this.total = query.count();
			 if(total > 1000) {
				 totalSkip = (this.total-1000)/100;
				 if (totalSkip > 0)  {
					 totalSkip = random.nextInt(totalSkip) -1;
				 } else {
					 totalSkip = 0;
				 }
				 query.setSkip(totalSkip*100);
			 }
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			List<ParseUser> dummy = query.find();
			for (ParseUser p: dummy) {
				mUsers.add(p);
			}
		} catch (ParseException e1) {
		}
		
		
		

		
		mMediaUri = getIntent().getData();
		mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
		msg = getIntent().getExtras().getString("storqm");
		senders = getIntent().getExtras().getString("contributors");
		text = getIntent().getBooleanExtra("storqt", false);
		forward = getIntent().getBooleanExtra("forward", false);
		Location = getIntent().getStringExtra("Location");
		
		if (Location == null) {
			Location = ParseUser.getCurrentUser().getString("location");
		}
		
		ParseObject message = createMessage();
		if (message == null) {
			// error
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.error_selecting_file)
				.setTitle(R.string.error_selecting_file_title)
				.setPositiveButton(android.R.string.ok, null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		else {
	        mProgressBar.setVisibility(View.INVISIBLE);
			send(message);
			finish();
		}
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
	} 


	@SuppressLint("DefaultLocale") protected ParseObject createMessage() {
		ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
		message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
		message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
		
		//Choose Random recipient
		recipientId = getRecipientIds();
		message.put(ParseConstants.KEY_RECIPIENT_IDS, recipientId);
		
		byte[] fileBytes = msg.getBytes();
		message.put(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_TEXT);
		ParseFile file = new ParseFile("testMsg.txt",fileBytes);
		
		message.put(ParseConstants.KEY_FILE, file);
		message.put("storq", msg);
		
		//Add information: Location and Gender
		if(forward) {				
		    message.put("contributors", senders);	
		} else {
			String gender = ParseUser.getCurrentUser().getString("gender");
			if(gender.equals("female")) {
				gender = "F";
			} else if (gender.equals("male")) {
				gender = "M";
			} else {
				gender = "NA";
			}
			
			String age = ParseUser.getCurrentUser().getString("age");
			String contributor = gender + ", " + Location + " (" + age + ")~"; 
			String c = contributor.toLowerCase();
			message.put("contributors", c);
	
		}
		
	return message;
	
	}
		
	//select Random user..
	protected ArrayList<String> getRecipientIds() {
		ArrayList<String> recipientIds = new ArrayList<String>();
		Random rnd = new Random();
		//TODO: Fix the random recipient.. more robust algorithm
		int res = rnd.nextInt(mUsers.size());
		recipientIds.add(mUsers.get(res).getObjectId());
		return recipientIds;
	}
	
	
	protected void send(ParseObject message) {
		message.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					sendPushNotifications();
					//Toast.makeText(SendStorqActivity.this, R.string.success_message, Toast.LENGTH_LONG).show();
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(SendStorqActivity.this);
					builder.setMessage(R.string.error_sending_message)
						.setTitle(R.string.error_selecting_file_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		});
	}
	

	
	protected void sendPushNotifications() {
		ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
		query.whereContainedIn(ParseConstants.KEY_USER_ID,recipientId);
		
		// send push notification
		ParsePush push = new ParsePush();
		push.setQuery(query);
		String location = "";
		if(MainActivity.Location != null) {
			location = MainActivity.Location;
		} else {
			location = ParseUser.getCurrentUser().getString("location");
		}
//		push.setMessage(getString(R.string.push_message, 
//				ParseUser.getCurrentUser().getUsername()));
		push.setMessage(getString(R.string.push_message) + location);
		push.sendInBackground();
	}
}






