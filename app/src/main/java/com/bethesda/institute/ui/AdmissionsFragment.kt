package com.bethesda.institute.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bethesda.institute.R
import com.bethesda.institute.WebViewActivity
import com.google.android.material.button.MaterialButton

class AdmissionsFragment : Fragment(R.layout.fragment_admissions) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val baseUrl = getString(R.string.base_url)

        view.findViewById<MaterialButton>(R.id.applyOnlineButton).setOnClickListener {
            openWeb(getString(R.string.admissions_url), "Apply Online")
        }
        view.findViewById<MaterialButton>(R.id.admissionProcessButton).setOnClickListener {
            openWeb(baseUrl + "admissions.php#process", "Admission Process")
        }
        view.findViewById<MaterialButton>(R.id.affiliationButton).setOnClickListener {
            openWeb(getString(R.string.affiliation_url), "Affiliation & Accreditation")
        }
    }

    private fun openWeb(url: String, title: String) {
        val intent = Intent(requireContext(), WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.EXTRA_URL, url)
        intent.putExtra(WebViewActivity.EXTRA_TITLE, title)
        startActivity(intent)
    }
}
