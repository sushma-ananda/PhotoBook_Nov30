package com.example.photobook;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder;
import com.nostra13.universalimageloader.utils.StorageUtils;
//import com.example.photobook.R;

import com.example.photobook.UploadService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

/*Once picture is taken, this screen will display picture and information. It will allow user to 
 * write a caption and then save the picture. User clicks save to send all information to database. User
 * can also cancel to return to the camera to take a different picture.
 */

public class PictureEditor extends Activity implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	
	EditText captionField;
	ImageView photoView;

	 


	File photo;
	String photoCaption, photoName, photoPath, timeStamp, gpsLocation, locAltitude, locTemp;
 	private String photoUri;
	LocationClient locationClient;
	Location loc;
	String userName;
	int userID;
	Thread weatherThread;
	
	String url = "http://forecast.weather.gov/MapClick.php?lat=";
	
	//Handler for weather reading
final Handler showContent = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//Load stock quotes
			String result = (String) msg.obj;
			double finalTemperature =0;
		
				JSONObject jsonObject;
				
				try {
					jsonObject = new JSONObject(result);
					JSONObject currentObj = jsonObject.getJSONObject("currentobservation");
					locTemp = currentObj.getString("Temp") + "F";
					JSONObject locObj = jsonObject.getJSONObject("location");
					locAltitude = locObj.getString("elevation")+"ft";
					Log.i(INPUT_SERVICE, locTemp + locAltitude);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return false;
		}
});

	
	/*Create menu with save and delete*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.editor, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			saveClicked();
			return true;
		case R.id.delete:
			delete();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picture_editor);
		locationClient = new LocationClient(this, (ConnectionCallbacks) this, this);
		
		/*Initialize image loader*/
		ImageLoader imageLoader;
		DisplayImageOptions displayOptions;
		
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
		
		
		/*Initialize caption field and layout*/
		captionField = (EditText) findViewById(R.id.captionText);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.pictureEditorLayout);

	
		
		/*Get photo from intent*/
		photoUri = getIntent().getStringExtra("photoUri");
		photoName = getIntent().getStringExtra("photoName");
		File storageDirectory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));
		//photo = new File(photoString); // Temporary file name
		userID = getIntent().getIntExtra("userID",0);
		userName = getIntent().getStringExtra("userName");
		
	
	String fileName = photoName + ".jpg";
	photo = new File(photoUri); // Temporary file name
	photoName = userID + fileName;
	String imageUri = getIntent().getStringExtra("imageUri");
	
		/* Display photo */
		photoView = new ImageView(this);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
		RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		//lp.addRule(RelativeLayout.ALIGN_TOP, R.id.pictureEditorLayout);
		lp.setMargins(180, 0, 180, 430);
		
		photoView.setLayoutParams(lp);
		
		ImageLoader.getInstance().displayImage(imageUri, photoView);

		layout.addView(photoView);	
	
	}
	
	/*Save caption and take picture to information gathering service*/
	private void saveClicked(){
		photoCaption = captionField.getText().toString();
		photoPath = "../photobook_files/"+photoName;
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
		    Date now = new Date();
		    String strDate = sdfDate.format(now);
		    
		timeStamp = strDate;
		//Intent startService = new Intent(PictureEditor.this, "service class name");
		//Pass caption and image as extra to service intent
		Uri imageUri = Uri.fromFile(photo);
		Intent pictureViewerIntent = new Intent(this, PictureViewer.class);
		pictureViewerIntent.putExtra("FromPictureEditor", "1111");
		//pictureViewerIntent.putExtra("directory", Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));
		pictureViewerIntent.putExtra("photoUri", photoUri);
		pictureViewerIntent.putExtra("photoCaption", photoCaption);
		pictureViewerIntent.putExtra("photoName", photoName);
		pictureViewerIntent.putExtra("timeStamp", timeStamp);
		pictureViewerIntent.putExtra("gpsLocation", gpsLocation);
		pictureViewerIntent.putExtra("locAltitude", locAltitude);
		pictureViewerIntent.putExtra("locTemp", locTemp);
		startService(pictureViewerIntent);
		Toast.makeText(this, "View Photo", Toast.LENGTH_SHORT).show();
		finish();
		Intent returnToStream = new Intent(this, PictureStream.class);
		startActivity(returnToStream);
		Log.i(INPUT_SERVICE, "Starting Picture Viewer"+ userID+" "+photoName +" "+photoCaption+" "+photoPath+" "+timeStamp+" "+gpsLocation+" "+locAltitude+" "+locTemp+" ");

//		returnToStream();
		
	
	
	/*	private void uploadPhoto(){
		Intent uploadPhotobookIntent = new Intent(this, UploadService.class);
		uploadPhotobookIntent.putExtra(UploadService.directory, Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));
		uploadPhotobookIntent.putExtra(UploadService.image, photo.getAbsolutePath());
		startService(uploadPhotobookIntent);
		Toast.makeText(this, "Uploading Photo", Toast.LENGTH_SHORT).show();
	}*/

		
		//Intent startService = new Intent(PictureEditor.this, "service class name");
		//Pass caption and image as extra to service intent
		//photoName = userName + "-"+ String.valueOf(System.currentTimeMillis()) + ".jpg";
		uploadPhoto();
		//returnToStream();
		
	}
	
	private void uploadPhoto(){
		
		Intent uploadPhotobookIntent = new Intent(this, UploadService.class);
		uploadPhotobookIntent.putExtra(UploadService.directory, Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));
		uploadPhotobookIntent.putExtra(UploadService.image, photo.getAbsolutePath());
		uploadPhotobookIntent.putExtra(UploadService.userID, userID);
		uploadPhotobookIntent.putExtra(UploadService.photoName,photoName);
		uploadPhotobookIntent.putExtra(UploadService.photoCaption, photoCaption);
		uploadPhotobookIntent.putExtra(UploadService.photoPath,photoPath);
		uploadPhotobookIntent.putExtra(UploadService.timeStamp, timeStamp);
		uploadPhotobookIntent.putExtra(UploadService.gpsLocation,gpsLocation);
		uploadPhotobookIntent.putExtra(UploadService.locAltitude, locAltitude);
		uploadPhotobookIntent.putExtra(UploadService.locTemp,locTemp);
		startService(uploadPhotobookIntent);
		Toast.makeText(this, "Uploading Photo", Toast.LENGTH_SHORT).show();
		Log.i(INPUT_SERVICE, "Uploading from service" + photo.getAbsolutePath() +" "+ userID+" photoName"+photoName +" "+photoCaption+" "+photoPath+" "+timeStamp+" "+gpsLocation+" "+locAltitude+" "+locTemp+" ");
	}

	
	/*Delete picture and restart - go back to stream?*/
	private void delete(){
		finish();
		Intent returnToStream = new Intent(this, PictureStream.class);
		startActivity(returnToStream);
		//returnToStream();	
	}
	
	
