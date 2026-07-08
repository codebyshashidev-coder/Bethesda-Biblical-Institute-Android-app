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
import com.bethesda.institute.adapter.TestimonialAdapter
import com.bethesda.institute.model.HomeBanner
import com.bethesda.institute.model.NewsItem
import com.bethesda.institute.net.HomeApi
import com.bethesda.institute.net.NewsApi
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var currentBanner: HomeBanner? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val baseUrl = getString(R.string.base_url)
        val newsApiUrl = getString(R.string.news_api_url)
        val homeApiUrl = getString(R.string.home_api_url)

        val applyNowButton = view.findViewById<MaterialButton>(R.id.applyNowButton)
        val learnMoreButton = view.findViewById<MaterialButton>(R.id.learnMoreButton)

        applyNowButton.setOnClickListener {
            openWeb(resolveLink(currentBanner?.button1Link, getString(R.string.admissions_url)), "Apply Now")
        }
        learnMoreButton.setOnClickListener {
            openWeb(resolveLink(currentBanner?.button2Link, baseUrl + "about.php"), "About Us")
        }
        view.findViewById<MaterialButton>(R.id.enquireButton).setOnClickListener {
            openWeb(baseUrl + "contact.php", "Contact Us")
        }
        view.findViewById<TextView>(R.id.viewAllNewsLink).setOnClickListener {
            openWeb(baseUrl + "news.php", "News & Updates")
        }

        // ── News list ──────────────────────────────────────────
        val newsRecyclerView = view.findViewById<RecyclerView>(R.id.newsRecyclerView)
        val newsEmptyText = view.findViewById<TextView>(R.id.newsEmptyText)
        newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val newsAdapter = NewsAdapter { item: NewsItem ->
            openWeb(baseUrl + "news.php?slug=" + item.slug, item.title)
        }
        newsRecyclerView.adapter = newsAdapter

        newsEmptyText.visibility = View.VISIBLE
        newsEmptyText.text = "Loading news…"

        NewsApi.fetchNews(newsApiUrl, limit = 3) { items, error ->
            activity?.runOnUiThread {
                if (items != null && items.isNotEmpty()) {
                    newsEmptyText.visibility = View.GONE
                    newsAdapter.submitList(items)
                } else {
                    newsEmptyText.visibility = View.VISIBLE
                    newsEmptyText.text = if (error != null)
                        "Couldn't load news right now."
                    else
                        "No news posted yet — check back soon."
                }
            }
        }

        // ── Testimonials ───────────────────────────────────────
        val testimonialsRecyclerView = view.findViewById<RecyclerView>(R.id.testimonialsRecyclerView)
        testimonialsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val testimonialAdapter = TestimonialAdapter()
        testimonialsRecyclerView.adapter = testimonialAdapter

        // ── Banner + testimonials (live from the database via api/home.php) ──
        val heroTitle = view.findViewById<TextView>(R.id.heroTitle)
        val heroSubtitle = view.findViewById<TextView>(R.id.heroSubtitle)

        HomeApi.fetchHome(homeApiUrl) { data, _ ->
            activity?.runOnUiThread {
                if (data != null) {
                    currentBanner = data.banner
                    heroTitle.text = data.banner.title
                    heroSubtitle.text = data.banner.subtitle
                    applyNowButton.text = data.banner.button1Text
                    learnMoreButton.text = data.banner.button2Text
                    testimonialAdapter.submitList(data.testimonials)
                }
                // On failure, the hero keeps its static default copy and the
                // testimonials section simply stays empty — no crash, no blank screen.
            }
        }
    }

    private fun resolveLink(link: String?, fallback: String): String {
        if (link.isNullOrBlank()) return fallback
        return if (link.startsWith("http://") || link.startsWith("https://")) {
            link
        } else {
            getString(R.string.base_url) + link.removePrefix("/")
        }
    }

    private fun openWeb(url: String, title: String) {
        val intent = Intent(requireContext(), WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.EXTRA_URL, url)
        intent.putExtra(WebViewActivity.EXTRA_TITLE, title)
        startActivity(intent)
    }
}
