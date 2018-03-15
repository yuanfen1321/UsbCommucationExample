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

host端采用了FileDescriptor获取文件流的方式进行数据的读写（传输）。
device端是采用了传统的BulkTransfer，对endpoint进行数据读写（传输）。
对于接收信息，则是采用了一个新的线程的while（Atomicboolean）循环来监听是否有新的数据到达。
host端和device端所需要的api的封装（要向客户提供api），如打开accessory设备，

困难点byte[]长度的选取。