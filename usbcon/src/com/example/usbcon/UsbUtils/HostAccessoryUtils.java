package com.example.usbcon.UsbUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

/**
 * @author Giec:lijh
 *
 */
public class HostAccessoryUtils {

	private static final String MANUFACTURER = "Giec";
	private static final String MODEL = "Accessory Display";
	private static final String DESCRIPTION = "Giec Usb Test Application";
	private static final String VERSION = "1.0";
	private static final String URI = "http://www.giec.cn/";
	private static final String SERIAL = "0000000012345678";

	private static boolean isConnected = false;

	private Context mContext;

	private UsbDevice mDevice;
	private UsbManager mUsbManager;
	private HashMap<String, UsbDevice> deviceList;
	
	private UsbInterface mUsbInterface;
	private UsbDeviceConnection mUsbDeviceConnection;
	private UsbEndpoint endpointIn, endpointOut;

	private int protocolVersion;

	public HostAccessoryUtils(Context context) {
		this.mContext = context;
		mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		deviceList =mUsbManager.getDeviceList();
	}

	/**
	 * check if device is in accessory mode
	 * @param device
	 * @return
	 */
	public boolean isAccessory(UsbDevice device) {
		final int vid = device.getVendorId();
		final int pid = device.getProductId();
		return vid == HostAccessoryConstants.USB_ACCESSORY_VENDOR_ID
				&& (pid == HostAccessoryConstants.USB_ACCESSORY_PRODUCT_ID
						|| pid == HostAccessoryConstants.USB_ACCESSORY_ADB_PRODUCT_ID
						|| pid == HostAccessoryConstants.USB_ACCESSORY_AUDIO_PRODUCT_ID
						|| pid == HostAccessoryConstants.USB_ACCESSORY_ADB_AUDIO_PRODUCT_ID);
	}

	/**
	 * switch device to accessory.
	 * @param device
	 * @return
	 */
	public boolean switchToAccessoryMode(UsbDevice device) {
		UsbDeviceConnection conn = mUsbManager.openDevice(device);
		return switchToAccessoryMode(conn);
	}
	
	/**
	 * search usbDevices and try to switch it to accessory mode
	 * @return UsbDevices in accessory mode
	 */
	public List<UsbDevice> searchForUsbAccessory() {
		List<UsbDevice> accessories = new ArrayList<>();
		if(deviceList.size()>0) {
			for (UsbDevice device : deviceList.values()) {
				LogUtils.d("searchForUsbAccessory--------host:deviceslist:\n\n" + device.toString() + "\n\n");
				if(!isAccessory(device)){
					if (switchToAccessoryMode(device)) {
						if (isAccessory(device)) {
							LogUtils.d("device:found!" + device.toString());
							accessories.add(device);
						} else {
							LogUtils.d("find device but set accessory mode fail;-----device is:" + device.toString());
						}
					}
				} else {
					LogUtils.d("device is in accessory mode now:"+device.toString());
					accessories.add(device);
				}
			}
		}
		return accessories;
	}

