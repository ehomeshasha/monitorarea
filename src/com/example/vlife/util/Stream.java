package com.example.vlife.util;

import java.util.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Stream {

	
	private Map<String, List<String>> header;
	private InputStream stream;
	
	public String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
	
	public InputStream getStreamFromUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		header = conn.getHeaderFields();
		stream = conn.getInputStream();
		return stream;
	}

	public Bitmap returnBitMap(String url) {
		Bitmap bitmap = null;
		try {
			InputStream is = getStreamFromUrl(url);
			bitmap = BitmapFactory.decodeStream(is);
			is.close();

		} catch (IOException e) {
			e.printStackTrace();

		}
		return bitmap;
	}
	
	
	public String getStringFromStream(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(in)));
		StringBuilder sb = new StringBuilder();
		int c;
		while((c = br.read()) != -1) {
			sb.append((char) c);
		}
		return sb.toString();
	}
	

	public void printHeader() {
		if(header == null) return;
		 
        System.out.println("显示响应Header信息\n");

        for (Map.Entry<String, List<String>> entry : header.entrySet()) {
                System.out.println("Key : " + entry.getKey() + 
                                   " ,Value : " + entry.getValue());
        }

        System.out.println("\n使用key获得响应Header信息 \n");
        List<String> server = header.get("Server");

        if (server == null) {
                System.out.println("Key 'Server' is not found!");
        } else {
                for (String values : server) {
                        System.out.println(values);
                }
        }
		
	}
	public String getContentType() {
		return header.get("Content-Type").get(0);
	}
	
	public Map<String, List<String>> getHeader() {
		return header;
	}

	public InputStream getStream() {
		return stream;
	}
	
	
}