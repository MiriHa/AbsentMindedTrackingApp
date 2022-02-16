package com.example.trackingapp.activity.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.trackingapp.DatabaseManager
import com.example.trackingapp.databinding.FragmentOnboradingBinding
import com.example.trackingapp.service.LoggingManager
import com.example.trackingapp.util.PermissionManager
import com.example.trackingapp.util.ScreenType
import com.example.trackingapp.util.navigate

class OnBoardingFragment: Fragment() {

    val TAG = "ONBORADING_FRAGMENT"
    private lateinit var binding: FragmentOnboradingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOnboradingBinding.inflate(inflater)
        val view = binding.root

        binding.onboradingLoginButton.setOnClickListener {
            navigate(to=ScreenType.Login, from=ScreenType.Welcome)
        }
        binding.onboradingSignupButton.setOnClickListener {
            navigate(to=ScreenType.SignUp, from=ScreenType.Welcome)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
       if(DatabaseManager.isUserLoggedIn){
           //G0 to the MainScreen
           if(PermissionManager.areAllPermissionGiven(this.activity)) {
               LoggingManager.isDataRecordingActive = true
               navigate(to=ScreenType.HomeScreen, from=ScreenType.Welcome)
           } else {
               navigate(to=ScreenType.Permission, from=ScreenType.Welcome)
           }
       } else {
           LoggingManager.isDataRecordingActive = false
       }

    }
}