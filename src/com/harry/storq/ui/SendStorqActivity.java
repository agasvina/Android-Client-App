package com.harry.storq.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.GridView;
import android.widget.Toast;

import com.harry.storq.R;
import com.harry.storq.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

//This will be edited to send an email...


public class SendStorqActivity extends Activity {

	public static final String TAG = RecipientsActivity.class.getSimpleName();

	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;	
	protected List<ParseUser> mFriends;	
	protected MenuItem mSendMenuItem;
	protected Uri mMediaUri;
	protected String mFileType;
	protected GridView mGridView;
	
	//for sending text message
	protected boolean text;
	protected boolean forward;
	protected String msg;
	protected String newMsg;

	protected List<ParseUser> mUsers;
	protected String senders;
	protected ArrayList<String> recipientId;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_send_storq);
		// Show the Up button in the action bar.
		mUsers = new ArrayList<ParseUser>();
		
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.orderByAscending(ParseConstants.KEY_USERNAME);
		query.setLimit(1000);
		try {
			List<ParseUser> dummy = query.find();
			for (ParseUser p: dummy) {
				mUsers.add(p);
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		mMediaUri = getIntent().getData();
		mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
		msg = getIntent().getExtras().getString("storqm");
		senders = getIntent().getExtras().getString("contributors");
		text = getIntent().getBooleanExtra("storqt", false);
		forward = getIntent().getBooleanExtra("forward", false);
		
		
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
			send(message);
			finish();
		}
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
	} 


	protected ParseObject createMessage() {
		ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
		message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
		message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
		recipientId = getRecipientIds();
		message.put(ParseConstants.KEY_RECIPIENT_IDS, recipientId);
		byte[] fileBytes = msg.getBytes();
		message.put(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_TEXT);
		ParseFile file = new ParseFile("testMsg.txt",fileBytes);
		message.put(ParseConstants.KEY_FILE, file);
		message.put("storq", msg);
	if(forward) {				
	    message.put("contributors", senders);
			
	} else {
		//TODO: put the message 
		String contributor = ParseUser.getCurrentUser().getString("gender")  + "-" + ParseUser.getCurrentUser().getString("location"); 
		message.put("contributors", contributor + ",");

	}
	
	return message;
	}
		
	//select Random user..
	protected ArrayList<String> getRecipientIds() {
		ArrayList<String> recipientIds = new ArrayList<String>();
		Random rnd = new Random();
		//TODO: Fix the random recipient.
		int res = rnd.nextInt(mUsers.size());
		recipientIds.add(mUsers.get(res).getObjectId());
		//choose from existing user as well..
		//recipientIds.add("a8RmRw9Kh2");
		return recipientIds;
	}
	
	
	protected void send(ParseObject message) {
		message.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					// success!
					sendPushNotifications();
					Toast.makeText(SendStorqActivity.this, R.string.success_message, Toast.LENGTH_LONG).show();
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
		push.setMessage(getString(R.string.push_message, 
				ParseUser.getCurrentUser().getUsername()));
		push.sendInBackground();
	}
}






