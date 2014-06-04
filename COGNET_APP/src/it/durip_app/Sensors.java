/*
Cognetive Network APP 

Copyright (C) 2014  Matteo Danieletto matteo.danieletto@dei.unipd.it
University of Padova, Italy +39 049 827 7778
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

//TYPE_ACCELEROMETER uses the accelerometer and only the accelerometer. It returns raw accelerometer events, with minimal or no processing at all.
//
//TYPE_GYROSCOPE (if present) uses the gyroscope and only the gyroscope. Like above, it returns raw events (angular speed un rad/s) with no processing at all (no offset / scale compensation).
//
//TYPE_ORIENTATION is deprecated. It returns the orientation as yaw/ pitch/roll in degres. It's not very well defined and can only be relied upon when the device has no "roll". This sensor uses a combination of the accelerometer and the magnetometer. Marginally better results can be obtained using SensorManager's helpers. This sensor is heavily "processed".
//
//TYPE_LINEAR_ACCELERATION, TYPE_GRAVITY, TYPE_ROTATION_VECTOR are "fused" 
//sensors which return respectively the linear acceleration, gravity and rotation vector 
//(a quaternion). It is not defined how these are implemented. 
//On some devices they are implemented in h/w, on some devices they use the accelerometer + 
//the magnetometer, on some other devices they use the gyro.

package it.durip_app;

//import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.Calendar;
//import java.util.Date;


//import java.util.Date;

import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
//import android.text.format.DateFormat;
import android.util.Log;
//import android.util.Log;

public class Sensors extends Service implements SensorEventListener {
	public Sensors() {
	}

	public static final String LOOP="LOOP";
	public static final String DESTINATION="DESTINATION";
	public static final String TIME="TIME";
	public static final String INTERVAL="INTERVAL";
	/*
	 * FILE NAME*/
	private static final String PATH_SENSOR_FOLDER = "/local/SensorLog/";
	private static final int TS_SENSOR = 1000; 
	private boolean isPlaying=false;
	private static SensorManager managerSensor = null;
    private static Sensor orSensor3 = null;
    private static Sensor orSensor2 = null;
    private static Sensor orSensor1 = null;
    private static OutputStream outGravity = null;
    private static OutputStream outLinear = null;
    private static OutputStream outRotation = null;
    private static Writer writeGravity;
    private static Writer writeRotation;
    private static Writer writeLinear;
	  @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    boolean loop=intent.getBooleanExtra(LOOP, false);
	    Calendar c = Calendar.getInstance();
	    
	    int hh     = c.get(Calendar.HOUR);
	    int mm     = c.get(Calendar.MINUTE);
	    int second = c.get(Calendar.HOUR);
	    int month  = c.get(Calendar.DAY_OF_MONTH);
	    int day    = c.get(Calendar.MONTH)+1;
	    
	    File file = new File(Environment.getExternalStorageDirectory(), PATH_SENSOR_FOLDER);
	    if (!file.exists()) {
	        if (!file.mkdirs()) {
	            Log.e("TravellerLog :: ", "Problem creating Image folder");
	        }
	    }
	    
	    String fileGravity =  Environment.getExternalStorageDirectory().getPath()+PATH_SENSOR_FOLDER +"logGravity_"+hh+"_"+mm+"_"+second+"_"+month+"_"+day;
	    String fileAcceleration =  Environment.getExternalStorageDirectory().getPath()+PATH_SENSOR_FOLDER +"logAcceleration_"+hh+"_"+mm+"_"+second+"_"+month+"_"+day;
	    String fileRotation =  Environment.getExternalStorageDirectory().getPath()+PATH_SENSOR_FOLDER +"logRotation_"+hh+"_"+mm+"_"+second+"_"+month+"_"+day;
