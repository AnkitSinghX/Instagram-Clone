package com.ankit.instagram_clone.Home;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


import com.ankit.instagram_clone.Utils.MainfeedListAdapter;
import com.ankit.instagram_clone.Utils.ViewCommentsFragment;
import com.ankit.instagram_clone.models.Photo;
import com.ankit.instagram_clone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.ankit.instagram_clone.Login.LoginActivity;
import com.ankit.instagram_clone.R;
import com.ankit.instagram_clone.Utils.BottomNavigationViewHelper;
import com.ankit.instagram_clone.Utils.SectionsPagerAdapter;
import com.ankit.instagram_clone.Utils.UniversalImageLoader;

public class HomeActivity extends AppCompatActivity implements
        MainfeedListAdapter.OnLoadMoreItemsListener{

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");
        HomeFragment fragment = (HomeFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPager.getCurrentItem());
        if(fragment != null){
            fragment.displayMorePhotos();
        }
    }

    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;

    private Context mContext = HomeActivity.this;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //widgets
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting.");
        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayoutParent);

        setupFirebaseAuth();

        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();

    }



    public void onCommentThreadSelected(Photo photo, String callingActivity){
        Log.d(TAG, "onCommentThreadSelected: selected a coemment thread");

        ViewCommentsFragment fragment  = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }

    public void hideLayout(){
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }


    public void showLayout(){
        Log.d(TAG, "hideLayout: showing layout");
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mFrameLayout.getVisibility() == View.VISIBLE){
            showLayout();
        }
    }


    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    /**
     * Responsible for adding the 3 tabs: Camera, Home, Messages
     */
    private void setupViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CameraFragment()); //index 0
        adapter.addFragment(new HomeFragment()); //index 1
        adapter.addFragment(new MessagesFragment()); //index 2
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_insta);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_arrow);
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


     /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * checks to see if the @param 'user' is logged in
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if(user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }
    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mViewPager.setCurrentItem(HOME_FRAGMENT);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
