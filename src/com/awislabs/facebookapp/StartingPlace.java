package com.awislabs.facebookapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.Facebook.DialogListener;

public class StartingPlace extends Activity implements OnClickListener {

	private String APP_ID;
	Facebook fb;
	ImageView pic, button;
	SharedPreferences sp;
	TextView welcome;
	String userName, userId;
	Bitmap bmp;
	Button postbtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_starting_place);
		
		APP_ID = getString(R.string.APP_ID);
		fb = new Facebook(APP_ID);
		
		sp = getPreferences(MODE_PRIVATE);
		String access_token = sp.getString("access_token", null);
		long expires = sp.getLong("access_expires", 0);
		
		if (access_token != null){
			fb.setAccessToken(access_token);
		}
		
		if (expires != 0){
			fb.setAccessExpires(expires); 
		}
		
		postbtn = (Button)findViewById(R.id.postbtn);
		welcome = (TextView)findViewById(R.id.welcometxt);
		button = (ImageView) findViewById(R.id.login);
		pic = (ImageView) findViewById(R.id.profile_pic);
		button.setOnClickListener(this);
		postbtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle parameters = new Bundle();
				parameters.putString("link", "http://awislabs.com");
				fb.dialog(StartingPlace.this, "feed", parameters, new DialogListener() {
					
					@Override
					public void onFacebookError(FacebookError e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onError(DialogError e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onComplete(Bundle values) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onCancel() {
						// TODO Auto-generated method stub
						
					}
				});
			}
		});
		updateButtonImage();
		
		
	}
	
	private void updateButtonImage(){
		if(fb.isSessionValid()){
			button.setImageResource(R.drawable.logout_button);
			pic.setVisibility(View.VISIBLE);
			postbtn.setVisibility(View.VISIBLE);
			new AfterLoginThread().execute(); //set name, id and profile image
			
		}else {
			button.setImageResource(R.drawable.login_button);
			pic.setVisibility(View.INVISIBLE);
			postbtn.setVisibility(View.INVISIBLE);
			welcome.setText(getString(R.string.hello_world));
		}
	}

	@Override
	public void onClick(View v) {
		if (fb.isSessionValid()) {
			// button close session - logout fb
			new logoutThread().execute();
		} else {
			// login to fb
			fb.authorize(StartingPlace.this, new String[] {"email"}, new DialogListener() {
				 
				@Override
				public void onFacebookError(FacebookError e) {
					Toast.makeText(StartingPlace.this, "facebook error", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
				
				@Override
				public void onError(DialogError e) {
					Toast.makeText(StartingPlace.this, "onError", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					
				}
				
				@Override
				public void onComplete(Bundle values) {
					Editor editor = sp.edit();
					editor.putString("access_token", fb.getAccessToken());
					editor.putLong("access_expires", fb.getAccessExpires());
					editor.commit();
					updateButtonImage();
					
				}
				
				@Override
				public void onCancel() {
					Toast.makeText(StartingPlace.this, "cancelled", Toast.LENGTH_SHORT).show();
				}
			});
		}

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fb.authorizeCallback(requestCode, resultCode, data);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_starting_place, menu);
		return true;
	}
	
	class logoutThread extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				fb.logout(getApplicationContext());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			Editor editor = sp.edit();
			editor.putString("access_token", null);
			editor.putLong("access_expires", 0);
			editor.commit();
			updateButtonImage();
		}
		
	}
	
	class AfterLoginThread extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			
			JSONObject object = null;
			URL img_url = null;
			String jsonUser;
			
			try {
				jsonUser = fb.request("me");
				object = Util.parseJson(jsonUser);
				userId = object.optString("id");
				userName = object.optString("name");	
				img_url = new URL("http://graph.facebook.com/"+userId+"/picture?type=large");
				bmp = BitmapFactory.decodeStream(img_url.openConnection().getInputStream());
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (FacebookError e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			welcome.setText("Welcome, " + userName + ", id = "+userId);
			pic.setImageBitmap(bmp);
		}
	}
	
	class postThread extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}
	}



}
