package vcmsa.projects.djmc_poe

import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Link to your layout file

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        // Grab references to UI components
        val usernameEditText = findViewById<EditText>(R.id.etUsername)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val signupButton = findViewById<Button>(R.id.signupBtn)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = hashMapOf(
                "username" to username,
                "password" to password
            )

            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("FIRESTORE", "User added with ID: ${documentReference.id}")
                    Toast.makeText(this, "User saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("FIRESTORE", "Error adding user", e)
                    Toast.makeText(this, "Error saving user", Toast.LENGTH_SHORT).show()
                }
        }

        signupButton.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

        }

    }
}
