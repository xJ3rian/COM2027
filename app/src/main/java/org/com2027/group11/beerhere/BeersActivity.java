package org.com2027.group11.beerhere;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.com2027.group11.beerhere.beer.Beer;
import org.com2027.group11.beerhere.beer.BeerListAdapter;

import org.com2027.group11.beerhere.user.User;
import org.com2027.group11.beerhere.user.UserDao;
import org.com2027.group11.beerhere.utilities.database.AppDatabase;
import org.w3c.dom.Text;

import org.com2027.group11.beerhere.utilities.FirebaseMutator;
import org.com2027.group11.beerhere.utilities.database.SynchronisationManager;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class BeersActivity extends AppCompatActivity implements FirebaseMutator {

    private RecyclerView rvBeers;
    private BeerListAdapter adapter;
    private SynchronisationManager firebaseManager = SynchronisationManager.getInstance();
    private Vector<Beer> beers = new Vector<Beer>();

    private DrawerLayout mDrawerLayout;
    private NavigationView headerLayout;
    private static final String TAG = "MAIN_ACTIVITY";


    private static final String LOG_TAG = "BEER-HERE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beers_page);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        headerLayout = findViewById(R.id.nav_view);

        //sets the toolbar as the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //adds the navigation drawer "hamburger" button
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);



        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    //set item as selected to persist highlight
                    menuItem.setChecked(true);
                    //close drawer when item is tapped
                    mDrawerLayout.closeDrawers();
                    return false;
                }
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent AddBeerIntent = new Intent(BeersActivity.this, AddBeerActivity.class);
                startActivity(AddBeerIntent);
            }
        });

        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Responds when the position of the drawer changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Responds when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Responds when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion stated changed
                    }
                }
        );

        displayBeers();
        userthread.start();


    }

    //opens the drawer when the navigation drawer "hamburger" button is tapped
    //handles click navigation events to start other fragments
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(LOG_TAG, "beers activity called onResume");
        this.firebaseManager.registerCallbackWithManager(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.nav_home:
                return super.onOptionsItemSelected(item);

            case R.id.nav_submissions:

                return true;

            case R.id.nav_favourites:

                return true;

            case R.id.nav_signout:

                return true;


        }

        return super.onOptionsItemSelected(item);
    }









    //method to obtain user info and display it on their profile on the header of the navigation drawer
    public User getUserInfo(){
        //Firebase authentication and user checking, and gets User ID
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        //Access database to find the User by his ID
        UserDao userDao = AppDatabase.getAppDatabase(this).userDao();
        User user = userDao.findByID(uid);

        //Inflate the header of the navigation drawer
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.nav_header, headerLayout, true );

        //check whether the user name and email are null and leave the default strings if they are null
        if (user.name == null){
        }
        else {
            //sets the name into the username textView
            TextView name = view.findViewById(R.id.userName);
            name.setText(user.name);
        }
        if (user.email == null) {
        }
        else {
            //sets the email into the email textView
            TextView email = view.findViewById(R.id.email);
            email.setText(user.email);
        }
        return user;
    }

    //Runs the getUserInfo() method on another thread to not cause conflict or freezing on the main thread
    Thread userthread = new Thread(new Runnable() {
        @Override
        public void run() {
            getUserInfo();
        }
    });


    private void displayBeers(){
        ViewGroup view = findViewById(android.R.id.content);
        getLayoutInflater().inflate(R.layout.content_beers_page, view, false);
        rvBeers = findViewById(R.id.rv_beers);
        adapter = new BeerListAdapter(this, this.beers);
        rvBeers.setAdapter(adapter);
        rvBeers.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void callbackGetObjectsFromFirebase(List<Object> objects) {
        Log.e(LOG_TAG, String.valueOf(objects.size()));
        for (Object object : objects) {
            Log.i(LOG_TAG, "Beer obtained! " + ((Beer) object).name);
            if (!(this.beers.contains(object))) {
                Log.e(LOG_TAG, String.valueOf(objects.size()));
                this.beers.add((Beer) object);
            }
        }

        for (Beer b : this.beers) {
            this.firebaseManager.getBitmapForBeerFromFirebase(b.name);
        }

        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void callbackObjectRemovedFromFirebase(String id) {
        // Find beer in list
        Beer foundBeer = null;
        for (Beer b : this.beers) {
            if (b.name.equals(id)) {
                foundBeer = b;
            }
        }

        if (foundBeer != null) {
            Log.e(LOG_TAG, "Removed: " + foundBeer.name);
            this.beers.remove(foundBeer);
        }

        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void callbackObjectChangedFromFirebase(Object object) {
        Log.i(LOG_TAG, "BeersActivity: object received from Firebase!");
        Beer beer = (Beer) object;
        Log.e(LOG_TAG, "Beer changed: " + beer.name);
        Beer originalBeer = null;

        // Find original beer in list
        for (Beer b : this.beers) {
            if (b.name.equals(beer.name)) {
                originalBeer = b;
            }
        }

        if (originalBeer != null) {
            this.beers.remove(originalBeer);
            this.beers.add(beer);
        } else {
            Log.e(LOG_TAG, "Changed beer not found in array adapter?");
        }

        for (Beer b : this.beers) {
            this.firebaseManager.getBitmapForBeerFromFirebase(b.name);
        }

        this.adapter.notifyDataSetChanged();

        Collections.reverse(this.beers);
    }

    @Override
    public void callbackGetBitmapForBeerFromFirebase(String beerName, Bitmap bitmap) {
        Log.i(LOG_TAG, "BeersActivity: got bitmap for " + beerName + " from Firebase!");

        for (Beer b : this.beers) {
            if (b.name.equals(beerName)) {
                b.setBeerImage(bitmap);
                Log.i(LOG_TAG, "beer bitmap: " + b.beerImageBmp.toString());
            }
        }

        this.adapter.notifyDataSetChanged();
    }

}
