package com.tejgokabhi.salonbooking.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.tejgokabhi.salonbooking.activities.HomeMainActivity
import com.tejgokabhi.salonbooking.databinding.FragmentPrivacyBinding

class PrivacyFragment : Fragment() {

    private val binding by lazy { FragmentPrivacyBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return binding.root
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = binding.web
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        val url = "https://www.app-privacy-policy.com/live.php?token=C4jTR9wDjYdYogikIJ23JCf1ljdgtaja"
        if (url.isNotBlank()) {
            try {
                webView.loadUrl(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
        }

        binding.btPolicyBack.setOnClickListener {
            startActivity(Intent(requireActivity(), HomeMainActivity::class.java))
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                startActivity(Intent(requireActivity(), HomeMainActivity::class.java))
            }

        })
    }

}