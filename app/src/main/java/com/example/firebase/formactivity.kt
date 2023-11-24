package com.example.firebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.firebase.databinding.ActivityFormactivityBinding
import com.example.firebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View

class formactivity : AppCompatActivity() {

    //intance titik awal penggunaan firebase
    private val firestore = FirebaseFirestore.getInstance()
    //devinisi database budgets
    private val complaintCollectionRef = firestore.collection("complaints")
    //binding doang
    private lateinit var binding: ActivityFormactivityBinding
    //NYIMPEN ID SAAT ADA UPDATE
    private var updateId = ""
    //deklarasi saat update
    private val budgetListLiveData: MutableLiveData<List<Complaint>> by lazy {
        MutableLiveData<List<Complaint>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val complaintId = intent.getStringExtra("complaint_id")
        val complaintName = intent.getStringExtra("complaint_name")
        val complaintTitle = intent.getStringExtra("complaint_title")
        val complaintDescription = intent.getStringExtra("complaint_description")
        val actionType = intent.getStringExtra("action_type")


        // Set the retrieved data to the EditText fields in ActivityFormActivity
        updateId = intent.getStringExtra("complaint_id") ?: ""
        binding.edtName.setText(complaintName ?: "")
        binding.edtTitle.setText(complaintTitle ?: "")
        binding.edtDesc.setText(complaintDescription ?: "")

        with(binding){
            if (actionType == "add") {
                // Menampilkan btnAdd jika jenis aksi adalah tambah
                btnAdd.visibility = View.VISIBLE
                btnUpdate.visibility = View.GONE
                //nambahin data yang diisi di form
                btnAdd.setOnClickListener {
                    val name = edtName.text.toString()
                    val title = edtTitle.text.toString()
                    val description = edtDesc.text.toString()
                    val newComplaint = Complaint(
                        name = name, title = title,
                        description = description
                    )
                    addComplaint(newComplaint)
                }
            }else if (actionType == "update") {
                // Menampilkan btnUpdate jika jenis aksi adalah pembaruan
                btnUpdate.visibility = View.VISIBLE
                btnAdd.visibility = View.GONE
                //update data yang ada diform
                btnUpdate.setOnClickListener {
                    val name = edtName.text.toString()
                    val title = edtTitle.text.toString()
                    val description = edtDesc.text.toString()
                    val complaintToUpdate =
                        Complaint(name = name, title = title, description = description)

                    // Jika updateId tidak kosong, maka lakukan pembaruan
                    if (updateId.isNotEmpty()) {
                        updateComplaint(complaintToUpdate)
                    } else {
                        // Jika updateId kosong, tambahkan komplain baru
                        addComplaint(complaintToUpdate)
                    }

                    // Bersihkan field setelah pembaruan atau penambahan data
                }
            }
        }
    }
    //nambah budgetnya
    private fun addComplaint(complaint: Complaint) {
        complaintCollectionRef
            .add(complaint)
            .addOnSuccessListener { documentReference ->
                val createdComplaintId = documentReference.id
                complaint.id = createdComplaintId
                documentReference.set(complaint)
                    .addOnSuccessListener {
                        // Data added successfully, start MainActivity
                        val intent = Intent(this@formactivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish() // Close current activity if needed
                    }
                    .addOnFailureListener { exception ->
                        Log.d("formactivity", "Error updating document", exception)
                        // Handle failure to set data if needed
                    }
            }
            .addOnFailureListener { exception ->
                Log.d("formactivity", "Error adding document", exception)
                // Handle failure to add data if needed
            }
    }
    //update data budget baru
    private fun updateComplaint(complaint: Complaint) {
        // Menggunakan update() untuk mempertahankan data yang ada
        complaintCollectionRef.document(updateId).update(
            "name", complaint.name,
            "title", complaint.title,
            "description", complaint.description
        )
            .addOnSuccessListener {
                // Data berhasil diperbarui, kembali ke MainActivity
                val intent = Intent(this@formactivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish() // Tutup activity saat ini jika diperlukan
            }
            .addOnFailureListener { exception ->
                Log.d("formactivity", "Error updating document", exception)
                // Tangani jika gagal memperbarui data jika diperlukan
            }
    }


    //format data yang isinya empty
    private fun setEmptyField() {
        with(binding) {
            edtName.setText("")
            edtTitle.setText("")
            edtName.setText("")
        }
    }
}
