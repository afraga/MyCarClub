package com.id2p.mycarclub.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Event;
import com.id2p.mycarclub.model.Route;
import com.id2p.mycarclub.model.User;
import com.id2p.mycarclub.utils.PlaceArrayAdapter;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventCreationActivity extends BaseDrawerActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private Event currentEvent = null;
    private EditText eventNameText = null;
    private EditText eventDescriptionText = null;
    private Spinner eventChapterSpinner = null;
    private EditText eventDate = null;
    private EditText eventTime = null;
    private AutoCompleteTextView addressText = null;
    private ImageButton addWaypointButton = null;
    private ImageButton removeWaypointButton = null;
    private ListView routeList = null;
    private Calendar calendar = Calendar.getInstance();
    private Route lastClickedLocation = new Route();
    private ArrayList<Route> parseGeoPointList = new ArrayList<Route>();
    private ArrayAdapter<Route> parseGeoPointListAdapter = null;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient googleApiClient = null;
    private PlaceArrayAdapter placeArrayAdapter = null;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(28.70, -127.50), new LatLng(48.85, -55.90));
    private static final String LOG_TAG = "EventCreationActivity";


    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        }
    };

    private void updateDateLabel() {
        String myFormat = "MMMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        eventDate.setText(sdf.format(calendar.getTime()));
    }

    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            updateTimeLabel();
        }
    };

    private void updateTimeLabel() {
        String myFormat = "kk:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        eventTime.setText(sdf.format(calendar.getTime()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleApiClient = new GoogleApiClient.Builder(EventCreationActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.eventDate:
                new DatePickerDialog(EventCreationActivity.this, dateSetListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.eventTime:
                new TimePickerDialog(EventCreationActivity.this, timeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                break;
            case R.id.addWaypoint:
                System.out.println("ADD WAYPOINT CLICKED for: " + lastClickedLocation.getAddress());
                if (!parseGeoPointList.contains(lastClickedLocation)) {
                    parseGeoPointList.add(lastClickedLocation);
                    parseGeoPointListAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.removeWaypoint:
                System.out.println("REMOVE WAYPOINT CLICKED");
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        parseUser = ParseUser.getCurrentUser();

        if (parseUser == null) {
            ParseLoginBuilder builder = new ParseLoginBuilder(EventCreationActivity.this);
            startActivityForResult(builder.build(), 0);
        } else {
            setContentView(R.layout.activity_event_creation);
            super.onCreateDrawer();

            // init UI elements
            eventNameText = (EditText) findViewById(R.id.eventName);
            eventDescriptionText = (EditText) findViewById(R.id.eventDescription);
            eventChapterSpinner = (Spinner) findViewById(R.id.eventChapter);
            eventDate = (EditText) findViewById(R.id.eventDate);
            eventTime = (EditText) findViewById(R.id.eventTime);
            addressText = (AutoCompleteTextView) findViewById(R.id.addressText);
            addWaypointButton = (ImageButton) findViewById(R.id.addWaypoint);
            removeWaypointButton = (ImageButton) findViewById(R.id.removeWaypoint);
            routeList = (ListView) findViewById(R.id.routeList);

            // set data adapters
            placeArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1, BOUNDS_MOUNTAIN_VIEW, null);
            addressText.setAdapter(placeArrayAdapter);

            parseGeoPointListAdapter = new ArrayAdapter<Route>(getApplicationContext(), android.R.layout.simple_list_item_1, parseGeoPointList);
            routeList.setAdapter(parseGeoPointListAdapter);

            // set click listeners
            eventDate.setOnClickListener(this);
            eventTime.setOnClickListener(this);
            addressText.setOnItemClickListener(this);
            addWaypointButton.setOnClickListener(this);
            removeWaypointButton.setOnClickListener(this);

            // load our logged in user
            ParseQuery<User> query = new ParseQuery<User>("User");
            query.whereEqualTo("parseUser", parseUser);
            try {
                List<User> userList = query.find();
                if (userList != null && userList.size() > 0)
                    currentUser = userList.get(0);
                else
                    currentUser = new User();
            } catch (ParseException e) {
                Toast.makeText(EventCreationActivity.this, "Error getting user information!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            // check if we are editing an existing Event or creating a new one
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.get("event") != null) {
                currentEvent = (Event) bundle.get("event");
            } else {
                currentEvent = new Event();
            }

            // load list of chapters
            loadChapters();

        }
    }

    private void loadChapters() {
        String[] chapterArray = { "Ottawa", "Montreal", "Toronto" };
        ArrayAdapter mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, chapterArray);
        eventChapterSpinner.setAdapter(mAdapter);
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        final PlaceArrayAdapter.PlaceAutocomplete item = placeArrayAdapter.getItem(position);
        final String placeId = String.valueOf(item.placeId);
        Log.i(LOG_TAG, "Selected: " + item.description);
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                .getPlaceById(googleApiClient, placeId);
        placeResult.setResultCallback(updatePlaceDetailsCallback);
        Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);

    }

    private ResultCallback<PlaceBuffer> updatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);

            Log.i(LOG_TAG, place.getAddress() + " - Latitude: " + place.getLatLng().latitude + " Longitude: " + place.getLatLng().longitude);
            System.out.println(place.getAddress() + " - Latitude: " + place.getLatLng().latitude + " Longitude: " + place.getLatLng().longitude);

            lastClickedLocation.setAddress(place.getAddress().toString());
            lastClickedLocation.setGeoPoint(new ParseGeoPoint(place.getLatLng().latitude, place.getLatLng().longitude));

            addressText.setText(Html.fromHtml(place.getAddress().toString()));
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        placeArrayAdapter.setGoogleApiClient(googleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        placeArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_creation, menu);
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

}
