package com.id2p.mycarclub.view;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Event;
import com.id2p.mycarclub.model.User;
import com.id2p.mycarclub.utils.adapter.EventsAdapter;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import java.util.ArrayList;

public class EventListActivity extends AppCompatActivity {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private ListView eventsListView = null;
    private EventsAdapter adapter = null;
    private static int CONTEXT_EDIT_INDEX = 0;
    private static int CONTEXT_DELETE_INDEX = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        parseUser = ParseUser.getCurrentUser();
        try {
            currentUser = User.getUser(parseUser);
        } catch (ParseException e) {
            Toast.makeText(this, "Unable to get user login information. Please try later! ", Toast.LENGTH_LONG).show();
            finish();
        }

        eventsListView = (ListView) findViewById(R.id.eventsListView);
        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Event clickedEvent = adapter.getItem(i);
                Intent eventDetailIntent = new Intent(getApplicationContext(), EventDetailActivity.class);
                eventDetailIntent.putExtra("eventId", clickedEvent.getObjectId());
                startActivity(eventDetailIntent);
            }
        });
        eventsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
        registerForContextMenu(eventsListView);
        getSupportActionBar().setTitle("My Events");

        try {
            adapter = new EventsAdapter(getApplicationContext(), (ArrayList)Event.getUserEvents(currentUser));
            eventsListView.setAdapter(adapter);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.eventsListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.add(Menu.NONE, CONTEXT_EDIT_INDEX, 0, "Edit");
            menu.add(Menu.NONE, CONTEXT_DELETE_INDEX, 0, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Event selectedEvent = adapter.getItem(info.position-1);
        int menuItemIndex = item.getItemId();

        if (selectedEvent.getOrganizer() != currentUser) {
            Toast.makeText(this, "Cannot modify events not created by you.", Toast.LENGTH_LONG).show();
            return true;
        }

        if (menuItemIndex == CONTEXT_EDIT_INDEX) {
            Intent eventIntent = new Intent(getApplicationContext(), EventCreationActivity.class);
            eventIntent.putExtra("eventId", selectedEvent.getObjectId());
            startActivity(eventIntent);
        } else if (menuItemIndex == CONTEXT_DELETE_INDEX) {
            AlertDialog diaBox = AskDeleteEvent(selectedEvent);
            diaBox.show();
        }
        return true;
    }

    private AlertDialog AskDeleteEvent(final Event event)
    {
        AlertDialog deleteEventDialogBox = new AlertDialog.Builder(this)
            //set message, title, and icon
//            .setTitle("Delete")
            .setMessage("Delete " + event.getName() + "?")
//            .setIcon(R.drawable.com_facebook_close)
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    final ProgressDialog progress = new ProgressDialog(EventListActivity.this);
                    progress.setMessage("Deleting...");
                    progress.show();

                    Event.deleteEventInBackGround(event, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            adapter.remove(event);
                            adapter.notifyDataSetChanged();

                            progress.dismiss();
                        }
                    });
                    dialog.dismiss();
                }
            })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
            })
                .create();
        return deleteEventDialogBox;
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
                getSupportActionBar().setTitle("My Events");
                adapter = new EventsAdapter(getApplicationContext(), (ArrayList)Event.getUserEvents(currentUser));
                eventsListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.action_chapter_events) {
            try {
                getSupportActionBar().setTitle("My Chapter Events");
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
