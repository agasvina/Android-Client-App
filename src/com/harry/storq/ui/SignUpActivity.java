package com.harry.storq.ui;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.harry.storq.R;
import com.harry.storq.StorqApplication;
import com.harry.storq.utils.DatePickerFragment;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends FragmentActivity implements OnItemSelectedListener {
	
	protected EditText mUsername;
	protected EditText mPassword;
	protected EditText mEmail;
	protected Spinner mGender;
	protected Button mSignUpButton;
	protected String gender = "Male";
	protected int year = -1;
	protected int month = -1;
	protected int day = -1;
	protected int currentYear = 2015;
	


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
		
	
		Calendar c = Calendar.getInstance(); 
		currentYear = c.get(Calendar.YEAR);
		
		
		
		
		
		mSignUpButton = (Button)findViewById(R.id.signupButton);
		mSignUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = mUsername.getText().toString();
				String password = mPassword.getText().toString();
				String email = mEmail.getText().toString();				
				username = username.trim();
				password = password.trim();
				email = email.trim();
				gender = gender.trim();
				
				if (username.isEmpty() || password.isEmpty() || email.isEmpty() || gender.isEmpty() || year == -1 || year > 200) {
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
					newUser.put("age",year+"");
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
        gender= adapter.getItemAtPosition(pos).toString();
		
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapter) {	
		}
	
	
	public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "datePicker");
	    year = currentYear - DatePickerFragment.mYear;
	    
	}
}
