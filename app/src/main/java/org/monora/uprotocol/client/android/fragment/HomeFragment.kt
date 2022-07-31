package org.monora.uprotocol.client.android.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.activity.ContentBrowserActivity
import org.monora.uprotocol.client.android.activity.MusicActivity
import org.monora.uprotocol.client.android.adapter.HomeAdapter
import org.monora.uprotocol.client.android.config.AppConfig
import org.monora.uprotocol.client.android.listener.HomeListener
import org.monora.uprotocol.client.android.model.HomeModel

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.layout_home_fragment), HomeListener {

    val data = ArrayList<HomeModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val homeRecyclerview = view.findViewById<RecyclerView>(R.id.homeRecyclerview)
        homeRecyclerview.layoutManager = LinearLayoutManager(context)

        homeData()
        val homeAdapter = HomeAdapter(data, this)
        homeRecyclerview.adapter = homeAdapter

        bottomNavigationView.isClickable = false
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> homeAdapter.notifyDataSetChanged()
                R.id.transfer -> findNavController().navigate(HomeFragmentDirections.actionHomeTransferHistoryFragment())
                R.id.leaf_music -> requireContext().startActivity(Intent(context, MusicActivity::class.java))
            }
            true
        }
    }

    private fun homeData() {
        data.clear()
        // viewType = storage
        data.add(HomeModel(context, AppConfig.STORAGE_LOCATION, getString(R.string.text_storage) + " (in beta)"))
        // viewType = share
        data.add(HomeModel(context, AppConfig.SHARE, getString(R.string.share)))
        //viewType = categories
        data.add(HomeModel(context, AppConfig.CATEGORY, getString(R.string.categories) + " (in beta)"))
    }

    override fun onShareClicked(button: String) {
        when (button) {
            "sendButton" -> startActivity(Intent(context, ContentBrowserActivity::class.java))
            "receiveButton" -> findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNavReceive())
        }
    }

}