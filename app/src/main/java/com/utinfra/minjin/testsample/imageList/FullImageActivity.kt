package com.utinfra.minjin.testsample.imageList

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.utinfra.minjin.testsample.R
import com.utinfra.minjin.testsample.databinding.ActivityFullImageBinding

class FullImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFullImageBinding.inflate(layoutInflater)

        setContentView(binding.root)

        Glide.with(this)
            .load(intent.getStringExtra("image"))
            .placeholder(R.color.codeGray)
            .into(binding.image)

        binding.back.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

    }

}