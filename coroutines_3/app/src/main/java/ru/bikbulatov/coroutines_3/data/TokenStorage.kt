package ru.bikbulatov.coroutines_3.data

object TokenStorage {
    var token = "old_HashedToken"
    
    fun reset() {
        token = "old_HashedToken"
    }
}