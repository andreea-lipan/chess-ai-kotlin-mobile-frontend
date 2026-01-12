package com.example.chessapp.service

import android.content.Context
import android.net.Uri
import com.example.chessapp.view.ChessboardCorners
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

import com.google.gson.annotations.SerializedName

// Response model for what we get from the API
data class ChessPositionResponse(
    val fen: String,
    @SerializedName("lichess_url")
    val lichessUrl: String,
    @SerializedName("board_matrix")
    val boardMatrix: List<List<String>>
)

// Corners data to send to the API
data class CornersData(
    val topLeft: CornerPoint,
    val topRight: CornerPoint,
    val bottomRight: CornerPoint,
    val bottomLeft: CornerPoint
)

data class CornerPoint(val x: Float, val y: Float)

// The whole data model that gets sent to the API
interface ChessDetectionApi {
    @Multipart
    @POST("detect")
    suspend fun detectChessPosition(
        @Part image: MultipartBody.Part,
        @Part("corners") corners: RequestBody,
        @Part("original_width") originalWidth: RequestBody,
        @Part("original_height") originalHeight: RequestBody
    ): ChessPositionResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://chess-ai-python-backend-production.up.railway.app/" // Use 10.0.2.2 for Android emulator
    // For running locally in network use "http://192.168.0.214:8000/" with your PC IP address

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ChessDetectionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChessDetectionApi::class.java)
    }
}

class ChessDetectionService {

    suspend fun detectChessPosition(
        context: Context,
        imageUri: Uri,
        corners: ChessboardCorners
    ): Result<ChessPositionResponse> = withContext(Dispatchers.IO) {
        try {
            // Prepare the image file
            val imageFile = uriToFile(context, imageUri)
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

            // Prepare corners JSON
            val cornersData = CornersData(
                topLeft = CornerPoint(corners.topLeft.x, corners.topLeft.y),
                topRight = CornerPoint(corners.topRight.x, corners.topRight.y),
                bottomRight = CornerPoint(corners.bottomRight.x, corners.bottomRight.y),
                bottomLeft = CornerPoint(corners.bottomLeft.x, corners.bottomLeft.y)
            )
            val cornersJson = Gson().toJson(cornersData)
            val cornersPart = cornersJson.toRequestBody("text/plain".toMediaTypeOrNull())

            // Prepare width and height
            val widthPart = corners.originalImageWidth.toInt().toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val heightPart = corners.originalImageHeight.toInt().toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())

            // Make API call with all parameters
            val response = RetrofitInstance.api.detectChessPosition(
                image = imagePart,
                corners = cornersPart,
                originalWidth = widthPart,
                originalHeight = heightPart
            )
            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI")

        val file = File(context.cacheDir, "temp_chess_image.jpg")
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()
        return file
    }
}