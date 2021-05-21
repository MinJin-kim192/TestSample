package com.utinfra.minjin.testsample

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.utinfra.minjin.testsample.databinding.ActivityGalleryBinding
import java.lang.Exception

class GalleryActivity : AppCompatActivity() {


    companion object {

        private const val REQUEST_CODE = 0

    }

    private lateinit var binding : ActivityGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryImage.setOnClickListener {

            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_CODE)

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE) {

            if(resultCode == RESULT_OK) {

                try {
                    val inputStream = contentResolver.openInputStream(data?.data!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    binding.galleryImage.setImageBitmap(bitmap)

                } catch (e: Exception) {
                    Log.d("로그","error $e")
                }

            }

        } else {

            Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_SHORT).show()

        }

    }

}