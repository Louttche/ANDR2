package com.example.stalkr.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
class LoginFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(AuthActivity::class.java)

    @Test
    fun loginFragmentShouldShowEmptyEmailAndPasswordErrorMessage() {

        val materialButton = onView(withId(R.id.buttonLogin))

        materialButton.perform(click())

        // Check if empty email error message is shown when no value is entered
        onView(withText(R.string.email_error)).check(matches(isDisplayed()))

        // Check if empty password error message is shown when no value is entered
        onView(withText(R.string.password_error)).check(matches(isDisplayed()))
    }

    @Test
    fun loginFragmentShouldShowInvalidEmailErrorMessage() {
        val materialButton = onView(withId(R.id.buttonLogin))
        val textInputEmail = onView(withId(R.id.textInputEmail))
        val textInputPassword = onView(withId(R.id.textInputPassword))

        textInputEmail.perform(typeText("ddfk"))
        textInputPassword.perform(typeText("dsf"))
        materialButton.perform(click())

        // Check if invalid email error message is shown when no value is entered
        onView(withText(R.string.error_invalid_email)).check(matches(isDisplayed()))
        // No error message should be found
        onView(withText(R.string.error_invalid_password)).check(doesNotExist())
    }
}
