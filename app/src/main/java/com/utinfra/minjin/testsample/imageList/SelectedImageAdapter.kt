package com.utinfra.minjin.testsample.imageList

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utinfra.minjin.testsample.R
import com.utinfra.minjin.testsample.databinding.SelectedImageListBinding

class SelectedImageAdapter(
    private val context: Context,
    private val stringArrayList: ArrayList<String>
) : RecyclerView.Adapter<SelectedImageAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {

        val view =
            SelectedImageListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        Glide.with(context).load(stringArrayList[position])
            .placeholder(R.color.codeGray)
            .centerCrop()
            .into(holder.binding.image)

        holder.binding.image.setOnClickListener {

            val intent = Intent(context, FullImageActivity::class.java)
            intent.putExtra("image", stringArrayList[position])
            context.startActivity(intent)

        }

    }

    override fun getItemCount(): Int = stringArrayList.size

    inner class CustomViewHolder(itemView: SelectedImageListBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        val binding = itemView

    }


}