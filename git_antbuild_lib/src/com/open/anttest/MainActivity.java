package com.open.anttest;

import com.open.anttest.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TextView txt=(TextView)findViewById(R.id.txt);
		txt.setText(getChanel(this));
	}

    public static String getChanel(Context ctx){  
        String CHANNELID="000000";  
        try {  
               ApplicationInfo  ai = ctx.getPackageManager().getApplicationInfo(  
                       ctx.getPackageName(), PackageManager.GET_META_DATA);  
               Object value = ai.metaData.get("channel");  
               if (value != null) {  
                   CHANNELID= value.toString();  
               }  
           } catch (Exception e) {  
               //  
           }  
          
        return CHANNELID;  
    } 
}
