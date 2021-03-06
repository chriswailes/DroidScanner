/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\David Cheeseman\\workspace\\DroidScanner\\src\\com\\nuvsoft\\android\\scanner\\ScannerServiceInterface.aidl
 */
package com.nuvsoft.android.scanner;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
// Declare the interface.

public interface ScannerServiceInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.nuvsoft.android.scanner.ScannerServiceInterface
{
private static final java.lang.String DESCRIPTOR = "com.nuvsoft.android.scanner.ScannerServiceInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an ScannerServiceInterface interface,
 * generating a proxy if needed.
 */
public static com.nuvsoft.android.scanner.ScannerServiceInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.nuvsoft.android.scanner.ScannerServiceInterface))) {
return ((com.nuvsoft.android.scanner.ScannerServiceInterface)iin);
}
return new com.nuvsoft.android.scanner.ScannerServiceInterface.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onEvent:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onEvent(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.nuvsoft.android.scanner.ScannerServiceInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void onEvent(java.lang.String e) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(e);
mRemote.transact(Stub.TRANSACTION_onEvent, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onEvent = (IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onEvent(java.lang.String e) throws android.os.RemoteException;
}
