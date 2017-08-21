package com.example.usbcon.UsbUtils;

public class Constants {
    public static final int USB_TIMEOUT_IN_MS = 100;
    /**
     * it is said that in linux  limit of the buffer pipe size is 32769 bytes, 
     * but from my test, the buffer size can be much larger.
     * @see https://stackoverflow.com/questions/10889461/android-usb-host-api-bulk-transfer-buffer-size
     */    
    public static final int BUFFER_SIZE_IN_BYTES = 16384;
    public static final String DISCONNECTED = "disconnectedCmd";
    public static final int ACCESSORY_MAX_BUFFER_SIZE = 16384;
    
}
