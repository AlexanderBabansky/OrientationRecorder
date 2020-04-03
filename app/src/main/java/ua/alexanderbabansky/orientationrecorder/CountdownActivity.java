package ua.alexanderbabansky.orientationrecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class CountdownActivity extends AppCompatActivity {

    private Timer timer;
    float time_left = 3;
    TextView tv_Timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);
        tv_Timer = (TextView) findViewById(R.id.tv_Timer);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
