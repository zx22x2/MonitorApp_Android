package com.example.monitor

import android.app.DownloadManager
import android.content.ContextParams
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Registration : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
    }

    fun toLogin(view: android.view.View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    fun register(view: android.view.View) {
        val username: EditText = findViewById(R.id.register_username)
        val password: EditText = findViewById(R.id.register_password)
        val info: TextView = findViewById(R.id.info)

        val queue = Volley.newRequestQueue(this)
        val url = "http://hi87.atwebpages.com/register.php"

        if(username.text.trim().isNotEmpty() && password.text.isNotEmpty()) {
            val registerRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    info.visibility = View.VISIBLE
                    info.text = response
                }, Response.ErrorListener { error ->
                    info.visibility = View.VISIBLE
                    info.text = error.toString()
                }) {

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("username", username.text.trim().toString())
                    params.put("password", password.text.toString())

                    return params
                }
            }
            queue.add(registerRequest)
        }
        else {
            info.visibility = View.VISIBLE
            info.text = "帳號和密碼不可空白"
        }

    }

}


