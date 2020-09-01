package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.squareup.picasso.Picasso

class FullViewImageActivity : AppCompatActivity() {
    private var image_full_view : ImageView? = null
    private var imageUrl : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_view_image)

        imageUrl = intent.getStringExtra("url")
        image_full_view = findViewById(R.id.iv_full_image)

        Picasso.get().load(imageUrl).into(image_full_view)
    }
}
