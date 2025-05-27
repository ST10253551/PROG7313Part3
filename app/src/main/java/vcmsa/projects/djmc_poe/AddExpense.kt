package vcmsa.projects.djmc_poe

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpense : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var expenseTitle: EditText
    private lateinit var expenseAmount: EditText
    private lateinit var expenseDate: EditText
    private lateinit var expenseDescription: EditText
    private lateinit var expenseCategory: Spinner
    private lateinit var expenseImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnCaptureImage: Button
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    private var selectedImageUri: Uri? = null

    private val IMAGE_PICK_CODE = 101
    private val IMAGE_CAPTURE_CODE = 102

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        expenseTitle = findViewById(R.id.Expense)
        expenseAmount = findViewById(R.id.ExpenseAmount)
        expenseDate = findViewById(R.id.Date)
        expenseDescription = findViewById(R.id.ExpenseDescription)
        expenseCategory = findViewById(R.id.ExpenseCategory)
        expenseImage = findViewById(R.id.ExpenseImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnCaptureImage = findViewById(R.id.btnCaptureImage)
        btnSave = findViewById(R.id.buttonsave)
        btnBack = findViewById(R.id.BackToReport)

        val categories = listOf("Food", "Fuel", "Transport", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        expenseCategory.adapter = adapter

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnCaptureImage.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
        }

        expenseDate.setOnClickListener {
            showDatePicker()
        }

        btnSave.setOnClickListener {
            saveExpense()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            expenseDate.setText(format.format(calendar.time))
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveExpense() {
        val title = expenseTitle.text.toString().trim()
        val amountText = expenseAmount.text.toString().trim()
        val date = expenseDate.text.toString().trim()
        val description = expenseDescription.text.toString().trim()
        val category = expenseCategory.selectedItem.toString()

        if (title.isEmpty() || amountText.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        // If image is selected, upload it and include imageUrl
        if (selectedImageUri != null) {
            val uploadUri = copyUriToTempFile(selectedImageUri!!) ?: selectedImageUri!!
            val imageRef = storage.reference
                .child("expenseImages/${user.uid}/${UUID.randomUUID()}.jpg")

            imageRef.putFile(uploadUri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveExpenseToFirestore(title, amount, date, description, category, downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("StorageError", "Image upload failed", e)
                    Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // No image selected, save without imageUrl
            saveExpenseToFirestore(title, amount, date, description, category, null)
        }
    }

    private fun saveExpenseToFirestore(
        title: String,
        amount: Double,
        date: String,
        description: String,
        category: String,
        imageUrl: String?
    ) {
        val user = auth.currentUser ?: return
        val expense = hashMapOf(
            "title" to title,
            "amount" to amount,
            "date" to date,
            "description" to description,
            "category" to category,
            "timestamp" to System.currentTimeMillis()
        )
        if (imageUrl != null) {
            expense["imageUrl"] = imageUrl
        }

        db.collection("users")
            .document(user.uid)
            .collection("expenses")
            .add(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ViewAllExpenses::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error saving expense", e)
                Toast.makeText(this, "Failed to save expense: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            IMAGE_PICK_CODE -> {
                selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    expenseImage.setImageURI(selectedImageUri)
                    contentResolver.takePersistableUriPermission(
                        selectedImageUri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } else {
                    Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
                }
            }

            IMAGE_CAPTURE_CODE -> {
                val photo = data?.extras?.get("data") as? Bitmap
                if (photo != null) {
                    val uri = saveImageToCacheAndGetUri(photo)
                    if (uri != null) {
                        selectedImageUri = uri
                        expenseImage.setImageURI(selectedImageUri)
                    } else {
                        Toast.makeText(this, "Failed to process captured image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageToCacheAndGetUri(bitmap: Bitmap): Uri? {
        return try {
            val filename = "captured_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, filename)
            val stream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

            FileProvider.getUriForFile(this, "${packageName}.provider", file)
        } catch (e: Exception) {
            Log.e("ImageSaveError", "Error saving captured image", e)
            null
        }
    }

    private fun copyUriToTempFile(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", ".jpg", cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            Log.e("CopyUriError", "Failed to copy uri to temp file", e)
            null
        }
    }
}
