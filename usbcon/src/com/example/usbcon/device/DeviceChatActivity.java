package com.example.usbcon.device;

import com.example.usbcon.main.BaseChatActivity;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

public class DeviceChatActivity extends BaseChatActivity {

    private AccessoryCommunicator communicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setBtText("devices_Send to Host");
        communicator = new AccessoryCommunicator(this) {

            @Override
            public void onReceive(byte[] payload, int length) {
                printLineToUI("host> " + new String(payload, 0, length));
            }

            @Override
            public void onError(String msg) {
                printLineToUI("error:" + msg);
            }

            @Override
            public void onConnected() {
                printLineToUI("connected");
            }

            @Override
            public void onDisconnected() {
                printLineToUI("disconnected");
            }
        };
    }    

    @Override
    public void sendString(String string) {
        communicator.send(string.getBytes());
    }
    @Override
    public void onDestroy() {    	
    	communicator.stop();    	
    	super.onDestroy();
    }
}
