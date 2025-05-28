package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class PaymentEntryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_entry)

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

        val backButton = findViewById<Button>(R.id.backBtn)
        val paymentEntryButton = findViewById<Button>(R.id.PaymentEntrybtn)
        val paymentAmountInput = findViewById<EditText>(R.id.editTextNumber)

        val debtId = intent.getStringExtra("debtId")

        backButton.setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java))
            finish()
        }

        paymentEntryButton.setOnClickListener {
            val paymentAmount = paymentAmountInput.text.toString().toDoubleOrNull()

            if (paymentAmount == null || paymentAmount <= 0) {
                Toast.makeText(this, "Enter a valid payment amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (debtId == null) {
                Toast.makeText(this, "Debt not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Step 1: Get the debt document
            val debtRef = db.collection("users").document(userId)
                .collection("debtdetails").document(debtId)

            debtRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentDebtAmount = documentSnapshot.getDouble("debtamt") ?: 0.0
                    val newDebtAmount = currentDebtAmount - paymentAmount

                    // Step 2: Update debtamt in Firestore
                    debtRef.update("debtamt", newDebtAmount)
                        .addOnSuccessListener {
                            // Step 3: Save payment record
                            val payment = hashMapOf(
                                "amount" to paymentAmount,
                                "timestamp" to Date(),
                                "debtId" to debtId
                            )

                            db.collection("users")
                                .document(userId)
                                .collection("payments")
                                .add(payment)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Payment saved and debt updated!", Toast.LENGTH_SHORT).show()
                                    paymentAmountInput.text.clear()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Payment saved, but failed to log payment: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update debt: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Debt record not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch debt: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}


//package vcmsa.projects.djmc_poe
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.FirebaseApp
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import java.util.*
//
//class PaymentEntryActivity : AppCompatActivity() {
//
//    private lateinit var db: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_payment_entry)
//
//        FirebaseApp.initializeApp(this)
//        db = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()
//
//        val currentUser = auth.currentUser
//        if (currentUser == null) {
//            Toast.makeText(this, "Not authenticated. Please log in again.", Toast.LENGTH_LONG).show()
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//            return
//        }
//
//        val userId = currentUser.uid
//
//        val backButton = findViewById<Button>(R.id.backBtn)
//        val paymentEntryButton = findViewById<Button>(R.id.PaymentEntrybtn)
//        val paymentAmountInput = findViewById<EditText>(R.id.editTextNumber)
//
//        val debtId = intent.getStringExtra("debtId")
//        val debtName = intent.getStringExtra("debtName")
//        val debtAmount = intent.getDoubleExtra("debtAmount", 0.0)
//
//        backButton.setOnClickListener {
//            startActivity(Intent(this, DebtTrackingActivity::class.java))
//            finish()
//        }
//
//        paymentEntryButton.setOnClickListener {
//            val paymentAmount = paymentAmountInput.text.toString().toDoubleOrNull()
//
//            if (paymentAmount == null || paymentAmount <= 0) {
//                Toast.makeText(this, "Enter a valid payment amount", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (debtId == null) {
//                Toast.makeText(this, "Debt not found", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // Payment data
//            val payment = hashMapOf(
//                "amount" to paymentAmount,
//                "timestamp" to Date(),
//                "debtId" to debtId,
//                "debtName" to debtName,
//                "debtAmount" to debtAmount
//            )
//
//            // Save under the authenticated user's payments collection
//            db.collection("users")
//                .document(userId)
//                .collection("payments")
//                .add(payment)
//                .addOnSuccessListener {
//                    Toast.makeText(this, "Payment saved!", Toast.LENGTH_SHORT).show()
//                    paymentAmountInput.text.clear()
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(this, "Failed to save payment: ${e.message}", Toast.LENGTH_LONG).show()
//                    e.printStackTrace()
//                }
//        }
//    }
//}
//
//
////package vcmsa.projects.djmc_poe
////
////import android.content.Intent
////import android.os.Bundle
////import android.widget.Button
////import android.widget.EditText
////import android.widget.Toast
////import androidx.appcompat.app.AppCompatActivity
////import com.google.firebase.FirebaseApp
////import com.google.firebase.auth.FirebaseAuth
////import com.google.firebase.firestore.FirebaseFirestore
////import java.util.*
////
////class PaymentEntryActivity : AppCompatActivity() {
////
////    private lateinit var db: FirebaseFirestore
////    private lateinit var auth: FirebaseAuth
////
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_payment_entry)
////
////        FirebaseApp.initializeApp(this)
////        db = FirebaseFirestore.getInstance()
////        auth = FirebaseAuth.getInstance()
////
////        val currentUser = auth.currentUser
////        if (currentUser == null) {
////            Toast.makeText(this, "Not authenticated. Please log in again.", Toast.LENGTH_LONG)
////                .show()
////            startActivity(Intent(this, LoginActivity::class.java))
////            finish()
////            return
////        }
////
////        val userId = currentUser.uid
////
////        db = FirebaseFirestore.getInstance()
////
////        val backbutton = findViewById<Button>(R.id.backBtn)
////        val paymentEntryButton = findViewById<Button>(R.id.PaymentEntrybtn)
////        val paymentAmountInput = findViewById<EditText>(R.id.editTextNumber)
////
////
////
////        val debtId = intent.getStringExtra("debtId")
////
////        backbutton.setOnClickListener {
////            val intent = Intent(this, DebtTrackingActivity::class.java)
////            startActivity(intent)
////            finish()
////        }
////
////        paymentEntryButton.setOnClickListener {
////            val paymentAmount = paymentAmountInput.text.toString().toDoubleOrNull()
////
////            if (paymentAmount == null || paymentAmount <= 0) {
////                Toast.makeText(this, "Enter a valid payment amount", Toast.LENGTH_SHORT).show()
////                return@setOnClickListener
////            }
////
////            if (debtId == null) {
////                Toast.makeText(this, "Debt not found", Toast.LENGTH_SHORT).show()
////                return@setOnClickListener
////            }
////
////            // Create payment data
////            val payment = hashMapOf(
////                "amount" to paymentAmount,
////                "timestamp" to Date()
////            )
////
////            // Save under the debt's payments subcollection
////            db.collection("users")
////                .document(debtId)
////                .collection("payments")
////                .add(payment)
////                .addOnSuccessListener {
////                    Toast.makeText(this, "Payment saved!", Toast.LENGTH_SHORT).show()
////                    paymentAmountInput.text.clear()
////                }
////                .addOnFailureListener { e ->
////                    Toast.makeText(this, "Failed to save payment: ${e.message}", Toast.LENGTH_LONG).show()
////                    e.printStackTrace()
////                }
////        }
////    }
////}
