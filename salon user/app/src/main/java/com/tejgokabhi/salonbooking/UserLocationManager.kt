package com.tejgokabhi.salonbooking

object UserLocationManager {
    private var _userLocation: String? = null
    private val listeners = mutableListOf<(String) -> Unit>()

    var userLocation: String?
        get() = _userLocation
        set(value) {
            _userLocation = value
            if (value != null) {
                notifyListeners(value)
            }
        }

    fun addListener(listener: (String) -> Unit) {
        listeners.add(listener)
        _userLocation?.let { listener(it) } // Immediately notify if location is already set
    }

    private fun notifyListeners(location: String) {
        for (listener in listeners) {
            listener(location)
        }
    }
}
