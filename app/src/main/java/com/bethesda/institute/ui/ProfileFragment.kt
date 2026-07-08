package com.bethesda.institute.ui

import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.bethesda.institute.R

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = view.findViewById<WebView>(R.id.profileWebView)
        val progressBar = view.findViewById<ProgressBar>(R.id.profileProgressBar)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
        }

        webView.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }
        }

        // This is the site's real, existing session-based login (login.php).
        // Credentials are typed by the student directly into that page and
        // are handled entirely server-side — the app never sees or stores them.
        webView.loadUrl(getString(R.string.login_url))
    }
}
