package com.example.saad.hci;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;


public class ListDisplayActivity extends Activity implements AdapterView.OnItemClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Class_MyApplication             app;
    ListView                        mainListView;
    Class_MyListAdapter             myListAdapter;
    Class_MyListAdapter2            myTrailListAdapter;
    String                          mode;
    ArrayList<Class_HeritageSite>   searched_HeritageSites;
    boolean                         displaySearch;
    GoogleApiClient                 mGoogleApiClient;
    Location                        mLastLocation;
    Location                        mCurrentLocation;
    LocationRequest                 mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_display);
        displaySearch = false;

        // reference the MyApplication variable to access global variables.
        app = (Class_MyApplication) getApplication();

        // reference the ListView
        mainListView = (ListView) findViewById(R.id.display_listview);

        // check which Mode to display
        Intent intent = getIntent();
        mode = intent.getStringExtra("MODE");

        myListAdapter = new Class_MyListAdapter(this, getLayoutInflater());
        mainListView.setAdapter(myListAdapter);
        mainListView.setOnItemClickListener(this);

        // Title
        TextView textView = (TextView) findViewById(R.id.main_title);

        // create the Adapter for the ListView.
        if (mode.equals("POPULAR")) {
            // sort the Heritage Site data by popularity.
            Collections.sort(app.all_HeritageSites, Class_HeritageSite.PopularityComparator);
            myListAdapter.updateData(app.all_HeritageSites);
            textView.setText("Popular");
        } else if (mode.equals("NEAREST")) {
            // sort the Heritage Site data by nearest position.
            Class_HeritageSite.NearestSort(app.all_HeritageSites, app.curLoc);
            myListAdapter.updateData(app.all_HeritageSites);
            textView.setText("Nearest");
        } else if (mode.equals("TRAIL")) {
            // create the Trail List Adapter
            myTrailListAdapter = new Class_MyListAdapter2(this, getLayoutInflater(), mode);
            mainListView.setAdapter(myTrailListAdapter);
            myTrailListAdapter.updateData(app.all_Trails);
            textView.setText("Trails");
        } else if (mode.equals("SEARCH")) {
            // find the Heritage Sites associated with a Tag.
            searched_HeritageSites          = new ArrayList<>();
            String tag_name                 = intent.getStringExtra("NAME");
            textView.setText(tag_name);
            Class_Tag tag                   = app.findTag(tag_name);
            ArrayList<String> tag_siteNames = tag.getSiteNames();

            for (String siteName : tag_siteNames) {
                Class_HeritageSite heritageSite = app.findHeritageSite(siteName);
                searched_HeritageSites.add(heritageSite);
            }

            // sort the Heritage Site data by nearest position.
            Class_HeritageSite.NearestSort(searched_HeritageSites, app.curLoc);
            myListAdapter.updateData(searched_HeritageSites);
        } else if (mode.equals("FAVOURITES")) {
            // find the Heritage Sites in Favourites.
            searched_HeritageSites          = new ArrayList<>();
            textView.setText("Favourites");

            for (String siteName : app.all_favourites) {
                Class_HeritageSite heritageSite = app.findHeritageSite(siteName);
                searched_HeritageSites.add(heritageSite);
            }

            // sort the Heritage Site data by nearest position.
            Class_HeritageSite.NearestSort(searched_HeritageSites, app.curLoc);
            myListAdapter.updateData(searched_HeritageSites);
        }

        mainListView.setSelectionFromTop(0, 0);

        // Search Bar
        setupSearch();

        // GPS
        buildGoogleApiClient();
        createLocationRequest();
    }

    public void setupSearch() {
        AutoCompleteTextView searchbar = (AutoCompleteTextView) findViewById(R.id.main_search);
        String[] s = new String[app.all_HeritageSites.size()];

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, app.all_searchables.toArray(s));
        searchbar.setAdapter(adapter);
        searchbar.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(null);
                    return true;
                }
                return false;
            }
        });
    }

    public void performSearch(View v) {
        if (displaySearch == false) {
            displaySearch = true;
            AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.main_search);
            textView.setVisibility(View.VISIBLE);
            (findViewById(R.id.main_title)).setVisibility(View.INVISIBLE);
            if (textView.requestFocus()) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.main_search);
            String text = textView.getText().toString();
            if (app.all_searchables.contains(text)) {
                Intent intent;
                if (app.isTag(text)) {
                    intent = new Intent(this, ListDisplayActivity.class);
                    intent.putExtra("MODE", "SEARCH");
                    intent.putExtra("NAME", text);
                    Toast.makeText(this, "Tag Found", Toast.LENGTH_SHORT).show();
                } else if (app.isTrail(text)) {
                    intent = new Intent(this, TrailDisplayActivity.class);
                    intent.putExtra("NAME", text);
                    Toast.makeText(this, "Trail Found", Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(this, SiteDisplayActivity.class);
                    intent.putExtra("NAME", text);
                    Toast.makeText(this, "Site Found", Toast.LENGTH_SHORT).show();
                }
                startActivity(intent);
            } else {
                Toast.makeText(this, "Does Not Exist", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mode.equals("TRAIL")) {
            Class_Trail trail = (Class_Trail) myTrailListAdapter.getItem(position);

            Intent intent = new Intent(this, TrailDisplayActivity.class);
            intent.putExtra("NAME", trail.getName());
            Toast.makeText(this, "Trail Found", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        } else {
            Class_HeritageSite heritageSite = (Class_HeritageSite) myListAdapter.getItem(position);

            Intent intent = new Intent(this, SiteDisplayActivity.class);
            intent.putExtra("NAME", heritageSite.getName());
            Toast.makeText(this, "Site Found", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSearchBar();
        if (mode.equals("POPULAR")) {
            // sort the Heritage Site data by popularity.
            Collections.sort(app.all_HeritageSites, Class_HeritageSite.PopularityComparator);
            myListAdapter.updateData(app.all_HeritageSites);
        } else if (mode.equals("NEAREST")) {
            // sort the Heritage Site data by nearest position.
            Class_HeritageSite.NearestSort(app.all_HeritageSites, app.curLoc);
            myListAdapter.updateData(app.all_HeritageSites);
        } else if (mode.equals("FAVOURITES")) {
            searched_HeritageSites.clear();
            for (String siteName : app.all_favourites) {
                Class_HeritageSite heritageSite = app.findHeritageSite(siteName);
                searched_HeritageSites.add(heritageSite);
            }
            // sort the Heritage Site data by nearest position.
            Class_HeritageSite.NearestSort(searched_HeritageSites, app.curLoc);
            myListAdapter.updateData(searched_HeritageSites);
        }
        mGoogleApiClient.connect();
    }

    public void goHome(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void hideSearchBar() {
        displaySearch = false;
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.main_search);
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(textView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        textView.setVisibility(View.GONE);
        textView.setText("");
        findViewById(R.id.main_title).setVisibility(View.VISIBLE);
    }

    public void performAugmented(View v) {
        Toast.makeText(this, "Augmented", Toast.LENGTH_SHORT).show();
    }




    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        app.setLoc(new float[] {(float)mLastLocation.getLatitude(), (float)mLastLocation.getLongitude()});
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            app.setLoc(new float[] {(float)mLastLocation.getLatitude(), (float)mLastLocation.getLongitude()});
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Error: Could not connect to GPS", Toast.LENGTH_LONG).show();
    }
}
