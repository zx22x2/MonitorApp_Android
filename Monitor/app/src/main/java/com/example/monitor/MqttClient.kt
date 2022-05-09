package com.example.monitor

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

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

    @Throws(MqttException::class)
    fun publish(client: MqttAndroidClient, topic: String, payload: ByteArray) {
        val token = client.publish(topic, payload, 2, false)
    }
}

class MqttMessageService(val context: Context, val messageManager: MessageManager) : Service() {

    private var mqttBrokerUrl = "tcp://10.0.2.2:1883"
    var mqttClient: MqttClient? = null
    var mqttAndroidClient: MqttAndroidClient? = null

    private val tag = "MqttMessageService"

    fun connect() {
        mqttClient = MqttClient()
        //val mDeviceID = Settings.System.getString(contentResolver, Settings.System.ANDROID_ID)
        mqttAndroidClient = mqttClient!!.getMqttClient(context, mqttBrokerUrl, "client1")

        mqttAndroidClient!!.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                Log.d(tag, "connectionLost")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                Log.d(tag, "messageArrived")
                Log.d(tag, "Topic : $topic, Content : $message")
                messageManager.add(topic, String(message.payload))
                //send notification
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.d(tag, "deliveryComplete")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                Log.d(tag, "connectComplete $serverURI")
                try {
                    mqttClient!!.subscribe(mqttAndroidClient!!, "monitor/door/state_change", 2)
                    mqttClient!!.subscribe(mqttAndroidClient!!, "monitor/door/log", 2)
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