package com.bethesda.institute

import android.Manifest
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bethesda.institute.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var uploadCallback: ValueCallback<Array<Uri>>? = null
    private val baseUrl by lazy { getString(R.string.base_url) }

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

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op: only affects download-complete notifications */ }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op: only needed for saving downloads on Android <= 9 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        setupWebView()
        setupSwipeRefresh()
        binding.retryButton.setOnClickListener { loadHome() }

        if (savedInstanceState != null) {
            binding.webView.restoreState(savedInstanceState)
        } else {
            loadHome()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.gold)
        binding.swipeRefresh.setOnRefreshListener {
            if (isOnline()) binding.webView.reload() else {
                binding.swipeRefresh.isRefreshing = false
                showOffline()
            }
        }
    }

    private fun setupWebView() {
        val webView = binding.webView
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
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                val host = request.url.host ?: ""

                // Keep institute pages inside the app's WebView.
                if (host.contains("bethesdabiblicalinstitute.com")) {
                    return false
                }

                // Hand off WhatsApp, tel, mailto, and other external links to their own apps.
                return try {
                    when {
                        url.startsWith("tel:") || url.startsWith("mailto:") ||
                            url.startsWith("whatsapp:") || host.contains("wa.me") -> {
                            startActivity(Intent(Intent.ACTION_VIEW, request.url))
                            true
                        }
                        else -> {
                            startActivity(Intent(Intent.ACTION_VIEW, request.url))
                            true
                        }
                    }
                } catch (e: Exception) {
                    false
                }
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
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
                binding.progressBar.progress = newProgress
                binding.progressBar.visibility =
                    if (newProgress in 1..99) View.VISIBLE else View.GONE
            }

            // Required so <input type="file"> works for application photos, ID uploads,
            // and certificate uploads on the admissions/affiliation forms.
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                uploadCallback?.onReceiveValue(null)
                uploadCallback = filePathCallback
                val intent = fileChooserParams.createIntent()
                return try {
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: Exception) {
                    uploadCallback = null
                    false
                }
            }
        }

        // Certificates and other documents served for download (PDFs, images).
        webView.setDownloadListener { url, _, contentDisposition, mimeType, _ ->
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

    private fun loadHome() {
        binding.offlineLayout.visibility = View.GONE
        if (isOnline()) {
            binding.webView.visibility = View.VISIBLE
            binding.webView.loadUrl(baseUrl)
        } else {
            showOffline()
        }
    }

    private fun showOffline() {
        binding.webView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        binding.offlineLayout.visibility = View.VISIBLE
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }
}
