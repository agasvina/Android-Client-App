package com.harry.storq.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.harry.storq.R;
import com.harry.storq.adapters.SectionsPagerAdapter;
import com.harry.storq.utils.GPSTracker;
import com.harry.storq.utils.ParseConstants;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
	
	public static final String TAG = MainActivity.class.getSimpleName();
	
	public static final int TAKE_PHOTO_REQUEST = 0;
	public static final int TAKE_VIDEO_REQUEST = 1;
	public static final int PICK_PHOTO_REQUEST = 2;
	public static final int PICK_VIDEO_REQUEST = 3;
	
	public static final int MEDIA_TYPE_IMAGE = 4;
	public static final int MEDIA_TYPE_VIDEO = 5;
	public static final int MEDIA_TYPE_TEXT = 6;	
	public static final int FILE_SIZE_LIMIT = 1024*1024*10; // 10 MB
	
	protected Uri mMediaUri;
	protected static String Longitude;
	protected static String Latitude;
	
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

	
	//added the location:
	//TODO: refractor later

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected TextView mLatitudeText;
    protected TextView mLongitudeText;

	
	
	
	protected DialogInterface.OnClickListener mDialogListener = 
			new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch(which) {
				case 0: 
				    Intent intent = new Intent(MainActivity.this,FeedbackActivity.class);
				    startActivityForResult(intent,1);
					break;
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

	private Button btnShowLocation;
	GPSTracker gps;


	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		
		//this is for the location

        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));

        buildGoogleApiClient();

		
		
		
		   btnShowLocation = (Button) findViewById(R.id.show_location);
	        
	        btnShowLocation.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					
					
					
					
					gps = new GPSTracker(MainActivity.this);
					
					if(gps.canGetLocation()) {
						double latitude = gps.getLatitude();
						double longitude = gps.getLongitude();
						
						Toast.makeText(
								getApplicationContext(),
								"Your Location is -\nLat: " + latitude + "\nLong: "
										+ longitude, Toast.LENGTH_LONG).show();
					} else {
						gps.showSettingsAlert();
					}
				}
			});

		
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
		try {
			if(query.find().size() > 0) {
				Intent intent = new Intent(MainActivity.this, Gesture2Activity.class);
				startActivity(intent);
			}
		} catch (ParseException e) {

		}
		}

	@Override
	public void onResume()
	{
		super.onResume();
		refresh();
        

	
	}

	public void refresh() {

		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
		query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
		try {
			if(query.find().size() > 0) {
				Intent intent = new Intent(MainActivity.this, Gesture2Activity.class);
				startActivity(intent);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			case R.id.action_mail:
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setItems(R.array.text_choices, mDialogListener);
				AlertDialog dialog2 = builder2.create();
				dialog2.show();
		}
		
		return super.onOptionsItemSelected(item);
	}

	
	 private void makeMeRequest() {
		    Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
		      new Request.GraphUserCallback() {
		        @Override
		        public void onCompleted(GraphUser user, Response response) {
		          if (user != null) {
		            ParseUser currentUser = ParseUser.getCurrentUser();
		            JSONObject userProfile = new JSONObject();
		            try {
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

		            
		            } catch (JSONException e) {
		            	//ERROR PARSING RETURN DATA
		            }

		          } else if (response.getError() != null) {
		            if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY) || 
		              (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
		            } else {
		            	//NOT SURE
		            }
		          }
		        }
		      }
		    );
		    request.executeAsync();
		  }
	

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
			Toast.makeText(MainActivity.this, R.string.searching_storq, Toast.LENGTH_SHORT).show();
			refresh();
			
	  }
	  
	  private void onUpSwipe() {
		  sendMessage();
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
		storqText.setText("");
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
	        } else if (-diff > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	        	MainActivity.this.onRightSwipe();
	        
	        }
	        
	        if (diffY > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	        	MainActivity.this.onUpSwipe();
	        } else if (-diffY > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	        	MainActivity.this.onDownSwipe();
	        }
	        
	        
	      } catch (Exception e) {
	    	  //ERROR GESTURE ACTIVITY
	      }
	      return false;
	    }
	  }
	
	  /**
	     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
	     */
	    protected synchronized void buildGoogleApiClient() {
	        mGoogleApiClient = new GoogleApiClient.Builder(this)
	                .addConnectionCallbacks(this)
	                .addOnConnectionFailedListener(this)
	                .addApi(LocationServices.API)
	                .build();
	    }

	    @Override
	    protected void onStart() {
	        super.onStart();
	        mGoogleApiClient.connect();
	    }

	    @Override
	    protected void onStop() {
	        super.onStop();
	        if (mGoogleApiClient.isConnected()) {
	            mGoogleApiClient.disconnect();
	        }
	    }

	    /**
	     * Runs when a GoogleApiClient object successfully connects.
	     */
	    @Override
	    public void onConnected(Bundle connectionHint) {
	        // Provides a simple way of getting a device's location and is well suited for
	        // applications that do not require a fine-grained location and that do not need location
	        // updates. Gets the best and most recent location currently available, which may be null
	        // in rare cases when a location is not available.
	        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
	        if (mLastLocation != null) {
	        	Latitude = String.valueOf(mLastLocation.getLatitude());
	        	Longitude = String.valueOf(mLastLocation.getLongitude());
	            mLatitudeText.setText(Latitude);
	            mLongitudeText.setText(Longitude);
	        }
	    }

	    @Override
	    public void onConnectionFailed(ConnectionResult result) {
	        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
	        // onConnectionFailed.
	        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
	    }

	    /*
	    * Called by Google Play services if the connection to GoogleApiClient drops because of an
	    * error.
	    */
	    public void onDisconnected() {
	        Log.i(TAG, "Disconnected");
	    }

	    @Override
	    public void onConnectionSuspended(int cause) {
	        // The connection to Google Play services was lost for some reason. We call connect() to
	        // attempt to re-establish the connection.
	        Log.i(TAG, "Connection suspended");
	        mGoogleApiClient.connect();
	    }
  
	
	
}
