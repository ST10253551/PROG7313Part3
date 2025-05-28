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
        val expenseButton = findViewById<Button>(R.id.expenseButton)


        categoryButton.setOnClickListener {
            startActivity(Intent(this, DebtCategoryActivity::class.java))
            finish()
        }

        detailButton.setOnClickListener {
            startActivity(Intent(this, DebtDetailsActivity::class.java))
            finish()
        }
    }

    private fun fetchDebtData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

