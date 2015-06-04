package com.id2p.mycarclub.view;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Event;
import com.id2p.mycarclub.model.User;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

import java.util.List;

public class EventCreationActivity extends BaseDrawerActivity {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private Event currentEvent = null;

    private EditText eventNameText = null;
    private EditText eventDescriptionText = null;
    private Spinner eventChapterSpinner = null;
    private DatePicker eventDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            eventDate = (DatePicker) findViewById(R.id.eventDate);

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
