package com.harry.storq.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.harry.storq.R;
import com.harry.storq.adapters.UserAdapter;
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

public class RecipientsActivity extends Activity {

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

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.user_grid);
		// Show the Up button in the action bar.
		setupActionBar();
		mGridView = (GridView)findViewById(R.id.friendsGrid);
		mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mGridView.setOnItemClickListener(mOnItemClickListener);

		TextView emptyTextView = (TextView)findViewById(android.R.id.empty);
		mGridView.setEmptyView(emptyTextView);
		mCurrentUser = ParseUser.getCurrentUser();
		setProgressBarIndeterminateVisibility(true);
		
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.orderByAscending(ParseConstants.KEY_USERNAME);
		query.setLimit(1000);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> users, ParseException e) {
				setProgressBarIndeterminateVisibility(false);
				
				if (e == null) {
					// Success
					mUsers = users;
					String[] usernames = new String[mUsers.size()];
					int i = 0;
					for(ParseUser user : mUsers) {
						usernames[i] = user.getUsername();
						i++;
					}
				}
			}
		});
		
		
		
		//set the intent... and delete the background object.
//	     chaining = new Intent(GestureActivity.this, RecipientsActivity.class);
//	     chaining.putExtra("forward", true);
//	     chaining.putExtra("storqt", false);
//	     chaining.putExtra("storq", storq);
//	     chaining.putExtra("sender", con);

		
		
		
		mMediaUri = getIntent().getData();
		mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
		msg = getIntent().getExtras().getString("storqm");
		senders = getIntent().getExtras().getString("contributors");

		text = getIntent().getBooleanExtra("storqt", false);
		forward = getIntent().getBooleanExtra("forward", false);
				
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
		
		setProgressBarIndeterminateVisibility(true);
		
		ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
		query.addAscendingOrder(ParseConstants.KEY_USERNAME);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				setProgressBarIndeterminateVisibility(false);
				
				if (e == null) {
					mFriends = friends;
					
					String[] usernames = new String[mFriends.size()];
					int i = 0;
					for(ParseUser user : mFriends) {
						usernames[i] = user.getUsername();
						i++;
					}

					if (mGridView.getAdapter() == null) {
						UserAdapter adapter = new UserAdapter(RecipientsActivity.this, mFriends);
						mGridView.setAdapter(adapter);
					}
					else {
						((UserAdapter)mGridView.getAdapter()).refill(mFriends);
					}
				}
				else {
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
					builder.setMessage(e.getMessage())
						.setTitle(R.string.error_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		});
		
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recipients, menu);
		mSendMenuItem = menu.getItem(0);
		mSendMenuItem.setVisible(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_send:
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected ParseObject createMessage() {
		ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
		message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
		message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
		message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
		byte[] fileBytes = msg.getBytes();
		message.put(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_TEXT);
		ParseFile file = new ParseFile("testMsg.txt",fileBytes);
		message.put(ParseConstants.KEY_FILE, file);
		message.put("storq", msg);
	if(forward) {	
			
			message.put("contributors", senders);
			
	} else {
		message.put("contributors", ParseUser.getCurrentUser().getUsername() + ",");

	}
	
	return message;
	
	
		
	}
		
		
//	protected ArrayList<String> getRecipientIds() {
//		ArrayList<String> recipientIds = new ArrayList<String>();
//		for (int i = 0; i < mGridView.getCount(); i++) {
//			if (mGridView.isItemChecked(i)) {
//				recipientIds.add(mFriends.get(i).getObjectId());
//			}
//		}
//		return recipientIds;
//	}
	
	//select Random user..
	protected ArrayList<String> getRecipientIds() {
		ArrayList<String> recipientIds = new ArrayList<String>();
		Random rnd = new Random();
		int res = rnd.nextInt(mUsers.size());
		recipientIds.add(mUsers.get(res).getObjectId());

		//choose from existing user as well..
		for (int i = 0; i < mGridView.getCount(); i++) {
			if (mGridView.isItemChecked(i)) {
				recipientIds.add(mFriends.get(i).getObjectId());
			}
		}
		
		return recipientIds;
	}
	
	
	protected void send(ParseObject message) {
		message.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					// success!
					Toast.makeText(RecipientsActivity.this, R.string.success_message, Toast.LENGTH_LONG).show();
					sendPushNotifications();
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
					builder.setMessage(R.string.error_sending_message)
						.setTitle(R.string.error_selecting_file_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		});
	}
	
	protected OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//mSendMenuItem.setVisible(true);
			ImageView checkImageView = (ImageView)view.findViewById(R.id.checkImageView);

			if (mGridView.isItemChecked(position)) {
				// add the recipient
				checkImageView.setVisibility(View.VISIBLE);
			}
			else {
				// remove the recipient
				checkImageView.setVisibility(View.INVISIBLE);
			}
		}
	};
	
	protected void sendPushNotifications() {
		ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
		query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());
		
		// send push notification
		ParsePush push = new ParsePush();
		push.setQuery(query);
		push.setMessage(getString(R.string.push_message, 
				ParseUser.getCurrentUser().getUsername()));
		push.sendInBackground();
	}
}






