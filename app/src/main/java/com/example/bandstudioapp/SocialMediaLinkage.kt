package com.example.bandstudioapp

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity

object SocialMediaLinkage {

    fun launchSpotifySocialMedia(rawURI: String) {

        val pm: PackageManager = MyClass.getContext().packageManager


        var accountURI = rawURI.substring(rawURI.indexOf("/artist/") + 8)
        accountURI = accountURI.substring(0, accountURI.indexOf("?si"))

        try {
            pm.getPackageInfo("com.spotify.music", 0)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:artist:$accountURI"))
            startActivity(MyClass.getContext(), intent, null)
        } catch (e: PackageManager.NameNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawURI))
            intent.putExtra(
                Intent.EXTRA_REFERRER,
                Uri.parse("android-app://" + MyClass.getContext().packageName)
            )
            startActivity(MyClass.getContext(), intent, null)
        }
    }

    fun launchFacebookSocialMedia(fbPageID: String) {
        val pm: PackageManager = MyClass.getContext().packageManager


        try {
            pm.getPackageInfo("com.facebook.katana", 0)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/$fbPageID"))
            intent.putExtra(
                Intent.EXTRA_REFERRER,
                Uri.parse("android-app://" + MyClass.getContext().packageName)
            )
            startActivity(MyClass.getContext(), intent, null)
        } catch (e: PackageManager.NameNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/$fbPageID"))
            intent.putExtra(
                Intent.EXTRA_REFERRER,
                Uri.parse("android-app://" + MyClass.getContext().packageName)
            )
            startActivity(MyClass.getContext(), intent, null)
        }


    }

    fun launchYoutubeSocialMedia(rawURI: String) {
        val pm: PackageManager = MyClass.getContext().packageManager


       try {
            pm.getPackageInfo("com.google.android.youtube", 0)
           val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawURI))
           intent.putExtra(
               Intent.EXTRA_REFERRER,
               Uri.parse("android-app://" + MyClass.getContext().packageName)
           )
           startActivity(MyClass.getContext(), intent, null)

        } catch (e: PackageManager.NameNotFoundException) {
           val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawURI))
           intent.putExtra(
               Intent.EXTRA_REFERRER,
               Uri.parse("android-app://" + MyClass.getContext().packageName)
           )
           startActivity(MyClass.getContext(), intent, null)
        }








    }


}