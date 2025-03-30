package com.tejgokabhi.salonbooking.authfragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.databinding.FragmentForgetPasswordBinding
import com.tejgokabhi.salonbooking.utils.Utils


class ForgetPasswordFragment : Fragment() {
    private val binding by lazy { FragmentForgetPasswordBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var progress: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        progress = Utils.showLoading(requireContext())

        binding.btForgetBack.setOnClickListener {
            findNavController().navigate(R.id.action_forgetPasswordFragment_to_loginFragment)
        }

        binding.btConfirm.setOnClickListener {
            if (binding.etEmail.text.toString().isEmpty()) {
                Utils.showMessage(requireContext(), "Please Enter Recovery Email")
            } else {
                isEmailRegistered(binding.etEmail.text.toString())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

    }


    private fun isEmailRegistered(email: String) {
        progress.show()
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    val isRegistered = !signInMethods.isNullOrEmpty()
                    if (isRegistered) {
                        sendPasswordResetEmail(binding.etEmail.text.toString())
                    } else {
                        progress.dismiss()
                        Utils.showMessage(requireContext(), "Gmail Not Registered")
                    }
                } else {
                    progress.dismiss()
                    Utils.showMessage(requireContext(), "Something went wrong")
                }
            }
    }

    private fun sendPasswordResetEmail(email: String) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progress.dismiss()
                    Utils.showMessage(requireContext(), "Check Email")
                    findNavController().navigate(R.id.action_forgetPasswordFragment_to_loginFragment)
                } else {
                    progress.dismiss()
                    Utils.showMessage(requireContext(), "Something went wrong")
                }
            }

    }
}