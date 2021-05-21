package com.utinfra.minjin.testsample.imageList

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.utinfra.minjin.testsample.R
import com.utinfra.minjin.testsample.databinding.ActivityImageBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ImageActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PICK_IMAGES = 2
        const val STORAGE_PERMISSION = 100

    }

    private lateinit var binding: ActivityImageBinding

    private lateinit var selectedImageAdapter: SelectedImageAdapter
    private lateinit var imageAdapter: ImageAdapter

    private var imageList = ArrayList<ImageModel>()
    private var selectedImageList = ArrayList<String>()
    private var resImg = arrayOf(R.drawable.ic_camera_white_30dp, R.drawable.ic_folder_white_30dp)
    private val title = arrayOf("Camera", "Folder")
    private var mCurrentPhotoPath: String? = null
    private val projection = arrayOf(MediaStore.MediaColumns.DATA)
    private var imageFileData: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (isStoragePermissionGranted()) {
            viewInit()
            getAllImages()
            setImageList()
            setSelectedImageList()
        }

    }

    private fun viewInit() {

        binding.done.setOnClickListener {
            for (i in selectedImageList.indices) {
                Toast.makeText(applicationContext, selectedImageList[i], Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setImageList() {

        binding.recyclerView.layoutManager = GridLayoutManager(applicationContext, 4)
        imageAdapter = ImageAdapter(applicationContext, imageList)
        binding.recyclerView.adapter = imageAdapter

        imageAdapter.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {

            override fun onItemClick(position: Int, v: View?) {
                when (position) {
                    0 -> {
                        takePicture()
                    }
                    1 -> {
                        getPickImageIntent()
                    }
                    else -> {

                        try {
                            if (!imageList[position].isSelected) {
                                selectImage(position)
                            } else {
                                unSelectImage(position)
                            }
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        })
        setImagePickerList()
    }

    private fun setSelectedImageList() {

        binding.selectedRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        selectedImageAdapter = SelectedImageAdapter(this, selectedImageList)
        binding.selectedRecyclerView.adapter = selectedImageAdapter

    }

    private fun setImagePickerList() {

        for (i in resImg.indices) {
            val imageModel = ImageModel("", title[i], resImg[i], false)
            imageList.add(i, imageModel)
        }

        imageAdapter.notifyDataSetChanged()

    }

    private fun getAllImages() {

        imageList.clear()
        val cursor: Cursor? =
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )

        while (cursor!!.moveToNext()) {
            val absolutePathOfImage =
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))

            val ImageModel = ImageModel(
                absolutePathOfImage,
                "",
                0,
                false
            )

            imageList.add(ImageModel)
        }

        cursor.close()

    }

    private fun takePicture() {

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val photoFile = createImageFile()

        if (photoFile != null) {

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))

            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
        }

    }

    private fun getPickImageIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGES)
    }

    private fun selectImage(position: Int) {

        // Check before add new item in ArrayList;
        if (!selectedImageList.contains(imageList[position].image)) {
            imageList[position].isSelected = true
            selectedImageList.add(0, imageList[position].image.toString())
            selectedImageAdapter.notifyDataSetChanged()
            imageAdapter.notifyDataSetChanged()
        }

    }

    private fun unSelectImage(position: Int) {

        for (i in selectedImageList.indices) {
            if (imageList[position].image != null) {

                if (selectedImageList[i] == imageList[position].image) {

                    imageList[position].isSelected = false
                    selectedImageList.removeAt(i)
                    selectedImageAdapter.notifyDataSetChanged()
                    imageAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun createImageFile(): File? {

        val dateTime = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMG_${dateTime}_"
        val storageDir: File =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        try {

            imageFileData = File.createTempFile(imageFileName, ".jpg", storageDir)

        } catch (e: IOException) {
            e.printStackTrace()
        }

        mCurrentPhotoPath = "file:${imageFileData?.absolutePath}"

        return imageFileData
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (mCurrentPhotoPath != null) {
                    addImage(mCurrentPhotoPath!!)
                }
            } else if (requestCode == PICK_IMAGES) {
                if (data?.clipData != null) {
                    val mClipData = data.clipData!!
                    for (i in 0..mClipData.itemCount) {
                        val item: ClipData.Item = mClipData.getItemAt(i)
                        val uri = item.uri
                        getImageFilePath(uri)
                    }

                } else if (data?.data != null) {
                    val uri = data.data!!
                    getImageFilePath(uri)
                }
            }
        }

    }

    private fun getImageFilePath(uri: Uri) {

        val cursor = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val absolutePathOfImage =
                    cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                if (absolutePathOfImage != null) {
                    checkImage(absolutePathOfImage)
                } else {
                    checkImage(uri.toString())
                }
            }
        }

    }

    private fun checkImage(filePath: String) {

        if (!selectedImageList.contains(filePath)) {

            for (pos in imageList.indices) {
                if (imageList[pos].image != null) {
                    if (imageList[pos].image.equals(filePath, ignoreCase = true)) {
                        imageList.removeAt(pos)
                    }
                }
            }
            addImage(filePath)
        }

    }

    private fun addImage(filePath: String) {

        val imageModel = ImageModel(filePath, null, 0, true)

        imageList.add(2, imageModel)
        selectedImageList.add(0, filePath)
        selectedImageAdapter.notifyDataSetChanged()
        imageAdapter.notifyDataSetChanged()

    }

    private fun isStoragePermissionGranted(): Boolean {

        val ACCESS_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if ((ACCESS_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION
            )

            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

            viewInit()
            getAllImages()
            setImageList()
            setSelectedImageList()
        }

    }

}