package com.harry.storq.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.harry.storq.R;
import com.harry.storq.utils.ParseConstants;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

public class ForgetPasswordActivity extends Activity {
	protected Button request;
  	protected EditText email;
  	protected String mEmail;
  	protected String message01;
  	protected String message02;
  	
	protected GestureDetector gestureDetector;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forget_password);
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		//as usual the font
		Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/GOTHICB.TTF");
		 this.message01 = this.getString(R.string.email_verify_01);
		 this.message02 = this.getString(R.string.email_verfy_02);
		
		gestureDetector = new GestureDetector(
                  new SwipeGestureDetector());
		
		email = (EditText) findViewById(R.id.email);
		email.setTypeface(tf);
		// Give dialog about request

		request = (Button) findViewById(R.id.request);
		request.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {		
            	//get the email. 
        		String mEmail = email.getText().toString();	
        		mEmail = mEmail.trim();   		
        		//send password error request 
        		if (mEmail.isEmpty()) {
        			AlertDialog.Builder builder = new AlertDialog.Builder(ForgetPasswordActivity.this);
        			builder.setMessage(R.string.forget_error_message)
        				.setTitle(R.string.forget_error_title)
        				.setPositiveButton(android.R.string.ok, null);
        			AlertDialog dialog = builder.create();
        			dialog.show();
        			
        		} else {
				 
        			ParseUser.requestPasswordResetInBackground(mEmail,
				                            new RequestPasswordResetCallback() {
        					public void done(ParseException e) {
									if (e == null) {
										String notification = message01 + message02;
					        			AlertDialog.Builder builder = new AlertDialog.Builder(ForgetPasswordActivity.this);
					        			builder.setMessage(notification)
					        				.setTitle(R.string.forget_title_notif)
					        				.setPositiveButton(android.R.string.ok, null);
					        			AlertDialog dialog = builder.create();
					        			dialog.show();
									} else {
									// Something went wrong. Look at the ParseException to see what's up.
									}
									}
			        		});//end of parse request 
        			} //end of else
        	   		}
        		}); //end of onClick button request
	
	} //end of onCreate

	  @Override
	  public boolean onTouchEvent(MotionEvent event) {
		  if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
		  }
	    if (gestureDetector.onTouchEvent(event)) {
	      return true;
	    }
	    return super.onTouchEvent(event);
	  }

	  private void onLeftSwipe() {
	  }

	  private void onRightSwipe() {
	  }
	  
	  private void onUpSwipe() {
		  
	  }

	  
	  private void onDownSwipe() {
		  
	  }

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.forget_password, menu);
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
	        	ForgetPasswordActivity.this.onLeftSwipe();

	        // Right swipe
	        } else if (-diff > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	        	ForgetPasswordActivity.this.onRightSwipe();
	        }
	        
	        if (diffY > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	        	ForgetPasswordActivity.this.onUpSwipe();

	        } else if (-diffY > SWIPE_MIN_DISTANCE
	        && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	        	ForgetPasswordActivity.this.onDownSwipe();
	        }
	        
	        
	      } catch (Exception e) {
	        Log.e("Gesture Activity", "Error on gestures");
	      }
	      return false;
	    }
	  }
	    
	    
}
