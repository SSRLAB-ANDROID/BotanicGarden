package com.ssrlab.audioguide.botanic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.databinding.FragmentInfoBinding

class FragmentInfo: Fragment() {

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
            infoPhoneText1.setOnClickListener { mainActivity.intentToDialer("+375293201717") }
            infoPhoneText2.setOnClickListener { mainActivity.intentToDialer("+375173796915") }
            infoEmailText.setOnClickListener { mainActivity.intentToMail("office@cbg.org.by") }
            infoWeblinkText.setOnClickListener { mainActivity.intentToBrowser("https://cbg.org.by/") }
            infoInstagram.setOnClickListener { mainActivity.intentToBrowser("https://www.instagram.com/botanicalgardenminsk") }
            infoFacebook.setOnClickListener { mainActivity.intentToBrowser("https://www.facebook.com/BotanicalGardenMinsk") }
            infoVk.setOnClickListener { mainActivity.intentToBrowser("https://vk.com/botanicalgardenminsk") }
        }
    }
}