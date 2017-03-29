package com.jonat.flutterby;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.Frame;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.SphericalUtil;
import com.jonat.flutterby.auth.LoginActivity;
import com.jonat.flutterby.auth.ProfileActivity;
import com.jonat.flutterby.config.Config;
import com.jonat.flutterby.display_stories.PoiTag;
import com.jonat.flutterby.display_stories.SimilarStoriesActivity;
import com.jonat.flutterby.poi.POIGenre;
import com.jonat.flutterby.poi.PointOfInterest;
import com.jonat.flutterby.poi.Recommender;
import com.jonat.flutterby.poi.Story;
import com.jonat.flutterby.poi.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static com.jonat.flutterby.R.id.parent;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // Static variables
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = "******* MapsActivity";

    // Views
    private View mapView;
    // Widgets


    // Firebase references
    private Firebase mRef;
    private DatabaseReference ref;
    private GeoFire geoFire;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private FirebaseAuth.AuthStateListener authListener;

    // Ordinary Vars
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Double mLastLng;
    private Double mLastLat;

    // Map vars
    public List<AsyncTask<Void, Void, Void>> threadList;
    public HashMap<String, Marker> markers;

    //Objects
    private User user;
    private Recommender recommender;
    private Config config;
    // End and start times for measuring interest
    private long endTime;
    private long startTime;
    private PlaceAutocompleteFragment autocompleteFragment;

    // Widgets
    private FloatingActionButton profileButton;


    // ************** OVERRIDE METHODS *****************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);;
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        profileButton = (FloatingActionButton) findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this, ProfileActivity.class));
                onPause();
            }
        });

        Firebase.setAndroidContext(this);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng location = place.getLatLng();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 10);
                mMap.animateCamera(cameraUpdate);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);

        // threads
        threadList = new ArrayList<>();

        markers = new HashMap<>();

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() == null){
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
            finish();
        }
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        user = new User(auth.getCurrentUser().getEmail());
        this.config = new Config();
        recommender = new Recommender(user);

        // Firebase ref
        mRef = new Firebase(config.FIREBASE_URL);
        this.ref = FirebaseDatabase.getInstance().getReference(config.GEO_FIRE_REF);
        // setup GeoFire
        this.geoFire = new GeoFire(ref);
        // Basic ref
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.storage = FirebaseStorage.getInstance();

        // Actual Locations
        geoFire.setLocation("POI01/location", new GeoLocation(51.9012121,-8.4645262));
        geoFire.setLocation("POI02/location", new GeoLocation(51.8929344,-8.4856053));
        geoFire.setLocation("POI03/location", new GeoLocation(51.899388,-8.4749062));
        geoFire.setLocation("POI04/location", new GeoLocation(51.897418,-8.4664452));
        geoFire.setLocation("POI05/location", new GeoLocation(51.8895726,-8.4715865));
        geoFire.setLocation("POI06/location", new GeoLocation(51.8913665,-8.4927169));
        geoFire.setLocation("POI07/location", new GeoLocation(51.8973901,-8.4785351));
        geoFire.setLocation("POI08/location", new GeoLocation(51.8967649,-8.4785212));
        geoFire.setLocation("POI09/location", new GeoLocation(51.8975504,-8.4813676));
        geoFire.setLocation("POI10/location", new GeoLocation(51.9025669,-8.4772437));
        geoFire.setLocation("POI11/location", new GeoLocation(51.8978499,-8.473263));

        // Little Island values for testing
