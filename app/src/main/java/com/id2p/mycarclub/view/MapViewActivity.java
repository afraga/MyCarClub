package com.id2p.mycarclub.view;

import android.app.ProgressDialog;
import android.content.IntentSender;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Event;
import com.id2p.mycarclub.model.InProgressRoute;
import com.id2p.mycarclub.model.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

public class MapViewActivity extends FragmentActivity implements
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private User currentUser = null;
    private ParseUser parseUser = null;
    private Event currentEvent = null;
    private GoogleMap googleMap = null;
    private GoogleApiClient googleApiClient = null;
    private LocationRequest locationRequest = null;
    private Location myCurrentLocation = null;
    private float currentBearing = 0f;
    private LatLng currentHeadLocation = null;
    private Marker headLocationMarker = null;
    private Marker tailLocationMarker = null;
    private InProgressRoute route = null;
    private boolean isHead = false;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int LOCATION_UPDATE_INTERVAL = 2 * 1000; // 5 seconds in millis
    private final static int LOCATION_UPDATE_FASTEST_INTERVAL = 1 * 1000; // 3 seconds in millis


    public static final String TAG = MapViewActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setUpMapIfNeeded();

        parseUser = ParseUser.getCurrentUser();
        try {
            currentUser = User.getUser(parseUser);
        } catch (ParseException e) {
            Toast.makeText(this, "Unable to get user login information. Please try later! ", Toast.LENGTH_LONG).show();
            finish();
        }

        // get the Event
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.get("eventId") != null) {
            final ProgressDialog progress = new ProgressDialog(MapViewActivity.this);
            progress.setMessage("Loading event data...");
            progress.show();

            String eventId = bundle.getString("eventId");
            Event.getEventByIdInBackGround(eventId, new GetCallback<Event>() {
                @Override
                public void done(Event event, ParseException e) {
                    currentEvent = event;

                    // TODO: DEBUG ONLY, REMOVE AFTER
                    if (currentEvent.getOrganizer() == currentUser) {
                        Toast.makeText(MapViewActivity.this, "You are the HEAD", Toast.LENGTH_LONG).show();
                        isHead = true;
                        googleMap.setMyLocationEnabled(false);
                    }
                    // TODO: DEBUG ONLY, REMOVE AFTER

                    // check if we already have a route in progress for this event and use it if so
                    InProgressRoute.getInProgressRouteForEventInBackground(currentEvent, new GetCallback<InProgressRoute>() {
                        @Override
                        public void done(InProgressRoute inProgressRoute, ParseException e) {
                            if (e == null) {
                                route = inProgressRoute;
                            } else {
                                route = new InProgressRoute();
                            }
                            route.setEvent(currentEvent);
                            progress.dismiss();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, "Unable to get event information. Please try later! ", Toast.LENGTH_LONG).show();
            finish();
        }

        // Google Maps API init
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (servicesAvailable()) {
//            Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//            if (lastKnownLocation == null) {
//                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
//            } else {
//                handleNewLocation(lastKnownLocation);
//            }
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        myCurrentLocation = location;

        // if head or tail, push new location to cloud so other users can se where you are
        if (isHead) {
            uploadHeadLocation(location);
        }

        updateUI();
    }

    private void uploadHeadLocation(Location location) {
        if (route != null) {
            route.setHeadLocation(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            route.saveInBackground();
        }
    }

    private void updateUI() {
        updateMap(new LatLng(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude()));
    }

    private void updateMap(LatLng position) {
        Toast.makeText(this, "Location updated to: " + position.toString(), Toast.LENGTH_LONG).show();

        if (isHead) {
            updateHeadLocationOnMap(position);
        } else {
            downloadAndUpdateHeadLocationOnMap();
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(17)
                .bearing(currentBearing)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private boolean servicesAvailable() {
        int res = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (res == ConnectionResult.SUCCESS) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(res, this, 0).show();
            return false;
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                if (isHead) {
                    // head will show up as a car on the Map
                    // users will show up as a blue dot
                    googleMap.setMyLocationEnabled(false);
                }
            }
        }
    }

    public void updateHeadLocationOnMap(LatLng position) {
        if (headLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .title("Head")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.cartop64))
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .position(position);
            headLocationMarker = googleMap.addMarker(markerOptions);
        } else {
            headLocationMarker.setPosition(position);
        }
    }

    public void downloadAndUpdateHeadLocationOnMap() {
        InProgressRoute.getInProgressRouteForEventInBackground(currentEvent, new GetCallback<InProgressRoute>() {
            @Override
            public void done(InProgressRoute route, ParseException e) {
                if (e == null) {
                    // process head location update
                    ParseGeoPoint headLocation = route.getHeadLocation();
                    currentHeadLocation = new LatLng(headLocation.getLatitude(), headLocation.getLongitude());
                    updateHeadLocationOnMap(currentHeadLocation);
                } else {
                    // DEBUG
                    Toast.makeText(MapViewActivity.this, "Error fetching HEAD location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map_view, menu);
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
    public void onConnectionSuspended(int i) {
        Log.d("MapViewActivity", "onConnectionSuspended!!!!!!!!!!!!!!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

}
