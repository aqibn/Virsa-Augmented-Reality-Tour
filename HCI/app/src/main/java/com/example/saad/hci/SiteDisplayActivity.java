package com.example.saad.hci;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;


public class SiteDisplayActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Class_MyApplication             app;
    Class_HeritageSite              heritageSite;
    boolean                         displaySearch;
    SliderLayout                    mDemoSlider;
    GoogleApiClient                 mGoogleApiClient;
    Location                        mLastLocation;
    Location                        mCurrentLocation;
    LocationRequest                 mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_display);
        displaySearch = false;

        // reference the MyApplication variable to access global variables.
        app = (Class_MyApplication) getApplication();

        // check which Site to display
        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME");
        heritageSite = app.findHeritageSite(name);

        // Slider
        mDemoSlider = (SliderLayout)findViewById(R.id.main_slider);
        setupSlider();
        PagerIndicator pagerIndicator = (PagerIndicator)findViewById(R.id.custom_indicator);
        mDemoSlider.setCustomIndicator(pagerIndicator);
        mDemoSlider.setDuration(3);
        mDemoSlider.setCurrentPosition(heritageSite.getImages().size()-1);

        // Search Bar
        setupSearch();

        // Title
        TextView textView = (TextView) findViewById(R.id.main_title);
        textView.setText(name);

        // Data
        setupData();

        // GPS
        buildGoogleApiClient();
        createLocationRequest();
    }

    public void setupData() {
        ((TextView)(findViewById(R.id.name))).setText(heritageSite.getName());

        if (app.all_favourites.contains(heritageSite.getName())) {
            ((ImageView)(findViewById(R.id.favourite))).setImageResource(R.drawable.icon_favourite_full);
        } else {
            ((ImageView)(findViewById(R.id.favourite))).setImageResource(R.drawable.icon_favourite_empty);
        }

        int f = 5;
        while (heritageSite.getPopularity() < f) {
            String ID = "popularity_icon" + f;
            f--;
            int resID = getResources().getIdentifier(ID, "id", getPackageName());
            findViewById(resID).setVisibility(View.INVISIBLE);
        }

        float dist = (float)(((int)(heritageSite.calculateDistance(app.curLoc) * 10))/10.0);
        if (dist < 1000) {
            ((TextView)(findViewById(R.id.distance))).setText(String.format("%.1f", dist) + " m");
        } else {
            dist = dist / 1000;
            ((TextView)(findViewById(R.id.distance))).setText(String.format("%.1f", dist) + " km");
        }

        ArrayList<String> tags = heritageSite.getTags();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            StringBuilder temp = new StringBuilder(tags.get(i));
            stringBuilder.append(temp.substring(2, temp.length()-2));
            if (i != tags.size()-1) {
                stringBuilder.append(", ");
            }
        }
        ((TextView)(findViewById(R.id.tags_2))).setText(stringBuilder);

        if (heritageSite.getDescription().equals("")) {
            findViewById(R.id.description).setVisibility(View.GONE);
            findViewById(R.id.description2).setVisibility(View.GONE);
        } else {
            ((TextView)(findViewById(R.id.description))).setText(heritageSite.getDescription());
        }

        Class_HeritageSite.NearestSort(app.all_HeritageSites, heritageSite.getGPS());
        int j = 0;
        for (int i = 1; i < 4; i++) {
            Class_HeritageSite tempSite;
            if (heritageSite.getName().equals(app.all_HeritageSites.get(j).getName())) {
                j++;
            }
            tempSite = app.all_HeritageSites.get(j);
            String ID = "imagebox_image_" + i;
            int resID = getResources().getIdentifier(ID, "id", getPackageName());
            int image_id = getResources().getIdentifier(tempSite.getImageMain(), "drawable", getPackageName());
            ((ImageView)(findViewById(resID))).setImageResource(image_id);

            ID = "imagebox_text_" + i;
            resID = getResources().getIdentifier(ID, "id", getPackageName());
            ((TextView)(findViewById(resID))).setText(tempSite.getName());
            j++;
        }

        j = 3 + Math.min(3, heritageSite.getTrails().size());
        if (j == 3) {
            findViewById(R.id.section_three).setVisibility(View.GONE);
        } else {
            for (int i = 4; i < 7; i++) {
                if (i > j) {
                    String ID = "imagebox_" + i;
                    int resID = getResources().getIdentifier(ID, "id", getPackageName());
                    findViewById(resID).setVisibility(View.INVISIBLE);
                } else {
                    Class_Trail tempTrail;
                    tempTrail = app.findTrail(heritageSite.getTrails().get(i-4));
                    String ID = "imagebox_image_" + i;
                    int resID = getResources().getIdentifier(ID, "id", getPackageName());
                    int image_id = getResources().getIdentifier(tempTrail.getFirstImageSrc(), "drawable", getPackageName());
                    ((ImageView)(findViewById(resID))).setImageResource(image_id);

                    ID = "imagebox_text_" + i;
                    resID = getResources().getIdentifier(ID, "id", getPackageName());
                    ((TextView)(findViewById(resID))).setText(tempTrail.getName());
                }
            }
        }
    }

    public void toggleFavourite(View v) {
        if (app.all_favourites.contains(heritageSite.getName())) {
            Toast.makeText(this, "Removed from Favourites", Toast.LENGTH_SHORT).show();
            app.all_favourites.remove(heritageSite.getName());
            ((ImageView)(findViewById(R.id.favourite))).setImageResource(R.drawable.icon_favourite_empty);
        } else {
            Toast.makeText(this, "Added to Favourites", Toast.LENGTH_SHORT).show();
            app.all_favourites.add(heritageSite.getName());
            ((ImageView)(findViewById(R.id.favourite))).setImageResource(R.drawable.icon_favourite_full);
        }
    }

    public void setupSlider() {
        ArrayList<String> images = heritageSite.getImages();
        for (String image : images) {
            int image_id = getResources().getIdentifier(image, "drawable", getPackageName());
            DefaultSliderView defaultSliderView = new DefaultSliderView(this);
            defaultSliderView.image(image_id);
            mDemoSlider.addSlider(defaultSliderView);
        }
        if (images.size() <= 1) {
            mDemoSlider.stopAutoCycle();
        }
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
        getMenuInflater().inflate(R.menu.menu_site_display, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSearchBar();
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

    public void redirectDirections(View v) {
        Toast.makeText(this, "Directions", Toast.LENGTH_SHORT).show();
    }

    public void redirectTour(View v) {
        Intent intent = new Intent(this, AugmentedActivity.class);
        startActivity(intent);
    }

    public void goToSite(View v) {
        Intent intent = new Intent(this, SiteDisplayActivity.class);
        int curID = v.getId();

        if (curID == R.id.imagebox_1) {
            String temp = ((TextView)(findViewById(R.id.imagebox_text_1))).getText().toString();
            intent.putExtra("NAME", temp);
            Toast.makeText(this, temp, Toast.LENGTH_SHORT).show();
        } else if (curID == R.id.imagebox_2) {
            String temp = ((TextView)(findViewById(R.id.imagebox_text_2))).getText().toString();
            intent.putExtra("NAME", temp);
            Toast.makeText(this, temp, Toast.LENGTH_SHORT).show();
        } else if (curID == R.id.imagebox_3) {
            String temp = ((TextView)(findViewById(R.id.imagebox_text_3))).getText().toString();
            intent.putExtra("NAME", temp);
            Toast.makeText(this, temp, Toast.LENGTH_SHORT).show();
        }
        startActivity(intent);
    }

    public void goToTrail(View v) {
        Intent intent = new Intent(this, TrailDisplayActivity.class);
        int curID = v.getId();

        if (curID == R.id.imagebox_4) {
            String temp = ((TextView)(findViewById(R.id.imagebox_text_4))).getText().toString();
            intent.putExtra("NAME", temp);
            Toast.makeText(this, temp, Toast.LENGTH_SHORT).show();
        } else if (curID == R.id.imagebox_5) {
            String temp = ((TextView)(findViewById(R.id.imagebox_text_5))).getText().toString();
            intent.putExtra("NAME", temp);
            Toast.makeText(this, temp, Toast.LENGTH_SHORT).show();
        } else if (curID == R.id.imagebox_6) {
            String temp = ((TextView)(findViewById(R.id.imagebox_text_6))).getText().toString();
            intent.putExtra("NAME", temp);
            Toast.makeText(this, temp, Toast.LENGTH_SHORT).show();
        }
        startActivity(intent);
    }

    public void viewText(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Description");

        // Set up the display
        ScrollView scrollView = (ScrollView) LayoutInflater.from(this).inflate(R.layout.dialogbox, null);
        ((TextView)scrollView.findViewById(R.id.dialogbox1)).setText(heritageSite.getDescription());
        builder.setView(scrollView);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void nothing(View v) {}




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
