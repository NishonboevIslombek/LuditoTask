package com.ludito.task.presentation.map.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ludito.task.R
import com.ludito.task.databinding.FragmentSearchDialogBinding
import com.ludito.task.presentation.map.dialog.adapter.SearchRecyclerAdapter
import com.ludito.task.presentation.map.model.PlaceItem

class SearchDialogFragment(
    val onPlaceClicked: (item: PlaceItem) -> Unit,
    val onSearchClicked: (String) -> Unit,
    val onDialogDismissed: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentSearchDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: BottomSheetDialog

    private lateinit var adapter: SearchRecyclerAdapter

    private var listPlaces: List<PlaceItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchDialogBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initAdapter()
    }

    /**
     * Initializes the view by setting up listeners for the search input field.
     *
     * - Triggers an initial search with an empty query.
     * - Adds a text changed listener to trigger a search when the text is cleared.
     * - Sets an editor action listener to trigger a search when the "search" action on the keyboard is pressed.
     */
    private fun initView() = with(binding) {
        onSearchClicked("")

        etMap.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                onSearchClicked("")
            }
        }
        etMap.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                onSearchClicked(etMap.text.toString())
            }
            true
        }
    }

    private fun initAdapter() = with(binding) {
        adapter = SearchRecyclerAdapter(onItemClicked = {
            onPlaceClicked(it)
            dialog.dismiss()
            onDialogDismissed()
        })
        rvPlaces.layoutManager = LinearLayoutManager(requireContext())
        rvPlaces.adapter = adapter

        if (listPlaces.isNotEmpty()) adapter.setList(listPlaces)
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

    /**
     * Sets the height of the given bottom sheet view to 80% of the screen height.
     *
     * @param bottomSheet The `View` representing the bottom sheet whose height will be adjusted.
     */
    private fun setupHeight(bottomSheet: View) {
        val metrics = resources.displayMetrics
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = (metrics.heightPixels * 0.8).toInt()
        bottomSheet.layoutParams = layoutParams
    }

    fun setList(list: List<PlaceItem>) {
        binding.rvPlaces.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        binding.txtEmptyAddress.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        adapter.setList(list)
    }

    /**
     * Builder class for creating a `SearchDialogFragment` with custom configuration.
     *
     * This builder allows setting a list of places and custom actions for place clicks,
     * search input, and dialog dismissal.
     *
     * @param onPlaceClicked Action to execute when a place is clicked.
     * @param onSearchClicked Action to execute when the search is clicked with the provided keyword.
     * @param onDialogDismissed Action to execute when the dialog is dismissed.
     */
    class Builder(
        onPlaceClicked: (item: PlaceItem) -> Unit,
        onSearchClicked: (String) -> Unit,
        onDialogDismissed: () -> Unit
    ) {
        private val dialog =
            SearchDialogFragment(
                onPlaceClicked = onPlaceClicked,
                onSearchClicked = onSearchClicked,
                onDialogDismissed = onDialogDismissed
            )

        fun setPlacesList(list: List<PlaceItem>): Builder {
            dialog.setList(list)
            return this
        }

        fun build(): SearchDialogFragment {
            return dialog
        }
    }

}