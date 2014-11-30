package com.example.photobook;

import java.io.File;

import com.example.photobook.*;
import android.app.IntentService;
import android.content.Intent;

public class UploadService extends IntentService{

	public static final String image = "image", directory = "directory", photoCaption = "photoCaption",
			visibilityKey = "visibility", photoName = "photoName", photoPath = "photoPath", userID = "userID",
					timeStamp = "timeStamp", gpsLocation ="gpsLocation", locAltitude = "locAltitude", 
					locTemp = "locTemp";
	public static final String REFRESH_ACTION = "refreshStream";
	
	public UploadService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	public UploadService(){
		super("default");
	}
	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		try {
			
			
			boolean status = httpReq.uploadPhotos(this, 
					intent.getStringExtra(userID),
			intent.getStringExtra(photoName), intent.getStringExtra(photoCaption), intent.getStringExtra(photoPath),
			intent.getStringExtra(timeStamp), intent.getStringExtra(gpsLocation),intent.getStringExtra(locAltitude),
			intent.getStringExtra(locTemp), new File(intent.getStringExtra(image)));			
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
