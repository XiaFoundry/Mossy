package com.savings.buddy

import android.app.Application
import com.savings.buddy.data.AppContainer

class SavingApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
