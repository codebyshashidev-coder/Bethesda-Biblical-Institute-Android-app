package com.bethesda.institute

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class WebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var offlineLayout: View
    private var uploadCallback: ValueCallback<Array<Uri>>? = null
    private var targetUrl: String = ""

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val results: Array<Uri>? = if (result.resultCode == RESULT_OK && data != null) {
            val clip = data.clipData
            if (clip != null) {
                Array(clip.itemCount) { i -> clip.getItemAt(i).uri }
            } else {
                data.data?.let { arrayOf(it) }
            }
        } else null
        uploadCallback?.onReceiveValue(results)
        uploadCallback = null
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        targetUrl = intent.getStringExtra(EXTRA_URL) ?: getString(R.string.base_url)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = title
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)
        offlineLayout = findViewById(R.id.offlineLayout)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        setupWebView()
        swipeRefresh.setColorSchemeResources(R.color.secondary)
        swipeRefresh.setOnRefreshListener {
            if (isOnline()) webView.reload() else {
                swipeRefresh.isRefreshing = false
                showOffline()
            }
        }
        findViewById<View>(R.id.retryButton).setOnClickListener { load() }

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            load()
        }
    }

    private fun setupWebView() {
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val host = request.url.host ?: ""
                if (host.contains("bethesdabiblicalinstitute.com")) return false
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, request.url))
                    true
                } catch (e: Exception) {
                    false
                }
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: android.webkit.WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                if (request.isForMainFrame) showOffline()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                uploadCallback?.onReceiveValue(null)
                uploadCallback = filePathCallback
                return try {
                    fileChooserLauncher.launch(fileChooserParams.createIntent())
                    true
                } catch (e: Exception) {
                    uploadCallback = null
                    false
                }
            }
        }

        webView.setDownloadListener { url, _, _, mimeType, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                request.setMimeType(mimeType)
                request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                val fileName = Uri.parse(url).lastPathSegment ?: "download"
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(this, getString(R.string.downloading), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to start download.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun load() {
        offlineLayout.visibility = View.GONE
        if (isOnline()) {
            webView.visibility = View.VISIBLE
            webView.loadUrl(targetUrl)
        } else {
            showOffline()
        }
    }

    private fun showOffline() {
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
        swipeRefresh.isRefreshing = false
        offlineLayout.visibility = View.VISIBLE
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }
}
