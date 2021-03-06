package com.admuc.locationreminders.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.admuc.locationreminders.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SelectLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker locationMarker;
    private String locationDescription;
    private MarkerOptions options = null;
    private Boolean locationDetected = false;
    private LatLng searchedLocation;
    private MenuItem selectedMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_18dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private String getAddressFromLatLng( LatLng latLng ) {
        Geocoder geocoder = new Geocoder(this);

        String address = "";
        try {
            address = geocoder
                    .getFromLocation( latLng.latitude, latLng.longitude, 1 )
                    .get( 0 ).getAddressLine( 0 );
        } catch (IOException e ) {
        }

        return address;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
               setMarker(latLng);
            }
        });

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location location) {

                // move camera only first time by location detection
                if (locationDetected == false) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    LatLng ll = new LatLng(lat, lng);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 20));
                    locationDetected = true;
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);

        selectedMenuItem = menu.findItem(R.id.setLocation);
        selectedMenuItem.setVisible(false);


        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                onSearchRequested();
                Log.d("Search query: ", query);

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocationName(query, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (addresses.size() > 0) {
                    double latitude = addresses.get(0).getLatitude();
                    double longitude = addresses.get(0).getLongitude();
                    Log.d("lat: ", String.valueOf(latitude));
                    Log.d("lat: ", String.valueOf(longitude));
                    searchedLocation = new LatLng(latitude, longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 15));

                    setMarker(searchedLocation);

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO Auto-generated method stub
                Log.d("new query: ", newText);
                return false;
            }
        });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.setLocation) {

            Intent intent = new Intent(getApplicationContext(), ManageActivity.class);
            Log.d("select lat;: ", String.valueOf(locationMarker.getPosition().latitude));
            intent.putExtra("REMINDER_LAT", locationMarker.getPosition().latitude);
            intent.putExtra("REMINDER_LON", locationMarker.getPosition().longitude);
            intent.putExtra("REMINDER_LOCATION", locationDescription);
            setResult(RESULT_OK, intent);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setMarker(LatLng latLng) {
        locationDescription = getAddressFromLatLng(latLng);

        if (locationMarker == null) {
            options = new MarkerOptions().position(latLng).title(locationDescription);
            locationMarker = mMap.addMarker(options);
            selectedMenuItem.setVisible(true);
        } else {
            //update marker
            locationMarker.setPosition(latLng);
            locationMarker.setTitle(locationDescription);

        }
    }
}
