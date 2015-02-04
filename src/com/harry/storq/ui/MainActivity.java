package com.harry.storq.ui;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.harry.storq.utils.ColorWheel;
import com.harry.storq.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;


public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener,
GestureDetector.OnGestureListener,
GestureDetector.OnDoubleTapListener
{
	
	public static final String TAG = MainActivity.class.getSimpleName();	
	protected static Double Longitude;
	protected static Double Latitude;
	
	//Add gesture activity
  	protected GestureDetector gestureDetector;
  	protected Intent passingIntent;
  	protected EditText storqText;
  	protected String message = "";
  	
  	//add ability to choose random user.
	protected List<ParseUser> mUsers;
	
	
	//This is for string builder for the address.
	protected static StringBuilder sb;
	protected static String Location;
	
	
	//Wallpaper 
	protected RelativeLayout relativLayout;
	protected int colorCounter = 0;
	protected int[] colorArray;
	
	
	//Show message automatically...
	public static List<ParseObject> mMessages;
	protected SwipeRefreshLayout mSwipeRefreshLayout;


    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

	
	protected DialogInterface.OnClickListener mDialogListener = 
			new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch(which) {
//				case 0: 
//				    Intent intent = new Intent(MainActivity.this,FeedbackActivity.class);
//				    startActivityForResult(intent,1);
//					break;
				case 0:
					ParseUser.logOut();
					navigateToLogin();				
					break;

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

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
    private GestureDetectorCompat mDetector; 

	@SuppressLint("ResourceAsColor") @SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		ActionBar actionBar = getActionBar();
		actionBar.hide();


		Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/GOTHICB.TTF");
		
		storqText = (EditText) findViewById(R.id.editText);
		storqText.setTypeface(tf);
        buildGoogleApiClient();      


	      mDetector = new GestureDetectorCompat(this,this);
	      mDetector.setOnDoubleTapListener(this);
		
	    
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser == null) {
		navigateToLogin();
		}
		else if (!currentUser.getBoolean("usingFB") && !currentUser.getBoolean("emailVerified"))
		{
		navigateToLogin();
		} else {
		}
		
		if (ParseUser.getCurrentUser() != null) {

			//setting up the wallpaper.
			relativLayout = (RelativeLayout) findViewById(R.id.pager);
			if(ParseUser.getCurrentUser().getString("wallpaper") != null) {
				relativLayout.setBackgroundColor(Color.parseColor(ParseUser.getCurrentUser().getString("wallpaper")));		
			} else {
				relativLayout.setBackgroundColor(Color.parseColor("#ffffff"));		
			}
			
	        if(Location == null) {
	        	Location = ParseUser.getCurrentUser().getString("location");
	        }
			
	        refresh();
			
			//Gesture Activity
			gestureDetector = new GestureDetector(
	                new SwipeGestureDetector());		
		}
	
	} //End of OnCreate Method

	
	@Override
	public void onResume()
	{
		super.onResume();
	}

	public void refresh() {
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
		query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> message, ParseException e) {		
				if (e == null) {
					Intent intent = new Intent(MainActivity.this, Gesture2Activity.class);
					intent.putExtra("Location", MainActivity.Location);
					startActivity(intent);
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setMessage(e.getMessage())
						.setTitle(R.string.error_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		});
	}
	
	
	private void navigateToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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

	
	
	  private void getLocation(double latitude, double longitude) {      
			String forecastUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
								 +latitude+ ","
								 + longitude +"&sensor=true&types=(cities)";

	        if (isNetworkAvailable()) {

	            OkHttpClient client = new OkHttpClient();
	            com.squareup.okhttp.Request request = new  com.squareup.okhttp.Request.Builder()
	                    .url(forecastUrl)
	                    .build();

	            Call call = client.newCall(request);
	            call.enqueue(new Callback() {
	                @Override
	                public void onFailure( com.squareup.okhttp.Request request, IOException e) {
	                }

	                @Override
	                public void onResponse( com.squareup.okhttp.Response response) throws IOException {
	                    try 
	                    {
	                        String jsonData = response.body().string();	 
	                        JSONObject forecast = new JSONObject(jsonData);
	                        JSONArray array = forecast.getJSONArray("results");
	                        JSONArray x = array.getJSONObject(0).getJSONArray("address_components");
	                        int size = x.length();
	                        sb = new StringBuilder();
	                 
	                        int i = 0;
	                        while(i  < size) {
		                       JSONArray xarr = x.getJSONObject(i).getJSONArray("types");
		                       String s = xarr.getString(0);
		                       if (s.equalsIgnoreCase("country") || s.equalsIgnoreCase("postal_town")) {
			                       sb.append(x.getJSONObject(i).getString("short_name"));
			                       sb.append(" ");
		                       }
		                       i++;
	                        }
	                        
	                        MainActivity.Location = MainActivity.sb.toString();
	                        if (response.isSuccessful()) {
		                        MainActivity.Location = MainActivity.sb.toString();
	                        } else {
	                        }
	                    }
	                    catch (IOException e) {
	                    } catch (JSONException e) {
						}
	                }
	            });
	        }
	        else {
	        
	        }
	    }


	private void checkMessage(final String Message) {     
		    	final String newMsg = Message.replaceAll(" ", "%20");
				String profanity = "http://www.wdyl.com/profanity?q="+newMsg;
	        if (isNetworkAvailable()) {


	            OkHttpClient client = new OkHttpClient();
	            com.squareup.okhttp.Request request = new  com.squareup.okhttp.Request.Builder()
	                    .url(profanity)
	                    .build();

	            Call call = client.newCall(request);
	            call.enqueue(new Callback() {
	                @Override
	                public void onFailure( com.squareup.okhttp.Request request, IOException e) {            
	                }

	                @Override
	                public void onResponse( com.squareup.okhttp.Response response) throws IOException {
	                    try 
	                    {
	                        String jsonData = response.body().string();	
	                        JSONObject object = new JSONObject(jsonData);
	                        final boolean prof = object.getBoolean("response");	                        
	                        message = Message;
	                        runOnUiThread(new Runnable() {
     	                        @Override

     	                        public void run() {
        	                        if(!prof) {
     		                        sendMessage(message);
        	                        } else { 
        	        					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        	        					builder.setMessage(R.string.profanity_error)
        	        						.setTitle(R.string.error_title)
        	        						.setPositiveButton(android.R.string.ok, null);
        	        					AlertDialog dialog = builder.create();
        	        					dialog.show();	                        	
        	                        }
     	                        }
    	                    });
	                        
	                        if (response.isSuccessful()) {
	                        	
	                        } else {
	                        }
	                    }
	                    catch (IOException e) {
	                    	 e.printStackTrace();
	                    } catch (JSONException e) {
	                    	 e.printStackTrace();

						}
	                }
	            });
	        }
	        else {
	        
	        	try {
	        		
	        	}catch (Exception e) {
	        		
	        	}
	        }
	    }

	  
	  
	  
	

	 private boolean isNetworkAvailable() {
	      ConnectivityManager manager = (ConnectivityManager)
	              getSystemService(Context.CONNECTIVITY_SERVICE);
	      NetworkInfo networkInfo = manager.getActiveNetworkInfo();
	      boolean isAvailable = false;
	      if (networkInfo != null && networkInfo.isConnected()) {
	          isAvailable = true;
	      }
	
	      return isAvailable;
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
		              String ages = user.getBirthday().substring(user.getBirthday().length()-4);
		              Calendar c = Calendar.getInstance(); 
		      		  int currentYear = c.get(Calendar.YEAR);
		      		  int realAge =  currentYear - Integer.parseInt(ages);
		              currentUser.put("age", realAge+ "");
		              userProfile.put("facebookId", user.getId());
		              userProfile.put("name", user.getName());
		              currentUser.put(ParseConstants.KEY_USERNAME, user.getName());
		              currentUser.put("usingFB", true);
		              currentUser.put("wallpaper","#ffffff");
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
		    
	        if(event.getAction() == MotionEvent.ACTION_DOWN)
	            handler.postDelayed(mLongPressed, 1000);
	        if((event.getAction() == MotionEvent.ACTION_MOVE)||(event.getAction() == MotionEvent.ACTION_UP))
	        {  handler.removeCallbacks(mLongPressed);

	        }
	   
		    return super.onTouchEvent(event);
		  }


	  private void onLeftSwipe() {
			if(colorCounter > 8) {
				colorCounter = 0; 
			}
			relativLayout.setBackgroundColor(Color.parseColor(ColorWheel.mColors[colorCounter]));
			ParseUser.getCurrentUser().put("wallpaper", ColorWheel.mColors[colorCounter]);
			ParseUser.getCurrentUser().saveInBackground();
			colorCounter++;		
			
	  }

	  private void onRightSwipe() {
			Toast.makeText(MainActivity.this, R.string.searching_storq, Toast.LENGTH_SHORT).show();
			refresh();
			
	  }
	  
	  private void onUpSwipe() {
			message = storqText.getText().toString();
		    checkMessage(message);
	  }
	  

	  
	  
	  private void onDownSwipe() {
			AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
			builder2.setItems(R.array.text_choices, mDialogListener);
			AlertDialog dialog2 = builder2.create();
			dialog2.show();
	  }
	  

	  @SuppressLint("DefaultLocale") 
	  public void sendMessage(String msg) {
		passingIntent= new Intent(MainActivity.this, SendStorqActivity.class);
		message = msg.toLowerCase();
		// Perform action on click
		passingIntent.putExtra("storqm", message);
		passingIntent.putExtra("storqt", true);
		
		if (Location != null) {
			passingIntent.putExtra("Location", Location);
		} else {
			passingIntent.putExtra("Location", ParseUser.getCurrentUser().getString("location"));
		}
		
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
	        	MainActivity.Latitude = mLastLocation.getLatitude();
	        	MainActivity.Longitude = mLastLocation.getLongitude();
	            getLocation(Latitude, Longitude);
	            //update the location
	            if (MainActivity.Location != null) {
	            	ParseUser.getCurrentUser().put("location", MainActivity.Location);
	            	ParseUser.getCurrentUser().saveInBackground();
	            	
	            }
	        }
	        
	        
	    
	    }

	   
	
	    
	    
	    @Override
	    public void onConnectionFailed(ConnectionResult result) {
	    }

	    public void onDisconnected() {
	    }

	    @Override
	    public void onConnectionSuspended(int cause) {
	        mGoogleApiClient.connect();
	    }
  
	    
	    
	    final Handler handler = new Handler(); 
	    Runnable mLongPressed = new Runnable() { 
	        public void run() { 
				AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
				builder2.setItems(R.array.text_choices, mDialogListener);
				AlertDialog dialog2 = builder2.create();
				dialog2.show();
				
	        }   
	    };

	    
	    @Override
	    public void onLongPress(MotionEvent event) {
	      
	    }

		@Override
		public boolean onDoubleTap(MotionEvent arg0) {
	
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onDown(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onShowPress(MotionEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}
	    
	    
	
	
}