//        geoFire.setLocation("POI01/location", new GeoLocation(51.9102333,-8.3567585));
//        geoFire.setLocation("POI02/location", new GeoLocation(51.9102333,-8.3567585));
//        geoFire.setLocation("POI03/location", new GeoLocation(51.9102333,-8.3567585));
//        geoFire.setLocation("POI04/location", new GeoLocation(51.9102333,-8.3567585));
//        geoFire.setLocation("POI05/location", new GeoLocation(51.9102333,-8.3567585));
//        geoFire.setLocation("POI06/location", new GeoLocation(51.9102333,-8.3601215));
//        geoFire.setLocation("POI07/location", new GeoLocation(51.9130961,-8.350365));
//        geoFire.setLocation("POI08/location", new GeoLocation(51.9068942,-8.3513384));
//        geoFire.setLocation("POI09/location", new GeoLocation(51.9068942,-8.3513384));
//        geoFire.setLocation("POI10/location", new GeoLocation(51.893177, -8.501783));
//        geoFire.setLocation("POI11/location", new GeoLocation(51.893783, -8.499128));

    }

    @Override
    protected void onStop() {
        super.onStop();

        for(AsyncTask thread: threadList){
            thread.cancel(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        mMap = map;
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);

            }
        }
        else {

            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        // Init location variables
        mLastLocation = location;
        mLastLng = mLastLocation.getLongitude();
        mLastLat = mLastLocation.getLongitude();
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(latLng , 14.0f) );
        autocompleteFragment.setBoundsBias(toBounds(latLng, 500));
        callAsyncTask();
        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {}

    // ********* My Methods *********
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    protected void callAsyncTask(){
        if(markers!=null) {
            for (String poi : markers.keySet()) {
                markers.get(poi).remove();
            }
        }
        Timer timer = new Timer();
        final Handler handler = new Handler();
        TimerTask doAsyncTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            // Define Asynchronous Task to allow for data access in different threads
                            AsyncTask<Void, Void, Void> poiLoop = new BackgroundPointOfInterest();
                            AsyncTask<Void, Void, Void> interestLoop = new BackgroundFillUserInterests();
                            threadList.add(poiLoop);
                            threadList.add(interestLoop);
                            Log.d(TAG, "Executing Interest Loop");
                            interestLoop.execute();
                            Log.d(TAG, "Executing PointOfInterest Loop");
                            synchronized(poiLoop) {
                                if (!user.noStoredInterests()) {
                                    Log.d(TAG, "User has no interests. Waiting for 1 second.");
                                    poiLoop.wait(1000);
                                }
                            }
                            if(!user.interestsNotFilled() || user.getInterests().containsKey(null)){
                                Log.d(TAG, "Interests filled, or are null. Continuing...");
                            }
                            poiLoop.execute();

                            Log.d(TAG, "Background Loop executed");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsyncTask, 0, 60000);
    }

    private class BackgroundFillUserInterests extends  AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            mDatabase = FirebaseDatabase.getInstance().getReference("users/" + user.getUid());
            final Map<String, Float> loadedInterests = new HashMap<>();
            try{
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.hasChild("interests")){
                            Log.d(TAG, "Background Fill User Interests: User Does not have a child interests");
                            loadedInterests.put(null, null);
                            user.setInterests(loadedInterests);
                        }else {
                            Log.d(TAG, "Background Fill User Interests: isInterestEmpty is not Null");
                            for (DataSnapshot interestSnapshot : dataSnapshot.child("interests").getChildren()) {
                                String storyTitle = interestSnapshot.getKey();
                                double storyScore = (double) interestSnapshot.getValue();
                                float interestScore = (float) storyScore;
                                Log.d(TAG, "Background Interest Loop: Story title: " + storyTitle);
                                // Check and ensure the user's interests aren't loaded if they have no interests.
                                loadedInterests.put(storyTitle, interestScore);
                                Log.d(TAG, "Genre = " + storyTitle + "; Score = " + interestScore);
                                user.setInterests(loadedInterests);
                            }
                        }
                        System.out.println(user.getInterests());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "Could not access data: ", databaseError.toException());
                        Log.d(TAG, "Error Code: " + databaseError.getCode());
                    }
                });
            }catch(DatabaseException e){
                Log.w(TAG, "Database Exception " + e);
            }
            return null;
        }
    }

