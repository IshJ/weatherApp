// ISharedMem.aidl
package org.woheller69.weather;

// Declare any non-default types here with import statements

interface ISharedMem {
    ParcelFileDescriptor OpenSharedMem(String name, int size, boolean create);
}