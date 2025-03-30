package com.tejgokabhi.salonbooking.authfragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.activities.HomeMainActivity
import com.tejgokabhi.salonbooking.databinding.FragmentLoginBinding
import com.tejgokabhi.salonbooking.model.UserModel
import com.tejgokabhi.salonbooking.preference.SharedPref
import com.tejgokabhi.salonbooking.utils.Constants
import com.tejgokabhi.salonbooking.utils.Utils

class LoginFragment : Fragment() {
    private val binding by lazy { FragmentLoginBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var progress: AlertDialog
    private lateinit var googleSignInClient: GoogleSignInClient

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

        binding.btLogin.setOnClickListener {
            login()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        binding.btGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }

        binding.btCreateAc.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.tvForgetPass.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        } else {
            Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(requireContext(), "Sign In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credentials).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser: FirebaseUser? = auth.currentUser
                if (currentUser != null) {
                    navigateToHome()
                }
            } else {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.etEmail.requestFocus()
                binding.etEmail.error = "Empty"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.requestFocus()
                binding.etPassword.error = "Empty"
                false
            }
            else -> true
        }
    }

    private fun login() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        if (validateForm(email, password)) {
            progress.show()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        fetchData()
                    } else {
                        progress.dismiss()
                        Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    progress.dismiss()
                    Utils.showMessage(requireContext(), "Please Check Email and Password")
                }
        }
    }

    private fun fetchData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            progress.dismiss()
            Utils.showMessage(requireContext(), "User not authenticated")
            return
        }

        val usersRef = database.getReference(Constants.USER_REF).child(currentUser.uid)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progress.dismiss()
                if (dataSnapshot.exists()) {
                    val userModel: UserModel? = dataSnapshot.getValue(UserModel::class.java)
                    if (userModel != null) {
                        SharedPref.saveUserData(requireContext(), userModel)
                        navigateToHome()
                    } else {
                        Utils.showMessage(requireContext(), "Failed to fetch user data")
                    }
                } else {
                    Utils.showMessage(requireContext(), "User data not found")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progress.dismiss()
                Utils.showMessage(requireContext(), "Database error: ${databaseError.message}")
            }
        })
    }

    private fun navigateToHome() {
        startActivity(Intent(requireActivity(), HomeMainActivity::class.java))
        requireActivity().finish()
    }
}
