package com.example.messengerapp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TableLayout
import androidx.appcompat.widget.DialogTitle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.messengerapp.Fragments.ChatsFragment
import com.example.messengerapp.Fragments.SearchFragment
import com.example.messengerapp.Fragments.SettingsFragment
import com.example.messengerapp.ModelClasses.Chat
import com.example.messengerapp.ModelClasses.ChatList
import com.example.messengerapp.ModelClasses.Users
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList
import com.google.firebase.database.DatabaseError
import androidx.annotation.NonNull
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.example.messengerapp.VideoCall.VideoCallActivity


class MainActivity : AppCompatActivity() {

    var refUsers: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    private var  calledBy: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)


        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)




        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""


        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        //val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        //viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
        //viewPagerAdapter.addFragment(SearchFragment(), "Search")
        //viewPagerAdapter.addFragment(SettingsFragment(), "Settings")

        //viewPager.adapter = viewPagerAdapter
        //tabLayout.setupWithViewPager(viewPager)

        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref!!.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var countUnreadMessages = 0

                for (dataSnapshot in p0.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if(chat!!.getReceiver().equals(firebaseUser!!.uid) && !chat.getIsseen()){
                        countUnreadMessages += 1
                    }
                }

                if(countUnreadMessages == 0){
                    viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
                }
                else{
                    viewPagerAdapter.addFragment(ChatsFragment(), "($countUnreadMessages) Chats")
                }
                viewPagerAdapter.addFragment(SearchFragment(), "Search")
                viewPagerAdapter.addFragment(SettingsFragment(), "Settings")
                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })


        // display username and profile picture
        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val user: Users? = p0.getValue(Users::class.java)

                    user_name.text = user!!.getUserName()
                    Picasso.get().load(user.getProfile()).placeholder(R.drawable.ic_person_white_24dp).into(profile_image)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


    }

    override fun onStart() {
        super.onStart()



        // Checking for receiving video calls or not
        refUsers!!.child("Ringing").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    if(p0.hasChild("ringing")){

                        calledBy = p0.child("ringing").value!!.toString()

                        val intent = Intent(this@MainActivity, VideoCallActivity::class.java)
                        intent.putExtra("visit_id",calledBy)
                        startActivity(intent)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


        //if(dataSnapshot.child(senderUserId).hasChild("Ringing")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity,WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                return true
            }
        }
        return false
    }


    internal class ViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager)
    {
        private val fragments: ArrayList<Fragment>
        private val titles: ArrayList<String>

        init {
            fragments = ArrayList<Fragment>()
            titles = ArrayList<String>()
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
           return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String)
        {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return  titles[position]
        }

    }

    private fun updateStatus(status: String){
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status

        ref.updateChildren(hashMap)

    }

    override fun onResume() {
        super.onResume()

        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()

        updateStatus("offline")
    }
}
