package com.id2p.mycarclub.utils.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.id2p.mycarclub.view.EventsTabFragment;

/**
 * Created by anfraga on 2015-07-03.
 */

public class EventsPagerAdapter extends FragmentPagerAdapter {
    private static final int EVENTS_COMING_UP = 0;
    private static final int EVENTS_USER_REGISTERED = 1;
    private static final int EVENTS_CREATED_BY_USER = 2;
    private static final int EVENTS_SAME_USER_CHAPTER = 3;
    private static final int EVENTS_USER_PARTICIPATED = 4;
    private final String[] TITLES = { "Coming Up", "Created", "Registered", "Same Chapter", "Participated" };

    public EventsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case EVENTS_COMING_UP:
                return EventsTabFragment.newInstance(EVENTS_COMING_UP);
            case EVENTS_CREATED_BY_USER:
                return EventsTabFragment.newInstance(EVENTS_CREATED_BY_USER);
            case EVENTS_USER_REGISTERED:
                return EventsTabFragment.newInstance(EVENTS_USER_REGISTERED);
            case EVENTS_SAME_USER_CHAPTER:
                return EventsTabFragment.newInstance(EVENTS_SAME_USER_CHAPTER);
            case EVENTS_USER_PARTICIPATED:
                return EventsTabFragment.newInstance(EVENTS_USER_PARTICIPATED);
        }
        return null;
    }

}
