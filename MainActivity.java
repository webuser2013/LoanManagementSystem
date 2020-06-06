package com.technodroid.backuprestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnItemSelectedListener {
	
	public GridView grid;
	final Context context = this;
	
	public String[] processName = {"Contacts", "Messages", "Call Logs" } ;
 	public int[] pssInactImages = {
            R.drawable.contactsdisabled,
            R.drawable.messagedisabled,
            R.drawable.calllogdisabled
    };
	public int[] pssActImages = {
            R.drawable.contactsenabled,
            R.drawable.messageenabled,
            R.drawable.calllogenabled
    };
	
	public int[] processInactId = {101, 201, 301 } ;
	public int[] processActId = {100, 200, 300 } ;
 	public String[] processStatus = {"0","0","0"} ;
 	
 	public ImageView submitImg;
 	public ImageView backupImage = null;
	public ImageView restoreImage = null;
	public ImageView messageImage = null;
	public ImageView callLogImage = null;
	public ImageView contactImage = null;
	
 	private String contactFileName = processName[0];
 	private String messageFileName = processName[1];
 	private String callLogsFileName = processName[2];
 	
 	private final String contactsFileExtn = ".vcf";
 	private final String messagesFileExtn = ".json";
 	private final String callLogsFileExtn = ".json";
 	
	private FileOutputStream mFileOutputStream;
	
	private static final String folderName = "T-DroidBackupRestore";
	private String contactFolderName = processName[0];
	private String messagesFolderName = processName[1];
	private String callLogsFolderName = processName[2];
	
	// Database Helper
    DatabaseHelper db;
    String defaultSmsApp;
    
    public static final String BKUP_TAG_NAME = "Backup";
    public static final String RST_TAG_NAME = "Restore";
	
 	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
    		
		// Create a folder in local memory
		createBaseFolder();
		createSubFolder(contactFolderName);
		createSubFolder(messagesFolderName);
		createSubFolder(callLogsFolderName);
		
		db = new DatabaseHelper(this);
		
		String lastBackupStr = db.getLastBkupDate(BKUP_TAG_NAME);
 		if(lastBackupStr != null && !lastBackupStr.isEmpty()){			
 			showDashboardContent();
		}
		
		CustomGrid adapter = new CustomGrid(MainActivity.this, processName,pssInactImages,processInactId,processStatus);
        grid =(GridView)findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setVerticalSpacing(1);
        grid.setHorizontalSpacing(1);
 
	     backupImage = (ImageView)findViewById(R.id.backupImgId);
	     restoreImage = (ImageView)findViewById(R.id.restoreImgId);	      
	     submitImg = (ImageView)findViewById(R.id.submitButtonId);
	    		  
	      backupImage.setOnClickListener(new OnClickListener(){
	        public void onClick(View view) {
	        	String backgroundImageName = String.valueOf(backupImage.getTag()); 
	        	if(backgroundImageName.equals("bd")){
	        		enableBackup();
	        		disableRestore();
  	        	}
 	       }});

	      restoreImage.setOnClickListener(new OnClickListener(){
	          public void onClick(View view) {
	        	  String backgroundImageName = String.valueOf(restoreImage.getTag()); 
	 	        	if(backgroundImageName.equals("rd")){	 	        		
	 	        		enableRestore();
	 	        		disableBackup();
		        	}
 	          }});
 	       
	      
	      submitImg.setOnClickListener(new OnClickListener() {
	 	        public void onClick(View view) {
 	 	        	processBackupRestore();
  	       }});
	      
	      defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);
	       
        
      }	
 	 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
          switch (item.getItemId()) {
			case R.id.action_settings:
				Intent intent = new Intent(this, Scheduler.class);
				startActivity(intent);
				return true;     
 			default:
				return super.onOptionsItemSelected(item);  
		}
    } 

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
 		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
 		
	}
	
	private void enableBackup(){
		backupImage.setImageResource(R.drawable.backupenabled);
		backupImage.setTag("be");
	}
	
	private void disableBackup(){
		backupImage.setImageResource(R.drawable.backupdisabled);
		backupImage.setTag("bd");
	}
	
	private void enableRestore(){
		restoreImage.setImageResource(R.drawable.restoreenabled);
		restoreImage.setTag("re");
	}
	
	private void disableRestore(){
		restoreImage.setImageResource(R.drawable.restoredisabled);
		restoreImage.setTag("rd");
	}
	
	public boolean isBackupEnabled(String tagName){
 		return tagName.equals("be");
 	}
	
	public boolean isRestoreEnabled(String tagName){
		return tagName.equals("re");
	}
	
	public boolean isContactEnabled(String tagName){
		return tagName.equals("100-1");
	}
	
	public boolean isMessageEnabled(String tagName){
		return tagName.equals("200-1");
	}
	
	public boolean isCallLogEnabled(String tagName){
		return tagName.equals("300-1");
	}
	
	public void createBaseFolder(){
		File folder = new File(Environment.getExternalStorageDirectory() + File.separator + folderName);
		if (!folder.exists()) {
		    folder.mkdir();
		}
	}
	
	public void createSubFolder(String subFolderName){
		File folder = new File(Environment.getExternalStorageDirectory() + File.separator + folderName + File.separator + subFolderName);
		if (!folder.exists()) {
		    folder.mkdir();
		}
	}
	
	public String getBaseFolder(){
		File folder = new File(Environment.getExternalStorageDirectory() + File.separator + folderName);
		if (folder.exists()) {
		    return folder.getAbsolutePath();
		}  
		return null;
 	}
	
	public String getSubFolderByName(String subFolderName){
		File folder = new File(Environment.getExternalStorageDirectory() + File.separator + folderName + File.separator + subFolderName);
		if (folder.exists()) {
		    return folder.getAbsolutePath();
		}  
		return null;
 	}
	
	public String getLatestBkupFile(String pssFolderName,String pssFileName,String pssFileExtn){
		String latestBkupFile = null;
		List<Long> bkupFileList = new ArrayList<Long>();
		String path = getSubFolderByName(pssFolderName);
 		File f = new File(path);        
		File file[] = f.listFiles();
 		for (int i=0; i < file.length; i++) {
 			String fileName = file[i].getName(); 	
 			
   			int stopIndex = 4;
 			int startIndex = pssFileName.length()+1;
 			if(pssFileName.equalsIgnoreCase(processName[1]) || pssFileName.equalsIgnoreCase(processName[2])){
 				stopIndex = 5;
 			}  
 			bkupFileList.add(Long.parseLong(fileName.substring(startIndex, fileName.length()-stopIndex)));
		}
 		
  		long lastBackupTime = bkupFileList.get(0);
  		for(Long i: bkupFileList) {
  		    if(i > lastBackupTime) lastBackupTime = i;
 		}
 		
  		latestBkupFile =  path + File.separator + pssFileName + "-" + lastBackupTime + pssFileExtn;
 		return latestBkupFile;
	}
	
	
	private void processBackupRestore(){
  		
		boolean isBackupEnabled = isBackupEnabled(String.valueOf(backupImage.getTag()));
		boolean isRestoreEnabled = isRestoreEnabled(String.valueOf(restoreImage.getTag())); 
  
		final int size = grid.getChildCount();
		for(int i = 0; i < size; i++) {
		  ViewGroup gridChild = (ViewGroup) grid.getChildAt(i);
  		    
	    	//Contacts
	    	if( i == 0 ){
	    		if( gridChild.getChildAt(0) instanceof ImageView ) {
 	    			contactImage = (ImageView) grid.findViewWithTag(gridChild.getChildAt(0).getTag());
	    		}
	    	}
	    	
	    	//Messages
	    	if( i == 1 ){
	    		if( gridChild.getChildAt(0) instanceof ImageView ) {
 	    			messageImage = (ImageView) grid.findViewWithTag(gridChild.getChildAt(0).getTag());
	    		}
	    	}
	    	
	    	//Call Logs
	    	if( i == 2 ){
	    		if( gridChild.getChildAt(0) instanceof ImageView ) {
 	    			callLogImage = (ImageView) grid.findViewWithTag(gridChild.getChildAt(0).getTag());
	    		}
	    	}
 		}
		
		
		final ContentResolver cResolver = getBaseContext().getContentResolver(); 
		if(isBackupEnabled){
			
			if( !isCallLogEnabled(String.valueOf(callLogImage.getTag())) && !isContactEnabled(String.valueOf(contactImage.getTag())) && !isMessageEnabled(String.valueOf(messageImage.getTag())) ) {
				showAlert("Please enable any one process to Backup!");		
				return;
			}
			
			final ProgressDialog progress = ProgressDialog.show(this, "Backup Restore", "Backup in Progress", true);
			new Thread(new Runnable() {
			 @Override
			 public void run() {
				invokeBackupProcess(cResolver,contactImage,messageImage,callLogImage);
				
			    runOnUiThread(new Runnable() {
			      @Override
			      public void run() {
			        progress.dismiss();
 			        showDashboardContent();
 			        showAlert("Backup Completed");
			      }
			    });
			  }
			}).start();
						
		} else if(isRestoreEnabled) {
			
			if(!isCallLogEnabled(String.valueOf(callLogImage.getTag())) && !isContactEnabled(String.valueOf(contactImage.getTag())) && !isMessageEnabled(String.valueOf(messageImage.getTag())) ) {
				showAlert("Please enable any one process to Restore!");		
				return;
			}
			
			final ProgressDialog progress = ProgressDialog.show(this, "Backup Restore", "Restore in Progress", true);
			new Thread(new Runnable() {
			 @Override
			 public void run() {
				invokeRestoreProcess(cResolver,contactImage,messageImage,callLogImage);
 			    runOnUiThread(new Runnable() {
			      @Override
			      public void run() {
			        progress.dismiss();
  			        showDashboardContent();
 			        showAlert("Restore Completed");
			      }
			    });
			  }
			}).start();
			
			
			
		} else {
			//Nothing chosen by the user. throw the error message
		}	
 
 	}
	
	private void showDashboardContent(){
		String lastBackupStr = db.getLastBkupDate(BKUP_TAG_NAME);
		if(lastBackupStr != null && !lastBackupStr.isEmpty()){			
			Date date = new Date(Long.parseLong(lastBackupStr));
 	        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
	        String dateText = dateFormat.format(date);
 	        TextView lstbkupTxt = (TextView)findViewById(R.id.lastBkupStrId);
			lstbkupTxt.setText("Last Backup on "+dateText);
		}
	}
	
	private void showAlert(String Message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
		builder.setMessage(Message)  
            .setNegativeButton("Ok", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int id) {  
                dialog.cancel();  
         }  
        }).show();
	}
	
	public void invokeBackupProcess(ContentResolver cResolver, ImageView contactImage, ImageView messageImage, ImageView callLogImage){
 		
 		if(isCallLogEnabled(String.valueOf(callLogImage.getTag()))) {			
			processCallLogsBackup(cResolver);
		}			
		if(isContactEnabled(String.valueOf(contactImage.getTag()))){	
 			processContactsBackup(cResolver);
 		}
		if(isMessageEnabled(String.valueOf(messageImage.getTag()))){
 			processMessagesBackup(cResolver);
		}
		
		long currentTime = System.currentTimeMillis();
 		db.insertOrUpdateProcess(BKUP_TAG_NAME, ""+currentTime, ""+currentTime);
  		
  	}
	
	public void invokeRestoreProcess(ContentResolver cResolver, ImageView contactImage, ImageView messageImage, ImageView callLogImage){
		
		if(isCallLogEnabled(String.valueOf(callLogImage.getTag()))) {
			processCallLogsRestore(cResolver);
		}
 		if(isContactEnabled(String.valueOf(contactImage.getTag()))){ 			
 			processContactsRestore(cResolver);
 		}
		if(isMessageEnabled(String.valueOf(messageImage.getTag()))) {
			
			//Get the package name and check if my app is not the default sms app
			final String myPackageName = getPackageName();
		    if (!Telephony.Sms.getDefaultSmsPackage(context).equals(myPackageName)) {
	             //Change the default sms app to my app
	            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
	            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.getPackageName());
	            startActivityForResult(intent, 1);
 	        } else {	        	
	        	processMessagesRestore(cResolver);
	        }
			
		}
		
		long currentTime = System.currentTimeMillis();
 		db.insertOrUpdateProcess(RST_TAG_NAME, ""+currentTime, ""+currentTime);
 	}
	
	public void processContactsBackup(ContentResolver cResolver) {
		if(cResolver == null){
			cResolver = getBaseContext().getContentResolver(); 
		}
		
 		Cursor phonesCursor = cResolver.query(ContactsContract.Contacts.CONTENT_URI, null,null, null, null);
		if (phonesCursor.moveToFirst()) {
			long currTime = System.currentTimeMillis();
 			for (int i = 0; i < phonesCursor.getCount(); i++) {
                String lookupKey = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI,lookupKey);
                AssetFileDescriptor fd;
                try {
                        fd = cResolver.openAssetFileDescriptor(uri, "r");
                        FileInputStream fis = fd.createInputStream();
                        byte[] buf = new byte[(int) fd.getDeclaredLength()];
                        fis.read(buf);
                        String VCard = new String(buf);
                        String contactsFolder = getSubFolderByName(processName[0]);
                        String path = "";
                        if(contactsFolder != null){
                        	path = contactsFolder + File.separator + contactFileName + "-" + currTime + contactsFileExtn;
                        }
                        mFileOutputStream = new FileOutputStream(path,true);
                        mFileOutputStream.write(VCard.toString().getBytes());
                        phonesCursor.moveToNext();
                 } catch (Exception e) {
                        e.printStackTrace();
                }
			}
 			Log.d("Dashboard", "Contact Backup Completed>>>>>>>>>>>>>>>>>>>>>>>");
 		}
	}
	
	public void processContactsRestore(ContentResolver cResolver) {
		try {
			  MimeTypeMap mime = MimeTypeMap.getSingleton();
			  String tmptype = mime.getMimeTypeFromExtension("vcf");
			  File file = new File(getLatestBkupFile(contactFolderName,contactFileName,contactsFileExtn));
			  Intent i = new Intent();
			  i.setAction(android.content.Intent.ACTION_VIEW);
			  i.setDataAndType(Uri.fromFile(file), "text/x-vcard");
			  startActivity(i);
			Log.d("Dashboard", "Contact Restore Completed>>>>>>>>>>>>>>>>>>>>>>>");
		} catch (Exception e) {
 			e.printStackTrace();
		}
	}
	
	public void processMessagesBackup(ContentResolver cResolver) {
		if(cResolver == null){
			cResolver = getBaseContext().getContentResolver(); 
		}
		try {
		    Uri message = Uri.parse("content://sms/");
 		    Cursor c = cResolver.query(message, null, null, null, null);
  		    
 		    int totMsgCount = c.getCount();
  		    System.out.println("totMsgCount>>>>>>"+totMsgCount);
 		    long currTime = System.currentTimeMillis();
  		    
 		    String messagesFolder = getSubFolderByName(processName[1]);
            String path = "";
            if(messagesFolder != null){
           	 	path = messagesFolder + File.separator + messageFileName + "-" + currTime + messagesFileExtn;
            }
  		    File file = new File(path);
  		    if(!file.exists())
  		    	file.createNewFile();
  		    
  		    JSONObject jsonObject = new JSONObject();
		    jsonObject.put("count", totMsgCount);
		    jsonObject.put("backup_date", currTime);
		    
		    JSONArray jsonArray = new JSONArray();	 
		    
 		    if (c.moveToFirst()) {
 		        for (int i = 0; i < totMsgCount; i++) {
  		            JSONObject formDetailsJson = new JSONObject();
		            formDetailsJson.put("id", c.getString(c.getColumnIndexOrThrow("_id")));
		            formDetailsJson.put("threadId", c.getString(c.getColumnIndexOrThrow("thread_id")));
		            formDetailsJson.put("address", c.getString(c.getColumnIndexOrThrow("address")));
		            formDetailsJson.put("person", c.getString(c.getColumnIndexOrThrow("person")));
		            formDetailsJson.put("date", c.getString(c.getColumnIndexOrThrow("date")));
		            formDetailsJson.put("protocol", c.getString(c.getColumnIndexOrThrow("protocol")));
		            formDetailsJson.put("type", c.getString(c.getColumnIndexOrThrow("type")));
		            formDetailsJson.put("subject", c.getString(c.getColumnIndexOrThrow("subject")));
		            formDetailsJson.put("body", c.getString(c.getColumnIndexOrThrow("body")));
		            formDetailsJson.put("serviceCenter", c.getString(c.getColumnIndexOrThrow("service_center")));
		            formDetailsJson.put("read", c.getString(c.getColumnIndexOrThrow("read")));
		            formDetailsJson.put("status", c.getString(c.getColumnIndexOrThrow("status")));
		            formDetailsJson.put("locked", c.getString(c.getColumnIndexOrThrow("locked")));
		            formDetailsJson.put("dateSent", c.getString(c.getColumnIndexOrThrow("date_sent")));
		            formDetailsJson.put("replyPathPresent", c.getString(c.getColumnIndexOrThrow("reply_path_present")));
		            jsonArray.put(formDetailsJson);		            
  		            c.moveToNext();
		        }
		    }
		    
		    c.close();
 		    jsonObject.put("smses", jsonArray);
		    
 		    FileWriter fileWriter = new FileWriter(file);  
 		    fileWriter.write(jsonObject.toString());  
 		    fileWriter.flush();  
 		    fileWriter.close(); 
 		    
 		    System.out.println("Completed>>>>>");
 		    
		} catch (Exception e) {
			e.printStackTrace();
		}
 	}
	    
	public void processMessagesRestore(ContentResolver cResolver) {
		try {
			try {
				final String myPackageName = getPackageName();
  				if (!Telephony.Sms.getDefaultSmsPackage(context).equals(myPackageName)) {
					showAlert("Please make Backup Restore as Default SMS App to restore your Messages");
					return;
				} else {
					//writeSms();	
				}
	            
			} catch (Exception e) {
 				e.printStackTrace();
			}
			
		} catch (Exception e) {
 			e.printStackTrace();
		}
	}
	
	//Write to the default sms app
	private void writeSms() {
		FileInputStream stream = null;
		StringBuffer stringBuffer = new StringBuffer();
		try {
			File file = new File(getLatestBkupFile(messagesFolderName,messageFileName,messagesFileExtn));
			stream = new FileInputStream(file);
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            stringBuffer.append(Charset.defaultCharset().decode(bb).toString());
          } catch(Exception e){
        	  e.printStackTrace();
          } finally {
            try {
				stream.close();
			} catch (IOException e) {
 				e.printStackTrace();
			}
          }
		try {
			JSONObject jsonObj = new JSONObject(stringBuffer.toString());
			JSONArray messagesArray  = jsonObj.getJSONArray("smses");
			Log.i("Log", ">>>>>"+messagesArray.length());
 			
			for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject c = messagesArray.getJSONObject(i);   
                
                ContentValues values = new ContentValues();
                values.put(Telephony.Sms._ID, c.getString("id"));
    			values.put(Telephony.Sms.THREAD_ID, c.getString("threadId"));
    			values.put(Telephony.Sms.ADDRESS, c.getString("address"));
    			values.put(Telephony.Sms.PERSON, c.getString("person"));
    			values.put(Telephony.Sms.DATE, c.getString("date"));
    			values.put(Telephony.Sms.PROTOCOL, c.has("protocol") ? c.getString("protocol") :  "0");
    			values.put(Telephony.Sms.TYPE, c.getString("type"));
    			
    			if(c.has("subject")){
    				values.put(Telephony.Sms.SUBJECT, c.getString("subject"));
    			}
    			
    			values.put(Telephony.Sms.BODY, c.getString("body"));
    			
    			if(c.has("serviceCenter")){    				
    				values.put(Telephony.Sms.SERVICE_CENTER, c.getString("serviceCenter"));
    			}
    			
    			values.put(Telephony.Sms.READ, c.getString("read"));
    			values.put(Telephony.Sms.STATUS, c.getString("status"));
    			values.put(Telephony.Sms.LOCKED, c.getString("locked"));
    			values.put(Telephony.Sms.DATE_SENT, c.getString("dateSent"));
    			
    			if(c.has("replyPathPresent")){    				
    				values.put(Telephony.Sms.REPLY_PATH_PRESENT, c.getString("replyPathPresent"));
    			}
                
//              values.put("address", c.getString("address"));
//	  			values.put(Telephony.Sms.TYPE, c.getString("type"));
//	  			values.put("body", c.getString("body"));
//	  			values.put("read", c.getString("read"));
// 	  			values.put("date", c.getString("date"));
     			
    			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      				context.getContentResolver().insert(Uri.parse("content://sms"), values);
      			  } else {
      				context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
      			  }    			
            }
			
			
			  /*//Put content values
			  ContentValues values = new ContentValues();
			  values.put("address", "8872743939");
			  values.put(Telephony.Sms.TYPE, "1");
			  values.put("body", "My Name is Kamal");
			  values.put("read", 1);
			  Date date = new Date();
			  Long Date = date.getTime();
			  values.put("date", Date.toString());
			  
			  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
  				context.getContentResolver().insert(Telephony.Sms.Sent.CONTENT_URI, values);
  			  } else 
  			  {
  				context.getContentResolver().insert(Uri.parse("content://sms"), values);
  			  }	*/		
			  System.out.println("Message Restor ecomplete>>>>>>>>>");
			  
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	}
	
	public void processCallLogsBackup(ContentResolver cResolver) {
		if(cResolver == null){
			cResolver = getBaseContext().getContentResolver(); 
		}
		
		try {
 			Cursor c = cResolver.query(CallLog.Calls.CONTENT_URI, null,null, null, null);
 			int totCallLogCount = c.getCount();
		    System.out.println("totCallLogCount>>>>>>"+totCallLogCount);
		    long currTime = System.currentTimeMillis();
			    
		    String messagesFolder = getSubFolderByName(processName[2]);
	        String path = "";
	        if(messagesFolder != null){
	       	 	path = messagesFolder + File.separator + callLogsFileName + "-" + currTime + callLogsFileExtn;
	        }
		    File file = new File(path);
		    if(!file.exists())
		    	file.createNewFile();
			    
			JSONObject jsonObject = new JSONObject();
		    jsonObject.put("count", totCallLogCount);
		    jsonObject.put("backup_date", currTime);
		    
		    JSONArray jsonArray = new JSONArray();	
			
	        int number = c.getColumnIndex(CallLog.Calls.NUMBER);
	        int type = c.getColumnIndex(CallLog.Calls.TYPE);
	        int date = c.getColumnIndex(CallLog.Calls.DATE);
	        int duration = c.getColumnIndex(CallLog.Calls.DURATION);
	        
  	        while (c.moveToNext()) {
 	        	JSONObject formDetailsJson = new JSONObject();
	        	formDetailsJson.put("phoneNumber",c.getString(number));
	        	formDetailsJson.put("duration",c.getString(duration));
	        	formDetailsJson.put("date",c.getString(date));
	        	String contactName = getContactName(cResolver, c.getString(number));
	        	formDetailsJson.put("contactName",contactName != null ? contactName : "(Unknown)" );
 	            String callType = c.getString(type);
	            int dircode = Integer.parseInt(callType);
  	            formDetailsJson.put("callType",dircode);
	            jsonArray.put(formDetailsJson);	
 	        }
	        c.close();
	        
	        jsonObject.put("calllogs", jsonArray);
		    
 		    FileWriter fileWriter = new FileWriter(file);  
 		    fileWriter.write(jsonObject.toString());  
 		    fileWriter.flush();  
 		    fileWriter.close(); 
 		    
 		    System.out.println("Completed>>>>>");
 		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public String getContactName(ContentResolver cResolver, String phoneNumber) {
 	    Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
	    Cursor cursor = cResolver.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
	    if (cursor == null) {
	        return null;
	    }
	    String contactName = null;
	    if(cursor.moveToFirst()) {
	        contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
	    }
 	    if(cursor != null && !cursor.isClosed()) {
	        cursor.close();
	    }
 	    return contactName;
	}
	
	public void processCallLogsRestore(ContentResolver cResolver) {
		try {
			
			File file = new File(getLatestBkupFile(callLogsFolderName,callLogsFileName,callLogsFileExtn));
			FileInputStream stream = new FileInputStream(file);
			StringBuffer stringBuffer = new StringBuffer();
			
			try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                stringBuffer.append(Charset.defaultCharset().decode(bb).toString());
              }
              catch(Exception e){
            	  e.printStackTrace();
              }
              finally {
                stream.close();
              }
			JSONObject jsonObj = new JSONObject(stringBuffer.toString());
			JSONArray callLogsArray  = jsonObj.getJSONArray("calllogs");
			
             for (int i = 0; i < callLogsArray.length(); i++) {
                JSONObject c = callLogsArray.getJSONObject(i);
                ContentValues values = new ContentValues();
    			//values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
    			values.put(CallLog.Calls.TYPE, c.getString("callType"));
    			values.put(CallLog.Calls.DATE, c.getString("date"));
    			values.put(CallLog.Calls.DURATION, c.getString("duration"));
    			values.put(CallLog.Calls.NUMBER, c.getString("phoneNumber"));
    			getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
               }
             System.out.println("Call Logs restore completed");
		} catch (Exception e) {
 			e.printStackTrace();
		}
	}
}
