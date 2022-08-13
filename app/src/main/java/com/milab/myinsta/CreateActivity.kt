package com.milab.myinsta

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.milab.myinsta.models.Post
import com.milab.myinsta.models.User
import kotlinx.android.synthetic.main.activity_create.*
import kotlin.math.sign

private const val TAG = "CreateActivity"
private const val PICK_PHOTO_CODE = 1234
class CreateActivity : AppCompatActivity() {
    private var photoUri: Uri?= null
    private var signedInUser : User? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageRef : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        storageRef = FirebaseStorage.getInstance().reference
        firestoreDb = FirebaseFirestore.getInstance()
        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "signed in user: $signedInUser")
            }
            .addOnFailureListener{ exception ->
                Log.i(TAG, "Failure in fetching signed in user", exception)
            }

        btnPickImage.setOnClickListener {
            Log.i(TAG, "Open up image picker on device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }

        }

        btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    private fun handleSubmitButtonClick() {
        if (photoUri == null){
            Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show()
            return
        }
        if(etDescription.text.isBlank()){
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if(signedInUser == null){
            Toast.makeText(this, "No signed in user, please wait", Toast.LENGTH_SHORT).show()
            return
        }
        btnSubmit.isEnabled = false
        val photoUploadUri = photoUri as Uri // Non null
        //Upload photo to FireBase Storage
        val photoRef = storageRef.child("images/${System.currentTimeMillis()}-photo.jpeg")
        photoRef.putFile(photoUploadUri)
            //Asynchronous call to FireStore to retrieve image url of the uploaded image
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                photoRef.downloadUrl
            }.continueWithTask{ downloadUrlTask ->
                // Create a post object with the image URl and add that to posts collection
                val post = Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)
                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                btnSubmit.isEnabled = true
                if (!postCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception during Firebase Operations", postCreationTask.exception)
                    Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
                }
                etDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this, "Post uploaded successfully !!", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileIntent)
                finish()
            }


        // Use Tasks API - A way to get notified when an async operation completes

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_PHOTO_CODE){
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                Log.i(TAG, "photoUri $photoUri")
                imageView.setImageURI(photoUri)
            }else{
                Toast.makeText(this, "Image picker action cancelled", Toast.LENGTH_SHORT).show()
            }
        }


    }
}