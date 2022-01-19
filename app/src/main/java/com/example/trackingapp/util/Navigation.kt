package com.example.trackingapp.util

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.trackingapp.R
import java.security.InvalidParameterException

enum class ScreenType {Welcome, SignUp, Login, HomeScreen}
const val TAG = "NAVIGATION"

fun Fragment.navigate(to: ScreenType, from: ScreenType) {
    if(to == from){
        throw InvalidParameterException("Can't navigate to same screen type!")
    }

    var navOptions = NavOptions.Builder().build()

    Log.d(TAG,"navigate from: $from to : $to")

    if( (from == ScreenType.Welcome && to != ScreenType.HomeScreen)
        || (from == ScreenType.Login && to == ScreenType.HomeScreen)
        || (from == ScreenType.SignUp && to == ScreenType.HomeScreen)){
       navOptions = NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
    }

    when(to){
        ScreenType.Welcome -> {
            findNavController().navigate(R.id.OnBoardingFragment,null, navOptions)
        }
        ScreenType.Login -> {
            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }
        ScreenType.SignUp -> {
            findNavController().navigate(R.id.signUpFragment, null, navOptions)
        }
        ScreenType.HomeScreen -> {
            findNavController().navigate(R.id.mainScreenFragment,null, navOptions)
        }
    }

}