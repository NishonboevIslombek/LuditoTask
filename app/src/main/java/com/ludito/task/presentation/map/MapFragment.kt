package com.ludito.task.presentation.map

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.LocationServices
import com.ludito.task.R
import com.ludito.task.databinding.FragmentMapBinding
import com.ludito.task.presentation.map.dialog.ConfirmDialogFragment
import com.ludito.task.presentation.map.dialog.DetailsDialogFragment
import com.ludito.task.presentation.map.dialog.SearchDialogFragment
import com.ludito.task.presentation.map.model.MapEvent
import com.ludito.task.presentation.map.model.MapUiState
import com.ludito.task.presentation.map.model.PlaceItem
import com.ludito.task.presentation.map.model.TypeSpecificState
import com.ludito.task.presentation.utils.hasRequiredLocationPermissions
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.SearchType
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val vm: MapViewModel by viewModels()
    private val args: MapFragmentArgs by navArgs()

    private var detailsDialog: DetailsDialogFragment? = null
    private var searchDialog: SearchDialogFragment? = null
    private var confirmDialog: ConfirmDialogFragment? = null

    private lateinit var mapView: MapView
    private lateinit var inputListener: InputListener
    private lateinit var placemark: PlacemarkMapObject

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
        observeEvent()
    }

    private fun initView() = with(binding) {
        this@MapFragment.mapView = mapView
        placemark = mapView.mapWindow.map.mapObjects.addPlacemark()

        if (args.latitude != 0.0f && args.longitude != 0.0f) {
            vm.searchLocation(
                zoom = 0,
                searchType = SearchType.GEO.value,
                point = Point(args.latitude.toDouble(), args.longitude.toDouble())
            )
        }

        if (requireActivity().hasRequiredLocationPermissions() && args.latitude == 0.0f)
            setLastLocation()
        setOnTapLocationListener()

        imgNavigate.setOnClickListener {
            if (requireActivity().hasRequiredLocationPermissions()) {
                setLastLocation()
            } else {
                vm.sendErrorEvent("Location Permissions not granted")
            }
        }

        etMap.setOnClickListener {
            showSearchDialog()
        }
    }

    /**
     * Retrieves the device's last known location using FusedLocationProviderClient and updates the UI state.
     */
    @SuppressLint("MissingPermission")
    private fun setLastLocation() {
        LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation
            .addOnSuccessListener {
                vm.setLastLocationData(
                    point = Point(it.latitude, it.longitude)
                )
            }.addOnFailureListener {
                vm.sendErrorEvent(it.message ?: "")
            }
    }

    /**
     * Sets a listener for map tap events to trigger location search.
     */
    private fun setOnTapLocationListener() {
        inputListener = object : InputListener {
            override fun onMapTap(map: Map, point: Point) {
                vm.searchLocation(
                    searchType = SearchType.GEO.value,
                    point = point,
                    zoom = map.cameraPosition.zoom.toInt()
                )
                binding.etMap.text?.clear()
            }

            override fun onMapLongTap(map: Map, point: Point) {}
        }
        mapView.mapWindow.map.addInputListener(inputListener)
    }


    /**
     * Sets a pin on the map at the specified location and moves the camera to that point.
     *
     * @param point The location where the pin should be placed on the map.
     */
    private fun setLocationPinOnMap(point: Point) {
        placemark.apply {
            geometry = point
            setIcon(
                ImageProvider.fromResource(requireContext(), R.drawable.ic_pin),
                IconStyle().apply { anchor = PointF(0.5f, 1.0f) })

        }

        mapView.mapWindow.map.move(
            CameraPosition(
                /* target */point,
                /* zoom = */ 17.0f,
                /* azimuth = */ 150.0f,
                /* tilt = */ 30.0f
            ), Animation(Animation.Type.SMOOTH, 1f)
        ) {}
    }


    private fun observeData() {
        lifecycleScope.launch {
            vm.uiState.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collect {
                handleData(uiState = it)
            }
        }
    }

    private fun handleData(uiState: MapUiState) {
        if (!uiState.isLoading && uiState.lastLocation != null && uiState.title.isNullOrEmpty() && uiState.listLocations.isEmpty()) {
            setLocationPinOnMap(uiState.lastLocation!!)
        }
        if (!uiState.isLoading && uiState.location != null && uiState.listLocations.isEmpty()) {
            setLocationPinOnMap(uiState.location!!)
            when (val state = uiState.geoObjectType) {
                is TypeSpecificState.Business -> {
                    showDetailsDialog(
                        placeItem = PlaceItem(
                            state.name,
                            uiState.descriptionText!!,
                            location = uiState.location
                        )
                    )
                }

                is TypeSpecificState.Toponym -> {
                    showDetailsDialog(
                        placeItem = PlaceItem(
                            uiState.title!!,
                            state.address,
                            location = uiState.location
                        )
                    )
                }

                TypeSpecificState.Undefined -> {
                    showDetailsDialog(
                        placeItem = PlaceItem(
                            uiState.title!!,
                            uiState.descriptionText!!,
                            location = uiState.location
                        )
                    )
                }
            }
        }
        if (uiState.listLocations.isNotEmpty() && !uiState.isLoading) searchDialog?.setList(uiState.listLocations)
    }

    private fun observeEvent() {
        lifecycleScope.launch {
            vm.event.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collect { event ->
                when (event) {
                    is MapEvent.Error -> {
                        Toast.makeText(requireContext(), event.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Displays a dialog with details of the provided place and handles actions through callbacks.
     *
     * @param placeItem The place whose details will be shown in the dialog.
     *
     * Initializes and shows a `DetailsDialogFragment` with the place's name and description.
     * - The `onDialogDismissed` callback is triggered when the dialog is dismissed.
     * - The `onAddClicked` callback is triggered when the "Add" button is clicked, showing a confirmation dialog for the place.
     */
    private fun showDetailsDialog(placeItem: PlaceItem) {
        detailsDialog = DetailsDialogFragment.Builder(
            requireContext(),
            onDialogDismissed = {},
            onAddClicked = {
                showConfirmDialog(placeItem)
            })
            .setName(placeItem.name)
            .setDescription(placeItem.description)
            .build()
        detailsDialog?.show(childFragmentManager, "")
    }

    /**
     * Displays a search dialog and shows searched addresses.
     *
     * The dialog allows the user to:
     * - Select a place, triggering the `onPlaceClicked` callback to update the UI with the selected place's details.
     * - Perform a search, triggering the `onSearchClicked` callback to initiate a location search within the visible region.
     * - Dismiss the dialog, triggering the `onDialogDismissed` callback to restore the visibility of the map input field.
     */
    private fun showSearchDialog() {
        val visibleRegion = mapView.mapWindow.map.visibleRegion
        searchDialog = SearchDialogFragment.Builder(
            onPlaceClicked = { item ->
                vm.setLocationData(
                    name = item.name,
                    descriptionText = item.description,
                    geoObjectType = item.geoObjectType,
                    point = item.location
                )
                binding.etMap.setText(item.name)
            },
            onSearchClicked = {
                if (requireActivity().hasRequiredLocationPermissions())
                    vm.searchLocation(
                        searchType = SearchType.BIZ.value,
                        keyword = it,
                        visibleRegion = visibleRegion
                    )
                else vm.sendErrorEvent("Searching is not accessible without location permission")
            },
            onDialogDismissed = {
                binding.etMap.visibility = View.VISIBLE
            })
            .build()
        searchDialog?.show(childFragmentManager, "")
        binding.etMap.visibility = View.GONE
    }

    /**
     * Displays a confirmation dialog for saving the provided place.
     *
     * @param placeItem The place whose name will be confirmed and potentially saved.
     */
    private fun showConfirmDialog(placeItem: PlaceItem) {
        confirmDialog = ConfirmDialogFragment(onConfirmClicked = {
            vm.saveAddress(placeItem.copy(name = it))
        }, placeItem.name)
        confirmDialog?.show(childFragmentManager, "")
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}