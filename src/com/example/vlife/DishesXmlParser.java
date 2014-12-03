package com.example.vlife;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses XML feeds from vlife website.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class DishesXmlParser {
    

    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, "UTF-8");
            return readDishes(parser);
        } finally {
            in.close();
        }
    }

    private List<Entry> readDishes(XmlPullParser parser) throws XmlPullParserException, IOException {
    	
    	List<Entry> entries = null;
    	Entry entry = null;
        
        int eventType = parser.getEventType();  
        while (eventType != XmlPullParser.END_DOCUMENT) {  
            switch (eventType) {  
            case XmlPullParser.START_DOCUMENT:  
            	entries = new ArrayList<Entry>();
                break;  
            case XmlPullParser.START_TAG:  
                if (parser.getName().equals("dish")) {  
                	entry = new Entry();  
                } else if (parser.getName().equals("name")) {  
                    eventType = parser.next();  
                    entry.setName(parser.getText());
                } else if (parser.getName().equals("description")) {  
                    eventType = parser.next();  
                    entry.setDescription(parser.getText());  
                } else if (parser.getName().equals("price")) {  
                    eventType = parser.next();  
                    entry.setPrice(parser.getText());  
                } else if (parser.getName().equals("filepath")) {  
                    eventType = parser.next();  
                    entry.setFilepath(parser.getText());  
                }  
                break;  
            case XmlPullParser.END_TAG:  
                if (parser.getName().equals("dish")) {  
                	entries.add(entry);  
                	entry = null;      
                }  
                break;  
            }  
            eventType = parser.next();  
        }  
        
        return entries;
    }

}
