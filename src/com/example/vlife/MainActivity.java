package com.example.vlife;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import com.radiusnetworks.ibeacon.Region;
import com.example.vlife.Constants;
import com.example.vlife.R;
import com.example.vlife.util.Stream;
import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements IBeaconConsumer, OnBackStackChangedListener {

	
	private static final String LOG_TAG = "MainActivity";
	public String Url;

	private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

	private int dishCount = 0;
	private View textEntryView;
	private Stream st = new Stream();
	private StringBuilder uuidSb = new StringBuilder();

	private int MIN_TRIGGER_COUNT = 5;
	private IBeacon popUpIBeacon = null;
	private boolean isAlertDialogClosed = false;
	private boolean DEBUG = false;
	private Map<IBeacon, Integer> IBEACON_SHOWUP_COUNT = new HashMap<IBeacon, Integer>();
	
	private String contentType;
	
	// A handle to the main screen view
    private View mMainView;
    
    // Tracks whether the app is in full-screen mode
	private boolean mFullScreen;
	
	// Tracks the number of Fragments on the back stack
    int mPreviousStackCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Sets fullscreen-related flags for the display
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
		
		
		super.onCreate(savedInstanceState);
		
		// Inflates the main View, which will be the host View for the fragments
        mMainView = getLayoutInflater().inflate(R.layout.activity_main, null);
		//setContentView(R.layout.activity_main);
        setContentView(mMainView);

		iBeaconManager.bind(this);

		// Gets an instance of the support library FragmentManager
        FragmentManager localFragmentManager = getSupportFragmentManager();
        
        localFragmentManager.addOnBackStackChangedListener(this);

        
        
        
		if (savedInstanceState == null) {
			
			Log.d(LOG_TAG, "onCreate() executed and savedInstanceState null");
			NetWorkFragment netWorkFragment = new NetWorkFragment();

			Bundle args = new Bundle();
			Url = getString(R.string.homeUrl);
			args.putString(NetWorkFragment.REQUEST_URL, Url);
			args.putString(NetWorkFragment.CONTENTTYPE, Constants.CONTENTTYPE_HTML);
			args.putInt(NetWorkFragment.VIEWID, R.id.webview);
			netWorkFragment.setArguments(args);

			FragmentTransaction ft = localFragmentManager.beginTransaction();
			ft.add(R.id.container, netWorkFragment, Constants.HOMEVIEW_FRAGMENT);
			//ft.addToBackStack(null);
			ft.commit();

		} else {
			
			Log.d(LOG_TAG, "onCreate() executed and savedInstanceState is null");
			// Gets the previous state of the fullscreen indicator
            mFullScreen = savedInstanceState.getBoolean(Constants.EXTENDED_FULLSCREEN);
            
            // Sets the fullscreen flag to its previous state
            setFullScreen(mFullScreen);
            
            // Gets the previous backstack entry count.
            mPreviousStackCount = localFragmentManager.getBackStackEntryCount();
		}

	}

	private String getUUIDFromIBeacon(IBeacon ibeacon) {
		uuidSb.setLength(0);
		return uuidSb.append(ibeacon.getProximityUuid()).append(":")
				.append(ibeacon.getMajor()).append(":").append(ibeacon.getMinor()).toString();
	}
	
	private synchronized void MyIBeaconNotifier(IBeacon nearestBeacon) {
		String currentUUID = getUUIDFromIBeacon(nearestBeacon);
	
		Log.d(LOG_TAG, "uuid is null or current uuid not equal with saved uuid");
		if (NetWorkFragment.wifiConnected || NetWorkFragment.mobileConnected) {
			String dishUrl = getString(R.string.DishesUrl) + currentUUID;
			Log.d(LOG_TAG, "get Dishes from " + dishUrl);
			new DownloadDishesTask().execute(dishUrl);
		} else {
			Toast.makeText(MainActivity.this, "Network connection failed", Toast.LENGTH_LONG).show();
		}
	}

	
	private class UpdateIBeaconSize extends AsyncTask<Integer, Void, Integer> {

		@Override
		protected Integer doInBackground(Integer... integers) {
			return integers[0]; 
		}

		protected void onPostExecute(Integer size) {
			getActionBar().setSubtitle("IBeacon: "+size);
		}
	}
	
	
	
	
	@Override
	public void onIBeaconServiceConnect() {

		iBeaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
				new UpdateIBeaconSize().execute(iBeacons.size());
				//beaconCount = iBeacons.size();
				//getActionBar().setSubtitle("IBeacon: "+beaconCount);
				//System.out.println(iBeacons.size());
				if (iBeacons.size() > 0) {
					
					
					ArrayList<IBeacon> detectedIBeacons = (ArrayList<IBeacon>) iBeacons;
					
					
					if(IBEACON_SHOWUP_COUNT.size() == 0) {
						//第一次执行将detectedIBeacons装载进Map
						for(IBeacon ibeacon : detectedIBeacons) {
							IBEACON_SHOWUP_COUNT.put(ibeacon, 1);
						}
					} else {
						Integer showupCount;
						ArrayList<IBeacon> removableIBeaconList = new ArrayList<IBeacon>();
						for(IBeacon ib : IBEACON_SHOWUP_COUNT.keySet()) {	//遍历Map
							if(!detectedIBeacons.contains(ib)) {	
								//如果Map节点不在detectedIBeacons里面，说明该Map节点所代表的ibeacon可能已远离或在区域外
								showupCount = IBEACON_SHOWUP_COUNT.get(ib);
								if(showupCount == 0) {	
									//减到0了就删除该Map节点，并检查这个节点是否就是刚popUp的IBeacon，如果是就将应用设置成可弹出状态
									removableIBeaconList.add(ib);
									if(ib.equals(popUpIBeacon)) {
										popUpIBeacon = null;
									}
								} else {	//该Map节点所代表的ibeacon计数-1
									IBEACON_SHOWUP_COUNT.put(ib, IBEACON_SHOWUP_COUNT.get(ib)-1);
								}
							}
						}
						for(IBeacon removableIBeacon : removableIBeaconList) {
							IBEACON_SHOWUP_COUNT.remove(removableIBeacon);
						}
						
						
						for(IBeacon ibeacon : detectedIBeacons) {	//遍历detectedIBeacons
							if(!IBEACON_SHOWUP_COUNT.containsKey(ibeacon)) {	
								//如果detectedIBeacons不在Map里面，说明可能有新的ibeacon靠近或者进入区域
								IBEACON_SHOWUP_COUNT.put(ibeacon, 1);	//计数设为1
							} else {	
								//如果在Map里面，说明ibeacon可能继续在区域里面，计数+1直至达到最小触发值MIN_TRIGGER_COUNT
								showupCount = IBEACON_SHOWUP_COUNT.get(ibeacon);	
								
								if(showupCount < MIN_TRIGGER_COUNT) {
									IBEACON_SHOWUP_COUNT.put(ibeacon, IBEACON_SHOWUP_COUNT.get(ibeacon)+1);
								}
								
							}
						}
						if(DEBUG) {
							if(popUpIBeacon == null) {
								Log.e(LOG_TAG, "popUpIBeacon is null");
							} else {
								uuidSb.setLength(0);
								String currentUUID = uuidSb.append(popUpIBeacon.getProximityUuid()).append(":")
										.append(popUpIBeacon.getMajor()).append(":").append(popUpIBeacon.getMinor()).toString();
								Log.e(LOG_TAG,"currentUUID:" + currentUUID);
								Log.e(LOG_TAG,"popUpIBeacon equal nearestIBeacon: "+popUpIBeacon.equals(detectedIBeacons.get(0)));
								Log.e(LOG_TAG,"isAlertDialogClosed: "+isAlertDialogClosed);
							}
							StringBuilder IBeaconMapString = new StringBuilder("{");
							for(IBeacon ibc: IBEACON_SHOWUP_COUNT.keySet()) {
								IBeaconMapString.append('"').append(getUUIDFromIBeacon(ibc)).append("\": ").append(IBEACON_SHOWUP_COUNT.get(ibc))
								.append(", ");
							}
							IBeaconMapString.append("}");
							Log.e(LOG_TAG, "IBeaconMapString: "+IBeaconMapString.toString());
							Log.e(LOG_TAG,"nearestIBeacon showupCount achieve MIN_TRIGGER_COUNT: "+(IBEACON_SHOWUP_COUNT.get(detectedIBeacons.get(0)) == MIN_TRIGGER_COUNT));
							
							Log.e(LOG_TAG, "FINAL_RESULT: "+((popUpIBeacon == null || 
									(!popUpIBeacon.equals(detectedIBeacons.get(0)) && isAlertDialogClosed == true)
								)  
									&& IBEACON_SHOWUP_COUNT.get(detectedIBeacons.get(0)) == MIN_TRIGGER_COUNT));
						}
						if((popUpIBeacon == null || 
								(!popUpIBeacon.equals(detectedIBeacons.get(0)) && isAlertDialogClosed == true)
							)  
								&& IBEACON_SHOWUP_COUNT.get(detectedIBeacons.get(0)) == MIN_TRIGGER_COUNT) {	
							//如果popUpIBeacon为null且检测到的ibeacon里距离最近的那个已经满足trigger条件
							Log.i(LOG_TAG, "MyIBeaconNotifier start: ");
							//IsEnterRegion = true;
							popUpIBeacon = detectedIBeacons.get(0);	//记录下该次的popUpIBeacon
							IBEACON_SHOWUP_COUNT.put(popUpIBeacon, MIN_TRIGGER_COUNT+10);
							isAlertDialogClosed = false;
							MyIBeaconNotifier(popUpIBeacon);
						}
					}
				}
			}
		});

		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
			@Override
			public void didEnterRegion(Region region) {
				Log.d(LOG_TAG, "You are in Ibeacon Region now");
			}

			@Override
			public void didExitRegion(Region region) {
				Log.i(LOG_TAG, "I no longer see an iBeacon");
			}

			@Override
			public void didDetermineStateForRegion(int state, Region region) {
				Log.i(LOG_TAG, "I have just switched from seeing/not seeing iBeacons: " + state);
			}
		});

		try {
			iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
			iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
		} catch (RemoteException e) {
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		iBeaconManager.checkAvailability();
		connectToService();
	}

	@Override
	public void onStop() {
		Log.d(LOG_TAG, "onStop() executed");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		iBeaconManager.unBind(this);
		// Sets the main View to null
        //mMainView = null;
	}

	private void connectToService() {
		getActionBar().setSubtitle("Scanning...");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		super.onOptionsItemSelected(item);
		setContentView(R.layout.activity_main);

		Bundle args = new Bundle();
		Url = getString(R.string.homeUrl);
		NetWorkFragment netWorkFragment = new NetWorkFragment();

		switch (item.getItemId()) {
		case R.id.action_menu:
			Url = Url + getString(R.string.menuUrl);
			break;
		case R.id.action_cart:
			Url = Url + getString(R.string.cartUrl);
			break;
		case R.id.action_order:
			Url = Url + getString(R.string.orderUrl);
			break;
		case R.id.action_settings:
			Url = Url + getString(R.string.settingUrl);
			break;
		default:
			return false;
		}

		args.putString(NetWorkFragment.REQUEST_URL, Url);
		args.putString(NetWorkFragment.CONTENTTYPE, Constants.CONTENTTYPE_HTML);
		args.putInt(NetWorkFragment.VIEWID, R.id.webview);
		netWorkFragment.setArguments(args);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		transaction.replace(R.id.container, netWorkFragment, Constants.WEBVIEW_FRAGMENT);
		transaction.commit();

		return true;

	}

	private class DownloadImageTask extends AsyncTask<Entry, Void, Entry> {

		@Override
		protected Entry doInBackground(Entry... entrys) {
			Entry e = entrys[0];
			String url = null;
			if(contentType.equals("text/xml")) {
				url = getString(R.string.hostUrl) + e.getFilepath();
			} else if(contentType.equals("text/json")) {
				url = e.getFilepath();
			}
			Bitmap bitmap = st.returnBitMap(url);
			e.setBitmap(bitmap);
			return e;
		}

		protected void onPostExecute(Entry e) {

			displayDish(e);

		}
	}

	private void displayDish(Entry entry) {

		textEntryView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dish_display, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		String dishName = entry.getName();
		String dishDescription = entry.getDescription();
		String dishPrice = entry.getPrice();
		String dishSiteName = entry.getSitename();
		// String dishImagepath =
		// getString(R.string.hostUrl)+entry.getFilepath();
		Bitmap bitmap = entry.getBitmap();

		if(contentType.equals("text/xml")) {
			builder.setTitle(dishName);
		} else if(contentType.equals("text/json")) {
			builder.setTitle(dishSiteName);
		}
		
		builder.setView(textEntryView);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				isAlertDialogClosed = true;
			}
		});
		// image
		ImageView dishImageView = (ImageView) textEntryView.findViewById(R.id.dish_image);
		dishImageView.setContentDescription(dishName);
		dishImageView.setImageBitmap(bitmap);

		// price
		TextView dishPriceView = (TextView) textEntryView.findViewById(R.id.dish_price);
		dishPriceView.setText("Price: " + dishPrice);

		// description
		TextView dishDescriptionView = (TextView) textEntryView.findViewById(R.id.dish_description);
		dishDescriptionView.setText(dishDescription);

		builder.create().show();
	}

	private class DownloadDishesTask extends AsyncTask<String, Void, List<Entry>> {

		private ListView listView;
		private String[] nameArray;
		private List<Entry> dishList;
		private String Url;

		private List<Entry> downloadDishes(String urlString) throws XmlPullParserException, IOException, JSONException {
			InputStream stream = null;
			List<Entry> entries = null;

			try {
				stream = st.getStreamFromUrl(urlString);
				contentType = st.getContentType();
				if(contentType.equals("text/xml")) {
					entries = new DishesXmlParser().parse(stream);
				} else if(contentType.equals("text/json")) {
					entries = new DishesJsonParser().parse(stream);
				} else if(contentType.equals("text/html")) {
					Url = st.getStringFromStream(stream);
				}

			} finally {
				if (stream != null) {
					stream.close();
				}
			}

			return entries;
		}
		
		@Override
		protected List<Entry> doInBackground(String... urls) {
			List<Entry> entries = null;
			try {
				entries = downloadDishes(urls[0]);
				return entries;
			} catch (XmlPullParserException | IOException | JSONException e) {
				return entries;
			}

		}

		@Override
		protected void onPostExecute(List<Entry> result) {
			if (result == null && contentType.equals("text/html")) {
				
				FragmentManager localFragmentManager = MainActivity.this.getSupportFragmentManager();
				
				
				
				NetWorkFragment netWorkFragment = new NetWorkFragment();
				
				Bundle args = new Bundle();
				
				args.putString(NetWorkFragment.REQUEST_URL, Url);
				args.putString(NetWorkFragment.CONTENTTYPE, Constants.CONTENTTYPE_HTML);
				args.putInt(NetWorkFragment.VIEWID, R.id.webview);
				netWorkFragment.setArguments(args);

				FragmentTransaction ft = localFragmentManager.beginTransaction();
				
				ft.replace(R.id.container, netWorkFragment, Constants.RECOMMENDVIEW_FRAGMENT);
				ft.addToBackStack(null);
				ft.commit();
				
				
				
				
				
				
				setFullScreen(true);
				
				
				
				
				
				
				
				
				
				
				return;
			}
			dishCount = result.size();
			//getActionBar().setSubtitle("IBeacon: "+beaconCount);
			if (dishCount == 1) {

				new DownloadImageTask().execute(result.get(0));

			} else if (dishCount > 1) {

				dishList = result;
				nameArray = new String[dishCount];
				int i = 0;
				String listTitle = getString(R.string.dishList);
				for (Entry d : result) {
					nameArray[i] = d.getName();
					if(i == 0 && d.getSitename() != null) {
						listTitle = d.getSitename()+"'s Deals";
					}
					i++;
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(listTitle);
				builder.setView(listView);
				builder.setItems(nameArray, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						Log.d(LOG_TAG, "你选择的id为" + which + " , " + nameArray[which]);
						new DownloadImageTask().execute(dishList.get(which));
					}
				});
				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						isAlertDialogClosed = true;
					}
				});
				builder.create().show();

			} else {
				return;
			}
		}
	}

	
	
	
	/**
     * Sets full screen mode on the device, by setting parameters in the current
     * window and View
     * @param fullscreen
     */
    public void setFullScreen(boolean fullscreen) {
        // If full screen is set, sets the fullscreen flag in the Window manager
        getWindow().setFlags(
                fullscreen ? WindowManager.LayoutParams.FLAG_FULLSCREEN : 0,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Sets the global fullscreen flag to the current setting
        mFullScreen = fullscreen;

        // If the platform version is Android 3.0 (Honeycomb) or above
        if (Build.VERSION.SDK_INT >= 11) {
            
            // Sets the View to be "low profile". Status and navigation bar icons will be dimmed
            int flag = fullscreen ? View.SYSTEM_UI_FLAG_LOW_PROFILE : 0;
            
            // If the platform version is Android 4.0 (ICS) or above
            if (Build.VERSION.SDK_INT >= 14 && fullscreen) {
                
                // Hides all of the navigation icons
                flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            
            // Applies the settings to the screen View
            mMainView.setSystemUiVisibility(flag);

            // If the user requests a full-screen view, hides the Action Bar.
            if ( fullscreen ) {
                this.getActionBar().hide();
            } else {
                this.getActionBar().show();
            }
        }
    }
    
    
    /*
     * A callback invoked when the task's back stack changes. This allows the app to
     * move to the previous state of the Fragment being displayed.
     *
     */
    @Override
    public void onBackStackChanged() {
        
        // Gets the previous global stack count
        int previousStackCount = mPreviousStackCount;
        
        // Gets a FragmentManager instance
        FragmentManager localFragmentManager = getSupportFragmentManager();
        
        // Sets the current back stack count
        int currentStackCount = localFragmentManager.getBackStackEntryCount();
        
        // Re-sets the global stack count to be the current count
        mPreviousStackCount = currentStackCount;
        
        /*
         * If the current stack count is less than the previous, something was popped off the stack
         * probably because the user clicked Back.
         */
        boolean popping = currentStackCount < previousStackCount;
        Log.d(LOG_TAG, "backstackchanged: popping = " + popping);
        
        // When going backwards in the back stack, turns off full screen mode.
        if (popping) {
        	//localFragmentManager.popBackStack();
            setFullScreen(false);
        }
    }
    
    /*
     * This callback is invoked by the system when the Activity is being killed
     * It saves the full screen status, so it can be restored when the Activity is restored
     *
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Constants.EXTENDED_FULLSCREEN, mFullScreen);
        super.onSaveInstanceState(outState);
    }

}
