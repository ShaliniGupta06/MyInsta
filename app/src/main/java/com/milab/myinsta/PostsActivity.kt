package com.milab.myinsta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.milab.myinsta.models.Post
import kotlinx.android.synthetic.main.activity_posts.*

private const val TAG = "PostsActivity"
class PostsActivity : AppCompatActivity() {

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
        val postsReference = firestoreDb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_profile){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}