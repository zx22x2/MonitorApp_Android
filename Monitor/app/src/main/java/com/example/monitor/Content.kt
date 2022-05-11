package com.example.monitor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

class Content : AppCompatActivity() {
    private val tag = "Activity/Content"

    var db: AppDatabase? = null
    var dao: DeviceStateDao? = null
    private var dataList = mutableListOf<DeviceState>()

    private var messageManager: MessageManager? = null

    var mAdapter: MyAdapter? = null
    var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content)
        Log.d(tag, "onCreate()")

        Thread {
            run {
                db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "smart_home").build()
                dao = db!!.getDao()
                dataList.addAll(dao!!.getAll())
            }
        }.start()

        val notificationHandler = NotificationHandler(this)

        val pref = getSharedPreferences("userdata", MODE_PRIVATE)
        val username = pref.getString("username", null)

        mAdapter = MyAdapter(dataList!!)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView!!.layoutManager = LinearLayoutManagerWrapper(this, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.adapter = mAdapter

        Thread {
            run {
                messageManager = MessageManager(applicationContext, this, notificationHandler, username!!
                )
            }
        }.start()
    }

    fun record(data: DeviceState) {
        Log.d(tag, "record()")
        Thread {
            run {
                dao?.insert(data)
            }
        }.start()
        dataList!!.add(data)
        Log.d(tag, "$dataList")
        mAdapter!!.notifyItemInserted(dataList!!.size-1)
    }

    fun confirm(view: View) {
        val buttonConfirm: TextView = view.findViewById(R.id.buttonConfirm)
        messageManager!!.verify()
        buttonConfirm.visibility = View.GONE
    }

    fun clear(view: View) {
        Thread {
            run {
                dao?.clear()
            }
        }.start()
        dataList!!.clear()
        mAdapter!!.notifyDataSetChanged()
    }

    fun logout(view: android.view.View) {
        val pref = getSharedPreferences("userdata", MODE_PRIVATE)
        val editor = pref.edit()
        editor.clear().commit()

        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        messageManager?.disconnectMqtt()
        Log.d(tag, "onDestroy()")
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





