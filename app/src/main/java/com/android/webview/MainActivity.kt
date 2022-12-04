package com.android.webview

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    companion object{
        const val REQUEST_SELECT_FILE = 100
        const val FILECHOOSER_RESULTCODE = 1
    }

    lateinit var webView: WebView
    private var mUploadMessage: ValueCallback<Uri>? = null
    var uploadMessage: ValueCallback<Array<Uri>>? = null

    private var mCM: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                FILECHOOSER_RESULTCODE
            )
        }

        webView = findViewById(R.id.wv)
        webView.webViewClient = XWebViewClient()
        webView.loadUrl("http://192.168.47.157/applikasi_wedding/")

        val webSettings = webView.settings

        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.allowUniversalAccessFromFileURLs = true

        webView.webChromeClient = object : WebChromeClient(){
//            protected fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String?) {
//                mUploadMessage = uploadMsg
//                val i = Intent(Intent.ACTION_GET_CONTENT)
//                i.addCategory(Intent.CATEGORY_OPENABLE)
//                i.type = "image/*"
//                startActivityForResult(
//                    Intent.createChooser(i, "File Browser"),
//                    FILECHOOSER_RESULTCODE
//                )
//            }
//
//            protected fun openFileChooser(
//                uploadMsg: ValueCallback<Uri>,
//                acceptType: String?,
//                capture: String?
//            ) {
//                mUploadMessage = uploadMsg
//                val intent = Intent(Intent.ACTION_GET_CONTENT)
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                intent.type = "image/*"
//                startActivityForResult(
//                    Intent.createChooser(intent, "File Browser"),
//                    FILECHOOSER_RESULTCODE
//                )
//            }
//
//
//
//            protected fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
//                mUploadMessage = uploadMsg
//                val i = Intent(Intent.ACTION_GET_CONTENT)
//                i.addCategory(Intent.CATEGORY_OPENABLE)
//                i.type = "image/*"
//                startActivityForResult(
//                    Intent.createChooser(i, "File Chooser"),
//                    FILECHOOSER_RESULTCODE
//                )
//            }

//            @SuppressLint("ObsoleteSdkInt")
//            override fun onShowFileChooser(
//                mWebView: WebView?,
//                filePathCallback: ValueCallback<Array<Uri>>,
//                fileChooserParams: FileChooserParams
//            ): Boolean {
//                if (uploadMessage != null) {
//                    uploadMessage?.onReceiveValue(null)
//                    uploadMessage = null
//                }
//                uploadMessage = filePathCallback
//                var intent: Intent? = null
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    intent = fileChooserParams.createIntent()
//                }
//                try {
//                    startActivityForResult(intent, REQUEST_SELECT_FILE)
//                } catch (e: ActivityNotFoundException) {
//                    uploadMessage = null
//                    return false
//                }
//                return true
//            }

            override fun onShowFileChooser(
                webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (uploadMessage != null) {
                    uploadMessage?.onReceiveValue(null)
                }
                uploadMessage = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(this@MainActivity.packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCM)
                    } catch (ex: IOException) {
                        Log.e("Main", "Image file creation failed", ex)
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.getAbsolutePath()
                        takePictureIntent!!.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile)
                        )
                    } else {
                        takePictureIntent = null
                    }
                }
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                val intentArray: Array<Intent?> = takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "gambar")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, REQUEST_SELECT_FILE)
                return true
            }
        }



    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                var results: Array<Uri>? = null
                if (uploadMessage == null) return
//                uploadMessage!!.onReceiveValue(
//                    WebChromeClient.FileChooserParams.parseResult(
//                        resultCode,
//                        intent
//                    )
//                )
//                uploadMessage = null

                if (intent == null) {
                    //Capture Photo if no image available
                    if (mCM != null) {
                        results = arrayOf(Uri.parse(mCM))
                    }
                } else {
                    val dataString = intent.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }

                uploadMessage?.onReceiveValue(results)
                uploadMessage = null
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            val result =
                if (intent == null || resultCode != RESULT_OK) null else intent.data
            mUploadMessage!!.onReceiveValue(result)
            mUploadMessage = null
        }
    }

    private class XWebViewClient : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()){
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}