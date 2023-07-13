package com.example.lvdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RegActivity : AppCompatActivity() {
    private lateinit var textEmail : EditText
    private lateinit var textPassword : EditText
    private lateinit var userEmail : TextView
    private var userKey : String = "User"

    private  var verification : Boolean = false

    private lateinit var bStart : Button
    private lateinit var bExit : Button
    private lateinit var bSignIn : Button
    private lateinit var bSignUp : Button

    private lateinit var mutableList : MutableList<String>

    private lateinit var mDataBase : DatabaseReference
    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestedOrientation =  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_reg)
        init()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser

        if(currentUser != null)
        {
            verification = false
            showSigned()
        }
        else
        {
            showNotSigned()
            verification = true
        }
    }

    private fun init()
    {
        textEmail = findViewById(R.id.emailText)
        textPassword = findViewById(R.id.passwordText)
        mDataBase = FirebaseDatabase.getInstance().getReference(userKey)
        mAuth = FirebaseAuth.getInstance()

        bSignIn = findViewById(R.id.bSignIn)
        bSignUp = findViewById(R.id.bSignUp)
        bStart = findViewById(R.id.bStart)
        bExit = findViewById(R.id.bExit)
        userEmail = findViewById(R.id.UserEmail)

    }

    private fun getDataFromDB()//чтение данных из БД
    {
        val vListener = object : ValueEventListener { //переопределение методов для одной нашей переменной
            override fun onDataChange(snapshot: DataSnapshot)
            {
                if(mutableList.size > 0)
                {
                    mutableList.clear()
                }

                for(userSnapshot in snapshot.children){
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        mutableList.add(user.email)
                    }
                }
            }

            override fun onCancelled(error : DatabaseError) {}
        }


        mDataBase.addValueEventListener(vListener)
    }

    fun onClickReg(view: View)
    {
        val email = textEmail.text.toString()
        val password = textPassword.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()) {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        sendEmailVer()
                        verification = true
                    } else {
                        showNotSigned()
                    }
                }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showSigned()
                    val id = mDataBase.key.toString()
                    val newUser = User(email,password,id)

                    mDataBase.push().setValue(newUser)

                    //Toast.makeText(this, "User SingIn successful",Toast.LENGTH_SHORT).show()
                } else {
                    showNotSigned()
                    //Toast.makeText(this, "User SignIn failed",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onClickIn(view: View)
    {
        val email = textEmail.text.toString()
        val password = textPassword.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()) {
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this){ task->
                if (task.isSuccessful)
                {
                    showSigned()
                    //Toast.makeText(this, "User SingIn successful",Toast.LENGTH_SHORT).show()
                }
                else
                {
                    showNotSigned()
                    //Toast.makeText(this, "User SignIn failed",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onClickSignOut(view: View)
    {
        FirebaseAuth.getInstance().signOut()
        showNotSigned()
    }

    private fun showSigned()
    {
        val currentUser = mAuth.currentUser
        if(currentUser != null && currentUser.isEmailVerified) {

            val email : String = "Вы вошли как: "+ currentUser.email
            userEmail.text = email

            bStart.visibility = View.VISIBLE
            bExit.visibility = View.VISIBLE
            userEmail.visibility = View.VISIBLE

            bSignUp.visibility = View.GONE
            bSignIn.visibility = View.GONE
            textPassword.visibility = View.GONE
            textEmail.visibility = View.GONE
        }
    }

    private fun showNotSigned()
    {
        bSignUp.visibility = View.VISIBLE
        bSignIn.visibility = View.VISIBLE
        textPassword.visibility = View.VISIBLE
        textEmail.visibility = View.VISIBLE

        bStart.visibility = View.GONE
        bExit.visibility = View.GONE
        userEmail.visibility = View.GONE
    }

    fun onClickStart(view: View)
    {
       /* if(verification) {
            val id = mDataBase.key.toString()
            val email = textEmail.text.toString()
            val password = textPassword.text.toString()
            val newUser = User(email,password,id)

            mDataBase.push().setValue(newUser)
        }*/

        val intentReg = Intent(this, MapsActivity::class.java)
        startActivity(intentReg)
    }

    private fun sendEmailVer()
    {
        mAuth.currentUser!!.sendEmailVerification().addOnCompleteListener(this){task ->
            if(task.isSuccessful)
            {
                Toast.makeText(this, "Проверьте вашу почту для подтверждения Email",Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(this, "Send Email failed",Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        return
    }
}