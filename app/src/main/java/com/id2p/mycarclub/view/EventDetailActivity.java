package com.id2p.mycarclub.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Event;
import com.id2p.mycarclub.model.EventRegistration;
import com.id2p.mycarclub.model.User;
import com.id2p.mycarclub.utils.adapter.EventLocationAdapter;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EventDetailActivity extends AppCompatActivity {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private Event currentEvent = null;
    private EventRegistration currentRegistration = null;
    private TextView eventNameText = null;
    private TextView eventDescriptionText = null;
    private TextView eventChapterText = null;
    private TextView eventDateText = null;
    private TextView eventTimeText = null;
    private TextView eventNumRegistered = null;
    private ListView eventRouteList = null;
    private Menu eventMenu = null;
    private Calendar calendar = null;
    private EventLocationAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        parseUser = ParseUser.getCurrentUser();
        try {
            currentUser = User.getUser(parseUser);
        } catch (ParseException e) {
            Toast.makeText(this, "Unable to get user login information. Please try later! ", Toast.LENGTH_LONG).show();
            finish();
        }

        // init UI elements
        eventNameText = (TextView) findViewById(R.id.eventName);
        eventDescriptionText = (TextView) findViewById(R.id.eventDescription);
        eventChapterText = (TextView) findViewById(R.id.eventChapter);
        eventDateText = (TextView) findViewById(R.id.eventDate);
        eventTimeText = (TextView) findViewById(R.id.eventTime);
        eventNumRegistered = (TextView) findViewById(R.id.eventNumRegistered);
        eventRouteList = (ListView) findViewById(R.id.routeList);

        // check if we are editing an existing Event or creating a new one
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.get("eventId") != null) {
            final ProgressDialog progress = new ProgressDialog(EventDetailActivity.this);
            progress.setMessage("Loading...");
            progress.show();

            String eventId = bundle.getString("eventId");
            Event.getEventByIdInBackGround(eventId, new GetCallback<Event>() {
                @Override
                public void done(Event event, ParseException e) {
                    currentEvent = event;
                    loadEventData();
                    loadRegistrationData();
                    progress.dismiss();
                }
            });

        } else {
            Toast.makeText(this, "Unable to get event information. Please try later! ", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private void loadEventData() {
        eventNameText.setText(currentEvent.getName());
        eventDescriptionText.setText(currentEvent.getDescription());
        eventChapterText.setText(currentEvent.getChapter());

        calendar = Calendar.getInstance();
        calendar.setTime(currentEvent.getDate());

        Date now = new Date();
        int diffInMins = (int)( (calendar.getTime().getTime() - now.getTime()) / (1000 * 60) );

        // TODO: only show checkin if within 2 hours from start
        if (diffInMins <= 120) {
            // users can checking to event at least 2 hours before event
            eventMenu.getItem(2).setVisible(true);
        } else {
            // hide checkin button until it is time for event
            eventMenu.getItem(2).setVisible(true);
        }

        updateDateLabel();
        updateTimeLabel();

        getNumberOfRegisteredUsersForEvent();

        adapter = new EventLocationAdapter(getApplicationContext(), (ArrayList)currentEvent.getRoute());
        eventRouteList.setAdapter(adapter);

        if (currentEvent.getOrganizer() == currentUser) {
            eventMenu.getItem(3).setEnabled(true);
            eventMenu.getItem(3).setVisible(true);
        } else {
            eventMenu.getItem(3).setEnabled(false);
            eventMenu.getItem(3).setVisible(false);
        }
    }

    private void loadRegistrationData() {
        try {
            currentRegistration = EventRegistration.getRegistration(currentEvent, currentUser);

            // user already registered for this event
            eventMenu.getItem(0).setEnabled(false);
            eventMenu.getItem(1).setEnabled(true);

        } catch (ParseException e) {
            currentRegistration = new EventRegistration();

            // user not yet registered for this event
            eventMenu.getItem(0).setEnabled(true);
            eventMenu.getItem(1).setEnabled(false);
        }
    }
    
    private void updateDateLabel() {
        String myFormat = "MMMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        eventDateText.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeLabel() {
        String myFormat = "kk:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        eventTimeText.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_detail, menu);
        eventMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_register) {
            registerForEvent();
        } else if (id == R.id.action_unregister) {
            unregisterFromEvent();
        } else if (id == R.id.action_edit) {
            Intent eventEditIntent = new Intent(EventDetailActivity.this, EventCreationActivity.class);
            eventEditIntent.putExtra("eventId", currentEvent.getObjectId());
            startActivity(eventEditIntent);
        } else if (id == R.id.action_map_view) {
            Intent mapViewIntent = new Intent(EventDetailActivity.this, MapViewActivity.class);
            mapViewIntent.putExtra("eventId", currentEvent.getObjectId());
            startActivity(mapViewIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void getNumberOfRegisteredUsersForEvent() {
        final ProgressDialog progress = new ProgressDialog(EventDetailActivity.this);
        progress.setMessage("Updating User Count...");
        progress.show();

        EventRegistration.getNumberOfRegistrationsInBackGround(currentEvent, new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    eventNumRegistered.setText(count + " Users Registered");
                }
                progress.dismiss();
            }
        });
    }

    public void registerForEvent() {
        final ProgressDialog progress = new ProgressDialog(EventDetailActivity.this);
        progress.setMessage("Registering...");
        progress.show();

        try {
            if (EventRegistration.isUserRegistered(currentEvent, currentUser) == false) {
                currentRegistration.setEvent(currentEvent);
                currentRegistration.setRegisteredUser(currentUser);
                currentRegistration.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            toggleRegistrationButtons();
                            getNumberOfRegisteredUsersForEvent();
                        } else {
                            Toast.makeText(getApplicationContext(), "Unable to register. Please try again later! ", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Unable to register. Please try again later! ", Toast.LENGTH_LONG).show();
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Unable to register. Please try again later! ", Toast.LENGTH_LONG).show();
        } finally {
            progress.dismiss();
        }

    }

    public void unregisterFromEvent() {
        final ProgressDialog progress = new ProgressDialog(EventDetailActivity.this);
        progress.setMessage("Unregistering...");
        progress.show();

        currentRegistration.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    toggleRegistrationButtons();
                    getNumberOfRegisteredUsersForEvent();
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to unregister. Please try again later! ", Toast.LENGTH_LONG).show();
                }
                progress.dismiss();
            }
        });
    }

    private void toggleRegistrationButtons() {
        if (eventMenu.getItem(0).isEnabled()) {
            eventMenu.getItem(0).setEnabled(false);
            eventMenu.getItem(1).setEnabled(true);
        } else {
            eventMenu.getItem(0).setEnabled(true);
            eventMenu.getItem(1).setEnabled(false);
        }
    }


}
