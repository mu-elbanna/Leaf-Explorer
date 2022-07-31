package org.monora.uprotocol.client.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import org.monora.uprotocol.client.android.GlideApp
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.model.CategoryModel

class HomeCategoryAdapter(val context: Context, private val mData: List<CategoryModel>) : RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_home_categories, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val mCategoryModel = mData[position]
        // sets the text to the textview from our itemHolder class
        holder.name.text = mCategoryModel.name
        holder.info.text = mCategoryModel.info

        GlideApp.with(holder.image.context)
            .load(mCategoryModel.image)
            .into(holder.image)

        holder.layout.setOnClickListener {
            Toast.makeText(context, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mData.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val layout: ConstraintLayout = itemView.findViewById(R.id.layout)
        val name: TextView = itemView.findViewById(R.id.name)
        val info: TextView = itemView.findViewById(R.id.info)
        val image: ImageView = itemView.findViewById(R.id.image)
    }
}