package com.milab.myinsta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.milab.myinsta.models.Post
import com.milab.myinsta.models.User
import kotlinx.android.synthetic.main.activity_posts.*
import kotlinx.coroutines.handleCoroutineException

private const val TAG = "PostsActivity"
const val EXTRA_USERNAME = "EXTRA_USERNAME"
open class PostsActivity : AppCompatActivity() {

    private var signedInUser : User? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts : MutableList<Post>
    private lateinit var adapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        // Create the layout file which represents one post -- item_post.xml
        // Create data source
        posts = mutableListOf()
        // Create adapter
        // Bind adapter and layout manager to the RV
        adapter = PostsAdapter(this, posts)
        //Querying Firestore to retrieve data
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)
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

        var postsReference = firestoreDb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        if(username != null){
            supportActionBar?.title = username
            postsReference = postsReference.whereEqualTo("user.username", username)
        }
        postsReference.addSnapshotListener{snapshot, exception ->
            if(exception != null || snapshot == null){
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            for(post in postList)
                Log.i(TAG, "Post: ${post}")
        }

        fabCreate.setOnClickListener{
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_profile){
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}