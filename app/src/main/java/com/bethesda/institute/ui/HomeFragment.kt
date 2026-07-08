package com.bethesda.institute.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bethesda.institute.R
import com.bethesda.institute.WebViewActivity
import com.bethesda.institute.adapter.NewsAdapter
import com.bethesda.institute.model.NewsItem
import com.bethesda.institute.net.NewsApi
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val baseUrl = getString(R.string.base_url)
        val newsApiUrl = getString(R.string.news_api_url)

        view.findViewById<MaterialButton>(R.id.applyNowButton).setOnClickListener {
            openWeb(getString(R.string.admissions_url), "Apply Now")
        }
        view.findViewById<MaterialButton>(R.id.learnMoreButton).setOnClickListener {
            openWeb(baseUrl + "about.php", "About Us")
        }
        view.findViewById<MaterialButton>(R.id.enquireButton).setOnClickListener {
            openWeb(baseUrl + "contact.php", "Contact Us")
        }
        view.findViewById<TextView>(R.id.viewAllNewsLink).setOnClickListener {
            openWeb(baseUrl + "news.php", "News & Updates")
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.newsRecyclerView)
        val emptyText = view.findViewById<TextView>(R.id.newsEmptyText)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = NewsAdapter { item: NewsItem ->
            openWeb(baseUrl + "news.php?slug=" + item.slug, item.title)
        }
        recyclerView.adapter = adapter

        emptyText.visibility = View.VISIBLE
        emptyText.text = "Loading news…"

        NewsApi.fetchNews(newsApiUrl, limit = 3) { items, error ->
            activity?.runOnUiThread {
                if (items != null && items.isNotEmpty()) {
                    emptyText.visibility = View.GONE
                    adapter.submitList(items)
                } else {
                    emptyText.visibility = View.VISIBLE
                    emptyText.text = if (error != null)
                        "Couldn't load news right now."
                    else
                        "No news posted yet — check back soon."
                }
            }
        }
    }

    private fun openWeb(url: String, title: String) {
        val intent = Intent(requireContext(), WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.EXTRA_URL, url)
        intent.putExtra(WebViewActivity.EXTRA_TITLE, title)
        startActivity(intent)
    }
}
