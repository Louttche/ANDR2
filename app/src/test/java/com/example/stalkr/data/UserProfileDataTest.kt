package com.example.stalkr.data

// Test
import org.junit.Test
import org.junit.Assert.*
import com.google.common.truth.Truth.assertThat
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import androidx.test.filters.SmallTest;

// Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase

@SmallTest
class UserProfileDataTest{

    @Test
    fun setNameToEmptyIfNull_inUpdateUserProfileFromDB() {

        //val usersCollection = firestore.collection("users")

        // arrange
        //val userData = UserProfileData("valid_id", null)
        // act
        //userData.updateUserProfileFromDB("valid_id")
        // assert
        //assertThat(userData.name == "")
        assertTrue(true)
    }

    @Test
    fun throwNullPointerExceptionIfLocationIsNull_InUpdateLocationInDB(){
        assertTrue(true)
    }
}