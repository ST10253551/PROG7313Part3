package vcmsa.projects.djmc_poe

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

// Display_Category.kt
class Display_Category : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var categoryList: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_category)

        categoryList = findViewById(R.id.CategoryRecyclerView)

        db.collection("categories").get()
            .addOnSuccessListener { result ->
                val stringBuilder = StringBuilder()
                for (doc in result) {
                    val category = doc.toObject(Category::class.java)
                    stringBuilder.append("${category.categoryName}\n")
                }
                categoryList.text = stringBuilder.toString()
            }
    }
}