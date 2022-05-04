package com.example.monitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class Content : AppCompatActivity() {
    private var messageManager: MessageManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val notificationHandler = NotificationHandler(this)

        Thread {
            run {
                messageManager = MessageManager(applicationContext, this, notificationHandler)
                recyclerView.adapter = messageManager!!.mAdapter

                val mqttService = MqttMessageService()
                mqttService.connect(applicationContext, messageManager!!)
            }
        }.start()
    }

    fun clear(view: View) {
        messageManager!!.clear()
    }

    fun logout(view: android.view.View) {
        val pref = getSharedPreferences("userdata", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("isLogin", false).commit()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

class MyAdapter(private val dataList: List<DeviceState>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stateText: TextView = view.findViewById(R.id.state)
        val dateText: TextView = view.findViewById(R.id.date)
        val timeText: TextView = view.findViewById(R.id.time)
    }

    override fun getItemCount() = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.stateText.text = dataList[position].state
        holder.dateText.text = dataList[position].date
        holder.timeText.text = dataList[position].time
    }
}





