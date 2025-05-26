package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Link to your layout file

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

            // Firestore query to check for matching user
            db.collection("users")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // User exists with matching username & password
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        Log.d("FIRESTORE", "User found: ${documents.documents[0].id}")
                        // TODO: Navigate to next screen or activity here

                        val intent = Intent(this, DebtTrackingActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("FIRESTORE", "Error checking user", e)
                    Toast.makeText(this, "Error logging in", Toast.LENGTH_SHORT).show()
                }
        }

        signupButton.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }

    }
}
