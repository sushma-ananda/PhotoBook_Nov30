package com.example.photobook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.photobook.PictureStream;
import com.example.photobook.StartScreen.Login;
import com.example.photobook.util.JSONParser;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

//FOR DEMO - use as method in general
public class Photo extends Service {
	ImageLoader imageLoader;
	DisplayImageOptions displayOptions;
	String photoCaption, photoName, photoPath, timeStamp, gpsLocation, locAltitude, locTemp, uri, directory;
	private static String url_uploadPhoto = "http://cis-linux2.temple.edu/~tuc69409/uploadPhoto.php";
    final Context context = this;
    JSONParser jsonParser = new JSONParser();
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    
	@Override
	public void onCreate(){
				
		imageLoader = ImageLoader.getInstance();
		
		displayOptions = new DisplayImageOptions.Builder()
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		File cacheDir = StorageUtils.getCacheDirectory(this);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .threadPoolSize(3)
        .threadPriority(Thread.NORM_PRIORITY - 1)
        .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // 2 MBs
        .discCache(new UnlimitedDiscCache(cacheDir))
        .discCacheSize(50 * 1024 * 1024) // 50 MBs
        .defaultDisplayImageOptions(displayOptions)
        .build();
		ImageLoader.getInstance().init(config);
	
	}
	
	@Override
	public void onStart(Intent fromPictureEditor, int startId) {
		

		uri = (String) fromPictureEditor.getExtras().get("uri");
		photoCaption = (String) fromPictureEditor.getExtras().get("photoCaption");
		photoName = (String) fromPictureEditor.getExtras().get("photoName");
		directory = (String) fromPictureEditor.getExtras().get("directory");
		photoPath = (String) fromPictureEditor.getExtras().get("photoPath");
		timeStamp = (String) fromPictureEditor.getExtras().get("timeStamp");
		gpsLocation =(String) fromPictureEditor.getExtras().get("gpsLocation");
		locAltitude = (String) fromPictureEditor.getExtras().get("locAltitude");
		locTemp = (String) fromPictureEditor.getExtras().get("locTemp");
		
		Log.i("in service class", uri);
		new uploadPhoto().execute();
		this.stopSelf();
	}
	
	class uploadPhoto extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... args) {
			
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
		//	params.add(new BasicNameValuePair("userName", userName));
		//	params.add(new BasicNameValuePair("userPwd", userPwd));
			
			// getting JSON Object
			// Note that create product url accepts POST method
			JSONObject json = jsonParser.makeHttpRequest(url_uploadPhoto, "POST", params);
			
			// check log cat from response
			Log.d("Create Response", json.toString());
			
			String result = null;
			// check for success tag
			try {
				int success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					
					String welcomemessage = json.getString(TAG_MESSAGE);
					/*Open login screen*/
					Intent openStream = new Intent(Photo.this, PictureStream.class);
					openStream.putExtra("welcome", welcomemessage);
					startActivity(openStream);
					
					result = welcomemessage;
				} else result = json.getString(TAG_MESSAGE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return result;	
		}
		
		//After completing background task Dismiss the progress dialog
		
		protected void onPostExecute(String result) {
		
		}
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
