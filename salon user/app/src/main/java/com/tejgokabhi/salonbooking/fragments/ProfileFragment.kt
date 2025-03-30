package com.tejgokabhi.salonbooking.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.activities.AuthActivity
import com.tejgokabhi.salonbooking.activities.EditProfileActivity
import com.tejgokabhi.salonbooking.activities.PolicyActivity
import com.tejgokabhi.salonbooking.activities.ViewBookingsActivity
import com.tejgokabhi.salonbooking.databinding.DialogDesignBinding
import com.tejgokabhi.salonbooking.databinding.FragmentProfileBinding
import com.tejgokabhi.salonbooking.model.UserModel
import com.tejgokabhi.salonbooking.preference.SharedPref
import com.tejgokabhi.salonbooking.utils.Constants
import com.tejgokabhi.salonbooking.utils.Utils


class ProfileFragment : Fragment() {
    private val binding by lazy { FragmentProfileBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var userId: String = ""
    private var user: UserModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        userId = auth.currentUser!!.uid
        user  = SharedPref.getUserData(requireContext())

        getUserData()

        binding.ivNotificationBack.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_Profile_to_navigation_home)
        }

        binding.tvDeleteAccount.setOnClickListener {
            deleteAccount()
        }

        binding.layEdit.setOnClickListener {
            if(user != null) {
                val intent = Intent(requireActivity(), EditProfileActivity::class.java)
                startActivity(intent)
            } else {
                Utils.showMessage(requireContext(), "Something went wrong")
            }
        }

        binding.layBooking.setOnClickListener {
            startActivity(Intent(requireActivity(), ViewBookingsActivity::class.java))
        }

        binding.layContact.setOnClickListener {
            Utils.openEmailApp(requireContext(), "appsupport@gmail.com")
        }


        binding.layPrivacy.setOnClickListener {
            startActivity(Intent(requireActivity(), PolicyActivity::class.java))
        }

        binding.btSignOut.setOnClickListener {
            logout()
        }

        binding.layShareApp.setOnClickListener {
            val app = "Download now https://play.google.com/store/apps/details?id=${requireActivity().packageName}"
            Utils.shareText(requireContext(), app)
        }

        binding.layAbout.setOnClickListener {
            val description = getString(R.string.aboutUs)
            val buttonText = "Close"
            val title = "About Us"
            Utils.showDialog(requireContext(), title, description, buttonText, false) {
                val dialogDesign = DialogDesignBinding.inflate(layoutInflater)
                dialogDesign.btConfirm.visibility = View.GONE
            }
        }
    }


    private fun logout() {
        if(auth.currentUser != null) {
            val title = "Logout Your Account"
            val description = "Do you really want to logout, We will miss you... if you still want to cancel this process else you can click logout to proceed"
            val confirmBtnText ="Logout"
            if(auth.currentUser != null) {
                Utils.showDialog(
                    requireContext(), title, description, confirmBtnText) {
                    auth.signOut()
                    SharedPref.clearData(requireContext())
                    Toast.makeText(requireContext(), "Account Logout Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), AuthActivity::class.java))
                }
            }
        } else {
            Toast.makeText(requireContext(), "User Not Login", Toast.LENGTH_SHORT).show()

        }
    }

    private fun deleteAccount() {
        val title = "Delete Your Account"
        val description = "Are you sure to Delete Car store account? Once you delete, you can't retrieve your action."
        val confirmBtnText ="Delete"
        if(Firebase.auth.currentUser != null) {
            Utils.showDialog(requireContext(), title, description, confirmBtnText) {
                val user = auth.currentUser
                user?.delete()?.addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        val databaseReference = database.getReference(Constants.USER_REF).child(userId)
                        databaseReference.removeValue()
                            .addOnSuccessListener {
                                SharedPref.clearData(requireContext())
                                Utils.showMessage(requireContext(), "User deleted successfully")
                                startActivity(Intent(requireContext(), AuthActivity::class.java))
                            }
                            .addOnFailureListener {
                                Utils.showMessage(requireContext(), "Failed to delete user data")
                            }
                    } else {
                        Utils.showMessage(requireContext(), "Failed to delete user account: ${task.exception?.message}")
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "User Not Logged In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserData() {
        if(user != null) {
            binding.tvFirstName.text = "${user!!.firstName} ${user!!.lastName}"
            val address = "${user!!.phoneNumber}\n${user!!.email}"
            binding.tvAddress.text = address
            val profileName = "${user!!.firstName.first()}${user!!.lastName.first()}"
            binding.tvProfile.text = profileName.uppercase()
        } else {
            Utils.showMessage(requireContext(), "Something went wrong")
        }

    }
}