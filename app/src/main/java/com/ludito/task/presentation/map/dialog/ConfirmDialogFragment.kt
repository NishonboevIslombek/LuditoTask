package com.ludito.task.presentation.map.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.ludito.task.R


class ConfirmDialogFragment(
    private val onConfirmClicked: (String) -> Unit,
    private val locationName: String
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_confirm_dialog, null)
            builder.setView(view)

            val txtCancel = view.findViewById<TextView>(R.id.txt_cancel)
            val txtConfirm = view.findViewById<TextView>(R.id.txt_confirm)
            val etLocation = view.findViewById<EditText>(R.id.et_location_name)

            etLocation.setText(locationName)
            txtCancel.setOnClickListener { this.dismiss() }
            txtConfirm.setOnClickListener {
                onConfirmClicked(etLocation.text.toString())
                this.dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}