package com.penguinsonabeach.shadetree.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.penguinsonabeach.shadetree.R;
import com.penguinsonabeach.shadetree.network.Config;
import com.penguinsonabeach.shadetree.util.PermissionUtils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class Main extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener,GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    //Variable Initialization
    protected static final String TAG = "MainActivity";
    private GoogleMap mMap;
    private String[] mUserOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String User_Id,First_Name,Last_Name,email,sender;
    int counter;
    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    RequestQueue requestQueue;
    //Initialize GoogleApiClient
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    // Lat Long Values
    protected double mLatitude,mLongitude;
    private ActionBarDrawerToggle mDrawerToggle;
    ShareDialog shareDialog = new ShareDialog(this);
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    final Context context = this;



    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        requestQueue = Volley.newRequestQueue(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_home);
        facebookSDKInitialize();
        buildGoogleApiClient();
        setUpMapIfNeeded();
        createNavBarLayout();
        getUserInfo();
        setProfilePic();
        setUserName();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void facebookSDKInitialize() {

        FacebookSdk.sdkInitialize(getApplicationContext());

        CallbackManager callbackManager = CallbackManager.Factory.create();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setProfilePic();
        setUserName();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);


    }
    //Setup map
    private void setUpMapIfNeeded() {

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        }
    //
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);

        if(mMap != null){
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
            @Override
            public View getInfoWindow(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.custom_info_win,null);
                TextView tvShadeName = (TextView) v.findViewById(R.id.shadeName);
                TextView tvRating = (TextView) v.findViewById(R.id.ratingText);
                // set contents in marker info window todo find way to change image to user image
                tvShadeName.setText(marker.getTitle());
                tvRating.setText(marker.getSnippet());

                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }});
            }

        enableMyLocation();
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            final int ZOOM_LIMIT = 12;

            @Override
            public void onCameraChange(CameraPosition position) {
                if (position.zoom > ZOOM_LIMIT) {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


                }
            }
        });


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 11));;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(11)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            popMap(location.getLatitude(),location.getLongitude());

            final double updateLat = location.getLatitude();
            final double updateLng = location.getLongitude();

            //Set RadioGroup that allows user to set radius, change to listview in future patch todo
            final RadioGroup distanceRadius = (RadioGroup) findViewById(R.id.radio_group_list_selector);
            distanceRadius.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    int pos = distanceRadius.indexOfChild(findViewById(checkedId));

                    switch (pos) {
                        case 0:
                            Toast.makeText(getBaseContext(), "Showing all techs within 30 miles",
                                    Toast.LENGTH_SHORT).show();
                            mMap.clear();
                            popMap(updateLat, updateLng);
                            break;
                        case 2:
                            Toast.makeText(getBaseContext(), "Showing all techs within 40 miles",
                                    Toast.LENGTH_SHORT).show();
                            mMap.clear();
                            popMapFar(updateLat, updateLng);
                            break;
                        case 4:
                            Toast.makeText(getBaseContext(), "Showing all techs within 50 miles",
                                    Toast.LENGTH_SHORT).show();
                            mMap.clear();
                            popMapFurthest(updateLat, updateLng);
                            break;
                        default:
                            //The default selection is RadioButton 1
                            Toast.makeText(getBaseContext(), "Showing all techs within 30 miles, default",
                                    Toast.LENGTH_SHORT).show();
                            popMap(updateLat, updateLng);
                            break;
                    }

                }
            });


        }}
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            UiSettings settings = mMap.getUiSettings();
            settings.setZoomControlsEnabled(true);
            settings.setMapToolbarEnabled(false);
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Recentered the world around you", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        if(mMap.getMyLocation()!=null) {
            Location myLocation = mMap.getMyLocation();
            mLatitude = myLocation.getLatitude();  // changed mylocation to mlastlocation
            mLongitude = myLocation.getLongitude(); //see above comment
            mMap.clear();
            popMap(mLatitude, mLongitude);
            techCounterToast();}
            return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        setProfilePic();
        setUserName();
        //if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
        //    showMissingPermissionError();
       //     mPermissionDenied = false;
       // }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    //onSearch Method moves map to info inserted in search bar
    public void onSearch(View view) {
        EditText location_set = (EditText) findViewById(R.id.search_map);
        String location = location_set.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);


            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("You"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitude= mLastLocation.getLatitude();
            mLongitude= mLastLocation.getLongitude();


        } else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

    }

    //create navigation bar layout
    private void createNavBarLayout(){

        //Navigation Drawer Implementation
        //initialize array for navigation bar
        mTitle = mDrawerTitle = "ShadeTree";
        //Header of the listview, go to header.xml to customize
        View header = getLayoutInflater().inflate(R.layout.navigation_header, null);
        mUserOptions = getResources().getStringArray(R.array.user_options);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        //addHeaderView is to add custom content into first row
        mDrawerList.addHeaderView(header);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mUserOptions));
        //Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
    //on click listener for drawer object
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
       @Override
      public void onItemClick(AdapterView parent, View view, int position, long id) {
           switch(position){
               case 1:
                   String STUrl= "http://www.penguinsonabeach.com";
                   Intent i=new Intent(Intent.ACTION_VIEW);
                   i.setData(Uri.parse(STUrl));
                   startActivity(i);
                   break;
               case 2:
                   shareIt();
                   break;
               case 3:
                   sendCustSuppEmail();
                   break;
               case 4:
                   String ShadeUrl= "http://www.penguinsonabeach.com/termsofservice.html";
                   Intent t=new Intent(Intent.ACTION_VIEW);
                   t.setData(Uri.parse(ShadeUrl));
                   startActivity(t);
                   break;

           }
       }
   }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

       mDrawerLayout.closeDrawer(mDrawerList);
   }

    private void getUserInfo() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null){
            User_Id = extras.getString("UserId");
            First_Name = extras.getString("FirstName");
            Last_Name = extras.getString("LastName");
            sender = User_Id;
        }
    }

    private void setProfilePic(){
        com.penguinsonabeach.shadetree.extras.ProfilePictureView profilePicture = (com.penguinsonabeach.shadetree.extras.ProfilePictureView)findViewById(R.id.profilePictureImg);
        profilePicture.setProfileId(User_Id);

    }

    private void setUserName(){
        TextView userName = (TextView) findViewById(R.id.nameTextView);
        Typeface customFont = Typeface.createFromAsset(getAssets(),"fonts/Capture_it.ttf");
        userName.setTypeface(customFont);
        userName.setText(First_Name + " " + Last_Name);

    }

    private void setShadeMarkers(String name, double lat, double lng, final String email,String phoneInput, float reviews, float reviewCount) {
         Marker myMarker;
         String reviewStr = String.valueOf(reviews);
         final String messageText="Help! My car needs repairs...";

        if(reviewCount < 1){

            myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                    .title(name)
                    .snippet(phoneInput)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3star)));
        }
        else if(reviews< 2.5){

            myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                    .title(name)
                    .snippet(phoneInput)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1star)));
        }
        else if((reviews > 2.5) && (reviews < 3.5)) {

            myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                    .title(name)
                    .snippet(phoneInput)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2star)));
        }
        else if(reviews > 3.5){

            myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                    .title(name)
                    .snippet(phoneInput)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3star)));
        }


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker myMarker) {
                Bundle  bundle = new Bundle();
                bundle.putString("phone",myMarker.getSnippet());
                bundle.putString("FirstName",First_Name);
                bundle.putString("LastName",Last_Name);
                bundle.putString("fbID",sender);
                bundle.putString("email",email);
                Intent loadProfile = new Intent(Main.this,ShadeProfileUser.class);
                loadProfile.putExtras(bundle);
                startActivity(loadProfile);

            }
        });


    }

    private void sendCustSuppEmail(){

        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"customersupport@penguinsonabeach.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    protected void popMap(double lat, double lng){

        String latString,lngString;
        latString = String.valueOf(lat); //replaced mLatitude with lat
        lngString = String.valueOf(lng); //replaced mLongitude with lng

        String popMapUrl= Config.POPMAP_URL_FAR+latString+"&lng="+lngString;//latstring and lngstring changed to lat and lng

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,popMapUrl,null,
                    new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            counter = jsonArray.length();
                            for(int i = 0; i < jsonArray.length(); i++){

                                JSONObject shades = jsonArray.getJSONObject(i);
                                String name = shades.getString("name");
                                String phone = shades.getString("phone");
                                String lat = shades.getString("lat");
                                String lng = shades.getString("lng");
                                email = shades.getString("email");
                                String review = shades.getString("review");
                                String scoretotal = shades.getString("scoretotal");
                                float reviews = Float.parseFloat(review);
                                float scoretotals = Float.parseFloat(scoretotal);
                                float reviewScore = scoretotals/reviews;
                                Double lati = Double.parseDouble(lat);
                                Double lngi = Double.parseDouble(lng);
                                setShadeMarkers(name, lati, lngi, email, phone, reviewScore, reviews);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY","ERROR");
                    }
                });

        requestQueue.add(jsonObjectRequest);

}
    protected void popMapFar(double lat, double lng){

        String latString,lngString;
        latString = String.valueOf(lat); //replaced mLatitude with lat
        lngString = String.valueOf(lng); //replaced mLongitude with lng

        String popMapUrl= Config.POPMAP_URL+latString+"&lng="+lngString;//latstring and lngstring changed to lat and lng

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,popMapUrl,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            counter = jsonArray.length();
                            for(int i = 0; i < jsonArray.length(); i++){

                                JSONObject shades = jsonArray.getJSONObject(i);
                                String name = shades.getString("name");
                                String phone = shades.getString("phone");
                                String lat = shades.getString("lat");
                                String lng = shades.getString("lng");
                                email = shades.getString("email");
                                String review = shades.getString("review");
                                String scoretotal = shades.getString("scoretotal");
                                float reviews = Float.parseFloat(review);
                                float scoretotals = Float.parseFloat(scoretotal);
                                float reviewScore = scoretotals/reviews;
                                Double lati = Double.parseDouble(lat);
                                Double lngi = Double.parseDouble(lng);
                                setShadeMarkers(name, lati, lngi, email, phone, reviewScore, reviews);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY","ERROR");
                    }
                });

        requestQueue.add(jsonObjectRequest);

    }
    protected void popMapFurthest(double lat, double lng){

        String latString,lngString;
        latString = String.valueOf(lat); //replaced mLatitude with lat
        lngString = String.valueOf(lng); //replaced mLongitude with lng

        String popMapUrl= Config.POPMAP_URL_FURTHEST+latString+"&lng="+lngString;//latstring and lngstring changed to lat and lng

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,popMapUrl,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            counter = jsonArray.length();
                            for(int i = 0; i < jsonArray.length(); i++){

                                JSONObject shades = jsonArray.getJSONObject(i);
                                String name = shades.getString("name");
                                String phone = shades.getString("phone");
                                String lat = shades.getString("lat");
                                String lng = shades.getString("lng");
                                email = shades.getString("email");
                                String review = shades.getString("review");
                                String scoretotal = shades.getString("scoretotal");
                                float reviews = Float.parseFloat(review);
                                float scoretotals = Float.parseFloat(scoretotal);
                                float reviewScore = scoretotals/reviews;
                                Double lati = Double.parseDouble(lat);
                                Double lngi = Double.parseDouble(lng);
                                setShadeMarkers(name, lati, lngi, email, phone, reviewScore, reviews);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY","ERROR");
                    }
                });

        requestQueue.add(jsonObjectRequest);

    }

    protected void shareIt(){

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("ShadeTree: The app that connects you straight to the mechanics")
                    .setImageUrl(Uri.parse("https://www.facebook.com/photo.php?fbid=1113943965303165&set=pb.100000627161559.-2207520000.1458360988.&type=3&theater"))
                    .setContentDescription(
                            "ShadeTree App Link")
                    .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.penguinsonabeach.shadetree"))
                    .build();

            shareDialog.show(linkContent);  // Show facebook ShareDialog
        }
    }

    private void techCounterToast(){

        if(counter == 0){
            Toast.makeText(this, "No Techs Found In Your Area, Please Share Shadetree And Help Us Get More Techs!", Toast.LENGTH_LONG).show();}
        else
        {
            Toast.makeText(this, "We Found "+counter+" Techs In Your Area!", Toast.LENGTH_SHORT).show();}}


}