//     Create subclass for Asynchronous Tasks that handle pulling data from Firebase
private class BackgroundPointOfInterest extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long idIncrement = 0;
                for (final DataSnapshot poiSnapshot : dataSnapshot.getChildren()) {

                    final Map<String, String> genreMap = new HashMap<>();
                    final String mPoiSnapshot = poiSnapshot.getKey();
                    final HashMap<String, Story> mapOfStories = new HashMap<>();
                    final Vector<POIGenre> genreVector = new Vector<>();

                    // Get POI data from Firebase
                    final String mPoiTitle = (String) poiSnapshot.child("title").getValue();

                    for(final DataSnapshot mPoiStories: poiSnapshot.child("stories").getChildren()){
                        final String mPoiStoryTitle = (String) mPoiStories.child("storyTitle").getValue();
                        final String mPoiStoryText = (String) mPoiStories.child("story").getValue();
                        final HashMap<String, Float> similarities = new HashMap<>();
                        for (DataSnapshot similaritySnapshot : mPoiStories.child("similarities").getChildren()) {
                            String document = similaritySnapshot.getKey();
                            double storedSim = (double) similaritySnapshot.getValue();
                            float similarity = (float) storedSim;
                            similarities.put(document, similarity);
                        }

                        final Story story = new Story(mPoiStoryTitle, mPoiStoryText, similarities);
                        mapOfStories.put(story.getStoryTitle(), story);

                        for(DataSnapshot genreSnapshot: poiSnapshot.child("genre").getChildren()){
                            final String mPoiStoryGenreId = genreSnapshot.getKey();
                            final String mPoiStoryGenreType = (String) genreSnapshot.child("genre").getValue();
                            final POIGenre mPoiGenre = new POIGenre(idIncrement, mPoiStoryGenreType);
                            genreMap.put(mPoiStoryGenreType, mPoiStoryGenreId);
                            genreVector.add(mPoiGenre);
                        }
                    }
                    //Query location and place marker on said location
                    geoFire.getLocation(mPoiSnapshot+"/location", new LocationCallback() {
                        @Override
                        public void onLocationResult(String key, GeoLocation location) {
                            try{
                                if(location == null){
                                    Log.d(TAG, "Location is null");
                                }
                                // Create poi object with title, story and genre
                                final Location poiLocation = new Location("POI");
                                poiLocation.setLatitude(location.latitude);
                                poiLocation.setLongitude(location.longitude);
                                float distance = mLastLocation.distanceTo(poiLocation);
                                final PointOfInterest poi = new PointOfInterest(poiLocation, mPoiTitle, mapOfStories, genreVector);

                                if (recommender.recommendPoi(poi, distance)) {
                                    Log.d(TAG, "On Location Result: Point Of Interest " + poi.getPOITitle() + " is recommended");
                                    displayMarker(new MarkerOptions(), poi);
                                }

                            }catch(Exception e){
                                Log.d(TAG, "Exception raised: " + e);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.err.println("There was an error " +
                                    "getting the GeoFire location: " + databaseError);
                            Toast.makeText(MapsActivity.this, "Permission Denied. Please Login again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the Firebase data: " + databaseError);
                Toast.makeText(MapsActivity.this, "Permission Denied. Please Login again.", Toast.LENGTH_SHORT).show();
            }
        });
        return null;
    }
}

    public void displayMarker(MarkerOptions options, PointOfInterest poi) {
        if(markers!=null){
            Log.d(TAG, "Display Marker: Markers map is not null");
            if(markers.get(poi.getPOITitle())!=null){
                Log.d(TAG, "Display Marker: Marker is not null, removing");
                markers.get(poi.getPOITitle()).remove();
            }
        }
        Location poiLocation = poi.getLocation();
        LatLng poiLatLng = new LatLng(poiLocation.getLatitude(), poiLocation.getLongitude());
        HashMap<String, Story> mPoiStories = poi.getPOIStories();

        Story recommendedStory = recommender.recommendStory(mPoiStories);
        String storyText = recommendedStory.getStory();

        PoiTag poiTag = new PoiTag(poi, recommendedStory);
        Marker marker = mMap.addMarker(options.position(poiLatLng)
                .title(poi.getPOITitle())
                .snippet(storyText)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.visit)));
        marker.setTag(poiTag);
        marker.setVisible(true);

        markers.put(poi.getPOITitle(), marker);

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (!marker.isInfoWindowShown()) {
                        startTime = System.currentTimeMillis();
                        recommender.setStartTime(startTime);
                        Log.d(TAG, "On Marker Click: Marker clicked");
                        showPopup(marker);
                        Log.d(TAG, "On Marker Click: showPopup called successfully");
                    }
                    return true;
                }
            });
    }

    public void showPopup(final Marker marker) {
        Log.d(TAG, "Show Popup: Called");
        View popupView = getLayoutInflater().inflate(R.layout.fragment_story, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final PoiTag poiTag = (PoiTag) marker.getTag();
        final PointOfInterest poi = poiTag.getPoi();
        HashMap<String, Story> storyMap = poi.getPOIStories();
        final ArrayList<String> stories = new ArrayList<>();
        for(String story: storyMap.keySet()){
            stories.add(storyMap.get(story).getStory());
        }

        String titleText = marker.getTitle();
        String storyText = marker.getSnippet();

        StorageReference storageRef = storage.getReference("images");
        StorageReference imageRef = storageRef.child(titleText+".jpg");

        TextView title = (TextView) popupView.findViewById(R.id.poiTitleView);
        TextView story = (TextView) popupView.findViewById(R.id.poiStoryView);
        ImageView picture = (ImageView) popupView.findViewById(R.id.poiImageView);
        ImageButton infoButton = (ImageButton) popupView.findViewById(R.id.infoButton);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, SimilarStoriesActivity.class);
                intent.putExtra("title", poi.getPOITitle());
                intent.putExtra(poi.getPOITitle(), stories);
                startActivity(intent);
            }
        });

        title.setText(titleText);
        story.setText(storyText);
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(imageRef)
                .into(picture);

        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);

        Display display = MapsActivity.this.getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        popupView.measure(size.x, size.y);

        int width = popupView.getMeasuredWidth();
        int height = popupView.getMeasuredHeight();

        popupWindow.setBackgroundDrawable(new ColorDrawable());

        if (marker != null && popupWindow != null) {
            // marker is visible
            if (mMap.getProjection().getVisibleRegion().latLngBounds.contains(marker.getPosition())) {
                if (!popupWindow.isShowing()) {
                    popupWindow.showAtLocation(popupView, Gravity.NO_GRAVITY, 0, 0);
                }
                Point p = mMap.getProjection().toScreenLocation(marker.getPosition());
                popupWindow.update(p.x - width / 2, p.y - height + 100, -1, -1);
            } else { // marker outside screen
                popupWindow.dismiss();
            }
        }

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                endTime = System.currentTimeMillis();
                recommender.setEndTime(endTime);

                Story story = poiTag.getRecommendedStory();
                String storyText = story.getStory();
                String storyTitle = story.getStoryTitle();
                float measure = recommender.normaliseScore(storyText);

                if(recommender.shouldRecommendTimer(storyText)){
                    user.pushData(storyTitle, measure);
                }
                Log.d(TAG, "Refreshing");
                callAsyncTask();
                Log.d(TAG, "Pop Up Window closed for " + marker.getTitle());
            }
        });

    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}