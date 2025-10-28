package com.dis.audioguide.botanic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dis.audioguide.botanic.MainActivity
import com.dis.audioguide.botanic.R
import com.dis.audioguide.botanic.databinding.FragmentInfoBinding

class FragmentInfo : Fragment() {

    private lateinit var binding: FragmentInfoBinding
    private lateinit var mainActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentInfoBinding.inflate(layoutInflater)

        binding.infoBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setUpInfoNavigation()

        return binding.root
    }

    private fun setUpInfoNavigation() {
        binding.apply {
            infoPhoneText1.setOnClickListener { mainActivity.intentToDialer(resources.getString(R.string.intent_phone_number)) }
            infoPhoneText2.setOnClickListener { mainActivity.intentToDialer(resources.getString(R.string.intent_phone_number_2)) }
            infoEmailText.setOnClickListener { mainActivity.intentToMail(resources.getString(R.string.email)) }
            infoWeblinkText.setOnClickListener { mainActivity.intentToBrowser(resources.getString(R.string.site)) }
            infoInstagram.setOnClickListener { mainActivity.intentToBrowser(resources.getString(R.string.intent_instagram)) }
            infoFacebook.setOnClickListener { mainActivity.intentToBrowser(resources.getString(R.string.intent_facebook)) }
            infoVk.setOnClickListener { mainActivity.intentToBrowser(resources.getString(R.string.intent_vk)) }
        }
    }
}