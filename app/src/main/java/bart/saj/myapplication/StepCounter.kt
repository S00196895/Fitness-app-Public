package bart.saj.myapplication
import android.content.ContentValues.TAG
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class StepCounter(context: Context, private val stepCountListener: StepCountListener, private var weight: Double?, private var height: Double?) : SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val stepSensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var stepCount = 0

    //Using the shared preference API for local storage
    private val sharedPreferences = context.getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE)
    private val stepCountKey = "stepCount"


    // Update the step count every 5 minutes
    //private var stepCountUpdateInterval = 5 * 60 * 1000 // 5 minutes in milliseconds
    private var stepCountUpdateInterval = 30 * 1000 // 30 seconds in milliseconds for testing
    private var lastStepCountUpdateTime: Long = System.currentTimeMillis()
    private var lastCalorieUpdateTime: Long = 0
    private val calorieUpdateInterval: Long = 10 * 1000 // 10 seconds

    init {
        // Registering the step counter sensor listener
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Retrieving the saved step count from SharedPreferences
        stepCount = sharedPreferences.getInt(stepCountKey, 0)
        stepCountListener.onStepCountUpdated(stepCount)
    }

    // Unused for now
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun calculateCalories(steps: Int, weight: Double?, height: Double?): Float {

        val defaultWeight = 70.0
        val defaultHeight = 180.0

        val actualWeight = weight ?: defaultWeight
        val actualHeight = height ?: defaultHeight

        val walkingSpeedMetersPerSecond = 1.0f
        val distanceMeters = steps * (actualHeight / 100) * 0.414f
        val timeHours = (distanceMeters / walkingSpeedMetersPerSecond) / 3600.0f
        val met = 3.0f
        val caloriesBurned = met * actualWeight * timeHours
        return caloriesBurned.toFloat()
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == stepSensor) {
            val newStepCount = event.values[0].toInt()

            if (stepCount == 0) {
                // Load the step count from SharedPreferences if it hasn't been set yet
                val savedStepCount = sharedPreferences.getInt(stepCountKey, 0)
                stepCount = savedStepCount
            }

            if (newStepCount > stepCount) {
                // Updating the step count
                stepCount = newStepCount
                stepCountListener.onStepCountUpdated(stepCount)

                val auth = FirebaseAuth.getInstance()

                if (auth.currentUser != null && System.currentTimeMillis() - lastStepCountUpdateTime >= stepCountUpdateInterval) {
                    val db = Firebase.firestore
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    weight = documentSnapshot.getDouble("weight")
                                    height = documentSnapshot.getDouble("height")
                                }
                            }
                    }
                    val caloriesBurned = calculateCalories(stepCount, weight, height)
                    // Get the steps document for the current user
                    val docRef = db.collection("steps").document(userId!!)
                    docRef.get()
                        .addOnSuccessListener { document ->
                           // val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            // Check if there's a date array for today
                            if (document.exists()) {
                                val data = document.data
                                val todaySteps = data?.get(currentDate) as? ArrayList<HashMap<String, Any>>?

                                if (todaySteps != null && todaySteps.isNotEmpty()) {
                                    // Update the step count for today
                                    val firstStepItem = todaySteps[0]
                                    firstStepItem["caloriesBurned"] = caloriesBurned
                                    firstStepItem["stepCount"] = stepCount
                                    docRef.update(currentDate, todaySteps)
                                } else {
                                    // Create a new date array for today with one item
                                    //val newStepItem = hashMapOf("date" to System.currentTimeMillis(), "stepCount" to stepCount)
                                    val newStepItem = hashMapOf("date" to currentDate, "stepCount" to stepCount, "caloriesBurned" to caloriesBurned)
                                    val newDateArray = arrayListOf(newStepItem)
                                    docRef.update(currentDate, newDateArray)
                                }
                            } else {
                                // Create a new document for the user with the current date array
                               // val newStepItem = hashMapOf("date" to System.currentTimeMillis(), "stepCount" to stepCount)
                                val newStepItem = hashMapOf("date" to currentDate, "stepCount" to stepCount, "caloriesBurned" to caloriesBurned)
                                val newDateArray = arrayListOf(newStepItem)
                                val newDocument = hashMapOf(userId to hashMapOf(currentDate to newDateArray))
                                db.collection("steps").document(userId)
                                    .set(newDocument)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Steps document successfully written!")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error writing steps document", e)
                                    }
                            }

                            // Save the updated step count to SharedPreferences
                            val editor = sharedPreferences.edit()
                            editor.putInt(stepCountKey, stepCount)
                            editor.apply()

                            lastStepCountUpdateTime = System.currentTimeMillis()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error getting steps document", e)
                        }

                } else if (auth.currentUser == null) {
                    // User is not authenticated, cannot write step count
                    Log.w(TAG, "User is not authenticated, cannot write step count")
                }

            }
        }
    }



    fun getStepCount(): Int {
        return stepCount
    }

    fun registerListener(listener: SensorEventListener) {
        sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregister() {
        sensorManager.unregisterListener(this, stepSensor)
    }
}