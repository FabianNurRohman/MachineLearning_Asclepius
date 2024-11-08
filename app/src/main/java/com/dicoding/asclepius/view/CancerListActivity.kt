package com.dicoding.asclepius.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.CancerAdapter
import com.dicoding.asclepius.database.CancerDao
import com.dicoding.asclepius.database.CancerRoomDatabase
import com.dicoding.asclepius.databinding.ActivityCancerListBinding

class CancerListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCancerListBinding
    private lateinit var cancerDao: CancerDao
    private lateinit var cancerDatabase: CancerRoomDatabase
    private lateinit var cancerAdapter: CancerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCancerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cancerDatabase = CancerRoomDatabase.getDatabase(this)
        cancerDao = cancerDatabase.noteDao()

        cancerAdapter = CancerAdapter()
        binding.cancerRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.cancerRecyclerView.adapter = cancerAdapter

        cancerDao.getAllCancer().observe(this, Observer { cancerList ->
            cancerList?.let {
                cancerAdapter.submitList(it)
            }
        })
    }
}
