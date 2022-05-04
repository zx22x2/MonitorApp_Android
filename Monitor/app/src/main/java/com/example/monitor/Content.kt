package com.example.monitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import androidx.room.*
import java.util.ArrayList

class Content : AppCompatActivity() {

    private val channelID = "channel_id"
    private val channelName = "channel name"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "smart_home").build()
        val dao = db.getDao()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        Thread {
            run {
                val dataList = dao.getAll()
                recyclerView.adapter = MyAdapter(dataList)
            }
        }.start()

        //listener()
    }



    fun logout(view: android.view.View) {
        val pref = getSharedPreferences("userdata", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("isLogin", false).commit()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun send(view: android.view.View) {
        val queue = Volley.newRequestQueue(this)
        val url = "http://hi87.atwebpages.com/receiver.php"

        val senderRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                Log.d("ResponseLog", response)
            }, Response.ErrorListener { error ->
                Log.e("ErrorLog", error.toString())
            }) {
            override fun getParams(): Map<String, String>? {
                val params = HashMap<String, String>()
                params.put("door_state", "open")

                return params
            }

        }

        queue.add(senderRequest)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun listener() {
        val url = "http://hi87.atwebpages.com/sender.php"

        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val builder = Notification.Builder(this, channelID)
            .setContentTitle("門窗監控")
            .setSmallIcon(R.drawable.ic_launcher_foreground)

        Thread {
            run {
                while(true) {
                    val receiveRequest = object : StringRequest(Method.POST, url,
                        Response.Listener { response ->
                            if(response == "open") {
                                builder.setContentText("裝置當前狀態 : 開啟")
                                val notification = builder.build()
                                manager.notify(0, notification)
                            }
                            if(response == "close") {
                                builder.setContentText("裝置當前狀態 : 關閉")
                                val notification = builder.build()
                                manager.notify(0, notification)
                            }
                        }, Response.ErrorListener {
                                error -> Log.e("ErrorLog", error.toString())
                        }) {
                        override fun getParams(): Map<String, String> {
                            val params = HashMap<String, String>()
                            params["notice"] = "1"

                            return params
                        }
                    }
                    val queue = Volley.newRequestQueue(this)
                    queue.add(receiveRequest)

                    Thread.sleep(1000)
                }
            }
        }.start()
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
        holder.stateText.text = "state" + dataList[position].state
        holder.dateText.text = "date" + dataList[position].date
        holder.timeText.text = "time" + dataList[position].time
    }

}

@Entity(tableName = "device_state")
data class DeviceState(
    @PrimaryKey(true) val id: Int,
    val state: String,
    val date: String,
    val time: String
)

@Dao
interface DeviceStateDao {
    @Query("SELECT * FROM device_state")
    fun getAll(): List<DeviceState>

    @Insert
    fun insert(data: DeviceState)

    @Delete
    fun delete(vararg data: DeviceState)
}

@Database(entities = [DeviceState::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): DeviceStateDao
}


