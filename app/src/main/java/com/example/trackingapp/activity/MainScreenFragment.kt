package com.example.trackingapp.activity


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trackingapp.DatabaseManager.saveToDataBase
import com.example.trackingapp.R
import com.example.trackingapp.databinding.CustomListItemBinding
import com.example.trackingapp.databinding.FragmentMainscreenBinding
import com.example.trackingapp.models.LogEvent
import com.example.trackingapp.models.LogEventName
import com.example.trackingapp.sensor.AbstractSensor
import com.example.trackingapp.service.LoggingManager
import com.example.trackingapp.util.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainScreenFragment : Fragment() {

    private val TAG = "TRACKINGAPP_Main_Screen_Fragment"

    private lateinit var binding: FragmentMainscreenBinding
    private lateinit var viewModel: MainScreenViewModel
    private lateinit var mContext: Context

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var listAdapter: ListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, MainScreenViewModelFactory())[MainScreenViewModel::class.java]

        binding = FragmentMainscreenBinding.inflate(inflater)
        val view = binding.root

        setHasOptionsMenu(true)

        notificationManager = NotificationManagerCompat.from(mContext)
        NotificationHelper.dismissESMNotification(mContext)

        setAdapter()

        val loggingObserver = Observer<Boolean> {
            setAdapter()
        }

        LoggingManager.isLoggingActive.observe(this, loggingObserver)

        LoggingManager.ensureLoggingManagerIsAlive(mContext)
        if (!SharedPrefManager.getBoolean(CONST.PREFERENCES_LOGGING_FIRST_STARTED)) {
            LoggingManager.startLoggingService(mContext as Activity)
        }

        binding.buttonTest.apply {
            text = getString(
                R.string.mainScreen_logging_start_button
            )
            setOnClickListener {
                Log.d(TAG, "startLoggingButton Click: running")
                LoggingManager.stopLoggingService(mContext)
                LoggingManager.startLoggingService(mContext as Activity)
                LogEvent(LogEventName.LOGIN, System.currentTimeMillis(), "startLoggingService", "test").saveToDataBase()
                //text = getString(R.string.logging_stop_button)
            }
        }

        binding.buttonTestStop.apply {
            text = getString(R.string.mainScreen_logging_stop_button)
            setOnClickListener {
                LoggingManager.stopLoggingService(mContext)
            }
        }

        return view
    }

    private fun setAdapter() {
            listAdapter = ListAdapter()
            binding.recyclerviewFragmentMainscreen.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(this@MainScreenFragment.mContext)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_setting, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.toolbarOption_logout -> {
                Firebase.auth.signOut()
                SharedPrefManager.saveBoolean(CONST.PREFERENCES_DATA_RECORDING_ACTIVE, false)
                LoggingManager.stopLoggingService(mContext)
                navigate(ScreenType.Welcome, ScreenType.HomeScreen)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    inner class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
        private val items: MutableList<AbstractSensor>
            get() = LoggingManager.sensorList

        inner class ViewHolder(val binding: CustomListItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = CustomListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder) {
                with(items[position]) {
                    binding.textViewListItemTitle.text = this.sensorName + " Sensor"
                    val runningText =  if (LoggingManager.isLoggingActive.value == true) getString(R.string.mainScreen_sensorList_is_running) else getString(R.string.mainScreen_sensorList_not_running)
                      //  if (this.isRunning) getString(R.string.mainScreen_sensorList_is_running) else getString(R.string.mainScreen_sensorList_not_running)
                    val drawable = if (LoggingManager.isLoggingActive.value == true) R.drawable.circle_green else R.drawable.circle_red
                    binding.textViewListItemSubtile.apply{
                        text = runningText
                        setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
                        compoundDrawablePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.resources.displayMetrics).toInt()
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

}