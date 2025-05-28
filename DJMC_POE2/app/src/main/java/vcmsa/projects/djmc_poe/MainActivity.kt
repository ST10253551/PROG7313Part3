package vcmsa.projects.djmc_poe

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

// AddCategory.kt
class MainActivity : AppCompatActivity() {
    private lateinit var titleInput: EditText
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        titleInput = findViewById(R.id.categoryNameInput)
        val saveButton: Button = findViewById(R.id.saveCategoryButton)

        saveButton.setOnClickListener {
            val title = titleInput.text.toString()
            val category = Category(title)

            db.collection("categories")
                .add(category)
                .addOnSuccessListener { finish() }
                .addOnFailureListener { Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show() }
        }
    }
}