package com.example.firebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import com.example.firebase.databinding.ActivityFormactivityBinding
import com.example.firebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    //intance titik awal penggunaan firebase
    private val firestore = FirebaseFirestore.getInstance()
    //devinisi database budgets
    private val complaintCollectionRef = firestore.collection("complaints")
    //binding doang
    private lateinit var binding: ActivityMainBinding
    //NYIMPEN ID SAAT ADA UPDATE
    private var updateId = ""
    //deklarasi saat update
    private val complaintListLiveData: MutableLiveData<List<Complaint>> by lazy {
        MutableLiveData<List<Complaint>>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        with(binding) {
            btnTambah.setOnClickListener {
                val intent = Intent(this@MainActivity, formactivity::class.java)
                startActivity(intent)
            }

            //datanya ke from kalau diklik
            listView.setOnItemClickListener { adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Complaint

                // Create an Intent to start the ActivityFormActivity
                val intent = Intent(this@MainActivity, formactivity::class.java).apply {
                    // Pass the data to the ActivityFormActivity
                    putExtra("complaint_id", item.id)
                    putExtra("complaint_name", item.name)
                    putExtra("complaint_title", item.title)
                    putExtra("complaint_description", item.description)
                }
                startActivity(intent)
            }
            // kalo list di klik lama nanti dia ngapus
            listView.onItemLongClickListener = AdapterView.OnItemLongClickListener {
                    adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Complaint
                deleteBudget(item)
                true
            }
        }
        observeBudgets()
        getAllBudgets()
    }

    //untuk ambil data budgetnya
    private fun getAllBudgets() {
        observeBudgetChanges()
    }
    //untuk observe nya budget yang ada
    private fun observeBudgets() {
        complaintListLiveData.observe(this) { complaints ->
            val adapter = ArrayAdapter(
                this,
                R.layout.list_item_aduan,  // Use the correct layout resource ID here
                R.id.textViewComplaintItem,    // Specify the ID of the TextView in the layout
                complaints.toMutableList()
            )
            binding.listView.adapter = adapter
        }
    }
    //cek perubahan di budget, kalo erorr nanti muncul notip di log, kalo dia val nya ada nanti dia muncul
    private fun observeBudgetChanges() {
        complaintCollectionRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("MainActivity", "Error listening for budget changes: ", error)
                return@addSnapshotListener
            }
            val complaints = snapshots?.toObjects(Complaint::class.java)
            if (complaints != null) {
                complaintListLiveData.postValue(complaints)
            }
        }
    }
    //buat hapusnya
    private fun deleteBudget(complaint: Complaint) {
        if (complaint.id.isEmpty()) {
            Log.d("MainActivity", "Error deleting: budget ID is empty!")
            return
        }
        complaintCollectionRef.document(complaint.id).delete()
            .addOnFailureListener {
                Log.d("MainActivity", "Error deleting budget: ", it)
            }
    }
}