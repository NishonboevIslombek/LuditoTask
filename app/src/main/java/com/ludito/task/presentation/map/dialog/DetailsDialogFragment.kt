package com.ludito.task.presentation.map.dialog


import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ludito.task.R
import com.ludito.task.databinding.FragmentDetailsDialogBinding


class DetailsDialogFragment(
    val onDialogDismissed: () -> Unit, val onAddClicked: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentDetailsDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: BottomSheetDialog

    private var _name: String = ""
    private var _description: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgClose.setOnClickListener {
            dialog.dismiss()
            onDialogDismissed()
        }

        binding.btnAdd.setOnClickListener {
            dialog.dismiss()
            onAddClicked()
        }

        if (_name.isNotEmpty()) binding.txtName.text = _name.trim()
        if (_description.isNotEmpty()) binding.txtDescription.text = _description.trim()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_FRAME, R.style.BottomSheetDialog)
        dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { view ->
                val behaviour = BottomSheetBehavior.from(view)
                setupHeight(view)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                behaviour.isDraggable = true
            }
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDialogDismissed()
    }

    fun setName(name: String) {
        _name = name
    }

    fun setDescription(description: String) {
        _description = description
    }

    private fun setupHeight(bottomSheet: View) {
        val metrics = resources.displayMetrics
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = (metrics.heightPixels * 0.25).toInt()
        bottomSheet.layoutParams = layoutParams
    }

    /**
     * Builder class for creating a `DetailsDialogFragment` with custom configuration.
     *
     * This builder allows setting the name and description for the details dialog, and provides
     * actions for dialog dismissal and the "Add" button click.
     *
     * @param context The context in which the dialog will be shown.
     * @param onDialogDismissed Action to execute when the dialog is dismissed.
     * @param onAddClicked Action to execute when the "Add" button is clicked.
     */
    class Builder(context: Context, onDialogDismissed: () -> Unit, onAddClicked: () -> Unit) {
        private val dialog = DetailsDialogFragment(
            onDialogDismissed = onDialogDismissed,
            onAddClicked = onAddClicked
        )

        fun setName(name: String): Builder {
            dialog.setName(name)
            return this
        }

        fun setDescription(description: String): Builder {
            dialog.setDescription(description)
            return this
        }

        fun build(): DetailsDialogFragment {
            return dialog
        }
    }
}