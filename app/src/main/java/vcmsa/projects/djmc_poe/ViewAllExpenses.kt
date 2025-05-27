package vcmsa.projects.djmc_poe

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ViewAllExpenses : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var progressBar: ProgressBar

    private lateinit var etDateFrom: EditText
    private lateinit var etDateTo: EditText
    private lateinit var btnFilterRange: Button
    private lateinit var btnClearFilter: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_expenses)

        recyclerView = findViewById(R.id.recyclerViewExpenses)
        etDateFrom = findViewById(R.id.etDateFrom)
        etDateTo = findViewById(R.id.etDateTo)
        btnFilterRange = findViewById(R.id.btnFilterRange)
        btnClearFilter = findViewById(R.id.btnClearFilter)
        progressBar = ProgressBar(this).apply { visibility = View.GONE } // Or add a progressBar in your layout and findViewById here

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(emptyList())
        recyclerView.adapter = adapter

        // Setup date pickers
        etDateFrom.setOnClickListener { showDatePicker(etDateFrom) }
        etDateTo.setOnClickListener { showDatePicker(etDateTo) }

        btnFilterRange.setOnClickListener {
            val fromDateStr = etDateFrom.text.toString().trim()
            val toDateStr = etDateTo.text.toString().trim()

            if (fromDateStr.isEmpty() || toDateStr.isEmpty()) {
                Toast.makeText(this, "Please select both From and To dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate dates
            try {
                val fromDate = dateFormat.parse(fromDateStr)
                val toDate = dateFormat.parse(toDateStr)
                if (fromDate == null || toDate == null) {
                    Toast.makeText(this, "Invalid dates", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (fromDate.after(toDate)) {
                    Toast.makeText(this, "'From' date cannot be after 'To' date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                loadExpensesFiltered(fromDate, toDate)
            } catch (e: Exception) {
                Toast.makeText(this, "Error parsing dates", Toast.LENGTH_SHORT).show()
            }
        }

        btnClearFilter.setOnClickListener {
            etDateFrom.setText("")
            etDateTo.setText("")
            loadAllExpenses()
        }

        loadAllExpenses()
    }

    private fun showDatePicker(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            targetEditText.setText(dateFormat.format(calendar.time))
        }
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadAllExpenses() {
        progressBar.visibility = View.VISIBLE

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .collection("expenses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val expenses = querySnapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
                adapter.updateList(expenses)
                progressBar.visibility = View.GONE
                if (expenses.isEmpty()) {
                    Toast.makeText(this, "No expenses found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to load expenses", e)
                Toast.makeText(this, "Failed to load expenses: ${e.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun loadExpensesFiltered(fromDate: Date, toDate: Date) {
        progressBar.visibility = View.VISIBLE

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        // Convert dates to timestamps for filtering (start of fromDate, end of toDate)
        val fromTimestamp = fromDate.time
        val toTimestamp = toDate.time + (24 * 60 * 60 * 1000) - 1 // End of day for 'toDate'

        db.collection("users")
            .document(currentUser.uid)
            .collection("expenses")
            .whereGreaterThanOrEqualTo("timestamp", fromTimestamp)
            .whereLessThanOrEqualTo("timestamp", toTimestamp)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val expenses = querySnapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
                adapter.updateList(expenses)
                progressBar.visibility = View.GONE
                if (expenses.isEmpty()) {
                    Toast.makeText(this, "No expenses found for the selected date range", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to load filtered expenses", e)
                Toast.makeText(this, "Failed to load expenses: ${e.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
    }
}
