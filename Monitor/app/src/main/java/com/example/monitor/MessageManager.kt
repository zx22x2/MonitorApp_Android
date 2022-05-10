package com.example.monitor

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import org.json.JSONException
import org.json.JSONObject

class MessageManager(applicationContext: Context,
                     private val activity: AppCompatActivity,
                     private val notificationHandler: NotificationHandler,
                     private val username: String) {

    private val tag = "MessageManager"
    private val mqttService = MqttMessageService(applicationContext, this)
    private val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "smart_home").build()
    private val dao = db.getDao()
    private val dataList = dao.getAll()
    val mAdapter = MyAdapter(dataList)

    private val buttonConfirm: Button = activity.findViewById(R.id.buttonConfirm)
    private var isVerifying = false
    private var isVerify = false
    private var isDeviceClose = true


    init {
        mqttService.connect()
    }

    fun add(topic: String, message: String) {
        //裝置狀態改變
        if (topic == "monitor/door/state_change") {
            val jsonObject = JSONObject(message)
            val state = jsonObject.getString("state")

            val deviceStateTextView: TextView = activity.findViewById(R.id.device_state)
            deviceStateTextView.text = state
            //進行認領判斷
            if (state == "開") {
                val jsonObject = JSONObject(message)
                val state = jsonObject.getString("state")
                val time = jsonObject.getString("time")

                notificationHandler.send("裝置於$time 被開啟，若是您開啟的請進行認領。")
                buttonConfirm.visibility = View.VISIBLE
                isVerifying = true
                isDeviceClose = false
                Thread {
                    run {
                        var count = 0.0
                        while (isVerifying && !isVerify && count <= 10 && !isDeviceClose) {
                            count += 0.1
                            Thread.sleep(100)
                        }
                        if (isVerify) {
                            val identity = username
                            isVerify = false
                            isVerifying = false
                            //傳送資料
                            val topic = "monitor/door/log"
                            val data = "{\"state\": \"$state\", \"time\": \"$time\", \"identity\": \"$identity\"}"
                            val payload = data.toByteArray()
                            mqttService.mqttClientService!!.publish(mqttService.mqttAndroidClient!!, topic, payload)
                        }
                        else if (count > 10 || isDeviceClose) {
                            cancel()
                            record(jsonObject)
                        }
                    }
                }.start()
            }
            else if (state == "關") {
                isDeviceClose = true
            }
        }
        //記錄資料
        if (topic == "monitor/door/log") {
            cancel()
            record(JSONObject(message))
        }
    }

    fun verify() {
        isVerify = true
    }

    private fun cancel() {
        if (isVerifying) {
            activity.runOnUiThread {
                buttonConfirm.visibility = View.GONE
            }
            isVerifying = false
        }
    }

    private fun record(jsonObject: JSONObject) {
        val state = jsonObject.getString("state")
        val time = jsonObject.getString("time")
        val identity = jsonObject.getString("identity")
        val deviceState = DeviceState(0, state, time, identity)

        Thread {
            run {
                dao.insert(deviceState)
            }
        }.start()
        dataList.add(deviceState)

        activity.runOnUiThread {
            mAdapter.notifyItemInserted(dataList.size-1)
        }

        if (identity != "none") {
            notificationHandler.send("於$time 開啟裝置的人是$identity。")
        }
        else {
            notificationHandler.send("於$time 遭開啟的裝置無人認領。")
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
