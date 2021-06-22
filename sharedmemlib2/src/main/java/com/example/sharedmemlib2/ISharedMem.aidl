// ISharedMem.aidl
package com.example.sharedmemlib2;

// Declare any non-default types here with import statements

interface ISharedMem {
    ParcelFileDescriptor OpenSharedMem(String name, int size, boolean create);
}