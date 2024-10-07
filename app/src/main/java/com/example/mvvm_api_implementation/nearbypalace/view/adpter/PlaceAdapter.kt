package com.example.mvvm_api_implementation.nearbypalace.view.adpter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvm_api_implementation.databinding.PlaceItemBinding
import com.example.mvvm_api_implementation.nearbypalace.model.PlacePojo

class PlaceAdapter(
    private var context: Context,
    private var arrayList: List<PlacePojo.Result>,
    private var placeLister: PlaceLister
) : RecyclerView.Adapter<PlaceAdapter.MyViewHolder>() {

    // Using ViewBinding for ViewHolder
    inner class MyViewHolder(val binding: PlaceItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Setting click listener in ViewHolder constructor
            binding.llMain.setOnClickListener {
                placeLister.click(adapterPosition)  // Use adapterPosition for position
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the layout using ViewBinding
        val binding = PlaceItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Bind data to the views using ViewBinding
        val place = arrayList[position]
        holder.binding.tvTitle.text = place.name
        holder.binding.tvAddress.text = place.vicinity
    }

    interface PlaceLister {
        fun click(position: Int)
    }
}
