package com.tejgokabhi.salonbooking.authfragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.activities.HomeMainActivity
import com.tejgokabhi.salonbooking.databinding.FragmentSignUpBinding
import com.tejgokabhi.salonbooking.model.UserModel
import com.tejgokabhi.salonbooking.preference.SharedPref
import com.tejgokabhi.salonbooking.utils.Constants
import com.tejgokabhi.salonbooking.utils.Utils


class SignUpFragment : Fragment() {
    private val binding by lazy { FragmentSignUpBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var progress: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        progress = Utils.showLoading(requireContext())


        binding.btCreateAcBack.setOnClickListener {
            launchLoginActivity()
        }

        binding.tvAlreadyAc1.setOnClickListener {
            launchLoginActivity()
        }

        binding.tvAlreadyAc2.setOnClickListener {
            launchLoginActivity()
        }

        binding.btCreateAccount.setOnClickListener {
            registerNewUser()
        }
    }

    private fun registerNewUser() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmailId.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val createPassword = binding.etCreatePass.text.toString().trim()
        val confirmPassword = binding.etConfirmPass.text.toString().trim()

        validateUserInput(
            firstName,
            lastName,
            email,
            phoneNumber,
            createPassword,
            confirmPassword
        )
    }

    private fun validateUserInput(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        createPassword: String,
        confirmPassword: String
    ) {
        if (firstName.isEmpty()) {
            binding.etFirstName.requestFocus()
            binding.etFirstName.error = "Empty"
        } else if (lastName.isEmpty()) {
            binding.etLastName.requestFocus()
            binding.etLastName.error = "Empty"
        } else if (email.isEmpty()) {
            binding.etEmailId.requestFocus()
            binding.etEmailId.error = "Empty"
        } else if (createPassword.isEmpty()) {
            binding.etCreatePass.requestFocus()
            binding.etCreatePass.error = "Empty"
        } else if (confirmPassword.isEmpty()) {
            binding.etConfirmPass.requestFocus()
            binding.etConfirmPass.error = "Empty"
        } else if (phoneNumber.isEmpty() || phoneNumber.length != 10) {
            binding.etPhoneNumber.requestFocus()
            binding.etPhoneNumber.error = "Error"
        }  else if (confirmPassword != createPassword) {
            Utils.showMessage(requireContext(), "Password Not Match")
        } else {
            val formattedPhoneNumber = if (phoneNumber.startsWith("+91")) phoneNumber else "+91$phoneNumber"
            createUser(firstName, lastName, email, formattedPhoneNumber)
        }
    }

    private fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
    ) {
        progress.show()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            val token = task.result
            auth.createUserWithEmailAndPassword(email, binding.etConfirmPass.text.toString())
                .addOnSuccessListener {
                    val userId = it.user!!.uid
                    val user = UserModel(
                        userId,
                        firstName,
                        lastName,
                        email,
                        phoneNumber,
                        token
                    )

                    val usersReference = database.getReference(Constants.USER_REF)

                    usersReference.child(userId).setValue(user).addOnSuccessListener {
                        SharedPref.saveUserData(requireContext(), user)
                        progress.dismiss()
                        findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                    }.addOnFailureListener {
                        progress.dismiss()
                        Utils.showMessage(requireContext(), "Something went wrong")
                    }


                }.addOnFailureListener {
                    progress.dismiss()
                    Utils.showMessage(requireContext(), "Account Creation Failed")
                }


        })

    }


    private fun launchLoginActivity() {
        findNavController().navigateUp()
    }

}