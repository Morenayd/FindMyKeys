package com.example.findmykeys

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private val uuid:UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var bluetoothSocket:BluetoothSocket? = null
        lateinit var progressBar:ProgressDialog
        lateinit var bluetoothAdapter: BluetoothAdapter
        lateinit var address:String
        var isConnected: Boolean = false
        var status: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        address = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)

        ConnectToDevice(this).execute()
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

    private class ConnectToDevice(c:Context):AsyncTask<Void, Void, String>() {
        private var connected: Boolean = true
        private val context: Context

        init {
            context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar = ProgressDialog.show(context, "Connecting....", "Please wait")
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connected) {
                Log.i("Data", "Could not connect")
            } else {
                connected = true
            }
            progressBar.dismiss()
        }

        override fun doInBackground(vararg params: Void?): String? {
            try {
                if (bluetoothSocket == null || !isConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    bluetoothSocket!!.connect()
                }
                } catch (e: IOException) {
                connected = false
                e.printStackTrace()
            }
            return null
            }

    }

}
