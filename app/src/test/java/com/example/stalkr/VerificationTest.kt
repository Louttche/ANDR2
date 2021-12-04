package com.example.stalkr

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


internal class VerificationTest {
    private lateinit var verification:Verification

    @BeforeEach
    internal fun setUp() {
        verification = Verification()
    }

    @ParameterizedTest
    @CsvSource(
        "t-justice98@hotmail.com, true",
        "mary@testdomain.com, true",
        "mary.smith@testdomain.com, true",
        "mary_smith123@testdomain.com, true",
        "Julia@007.com, true",
        "Julia.007@abc.com, true",
        "mary@testdomaindotcom, false",
        "mary-smith@testdomain, false",
        "testdomain.com, false",
        "Julia.abc@, false",
        "Samantha@com, false",
        "Samantha_21., false",
        ".1Samantha, false",
        "Samantha@10_2A, false",
        "JuliaZ007, false",
        "_Julia007.com, false",
        "_Julia007@abc.co.in, false",
        )
    fun `isEmailValid should check if email is valid or not`(email: String, expected: Boolean) {
        val isEmailValid = verification.isEmailValid(email)
        assertEquals(expected, isEmailValid)
    }

    @ParameterizedTest
    @CsvSource(
        "Test123!, true",
        "jtesT12!, true",
        "12Es@t123, true",
        "12345678Fj#, true",
        "' ',false",
        "1, false",
        "j, false",
        "t  , false",
        "test123!, false",
        "DSFSD123!, false",

    )
    fun `isPasswordValid should check if password is valid or not`(password: String, expected: Boolean) {
        val isPasswordValid = verification.isPasswordValid(password)
        assertEquals(expected, isPasswordValid)
    }

    @Test
    fun `isPasswordValid should check if password is empty`() {
        verification.isPasswordValid("")
        assertTrue(verification.getIsPasswordEmpty())
    }

    @Test
    fun `isEmailValid should check if email is empty`() {
        verification.isEmailValid("")
        assertTrue(verification.getIsEmailEmpty())
    }
}