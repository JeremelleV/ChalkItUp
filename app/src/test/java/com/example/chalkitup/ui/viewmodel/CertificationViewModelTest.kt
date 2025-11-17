package com.example.chalkitup.ui.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.chalkitup.domain.model.Certification
import com.example.chalkitup.domain.repository.CertificationRepositoryInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CertificationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CertificationViewModel

    @Mock
    private lateinit var mockRepo: CertificationRepositoryInterface

    @Mock
    private lateinit var mockUri: Uri

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = CertificationViewModel(mockRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCertifications updates state successfully`() = runTest {
        val certs = listOf(Certification("file.pdf", "url"))
        val uris = listOf(mockUri)

        `when`(mockRepo.fetchCertifications("")).thenReturn(Result.success(certs to uris))

        viewModel.getCertifications()

        advanceUntilIdle()

        Assert.assertEquals(certs, viewModel.certifications.value)
        Assert.assertEquals(uris, viewModel.selectedFiles.value)
    }

    @Test
    fun `uploadFiles uploads and clears state`() = runTest {
        val userId = "user123"
        val files = listOf(mockUri)

        viewModel.addSelectedFiles(files)

        `when`(mockRepo.uploadFiles(userId, files)).thenReturn(Result.success(Unit))
        `when`(mockRepo.fetchCertifications("")).thenReturn(Result.success(emptyList<Certification>() to emptyList()))

        viewModel.uploadFiles(userId)

        advanceUntilIdle()

        Assert.assertEquals(emptyList<Uri>(), viewModel.selectedFiles.value)
    }

    @Test
    fun `updateCertifications calls repository and clears state`() = runTest {
        val files = listOf(mockUri)
        val certs = listOf(Certification("test.pdf", "url"))
        val uris = listOf(mockUri)

        viewModel.addSelectedFiles(files)

        `when`(mockRepo.getCurrentUserId()).thenReturn("user123")
        `when`(mockRepo.updateCertifications("user123", files)).thenReturn(Result.success(Unit))
        `when`(mockRepo.fetchCertifications("")).thenReturn(Result.success(certs to emptyList()))

        viewModel.updateCertifications()

        advanceUntilIdle()

        Assert.assertEquals(certs, viewModel.certifications.value)
        Assert.assertEquals(emptyList<Uri>(), viewModel.selectedFiles.value)
    }

    @Test
    fun `downloadFileToCache updates fileUri on success`() = runTest {
        val fileName = "test.pdf"
        val fileUri = mock(Uri::class.java)
        val observer = mock(Observer::class.java) as Observer<Uri?>

        viewModel.fileUri.observeForever(observer)

        `when`(mockRepo.getCurrentUserId()).thenReturn("user123")
        `when`(mockRepo.downloadFileToCache("user123", fileName)).thenReturn(Result.success(fileUri))

        viewModel.downloadFileToCache(fileName)

        advanceUntilIdle()

        verify(observer).onChanged(fileUri)
    }

    @Test
    fun `downloadFileToCache sets null on failure`() = runTest {
        val fileName = "fail.pdf"
        val observer = mock(Observer::class.java) as Observer<Uri?>

        viewModel.fileUri.observeForever(observer)

        `when`(mockRepo.getCurrentUserId()).thenReturn("user123")
        `when`(mockRepo.downloadFileToCache("user123", fileName)).thenReturn(Result.failure(Exception()))

        viewModel.downloadFileToCache(fileName)

        advanceUntilIdle()

        verify(observer).onChanged(null)
    }

    @Test
    fun `resetFileUri sets fileUri to null`() {
        val observer = mock(Observer::class.java) as Observer<Uri?>

        viewModel.fileUri.observeForever(observer)
        viewModel.resetFileUri()

        verify(observer).onChanged(null)
    }

    @Test
    fun `addSelectedFiles adds to selectedFiles`() = runTest {
        val files = listOf(mockUri)
        viewModel.addSelectedFiles(files)

        Assert.assertEquals(files, viewModel.selectedFiles.value)
    }

    @Test
    fun `removeSelectedFile removes from selectedFiles`() = runTest {
        val files = listOf(mockUri)
        viewModel.addSelectedFiles(files)
        viewModel.removeSelectedFile(mockUri)

        Assert.assertEquals(emptyList<Uri>(), viewModel.selectedFiles.value)
    }
}
