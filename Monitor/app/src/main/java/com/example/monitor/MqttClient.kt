package com.example.monitor

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONException
import org.json.JSONObject

class MqttClient {
    private val tag = "PahoMqttClient"

    private fun getMqttConnectionOptions(): MqttConnectOptions {
        val mqttConnectOptions = MqttConnectOptions()

        mqttConnectOptions.connectionTimeout = 30
        mqttConnectOptions.keepAliveInterval = 120
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = true

        return mqttConnectOptions
    }

    private fun getDisconnectedBufferOptions(): DisconnectedBufferOptions {
        val disconnectedBufferOptions = DisconnectedBufferOptions()

        disconnectedBufferOptions.isBufferEnabled = true
        disconnectedBufferOptions.bufferSize = 100
        disconnectedBufferOptions.isPersistBuffer = false
        disconnectedBufferOptions.isDeleteOldestMessages = false

        return disconnectedBufferOptions
    }

    fun getMqttClient(context: Context, serverUri: String, clientId: String): MqttAndroidClient {
        val mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
        try {
            val token = mqttAndroidClient.connect(getMqttConnectionOptions())
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions())
                    Log.d(tag, "Success")
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(tag, "Failure : $exception")
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        return mqttAndroidClient
    }

    @Throws(MqttException::class)
    fun subscribe(client: MqttAndroidClient, topic: String, qos: Int) {
        val token = client.subscribe(topic, qos)
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d(tag, "Subscribe Successfully : $topic")
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.d(tag, "Subscribe Failed : $topic")
            }
        }
    }
}

class MqttMessageService : Service() {

    private var mqttBrokerUrl = "tcp://10.0.2.2:1883"
    var mMqttClient: MqttClient? = null
    var mqttClient: MqttAndroidClient? = null

    private val tag = "MqttMessageService"

    fun connect(context: Context, messageManager: MessageManager) {
        mMqttClient = MqttClient()
        //val mDeviceID = Settings.System.getString(contentResolver, Settings.System.ANDROID_ID)
        mqttClient = mMqttClient!!.getMqttClient(context, mqttBrokerUrl, "client1")

        mqttClient!!.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                Log.d(tag, "connectionLost")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                Log.d(tag, "messageArrived")
                Log.d(tag, "Topic : $topic, Content : $message")
                messageManager.add(String(message.payload))
                //send notification
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.d(tag, "deliveryComplete")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                Log.d(tag, "connectComplete $serverURI")
                try {
                    mMqttClient!!.subscribe(mqttClient!!, "MQTT_Test", 2)
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}