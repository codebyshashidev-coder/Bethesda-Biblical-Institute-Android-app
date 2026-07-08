package com.bethesda.institute.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bethesda.institute.R
import com.bethesda.institute.WebViewActivity
import com.bethesda.institute.adapter.ProgramAdapter
import com.bethesda.institute.model.Program
import com.bethesda.institute.model.Programs
import com.google.android.material.chip.ChipGroup

class CoursesFragment : Fragment(R.layout.fragment_courses) {

    private var currentCategory: String = "All"
    private var currentQuery: String = ""
    private lateinit var adapter: ProgramAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.programsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProgramAdapter(Programs.all) { program: Program ->
            val intent = Intent(requireContext(), WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.EXTRA_URL, program.url)
            intent.putExtra(WebViewActivity.EXTRA_TITLE, program.title)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val chipGroup = view.findViewById<ChipGroup>(R.id.levelChipGroup)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            currentCategory = when (checkedIds.firstOrNull()) {
                R.id.chipCertificate -> "Certificate"
                R.id.chipDiploma -> "Diploma"
                R.id.chipBachelor -> "Bachelor"
                R.id.chipMaster -> "Master"
                R.id.chipDoctoral -> "Doctoral"
                else -> "All"
            }
            applyFilter()
        }

        view.findViewById<EditText>(R.id.searchCoursesInput).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString().orEmpty()
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilter() {
        val filtered = Programs.all.filter { program ->
            val matchesCategory = currentCategory == "All" || program.category == currentCategory
            val matchesQuery = currentQuery.isBlank() ||
                program.title.contains(currentQuery, ignoreCase = true) ||
                program.description.contains(currentQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
        adapter.submitList(filtered)
    }
}