/*	private void returnToStream(){
		Intent returns = new Intent(PictureEditor.this, PictureStream.class);
	
		//FOR DEMO//
		returns.putExtra("photoString", photoString);
	
		startActivity(returns);
	}
*/
	@Override
	protected void onStart()
	{
		super.onStart();
		locationClient.connect();
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		locationClient.disconnect();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		if(locationClient.getLastLocation() != null )
		{
			loc = locationClient.getLastLocation();
			gpsLocation = getAddressDetails(this, loc.getLatitude(), loc.getLongitude());
			//locAltitude = String.valueOf(loc.getAltitude());
			//locTemp = "70";
			DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			Date dateobj = new Date();
			timeStamp = df.format(dateobj);
			(Toast.makeText(this, "Address is: " + gpsLocation + "and Altitude is : " + locAltitude, Toast.LENGTH_SHORT)).show();
		
			//get temperature - thread

			weatherThread = new Thread() {
				@Override
				public void run()
				{
					String result = "";
					InputStream inputStream = null;					
					String url_selected = url+String.valueOf(loc.getLatitude())+"&lon="+String.valueOf(loc.getLongitude()) + "&FcstType=json";
					ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
					
					try
					{
						HttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());				
						HttpGet httpGet = new HttpGet(url_selected);
						httpGet.setHeader("Content-type", "application/json");				
						HttpResponse httpResponse = httpClient.execute(httpGet);
						HttpEntity httpEntity = httpResponse.getEntity();						
						inputStream = httpEntity.getContent();
						
					}catch (UnsupportedEncodingException e1)
					{
						Log.e("UnsupportedEncodingException",e1.toString());
						e1.printStackTrace();
					}
					catch(ClientProtocolException e2)
					{
						Log.e("ClientProtocolException",e2.toString());
						e2.printStackTrace();
					}
					catch(IllegalStateException e3)
					{
						Log.e("IllegalStateException",e3.toString());
						e3.printStackTrace();
					}
					catch(IOException e4)
				 	{
						Log.e("IOException",e4.toString());
						e4.printStackTrace();
					}
					try
					{
						
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
						StringBuilder builderString = new StringBuilder();
						String line = null;
						while((line=reader.readLine()) != null)
						{
							builderString.append(line + "\n");
						}
						inputStream.close();
						result = builderString.toString();
						
					}catch(Exception e){
						Log.e("StringBuilding and BufferedReader", "Error converting" + e.toString());
					}
					
					Message msg = Message.obtain();
					msg.obj = result;
						
					showContent.sendMessage(msg);
					try {
						inputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			 
					
					
				}
			};
			weatherThread.start(); 
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
		}
	}


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if(locationClient.getLastLocation() != null )
		{
			loc = locationClient.getLastLocation();
			gpsLocation = getAddressDetails(this, loc.getLatitude(), loc.getLongitude());
			//locAltitude = String.valueOf(loc.getAltitude());
			locTemp = "70";
			DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			Date dateobj = new Date();
			timeStamp = df.format(dateobj);
			(Toast.makeText(this, "Address is: " + gpsLocation, Toast.LENGTH_SHORT)).show();
		}
	}

	private void returnToStream(){
		Intent returns = new Intent(PictureEditor.this, PictureStream.class);
		//FOR DEMO//
		returns.putExtra("photoString", photoUri);
		startActivity(returns);
	}


	/*@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		if(locationClient.getLastLocation() != null )
		{
			loc = locationClient.getLastLocation();
			gpsLocation = getAddressDetails(this, loc.getLatitude(), loc.getLongitude());
			locAltitude = String.valueOf(loc.getAltitude());
			//(Toast.makeText(this, "Address is: " + gpsLocation + "and Altitude is : " + locAltitude, Toast.LENGTH_SHORT)).show();
		}
	}*/


	
	
	public String getAddressDetails(Context context, double latitude, double longitude)
	{
		String address = "";
		try {
			
			
	        Geocoder geo = new Geocoder(context, Locale.getDefault());
	        List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
	        if (addresses.isEmpty()) {
	           address = "No Location";
	        }
	        else {
	            if (addresses.size() > 0) {
	               address = addresses.get(0).getAddressLine(0).substring(5) + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
	                //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
	            }
	        }
	    }
	    catch (Exception e) {
	        e.printStackTrace(); // getFromLocation() may sometimes fail
	    }
		return address;
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		(Toast.makeText(this, "Connection unsuccessful" + result.toString(), Toast.LENGTH_LONG)).show();

	}

}
