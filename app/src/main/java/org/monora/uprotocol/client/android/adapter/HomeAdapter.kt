package org.monora.uprotocol.client.android.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.config.AppConfig
import org.monora.uprotocol.client.android.listener.HomeListener
import org.monora.uprotocol.client.android.model.CategoryModel
import org.monora.uprotocol.client.android.model.HomeModel
import org.monora.uprotocol.client.android.util.HomeFragHelper

class HomeAdapter(private var itemsList: ArrayList<HomeModel>, private val mHomeListener: HomeListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_TYPE_STORAGE, VIEW_TYPE_CATEGORIES -> {
                ViewHolderType1(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_home_adapter_type1, parent, false)
                )
            }
            VIEW_TYPE_SHARE -> {
                ViewHolderTypeShare(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_home_adapter_type_share, parent, false)
                )
            }
            else -> throw UnsupportedOperationException()
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val homeModel = itemsList[position]

        when (homeModel.viewType) {
            AppConfig.STORAGE_LOCATION -> {
                (holder as ViewHolderType1).typeName.text = homeModel.typeName
              //  holder.mount.visibility = View.VISIBLE
                holder.mount.setOnClickListener {
                    homeModel.context!!.startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(AppConfig.URI_SHIV_SHAMBHU_DONATE)))
                }

                holder.mRecyclerView.visibility = View.VISIBLE
                val storageLayoutManager: RecyclerView.LayoutManager =
                    LinearLayoutManager(homeModel.context, LinearLayoutManager.HORIZONTAL, false)
                holder.mRecyclerView.layoutManager = storageLayoutManager

                val adapter = StorageAdapter(homeModel.context!!, HomeFragHelper.getStorageLocation(homeModel.context))
                // Setting the Adapter with the recyclerview
                holder.mRecyclerView.adapter = adapter
            }
            AppConfig.CATEGORY -> {
                (holder as ViewHolderType1).typeName.text = homeModel.typeName
              //  holder.mount.visibility = View.VISIBLE
                holder.mount.setOnClickListener {
                    homeModel.context!!.startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(AppConfig.URI_SHIV_SHAMBHU_DONATE)))
                }

                holder.mGridRecyclerView.visibility = View.VISIBLE
                val adapter = HomeCategoryAdapter(homeModel.context!!, getHomeCategory())
                holder.mGridRecyclerView.adapter = adapter
            }
            AppConfig.SHARE -> {
                (holder as ViewHolderTypeShare).typeNameShare.text = homeModel.typeName

                holder.sendButton.setOnClickListener {
                    mHomeListener.onShareClicked("sendButton")
                }
                holder.receiveButton.setOnClickListener {
                    mHomeListener.onShareClicked("receiveButton")
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    class ViewHolderType1(view: View) : RecyclerView.ViewHolder(view) {
        var typeName: TextView = view.findViewById(R.id.itemTextView)
        var mount: MaterialButton = view.findViewById(R.id.mount)
        var mRecyclerView: RecyclerView = view.findViewById(R.id.mRecyclerView)
        var mGridRecyclerView: RecyclerView = view.findViewById(R.id.mGridRecyclerView)
    }

    class ViewHolderTypeShare(view: View) : RecyclerView.ViewHolder(view) {
        var typeNameShare: TextView = view.findViewById(R.id.itemTextView)
        var sendButton: MaterialButton = view.findViewById(R.id.sendButton)
        var receiveButton: MaterialButton = view.findViewById(R.id.receiveButton)
    }

    companion object {
        const val VIEW_TYPE_STORAGE = 0
        const val VIEW_TYPE_SHARE = 1
        const val VIEW_TYPE_CATEGORIES = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemsList[position].viewType) {
            AppConfig.STORAGE_LOCATION -> {
                VIEW_TYPE_STORAGE
            }
            AppConfig.SHARE -> {
                VIEW_TYPE_SHARE
            }
            AppConfig.CATEGORY -> {
                VIEW_TYPE_CATEGORIES
            }
            else -> -1
        }
    }

    private fun getHomeCategory(): ArrayList<CategoryModel> {

        val dataCategories = ArrayList<CategoryModel>()
        dataCategories.clear()

        dataCategories.add(CategoryModel(AppConfig.CATEGORY_AUDIO, "____",
            R.drawable.ic_music_note_white_24dp))
        dataCategories.add(CategoryModel(AppConfig.CATEGORY_DOWNLOAD, "____",
            R.drawable.ic_file_download_white_24dp))
        dataCategories.add(CategoryModel(AppConfig.CATEGORY_IMAGE, "____",
            R.drawable.ic_photo_white_24dp))
        dataCategories.add(CategoryModel(AppConfig.CATEGORY_VIDEO, "____",
            R.drawable.ic_video_white_24dp))
        dataCategories.add(CategoryModel(AppConfig.CATEGORY_DOCUMENT_OTHER, "____",
            R.drawable.ic_file_document_box_white_24dp))
        dataCategories.add(CategoryModel(AppConfig.CATEGORY_APP, "____",
            R.drawable.ic_android_head_white_24dp))

        return dataCategories
    }

}