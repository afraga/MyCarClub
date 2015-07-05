package com.id2p.mycarclub.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.User;
import com.id2p.mycarclub.utils.adapter.EventsCardAdapter;
import com.parse.ParseException;
import com.parse.ParseUser;

public class EventsTabFragment extends Fragment {

    private static final int EVENTS_COMING_UP = 0;
    private static final int EVENTS_USER_REGISTERED = 1;
    private static final int EVENTS_CREATED_BY_USER = 2;
    private static final int EVENTS_SAME_USER_CHAPTER = 3;
    private static final int EVENTS_USER_PARTICIPATED = 4;
    private static final String EVENT_LIST_TYPE = "eventListType";
    private int mEventListType = 0;
    private ParseUser parseUser = null;
    private User currentUser = null;
    private RecyclerView mRecyclerView = null;
    private RecyclerView.LayoutManager mLayoutManager = null;
    private RecyclerView.Adapter mAdapter = null;

    public static EventsTabFragment newInstance(int eventListType) {
        EventsTabFragment fragment = new EventsTabFragment();
        Bundle args = new Bundle();
        args.putInt(EVENT_LIST_TYPE, eventListType);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventsTabFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mEventListType = getArguments().getInt(EVENT_LIST_TYPE);
        }

        parseUser = ParseUser.getCurrentUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events_tab, container, false);
        try {
            currentUser = User.getUser(parseUser);

            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);

            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            mAdapter = new EventsCardAdapter(mEventListType, currentUser);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        } catch (ParseException e) {
            Toast.makeText(getActivity(), "Unable to get user login information. Please try later! ", Toast.LENGTH_LONG).show();
        }

        return rootView;
    }

}
