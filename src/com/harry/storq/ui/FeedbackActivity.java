package com.harry.storq.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.harry.storq.R;
import com.harry.storq.utils.ParseConstants;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class FeedbackActivity extends Activity {
	
	String current;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//set the text editor and button
		Button button = (Button) findViewById(R.id.getTextButton);
		button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	EditText editText = (EditText) findViewById(R.id.editText);
        	    String feeds = editText.getText().toString();
        	    ParseObject message = new ParseObject(ParseConstants.CLASS_FEEDBACK);
        		message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        		message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        		message.put(ParseConstants.KEY_EMAIL, ParseUser.getCurrentUser().getEmail());
        		message.put(ParseConstants.KEY_FEED_MESSAGE, feeds);
        		//Save feedback to the background...
        		message.saveInBackground(new SaveCallback() {
        			@Override
        			public void done(ParseException e) {
        				if (e == null) {
        					// success!
        					Toast.makeText(FeedbackActivity.this, R.string.feedback_respond, Toast.LENGTH_LONG).show();
        				}
        				else {
        					AlertDialog.Builder builder = new AlertDialog.Builder(FeedbackActivity.this);
        					builder.setMessage(R.string.error_sending_message)
        						.setTitle(R.string.error_selecting_file_title)
        						.setPositiveButton(android.R.string.ok, null);
        					AlertDialog dialog = builder.create();
        					dialog.show();
        				}
        			}
        		});
        		
        	    finish();
            }
        });
		
		getMenuInflater().inflate(R.menu.get, menu);
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
