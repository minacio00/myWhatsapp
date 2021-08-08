package com.example.mywhatsapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // TODO: implementar login, como no método de cadastro da main activity
        // enxaguar o cabelo
        auth = Firebase.auth

        val loginBtn = findViewById<Button>(R.id.login_btn)
        loginBtn.setOnClickListener{
            val email = findViewById<EditText>(R.id.email_login)
            val senha = findViewById<EditText>(R.id.senha_login)
            if (email.text.isEmpty() || senha.text.isEmpty()){
                Toast.makeText(baseContext, "Preencha email e senha",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.text.toString().trim(),senha.text.toString().trim())
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("mainA", "Login:success")
                        Toast.makeText(baseContext, "Logado com sucesso.",
                            Toast.LENGTH_SHORT).show()
                        val user = auth.currentUser
                        val intent = Intent(this,LatestsMessagesActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        return@addOnCompleteListener

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("mainA", "login:failure", task.exception)
//                        Log.d("mainA","email: ${email.toString().trim()}")
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()

                    }
                }
        }


    }
}