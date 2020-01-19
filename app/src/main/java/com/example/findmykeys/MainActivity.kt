package com.example.findmykeys

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        var bluetoothSocket:BluetoothSocket? = null
        var isConnected: Boolean = false
        var status: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        find_my_keys.setOnClickListener {
            if(!status) {
                status = true
                sendCommand("49")
                find_my_keys.setBackgroundResource(R.drawable.red_button_background)
                find_my_keys.setText("Buzzing....")
            } else {
                status = false
                sendCommand("48")
                find_my_keys.setBackgroundResource(R.drawable.button_background)
                find_my_keys.setText("Find My keys")
            }
        }

        disconnect_button.setOnClickListener{
            disconnect()
            val intent = Intent(this, SelectDeviceActivity::class.java)
            startActivity(intent)
        }
    }

    private fun disconnect() {
        if(bluetoothSocket != null) {
            try{
                bluetoothSocket!!.close()
                bluetoothSocket = null
                isConnected = false
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }

    }


    private fun sendCommand(input: String) {
        if(bluetoothSocket != null) {
            try {
                bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }

    }
}
