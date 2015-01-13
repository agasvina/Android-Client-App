package com.harry.storq.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.harry.storq.R;
import com.harry.storq.StorqApplication;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends Activity implements OnItemSelectedListener {
	
	protected EditText mUsername;
	protected EditText mPassword;
	protected EditText mEmail;
	protected Spinner mGender;
	protected Button mSignUpButton;
	protected String gender = "Male";
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_sign_up);
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
		mUsername = (EditText)findViewById(R.id.usernameField);
		mPassword = (EditText)findViewById(R.id.passwordField);
		mEmail = (EditText)findViewById(R.id.emailField);
		mGender = (Spinner) findViewById(R.id.spinner);
		mGender.setOnItemSelectedListener(this);
		
	
		
		
		
		mSignUpButton = (Button)findViewById(R.id.signupButton);
		mSignUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = mUsername.getText().toString();
				String password = mPassword.getText().toString();
				String email = mEmail.getText().toString();
				//String gender = String.valueOf(mGender.getSelectedItem());
				
				username = username.trim();
				password = password.trim();
				email = email.trim();
				gender = gender.trim();
				
				if (username.isEmpty() || password.isEmpty() || email.isEmpty() || gender.isEmpty()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
					builder.setMessage(R.string.signup_error_message)
						.setTitle(R.string.signup_error_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
				else {
					// create the new user!
					setProgressBarIndeterminateVisibility(true);
					
					ParseUser newUser = new ParseUser();
					newUser.setUsername(username);
					newUser.setPassword(password);
					newUser.setEmail(email);
					newUser.put("gender", gender);
					newUser.put("location", "Unknown");
					newUser.put("wallpaper","#ffffff");
					newUser.signUpInBackground(new SignUpCallback() {
						@Override
						public void done(ParseException e) {
							setProgressBarIndeterminateVisibility(false);
							
							if (e == null) {
								// Success!
								StorqApplication.updateParseInstallation(
										ParseUser.getCurrentUser());
								
								Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
							}
							else {
								AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
								builder.setMessage(e.getMessage())
									.setTitle(R.string.signup_error_title)
									.setPositiveButton(android.R.string.ok, null);
								AlertDialog dialog = builder.create();
								dialog.show();
							}
						}
					});
				}
			}
		});
	}



	@Override
	public void onItemSelected(AdapterView<?> adapter, View arg1, int pos,
			long arg3) {
		// TODO Auto-generated method stub
        gender= adapter.getItemAtPosition(pos).toString();
		
		
	}



	@Override
	public void onNothingSelected(AdapterView<?> adapter) {
		// TODO Auto-generated method stub
		// ((TextView) adapter.getChildAt(0)).setTextColor(Color.BLACK);	
		}
}
