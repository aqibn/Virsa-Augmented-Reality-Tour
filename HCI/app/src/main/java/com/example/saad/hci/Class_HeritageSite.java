package com.example.saad.hci;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;

public class Class_HeritageSite {
    private String 	            name;
    private String              address;
    private ArrayList<String>   images;
    private float[]             GPS;
    private float               popularity;
    private ArrayList<String>   tags;
    private ArrayList<String>   trails;
    private String              description;

    public Class_HeritageSite(JSONObject jsonObject) {
  		name 		= jsonObject.optString("name");
        address 	= jsonObject.optString("address");
  		GPS 		= new float[] {(float)jsonObject.optDouble("lat"), (float)jsonObject.optDouble("long")};
        popularity  = (float)jsonObject.optDouble("popularity");
        images      = new ArrayList<>();
        tags        = new ArrayList<>();
        trails      = new ArrayList<>();
        JSONArray imageSource = jsonObject.optJSONArray("image_src");
        for (int i = 0; i < imageSource.length(); i++) {
            images.add(imageSource.optString(i));
        }
        description = jsonObject.optString("description");
  	}

    public String 	            getName() 						{return name;}
    public String 	            getAddress() 					{return address;}
    public String               getImageMain() 					{return images.get(0);}
    public String               getImageHor() 					{if (images.size() == 1) {return images.get(0);} else {return images.get(1);}}
    public ArrayList<String>    getImages()                     {return images;}
    public float[]              getGPS()						{return GPS;}
    public float                getPopularity()					{return popularity;}
    public void                 addTag(String tag)              {tags.add(tag);}
    public void                 addTrail(String trail)          {trails.add(trail);}
    public ArrayList<String>    getTags()                       {return tags;}
    public ArrayList<String>    getTrails()                     {return trails;}
    public String               getDescription()                {return description;}
    public float                calculateDistance(float[] _GPS) {
        Location loc1 = new Location("");
        loc1.setLatitude(GPS[0]);
        loc1.setLongitude(GPS[1]);

        Location loc2 = new Location("");
        loc2.setLatitude(_GPS[0]);
        loc2.setLongitude(_GPS[1]);

        return loc1.distanceTo(loc2);
    }

    public static Comparator<Class_HeritageSite> PopularityComparator = new Comparator<Class_HeritageSite>() {
        public int compare(Class_HeritageSite hs1, Class_HeritageSite hs2) {
            return (int)((hs2.getPopularity() - hs1.getPopularity())*10);
        }
    };

    public static void NearestSort(ArrayList<Class_HeritageSite> all_HeritageSites, float[] curLoc) {
        for (int i = 0; i < all_HeritageSites.size(); i++) {
            int j = i;
            while (j > 0 && all_HeritageSites.get(j).calculateDistance(curLoc) < all_HeritageSites.get(j-1).calculateDistance(curLoc)) {
                Class_HeritageSite temp = all_HeritageSites.get(j);
                all_HeritageSites.set(j, all_HeritageSites.get(j-1));
                all_HeritageSites.set(j-1, temp);
                j--;
            }
        }
    }
}