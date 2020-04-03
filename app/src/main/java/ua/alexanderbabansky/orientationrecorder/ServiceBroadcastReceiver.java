package ua.alexanderbabansky.orientationrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceBroadcastReceiver extends BroadcastReceiver {

    RecordService service=null;
    public ServiceBroadcastReceiver(RecordService service){
        this.service = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (service==null)return;
        if (intent.getAction()=="ua.AlexanderBabansky.OrientationRecorder.STOP_RECORDING"){
            service.StopRecording();
            service=null;
            service.unregisterReceiver(this);
        }
    }
}
