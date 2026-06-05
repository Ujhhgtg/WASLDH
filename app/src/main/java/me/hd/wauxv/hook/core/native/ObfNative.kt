package me.hd.wauxv.hook.core.native

import android.util.Log

object ObfNative {
    init {
        try {
            // Load the library name matching your logs: libwauxv-core.so
            System.loadLibrary("wauxv-core")
            Log.d("JNI_Bridge", "libwauxv-core.so loaded successfully!")
        } catch (e: Throwable) {
            Log.e("JNI_Bridge", "Failed to load native library", e)
        }
    }

    external fun get(key: Long, array: Array<String>): String
}