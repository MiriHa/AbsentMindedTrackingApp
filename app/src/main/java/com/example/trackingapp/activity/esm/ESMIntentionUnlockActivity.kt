package com.example.trackingapp.activity.esm

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.trackingapp.DatabaseManager
import com.example.trackingapp.DatabaseManager.saveToDataBase
import com.example.trackingapp.R
import com.example.trackingapp.databinding.LayoutEsmIntentionOverlayBinding
import com.example.trackingapp.models.ESMState
import com.example.trackingapp.models.LogEvent
import com.example.trackingapp.models.LogEventName
import com.example.trackingapp.util.NotificationHelper.dismissESMNotification
import java.util.*


class ESMIntentionUnlockActivity: AppCompatActivity(){
    private lateinit var viewModel: ESMIntentionViewModel
    private lateinit var binding: LayoutEsmIntentionOverlayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, ESMIntentionViewModelFactory())[ESMIntentionViewModel::class.java]

        binding = LayoutEsmIntentionOverlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //DatabaseManager.getSavedIntentions()

        val adapter = ArrayAdapter(
            this@ESMIntentionUnlockActivity,
            R.layout.support_simple_spinner_dropdown_item,
            ArrayList(DatabaseManager.intentionList)
        )

        binding.esmUnlockAutoCompleteTextView.apply {
            setAdapter(adapter)
            threshold = 1
            setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus)
                    this.showDropDown()
            }
            setOnEditorActionListener{ _, actionID, _ ->
                if(actionID == EditorInfo.IME_ACTION_DONE){
                    actionDone()
                    return@setOnEditorActionListener true
                }
                false
            }
        }

        binding.esmUnlockButtonstart.setOnClickListener {
            actionDone()
        }

    }

    override fun onBackPressed() {
        // Do Nothing
    }

   private fun actionDone(){
       val intention = binding.esmUnlockAutoCompleteTextView.text.toString()
       if(intention.isNotBlank()){
        viewModel.checkDuplicateIntentionAnSave(intention)
        dismissFullScreenNotification()
           LogEvent(
               LogEventName.ESM,
               System.currentTimeMillis(),
               ESMState.ESM_UNLOCK.name,
               intention
           ).saveToDataBase()
       } else {
           Toast.makeText(this, R.string.esm_unlock_intention_error, Toast.LENGTH_LONG).show()
       }
    }

    private fun dismissFullScreenNotification(){
        this.finish()
        moveTaskToBack(true)
        dismissESMNotification(this)
    }

}