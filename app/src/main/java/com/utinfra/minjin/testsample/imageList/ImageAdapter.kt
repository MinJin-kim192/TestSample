package com.utinfra.minjin.testsample.imageList

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.utinfra.minjin.testsample.R
import com.utinfra.minjin.testsample.databinding.ImageListBinding
import com.utinfra.minjin.testsample.databinding.ImagePickerListBinding

class ImageAdapter(
    private val context: Context,
    private val imageList: ArrayList<ImageModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        private lateinit var onItemClickListener: OnItemClickListener
        private const val IMAGE_LIST = 0
        private const val IMAGE_PICKER = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            IMAGE_LIST -> {
                val view = ImageListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ImageListViewHolder(view)
            }
            else -> {

                val view =
                    ImagePickerListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ImagePickerViewHolder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {

            is ImageListViewHolder -> {

                Log.d("로그", "폴더 ${imageList[position].image}")

                Glide.with(context)
                    .load(imageList[position].image)
                    .placeholder(R.color.codeGray)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .into(holder.listBinding.image)

                holder.listBinding.circle.isChecked = imageList[position].isSelected
                holder.listBinding.root.setOnClickListener {
                    onItemClickListener.onItemClick(position, it)
                }

            }

            is ImagePickerViewHolder -> {
                holder.pickerBinding.image.setImageResource(imageList[position].resImg)
                holder.pickerBinding.title.text = imageList[position].title
                holder.pickerBinding.root.setOnClickListener {
                    onItemClickListener.onItemClick(position, it)
                }

            }

        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position < 2) IMAGE_PICKER else IMAGE_LIST
    }

    override fun getItemCount(): Int = imageList.size

    inner class ImageListViewHolder(binding: ImageListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val listBinding = binding

    }

    inner class ImagePickerViewHolder(binding: ImagePickerListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val pickerBinding = binding

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, v: View?)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        ImageAdapter.onItemClickListener = onItemClickListener
    }

}