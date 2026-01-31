package com.example.cpreminder20

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- 1. DATA MODELS ---

// User Profile (For the new Stats Card)
data class UserResponse(
    val status: String,
    val result: List<User>
)

data class User(
    val handle: String,
    val rating: Int?,
    val maxRating: Int?,
    val rank: String?,
    val maxRank: String?
)

// Submissions (For the Daily 10:30 PM Check)
data class SubmissionResponse(
    val status: String,
    val result: List<Submission>
)

data class Submission(
    val id: Int,
    val creationTimeSeconds: Long,
    val verdict: String? // e.g., "OK", "WRONG_ANSWER"
)

// Contests (For the 30-min Alarm)
data class ContestResponse(
    val status: String,
    val result: List<Contest>
)

data class Contest(
    val id: Int,
    val name: String,
    val startTimeSeconds: Long,
    val phase: String // "BEFORE", "CODING", "FINISHED"
)

// --- 2. THE INTERFACE ---
interface CodeforcesApi {

    // NEW: Get User Profile (Rating, Rank, etc.)
    @GET("user.info")
    suspend fun getUserInfo(
        @Query("handles") handles: String
    ): UserResponse

    // Existing: Get Submissions
    @GET("user.status")
    suspend fun getUserSubmissions(
        @Query("handle") handle: String,
        @Query("from") from: Int = 1,
        @Query("count") count: Int = 10
    ): SubmissionResponse

    // Existing: Get Contest List
    @GET("contest.list")
    suspend fun getContestList(
        @Query("gym") gym: Boolean = false
    ): ContestResponse
}

// --- 3. THE CONNECTION ENGINE ---
object RetrofitInstance {
    private const val BASE_URL = "https://codeforces.com/api/"

    val api: CodeforcesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CodeforcesApi::class.java)
    }
}