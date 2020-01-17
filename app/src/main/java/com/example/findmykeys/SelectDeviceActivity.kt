package com.example.findmykeys

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.select_device_fragment.*

class SelectDeviceActivity: AppCompatActivity() {
    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var pairedDevices: Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1

    companion object {
        var EXTRA_ADDRESS: String = "Device_address"
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

        refresh_button.setOnClickListener{ pairedDeviceList()}
    }

    fun pairedDeviceList() {
        pairedDevices = bluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()
        if(!pairedDevices.isEmpty()) {
            for(device: BluetoothDevice in pairedDevices) {
                list.add(device)
            }
        } else {
            Toast.makeText(this,"No paired device available", Toast.LENGTH_SHORT).show()
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        select_device_list.adapter = adapter
        select_device_list.onItemClickListener = AdapterView.OnItemClickListener{_ ,_ ,position, _ ->
            val device : BluetoothDevice = list[position]
            val address = device.address
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_ADDRESS, address)
            startActivity(intent)
        }


    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
            if(resultCode == Activity.RESULT_OK) {
                if(!bluetoothAdapter!!.isEnabled) {
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(this, "Bluetooth is disabled", Toast.LENGTH_SHORT).show()
            }
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth enabling has been cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}