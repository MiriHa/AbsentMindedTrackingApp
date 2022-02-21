package com.example.trackingapp.activity.esm

import androidx.annotation.ArrayRes
import com.example.trackingapp.R

sealed interface ESMItem {
    val question: Int
    val questionType: ESMQuestionType
    var value: String
    val visible: Boolean
}

class ESMSliderItem(
    override val question: Int,
    override val questionType: ESMQuestionType,
    override val visible: Boolean  = true,
    override var value: String = "",
    val sliderStepSize: Float,
    val sliderMax: Float,
    val sliderMin: Float,
    val sliderMinLabel: String = sliderMin.toInt().toString(),
    val sliderMaxLabel: String = sliderMax.toInt().toString()
) : ESMItem

class ESMDropDownItem(
    override val question: Int,
    override val questionType: ESMQuestionType,
    override val visible: Boolean  = true,
    override var value: String = "",
    @ArrayRes val dropdownList: Int
) : ESMItem

class ESMRadioGroupItem(
    override val question: Int,
    override val questionType: ESMQuestionType,
    override val visible: Boolean = true,
    override var value: String = "",
    val buttonList: List<Int> = arrayListOf(R.string.esm_button_yes, R.string.esm_button_no)
) : ESMItem


enum class ESMQuestionType{
    ESM_UNLOCK_INTENTION,
    ESM_LOCK_Q_FINISH,
    ESM_LOCK_Q_MORE,
    ESM_LOCK_Q_TRACK_OF_TIME,
    ESM_LOCK_Q_TRACK_OF_SPACE,
    ESM_LOCK_Q_EMOTION,
    ESM_LOCK_Q_REGRET,
    ESM_LOCK_Q_AGENCY,
}