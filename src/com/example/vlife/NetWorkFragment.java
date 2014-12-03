package com.example.vlife;


import com.example.vlife.R;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class NetWorkFragment extends Fragment {

	private static final String LOG_TAG = "WebViewFragment";
	public static final String WIFI = "Wi-Fi";
	public static final String ANY = "Any";
	
	
	
	//args key from activity
	public static final String REQUEST_URL = Constants.REQUEST_URL;
	public static final String CONTENTTYPE = Constants.CONTENTTYPE;
	public static final String VIEWID = Constants.VIEWID;
	//args value from activity
	private String RequestURL;
	private String ContentType;
	private int viewId;
	// Whether there is a Wi-Fi connection.
	public static boolean wifiConnected = false;
	// Whether there is a mobile connection.
	public static boolean mobileConnected = false;
	// Whether the display should be refreshed.
	public static boolean refreshDisplay = true;

	// The BroadcastReceiver that tracks network connectivity changes.
	private NetworkReceiver receiver;
	//
	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	public int getViewId() {
		return viewId;
	}

	public void setViewId(int viewId) {
		this.viewId = viewId;
	}

	public String getContentType() {
		return ContentType;
	}

	public void setContentType(String contentType) {
		ContentType = contentType;
	}
	
	public String getRequestURL() {
		return RequestURL;
	}

	public void setRequestURL(String requestURL) {
		RequestURL = requestURL;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreateView() executed");
		
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		receiver = new NetworkReceiver();
		getActivity().registerReceiver(receiver, filter);
		
		return inflater.inflate(R.layout.fragment_webview, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(LOG_TAG, "onStart() executed");
		// During startup, check if there are arguments passed to the fragment.
		// onStart is a good place to do this because the layout has already
		// been
		// applied to the fragment at this point so we can safely call the
		// method
		// below that sets the article text.
		
		
		Bundle args = getArguments();
		if (args != null) {
			// Set article based on argument passed in
			RequestURL = args.getString(REQUEST_URL);
			ContentType = args.getString(CONTENTTYPE);
			viewId = args.getInt(VIEWID);
		} else {
			Toast.makeText(getActivity(), "No Request Url recieved, program failed!", Toast.LENGTH_SHORT).show();
		}

		updateConnectedFlags();

		if (refreshDisplay) {
			loadPage();
		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		if (receiver != null) {
			Log.d(LOG_TAG, "onDestroy() executed and receiver is not null");
			getActivity().unregisterReceiver(receiver);
		} else {
			Log.d(LOG_TAG, "onDestroy() executed and receiver is null");
		}
		//Log.d(LOG_TAG, "onDestroy() executed");
	}

	private void updateConnectedFlags() {
		ConnectivityManager connMgr = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		if (activeInfo != null && activeInfo.isConnected()) {
			wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
			mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
		} else {
			wifiConnected = false;
			mobileConnected = false;
		}
	}

	public void loadPage() {
		if (wifiConnected || mobileConnected) {
			
			WebView mView = (WebView) getActivity().findViewById(viewId);
			
			if(ContentType.equals(Constants.CONTENTTYPE_HTML)) {
				//contentType is html, so use loadUrl to get content.
				
				mView.setWebViewClient(new MyWebViewClient());
				WebSettings webSettings = mView.getSettings();
				webSettings.setJavaScriptEnabled(true);
				mView.loadUrl(RequestURL);
			} else if(ContentType.equals(Constants.CONTENTTYPE_XML)) {
				//if contentType is xml, download xml content and parse
				
				
				
				
			} else if(ContentType.equals(Constants.CONTENTTYPE_JSON)) {
				//if contentType is json, download json content and parse
				
			}

		} else {
			showErrorPage();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	// Displays an error if the app is unable to load content.
	private void showErrorPage() {
		getActivity().setContentView(R.layout.fragment_webview);

		// The specified network connection is not available. Displays error
		// message.
		WebView myWebView = (WebView) getActivity().findViewById(R.id.webview);
		myWebView.loadData(getResources().getString(R.string.connection_error), "text/html", null);
	}

	/**
	 * 
	 * This BroadcastReceiver intercepts the
	 * android.net.ConnectivityManager.CONNECTIVITY_ACTION, which indicates a
	 * connection change. It checks whether the type is TYPE_WIFI. If it is, it
	 * checks whether Wi-Fi is connected and sets the wifiConnected flag in the
	 * main activity accordingly.
	 * 
	 */
	public class NetworkReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			// Checks the user prefs and the network connection. Based on the
			// result, decides
			// whether
			// to refresh the display or keep the current display.
			// If the userpref is Wi-Fi only, checks to see if the device has a
			// Wi-Fi connection.
			if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				// If device has its Wi-Fi connection, sets refreshDisplay
				// to true. This causes the display to be refreshed when the
				// user
				// returns to the app.
				refreshDisplay = true;
				// Toast.makeText(context, R.string.wifi_connected,
				// Toast.LENGTH_SHORT).show();

				// If the setting is ANY network and there is a network
				// connection
				// (which by process of elimination would be mobile), sets
				// refreshDisplay to true.
			} else if (networkInfo != null) {
				refreshDisplay = true;

				// Otherwise, the app can't download content--either because
				// there is no network
				// connection (mobile or Wi-Fi), or because the pref setting is
				// WIFI, and there
				// is no Wi-Fi connection.
				// Sets refreshDisplay to false.
			} else {
				refreshDisplay = false;
				// Toast.makeText(context, R.string.lost_connection,
				// Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
			/*
			if (Uri.parse(url).getHost().equals(getActivity().getString(R.string.host))) {
				// This is my web site, so do not override; let my WebView load
				// the page
				return false;
			}
			// Otherwise, the link is not for a page on my site, so launch
			// another Activity that handles URLs
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
			*/
		}
	}
}