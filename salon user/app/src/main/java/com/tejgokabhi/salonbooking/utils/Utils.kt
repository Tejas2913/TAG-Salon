package com.tejgokabhi.salonbooking.utils

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.navigation.Navigation
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.databinding.DialogDesignBinding
import com.tejgokabhi.salonbooking.databinding.DialogProgressBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Utils {
    companion object {

        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

        fun navigate(view: View, id: Int) {
            Navigation.findNavController(view).navigate(id)
        }

        fun openEmailApp(context: Context, emailAddress: String) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$emailAddress")
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))

            }
            context.startActivity(intent)

        }

        fun getCurrentTime(): String {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            return currentDateTime.format(formatter)
        }

        fun showLoading(context: Context): AlertDialog {
            val progress = AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
            val processLayout = DialogProgressBinding.inflate(LayoutInflater.from(context))
            progress.setView(processLayout.root)
            progress.setCancelable(false)
            return progress
        }

        fun showDialog(
            context: Context, title: String, description: String,
            confirmButtonText: String, visibility: Boolean = true, confirmButtonAction: () -> Unit
        ) {
            val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
            val dialogLayout = DialogDesignBinding.inflate(LayoutInflater.from(context))
            dialog.setView(dialogLayout.root)
            dialog.setCancelable(true)

            dialogLayout.dialogBody.text = description
            dialogLayout.dialogTitle.text = title
            dialogLayout.btConfirm.text = confirmButtonText

            dialogLayout.btCancle.setOnClickListener {
                dialog.dismiss()
            }
            dialogLayout.btConfirm.setOnClickListener {
                confirmButtonAction()
                dialog.dismiss()
            }

            if(visibility) {
                dialogLayout.btCancle.visibility = View.VISIBLE
            } else {
                dialogLayout.btCancle.visibility = View.GONE
            }

            dialog.show()
        }


        fun shareTextToWhatsApp(context: Context, text: String) {
            val whatsappIntent = Intent(Intent.ACTION_SEND)
            whatsappIntent.type = "text/plain"
            whatsappIntent.setPackage("com.whatsapp")

            whatsappIntent.putExtra(Intent.EXTRA_TEXT, text)

            try {
                context.startActivity(whatsappIntent)
            } catch (e: ActivityNotFoundException) {

                Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
            }
        }


        fun showMessage(context: Context, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        fun copyTextToClipboard(context: Context, text: String) {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text label", text)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied Successfully", Toast.LENGTH_SHORT).show()
        }

        fun shareText(context: Context, text: String) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
            context.startActivity(Intent.createChooser(shareIntent, "Share Text"))
        }



        fun openWhatsAppChatWithMessage(context: Context, countryCode: String, phoneNumber: String, message: String) {
            val number = "$countryCode$phoneNumber"
            val formattedPhoneNumber = countryCode + phoneNumber.replace("+", "").replace(" ", "").trim()


            val uri = Uri.parse("https://wa.me/$formattedPhoneNumber?text=${Uri.encode(message)}")


            val intent = Intent(Intent.ACTION_VIEW, uri)

            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "WhatsApp is not installed on your device", Toast.LENGTH_SHORT).show()
            }
        }

        fun rateUs(context: Context) {
            try {
                val marketUri = Uri.parse("market://details?id=${context.packageName}")
                val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                context.startActivity(marketIntent)
            } catch (e: ActivityNotFoundException) {
                val marketUri =
                    Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                context.startActivity(marketIntent)
            }
        }

    }

}