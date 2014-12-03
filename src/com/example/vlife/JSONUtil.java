package com.example.vlife;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.example.vlife.util.Stream;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;



public class JSONUtil {
    
	String jsonString;
	JsonElement element;
	JsonParser parser = new JsonParser();
	
    public JSONUtil(String url) throws MalformedURLException, IOException {
    	jsonString = IOUtils.toString(new URL(url));
    }

    public JSONUtil(InputStream in) throws IOException {
    	Stream st = new Stream();
    	jsonString = st.getStringFromStream(in);
    }
    
    public JsonElement parse() {
    	element = parser.parse(jsonString);
    	return element;
    }
    
}