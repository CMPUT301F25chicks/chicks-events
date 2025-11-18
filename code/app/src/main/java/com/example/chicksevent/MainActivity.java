package com.example.chicksevent;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.chicksevent.databinding.ActivityMainBinding;

/**
 * The main entry point of the ChicksEvent application.
 * <p>
 * This activity sets up navigation between fragments using a {@link NavController}, configures
 * the app bar for navigation support, and binds the main layout using ViewBinding. It also
 * provides a floating action button with a placeholder Snackbar action.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Inflate and initialize the main activity layout.</li>
 *   <li>Configure the app bar and navigation graph integration.</li>
 *   <li>Handle menu inflation and navigation-up events.</li>
 * </ul>
 * </p>
 *
 * @author Jordan Kwan
 */
public class MainActivity extends AppCompatActivity {

    /** The configuration for the app bar navigation. */
    private AppBarConfiguration appBarConfiguration;

    /** ViewBinding instance for accessing layout components. */
    private ActivityMainBinding binding;

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the layout, sets up navigation, and configures the toolbar and FAB.
     * </p>
     *
     * @param savedInstanceState the previously saved state, or {@code null} for a fresh start.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Log.i("hehe", "reach");
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        Log.i("hehe", "reach");

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        Log.i("hehe", "reach3");

        Button eventButton = findViewById(R.id.btn_events);
        Button createEventButton = findViewById(R.id.btn_addEvent);
        Button profileButton = findViewById(R.id.btn_profile);
        Button notificationButton = findViewById(R.id.btn_notification);

        notificationButton.setOnClickListener(v -> {
            navController.navigate(R.id.NotificationFragment);
        });

        eventButton.setOnClickListener(v -> {
            navController.navigate(R.id.EventFragment);
        });


        createEventButton.setOnClickListener(v -> {
            navController.navigate(R.id.CreateEventFragment);

        });

        profileButton.setOnClickListener(v -> {
            navController.navigate(R.id.ProfileFragment);

        });

        Log.i("hehe4", "checking event");
    }

    /**
     * Inflates the main menu into the app bar.
     *
     * @param menu the menu to inflate.
     * @return {@code true} to display the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles selection of action bar menu items.
     *
     * @param item the selected menu item.
     * @return {@code true} if the event was handled, otherwise delegates to the superclass.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles navigation when the up button is pressed in the app bar.
     *
     * @return {@code true} if navigation succeeded; otherwise delegates to the superclass.
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}