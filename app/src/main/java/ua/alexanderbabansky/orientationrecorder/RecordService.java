package ua.alexanderbabansky.orientationrecorder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Clock;

public class RecordService extends Service {

    Uri file_uri;
    boolean write_binary,afap;
    long waitTime;
    volatile boolean stopingRecording=true;
    volatile boolean stopedRecording=true;

    private final IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        RecordService getService() {
            return RecordService.this;
        }
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {

    }

    public void StartRecording() {
        Notification.Builder notificationBuilder=null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(this, "channel1");
        }else{
            notificationBuilder = new Notification.Builder(this);
        }
        notificationBuilder.setContentTitle("OrientationRecorder")
                .setContentText("Recording started")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
        Notification notification = notificationBuilder.build();
        startForeground(1, notification);


        InitNative();
        stopingRecording=false;
        stopedRecording=false;

        try {
            final ParcelFileDescriptor pfd = getApplication().getContentResolver().openFileDescriptor(file_uri, "w");
            final FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    long lastTimespamp = System.currentTimeMillis();
                    long firstTimestamp = lastTimespamp;
                    while (true) {
                        try {
                            long currTime = System.currentTimeMillis();
                            if (currTime - lastTimespamp < waitTime && !afap) {
                                continue;
                            }
                            lastTimespamp = System.currentTimeMillis();
                            float[] orientation = GetOrientation();
                            byte[] data;
                            if (write_binary) {
                                data = ByteBuffer.allocate(8 + 4 * 4)
                                        .putLong(lastTimespamp - firstTimestamp)
                                        .putFloat(orientation[0])
                                        .putFloat(orientation[1])
                                        .putFloat(orientation[2])
                                        .putFloat(orientation[3]).array();
                            }else {
                                data = (Long.toString(lastTimespamp - firstTimestamp) + " " +
                                        Float.toString(orientation[0]) + " " +
                                        Float.toString(orientation[1]) + " " +
                                        Float.toString(orientation[2]) + " " +
                                        Float.toString(orientation[3]) + "\n").getBytes();
                            }

                            fileOutputStream.write(data);
                            if (stopingRecording) {
                                fileOutputStream.close();
                                pfd.close();
                                break;
                            }
                        }catch (IOException io){
                            stopingRecording=true;
                            break;
                        }
                    }

                    RecordService.this.stopForeground(true);
                    DisposeNative();
                    stopedRecording = true;
                }
            });
            thread.start();
        }catch (FileNotFoundException fnf){

        }catch (IOError io){

        }

    }

    public void StopRecording(){
        stopingRecording=true;
    }

    public native void InitNative();
    public native void DisposeNative();
    public native float[] GetOrientation();
}
