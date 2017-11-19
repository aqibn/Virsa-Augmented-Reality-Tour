package com.example.saad.hci;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Class_Trail {
    private String 	            name;
    private ArrayList<String>   imageSrc;
    private ArrayList<String>   siteNames;
    private int                 popularity;
    private int                 numPlaces;

    public Class_Trail(JSONObject jsonObject) {
        imageSrc 	            = new ArrayList<>();
        siteNames               = new ArrayList<>();
        name 		            = jsonObject.optString("name");
        JSONArray imageSource   = jsonObject.optJSONArray("image_src");
        JSONArray siteNames1    = jsonObject.optJSONArray("site_names");

        for (int i = 0; i < imageSource.length(); i++) {
            imageSrc.add(imageSource.optString(i));
            siteNames.add(siteNames1.optString(i));
        }

        popularity  = jsonObject.optInt("popularity");
        numPlaces   = jsonObject.optInt("numPlaces");
    }

    public String 	            getName() 			    {return name;}
    public ArrayList<String>    getSiteNames()          {return siteNames;}
    public ArrayList<String>    getImageSrc()           {return imageSrc;}
    public String               getFirstImageSrc()      {return imageSrc.get(0);}
    public int                  getPopularity()         {return popularity;}
    public int                  getNumPlaces()          {return numPlaces;}
}
