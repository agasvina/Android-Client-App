package com.harry.storq.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.harry.storq.R;

public class GetActivity extends Activity {
	
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
        	    Intent intent = new Intent(GetActivity.this, RecipientsActivity.class);
        	    EditText editText = (EditText) findViewById(R.id.editText);
        	    String message = editText.getText().toString();
        	    intent.putExtra("storqm", message);
        	    intent.putExtra("storqt", true);
        	    startActivity(intent);
        	    finish();
            }
        });

	    

		//get the intent
		
		
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
