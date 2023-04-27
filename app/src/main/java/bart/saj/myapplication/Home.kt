package bart.saj.myapplication

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_workout.*
import java.text.DecimalFormat
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context.SENSOR_SERVICE
import android.graphics.Color
import android.util.Log
import android.view.*
import com.androidplot.ui.HorizontalPositioning
import com.androidplot.ui.VerticalPositioning
import com.androidplot.xy.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.lang.Math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


interface StepCountListener {
    fun onStepCountUpdated(stepCount: Int)
}
class Home :   Fragment(), StepCountListener {
    private var weekOffset = 0
    private var param1: String? = null
    private var param2: String? = null
    var timerTextView: TextView? = null
    var StartTime: Long = 0
    var timerHandler = Handler()
    var waterCount = 0

    var counter = 0 // step counter




    var tvx: TextView? = null
    var tvy: TextView? = null
    var tvz: TextView? = null
    var tvMag: TextView? = null
    var tvSteps: TextView? = null
    var tvDistance: TextView? = null
    var tvCalories: TextView? = null
    var tvWater: TextView? = null
  //  var startButton: Button? = getView()?.findViewById<Button>(R.id.startButton)
    private var mSensorManager: SensorManager? = null
    private var mSensor: Sensor? = null
    var startClicked = false
    var btnWater: Button? = null
    var btnRun: Button? = null

    private var startTime = 0L
    private var timeInMilliseconds = 0L
    private var timeSwapBuff = 0L
    private var updatedTime = 0L
    private var t = 0L
    private var secs = 0
    private var mins = 0
    private var milliseconds = 0
    private var timerStarted = false
    private lateinit var graphLayout: LinearLayout
    private lateinit var caloriesBurnedTextView: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var weight: Double? = 70.0
    private var height: Double? = 180.0
    private var displaySteps = true // keeping track of current histogram display mode
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val caloriesBurnedTextView = requireView().findViewById<TextView>(R.id.tvCalories)

        val stepCounter = StepCounter(requireContext(), this, weight, height)

        val stepCountTextView = getView()?.findViewById<TextView>(R.id.tvSteps)
        // caloriesBurnedTextView = requireView().findViewById<TextView>(R.id.tvCalories)
        // Set the initial step count
        stepCountTextView?.text = stepCounter.getStepCount().toString()


