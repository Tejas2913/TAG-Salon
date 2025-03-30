package com.tejgokabhi.salonbooking.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.tejgokabhi.salonbooking.databinding.ActivityEditProfileBinding
import com.tejgokabhi.salonbooking.model.UserModel
import com.tejgokabhi.salonbooking.preference.SharedPref
import com.tejgokabhi.salonbooking.utils.Constants
import com.tejgokabhi.salonbooking.utils.Utils

class EditProfileActivity : AppCompatActivity() {
    private val binding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }
    private lateinit var database: FirebaseDatabase
    private var userData: UserModel? = UserModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        userData = SharedPref.getUserData(this@EditProfileActivity)
        database = FirebaseDatabase.getInstance()
        setUserData(userData)

        binding.apply {
            btBack.setOnClickListener {
                startActivity(Intent(this@EditProfileActivity, HomeMainActivity::class.java))
                finish()
            }

            btUpdate.setOnClickListener {
                val firstName = binding.etFirstName.text.toString().trim()
                val lastName = binding.etLastName.text.toString().trim()
                val email = binding.etEmailId.text.toString().trim()
                val phoneNumber = binding.etPhoneNumber.text.toString().trim()

                validateUserInput(firstName, lastName, email, phoneNumber)
            }

        }

    }



    private fun validateUserInput(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String
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
        } else if (phoneNumber.isEmpty() || phoneNumber.length != 13) {
            binding.etPhoneNumber.requestFocus()
            binding.etPhoneNumber.error = "Error"
        }  else {
            updateUser(firstName, lastName, email, phoneNumber)
        }

    }

    private fun updateUser(firstName: String, lastName: String, email: String, phoneNumber: String) {
        val userId = Firebase.auth.currentUser!!.uid
        val user = UserModel(
            userId,
            firstName,
            lastName,
            email,
            phoneNumber,
            userData!!.token
        )

        val usersReference = database.getReference(Constants.USER_REF)

        usersReference.child(userId).setValue(user).addOnSuccessListener {
            SharedPref.saveUserData(this@EditProfileActivity, user)
            Utils.showMessage(this@EditProfileActivity, "Profile Updated")
            startActivity(Intent(this@EditProfileActivity, HomeMainActivity::class.java))
        }.addOnFailureListener {
            Utils.showMessage(this@EditProfileActivity, "Something went wrong")
        }



    }


    private fun setUserData(userData: UserModel?) {
        if(userData != null) {
            binding.apply {
                etEmailId.setText(userData.email)
                etFirstName.setText(userData.firstName)
                etLastName.setText(userData.lastName)
                etPhoneNumber.setText(userData.phoneNumber)
            }
        } else {
            Utils.showMessage(this@EditProfileActivity, "Something went wrong")
        }

    }
}