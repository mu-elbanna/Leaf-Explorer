package org.monora.uprotocol.client.android.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.leaf.music.helper.ArcProgress
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.activity.ExplorerActivity
import org.monora.uprotocol.client.android.model.StorageModel

class StorageAdapter(val context: Context, private val mData: List<StorageModel>) : RecyclerView.Adapter<StorageAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_memory_info, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val mStorageModel = mData[position]
        // sets the text to the textview from our itemHolder class
        holder.name.text = mStorageModel.name
        holder.free.text = mStorageModel.free
        holder.total.text = mStorageModel.total
        holder.used.text = mStorageModel.used
        holder.progressStorage.progress = mStorageModel.percentage

        holder.layout.setOnClickListener {
            context.startActivity(Intent(context, ExplorerActivity::class.java))
          //  Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()

//            AlertDialog.Builder(context)
//                .setMessage(R.string.install_leaf_file_explorer)
//                .setNegativeButton(R.string.cancel, null)
//                .setPositiveButton(R.string.donate) { _, _ ->
//                    context.startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(AppConfig.URI_SHIV_SHAMBHU_DONATE)))
//                }
//                .show()
//            if (fileManagerAvailable()) {
//                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.leaf.explorer.pro")
//                if (launchIntent != null) {
//                    context.startActivity(launchIntent)
//                }
//            } else {
//                Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
//            }

        }

    }

//    private fun fileManagerAvailable(): Boolean {
//        var available = true
//
//        try {
//            context.packageManager.getPackageInfo("com.leaf.explorer.pro", 0)
//        } catch (e: PackageManager.NameNotFoundException) {
//            available = false
//        }
//
//        return available
//    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mData.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val layout: LinearLayout = itemView.findViewById(R.id.layout)
        val name: TextView = itemView.findViewById(R.id.name)
        val free: TextView = itemView.findViewById(R.id.free)
        val total: TextView = itemView.findViewById(R.id.total)
        val used: TextView = itemView.findViewById(R.id.used)
        val progressStorage: ArcProgress = itemView.findViewById(R.id.progress_storage)
    }
}