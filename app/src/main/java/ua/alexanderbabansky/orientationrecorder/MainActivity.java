package ua.alexanderbabansky.orientationrecorder;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int CREATE_FILE = 1;

    static {
        System.loadLibrary("native-lib");
    }

    TextView tv_FilePath, tv_Timer;
    Button btn_SelectDirectory, btn_StartRecording, btn_StopRecording;
    RecordService service = null;
    Uri file_uri = null;
    Timer timer;
    boolean directory_selected = false;
    float time_left = 3;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder iBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RecordService.LocalBinder binder = (RecordService.LocalBinder) iBinder;
            service = binder.getService();
            UpdateGUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        setContentView(R.layout.activity_main);
        InitGUI();
        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (service == null) return;
        UpdateGUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == CREATE_FILE
                && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                file_uri = resultData.getData();
                directory_selected = true;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_about:
                intent = new Intent(this,AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void SelectDirectory(View view) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/binary");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean write_binary = sharedPref.getBoolean("write_binary", true);
        if (write_binary) {
            intent.putExtra(Intent.EXTRA_TITLE, "record.bin");
        } else {
            intent.putExtra(Intent.EXTRA_TITLE, "record.txt");
        }
        startActivityForResult(intent, CREATE_FILE);
    }

    public void StartRecording(View view) {
        if (!directory_selected) {
            tv_FilePath.setText("Select directory");
            return;
        }
        if (service == null) {
            tv_FilePath.setText("Not initialised");
            return;
        }

        if (service.stopedRecording) {
            setContentView(R.layout.activity_countdown);
            tv_Timer = (TextView) findViewById(R.id.tv_Timer);
            timer = new Timer();
            time_left = 3;

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    time_left -= 0.1;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String time_left_text = Float.toString(time_left);
                            tv_Timer.setText(time_left_text.substring(0, 3));
                        }
                    });

                    if (time_left <= 0) {
                        timer.cancel();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setContentView(R.layout.activity_main);
                                InitGUI();
                                service.file_uri = file_uri;

                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                service.afap = sharedPref.getBoolean("afap", false);
                                service.waitTime = 1000/(Integer.parseInt(sharedPref.getString("tps","100")));
                                service.write_binary=sharedPref.getBoolean("write_binary",true);

                                service.StartRecording();
                                tv_FilePath.setText("Started");
                                UpdateGUI();
                            }
                        });

                    }
                }
            };
            timer.schedule(task, 100, 100);

        }
    }

    public void StopRecording(View view) {
        if (service == null) {
            tv_FilePath.setText("Not initialised");
            return;
        }

        if (service.stopedRecording) {
            tv_FilePath.setText("Wasn't started yet");
            return;
        }

        service.stopingRecording = true;
        while (true) {
            if (service.stopedRecording) break;
        }
        tv_FilePath.setText("Stoped");
        UpdateGUI();
    }


    public void UpdateGUI() {
        if (service.stopedRecording) {
            btn_StartRecording.setEnabled(true);
            btn_StopRecording.setEnabled(false);
        } else {
            btn_StartRecording.setEnabled(false);
            btn_StopRecording.setEnabled(true);
        }
    }

    void InitGUI() {
        tv_FilePath = (TextView) findViewById(R.id.tv_Status);
        btn_SelectDirectory = (Button) findViewById(R.id.btn_SelectDirectory);
        btn_StartRecording = (Button) findViewById(R.id.btn_Start);
        btn_StopRecording = (Button) findViewById(R.id.btn_Stop);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Main";
            String description = "Service notificatins";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("channel1", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
