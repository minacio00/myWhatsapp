package com.example.mywhatsapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.net.URI
import java.util.*


class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    lateinit var auth: FirebaseAuth
    var selectedUri :Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        mAuth = FirebaseAuth.getInstance()
        val email = findViewById<EditText>(R.id.email_reg)
        val senha = findViewById<EditText>(R.id.senha_reg)

        val btn = findViewById<Button>(R.id.registerbtn)
        btn.setOnClickListener{
            Log.d("Cadastro", "email: ${email.text.toString()}")
            Log.d("Cadastro", "senha: ${senha.text.toString()}")

            if (email.text.isEmpty() || senha.text.isEmpty()){
                Toast.makeText(baseContext, "Preencha email e senha",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.text.toString().trim(),senha.text.toString().trim())
                .addOnCompleteListener(this){ task->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("mainA", "createUserWithEmail:success")
                        val user = auth.currentUser
                        uploadImageFirebase()
                        return@addOnCompleteListener

                    } else {
                        Log.w("Cadastro", "createUserWithEmail:failure", task.exception)
                        Log.d("Cadastro","email: ${email.toString().trim()}")
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()

                    }
                }
        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                // There are no request codes
                val data: Intent? = result.data
                Log.d("Cadastro","imagem selecionada")
//                val uri: Uri = Uri.parse(data?.extras.toString())
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,data?.data) //data?.extras?.get("data") as Bitmap
                selectedUri = data?.data
                val bitmapDrawable = BitmapDrawable(bitmap)
//                findViewById<Button>(R.id.circularImg).setBackgroundDrawable(bitmapDrawable)
                findViewById<CircleImageView>(R.id.circularImg).setImageBitmap(bitmap)
                findViewById<Button>(R.id.img_btn).alpha = 0f
            }
        }

        findViewById<Button>(R.id.img_btn).setOnClickListener{
            Log.d("Cadastro","cheguei aqui")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
//            resultLauncher.launch("image/*")
            resultLauncher.launch(intent)

        }
        val login = findViewById<TextView>(R.id.login_textview)
        login.setOnClickListener{
//            Log.d("mainA","cheguei aqui")
            startActivity( Intent(this,LoginActivity::class.java))

        }

    }

    private fun uploadImageFirebase(){
        if(selectedUri != null){
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

            ref.putFile(selectedUri!!)
                .addOnSuccessListener {
                    Log.d("Cadastro","Upload concluido ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener{
                        val x = ref.downloadUrl
//                        it.toString()
                        Log.d("CadastroActivity","file Location : $it")
                        saveUsertoFirebase(it.toString())
                    }
                }
        }
        else{
            Log.d("Cadastro","imagem não salva")
        }
    }

    private fun saveUsertoFirebase(profileImage: String){
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        val username = findViewById<EditText>(R.id.nome_reg).text.toString()
        val user = User(uid, username,profileImage)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("CadastroActivity","Usuario salvo no firebase")
                val intent = Intent(this,LatestsMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d("CadastroActivity","usuario não foi salvo no firebase")
            }
    }

}
@Parcelize
class User(val uid: String, val username: String, val profileImage: String):Parcelable{
    constructor() : this ("","","")
}