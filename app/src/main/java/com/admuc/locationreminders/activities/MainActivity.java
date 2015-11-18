package com.admuc.locationreminders.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.admuc.locationreminders.R;
import com.admuc.locationreminders.adapters.ReminderAdapter;
import com.admuc.locationreminders.models.AutomaticReminder;
import com.admuc.locationreminders.models.ManualReminder;
import com.admuc.locationreminders.models.Reminder;
import com.admuc.locationreminders.utils.ReminderComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ReminderAdapter adapter;

    private boolean active = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.closed_drawer);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationClickListener());

        AutomaticReminder ar = new AutomaticReminder("Buy milk", "comment here", "shop");
        ar.save();
        AutomaticReminder ar2 = new AutomaticReminder("Buy cookies", "add notes", "shop");
        ar2.save();

        final List<Reminder> reminders = new ArrayList<>();

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new ReminderAdapter(reminders, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        loadReminders();

        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));

        final Intent intent = new Intent(this, ManageActivity.class);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }


    private List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();

        Iterator<AutomaticReminder> automaticRemindersIterator = AutomaticReminder.findAll(AutomaticReminder.class);
        while (automaticRemindersIterator.hasNext()) {
            reminders.add(automaticRemindersIterator.next());
        }

        Iterator<ManualReminder> manualReminderIterator = ManualReminder.findAll(ManualReminder.class);
        while (manualReminderIterator.hasNext()) {
            Log.d("Reminder", "item");
            reminders.add(manualReminderIterator.next());
        }

        Collections.sort(reminders, new ReminderComparator());

        return reminders;
    }

    private List<Reminder> getActiveReminders() {
        List<Reminder> reminders = new ArrayList<>();
        Iterator<AutomaticReminder> automaticRemindersIterator = AutomaticReminder.findAsIterator(AutomaticReminder.class, "COMPLETED = ?", "0");
        while (automaticRemindersIterator.hasNext()) {
            Log.d("Reminder", "something");
            reminders.add(automaticRemindersIterator.next());
        }

        Iterator<ManualReminder> manualReminderIterator = ManualReminder.findAsIterator(ManualReminder.class, "COMPLETED = ?", "0");
        while (manualReminderIterator.hasNext()) {
            reminders.add(manualReminderIterator.next());
        }

        Collections.sort(reminders, new ReminderComparator());

        return reminders;
    }

    private List<Reminder> getCompletedReminders() {
        List<Reminder> reminders = new ArrayList<>();
        Iterator<AutomaticReminder> automaticRemindersIterator = AutomaticReminder.findAsIterator(AutomaticReminder.class, "COMPLETED = ?", "1");
        while (automaticRemindersIterator.hasNext()) {
            reminders.add(automaticRemindersIterator.next());
        }

        Iterator<ManualReminder> manualReminderIterator = ManualReminder.findAsIterator(ManualReminder.class, "COMPLETED = ?", "1");
        while (manualReminderIterator.hasNext()) {
            reminders.add(manualReminderIterator.next());
        }

        Collections.sort(reminders, new ReminderComparator());

        return reminders;
    }

    private void loadReminders() {
        List<Reminder> reminders;

        if (active) {
            reminders = getActiveReminders();
        } else {
            reminders = getCompletedReminders();
        }

        adapter.setReminders(reminders);
        adapter.notifyDataSetChanged();
    }


    public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public SimpleDividerItemDecoration(Context context) {
            mDivider = context.getResources().getDrawable(R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    private class NavigationClickListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.nav_active) {
                active = true;
            } else if (id == R.id.nav_completed) {
                active = false;
            }

            loadReminders();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
    }

    private enum ReminderState {
        ACTIVE, COMPLETED
    }
}
