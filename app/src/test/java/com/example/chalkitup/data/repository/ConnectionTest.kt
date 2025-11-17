package com.example.chalkitup.data.repository


import com.example.chalkitup.Connection
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ConnectionTest {

    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var network: Network
    private lateinit var networkCapabilities: NetworkCapabilities

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        connectivityManager = mock(ConnectivityManager::class.java)
        network = mock(Network::class.java)
        networkCapabilities = mock(NetworkCapabilities::class.java)

        `when`(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)

        resetConnectionSingleton()
    }

    @After
    fun tearDown() {
        resetConnectionSingleton()
    }

    @Test
    fun `isConnected returns true for WIFI`() {
        `when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        `when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(false)

        val connection = Connection.getInstance(context)
        assertThat(connection.isConnected).isTrue()
    }

    @Test
    fun `isConnected returns true for CELLULAR`() {
        `when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(false)
        `when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)

        val connection = Connection.getInstance(context)
        assertThat(connection.isConnected).isTrue()
    }

    @Test
    fun `isConnected returns false when no network`() {
        `when`(connectivityManager.activeNetwork).thenReturn(null)

        val connection = Connection.getInstance(context)
        assertThat(connection.isConnected).isFalse()
    }

    // Helper to reset the singleton between tests
    private fun resetConnectionSingleton() {
        val field = Connection::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }
}