package com.digitaldesign.dragantest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.digitaldesign.dragantest.customViews.InstrumentView;
import com.digitaldesign.dragantest.interfaces.OnReceiveValue;
import com.digitaldesign.dragantest.interfaces.OnWriteListener;

public class MainActivity<MenuInflater> extends Activity implements OnClickListener, OnWriteListener, OnReceiveValue {
	private InstrumentView instrument;
	private final String LOG_TAG = MainActivity.class.getCanonicalName();
	private boolean DEBUG = true;
	private int value = 0;
	private Tajmer nasTajmer;
	private TextView numericDisplay;
	
	private BluetoothByteStream byteStream;
	
	private static BluetoothSerialService mSerialService;
	
	public static final String DEVICE_NAME = "device_name";
	
	private TextView mTitle;
    private String mConnectedDeviceName = "";
    
    private ObjectAnimator instrumentAnimator;
	
	private MenuItem mMenuItemConnect;
	
//	 bluetooth
	
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mEnablingBT;
	
    // Message types sent from the BluetoothReadService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // The Handler that gets information back from the BluetoothService
    @SuppressLint("HandlerLeak")
	private final Handler mHandlerBT = new Handler() {
    	
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
            	if(DEBUG) Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
            	switch (msg.arg1) {
            	case BluetoothSerialService.STATE_CONNECTED:
                	if (mMenuItemConnect != null) {
                		mMenuItemConnect.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
                		mMenuItemConnect.setTitle(R.string.disconnect);
                	}
                	
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                break;
                    
            case BluetoothSerialService.STATE_CONNECTING:
                mTitle.setText(R.string.title_connecting);
                
                break;
                    
            case BluetoothSerialService.STATE_LISTEN:
                case BluetoothSerialService.STATE_NONE:
                	if (mMenuItemConnect != null) {
                		mMenuItemConnect.setIcon(android.R.drawable.ic_menu_search);
                		mMenuItemConnect.setTitle(R.string.connect);
                	}
                	
                    mTitle.setText(R.string.title_not_connected);

                    break;
                }
                break;
            case MESSAGE_READ:
            	Short res = (Short) msg.obj;
            	
            	break;
/*                
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;              
                mEmulatorView.write(readBuf, msg.arg1);
                
                break;
*/                
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString("TOAST"),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };    

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //---hides the title bar---
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_main);
        instrument = (InstrumentView) findViewById(R.id.skala);

        ((Button) findViewById(R.id.MainActivity_button_start)).setOnClickListener(this);
        ((Button) findViewById(R.id.MainActivity_button_stop)).setOnClickListener(this);

        ((ImageView) findViewById(R.id.MainActivity_button_connect)).setOnClickListener(this);

        ((Button) findViewById(R.id.MainActivity_button_3)).setOnClickListener(this);
        ((Button) findViewById(R.id.MainActivity_button_4)).setOnClickListener(this);
        
 //       ((ImageButton) findViewById(R.id.MainActivity_button_connect)).setOnClickListener(this);
        
        mTitle = (TextView) findViewById(R.id.gornji_tekst);
        numericDisplay = (TextView) findViewById(R.id.numeric_display);
        
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/digital7.ttf");
        numericDisplay.setTypeface(tf);
        
        final FrameLayout fr = (FrameLayout) numericDisplay.getParent();
        final ViewTreeObserver obs = fr.getViewTreeObserver();
        obs.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				//numericDisplay.setY(); // 80%
		        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		        lp.setMargins(0, fr.getHeight() * 82 / 100 - numericDisplay.getHeight(), 0, 0);
		        numericDisplay.setLayoutParams(lp);
				numericDisplay.invalidate();
			}
		});
        
        byteStream = new BluetoothByteStream(this);
        
        //instrument.registerValueCallback(this);
        
        /**
         * Bluetooth
         */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
            finishDialogNoBluetooth();
			return;
		}
		
		mSerialService = new BluetoothSerialService(this, mHandlerBT, this);    
   }

	@Override
	public void onStart() {
		super.onStart();
		if (DEBUG)
			Log.e(LOG_TAG, "++ ON START ++");
		
		mEnablingBT = false;
	}

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(DEBUG) Log.d(LOG_TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        
        case REQUEST_CONNECT_DEVICE:

            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mSerialService.connect(device);                
            }
            break;

        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {Log.d(LOG_TAG, "BT enabled");               
            }
            if (resultCode == Activity.RESULT_CANCELED) {Log.d(LOG_TAG, "BT not enabled");               
           	finishDialogNoBluetooth();                
            }
        }
    }

	@Override
	public synchronized void onResume() {
		super.onResume();

		if (DEBUG) {
			Log.e(LOG_TAG, "+ ON RESUME +");
		}
		
		if (!mEnablingBT) { // If we are turning on the BT we cannot check if it's enable
		    if ( (mBluetoothAdapter != null)  && (!mBluetoothAdapter.isEnabled()) ) {
			
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.alert_dialog_turn_on_bt)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.alert_dialog_warning_title)
                    .setCancelable( false )
                    .setPositiveButton(R.string.alert_dialog_yes, new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog, int id) {
                    		mEnablingBT = true;
                    		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);			
                    	}
                    })
                    .setNegativeButton(R.string.alert_dialog_no, new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog, int id) {
                    		finishDialogNoBluetooth();            	
                    	}
                    });
                AlertDialog alert = builder.create();
                alert.show();
		    }		
		
		    if (mSerialService != null) {
		    	// Only if the state is STATE_NONE, do we know that we haven't started already
		    	if (mSerialService.getState() == BluetoothSerialService.STATE_NONE) {
		    		// Start the Bluetooth chat services
		    		mSerialService.start();
		    	}
		    }

		    if (mBluetoothAdapter != null) {
		    	//readPrefs();
		    	//updatePrefs();
		    	//mEmulatorView.onResume();
		    }
		}
	}
    
    @Override
    protected void onStop() {
    	if(nasTajmer != null && nasTajmer.isAlive())
    		nasTajmer.interrupt();
    	super.onStop();
    }
    @Override 
    protected void onRestart() {
    	super.onRestart();
    	// Activity being restarted from stopped state     
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
        // Stop method tracing that the activity started during onCreate() 
    	android.os.Debug.stopMethodTracing();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
 //       getMenuInflater().inflate(R.menu.option_menu, menu);
 //       mMenuItemConnect = menu.getItem(0);
 //       return true;
 
        // Inflate the menu items for use in the action bar     
        Object inflater = getMenuInflater();     
        ((android.view.MenuInflater) inflater).inflate(R.menu.activity_main, menu);     
        return super.onCreateOptionsMenu(menu); 
    }
  
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	return super.onOptionsItemSelected(item);
    }
    
    private class Tajmer extends Thread {
		@Override
		public void run() {
			while(true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return;
				}
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//instrument.setValue(value++);
						onValue(value++);
						if ( value == 256 ) {
							value = 0;
						}
					}
				});
			
			}
		}
    }

    public void finishDialogNoBluetooth() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_no_bt)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(R.string.app_name)
        .setCancelable( false )
        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       finish();            	
                	   }
               });
        AlertDialog alert = builder.create();
        alert.show(); 
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.MainActivity_button_start:
			nasTajmer = new Tajmer();
			nasTajmer.start();
		break;
		case R.id.MainActivity_button_stop:
			nasTajmer.interrupt();
		break;
		case R.id.MainActivity_button_connect:
        	if (mSerialService.getState() == BluetoothSerialService.STATE_NONE) {
        		// Launch the DeviceListActivity to see devices and do scan
        		Intent serverIntent = new Intent(this, DeviceListActivity.class);
        		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        	}
        	else
            	if (mSerialService.getState() == BluetoothSerialService.STATE_CONNECTED) {
            		mSerialService.stop();
		    		mSerialService.start();
            	}
			break;
		case R.id.MainActivity_button_3:
			byte[] f = new byte[12]; // byte array
			f[0] = (byte) 11;
			f[1] = (byte) 128;
			f[2] = 48;
			f[3] = 52;
			f[4] = 48;
			f[5] = 48;
			f[6] = 48;
			f[7] = 48;
			f[8] = 50;
			f[9] = 70;
			f[10] = 53;
			f[11] = 70;
			
			//11,128 + "04" + "0000" + "2F5F";
		    mSerialService.write(f); 
			break;
		case R.id.MainActivity_button_4:
			/**
			 * byte koji se salje
			 */
			String s = "------";    
			mSerialService.write(s.getBytes());
			break;
		default:
			Log.w(LOG_TAG, "Unknown Button pressed");
		}
	}
	
	@Override
    public void write(byte[] buffer, int length) {
		/*
		Message msg= new Message();
		
		ByteBuffer byBuff = ByteBuffer.wrap(buffer);
		byBuff.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
		*/
		
		for(int i=0;i<length;i++){
			byte b = buffer[i];
			byteStream.addByte(b);
			Log.d(LOG_TAG, String.format("%d: Byte [%d]", i, b));
		}
		
		/*
		msg.obj = byBuff.getShort();
		msg.what = MESSAGE_READ;

		mHandlerBT.sendMessage(msg);
		*/		
		
    }

	@Override
	public void onValue(final int value) {
		
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
		    	if(instrumentAnimator != null && instrumentAnimator.isRunning()){
		    		instrumentAnimator.cancel();
		    	}
		    	instrumentAnimator = ObjectAnimator.ofInt(instrument, "value", value);
		    	
		    	instrumentAnimator.setDuration(100); // Duzina trajanja animacije
		    	/**
		    	 * Tipovi interpolacija
		    	 * http://developer.android.com/reference/android/animation/TimeInterpolator.html
		    	 */
		    	instrumentAnimator.setInterpolator(new AnticipateOvershootInterpolator());
		    	instrumentAnimator.start();
		    	
				instrument.setValue(value);
				numericDisplay.setText(String.format("%d", value));
			}
		});
	}
}