        // Initialize FirebaseAuth and FirebaseFirestore
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val user = mAuth.currentUser
        val userId = user?.uid
        // Fetch weight and height from Firestore for the currently authenticated user

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                         weight = documentSnapshot.getDouble("weight")
                         height = documentSnapshot.getDouble("height")
                    }
                }
        }

        // Update the step count in the TextView when it changes
        val stepCountListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == stepCounter.stepSensor.type) {
                    stepCountTextView?.text = stepCounter.getStepCount().toString()

                    val steps = stepCounter.getStepCount()
                    val caloriesBurned = stepCounter.calculateCalories(steps, weight, height)
                    Log.d(TAG, "Calories and weight and height logged: $caloriesBurned  $weight  $height")
                    caloriesBurnedTextView.text = "%.1f".format(caloriesBurned)

                }
            }
        }

        // Register the step counter listener on the stepCounter object
        stepCounter.registerListener(stepCountListener)

        btnWater = getView()?.findViewById<Button>(R.id.btnWater)

        btnWater?.setOnClickListener{
            doHydrate()
        }


        displayStepsOnHistogramWithWeekOffset(weekOffset)
        val toggleDisplayButton = view.findViewById<Button>(R.id.toggleDisplayButton)

        // Set the OnClickListener for the button
        toggleDisplayButton.setOnClickListener {
            displaySteps = !displaySteps // Toggle the display mode
            if (displaySteps) {
                displayStepsOnHistogramWithWeekOffset(weekOffset)
            } else {
                displayCaloriesOnHistogramWithWeekOffset(weekOffset)
            }
        }
        val chart = view.findViewById<BarChart>(R.id.stepsGraph)
        setupChartSwipeListener(chart)
        //temporarySteps()
    }
    private var r: Runnable = object : Runnable {
        override fun run() {

            // get current time

            var millis = System.currentTimeMillis() - StartTime

            // call postdelayed with 1000 millisecs
            timerHandler.postDelayed(this, 1000)
        }
    }


    override fun onStepCountUpdated(stepCount: Int) {
        val stepCountTextView = getView()?.findViewById<TextView>(R.id.tvSteps)
        stepCountTextView?.text = stepCount.toString()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
      // Check if the permission to access activity recognition has been granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Requesting the permission from the user
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                Companion.PERMISSION_REQUEST_ACTIVITY_RECOGNITION
            )
        } else {
            // Permission is already granted, start the sensor
            startSensor()
        }


        return inflater.inflate(R.layout.fragment_home, container, false)



    }

    private fun startSensor() {
        val stepCounter = StepCounter(requireContext(), this, weight, height)
        val sensorManager = requireContext().getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(stepCounter, stepCounter.stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        val stepCounter = StepCounter(requireContext(), this, weight, height)
        stepCounter.unregister()
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_THRESHOLD_VELOCITY = 200
    }




    fun doHydrate() {
        waterCount += 250
        tvWater = getView()?.findViewById<TextView>(R.id.tvWater)
        Log.d("MyTag", "This is a debug message")

        tvWater?.setText("$waterCount ml / 2000 ml")

        if(waterCount == 2000)
        {
            waterPopup()
        }
    }
    fun waterPopup() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Water Goal Reached!")
        builder.setMessage("You reached your daily goal for water drank.")
        builder.setPositiveButton("OK") { dialog, which ->
            // Add any actions you want to perform when the "OK" button is clicked
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    fun displayStepsOnHistogramWithWeekOffset(weekOffset: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -7 * (weekOffset + 1))
        val startDate = cal.time
        cal.add(Calendar.DATE, 7)
        val endDate = cal.time

        val db = FirebaseFirestore.getInstance()
        val stepsRef = db.collection("steps").document(userId!!)
        stepsRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val stepsByDay = mutableMapOf<String, Int>()
                for (key in documentSnapshot.data!!.keys) {
                    if (key != "date") {
                        val stepsArray = documentSnapshot.get(key) as ArrayList<*>
                        for (step in stepsArray) {
                            val stepData = step as HashMap<*, *>
                            val stepCount = stepData["stepCount"] as Long
                            val dateStr = stepData["date"] as String
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                            val dateKey = SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(date)
                            stepsByDay[dateKey] = (stepsByDay[dateKey] ?: 0) + stepCount.toInt()
                        }
                    }
                }

                val domainLabels = mutableListOf<String>()
                val stepsValues = mutableListOf<Double>()
                val calendar = Calendar.getInstance()
                calendar.time = startDate
                while (calendar.time <= endDate) {
                    val formattedDate = SimpleDateFormat("d", Locale.getDefault()).format(calendar.time)
                    domainLabels.add(formattedDate)
                    val dateKey = SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(calendar.time)
                    val steps = stepsByDay[dateKey] ?: 0
                    stepsValues.add(steps.toDouble())
                    calendar.add(Calendar.DATE, 1)
                }

                val barEntries = stepsValues.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }

                val barDataSet = BarDataSet(barEntries, "Steps")
                barDataSet.color = Color.BLUE
                barDataSet.valueTextColor = Color.WHITE
                barDataSet.valueTextSize = 12f
                barDataSet.setDrawValues(true)

                val barData = BarData(barDataSet)

                val plot = view?.findViewById<BarChart>(R.id.stepsGraph)
                plot?.apply {
                    data = barData
                    description.isEnabled = false
                    setScaleEnabled(false)
                    setTouchEnabled(true)
                    legend.isEnabled = false
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        textColor = Color.WHITE
                        valueFormatter = IndexAxisValueFormatter(domainLabels)
                        textSize = 12f
                        granularity = 1f
                        setDrawGridLines(false)
                    }
                    axisLeft.apply {
                        textColor = Color.WHITE
                        setDrawGridLines(false)
                        setDrawLabels(true)
                        textSize = 12f
                    }
                    axisRight.isEnabled = false
                    invalidate()
                }
                updateMonthTextView()
            } else {
                Log.d(TAG, "No steps document found for user: $userId")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error getting steps document for user: $userId", e)
        }
    }
    fun displayCaloriesOnHistogramWithWeekOffset(weekOffset: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -7 * (weekOffset + 1))
        val startDate = cal.time
        cal.add(Calendar.DATE, 7)
        val endDate = cal.time

        val db = FirebaseFirestore.getInstance()
        val stepsRef = db.collection("steps").document(userId!!)
        stepsRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val caloriesByDay = mutableMapOf<String, Double>()
                for (key in documentSnapshot.data!!.keys) {
                    if (key != "date") {
                        val caloriesArray = documentSnapshot.get(key) as ArrayList<*>
                        for (steps in caloriesArray) {
                            val stepData = steps as HashMap<*, *>
                            val caloriesCount = (stepData["caloriesBurned"] as? Number)?.toDouble() ?: 0.0

                            val dateStr = stepData["date"] as String
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                            val dateKey = SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(date)
                            caloriesByDay[dateKey] = (caloriesByDay[dateKey] ?: 0.0) + caloriesCount
                        }
                    }
                }

                val domainLabels = mutableListOf<String>()
                val caloriesValues = mutableListOf<Double>()
                val calendar = Calendar.getInstance()
                calendar.time = startDate
                while (calendar.time <= endDate) {
                    val formattedDate = SimpleDateFormat("d", Locale.getDefault()).format(calendar.time)
                    domainLabels.add(formattedDate)
                    val dateKey = SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(calendar.time)
                    val calories = caloriesByDay[dateKey] ?: 0
                    caloriesValues.add(calories.toDouble())
                    calendar.add(Calendar.DATE, 1)
                }

                val barEntries = caloriesValues.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }

                val barDataSet = BarDataSet(barEntries, "Steps")
                barDataSet.color = Color.parseColor("#FFA500")
                barDataSet.valueTextColor = Color.WHITE
                barDataSet.valueTextSize = 12f
                barDataSet.setDrawValues(true)

                val barData = BarData(barDataSet)

                val plot = view?.findViewById<BarChart>(R.id.stepsGraph)
                plot?.apply {
                    data = barData
                    description.isEnabled = false
                    setScaleEnabled(false)
                    setTouchEnabled(true)
                    legend.isEnabled = false
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        textColor = Color.WHITE
                        valueFormatter = IndexAxisValueFormatter(domainLabels)
                        textSize = 12f
                        granularity = 1f
                        setDrawGridLines(false)
                    }
                    axisLeft.apply {
                        textColor = Color.WHITE
                        setDrawGridLines(false)
                        setDrawLabels(true)
                        textSize = 12f
                    }
                    axisRight.isEnabled = false
                    invalidate()
                }
                updateMonthTextView()

            } else {
                Log.d(TAG, "No steps document found for user: $userId")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error getting steps document for user: $userId", e)
        }
    }
    private fun updateMonthTextView() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -7 * (weekOffset + 1))
        val startDate = cal.time
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(startDate)
        val monthTextView = view?.findViewById<TextView>(R.id.monthTextView)
        monthTextView?.text = monthName
    }

    private fun setupChartSwipeListener(chart: BarChart) {
        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null || e2 == null) return false

                val deltaX = e2.x - e1.x
                if (abs(deltaX) > SWIPE_MIN_DISTANCE && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (deltaX < 0) {
                        // Swipe left (previous week)
                        weekOffset--
                    } else {
                        // Swipe right (next week)
                        weekOffset++
                    }

                    // Display steps or calories based on the current display mode
                    if (displaySteps) {
                        displayStepsOnHistogramWithWeekOffset(weekOffset)
                    } else {
                        displayCaloriesOnHistogramWithWeekOffset(weekOffset)
                    }
                    updateMonthTextView()
                    return true
                }
                return false
            }
        })

        chart.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }


    /* fun displayStepsOnGraph() {
         val userId = FirebaseAuth.getInstance().currentUser?.uid
         val cal = Calendar.getInstance()
         cal.add(Calendar.DATE, -7)
         val startDate = cal.time
         val endDate = Date()

         val db = FirebaseFirestore.getInstance()
         val stepsRef = db.collection("steps").document(userId!!)
         stepsRef.get().addOnSuccessListener { documentSnapshot ->
             if (documentSnapshot.exists()) {
                 val stepsByDay = mutableMapOf<String, Int>()
                 for (document in documentSnapshot.data!!.keys) {
                     if (document != "date") {
                         val stepsArray = documentSnapshot.get(document) as ArrayList<*>
                         for (step in stepsArray) {
                             val stepData = step as HashMap<*, *>
                             val stepCount = stepData["stepCount"] as Long
                             val dateStr = stepData["date"] as String
                             val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                             val dateKey = SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(date)
                             println("stepCount: $stepCount")
                             println("dateStr: $dateStr")
                             stepsByDay[dateKey] = (stepsByDay[dateKey] ?: 0) + stepCount.toInt()
                         }
                     }
                 }

                 val plot = view?.findViewById<XYPlot>(R.id.stepsGraph)
                 plot?.clear()


                 plot?.setTitle("Steps taken over the past 7 days")


                 plot?.setDomainLabel("Date")
                 plot?.setRangeLabel("Steps")

                 val domainLabels = mutableListOf<String>()
                 val stepsValues = mutableListOf<Double>()
                 val calendar = Calendar.getInstance()
                 calendar.time = startDate
                 while (calendar.time <= endDate) {
                     val formattedDate = SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(calendar.time)
                     domainLabels.add(formattedDate)
                     val steps = stepsByDay[formattedDate] ?: 0
                     stepsValues.add(steps.toDouble())
                     calendar.add(Calendar.DATE, 1)
                 }
                 val domainValues = domainLabels.map { SimpleDateFormat("EEE MMM d", Locale.getDefault()).parse(it).time.toDouble() }

                 val stepsSeries = SimpleXYSeries(domainValues, stepsValues, null)
                 val formatter = LineAndPointFormatter(Color.BLUE, null, null, null)
                 plot?.addSeries(stepsSeries, formatter)


                 plot?.setRangeBoundaries(0.0, null, BoundaryMode.AUTO)
                 plot?.setDomainBoundaries(startDate.time.toDouble(), endDate.time.toDouble(), BoundaryMode.AUTO)


                 plot?.redraw()

             } else {
                 Log.d(TAG, "No steps document found for user: $userId")
             }
         }.addOnFailureListener { e ->
             Log.e(TAG, "Error getting steps document for user: $userId", e)
         }
     }*/


   /* fun temporarySteps() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid


        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -7)
        val startDate = cal.time
        val endDate = Date()


        val db = FirebaseFirestore.getInstance()
        val stepsRef = db.collection("steps").document(userId!!)
        stepsRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val stepsByDay = mutableMapOf<String, Int>()
                for (document in documentSnapshot.data!!.keys) {
                    if (document != "date") {
                        val stepsArray = documentSnapshot.get(document) as ArrayList<*>
                        for (step in stepsArray) {
                            val stepData = step as HashMap<*, *>
                            val stepCount = stepData["stepCount"] as Long
                            val dateStr = stepData["date"] as String

                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)


                            val dateKey = SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(date)
                                stepsByDay[dateKey] = (stepsByDay[dateKey] ?: 0) + stepCount.toInt()
                                println("Added $stepCount steps on $dateKey")

                        }
                    }
                }


                val textView = getView()?.findViewById<TextView>(R.id.firestoreSteps)
                val calendar = Calendar.getInstance()
                calendar.time = startDate
                while (calendar.time <= endDate) {
                    val formattedDate = SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(calendar.time)
                    val steps = stepsByDay[formattedDate] ?: 0
                    val text = "$formattedDate: $steps\n"
                    textView?.append(text)
                    calendar.add(Calendar.DATE, 1)
                }

            } else {
                Log.d(TAG, "No steps document found for user: $userId")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error getting steps document for user: $userId", e)
        }
    }*/





}