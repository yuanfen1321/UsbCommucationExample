#UsbCommucationExample
UsbCommucationExample

Main Points:

mainly objects used:
UsbDevice, UsbManager,UsbInterface,UsbEndpoint,UsbDeviceConnection,


UsbEndpoint type :
bulk endpoints, zero endpoint, Interrupt endpoints, Isochronous endpoints

transaction supported type are:
Bulk Transaction、Control Transaction、Interrupt Transaction和Isochronous Transaction


endpoint alse has direction(endpoint.getDirection),which is  USB_DIR_IN or USB_DIR_OUT;


mainly function:

UsbDeviceConnection.bulkTransfer(endpointOut, bytes, bytes.length, Constants.USB_TIMEOUT_IN_MS);


ParcelFileDescriptor pfd = usbManager.openAccessory(accessory);
FileDescriptor fd = pfd.getFileDescriptor();
FileInputStream fis = new FileInputStream(fd);


Wraps a Unix file descriptor. It's possible to get the file descriptor used by some classes (such as FileInputStream, FileOutputStream, and RandomAccessFile), and then create new streams that point to the same file descriptor.

USB Host API 


技术要点：
新建线程监听是否有数据到达。采用while(AtomicBoolean)来判断线程是否终止。