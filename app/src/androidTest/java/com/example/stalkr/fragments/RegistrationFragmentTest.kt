package com.example.stalkr.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.stalkr.R
import com.example.stalkr.activities.AuthActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class RegistrationFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(AuthActivity::class.java)

    @Test
    fun registrationFragmentShouldShowEmptyErrorMessages() {

        // Change fragment to registration fragment
        onView(withId(R.id.buttonShowRegistration)).perform(click())

        // Check if registration fragment has open
        onView(withId(R.id.frameLayoutRegistrationFragment)).check(matches(isDisplayed()))

        val materialButton = onView(withId(R.id.buttonRegisterR))

        // Click register button
        materialButton.perform(click())

        // Sleep for 5 seconds
        try {
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        // Check if empty email error message is shown when no value is entered
        onView(withText(R.string.email_error)).check(matches(isDisplayed()))

        // Check if empty password error message is shown when no value is entered
        onView(withText(R.string.password_error)).check(matches(isDisplayed()))

        // Check if empty name error message is shown when no value is entered
        onView(withText(R.string.name_error)).check(matches(isDisplayed()))

        // Check if empty password error message is shown when no value is entered
        onView(withText(R.string.confirmation_password_error)).check(matches(isDisplayed()))
    }

    @Test
    fun registrationFragmentShouldShowInvalidEmailErrorMessage() {
        // Change fragment to registration fragment
        onView(withId(R.id.buttonShowRegistration)).perform(click())

        // Check if registration fragment has open
        onView(withId(R.id.frameLayoutRegistrationFragment)).check(matches(isDisplayed()))

        // Enter invalid email in the input text field
        onView(withId(R.id.textInputEmailR)).perform(typeText("dsf"))

        // Enter invalid password in the input text field
        onView(withId(R.id.textInputPasswordR)).perform(typeText("ddfesf"))

        // Enter invalid confirmation password in the input text field
        onView(withId(R.id.textInputConfirmationPasswordR)).perform(typeText("dsdff"))

        // Close keyboard
        pressBack()
        
        // Click register button
        onView(withId(R.id.buttonRegisterR)).perform(click())

        // Sleep for 5 seconds
        try {
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        // Check if invalid email error message is shown when value is invalid
        onView(withText(R.string.error_invalid_email)).check(matches(isDisplayed()))
        // Check if invalid password error message is shown when value is invalid
        onView(withText(R.string.error_invalid_password)).check(matches(isDisplayed()))
        // Check if invalid confirmation password error message is shown when value is invalid
        onView(withText(R.string.error_invalid_confirmation_password)).check(matches(isDisplayed()))
    }
}
