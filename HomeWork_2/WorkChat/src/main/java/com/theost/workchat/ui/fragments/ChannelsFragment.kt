package com.theost.workchat.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.theost.workchat.data.models.state.ChannelsType
import com.theost.workchat.data.models.state.ResourceStatus
import com.theost.workchat.databinding.FragmentChannelsBinding
import com.theost.workchat.ui.adapters.core.BaseAdapter
import com.theost.workchat.ui.adapters.delegates.ChannelAdapterDelegate
import com.theost.workchat.ui.adapters.delegates.TopicAdapterDelegate
import com.theost.workchat.ui.interfaces.SearchHandler
import com.theost.workchat.ui.interfaces.TopicListener
import com.theost.workchat.ui.viewmodels.ChannelsViewModel

class ChannelsFragment : Fragment(), SearchHandler {

    private var channelsType = ChannelsType.ALL
    private val userId = 0
    private val adapter = BaseAdapter()

    private val viewModel: ChannelsViewModel by viewModels()

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentChannelsBinding.inflate(layoutInflater)

        binding.channelsList.adapter = adapter.apply {
            addDelegate(ChannelAdapterDelegate() { channelId, isSelected ->
                viewModel.updateTopics(channelId, isSelected)
            })
            addDelegate(TopicAdapterDelegate() { topicId ->
                (activity as TopicListener).showTopicDialog(topicId)
            })
        }

        viewModel.loadingStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                ResourceStatus.SUCCESS -> hideShimmerLayout()
                ResourceStatus.ERROR -> { /* todo */ }
                ResourceStatus.LOADING ->  {}
                else -> {}
            }
        }
        viewModel.allData.observe(viewLifecycleOwner) { adapter.submitList(it)}
        viewModel.loadData(userId, channelsType)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        channelsType = savedInstanceState?.getSerializable(CHANNELS_TYPE_EXTRA) as? ChannelsType
            ?: (arguments?.getSerializable(CHANNELS_TYPE_EXTRA) ?: 0) as ChannelsType
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onSearch(query: String) {
        viewModel.filterData(query)
    }

    private fun hideShimmerLayout() {
        binding.shimmerLayout.shimmer.visibility = View.GONE
        binding.channelsList.visibility = View.VISIBLE
    }

    companion object {
        private const val CHANNELS_TYPE_EXTRA = "channels_type"

        fun newFragment(channelsType: ChannelsType): Fragment {
            val fragment = ChannelsFragment()
            val bundle = Bundle()
            bundle.putSerializable(CHANNELS_TYPE_EXTRA, channelsType)
            fragment.arguments = bundle
            return fragment
        }
    }

}