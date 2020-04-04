package ua.alexanderbabansky.orientationrecorder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void GetMoreInfo(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AlexanderBabansky/OrientationRecorder"));
        startActivity(intent);
    }

}
