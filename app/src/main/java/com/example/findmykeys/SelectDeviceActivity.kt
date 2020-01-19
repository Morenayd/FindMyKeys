package com.example.findmykeys

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcel
import android.os.ParcelUuid
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.select_device_fragment.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class SelectDeviceActivity: AppCompatActivity() {
    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var pairedDevices: Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1

    companion object {
        var bluetoothSocket: BluetoothSocket? = null
        lateinit var progressBar:ProgressDialog
        lateinit var bluetoothAdapter: BluetoothAdapter
        lateinit var address:String
        var isConnected: Boolean = false
        private lateinit var uuid: UUID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_device_fragment)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null) {
            Toast.makeText(this, "This device does not support bluetooth",Toast.LENGTH_LONG).show()
            return
        }

        if(!bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }
        pairedDeviceList()
        refresh_button.setOnClickListener{ pairedDeviceList()}
    }

    fun pairedDeviceList() {
        pairedDevices = bluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()
        val namelist : ArrayList<String> = ArrayList()
        if(!pairedDevices.isEmpty()) {
            for(device: BluetoothDevice in pairedDevices) {
                namelist.add(device.name)
                list.add(device)
            }
        } else {
            Toast.makeText(this,"No paired device available", Toast.LENGTH_SHORT).show()
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, namelist)
        select_device_list.adapter = adapter
        select_device_list.onItemClickListener = AdapterView.OnItemClickListener{_ ,_ ,position, _ ->
            val device : BluetoothDevice = list[position]
            address = device.address
            ConnectToDevice(this).execute()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
            if(resultCode == Activity.RESULT_OK) {
                if(bluetoothAdapter!!.isEnabled) {
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(this, "Bluetooth is disabled", Toast.LENGTH_SHORT).show()
            }
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth enabling has been cancelled", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Could not connect", Toast.LENGTH_LONG).show()
                Log.i("Data", "Could not connect")
            } else {
                connected = true
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }
            progressBar.dismiss()
        }

        override fun doInBackground(vararg params: Void?): String? {
            try {
                if (bluetoothSocket == null || !isConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
                    uuid = UUID.fromString(device.uuids[0].toString())
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    try {
                        bluetoothSocket!!.connect()
                    } catch(e:IOException) {
                        e.printStackTrace()
                        try {
                            println("trying fallback...")
                            var paramTypes = arrayOf<Class<*>>(Integer.TYPE)
                            bluetoothSocket = device.javaClass.getMethod("createRfcommSocket", *paramTypes).invoke(device, Integer.valueOf(2)) as BluetoothSocket
                            bluetoothSocket!!.connect()
                            println("connected")
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                            println("Couldn't establish Bluetooth connection!")
                        }
                    }
                }
            } catch (e: IOException) {
                connected = false
                e.printStackTrace()
            }
            return null
        }

    }
}