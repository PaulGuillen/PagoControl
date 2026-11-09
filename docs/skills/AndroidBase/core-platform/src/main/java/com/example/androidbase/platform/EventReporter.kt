package com.example.androidbase.platform

interface EventReporter {
    fun screen(name: String)
    fun action(name: String)
    fun error(operation: String)
}
