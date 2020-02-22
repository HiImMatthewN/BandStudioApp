package com.example.bandstudioapp.Misc

import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.bandstudioapp.R
import com.example.bandstudioapp.RegisterLogin.LoginActivity


class SplashActivity : AppCompatActivity() {
private lateinit var  videoView:VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // make the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

//        val path = "android.resource://" + packageName + "/" + R.raw.splash_screen_video
//        videoView = findViewById(R.id.splashScreen_video)
//        videoView.setVideoURI(Uri.parse(path))
//        videoView.start()
            playVideo()




        Handler().postDelayed({
            Handler().postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 0)
        },  3500)

    }


    private fun playVideo(){

        val rootView = findViewById(R.id.splash_layout) as RelativeLayout
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val rootViewParams =
            rootView.layoutParams as FrameLayout.LayoutParams
        val videoWidth = 864
        val videoHeight = 1280

        if ((videoWidth.toFloat() / videoHeight.toFloat())<size.x.toFloat() / size.y.toFloat()) {
            rootViewParams.width = size.x
            rootViewParams.height = videoHeight * size.x / videoWidth
            rootView.x = 0f
            rootView.y = ((rootViewParams.height - size.y) / 2 * -1).toFloat()
        } else {
            rootViewParams.width = videoWidth * size.y / videoHeight
            rootViewParams.height = size.y
            rootView.x = ((rootViewParams.width - size.x) / 2 * -1).toFloat()
            rootView.y = 0f
        }
        rootView.layoutParams = rootViewParams


        val mVideoView = findViewById(R.id.splashScreen_video) as VideoView
        mVideoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.splash_screen_video))
        mVideoView.requestFocus()
        mVideoView.setOnPreparedListener { mVideoView.start() }

    }
}