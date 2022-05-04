package com.example.monitor

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import org.json.JSONException
import org.json.JSONObject

class MessageManager(applicationContext: Context,
                     private val activity: AppCompatActivity,
                     val notificationHandler: NotificationHandler) {

    private val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "smart_home").build()
    private val dao = db.getDao()
    private val dataList = dao.getAll()
    val mAdapter = MyAdapter(dataList)

    fun add(message: String) {
        var state = ""
        var date = ""
        var time = ""

        try {
            val jsonObject = JSONObject(message)
            state = jsonObject.getString("state")
            date = jsonObject.getString("date")
            time = jsonObject.getString("time")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        notificationHandler.send("狀態 : $state, 日期 : $date, 時間 : $time")
        val deviceState = DeviceState(0, state, date, time)

        Thread {
            run {
                dao.insert(deviceState)
            }
        }.start()
        dataList.add(deviceState)

        activity.runOnUiThread {
            mAdapter.notifyItemInserted(dataList.size-1)
        }
    }

    fun clear() {
        Thread {
            run {
                dao.clear()
            }
        }.start()
        dataList.clear()

        activity.runOnUiThread {
            mAdapter.notifyDataSetChanged()
        }
    }
}
