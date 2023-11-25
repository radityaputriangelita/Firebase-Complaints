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
    //definisiin database
    private val complaintCollectionRef = firestore.collection("complaints")
    //binding doang
    private lateinit var binding: ActivityFormactivityBinding
    //NYIMPEN ID SAAT ADA UPDATE
    private var updateId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get data yang udah di intent
        val complaintId = intent.getStringExtra("complaint_id")
        val complaintName = intent.getStringExtra("complaint_name")
        val complaintTitle = intent.getStringExtra("complaint_title")
        val complaintDescription = intent.getStringExtra("complaint_description")
        //bawa action type nya juga biar tau dia mau update atau add biar button yang muncul sesuai
        val actionType = intent.getStringExtra("action_type")

        //set daya yang udah di intent sesuai inputannya dan nyimpen id yang udh di intent buat update nanti kalau ada.
        //nyimpen id kalau mau update ke updateId
        updateId = intent.getStringExtra("complaint_id") ?: ""
        binding.edtName.setText(complaintName ?: "")
        binding.edtTitle.setText(complaintTitle ?: "")
        binding.edtDesc.setText(complaintDescription ?: "")

        with(binding){
            if (actionType == "add") {
                // Menampilkan btnAdd kalau dia milihnya buat tambah data
                btnAdd.visibility = View.VISIBLE
                btnUpdate.visibility = View.GONE
                //hubungin data yang diisi di form buat ditambahin
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
                // munculin btn update kalo klik listnya buat update
                btnUpdate.visibility = View.VISIBLE
                btnAdd.visibility = View.GONE
                //terus ada functionnya btn update
                btnUpdate.setOnClickListener {
                    val name = edtName.text.toString()
                    val title = edtTitle.text.toString()
                    val description = edtDesc.text.toString()
                    val complaintToUpdate =
                        Complaint(name = name, title = title, description = description)

                    // kalo update id nya ga kosong terus di update sesuai id nya
                    if (updateId.isNotEmpty()) {
                        updateComplaint(complaintToUpdate)
                    } else {
                        // tapi kalau dia kosong jadiin dia sebagai update baru
                        addComplaint(complaintToUpdate)
                    }
                }
            }
        }
    }
    //nambah budgetnya
    private fun addComplaint(complaint: Complaint) {
        //definisiin dia dengan databasenya
        complaintCollectionRef
            .add(complaint)
            .addOnSuccessListener { documentReference ->
                val createdComplaintId = documentReference.id
                complaint.id = createdComplaintId
                documentReference.set(complaint)
                    .addOnSuccessListener {
                        // balik ke main kalo nambahnya berhasil
                        val intent = Intent(this@formactivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    // error handlingnya kalo ga bisa balik
                    .addOnFailureListener { exception ->
                        Log.d("formactivity", "Error updating document", exception)
                    }
            }
            // error handling kalo ga bisa nyimpen
            .addOnFailureListener { exception ->
                Log.d("formactivity", "Error adding document", exception)
            }
    }
    //update data budget baru
    private fun updateComplaint(complaint: Complaint) {
        // pake function update() dimana nanti data yang ga di update bakal tetap
        complaintCollectionRef.document(updateId).update(
            "name", complaint.name,
            "title", complaint.title,
            "description", complaint.description
        )

            // Dbalikin data ke main kalo udh update
            .addOnSuccessListener {
                val intent = Intent(this@formactivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            //error handling kalo gagal update datanya
            .addOnFailureListener { exception ->
                Log.d("formactivity", "Error updating document", exception)
            }
    }
}
