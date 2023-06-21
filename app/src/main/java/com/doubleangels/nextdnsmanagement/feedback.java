package com.doubleangels.nextdnsmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.UserFeedback;
import io.sentry.protocol.SentryId;

public class feedback extends AppCompatActivity {

    public ExceptionHandler exceptionHandler = new ExceptionHandler();
    public Boolean overrideDarkMode;
    public Boolean manualDarkMode;
    public Boolean isDarkModeOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ITransaction feedback_create_transaction = Sentry.startTransaction("feedback_onCreate()", "feedback");
        setContentView(R.layout.activity_feedback);

        try {
            // Set up our window, status bar, and toolbar.
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_background_color));
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbar_background_color));

            // Set up the visual indicator.
            VisualIndicator visualIndicator = new VisualIndicator();
            visualIndicator.initiateVisualIndicator(this, getApplicationContext());

            // Let us touch the visual indicator to open an explanation.
            ImageView statusIcon = findViewById(R.id.connectionStatus);
            statusIcon.setOnClickListener(v -> {
                Intent helpIntent = new Intent(v.getContext(), help.class);
                startActivity(helpIntent);
            });

            // Get our exception from whatever activity sent us here.
            Bundle bundle = getIntent().getExtras();
            Throwable exception = (Throwable) bundle.getSerializable("e");

            // Get feedback comments and submit them along with the error when submit button is pressed.
            Button feedbackSubmitButton = findViewById(R.id.feedbackButton);
            feedbackSubmitButton.setOnClickListener(v -> {
                EditText feedbackTextView = findViewById(R.id.feedbackTextView);
                String feedbackString = feedbackTextView.getText().toString();
                SentryId sentryID = Sentry.captureException(exception);
                UserFeedback userFeedback = new UserFeedback(sentryID);
                userFeedback.setComments(feedbackString);
                Sentry.captureUserFeedback(userFeedback);
                finish();
            });
        } catch (Exception e) {
            exceptionHandler.captureException(e);
        } finally {
            feedback_create_transaction.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        overrideDarkMode = sharedPreferences.getBoolean(settings.OVERRIDE_DARK_MODE, false);
        manualDarkMode = sharedPreferences.getBoolean(settings.MANUAL_DARK_MODE, false);
        if (overrideDarkMode) {
            isDarkModeOn = manualDarkMode;
        } else {
            isDarkModeOn = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)  == Configuration.UI_MODE_NIGHT_YES;
        }
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}