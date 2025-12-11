package com.example.lab4notesreminderapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast

class ConnectivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val active = cm.activeNetworkInfo?.isConnectedOrConnecting == true
        Toast.makeText(context, "Network changed: connected = $active", Toast.LENGTH_SHORT).show()
    }
}