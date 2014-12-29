package com.harry.storq.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.harry.storq.R;
import com.harry.storq.utils.ParseConstants;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class GestureActivity extends ListActivity {

	  	TextView text;
	  	private GestureDetector gestureDetector;
		ParseObject currentStorq;
		String objectId;
		String storq;
		Intent chaining;
		TextView another;
		String [] listMessage;
		ArrayList<String> parseObjectId;
		int counter;
		protected SwipeRefreshLayout mSwipeRefreshLayout;
		protected List<ParseObject> mMessages;
		protected Map<String, ParseObject> MapMessages;
		String firstObjectId;
		boolean forwarded;

		
		
		


	  
	  @SuppressWarnings("deprecation")
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_gesture);
			text = (TextView) findViewById(R.id.textView);
	    Intent intent = getIntent();
		text = (TextView) findViewById(R.id.textView);
		firstObjectId = intent.getExtras().getString(ParseConstants.KEY_OBJECT_ID);
		storq = intent.getExtras().getString("storq");
		mMessages = new ArrayList<ParseObject>();
		parseObjectId = new ArrayList<String>();
		String con = intent.getExtras().getString("contributors");
		con += ParseUser.getCurrentUser().getUsername() + ",";
		
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
		query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
		
		MapMessages = new HashMap<String, ParseObject>();
		try {
			mMessages = query.find();
			for(ParseObject m : mMessages) {
				MapMessages.put(m.getObjectId(), m);
				parseObjectId.add(m.getObjectId());
			}
			MapMessages.remove(firstObjectId);
			parseObjectId.remove(firstObjectId);
			counter = parseObjectId.size();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		text.setText(storq);
		gestureDetector = new GestureDetector(
                  new SwipeGestureDetector());

		String []names = (String[]) ParseConstants.parseString(con);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				 android.R.layout.simple_list_item_1, 
				names);
		
		setListAdapter(adapter);

	     chaining = new Intent(GestureActivity.this, RecipientsActivity.class);
	     chaining.putExtra("storqm", storq);
	     chaining.putExtra("storqt", false);
	     chaining.putExtra("forward", true);
	     chaining.putExtra("contributors", con);	
	  
	  }
	  
	  


	
	  
	  
	  

	  /* ... */

	  @Override
	  public boolean onTouchEvent(MotionEvent event) {
	    if (gestureDetector.onTouchEvent(event)) {
	      return true;
	    }
	    return super.onTouchEvent(event);
	  }

	  private void onLeftSwipe() {
		  //text.setText("Left Swipe");
		  finish();
	  
	  }

	  private void onRightSwipe() {
		    startActivity(chaining);
		    finish();

		  
	  }
	  
	  private void onUpSwipe() {
		  
		  if (counter > 0) {
		  
			  	ParseObject dummy = MapMessages.get(parseObjectId.get(counter-1));
			  	String storq = dummy.getString("storq");
			  	String con = dummy.getString("contributors");
			  	con += ParseUser.getCurrentUser().getUsername() + ",";
			  	deleteSender(dummy);

			     chaining = new Intent(GestureActivity.this, RecipientsActivity.class);
			     chaining.putExtra("storqm", storq);
			     chaining.putExtra("storqt", false);
			     chaining.putExtra("forward", true);
			     chaining.putExtra("contributors", con);
			    
				text.setText(storq);
				String []names = (String[]) ParseConstants.parseString(con);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						 android.R.layout.simple_list_item_1, 
						names);
				setListAdapter(adapter);

				counter--;
		  }
	  }



	private void deleteSender(ParseObject dummy) {
		List<String> ids = dummy.getList(ParseConstants.KEY_RECIPIENT_IDS);
			
			if (ids.size() == 1) {
				// last recipient - delete the whole thing!
				dummy.deleteInBackground();
			}
			else {
				// remove the recipient and save
				ids.remove(ParseUser.getCurrentUser().getObjectId());
				
				ArrayList<String> idsToRemove = new ArrayList<String>();
				idsToRemove.add(ParseUser.getCurrentUser().getObjectId());
				
				dummy.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
				dummy.saveInBackground();
			}
	}
	  
	  
	  private void onDownSwipe() {
		  
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
	           GestureActivity.this.onLeftSwipe();

	        // Right swipe
	        } else if (-diff > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	        	GestureActivity.this.onRightSwipe();
	        
	        // Up swipe 
	        }// else if
	        
	        if (diffY > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	           GestureActivity.this.onUpSwipe();

	        // Right swipe
	        } else if (-diffY > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	        	GestureActivity.this.onDownSwipe();
	        }
	        
	        
	      } catch (Exception e) {
	        Log.e("Gesture Activity", "Error on gestures");
	      }
	      return false;
	    }
	  }
	

	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gesture, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
