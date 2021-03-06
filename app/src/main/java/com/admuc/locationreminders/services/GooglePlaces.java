package com.admuc.locationreminders.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;

import com.admuc.locationreminders.BuildConfig;
import com.admuc.locationreminders.models.AutomaticReminder;
import com.admuc.locationreminders.models.Location;
import com.admuc.locationreminders.models.Reminder;
import com.admuc.locationreminders.utils.PlacesAPIRequestBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by 4gray on 26.11.15.
 */
public class GooglePlaces extends AsyncTask<Void, Void, Void> {

    private PlacesCallback callback;

    private String temp;
    private String type;
    private ArrayList venuesList;
    private ArrayAdapter myAdapter;
    private Location location;
    private Context context;
    private SharedPreferences preferences;
    private int radius;

    public GooglePlaces(Location location, Reminder reminder, PlacesCallback callback, Context context) {
        this.location = location;
        this.callback = callback;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        radius = Integer.parseInt(preferences.getString("pref_radius", "200"));

        if (reminder instanceof AutomaticReminder) {
            this.type = ((AutomaticReminder) reminder).getPoi();
        } else {
            this.type = null;
        }
    }

    @Override
    protected Void doInBackground(Void[] params) {

        // make Call to the url
        //temp = makeCall("https://maps.googleapis.com/maps/api/place/search/json?location=" + locationLat + "," + locationLon + "&types=" + type + "&radius=200&sensor=true&key=" + BuildConfig.PLACES_WEB_SERVICE_API);
        temp = makeCall(PlacesAPIRequestBuilder.build(location)
                .setType(type)
                .setRadius(radius)
                .setKey(BuildConfig.PLACES_WEB_SERVICE_API)
                .get());

        //print the call in the console
        System.out.println(PlacesAPIRequestBuilder.build(location)
                .setType(type)
                .setRadius(radius)
                .setKey(BuildConfig.PLACES_WEB_SERVICE_API)
                .get());

        return null;
    }

    @Override
    protected void onPreExecute() {
        // we can start a progress bar here
    }

    @Override
    protected void onPostExecute(Void o) {
        super.onPostExecute(o);

        if (temp != null)
            callback.call(temp);

    }

    public static String makeCall(String url) {

        // string buffers the url
        StringBuffer buffer_string = new StringBuffer(url);
        String replyString = "";

        // instanciate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        // instanciate an HttpGet
        HttpGet httpget = new HttpGet(buffer_string.toString());

        try {
            // get the responce of the httpclient execution of the url
            HttpResponse response = httpclient.execute(httpget);
            InputStream is = response.getEntity().getContent();

            // buffer input stream the result
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            // the result as a string is ready for parsing
            replyString = new String(baf.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(replyString);

        // trim the whitespaces
        return replyString.trim();
    }

}
