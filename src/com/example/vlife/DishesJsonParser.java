package com.example.vlife;


import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses JSON feeds from vlife website.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the JSON feed.
 */
public class DishesJsonParser {
    

    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException, JSONException {
        try {
        	//JSONUtil json = new JSONUtil("http://u17892083.onlinehome-server.com/vlife/index.php?home=misc&act=get_recommend_dishes&uuid=b9407f30-f5f8-466e-aff9-25556b57fe6d:2260:3116");
        	JSONUtil json = new JSONUtil(in);
        	JsonElement element = json.parse();
        	return readDishes(element);
        } finally {
            in.close();
        }
    }

    private List<Entry> readDishes(JsonElement element) throws XmlPullParserException, IOException {
    	
    	List<Entry> entries = new ArrayList<Entry>();
    	Entry entry = null;
        
    	if (element.isJsonObject()) {
			JsonObject root = element.getAsJsonObject();
			JsonObject response = root.get("response").getAsJsonObject();
			JsonArray docs = response.getAsJsonArray("docs");
			
			for (int i = 0; i < docs.size(); i++) {
				JsonObject doc = docs.get(i).getAsJsonObject();
				entry = readDish(doc);
				entries.add(entry);
			}
		}
    	
        
        return entries;
    }

	private Entry readDish(JsonObject doc) {
		Entry entry = new Entry();
		String name = doc.get("dw_subject").getAsString();
		if(name.length() > 50) {
			name = name.substring(0, 50)+"...";
		}
		entry.setName(name);
		entry.setDescription(doc.get("dw_subject").getAsString());
		entry.setFilepath(doc.get("dw_thumb").getAsString());
		entry.setPrice(doc.get("dw_nowprice").getAsString());
		entry.setSitename(doc.get("dw_groupsite").getAsString());
		return entry;
	}


    
}
