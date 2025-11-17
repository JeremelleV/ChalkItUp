package com.example.chalkitup

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.datastore.preferences.core.Preferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chalkitup.domain.model.UserProfile
import com.example.chalkitup.ui.screens.MainScreen
import com.example.chalkitup.ui.theme.ChalkitupTheme
import com.example.chalkitup.ui.viewmodel.BookingManager
import com.example.chalkitup.ui.viewmodel.OfflineDataManager
import com.example.chalkitup.ui.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.google.gson.Gson
import java.security.MessageDigest
import dagger.hilt.android.AndroidEntryPoint

// Initializes app on launch
// -> launches MainScreen()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OfflineDataManager.init(filesDir)
        BookingManager.init(filesDir)
        val secureStorage = SecureStorage(this)
        val lastUser = secureStorage.getUser()
        // Initialize Firebase and other services
        Firebase.initialize(this)
        Connection(this).isConnected
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val darkTheme by themeViewModel.isDarkTheme

            ChalkitupTheme(darkTheme = darkTheme) {
                Surface {
                    CheckAuthStatus(this, lastUser = lastUser)
                }
            }
        }
    }
}

@Composable
fun CheckAuthStatus(context: Context, lastUser: UserProfile?) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Get network status from the Connection singleton
    val isConnected = Connection.getInstance(context).isConnected

    // Create a state to hold the loaded user profile
    var authUser by remember { mutableStateOf<UserProfile?>(null) }

    // If online and user exists, load user profile
    LaunchedEffect(currentUser, isConnected) {
        if (isConnected && currentUser != null) {
            UserProfile.fromUser(currentUser) { loadedUser ->
                authUser = loadedUser
            }
        }
    }

    // Handle UI based on authentication & connection status
    when {
        isConnected && authUser != null -> MainScreen()
        isConnected && currentUser == null -> MainScreen()
        //offline
        !isConnected && lastUser != null -> MainScreen()
        else -> MainScreen()
    }
}






private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_storage")

class SecureStorage(private val context: Context) {

    private val gson = Gson()
    private val keyEmail = stringPreferencesKey("email")
    private val keyPassword = stringPreferencesKey("password")
    private val keyUserDetail = stringPreferencesKey("userDetail")

    fun setEmail(email: String) {
        runBlocking {
            context.dataStore.edit { it[keyEmail] = email }
        }
    }

    fun getEmail(): String? {
        return runBlocking {
            context.dataStore.data.first()[keyEmail]
        }
    }

    fun setUser(user: UserProfile) {
        val userJson = gson.toJson(user)
        runBlocking {
            context.dataStore.edit { it[keyUserDetail] = userJson }
        }
    }

    fun getUser(): UserProfile? {
        val userJson = runBlocking {
            context.dataStore.data.first()[keyUserDetail]
        }
        return userJson?.let { gson.fromJson(it, UserProfile::class.java) }
    }

    fun setPassword(password: String) {
        val hashedPassword = hashPassword(password)
        runBlocking {
            context.dataStore.edit { it[keyPassword] = hashedPassword }
        }
    }

    fun checkPassword(password: String): Boolean {
        val storedHashedPassword = runBlocking {
            context.dataStore.data.first()[keyPassword]
        }
        return storedHashedPassword == hashPassword(password)
    }

    fun deleteAll() {
        runBlocking {
            context.dataStore.edit {
                it.remove(keyEmail)
                it.remove(keyPassword)
                it.remove(keyUserDetail)
            }
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }
}


class Connection constructor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _connectionStatus = MutableStateFlow(checkConnectivity())
    val connectionStatus: StateFlow<Boolean> get() = _connectionStatus
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _connectionStatus.value = true
        }

        override fun onLost(network: Network) {
            _connectionStatus.value = false
        }
    }
    init {
        // Register the callback to listen for changes in network status
        _connectionStatus.value = checkConnectivity()

        // Register the callback to listen for changes in network status
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback
        )
        Log.d("Login", "ConnectionChecked: ${_connectionStatus.value}")
    }

    // Unregister the callback to prevent memory leaks
    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
    companion object {
        @Volatile
        private var instance: Connection? = null

        fun getInstance(context: Context): Connection {
            return instance ?: synchronized(this) {
                instance ?: Connection(context.applicationContext).also { instance = it }
            }
        }
    }

    private fun checkConnectivity(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    val isConnected: Boolean
        get() = _connectionStatus.value
}


class DatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "appointments.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "appointments"
        private const val COLUMN_ID = "id"
        private const val COLUMN_STUDENT_ID = "studentID"
        private const val COLUMN_TUTOR_ID = "tutorID"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_SUBJECT = "subject"
        private const val COLUMN_MODE = "mode"
        private const val COLUMN_COMMENTS = "comments"

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_STUDENT_ID TEXT,
                $COLUMN_TUTOR_ID TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_TIME TEXT,
                $COLUMN_SUBJECT TEXT,
                $COLUMN_MODE TEXT,
                $COLUMN_COMMENTS TEXT
            )
        """.trimIndent()
        db.execSQL(createTableStatement)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun getAppointments(): List<Map<String, String>> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val appointments = mutableListOf<Map<String, String>>()

        while (cursor.moveToNext()) {
            val appointment = mapOf(
                COLUMN_ID to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString(),
                COLUMN_STUDENT_ID to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID)),
                COLUMN_TUTOR_ID to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_ID)),
                COLUMN_DATE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                COLUMN_TIME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                COLUMN_SUBJECT to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT)),
                COLUMN_MODE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODE)),
                COLUMN_COMMENTS to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS))
            )
            appointments.add(appointment)
        }
        cursor.close()
        return appointments
    }

    fun insertAppointment(appointment: Map<String, String>): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STUDENT_ID, appointment[COLUMN_STUDENT_ID])
            put(COLUMN_TUTOR_ID, appointment[COLUMN_TUTOR_ID])
            put(COLUMN_DATE, appointment[COLUMN_DATE])
            put(COLUMN_TIME, appointment[COLUMN_TIME])
            put(COLUMN_SUBJECT, appointment[COLUMN_SUBJECT])
            put(COLUMN_MODE, appointment[COLUMN_MODE])
            put(COLUMN_COMMENTS, appointment[COLUMN_COMMENTS])
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun updateAppointment(appointment: Map<String, String>): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STUDENT_ID, appointment[COLUMN_STUDENT_ID])
            put(COLUMN_TUTOR_ID, appointment[COLUMN_TUTOR_ID])
            put(COLUMN_DATE, appointment[COLUMN_DATE])
            put(COLUMN_TIME, appointment[COLUMN_TIME])
            put(COLUMN_SUBJECT, appointment[COLUMN_SUBJECT])
            put(COLUMN_MODE, appointment[COLUMN_MODE])
            put(COLUMN_COMMENTS, appointment[COLUMN_COMMENTS])
        }
        return db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(appointment[COLUMN_ID]))
    }

    fun deleteAllAppointments(): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, null, null)
    }

    fun deleteAppointment(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id))
    }
}

//offline data file
//user logs in we will call log_user-- stores user password and status in a json file
//if only status changes call change status-- modify json file
//if login is offline then check json for username, password and status
//----------possible status: need_email, need_approval, true
