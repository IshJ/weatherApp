// IMyAidlInterface.aidl
package com.example.sharedmem;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    ParcelFileDescriptor OpenSharedMem(String name, int size, boolean create);
}
