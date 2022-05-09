package com.example.monitor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
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
            }
        }.start()
    }

    fun confirm(view: View) {
        val buttonConfirm: TextView = view.findViewById(R.id.buttonConfirm)
        messageManager!!.verify()
        buttonConfirm.visibility = View.GONE
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
        val timeText: TextView = view.findViewById(R.id.time)
        val identityText: TextView = view.findViewById(R.id.identity)
    }

    override fun getItemCount() = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.stateText.text = dataList[position].state
        holder.timeText.text = dataList[position].time
        holder.identityText.text = dataList[position].identity
    }
}





