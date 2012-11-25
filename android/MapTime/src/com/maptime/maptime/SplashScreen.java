package com.maptime.maptime;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

/**
 * Class responsible for showing a splash screen when the application starts, 
 * and closing the app when the last activity is closed.
 */

public class SplashScreen extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        
        Handler handler = new Handler();
        
        // run a thread after 0.75 seconds to start the home screen
        handler.postDelayed(new Runnable() {
 
            public void run() { 
                // make sure we close the splash screen so the user won't come back when it presses back key
                //finish();
                
                // start the home screen
                Intent intent = new Intent(SplashScreen.this, Home.class);
                SplashScreen.this.startActivityForResult(intent, 0);
            }
 
        }, 750);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //When the Home activity is closed, release all resources
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	if  (requestCode == 0) {
    		//finish();
    	}
    }
    
    @Override
    protected void onDestroy() { //When the resources are released, kill the process to clean up background threads
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	//android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.splash_screen, menu);
        return true;
    }
}
