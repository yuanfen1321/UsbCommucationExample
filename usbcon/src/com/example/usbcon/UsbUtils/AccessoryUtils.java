package com.example.usbcon.UsbUtils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

public class AccessoryUtils {
	
	private Context mContext;
	
	private UsbManager usbManager;
    private FileInputStream inStream;
    private FileOutputStream outStream;
    private ParcelFileDescriptor fileDescriptor;
    
	
	public AccessoryUtils(Context context) {
		this.mContext = context;
		usbManager = (UsbManager) this.mContext.getSystemService(Context.USB_SERVICE);        
	}
	public UsbAccessory searchAccessory() {
		final UsbAccessory[] accessoryList = usbManager.getAccessoryList();
        if (accessoryList == null || accessoryList.length == 0) {
            LogUtils.e("no accessory found");
            return null;
        } else {
            return accessoryList[0];
        }
	}
	public boolean openAccessory(UsbAccessory accessory) {
		if(accessory != null) {
	    	if(!usbManager.hasPermission(accessory)) {
	    		usbManager.requestPermission(accessory, PendingIntent.getBroadcast(mContext, 0, new Intent("giec"), 0));
	    	}
	        fileDescriptor = usbManager.openAccessory(accessory);
	        if (fileDescriptor != null) {
	
	            FileDescriptor fd = fileDescriptor.getFileDescriptor();
	            inStream = new FileInputStream(fd);
	            outStream = new FileOutputStream(fd);
	            if(inStream == null || outStream == null) {
	            	LogUtils.e("error:isStream or OutStream");
	            	return false;
	            } else {
	            	return true;
	            }
	        } else {
	            LogUtils.e("could not connect");
	            return false;
	        }
		}else {
			LogUtils.e("accessory is null");
			return false;
		}
    }
	
	public boolean sendData(byte[] bytes) {
		if(outStream != null) {
			try{
				outStream.write(bytes);
				LogUtils.d("send data successfullly");
				return true;
			} catch(IOException e) {
				LogUtils.e("error:"+e.toString());
				return false;
			}
		} else {
			LogUtils.e("outStream is null,did you open accessory?");
			return false;
		}
	}
	public byte[] receiveData() {
		if(inStream == null) {
			LogUtils.e("inStream is null, did you open accessory?");
			return null;
		} else {
//			byte[] bytes = new byte[Constants.BUFFER_SIZE_IN_BYTES];
			byte[] bytes = new byte[Constants.ACCESSORY_MAX_BUFFER_SIZE];
			try {
//				if(inStream.available() > 0) {
					int len = inStream.read(bytes);
					if(len > 0){
						LogUtils.d("receive data successfully");
						return bytes;
					} else {
						return null;
					}
//				} else {
//					return null;
//				}
			} catch (IOException e) {
				LogUtils.e("error:"+e.toString());
				return null;
			}
		}		
	}

    public void closeAccessory() {

        try {
            if (fileDescriptor != null) {
            	sendData(Constants.DISCONNECTED.getBytes());
                fileDescriptor.close();
            } else if(inStream != null) {
            	inStream.close();
            } else if(outStream != null) {
            	outStream.close();
            }
        } catch (IOException e) {
        } finally {
            fileDescriptor = null;
            inStream = null;
            outStream = null;
        }
    }
}
