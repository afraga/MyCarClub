package com.id2p.mycarclub.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Event;
import com.id2p.mycarclub.model.User;
import com.id2p.mycarclub.utils.adapter.EventsAdapter;
import com.id2p.mycarclub.utils.ui.SlidingTabLayout;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends BaseDrawerActivity  {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private ListView eventsListView = null;
    private EventsAdapter adapter = null;
    private TextView headerText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) {
            ParseLoginBuilder builder = new ParseLoginBuilder(EventListActivity.this);
            startActivityForResult(builder.build(), 0);

            currentUser = new User();
        } else {
            setContentView(R.layout.activity_event_list);

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
                Toast.makeText(EventListActivity.this, "Error getting user information!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            eventsListView = (ListView) findViewById(R.id.eventsListView);

            View header = (View)getLayoutInflater().inflate(R.layout.event_item_header, null);
            headerText = (TextView) header.findViewById(R.id.txtHeader);

            eventsListView.addHeaderView(header);
            headerText.setText("My Events");

            try {
                adapter = new EventsAdapter(getApplicationContext(), (ArrayList)Event.getUserEvents(currentUser));
                eventsListView.setAdapter(adapter);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            super.onCreateDrawer(currentUser);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_user_events) {
            try {
                if (headerText != null) headerText.setText("My Events");
                adapter = new EventsAdapter(getApplicationContext(), (ArrayList)Event.getUserEvents(currentUser));
                eventsListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.action_chapter_events) {
            try {
                if (headerText != null) headerText.setText("My Chapter Events");
                adapter = new EventsAdapter(getApplicationContext(), (ArrayList)Event.getChapterEvents(currentUser));
                eventsListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.action_create_event) {
            Intent eventIntent = new Intent(getApplicationContext(), EventCreationActivity.class);
            startActivity(eventIntent);
        }

        return super.onOptionsItemSelected(item);
    }

}
