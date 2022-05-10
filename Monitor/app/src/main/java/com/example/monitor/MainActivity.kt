package com.example.monitor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pref = getSharedPreferences("userdata", MODE_PRIVATE)
        val isLogin = pref.getBoolean("isLogin", false)

        if (isLogin) {
            toContent()
        }
    }

    fun toRegistration(view: android.view.View) {
        val intent = Intent(this, Registration::class.java)
        startActivity(intent)
        this.finish()
    }

    fun login(view: android.view.View) {
        val usernameEditText: EditText = findViewById(R.id.username)
        val passwordEditText: EditText = findViewById(R.id.password)
        val info: TextView = findViewById(R.id.login_info)

        val username = usernameEditText.text.trim().toString()
        val password = passwordEditText.text.trim().toString()

        val queue = Volley.newRequestQueue(this)
        val url = "http://hi87.atwebpages.com/login.php"

        val pref = getSharedPreferences("userdata", MODE_PRIVATE)
        val editor = pref.edit()

        if(username.isNotEmpty() && password.isNotEmpty()) {
            val loginRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    if(response == "登入成功!") {
                        editor.putString("username", username).commit()
                        editor.putBoolean("isLogin", true).commit()
                        toContent()
                    }
                    else {
                        info.visibility = View.VISIBLE
                        info.text = response
                    }
                }, Response.ErrorListener { error ->
                    info.visibility = View.VISIBLE
                    info.text = error.toString()
                }) {

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("username", username)
                    params.put("password", password)

                    return params
                }
            }
            queue.add(loginRequest)
        }
        else {
            info.visibility = View.VISIBLE
            info.text = "帳號和密碼不可空白"
        }

    }

    fun toContent() {
        val intent = Intent(this, Content::class.java)
        startActivity(intent)
        this.finish()
    }
}


