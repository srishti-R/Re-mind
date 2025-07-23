package com.example.re_mind.android.ui

import ReminderEntity
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Locale
import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.re_mind.android.db.AppDatabase
import com.example.re_mind.android.db.GeofenceEntity
import com.example.re_mind.android.geofence.GeofenceManager
import com.example.re_mind.android.utils.PermissionBox
import com.example.re_mind.android.viewmodel.MyViewModel
import com.google.android.gms.location.Geofence
import com.google.maps.android.compose.MapUiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    var location = SearchedLocation(LatLng(1.35, 103.87))
                    navController.currentBackStackEntry?.savedStateHandle?.getLiveData<SearchedLocation>(
                        "key"
                    )
                        ?.observe(this@MainActivity) { searchedLocation ->
                            location = searchedLocation
                        }
                    MyApplicationTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            ReminderMainScreen(navController, modifier = Modifier, location)
                        }
                    }
                }
                composable(
                    route = "location_picker"
                ) {
                    GeofencingScreen(navController)
                }

            }
        }
    }


    data class Reminder(var id: String, var title: String, var time: String, var location: SearchedLocation) {
        fun toReminderEntity(): ReminderEntity {
            return ReminderEntity(
                geofenceId = id,
                message = title,
                creationTimeMillis = System.currentTimeMillis(),
                triggerTimeMillis = null,
                isEnabled = true,
                isTriggered = false
            )
        }
    }

    @Composable
    fun ReminderMainScreen(navController: NavController, modifier: Modifier = Modifier, location: SearchedLocation?) {
        val context = LocalContext.current
        val geofenceManager = remember { GeofenceManager(context) }
        val reminder = Reminder(Random.nextInt().toString(), "title", "time", location ?: SearchedLocation(LatLng(1.35, 103.87)))
        val viewModel: MyViewModel = viewModel()
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create New Reminder",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SimpleTextField("What?", "Call mom", Icons.Filled.Info, onTextChange = { changedText ->
                reminder.title = changedText
            })
            DateTimePickerTextField(onDateTimeSelected = { dateAndTimeText ->
                reminder.time = dateAndTimeText
            })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        navController.navigate("location_picker")
                    })
            ) {
                OutlinedTextField(
                    value = location?.address ?: "Select Location",
                    onValueChange = { /* Do nothing, it's read-only */ },
                    label = { Text("place") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = "Location"
                        )
                    },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            SimpleTextField("How? (Optional)", "by mobile", Icons.Filled.Create, onTextChange = { changedText ->
                })
            val scope = rememberCoroutineScope()
            Button(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            if (reminder.title.isBlank() or (reminder.time.isBlank() and (reminder.location.address?.isBlank() == true))) {
                                withContext(Dispatchers.Main)  {
                                    showToast("Hey please fill the reminder correctly")
                                }
                                return@withContext
                            }

                            viewModel.addReminder(reminderDao = AppDatabase.getInstance(context).reminderDao(), newReminder = reminder.toReminderEntity())
                            val geofenceToSave = GeofenceEntity(
                                id = reminder.id,
                                latitude = location?.latLng?.latitude ?: 0.0,
                                longitude = location?.latLng?.longitude ?: 0.0,
                                address = location?.address,
                                transitionType = Geofence.GEOFENCE_TRANSITION_ENTER,
                                expirationTimeMillis = null,
                                registeredTimeMillis = System.currentTimeMillis()
                            )
                            viewModel.saveAndRegisterGeofence(geofenceToSave, geofenceManager)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Save Reminder")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DateTimePickerTextField(
        modifier: Modifier = Modifier,
        label: String = "Select Date and Time",
        onDateTimeSelected: (String) -> Unit
    ) {
        var selectedDate by rememberSaveable { mutableStateOf<Calendar?>(null) }
        var selectedTime by rememberSaveable { mutableStateOf<Calendar?>(null) } // Stores hour and minute

        var showDatePicker by rememberSaveable { mutableStateOf(false) }
        var showTimePicker by rememberSaveable { mutableStateOf(false) }


        val dateFormatter = rememberSaveable {
            SimpleDateFormat(
                "EEE, MMM d, yyyy",
                Locale.getDefault()
            )
        }
        val timeFormatter = rememberSaveable { SimpleDateFormat("h:mm a", Locale.getDefault()) }

        val combinedDateTimeText = rememberSaveable(selectedDate, selectedTime) {
            val dateStr = selectedDate?.let { dateFormatter.format(it.time) } ?: "Select Date"
            val timeStr = selectedTime?.let { timeFormatter.format(it.time) } ?: "Select Time"
            if (selectedDate != null && selectedTime != null) {
                "$dateStr at $timeStr"
            } else if (selectedDate != null) {
                dateStr
            } else {
                label // Show initial label if nothing is selected
            }
        }

        // State for DatePickerDialog

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.timeInMillis ?: System.currentTimeMillis(),
            )


        // State for TimePickerDialog
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime?.get(Calendar.HOUR_OF_DAY) ?: Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedTime?.get(Calendar.MINUTE) ?: Calendar.getInstance()
                .get(Calendar.MINUTE),
            is24Hour = false // Or true, based on your preference
        )

        // --- UI ---
        OutlinedTextField(
            value = combinedDateTimeText,
            onValueChange = { /* Read-only, value changes via pickers */ },
            label = { Text(if (selectedDate != null || selectedTime != null) label else "Date and Time") },
            leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = "Select Date") },
            readOnly = true,
            modifier = modifier
                .fillMaxWidth()
                .pointerInput(selectedDate) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            showDatePicker = true
                        }
                    }
                }
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },

                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newSelectedDate =
                                Calendar.getInstance().apply { timeInMillis = millis }
                            selectedDate = newSelectedDate
                            showTimePicker = true // Show time picker after date is confirmed
                        }
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    dateValidator = { millis ->
                        millis >= System.currentTimeMillis()
                    }
                )
            }
        }

        // --- Time Picker Dialog ---
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                modifier = Modifier.wrapContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            showTimePicker = false
                            val newSelectedTime = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            selectedTime = newSelectedTime

                            selectedDate?.let { finalDateCal ->
                                finalDateCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                finalDateCal.set(Calendar.MINUTE, timePickerState.minute)
                                finalDateCal.set(
                                    Calendar.SECOND,
                                    0
                                )
                                finalDateCal.set(Calendar.MILLISECOND, 0)
                                onDateTimeSelected(combinedDateTimeText)
                            }
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SimpleTextField(label: String, placeholder: String, leadingIcon: ImageVector? = null, onTextChange: (String) -> Unit) {
        var text by rememberSaveable { mutableStateOf("") }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it
                onTextChange(it) },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                if (leadingIcon != null) {
                    Icon(imageVector = leadingIcon, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }


    data class SearchedLocation(
        val latLng: LatLng?,
        val address: String? = null
    ): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readParcelable(LatLng::class.java.classLoader),
            parcel.readString()
        )

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(p0: Parcel, p1: Int) {
            p0.writeParcelable(latLng, p1)
            p0.writeString(address)
        }

        companion object CREATOR : Parcelable.Creator<SearchedLocation> {
            override fun createFromParcel(parcel: Parcel): SearchedLocation {
                return SearchedLocation(parcel)
            }

            override fun newArray(size: Int): Array<SearchedLocation?> {
                return arrayOfNulls(size)
            }
        }
    }

    @Composable
    fun GeofencingScreen(navController: NavController) {
        val permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        // Requires at least coarse permission
        PermissionBox(
            permissions = permissions,
            requiredPermissions = listOf(permissions.first()),
        ) {
            // For Android 10 onwards, we need background permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionBox(
                    permissions = listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                ) {
                    MapWithSearchScreen(navController = navController)
                }
            } else {
                MapWithSearchScreen(navController)
            }
        }
    }

    @Composable
    fun MapWithSearchScreen(navController: NavController) {
        val context = LocalContext.current
        var geofenceTransitionEventInfo by remember {
            mutableStateOf("")
        }
        var searchQuery by remember { mutableStateOf("") }
        var searchedLocationMarker by remember { mutableStateOf<SearchedLocation?>(null) }
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(1.35, 103.87), 10f) // Default to Singapore
        }

        // Coroutine scope for launching geocoding tasks
        val coroutineScope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        fun performSearch(query: String) {
            if (query.isBlank()) return

            keyboardController?.hide()
            focusManager.clearFocus()

            coroutineScope.launch {
                try {
                    val geocoder = Geocoder(context)
                    // Using withContext to move geocoding to a background thread
                    val addresses: List<Address>? = try {
                        // Geocoder.getFromLocationName with a GeocodeListener is available on API 33+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            suspendCancellableCoroutine { continuation ->
                                geocoder.getFromLocationName(query, 1,
                                    object : GeocodeListener {
                                        override fun onGeocode(results: MutableList<Address>) { // Note: results is non-null here
                                            if (continuation.isActive) {
                                                continuation.resume(results.toList()) // Resume with the list
                                            }
                                        }

                                        override fun onError(errorMessage: String?) {
                                            if (continuation.isActive) {
                                                Log.e("MapWithSearchScreen", "Geocoding error for query '$query': $errorMessage")
                                                continuation.resumeWithException(IOException("Geocoding failed: $errorMessage"))
                                            }
                                        }
                                    }
                                )
                                continuation.invokeOnCancellation {
                                    Log.d("MapWithSearchScreen", "Geocoding for '$query' was cancelled.")
                                }
                            }
                        } else {
                            withContext(Dispatchers.IO) {
                                try {
                                    @Suppress("DEPRECATION")
                                    geocoder.getFromLocationName(query, 1)?.toList()
                                } catch (e: IOException) {
                                    Log.e("MapWithSearchScreen", "Geocoding (legacy) failed for query: $query", e)
                                    null
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MapWithSearchScreen", "Geocoding failed for query: $query", e)
                         withContext(Dispatchers.Main) { Toast.makeText(context, "Error finding location", Toast.LENGTH_SHORT).show() }
                        null
                    }

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val latLng = LatLng(address.latitude, address.longitude)
                        searchedLocationMarker = SearchedLocation(latLng, address.getAddressLine(0))
                        cameraPositionState.animate(
                            update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                            durationMs = 1000
                        )
                    } else {
                        Log.w("MapWithSearchScreen", "No location found for query: $query")
                        // Handle no results (e.g., show a Toast)
                         withContext(Dispatchers.Main) { Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show() }
                        searchedLocationMarker = null
                    }
                } catch (e: Exception) { // Catch any other unexpected errors
                    Log.e("MapWithSearchScreen", "An error occurred during search: $query", e)
                     withContext(Dispatchers.Main) { Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show() }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                searchedLocationMarker?.let { location ->
                    Marker(
                        state = MarkerState(position = location.latLng ?: LatLng(1.35, 103.87)),
                        title = "Searched Location",
                        snippet = location.address
                    )
                }
            }

            // Search Bar - aligned to the top
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 8.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    label = { Text("Search Location") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            performSearch(searchQuery)
                        }
                    )
                )
            }
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                onClick = {
                    searchedLocationMarker?.let {
                        navController.previousBackStackEntry?.savedStateHandle?.set("key", it)
                        navController.popBackStack()
                    }

                }
            ) {
                Text("Confirm Location")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}