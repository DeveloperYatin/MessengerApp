package com.example.messengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.AdapterClasses.ChatsAdapter
import com.example.messengerapp.Fragments.ApiService
import com.example.messengerapp.ModelClasses.Chat
import com.example.messengerapp.ModelClasses.Users
import com.example.messengerapp.Notifications.*
import com.example.messengerapp.VideoCall.VideoCallActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import kotlinx.android.synthetic.main.fab_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageChatActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chat>? = null
    var  calledBy: String = ""
    lateinit var  recycler_view_chats: RecyclerView
    var reference: DatabaseReference? = null
    var refUsers: DatabaseReference? = null

    var notify = false

    var apiService: ApiService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)


        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""

        //Back on press profile pic or back button on tool bar
        profile_image_mchat.setOnClickListener { 
            onBackPressed()
        }
        backButton.setOnClickListener {
            onBackPressed()
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(ApiService::class.java)


        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManager

        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)

        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val user: Users? = p0.getValue(Users::class.java)

                username_mchat.text = user!!.getUserName()
                Picasso.get().load(user.getProfile()).into(profile_image_mchat)

                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


        //VideoCalling button on toolbar

        Video_call.setOnClickListener {
//            val intent = Intent(this@MessageChatActivity, VideoCallActivity::class.java)
//            intent.putExtra("visit_id",userIdVisit)
//            startActivity(intent)

            Toast.makeText(this@MessageChatActivity, "Video Call button clicked", Toast.LENGTH_LONG).show()
        }

        //Calling button on toolbar

        callingButton.setOnClickListener {

            Toast.makeText(this@MessageChatActivity, "Calling button clicked", Toast.LENGTH_LONG).show()
        }


        //Sending message button

        send_message_btn.setOnClickListener {
            notify = true
            val message = text_message.text.toString()
            if (message == ""){
                Toast.makeText(this@MessageChatActivity, "Please write a Message first!! ", Toast.LENGTH_LONG).show()
            }else{
             sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            text_message.setText("")

        }


        // AttachFiles button

        attach_image_file_btn.setOnClickListener {

            val animationAppear = AnimationUtils.loadAnimation(this@MessageChatActivity,R.anim.translate_appear)
            val animationDisappear = AnimationUtils.loadAnimation(this@MessageChatActivity,R.anim.translate_disappear)

            if(attach_image_file_btn.isChecked){
                fab_1.visibility = View.VISIBLE
                fab_2.visibility = View.VISIBLE
                fab_3.visibility = View.VISIBLE
                fab_1.startAnimation(animationAppear)
                fab_2.startAnimation(animationAppear)
                fab_3.startAnimation(animationAppear)
                fab_1.isClickable = true
                fab_2.isClickable = true
                fab_3.isClickable = true
                attach_image_file_btn.setBackgroundResource(R.drawable.ic_attachment2)
            }
            else{
                fab_1.visibility = View.GONE
                fab_2.visibility = View.GONE
                fab_3.visibility = View.GONE
                fab_1.startAnimation(animationDisappear)
                fab_2.startAnimation(animationDisappear)
                fab_3.startAnimation(animationDisappear)
                fab_1.isClickable = false
                fab_2.isClickable = false
                fab_3.isClickable = false
                attach_image_file_btn.setBackgroundResource(R.drawable.ic_attachment1)
            }

        }

        //File attach buttons

        fab_1.setOnClickListener {
            Toast.makeText(this@MessageChatActivity, "Location button", Toast.LENGTH_SHORT).show()
        }

        fab_2.setOnClickListener {
            sendImage()

            Toast.makeText(this@MessageChatActivity, "Image file attach button", Toast.LENGTH_SHORT).show()
        }

        fab_3.setOnClickListener {
            Toast.makeText(this@MessageChatActivity, "Video file attach button", Toast.LENGTH_SHORT).show()
        }

        seenMessage(userIdVisit)
    }

    private fun sendImage() {
        notify = true
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent,"Pick image"), 438)
    }

    override fun onStart() {
        super.onStart()


        //Checking for receiving video calls or not
        refUsers!!.child("Ringing").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    if(p0.hasChild("ringing")){

                        calledBy = p0.child("ringing").value!!.toString()

                        val intent = Intent(this@MessageChatActivity,VideoCallActivity::class.java)
                        intent.putExtra("visit_id",calledBy)
                        startActivity(intent)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }



    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {

        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val chatsListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)
                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            if(!p0.exists()){
                                chatsListReference.child("id").setValue(userIdVisit)

                            }
                            val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)

                            chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })

                }
            }



        //implement the push notifications using fcm


        val userReference = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)

        userReference.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(Users::class.java)

                if(notify){
                    sendNotification(receiverId, user!!.getUserName(), message)
                }
                notify = false
            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })

    }

    private fun sendNotification(receiverId: String?, userName: String?, message: String) {

        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                for(dataSnapshot in p0.children){
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    val data = Data(firebaseUser!!.uid, R.mipmap.ic_launcher_round, "$userName: $message","New Message", userIdVisit )

                    val sender = Sender(data!!, token!!.getToken().toString())

                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse>{

                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if(response.code() == 200){
                                    if (response.body()!!.success !== 1){
                                        Toast.makeText(this@MessageChatActivity,"Failed Nothing happen.",Toast.LENGTH_LONG).show()

                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                            }


                        })


                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data!!.data != null){

            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Image is uploading, please wait...")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->

                if(task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }

                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->

                if(task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId


                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){

                                progressBar.dismiss()
                                //implement the push notifications using fcm


                                val reference = FirebaseDatabase.getInstance().reference
                                    .child("Users").child(firebaseUser!!.uid)

                                reference.addValueEventListener(object : ValueEventListener{

                                    override fun onDataChange(p0: DataSnapshot) {
                                        val user = p0.getValue(Users::class.java)

                                        if(notify){
                                            sendNotification(userIdVisit, user!!.getUserName(), "sent you an image.")
                                        }
                                        notify = false
                                    }

                                    override fun onCancelled(p0: DatabaseError) {

                                    }


                                })
                        } }


                }

            }
        }
    }
    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String?) {

        mChatList = ArrayList()
        var reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()

                for (snapshot in p0.children){
                    val chat = snapshot.getValue(Chat::class.java)

                    if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                        || chat.getReceiver().equals(receiverId)&& chat.getSender().equals(senderId))
                    {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatsAdapter(this@MessageChatActivity, (mChatList as ArrayList<Chat>), receiverImageUrl!!)
                    recycler_view_chats.adapter = chatsAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
    var seenListener:  ValueEventListener? = null

    private fun seenMessage(userId: String){
        var reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = reference!!.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId)){
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListener!!)
    }

}
