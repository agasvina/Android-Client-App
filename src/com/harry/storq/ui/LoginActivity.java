package com.harry.storq.ui;

import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.harry.storq.R;
import com.harry.storq.StorqApplication;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class LoginActivity extends Activity {

	protected EditText mUsername;
	protected EditText mPassword;
	protected Button mLoginButton;

	
	protected TextView mSignUpTextView;
	protected TextView mForgetPassword;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_login);
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
	    ParseFacebookUtils.initialize(getString(R.string.facebook_id));
		mSignUpTextView = (TextView)findViewById(R.id.signUpText);
		mSignUpTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
				startActivity(intent);
			}
		});
		
		mForgetPassword = (TextView) findViewById(R.id.forget);
		mForgetPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
				startActivity(intent);
			}
		});
		

		Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/GOTHICB.TTF");
		mSignUpTextView.setTypeface(tf);

		
		
		mUsername = (EditText)findViewById(R.id.usernameField);
		mPassword = (EditText)findViewById(R.id.passwordField);
		mLoginButton = (Button)findViewById(R.id.loginButton);
		
		
		mUsername.setTypeface(tf);
		mPassword.setTypeface(tf);
		mLoginButton.setTypeface(tf);


		mLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = mUsername.getText().toString();
				String password = mPassword.getText().toString();
				
				username = username.trim();
				password = password.trim();
				
				if (username.isEmpty() || password.isEmpty()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
					builder.setMessage(R.string.login_error_message)
						.setTitle(R.string.login_error_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
				else {
					setProgressBarIndeterminateVisibility(true);
					
					ParseUser.logInInBackground(username, password, new LogInCallback() {
						@Override
						public void done(ParseUser user, ParseException e) {
							setProgressBarIndeterminateVisibility(false);
							
							if (e == null) {
								StorqApplication.updateParseInstallation(user);								
								Intent intent = new Intent(LoginActivity.this, MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
							}
							else {
								AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
								builder.setMessage(e.getMessage())
									.setTitle(R.string.login_error_title)
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


	
//
//	  @Override
//	  public void onActivityResult(int requestCode, int resultCode, Intent data) {
//	    super.onActivityResult(requestCode, resultCode, data);
//	    ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
//	  }
//
//	  public void onLoginClick(View v) {
//	    
//	    List<String> permissions = Arrays.asList("public_profile", "email","user_location","user_birthday");
//	  
//	    ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
//	      @Override
//	      public void done(ParseUser user, ParseException err) {
//	        if (user == null) {
//	        } else if (user.isNew()) {
//	          showUserDetailsActivity();
//	        } else {
//	          showUserDetailsActivity();
//	        }
//	      }
//	    });
//	  } 
//
//		  private void showUserDetailsActivity() {
//			    Intent intent = new Intent(this, MainActivity.class);
//			    startActivity(intent);
//		  
//		  }
//	
	
	


}


