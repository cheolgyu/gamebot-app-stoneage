package com.highserpot.myad

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds

open class IntentFragment : Fragment() {
    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.intent_fragment, container, false)
        MobileAds.initialize(context) {}
        mInterstitialAd = InterstitialAd(context)
        mInterstitialAd.adUnitId = getString(R.string.ad_front)
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("TAGHSS", "onAdLoaded")
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                Log.d("TAGHSS", "onAdFailedToLoad")
            }

            override fun onAdOpened() {
                Log.d("TAGHSS", "onAdOpened")
            }

            override fun onAdClicked() {
                Log.d("TAGHSS", "onAdClicked")
            }

            override fun onAdLeftApplication() {
                Log.d("TAGHSS", "onAdLeftApplication")
            }

            override fun onAdClosed() {
                Log.d("TAGHSS", "onAdClosed")
                // Code to be executed when the interstitial ad is closed.
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        var mbtn = v.findViewById<View>(R.id.button)
        mbtn.setOnClickListener { btn_click() }
        return v
    }

     fun btn_click() {
         Log.d("TAGHSS", "btn_click")
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            Log.d("TAGHSS", "The interstitial wasn't loaded yet.")
        }
    }


}