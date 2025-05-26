package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DebtDetailsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_details)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Not authenticated. Please log in again.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = currentUser.uid

        val debtnameEditText = findViewById<EditText>(R.id.etDebtname)
        val debtamountEditText = findViewById<EditText>(R.id.etDebtAmount)
        val monthsperiodNumberPicker = findViewById<NumberPicker>(R.id.npMonthsPeriod)
        val interestAmount = findViewById<EditText>(R.id.etInterestAmount)
        val backButton = findViewById<Button>(R.id.backBtn)
        val createButton = findViewById<Button>(R.id.createBtn)

        monthsperiodNumberPicker.minValue = 1
        monthsperiodNumberPicker.maxValue = 60
        monthsperiodNumberPicker.wrapSelectorWheel = true

        val debtCategorySpinner = findViewById<Spinner>(R.id.spDebtCategory)
        val categoryList = mutableListOf<String>()

        // Fetch categories for this specific user
        db.collection("users").document(userId).collection("debtcategorynames")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val categoryName = document.getString("debtcategoryname")
                    if (!categoryName.isNullOrEmpty()) {
                        categoryList.add(categoryName)
                    }
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                debtCategorySpinner.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w("FIRESTORE", "Error getting categories: ", exception)
                Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }

        createButton.setOnClickListener {
            val debtname = debtnameEditText.text.toString().trim()
            val debtamtText = debtamountEditText.text.toString().trim()
            val interestamtText = interestAmount.text.toString().trim()
            val npmonthsperiod = monthsperiodNumberPicker.value
            val selectedCategory = debtCategorySpinner.selectedItem?.toString() ?: ""

            if (debtname.isEmpty() || debtamtText.isEmpty() || interestamtText.isEmpty() || selectedCategory.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val debtamt = debtamtText.toDoubleOrNull()
            val interestamt = interestamtText.toDoubleOrNull()

            if (debtamt == null || interestamt == null) {
                Toast.makeText(this, "Debt and interest must be valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val debt = hashMapOf(
                "debtname" to debtname,
                "debtamt" to debtamt,
                "interestamt" to interestamt,
                "npmonthsperiod" to npmonthsperiod,
                "category" to selectedCategory
            )

            // Save under /users/{userId}/debtdetails/
            db.collection("users").document(userId).collection("debtdetails")
                .add(debt)
                .addOnSuccessListener { documentReference ->
                    Log.d("FIRESTORE", "Debt successfully saved: ${documentReference.id}")
                    Toast.makeText(this, "Debt saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("FIRESTORE", "Error adding debt", e)
                    Toast.makeText(this, "Error saving debt", Toast.LENGTH_SHORT).show()
                }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java))
            finish()
        }
    }
}


//package vcmsa.projects.djmc_poe
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import android.widget.Spinner
//import android.widget.ArrayAdapter
//import android.widget.NumberPicker
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.FirebaseApp
//import com.google.firebase.firestore.FirebaseFirestore
//
//class DebtDetailsActivity : AppCompatActivity() {
//
//    private lateinit var db: FirebaseFirestore
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_debt_details)
//
//        FirebaseApp.initializeApp(this)
//        db = FirebaseFirestore.getInstance()
//
//        // Grab references to UI components
//        val debtnameEditText = findViewById<EditText>(R.id.etDebtname)
//        val debtamountEditText = findViewById<EditText>(R.id.etDebtAmount)
//        val monthsperiodNumberPicker = findViewById<NumberPicker>(R.id.npMonthsPeriod)
//        val interestAmount = findViewById<EditText>(R.id.etInterestAmount)
//        val BackButton = findViewById<Button>(R.id.backBtn)
//        val CreateButton = findViewById<Button>(R.id.createBtn)
//
//        monthsperiodNumberPicker.minValue = 1
//        monthsperiodNumberPicker.maxValue = 60
//        monthsperiodNumberPicker.wrapSelectorWheel = true
//
//        // displaying categories
//
//        val debtCategorySpinner = findViewById<Spinner>(R.id.spDebtCategory)
//        val categoryList = mutableListOf<String>()
//
//// Fetch categories from Firestore and populate spinner
//        db.collection("debtcategorynames")
//            .get()
//            .addOnSuccessListener { result ->
//                for (document in result) {
//                    val categoryName = document.getString("debtcategoryname")
//                    if (!categoryName.isNullOrEmpty()) {
//                        categoryList.add(categoryName)
//                    }
//                }
//
//                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                debtCategorySpinner.adapter = adapter
//            }
//            .addOnFailureListener { exception ->
//                Log.w("FIRESTORE", "Error getting categories: ", exception)
//                Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
//            }
//
//
//        CreateButton.setOnClickListener {
//            val debtname = debtnameEditText.text.toString().trim()
//            val debtamtText = debtamountEditText.text.toString().trim()
//            val interestamtText = interestAmount.text.toString().trim()
//            val npmonthsperiod = monthsperiodNumberPicker.value
//
//
//            // Validation
//            if (debtname.isEmpty() || debtamtText.isEmpty() || interestamtText.isEmpty()) {
//                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val debtamt = debtamtText.toDoubleOrNull()
//            val interestamt = interestamtText.toDoubleOrNull()
//
//            if (debtamt == null || interestamt == null) {
//                Toast.makeText(this, "Debt and interest must be valid numbers", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val debt = hashMapOf(
//                "debtname" to debtname,
//                "debtamt" to debtamt,
//                "interestamt" to interestamt,
//                "npmonthsperiod" to npmonthsperiod
//            )
//
//            db.collection("debtdetails")
//                .add(debt)
//                .addOnSuccessListener { documentReference ->
//                    Log.d("FIRESTORE", "Debt was successfully saved: ${documentReference.id}")
//                    Toast.makeText(this, "Debt saved!", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener { e ->
//                    Log.w("FIRESTORE", "Error adding debt", e)
//                    Toast.makeText(this, "Error saving debt", Toast.LENGTH_SHORT).show()
//                }
//        }
//
//        BackButton.setOnClickListener {
//            val intent = Intent(this, DebtTrackingActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//    }
//}
