package com.example.droidmentor.ui.title

import com.example.droidmentor.MainDispatcherRule
import com.example.droidmentor.data.repository.FakeConversationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class TitleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeConversationRepository()

    @Test
    fun `cold start with an active conversation redirects to that conversation`() = runTest {
        // Arrange
        val id = repository.createConversation("Left open")
        val vm = TitleViewModel(repository)

        // Act
        vm.resolveStartup(isColdStart = true)

        // Assert
        assertEquals(StartupState.ResumeConversation(id), vm.startupState.value)
    }

    @Test
    fun `cold start without an active conversation shows the title`() = runTest {
        // Arrange
        repository.createConversation("Closed")
        repository.clearActiveConversation()
        val vm = TitleViewModel(repository)

        // Act
        vm.resolveStartup(isColdStart = true)

        // Assert
        assertEquals(StartupState.ShowTitle, vm.startupState.value)
    }

    @Test
    fun `recreation shows the title even with an active conversation`() = runTest {
        // Arrange
        repository.createConversation("Left open")
        val vm = TitleViewModel(repository)

        // Act
        vm.resolveStartup(isColdStart = false)

        // Assert
        assertEquals(StartupState.ShowTitle, vm.startupState.value)
    }

    @Test
    fun `startup is resolved only once per view model`() = runTest {
        // Arrange
        repository.createConversation("Left open")
        val vm = TitleViewModel(repository)
        vm.resolveStartup(isColdStart = true)
        vm.onRedirectHandled()

        // Act
        vm.resolveStartup(isColdStart = true)

        // Assert
        assertEquals(StartupState.ShowTitle, vm.startupState.value)
    }

    @Test
    fun `only the most recently created conversation is active`() = runTest {
        // Arrange
        repository.createConversation("Older")
        val newest = repository.createConversation("Newest")
        val vm = TitleViewModel(repository)

        // Act
        vm.resolveStartup(isColdStart = true)

        // Assert
        assertEquals(StartupState.ResumeConversation(newest), vm.startupState.value)
    }

    @Test
    fun `endActiveSession prevents the redirect on the next launch`() = runTest {
        // Arrange
        repository.createConversation("Left open")
        TitleViewModel(repository).endActiveSession()
        val nextLaunch = TitleViewModel(repository)

        // Act
        nextLaunch.resolveStartup(isColdStart = true)

        // Assert
        assertNull(repository.getActiveConversation())
        assertEquals(StartupState.ShowTitle, nextLaunch.startupState.value)
    }
}
