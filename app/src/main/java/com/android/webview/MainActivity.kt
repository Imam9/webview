package com.android.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.*


@Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    lateinit var webView: WebView
    private var filePath: ValueCallback<Array<Uri>>? = null
    private val getFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_CANCELED) {
            filePath?.onReceiveValue(null)
        } else if (it.resultCode == Activity.RESULT_OK && filePath != null) {
            filePath?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(it.resultCode, it.data))
            filePath = null
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        webView = findViewById(R.id.wv)
        webView.webViewClient = XWebViewClient()
        webView.loadUrl("https://wedding12345.000webhostapp.com/")

        val webSettings = webView.settings

        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.allowUniversalAccessFromFileURLs = true

        webView.webChromeClient = object : WebChromeClient(){
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                filePath = filePathCallback

                val contentIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentIntent.type = "*/*"
                contentIntent.addCategory(Intent.CATEGORY_OPENABLE)

                getFile.launch(contentIntent)
                return true
            }
        }
    }

    private class XWebViewClient : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

}