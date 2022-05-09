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
                     private val notificationHandler: NotificationHandler) {

    private val tag = "MessageManager"
    private val mqttService = MqttMessageService(applicationContext, this)
    private val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "smart_home").build()
    private val dao = db.getDao()
    private val dataList = dao.getAll()
    val mAdapter = MyAdapter(dataList)

    private var isVerifying = false
    private var isVerify = false


    init {
        mqttService.connect()
    }

    fun add(topic: String, message: String) {
        var jsonObject = JSONObject(message)
        var state = ""
        var time = ""
        var identity = ""

        try {
            state = jsonObject.getString("state")
            time = jsonObject.getString("time")
            identity = jsonObject.getString("identity")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //裝置狀態改變
        if (topic == "monitor/door/state_change") {
            val deviceStateTextView: TextView = activity.findViewById(R.id.device_state)
            val buttonConfirm: Button = activity.findViewById(R.id.buttonConfirm)
            deviceStateTextView.text = state
            //進行認領判斷
            if (state == "開") {
                notificationHandler.send("裝置於$time 被開啟，若是您開啟的請進行認領。")
                buttonConfirm.visibility = View.VISIBLE
                isVerifying = true
                Thread {
                    run {
                        var count = 0
                        while (isVerifying && !isVerify && count < 11) {
                            count++
                            Log.d(tag, "count: $count")
                            Thread.sleep(1000)
                        }
                        if (isVerify) {
                            identity = "ㄐㄐ"
                            isVerify = false
                        }
                        else {
                            if (isVerifying) {
                                isVerifying = false
                            }
                            activity.runOnUiThread {
                                buttonConfirm.visibility = View.GONE
                            }
                        }
                        //傳送資料
                        val topic = "monitor/door/log"
                        val data = "{\"state\": \"$state\", \"time\": \"$time\", \"identity\": \"$identity\"}"
                        val payload = data.toByteArray()
                        mqttService.mqttClient!!.publish(mqttService.mqttAndroidClient!!, topic, payload)
                    }
                }.start()
            }
            else if (state == "關") {
                if (isVerifying) {
                    isVerifying = false
                }
            }
        }
        //記錄資料
        if (topic == "monitor/door/log") {
            state = jsonObject.getString("state")
            time = jsonObject.getString("time")
            identity = jsonObject.getString("identity")

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
    }

    fun verify() {
        isVerify = true
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
