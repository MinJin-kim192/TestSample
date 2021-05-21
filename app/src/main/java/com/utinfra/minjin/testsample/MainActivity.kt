package com.utinfra.minjin.testsample

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.utinfra.minjin.testsample.databinding.ActivityMainBinding
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        lateinit var mCurrentPhotoPath: String
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.photoBtn.setOnClickListener {
            permissionCheck()
        }

    }

    private fun permissionCheck() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            ) {

                dispatchTakePictureIntent()
                Log.d("로그", "권한 승인")
            } else {

                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 1
                )
            }
        }

    }

    private fun dispatchTakePictureIntent() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(packageManager) != null) {

            var photoFile: File? = null

            try {
                photoFile = createImageFile()

            } catch (e: Exception) {
                Log.d("로그", "error $e")
            }

            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.utinfra.minjin.testsample.fileprovider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, REQUEST_TAKE_PHOTO)
            }

        }

    }

    private fun createImageFile(): File {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

        mCurrentPhotoPath = image.absolutePath

        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {

            when (requestCode) {

                REQUEST_TAKE_PHOTO -> {
                    val file = File(mCurrentPhotoPath)
                    var bitmap: Bitmap? =
                        MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))

                    if (bitmap != null) {

                        val rotate = Matrix()
                        val folderName = "test"
                        rotate.postRotate(90f)
                        bitmap = Bitmap.createBitmap(bitmap,0,0, bitmap.width, bitmap.height, rotate, false)

                        saveImage(bitmap, folderName)
                        binding.uploadView.setImageBitmap(bitmap)
                    }

                }

            }

        } catch (e: Exception) {

        }

    }

    private fun saveImage(bitmap: Bitmap, GalleryFolderName: String) {

        if (Build.VERSION.SDK_INT >= 29) {

            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$GalleryFolderName")
            values.put(MediaStore.Images.Media.IS_PENDING, true)

            val uri: Uri? =
                this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            if (uri != null) {
                saveImageToStream(bitmap, this.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                this.contentResolver.update(uri, values, null, null)
            }

        } else {

            val directory =
                File(Environment.getExternalStorageDirectory().toString() + separator + GalleryFolderName)

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)

            saveImageToStream(bitmap, FileOutputStream(file))

            val values = contentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        }

    }

    private fun contentValues(): ContentValues {

        val values = ContentValues()

        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {

        if (outputStream != null) {

            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

}