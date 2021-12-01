package com.example.stalkr.data

// Test
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import androidx.test.filters.SmallTest;

@SmallTest
class UserProfileDataTest{

    @Test
    fun setNameToDefaultStringIfEmptyOrNull_inUpdateUserProfileFromDB() {
        // arrange
        //val userData_null = UserProfileData("valid_id", null)
        //val userData_empty = UserProfileData("valid_id", "")

        // act
        //userData_null.updateUserProfileFromDB("valid_id")
        //userData_empty.updateUserProfileFromDB("valid_id")

        // assert
        //assertThat(userData_null.name == "N/A")
        //assertThat(userData_empty.name == "N/A")

        fail("Not yet implemented")
    }

    @Test
    fun throwNullPointerExceptionIfLocationIsNull_InUpdateLocationInDB(){
        fail("Not yet implemented")
    }
}