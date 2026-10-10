package com.example.cal2hubapp.ui.auth

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cal2hubapp.R
import com.example.cal2hubapp.data.AuthRepository
import com.example.cal2hubapp.databinding.FragmentLoginBinding
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository(requireContext())

        binding.errorTextView.gravity = Gravity.CENTER

        binding.loginButton.setOnClickListener {
            performLogin()
        }

        binding.registerLinkTextView.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

        binding.forgotPasswordLinkTextView.setOnClickListener {
            showForgotPasswordDialogs()
        }
    }

    private fun performLogin() {
        val username = binding.usernameEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields")
            return
        }

        if (authRepository.login(username, password)) {
            Toast.makeText(requireContext(), "Successful Log in", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.mainFragment)
        } else {
            showError("Invalid username or password")
        }
    }

    private fun showForgotPasswordDialogs() {
        val verifyUserView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_verify_user, null)
        val usernameEditText = verifyUserView.findViewById<TextInputEditText>(R.id.usernameEditText)
        val birthdayPicker = verifyUserView.findViewById<DatePicker>(R.id.birthdayPicker)
        val favoriteFoodEditText = verifyUserView.findViewById<TextInputEditText>(R.id.favoriteFoodEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Verify Your Identity")
            .setView(verifyUserView)
            .setPositiveButton("Verify") { _, _ ->
                val username = usernameEditText.text.toString().trim()
                val favoriteFood = favoriteFoodEditText.text.toString().trim()
                val day = birthdayPicker.dayOfMonth
                val month = birthdayPicker.month
                val year = birthdayPicker.year
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val birthday = sdf.format(calendar.time)

                if (username.isEmpty() || favoriteFood.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter your username and favorite food", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val user = authRepository.getUserByUsername(username)
                if (user == null) {
                    Toast.makeText(requireContext(), "Username not found", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (user.birthday != birthday || user.favoriteFood?.equals(favoriteFood, ignoreCase = true) != true) {
                    Toast.makeText(requireContext(), "Incorrect information, please try again", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // If verification is successful, show the reset password dialog
                showResetPasswordDialog(username)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetPasswordDialog(username: String) {
        val resetPasswordView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_password, null)
        val newPasswordEditText = resetPasswordView.findViewById<TextInputEditText>(R.id.newPasswordEditText)
        val confirmPasswordEditText = resetPasswordView.findViewById<TextInputEditText>(R.id.confirmPasswordEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Set New Password")
            .setView(resetPasswordView)
            .setPositiveButton("Reset") { _, _ ->
                val newPassword = newPasswordEditText.text.toString().trim()
                val confirmPassword = confirmPasswordEditText.text.toString().trim()

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all password fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword.length < 6) {
                    Toast.makeText(requireContext(), "New password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (authRepository.updatePassword(username, newPassword)) {
                    Toast.makeText(requireContext(), "Password reset successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to reset password", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        binding.errorTextView.text = message
        binding.errorTextView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