	public boolean connect(UsbDevice device) {
		isConnected = false;
		mDevice = device;
		if (!mUsbManager.hasPermission(device)) {
			mUsbManager.requestPermission(device, PendingIntent.getBroadcast(mContext, 0, new Intent("Giec:lijh"), 0));
		}
		UsbDeviceConnection conn = mUsbManager.openDevice(device);
		mUsbInterface = device.getInterface(0);
		if (mUsbInterface != null) {
			for (int i = 0; i < mUsbInterface.getEndpointCount(); i++) {
				UsbEndpoint endpoint = mUsbInterface.getEndpoint(i);
				if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
					if (endpoint.getDirection() == UsbConstants.USB_DIR_IN)
						endpointIn = endpoint;
					else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT)
						endpointOut = endpoint;
				}
			}
		}
		if (endpointIn == null) {
			LogUtils.e("Input Endpoint not found");
			return false;
		} else if (endpointOut == null) {
			LogUtils.e("Output Endpoint not found");
			return false;
		} else if (conn == null) {
			LogUtils.e("Could not open device");
			return false;
		} else if (!conn.claimInterface(mUsbInterface, true)) {
			LogUtils.e("clainmInterface failed");
			return false;
		} else {
			mUsbDeviceConnection = conn;
			isConnected = true;
			return true;
		}
	}

	/**
	 * send data to accessory, it should be used in another thread
	 * @param bytes
	 * @return true means successfully
	 */
	public boolean sendData(byte[] bytes) {
		int result = mUsbDeviceConnection.bulkTransfer(endpointOut, bytes, bytes.length, Constants.USB_TIMEOUT_IN_MS);
		if (result < 0) {
			LogUtils.e("send fail");
			return false;
		} else if (result != bytes.length) {
			LogUtils.e("send error: result is " + result + "but bytes is " + bytes.length);
			return false;
		} else {
			LogUtils.d("byte is " + bytes.length);
			return true;
		}
	}

	/**
	 * receive data from accessory, it should be used in another thread;
	 * @return data in byte[] format
	 */
	public byte[] receiveData() {
		byte buff[] = new byte[Constants.BUFFER_SIZE_IN_BYTES];
		int len = mUsbDeviceConnection.bulkTransfer(endpointIn, buff, buff.length, Constants.USB_TIMEOUT_IN_MS);
		if (len > 0) {
			LogUtils.d("received: " + len);			
			return buff;
		} else {
			return null;
		}
	}

	public void disconnect() {
		if (isConnected && mUsbDeviceConnection != null) {
			LogUtils.d("Disconnecting from device: " + mDevice);
			sendData(Constants.DISCONNECTED.getBytes());
			mUsbDeviceConnection.releaseInterface(mUsbInterface);
			mUsbDeviceConnection.close();
		}
		mDevice = null;
		mUsbDeviceConnection = null;
		endpointIn = null;
		endpointOut = null;
		mUsbInterface = null;
		isConnected = false;
	}
	
	private boolean switchToAccessoryMode(UsbDeviceConnection conn) {
		if (conn == null)
			return false;
		protocolVersion = getProtocol(conn);

		LogUtils.d("Protocol version: " + protocolVersion);

		if (protocolVersion < 1) {
			LogUtils.e("Device does not support accessory protocol.");
			return false;
		}
		// Send identifying strings.
		sendString(conn, HostAccessoryConstants.ACCESSORY_STRING_MANUFACTURER, MANUFACTURER);
		sendString(conn, HostAccessoryConstants.ACCESSORY_STRING_MODEL, MODEL);
		sendString(conn, HostAccessoryConstants.ACCESSORY_STRING_DESCRIPTION, DESCRIPTION);
		sendString(conn, HostAccessoryConstants.ACCESSORY_STRING_VERSION, VERSION);
		sendString(conn, HostAccessoryConstants.ACCESSORY_STRING_URI, URI);
		sendString(conn, HostAccessoryConstants.ACCESSORY_STRING_SERIAL, SERIAL);
		// set accessory mode start.
		LogUtils.d("Sending accessory start request.");
		int len = conn.controlTransfer(UsbConstants.USB_DIR_OUT | UsbConstants.USB_TYPE_VENDOR,
				HostAccessoryConstants.ACCESSORY_START, 0, 0, null, 0, Constants.USB_TIMEOUT_IN_MS);

		if (conn != null)
			conn.close();

		if (len != 0) {
			LogUtils.e("Device refused to switch to accessory mode.");
			return false;
		} else {
			LogUtils.d("Device switch to accessroy mode:" + len);
			return true;
		}
	}

	private int sendString(UsbDeviceConnection conn, int index, String string) {
		byte[] buffer = (string + "\0").getBytes();
		int len = conn.controlTransfer(UsbConstants.USB_DIR_OUT | UsbConstants.USB_TYPE_VENDOR,
				HostAccessoryConstants.ACCESSORY_SEND_STRING, 0, index, buffer, buffer.length,
				Constants.USB_TIMEOUT_IN_MS);
		if (len != buffer.length) {
			LogUtils.e("Failed to send string " + index + ": \"" + string + "\"");
		} else {
			LogUtils.d("Sent string " + index + ": \"" + string + "\"");
		}
		return len;
	}

	private int getProtocol(UsbDeviceConnection conn) {
		byte buffer[] = new byte[2];
		int len = conn.controlTransfer(UsbConstants.USB_DIR_IN | UsbConstants.USB_TYPE_VENDOR,
				HostAccessoryConstants.ACCESSORY_GET_PROTOCOL, 0, 0, buffer, 2, 10000);
		if (len != 2) {
			return -1;
		}
		return (buffer[1] << 8) | buffer[0];
	}
}
