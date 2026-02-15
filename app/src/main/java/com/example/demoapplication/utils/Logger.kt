package com.example.demoapplication.utils

import timber.log.Timber
import android.util.Log

object Logger {
    
    enum class LogLevel(val priority: Int) {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR)
    }
    
    fun init(isDebug: Boolean = true) {
        if (isDebug) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return "${super.createStackElementTag(element)}:${element.lineNumber}"
                }
            })
        }
    }
    
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.VERBOSE, tag, message, throwable)
    }
    
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.DEBUG, tag, message, throwable)
    }
    
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.INFO, tag, message, throwable)
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.WARN, tag, message, throwable)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, throwable)
    }
    
    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.VERBOSE -> if (throwable != null) Timber.tag(tag).v(throwable, message) else Timber.tag(tag).v(message)
            LogLevel.DEBUG -> if (throwable != null) Timber.tag(tag).d(throwable, message) else Timber.tag(tag).d(message)
            LogLevel.INFO -> if (throwable != null) Timber.tag(tag).i(throwable, message) else Timber.tag(tag).i(message)
            LogLevel.WARN -> if (throwable != null) Timber.tag(tag).w(throwable, message) else Timber.tag(tag).w(message)
            LogLevel.ERROR -> if (throwable != null) Timber.tag(tag).e(throwable, message) else Timber.tag(tag).e(message)
        }
    }
    
    inline fun performanceLog(tag: String, operation: String, block: () -> Unit) {
        val startTime = System.currentTimeMillis()
        d(tag, "Starting $operation")
        try {
            block()
            val duration = System.currentTimeMillis() - startTime
            d(tag, "Completed $operation in ${duration}ms")
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            e(tag, "Failed $operation after ${duration}ms", e)
            throw e
        }
    }
}