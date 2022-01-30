package com.example.trackingapp.activity.esm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.trackingapp.DatabaseManager
import com.example.trackingapp.DatabaseManager.saveToDataBase
import com.example.trackingapp.models.ESMState
import com.example.trackingapp.models.ESM_Intention_Lock_Answer
import com.example.trackingapp.models.LogEvent
import com.example.trackingapp.models.LogEventName
import com.example.trackingapp.util.SharedPrefManager
import java.util.*

class ESMIntentionViewModel : ViewModel() {

    var esmLockQuestion1answered = false
    var esmLockQuestion2Answered = false

    val savedIntention
        get() = SharedPrefManager.getLastSavedIntention()

    fun makeLogQuestion1(isIntentionFinished: Boolean) {
        esmLockQuestion1answered = true
        val answer = if (isIntentionFinished) ESM_Intention_Lock_Answer.ESM_INTENTION_FINISHED else ESM_Intention_Lock_Answer.ESM_INTENTION_UNFINISHED

        LogEvent(
            LogEventName.ESM,
            System.currentTimeMillis(),
            ESMState.ESM_LOCK_Q1.name,
            answer.toString(),
            savedIntention
        ).saveToDataBase()
    }

    fun makeLogQuestion2(moreThanInitialIntention: Boolean) {
        esmLockQuestion2Answered = true
        val answer = if (moreThanInitialIntention) ESM_Intention_Lock_Answer.ESM_MORE_THAN_INITIAL_INTENTION else ESM_Intention_Lock_Answer.ESM_NOT_MORE_THAN_INITIAL_INTENTION

        LogEvent(
            LogEventName.ESM,
            System.currentTimeMillis(),
            ESMState.ESM_LOCK_Q2.name,
            answer.toString(),
            savedIntention
        ).saveToDataBase()
    }

    fun checkDuplicateIntentionAnSave(newIntention: String) {
        SharedPrefManager.saveLastIntention(newIntention)
        if (!DatabaseManager.intentionList.contains(newIntention)) {
            //save new Intention to Firebase
            DatabaseManager.saveNewIntention(Date(), newIntention)
        }
    }

}

class ESMIntentionViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ESMIntentionViewModel::class.java)) {
            return ESMIntentionViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}