package com.ludito.task.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludito.task.data.bookmark.dto.LocationDto
import com.ludito.task.domain.bookmark.repository.BookmarkRepository
import com.ludito.task.presentation.map.model.MapEvent
import com.ludito.task.presentation.map.model.MapUiState
import com.ludito.task.presentation.map.model.PlaceItem
import com.ludito.task.presentation.map.model.TypeSpecificState
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.directions.driving.VehicleType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.geo.PolylineIndex
import com.yandex.mapkit.geometry.geo.PolylineUtils
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.search.BusinessObjectMetadata
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.runtime.Error
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class MapViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val searchFactory =
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)

    private val drivingRouter =
        DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)


    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState>
        get() = _uiState


    private val _events = Channel<MapEvent>()
    val event = _events.receiveAsFlow()

    /**
     * Saves a place as a bookmarked address in the repository.
     *
     * @param placeItem The place to save, containing location coordinates, name, and description.
     *
     * Converts `PlaceItem` to `LocationDto` and saves it. Defaults latitude and longitude to `0.0` if null.
     */
    fun saveAddress(placeItem: PlaceItem) {
        bookmarkRepository.insertLocation(
            LocationDto(
                placeItem.location?.latitude ?: 0.0,
                placeItem.location?.longitude ?: 0.0,
                name = placeItem.name,
                placeItem.description,
            )
        )
    }


    /**
     * Searches for a location using the provided parameters.
     *
     * @param zoom Optional zoom level for point-based searches.
     * @param searchType The type of search (e.g., GEO or BIZ).
     * @param point Optional geographic point for GEO searches.
     * @param keyword Optional search term for keyword-based searches.
     * @param visibleRegion Optional map region to narrow keyword searches.
     *
     * Uses Yandex MapKit's `searchFactory` to perform GEO or keyword searches
     * and updates location data or search results accordingly. Triggers an error event on failure.
     */
    fun searchLocation(
        zoom: Int? = null,
        searchType: Int,
        point: Point? = null,
        keyword: String? = null,
        visibleRegion: VisibleRegion? = null
    ) {
        val searchSessionListener = object : Session.SearchListener {
            override fun onSearchResponse(p0: Response) {
                if (searchType == SearchType.GEO.value)
                    setLocationData(
                        name = p0.collection.children[0].obj?.name,
                        descriptionText = p0.collection.children[0].obj?.descriptionText,
                        point = point,
                        geoObjectType = getGeoObjectType(p0.collection.children[0].obj!!)
                    )
                else {
                    setSearchedLocationsList(p0.collection.children.mapNotNull { it.obj })
                }
            }

            override fun onSearchError(p0: Error) {
                sendErrorEvent("Search Error")
            }
        }

        if (searchType == SearchType.GEO.value && point != null) {
            searchFactory.submit(point, zoom, SearchOptions().apply {
                searchTypes = searchType
                resultPageSize = 47
            }, searchSessionListener)
        } else if (keyword != null && visibleRegion != null) {
            searchFactory.submit(
                keyword,
                VisibleRegionUtils.toPolygon(visibleRegion),
                SearchOptions().apply {
                    searchTypes = searchType
                    resultPageSize = 5
                },
                searchSessionListener
            )
        }
    }

    /**
     * Updates the UI state with the provided last known location.
     *
     * @param point Optional geographic point representing the last location.
     *
     * Resets other UI state properties, including title, description, location,
     * and list of locations, while setting the last location to the provided point.
     */
    fun setLastLocationData(
        point: Point? = null
    ) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                title = null,
                descriptionText = null,
                location = null,
                lastLocation = point,
                listLocations = emptyList(),
            )
        }
    }


    /**
     * Updates the UI state with the provided location details.
     *
     * @param name Optional name of the location.
     * @param descriptionText Optional description of the location.
     * @param geoObjectType The type of geographic object (default is Undefined).
     * @param point Optional geographic coordinates of the location.
     *
     * Sets the title, description, location, and geographic object type in the UI state.
     * Clears the list of locations.
     */
    fun setLocationData(
        name: String? = null,
        descriptionText: String? = null,
        geoObjectType: TypeSpecificState = TypeSpecificState.Undefined,
        point: Point? = null
    ) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                title = name,
                descriptionText = descriptionText,
                location = point,
                geoObjectType = geoObjectType,
                listLocations = emptyList()
            )
        }
    }

    /**
     * Updates the UI state with a list of searched locations and their details.
     *
     * @param geoObjectList A list of `GeoObject` representing the locations to be processed.
     */
    private fun setSearchedLocationsList(geoObjectList: List<GeoObject>) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true
                )
            }
            val list = mutableListOf<PlaceItem>()
            geoObjectList.forEach { geoObject ->

                val routes = getDrivingRoutesBetweenPoints(
                    start = uiState.value.lastLocation!!,
                    end = geoObject.geometry[0].point!!
                )

                val distance = getMinimumDistanceByRoute(
                    start = uiState.value.lastLocation!!,
                    end = geoObject.geometry[0].point!!,
                    routes
                ) / 10

                list.add(
                    PlaceItem(
                        name = geoObject.name ?: "",
                        description = geoObject.descriptionText ?: "",
                        distance = distance.toInt(),
                        geoObjectType = getGeoObjectType(geoObject),
                        location = geoObject.geometry[0].point,
                    )
                )
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    listLocations = list
                )
            }
        }

    }

    /**
     * Retrieves the type-specific information for a given `GeoObject`.
     *
     * @param geoObject The `GeoObject` to extract the type from.
     * @return A `TypeSpecificState` representing the geo object type (Toponym, Business, or Undefined).
     *
     * The function checks the metadata of the `GeoObject` and returns either:
     * - `TypeSpecificState.Toponym` with the formatted address for toponyms.
     * - `TypeSpecificState.Business` with business details like name, working hours, categories, phones, and link.
     * - `TypeSpecificState.Undefined` if neither type is found.
     */
    private fun getGeoObjectType(geoObject: GeoObject): TypeSpecificState {
        return geoObject.metadataContainer.getItem(ToponymObjectMetadata::class.java)?.let {
            TypeSpecificState.Toponym(address = it.address.formattedAddress)
        } ?: geoObject.metadataContainer.getItem(BusinessObjectMetadata::class.java)
            ?.let { item ->
                TypeSpecificState.Business(
                    name = item.name,
                    workingHours = item.workingHours?.text,
                    categories = item.categories.map { it.name }
                        .takeIf { it.isNotEmpty() }?.toSet()
                        ?.joinToString(", "),
                    phones = item.phones.map { it.formattedNumber }
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString(", "),
                    link = item.links.firstOrNull()?.link?.href,
                )
            } ?: TypeSpecificState.Undefined
    }

    /**
     * Fetches driving routes between two points asynchronously.
     *
     * @param start The starting point of the route.
     * @param end The destination point of the route.
     * @return A list of `DrivingRoute` objects representing the possible routes between the points.
     */
    private suspend fun getDrivingRoutesBetweenPoints(
        start: Point,
        end: Point
    ): List<DrivingRoute> = suspendCoroutine { continuation ->
        val points = buildList {
            add(RequestPoint(start, RequestPointType.WAYPOINT, null, null))
            add(RequestPoint(end, RequestPointType.WAYPOINT, null, null))
        }
        drivingRouter.requestRoutes(
            points,
            DrivingOptions().apply { routesCount = 3 },
            VehicleOptions().apply { vehicleType = VehicleType.DEFAULT },
            object : DrivingSession.DrivingRouteListener {
                override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
                    continuation.resume(drivingRoutes)
                }

                override fun onDrivingRoutesError(error: Error) {
                    sendErrorEvent("Route error")
                }
            }
        )
    }

    /**
     * Calculates the minimum distance between two points from a list of routes.
     *
     * @param start The starting point of the route.
     * @param end The destination point of the route.
     * @param routes A list of `DrivingRoute` objects to calculate distances from.
     * @return The minimum distance between the start and end points across all routes, or 0.0 if no valid distances are found.
     */
    private fun getMinimumDistanceByRoute(
        start: Point,
        end: Point,
        routes: List<DrivingRoute>
    ): Double {
        val distances = mutableListOf<Double>()
        routes.forEach { route ->
            distances.add(distanceBetweenPointsOnRoute(route = route, start = start, end = end))
        }
        return distances.filter { it > 0.0 }.minOrNull() ?: 0.0
    }

    /**
     * Calculates the distance between two points along a route.
     *
     * @param route The `DrivingRoute` containing the geometry of the route.
     * @param start The starting point of the distance calculation.
     * @param end The destination point of the distance calculation.
     * @return The distance between the start and end points on the route, or 0.0 if positions cannot be found.
     *
     * Finds the closest points on the route to the start and end points, then calculates the distance
     * between those points using the route's geometry.
     */
    private fun distanceBetweenPointsOnRoute(
        route: DrivingRoute,
        start: Point,
        end: Point
    ): Double {
        val polylineIndex = PolylineUtils.createPolylineIndex(route.geometry)

        val firstPosition = polylineIndex.closestPolylinePosition(
            start,
            PolylineIndex.Priority.CLOSEST_TO_RAW_POINT,
            100.0
        )
        val secondPosition = polylineIndex.closestPolylinePosition(
            end,
            PolylineIndex.Priority.CLOSEST_TO_RAW_POINT,
            100.0
        )

        return if (secondPosition != null && firstPosition != null)
            PolylineUtils.distanceBetweenPolylinePositions(
                route.geometry,
                firstPosition,
                secondPosition
            )
        else 0.0
    }

    /**
     * Sends an error event to the UI.
     *
     * @param error A string describing the error.
     */
    fun sendErrorEvent(error: String) {
        viewModelScope.launch {
            _events.send(MapEvent.Error(error))
        }
    }
}