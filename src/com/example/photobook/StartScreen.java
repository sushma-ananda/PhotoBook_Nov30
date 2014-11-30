package com.example.photobook;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.photobook.SignUp.CreateNewUser;
import com.example.photobook.util.JSONParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartScreen extends Activity {
	
	
	/* OVERAL TO DO:
	 
	set left margins of each widget so that they are relative to background image and
	work for all screens
	
	*/
	
	/*Name Widgets*/
	Button signUp, login;
	EditText username, password;
	String userName, userPwd;
    // Progress Dialog
    private ProgressDialog pDialog;
 
    private static String url_login = "http://cis-linux2.temple.edu/~tuc69409/login.php";
    final Context context = this;
    
    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_main);
		
		/*Initialize widgets*/
		signUp = (Button) findViewById(R.id.signUp);
		login = (Button) findViewById(R.id.login);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		
		 /*Sign up button clicked*/
		 signUp.setOnClickListener(new View.OnClickListener() {
			
			@Override
		 public void onClick(View v) {
				/*Open sign up form*/
				Intent openSignUp = new Intent(StartScreen.this, SignUp.class);
				startActivity(openSignUp);
			}
		 });
		 
		 /*Login button clicked*/
		 login.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
								
					/*Get user input*/
					userName = username.getText().toString();
					userPwd = password.getText().toString();
					
					 if(!TextUtils.isEmpty(userName) && userName.length() >= 6 && !TextUtils.isEmpty(userPwd) && userPwd.length() >= 6) {				 
						new Login().execute();
					 } else {
						showDialog("Login to PhotoBook", "User name and password has to be at least 6 character long"); 
					 }
			
				}
			});		
	}
	
	/**
	†Background Async Task to login
	†* */
	class Login extends AsyncTask<String, String, String> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(StartScreen.this);
			pDialog.setMessage("Signing in..");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();	
		}
		
		@Override
		protected String doInBackground(String... args) {
		
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userName", userName));
			params.add(new BasicNameValuePair("userPwd", userPwd));
		
			// getting JSON Object
			// Note that create product url accepts POST method
			JSONObject json = jsonParser.makeHttpRequest(url_login, "GET", params);
			String result = null;
			// check for success tag
			try {
				int success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					
					
					String welcomemessage = "Login Successful";
					result = welcomemessage;
					/*Open login screen*/
					Intent openPictureBook = new Intent(StartScreen.this, PictureStream.class);
					openPictureBook.putExtra("userName", userName);
					openPictureBook.putExtra("userID", Integer.parseInt(json.getString(TAG_MESSAGE)));
					openPictureBook.putExtra("welcome", welcomemessage);
					startActivity(openPictureBook);
					
					Log.i(INPUT_SERVICE, json.getString(TAG_MESSAGE) + " " + userName);
					
					finish();
					
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}
		
		//After completing background task Dismiss the progress dialog
		
		protected void onPostExecute(String result) {
			pDialog.dismiss();
			
			if (result != "Login Successful") {
				showDialog("Login to PhotoBook" , "Incorrect UserName/Password");
			}
		}
		
	}
	
	private void showDialog(String title, String message) {
		
		AlertDialog.Builder aDialog = new AlertDialog.Builder(StartScreen.this);
					// set title
		aDialog.setTitle(title);
		
		// set dialog message
		aDialog
		.setMessage(message)
		.setCancelable(false)
		.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
		}
		});
		// create alert dialog
		AlertDialog alertDialog = aDialog.create();
		// show it
		alertDialog.show();
		Toast.makeText(StartScreen.this, message, Toast.LENGTH_LONG).show();
	}
	
}
