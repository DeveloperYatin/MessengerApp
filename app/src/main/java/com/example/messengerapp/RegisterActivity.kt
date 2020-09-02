package com.example.messengerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {


    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegisterActivity,WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        mAuth = FirebaseAuth.getInstance()

        register_btn.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val userName: String = username_register.text.toString()
        val email: String = email_register.text.toString()
        val password: String = password_register.text.toString()

        if(userName == ""){
            Toast.makeText(this@RegisterActivity, "Please write User Name", Toast.LENGTH_LONG).show()
        }
        else if (email == ""){
            Toast.makeText(this@RegisterActivity, "Please write Email", Toast.LENGTH_LONG).show()
        }
        else if (password == ""){
            Toast.makeText(this@RegisterActivity, "Please write Password", Toast.LENGTH_LONG).show()
        }
        else{

            mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        //send verification link

                        mAuth.currentUser!!.sendEmailVerification().addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Toast.makeText(this@RegisterActivity, "Verification mail sent to your e mail, check your mail for verification instructions", Toast.LENGTH_LONG).show()

                                firebaseUserID = mAuth.currentUser!!.uid

                                refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                                val userHashmap = HashMap<String, Any>()
                                userHashmap["uid"] = firebaseUserID
                                userHashmap["username"] = userName
                                userHashmap["profile"] = "https://firebasestorage.googleapis.com/v0/b/messenger-app-9209b.appspot.com/o/profile.png?alt=media&token=9e3d4057-f6b9-4671-b61f-120352b4a873"
                                userHashmap["cover"] = "https://firebasestorage.googleapis.com/v0/b/messenger-app-9209b.appspot.com/o/cover.jpg?alt=media&token=dcdd1ed0-77ad-4504-8d5a-d5250079c751"
                                userHashmap["status"] = "offline"
                                userHashmap["search"] = userName.toLowerCase()
                                //userHashmap["facebook"] = "https://m.facebook.com"
                                //userHashmap["instagram"] = "https://m.instagram.com"
                                //userHashmap["website"] = "https://www.google.com"

                                refUsers.updateChildren(userHashmap)
                                    .addOnCompleteListener { task ->
                                        if(task.isSuccessful){
                                           // Toast.makeText(this@RegisterActivity, "UserCreated", Toast.LENGTH_LONG).show()

                                            // val intent = Intent(this@RegisterActivity,MainActivity::class.java)
                                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                            //startActivity(intent)
                                            //finish()
                                        }
                                    }

                            }
                            else{
                                Toast.makeText(this@RegisterActivity, "Error Message: " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                            }
                        }

                    }
                    else{
                        Toast.makeText(this@RegisterActivity, "Error Message: " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
