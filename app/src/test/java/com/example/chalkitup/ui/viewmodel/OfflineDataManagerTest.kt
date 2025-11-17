package com.example.chalkitup.ui.viewmodel

import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class OfflineDataManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var testFileDir: File

    @Before
    fun setup() {
        testFileDir = tempFolder.newFolder("offlineTest")
        OfflineDataManager.init(testFileDir)
    }

    @Test
    fun `logUser writes data to file correctly`() {
        OfflineDataManager.logUser("user1", "pass123", "true", "student")
        val status = OfflineDataManager.checkOfflineLogin("user1", "pass123")
        val type = OfflineDataManager.checkUserType("user1", "pass123")

        Assert.assertEquals("true", status)
        Assert.assertEquals("student", type)
    }

    @Test
    fun `checkOfflineLogin returns null on incorrect credentials`() {
        OfflineDataManager.logUser("user2", "secret", "true", "student")

        val result = OfflineDataManager.checkOfflineLogin("wrong", "secret")
        Assert.assertNull(result)

        val result2 = OfflineDataManager.checkOfflineLogin("user2", "wrong")
        Assert.assertNull(result2)
    }

    @Test
    fun `changeStatus updates user status`() {
        OfflineDataManager.logUser("user3", "1234", "true", "student")
        OfflineDataManager.changeStatus("need_approval")
        val updatedStatus = OfflineDataManager.checkOfflineLogin("user3", "1234")
        Assert.assertEquals("need_approval", updatedStatus)
    }

    @Test
    fun `checkUserType returns correct type`() {
        OfflineDataManager.logUser("admin", "adminpass", "true", "admin")
        val userType = OfflineDataManager.checkUserType("admin", "adminpass")
        Assert.assertEquals("admin", userType)
    }

    @Test
    fun `offlineLoginWithEmail triggers onSuccess`() {
        OfflineDataManager.logUser("user4", "pw", "true", "student")

        var successCalled = false
        OfflineDataManager.offlineLoginWithEmail(
            email = "user4",
            password = "pw",
            onSuccess = { successCalled = true },
            onEmailError = {},
            onTermsError = {},
            onError = {},
            awaitingApproval = {},
            isAdmin = {}
        )

        Assert.assertTrue(successCalled)
    }

    @Test
    fun `offlineLoginWithEmail triggers awaitingApproval`() {
        OfflineDataManager.logUser("user5", "pw", "need_approval", "student")

        var awaitingCalled = false
        OfflineDataManager.offlineLoginWithEmail(
            email = "user5",
            password = "pw",
            onSuccess = {},
            onEmailError = {},
            onTermsError = {},
            onError = {},
            awaitingApproval = { awaitingCalled = true },
            isAdmin = {}
        )

        Assert.assertTrue(awaitingCalled)
    }

    @Test
    fun `offlineLoginWithEmail triggers isAdmin`() {
        OfflineDataManager.logUser("adminUser", "admin123", "true", "admin")

        var adminCalled = false
        OfflineDataManager.offlineLoginWithEmail(
            email = "adminUser",
            password = "admin123",
            onSuccess = {},
            onEmailError = {},
            onTermsError = {},
            onError = {},
            awaitingApproval = {},
            isAdmin = { adminCalled = true }
        )

        Assert.assertTrue(adminCalled)
    }

    @Test
    fun `removeUser clears user data`() {
        OfflineDataManager.logUser("removeMe", "bye", "true", "student")
        val removed = OfflineDataManager.removeUser("removeMe")
        val result = OfflineDataManager.checkOfflineLogin("removeMe", "bye")

        Assert.assertTrue(removed)
        Assert.assertNull(result)
    }

    @Test
    fun `removeUser returns false if email doesn't match`() {
        OfflineDataManager.logUser("realUser", "secure", "true", "student")
        val removed = OfflineDataManager.removeUser("fakeUser")

        Assert.assertFalse(removed)
    }
}