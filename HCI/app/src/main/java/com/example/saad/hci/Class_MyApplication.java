package com.example.saad.hci;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Class_MyApplication extends Application {
    public SharedPreferences                sharedPreferences;
    public ArrayList<Class_HeritageSite>    all_HeritageSites   = new ArrayList<>();
    public ArrayList<Class_Trail>           all_Trails          = new ArrayList<>();
    public ArrayList<Class_Tag>             all_Tags            = new ArrayList<>();
    public ArrayList<String>                all_searchables     = new ArrayList<>();
    public ArrayList<String>                all_favourites      = new ArrayList<>();
    public float[]                          curLoc              = new float[] {0, 0};

    @Override
    public void onCreate()
    {
        super.onCreate();

        // create HeritageSite variables by reading JSON data from file.
        populateAllHeritageSites();

        // create Trail variables by reading JSON data from file.
        populateAllTrails();

        // create Tag variables by reading JSON data from file.
        populateAllTags();

        // populate Favourites
        populateAllFavourites();
    }

    public void populateAllHeritageSites() {
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("HeritageSiteData.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line, strJson = "";

            while ((line = reader.readLine()) != null)
                strJson += line;

            JSONObject jsonRootObject = new JSONObject(strJson);
            JSONArray jsonArray = jsonRootObject.optJSONArray("data");
            all_HeritageSites.ensureCapacity(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                all_HeritageSites.add(new Class_HeritageSite(jsonObject));
                all_searchables.add(all_HeritageSites.get(i).getName());
            }

        } catch (Exception e) {
            Log.d("mylog", e.toString());
        }
    }

    public void populateAllTrails() {
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("TrailData.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line, strJson = "";

            while ((line = reader.readLine()) != null)
                strJson += line;

            JSONObject jsonRootObject = new JSONObject(strJson);
            JSONArray jsonArray = jsonRootObject.optJSONArray("data");
            all_Trails.ensureCapacity(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                Class_Trail trail = new Class_Trail(jsonObject);
                all_Trails.add(trail);
                all_searchables.add(trail.getName());

                ArrayList<String> siteNames = trail.getSiteNames();
                for (String site : siteNames) {
                    Class_HeritageSite heritageSite = findHeritageSite(site);
                    heritageSite.addTrail(trail.getName());
                }
            }

        } catch (Exception e) {
            Log.d("mylog", e.toString());
        }
    }

    public void populateAllTags() {
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("TagData.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line, strJson = "";

            while ((line = reader.readLine()) != null)
                strJson += line;

            JSONObject jsonRootObject = new JSONObject(strJson);
            JSONArray jsonArray = jsonRootObject.optJSONArray("data");
            all_Tags.ensureCapacity(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                Class_Tag tag = new Class_Tag(jsonObject);
                all_Tags.add(tag);
                all_searchables.add(tag.getName());

                ArrayList<String> siteNames = tag.getSiteNames();
                for (String site : siteNames) {
                    Class_HeritageSite heritageSite = findHeritageSite(site);
                    heritageSite.addTag(tag.getName());
                }
            }

        } catch (Exception e) {
            Log.d("mylog", e.toString());
        }
    }

    public void populateAllFavourites() {
        sharedPreferences = getSharedPreferences("Virsa", MODE_PRIVATE);
        int numFavourites = sharedPreferences.getInt("num", 0);

        for (int i = 0; i < numFavourites; i++) {
            String num = String.format("%03d", i);
            String name = sharedPreferences.getString(num, "");

            if (!name.equals("")) {
                all_favourites.add(name);
            }
        }
    }

    public boolean isTag(String name) {
        for (Class_Tag tag : all_Tags) {
            if (tag.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Class_Tag findTag(String name) {
        for (Class_Tag tag : all_Tags) {
            if (tag.getName().equals(name)) {
                return tag;
            }
        }
        return null;
    }

    public boolean isTrail(String name) {
        for (Class_Trail trail : all_Trails) {
            if (trail.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Class_Trail findTrail(String name) {
        for (Class_Trail trail : all_Trails) {
            if (trail.getName().equals(name)) {
                return trail;
            }
        }
        return null;
    }

    public Class_HeritageSite findHeritageSite(String name) {
        for (Class_HeritageSite heritageSite : all_HeritageSites) {
            if (heritageSite.getName().equals(name)) {
                return heritageSite;
            }
        }
        return null;
    }

    public float[] getLoc() {return curLoc;}

    public void setLoc(float[] newLoc) {curLoc = newLoc;}
}
