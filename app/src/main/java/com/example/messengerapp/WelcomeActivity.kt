package com.example.messengerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    var firebaseUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        register_welcome_btn.setOnClickListener {
            val intent = Intent(this@WelcomeActivity,RegisterActivity::class.java)
            startActivity(intent)
        }

        login_welcome_btn.setOnClickListener {
            val intent = Intent(this@WelcomeActivity,LoginActivity::class.java)
            startActivity(intent)

        }

        phone_welcome_btn.setOnClickListener {
            val intent = Intent(this@WelcomeActivity,PhoneAuthActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onStart() {
        super.onStart()

        firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser != null){
                val intent = Intent(this@WelcomeActivity,MainActivity::class.java)
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

        }else{
            }
    }
}
