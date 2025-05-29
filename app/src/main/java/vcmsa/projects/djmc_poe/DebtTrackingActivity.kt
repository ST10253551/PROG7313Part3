package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DebtTrackingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var debtAdapter: DebtAdapter
    private val debtList = mutableListOf<Debt>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_tracking)

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up adapter with delete callback
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        val firestore = FirebaseFirestore.getInstance()

        debtAdapter = DebtAdapter(this, debtList) { debt, position ->
            firestore.collection("users")
                .document(uid)
                .collection("debtdetails")
                .document(debt.documentId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Debt deleted", Toast.LENGTH_SHORT).show()
                    debtAdapter.removeDebt(position)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
        }


//        debtAdapter = DebtAdapter(debtList) { debt, position ->
//            firestore.collection("users")
//                .document(uid)
//                .collection("debtdetails")
//                .document(debt.documentId)
//                .delete()
//                .addOnSuccessListener {
//                    Toast.makeText(this, "Debt deleted", Toast.LENGTH_SHORT).show()
//                    debtAdapter.removeDebt(position)
//                }
//                .addOnFailureListener {
//                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
//                }
//        }

        recyclerView.adapter = debtAdapter

        // Fetch debts from Firestore
        fetchDebtData()

        // Button navigation
        val categoryButton = findViewById<Button>(R.id.categoriesBtn)
        val detailButton = findViewById<Button>(R.id.detailBtn)
        val paymenthistorybutton = findViewById<Button>(R.id.paymentHistoryBtn)

        categoryButton.setOnClickListener {
            startActivity(Intent(this, DebtCategoryActivity::class.java))
            finish()
        }

        detailButton.setOnClickListener {
            startActivity(Intent(this, DebtDetailsActivity::class.java))
            finish()
        }

        paymenthistorybutton.setOnClickListener {
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
            finish()
        }
    }

    private fun fetchDebtData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val debtRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("debtdetails")

        debtRef.get()
            .addOnSuccessListener { snapshot ->
                debtList.clear()
                for (doc in snapshot) {
                    val debt = doc.toObject(Debt::class.java).copy(documentId = doc.id)
                    debtList.add(debt)
                }
                debtAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load debt data", Toast.LENGTH_SHORT).show()
            }
    }
}


//package vcmsa.projects.djmc_poe
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//
//class DebtTrackingActivity : AppCompatActivity() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: DebtAdapter
//    private val debtList = mutableListOf<Debt>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_debt_tracking)
//
//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        adapter = DebtAdapter(debtList)
//        recyclerView.adapter = adapter
//
//        fetchDebtData()
//
//        // Button setup
//        val categoryButton = findViewById<Button>(R.id.categoriesBtn)
//        val detailButton = findViewById<Button>(R.id.detailBtn)
//
//        categoryButton.setOnClickListener {
//            val intent = Intent(this, DebtCategoryActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        detailButton.setOnClickListener {
//            val intent = Intent(this, DebtDetailsActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }
//
//    private fun fetchDebtData() {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//
//        val debtRef = FirebaseFirestore.getInstance()
//            .collection("users")
//            .document(userId)
//            .collection("debtdetails")
//
//        debtRef.get()
//            .addOnSuccessListener { snapshot ->
//                debtList.clear()
//                for (doc in snapshot) {
//                    val debt = doc.toObject(Debt::class.java)
//                    debtList.add(debt)
//                }
//                adapter.notifyDataSetChanged()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to load debt data", Toast.LENGTH_SHORT).show()
//            }
//    }
//}
//
//
////package vcmsa.projects.djmc_poe
////
////import android.content.Intent
////import android.os.Bundle
////import android.widget.Button
////import androidx.activity.enableEdgeToEdge
////import androidx.appcompat.app.AppCompatActivity
////import androidx.core.view.ViewCompat
////import androidx.core.view.WindowInsetsCompat
////import com.google.firebase.FirebaseApp
////import com.google.firebase.auth.FirebaseAuth
////import com.google.firebase.firestore.FirebaseFirestore
////
////class DebtTrackingActivity : AppCompatActivity() {
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        enableEdgeToEdge()
////        setContentView(R.layout.activity_debt_tracking)
////
////
////        val userId = FirebaseAuth.getInstance().currentUser?.uid
////        val debtDetailsRef = FirebaseFirestore.getInstance()
////            .collection("users")
////            .document(userId!!)
////            .collection("debtdetails")
////
////
////        val categoryButton = findViewById<Button>(R.id.categoriesBtn)
////        val detailButton = findViewById<Button>(R.id.detailBtn)
////
////        categoryButton.setOnClickListener {
////            val intent = Intent(this, DebtCategoryActivity::class.java)
////            startActivity(intent)
////            finish()
////        }
////
////        detailButton.setOnClickListener {
////            val intent = Intent(this, DebtDetailsActivity::class.java)
////            startActivity(intent)
////            finish()
////        }
////
////    }
////}