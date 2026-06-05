package me.hd.wauxv.hook.factory

import me.hd.wauxv.hook.core.native.ObfNative

object MagicFactory {

    @JvmStatic
    fun get(j: Long, strArr: Array<String>): String {
        return ObfNative.get(j, strArr)
    }

    @JvmStatic
    fun toAppClass(str: String): Class<*> {
        return MagicFactory::class.java.classLoader!!.loadClass(str)
    }
}