//	    System.out.println(Calendar.HOUR));
//	    System.out.println(c.get(Calendar.MINUTE));
//	    System.out.println(c.get(Calendar.SECOND));
//	    System.out.println(c.get(Calendar.DAY_OF_MONTH));
//	    System.out.println(c.get(Calendar.MONTH)+1);

	    managerSensor = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
		
        
        try {			
			outGravity = new FileOutputStream(fileGravity, true);
			outLinear = new FileOutputStream(fileAcceleration, true);
			outRotation = new FileOutputStream(fileRotation, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block			
			e.printStackTrace();
		}
        try {
			writeGravity = new OutputStreamWriter(outGravity, "UTF-8");
			writeLinear = new OutputStreamWriter(outLinear, "UTF-8");
			writeRotation = new OutputStreamWriter(outRotation, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//    	try {
//			writeGravity.append("TIME Xm/s2 Ym/s2 Zm/s2\n");
//			writeLinear.append("TIME Xm/s2 Ym/s2 Zm/s2\n");
//			writeRotation.append("TIME X Y Z\n");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	    play(loop);
	    
	    //AGGIUNGO UN THREAD PER LA LETTURA DELLA BATTERIA!!
	    
	    
	    return(START_NOT_STICKY);
	}
	  
	@Override
	public void onDestroy() {
	    stop();
	}
	  
	@Override
	public IBinder onBind(Intent intent) {
	    return(null);
	}
	  
	private void play(boolean loop) {

	    if (!isPlaying) {
	    	
	    	System.out.println("TEST");
	    	//THIS SENSOR REPORT ONLY THE FUSION VALUE
	    	
	        for (Sensor sensor : managerSensor.getSensorList(Sensor.TYPE_ALL)) {
			    if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
			        orSensor1 = sensor;
			    }
			    if (sensor.getType() == Sensor.TYPE_GRAVITY) {
			        orSensor2 = sensor;
			    }
			    if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			        orSensor3 = sensor;
			    }
			}

	        managerSensor.registerListener(this, orSensor1, TS_SENSOR);
			managerSensor.registerListener(this, orSensor2, TS_SENSOR);
			managerSensor.registerListener(this, orSensor3, TS_SENSOR);
	        System.out.println("Us RATE:" + SensorManager.SENSOR_DELAY_NORMAL);
//	        managerSensor.registerListener(this, orSensor1, SensorManager.SENSOR_DELAY_NORMAL);
//			managerSensor.registerListener(this, orSensor2, SensorManager.SENSOR_DELAY_NORMAL);
//			managerSensor.registerListener(this, orSensor3, SensorManager.SENSOR_DELAY_NORMAL);
	        
			
	    	isPlaying=true;
	    }
	}
	  
	private void stop() {
	    if (isPlaying) {
	    	try {
	    		
				writeGravity.close();
				writeRotation.close();
				writeLinear.close();
				
				managerSensor.unregisterListener(this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	isPlaying=false;
	    }
	}
	
    // Called whenever a new orSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
    	SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss.S" , Locale.ITALY);
//    	formatTime.format(new Date());
        // update instantaneous data:
		//System.out.println(sensorEvent.sensor.getType() + " VALS: "+ sensorEvent.values[0] + " - " + sensorEvent.values[1] + " - "+sensorEvent.values[2]);
    	if(isPlaying){
    		if(sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
    			try {
    				writeGravity.append(formatTime.format(System.currentTimeMillis()) + " " + sensorEvent.values[0] + " " + sensorEvent.values[1] + " " + sensorEvent.values[2] + "\n");
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    		if(sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
    			try {
    				writeRotation.append(formatTime.format(System.currentTimeMillis()) + " " + sensorEvent.values[0] + " " + sensorEvent.values[1] + " " + sensorEvent.values[2] + "\n");
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}           	
    		if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
    			try {
    				writeLinear.append(formatTime.format(System.currentTimeMillis()) + " " + sensorEvent.values[0] + " " + sensorEvent.values[1] + " " + sensorEvent.values[2] + "\n");
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
