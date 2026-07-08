package com.bethesda.institute.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bethesda.institute.R
import com.bethesda.institute.WebViewActivity
import com.bethesda.institute.adapter.ProgramAdapter
import com.bethesda.institute.model.Programs

class CoursesFragment : Fragment(R.layout.fragment_courses) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.programsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ProgramAdapter(Programs.all) { program ->
            val intent = Intent(requireContext(), WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.EXTRA_URL, program.url)
            intent.putExtra(WebViewActivity.EXTRA_TITLE, program.title)
            startActivity(intent)
        }
    }
}
