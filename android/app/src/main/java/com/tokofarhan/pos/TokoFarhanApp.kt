package com.tokofarhan.pos

import android.app.Application
import com.tokofarhan.pos.data.local.AppDatabase

class TokoFarhanApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
