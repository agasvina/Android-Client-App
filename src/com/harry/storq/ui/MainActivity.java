package com.harry.storq.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.harry.storq.R;
import com.harry.storq.adapters.MessageAdapter;
import com.harry.storq.adapters.SectionsPagerAdapter;
import com.harry.storq.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {
	
	public static final String TAG = MainActivity.class.getSimpleName();
	
	public static final int TAKE_PHOTO_REQUEST = 0;
	public static final int TAKE_VIDEO_REQUEST = 1;
	public static final int PICK_PHOTO_REQUEST = 2;
	public static final int PICK_VIDEO_REQUEST = 3;
	
	public static final int MEDIA_TYPE_IMAGE = 4;
	public static final int MEDIA_TYPE_VIDEO = 5;
	public static final int MEDIA_TYPE_TEXT = 6;
	public static final int CREATE_TEXT = 7;
	
	public static final int FILE_SIZE_LIMIT = 1024*1024*10; // 10 MB
	
	protected Uri mMediaUri;
	
	
	//Add gesture activity
  	protected GestureDetector gestureDetector;
  	protected Intent passingIntent;
  	protected EditText storqText;
  	protected String message = "";
  	
  	//add ability to choose random user.
	protected List<ParseUser> mUsers;
	
	//Show message automatically...
	public static List<ParseObject> mMessages;
	protected SwipeRefreshLayout mSwipeRefreshLayout;
	protected String [] listMessage;
	protected String [] parseObjectId;

	
	protected DialogInterface.OnClickListener mDialogListener = 
			new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch(which) {
				case 0: 
				    Intent intent = new Intent(MainActivity.this,GetActivity.class);
				    startActivityForResult(intent,CREATE_TEXT);
					break;
				case 1: 
					//testing gesture
					//send activity to the gesture Activity
					Toast.makeText(MainActivity.this, ParseUser.getCurrentUser().getString("gender"), Toast.LENGTH_LONG).show();
					Intent intentGest = new Intent(MainActivity.this,GestureActivity.class);
					startActivity(intentGest);
					break;
//				case 2: // Choose picture
//					Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
//					choosePhotoIntent.setType("image/*");
//					startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
//					break;
//				case 3: // Choose video
//					Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
//					chooseVideoIntent.setType("video/*");
//					Toast.makeText(MainActivity.this, R.string.video_file_size_warning, Toast.LENGTH_LONG).show();
//					startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
//					break;
			}
		}

		private Uri getOutputMediaFileUri(int mediaType) {
			// To be safe, you should check that the SDCard is mounted
		    // using Environment.getExternalStorageState() before doing this.
			if (isExternalStorageAvailable()) {
				// get the URI
				
				// 1. Get the external storage directory
				String appName = MainActivity.this.getString(R.string.app_name);
				File mediaStorageDir = new File(
						Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						appName);
				
				// 2. Create our subdirectory
				if (! mediaStorageDir.exists()) {
					if (! mediaStorageDir.mkdirs()) {
						Log.e(TAG, "Failed to create directory.");
						return null;
					}
				}
				
				// 3. Create a file name
				// 4. Create the file
				File mediaFile;
				Date now = new Date();
				String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);
				
				String path = mediaStorageDir.getPath() + File.separator;
				if (mediaType == MEDIA_TYPE_IMAGE) {
					mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
				}
				else if (mediaType == MEDIA_TYPE_VIDEO) {
					mediaFile = new File(path + "VID_" + timestamp + ".mp4");
				}
				else {
					return null;
				}
				
				Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
				
				// 5. Return the file's URI				
				return Uri.fromFile(mediaFile);
			}
			else {
				return null;
			}
		}
		
		private boolean isExternalStorageAvailable() {
			String state = Environment.getExternalStorageState();
			
			if (state.equals(Environment.MEDIA_MOUNTED)) {
				return true;
			}
			else {
				return false;
			}
		}
	};

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		//get user from facebook...
	    // Fetch Facebook user info if the session is active
	    Session session = ParseFacebookUtils.getSession();
	    if (session != null && session.isOpened()) {
	      makeMeRequest();
	    }
		
		
		ParseAnalytics.trackAppOpened(getIntent());
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser == null) {
			navigateToLogin();
		}
		else {
			Log.i(TAG, currentUser.getUsername());
		}
	    
		
		//TODO: is this correct...?
		//Retrieve message... 
		//retrieveMessages();
		
		
		
		
		
		//Gesture Activity
		gestureDetector = new GestureDetector(
                new SwipeGestureDetector());		
		
		
		//Binding the text edit and button to the appropriate android object
		Button button = (Button) findViewById(R.id.getTextButton);
		button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	sendMessage();
            }
        });
		
		
		

		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
		query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> messages, ParseException e) {	
				if (e == null) {
					// We found messages!
					Intent intent = new Intent(MainActivity.this, Gesture2Activity.class);
					startActivityForResult(intent, 1);
				}
			}
		});
		
		

		
		
		
		//This is for restoring the tab for future request... (in case they want friends)
		
		/*
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(this, 
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setIcon(mSectionsPagerAdapter.getIcon(i))
					.setTabListener(this));
		}
		*/
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		
		
		if (resultCode == RESULT_OK) {			
			if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
				if (data == null) {
					Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
				}
				else {
					mMediaUri = data.getData();
				}
				
				Log.i(TAG, "Media URI: " + mMediaUri);
				if (requestCode == PICK_VIDEO_REQUEST) {
					// make sure the file is less than 10 MB
					int fileSize = 0;
					InputStream inputStream = null;
					
					try {
						inputStream = getContentResolver().openInputStream(mMediaUri);
						fileSize = inputStream.available();
					}
					catch (FileNotFoundException e) {
						Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
						return;
					}
					catch (IOException e) {
						Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
						return;
					}
					finally {
						try {
							inputStream.close();
						} catch (IOException e) { /* Intentionally blank */ }
					}
					
					if (fileSize >= FILE_SIZE_LIMIT) {
						Toast.makeText(this, R.string.error_file_size_too_large, Toast.LENGTH_LONG).show();
						return;
					}
				}
			}
			else {
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				mediaScanIntent.setData(mMediaUri);
				sendBroadcast(mediaScanIntent);
			}
			
			Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
			recipientsIntent.setData(mMediaUri);
			
			String fileType;
			if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {
				fileType = ParseConstants.TYPE_IMAGE;
			}
			else {
				fileType = ParseConstants.TYPE_VIDEO;
			}
			
			recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
			startActivity(recipientsIntent);
		}
		else if (resultCode != RESULT_CANCELED) {
			Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
		}
	}

	private void navigateToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		
		switch(itemId) {
			case R.id.action_logout:
				ParseUser.logOut();
				navigateToLogin();
				break;
			case R.id.action_edit_friends:
				Intent intent = new Intent(this, EditFriendsActivity.class);
				startActivity(intent);
				break;
//			case R.id.action_camera:
//				AlertDialog.Builder builder = new AlertDialog.Builder(this);
//				builder.setItems(R.array.camera_choices, mDialogListener);
//				AlertDialog dialog = builder.create();
//				dialog.show();
//				break;
			case R.id.action_mail:
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setItems(R.array.text_choices, mDialogListener);
				AlertDialog dialog2 = builder2.create();
				dialog2.show();
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	 private void makeMeRequest() {
		    Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
		      new Request.GraphUserCallback() {
		        @Override
		        public void onCompleted(GraphUser user, Response response) {
		          if (user != null) {
		            // Create a JSON object to hold the profile info
		            ParseUser currentUser = ParseUser.getCurrentUser();
		            JSONObject userProfile = new JSONObject();
		            try {
                      
		            	//}
		              userProfile.put("facebookId", user.getId());
		              userProfile.put("name", user.getName());
		              if (user.getProperty("gender") != null) {
		                userProfile.put("gender", user.getProperty("gender"));
		            	currentUser.put("gender", user.getProperty("gender"));
		              } else {
			            currentUser.put("gender", "NA");
		              }
		              if (user.getProperty("email") != null) {
		                userProfile.put("email", user.getProperty("email"));
			            currentUser.put("email", user.getProperty("email"));
			          }
		              if (user.getLocation().getProperty("name") != null) {
		            	  userProfile.put("loc", user.getLocation().getProperty("name"));
		            	  currentUser.put("location", user.getLocation().getProperty("name"));
		              } else {
		            	  currentUser.put("location", "Unknown");
		              }

		              // Save the user profile info in a user property
		              currentUser.put("profile", userProfile);
		              currentUser.put(ParseConstants.KEY_USERNAME, user.getName());
		              currentUser.saveInBackground();

		              // Show the user info

		            
		            } catch (JSONException e) {
		             // Log.d(IntegratingFacebookTutorialApplication.TAG, "Error parsing returned user data. " + e);
		            }

		          } else if (response.getError() != null) {
		            if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY) || 
		              (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
		            //TODO: Handling logout
		             // Log.d(IntegratingFacebookTutorialApplication.TAG, "The facebook session was invalidated." + response.getError());
		             // logout();
		            } else {
		            //  Log.d(IntegratingFacebookTutorialApplication.TAG, 
		            //    "Some other error: " + response.getError());
		            }
		          }
		        }
		      }
		    );
		    request.executeAsync();
		  }
	
	 
	 //Add gesture to the Main Activity...

	  @Override
	  public boolean onTouchEvent(MotionEvent event) {
	    if (gestureDetector.onTouchEvent(event)) {
	      return true;
	    }
	    return super.onTouchEvent(event);
	  }

	  private void onLeftSwipe() {
	  }

	  private void onRightSwipe() {
		  sendMessage();
	  }
	  
	  private void onUpSwipe() {
	
	  }
	  

	  
	  
	  private void onDownSwipe() {
		  
	  }
	  

	  public void sendMessage() {
		passingIntent= new Intent(MainActivity.this, SendStorqActivity.class);
		storqText = (EditText) findViewById(R.id.editText);
		message = storqText.getText().toString();
		// Perform action on click
		passingIntent.putExtra("storqm", message);
		passingIntent.putExtra("storqt", true);
		startActivity(passingIntent);
	}


	// Private class for gestures
	  private class SwipeGestureDetector
	          extends SimpleOnGestureListener {
	    // Swipe properties, you can change it to make the swipe
	    // longer or shorter and speed
	    private static final int SWIPE_MIN_DISTANCE = 120;
	    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2,
	                         float velocityX, float velocityY) {
	      try {
	        float diff = e1.getX() - e2.getX();
	        float diffY = e1.getY() - e2.getY();
	        
	        
	        // Left swipe
	        if (diff > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	           MainActivity.this.onLeftSwipe();

	        // Right swipe
	        //only the right Swipe will pass the message... 
	        //The other is not...
	        } else if (-diff > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	        	MainActivity.this.onRightSwipe();
	        
	        // Up swipe 
	        }// else if
	        
	        if (diffY > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	        	MainActivity.this.onUpSwipe();

	        // Right swipe
	        } else if (-diffY > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	        	MainActivity.this.onDownSwipe();
	        }
	        
	        
	      } catch (Exception e) {
	        Log.e("Gesture Activity", "Error on gestures");
	      }
	      return false;
	    }
	  }
	

	  //Retrieving the message...
	  private void retrieveMessages() {
			
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
			query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
			query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> messages, ParseException e) {
					
					if (e == null) {
						// We found messages!
						mMessages = messages;
						listMessage = new String[mMessages.size()];
						parseObjectId = new String[mMessages.size()];
						String[] usernames = new String[mMessages.size()];
						int i = 0;
						for(ParseObject message : mMessages) {
							usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
							listMessage[i] = message.getString("storq");
							parseObjectId[i] = message.getString(ParseConstants.KEY_OBJECT_ID);
							i++;
						}
					}
				}
			});
		}

	  
	  //Show message automatically
		public void sendStorq() {		
			ParseObject message = mMessages.get(0);
			String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);
			ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
			Uri fileUri = Uri.parse(file.getUrl());
			
			if (messageType.equals("text")) {
				String storqText = message.getString("storq");
				String objectId  = message.getString("objectId"); //TODO: create parse constant.
				Intent intent = new Intent(this,GestureActivity.class);
				intent.putExtra("storq", storqText);
				intent.putExtra(ParseConstants.KEY_OBJECT_ID, parseObjectId);	
				intent.putExtra("contributors", message.getString("contributors"));
				
				ParseConstants.deleteMessage(message);		
				startActivity(intent);
			} 

			
		}
	  
	
}
