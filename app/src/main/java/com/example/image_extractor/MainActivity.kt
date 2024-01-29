package com.example.image_extractor

import DatabaseHelper
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.IOException

// Import necessary libraries
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody


class MainActivity : AppCompatActivity() {

    private lateinit var currentPhotoPath: String
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var databaseHelper: DatabaseHelper
    private val PICK_PHOTO_REQUEST = 1 // You can choose any value you like
    private lateinit var currentPhotoBytes: ByteArray


    private var Date: TextInputEditText? = null
    private var CompetitorName: TextInputEditText? = null

    private lateinit var chooseButton: Button
    private lateinit var uploadButton: Button

    private val imageView: ImageView by lazy { findViewById(R.id.imageView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        okHttpClient = OkHttpClient()


        Date = findViewById(R.id.editTextID2)
        CompetitorName = findViewById(R.id.editTextID)
        chooseButton = findViewById(R.id.button2)
        uploadButton = findViewById(R.id.button)

        databaseHelper = DatabaseHelper(this)

        // Set click listener for Choose Image button
        chooseButton.setOnClickListener {
            // Replace the camera intent with code to choose an image from the gallery
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, PICK_PHOTO_REQUEST)
        }

        // Set click listener for Upload button
        uploadButton.setOnClickListener {
            //uploadDataToDatabase()
            //createCopyOnDesktop()
            uploadDataToBackend()

        }

    }

    // Inside your MainActivity class
    private fun uploadDataToBackend() {
        println("poda punde")
        if (::currentPhotoPath.isInitialized) {
            val file = File(currentPhotoPath)
            val request = Request.Builder()
                .url("http://192.168.8.124:5000/")
                .post(file.asRequestBody("image/*".toMediaTypeOrNull()))
                .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to connect to server", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    // Handle the response as needed
                    // Print or display the result from the Flask backend
                    println(responseBody)
                    runOnUiThread {
                        Toast.makeText(applicationContext, responseBody, Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            Toast.makeText(applicationContext, "Please choose an image first", Toast.LENGTH_SHORT).show()
        }


    }

    // Function to copy the selected image to the desktop and return the desktop path
    @Throws(IOException::class)
    private fun copyImageToDesktop(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = File(uri.path!!).name

        // Check if external storage is writable
        if (isExternalStorageWritable()) {
            val desktopPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destinationPath = "$desktopPath/$fileName"

            inputStream?.use { input ->
                File(destinationPath).outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            return destinationPath
        } else {
            throw IOException("External storage is not writable")
        }
    }

    // Function to check if external storage is writable
    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }






    // Handle result of the image capture intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { selectedImage ->
                try {
                    // Copy the selected image to the desktop
                    //val destinationPath = copyImageToDesktop(selectedImage)
                    val destinationPath = copyImageToDesktop(selectedImage)
                    val inputStream = contentResolver.openInputStream(selectedImage)
                    currentPhotoBytes = inputStream?.readBytes() ?: byteArrayOf()

                    // Save the path for later use
                    currentPhotoPath = destinationPath

                    /// Set the selected image to the ImageView using Glide
                    Glide.with(this).load(currentPhotoBytes).into(imageView)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error converting image to bytes", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

// ... (other functions)


    // Function to copy the selected image to the project's root folder
    @Throws(IOException::class)
//    private fun copyImageToProjectFolder(uri: Uri): String {
//        val inputStream = contentResolver.openInputStream(uri)
//        val destinationPath = "/data/data/com.example.image_extractor/image.jpg"
//        // Change this to the desired destination path
//
//        inputStream?.use { input ->
//            File(destinationPath).outputStream().use { output ->
//                input.copyTo(output)
//            }
//        }
//
//        return destinationPath
//    }



//    private fun uploadDataToDatabase() {
//        val competitorName = CompetitorName?.text.toString()
//        val date = Date?.text.toString()
//
//        if (competitorName.isNotEmpty() && date.isNotEmpty() && ::currentPhotoBytes.isInitialized) {
//            // Save the data to the SQLite database
//            saveDataToDatabase(competitorName, date, currentPhotoBytes)
//        }
//    }

    // Function to save data to the SQLite database using SQLiteOpenHelper
    private fun saveDataToDatabase(competitorName: String, date: String, imageBytes: ByteArray) {
        // Insert the data into the database
        databaseHelper.insertData(competitorName, date, imageBytes)
        Toast.makeText(this, "Successfully Uploaded", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        // Close the database when the activity is destroyed
        databaseHelper.close()
        super.onDestroy()
    }
    



}