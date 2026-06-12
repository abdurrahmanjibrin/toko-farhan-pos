package com.tokofarhan.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tokofarhan.pos.ui.navigation.AppNavigation
import com.tokofarhan.pos.ui.theme.TokoFarhanTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.tokofarhan.pos.data.local.DatabaseSeeder
import com.tokofarhan.pos.data.local.TransactionSeeder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            val db = (applicationContext as TokoFarhanApp).database
            DatabaseSeeder.seedDatabase(this@MainActivity, db.productDao())
            TransactionSeeder.seedTransactions(this@MainActivity, db.productDao(), db.transactionDao())
        }

        setContent {
            TokoFarhanTheme {
                AppNavigation()
            }
        }
    }
}
