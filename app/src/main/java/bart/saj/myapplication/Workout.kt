package bart.saj.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import bart.saj.myapplication.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.fragment_workout.*
import android.os.CountDownTimer
import android.os.Handler
import android.os.SystemClock
import android.widget.TextView
import bart.saj.myapplication.ExerciseData
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Workout.newInstance] factory method to
 * create an instance of this fragment.
 */
class Workout : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var weight: Double? = null
    private var height: Double? = null
    private var totalCaloriesBurned = 0.0
    private var currentTimer: MyCounter? = null

    private lateinit var serviceIntent: Intent
    private var time = 0.0
    private var timerStarted = false
    private var currentExerciseIndex = 0

    private var startTime = 0L
    private var timeInMilliseconds = 0L
    private var timeSwapBuff = 0L
    private var updatedTime = 0L
    private var t = 0L
    private var secs = 0
    private var mins = 0
    private var milliseconds = 0
    private lateinit var workoutType: String
    private lateinit var tvExerciseName: TextView
    private var currentBreakTimer: CountDownTimer? = null


    private val exercises by lazy {
        when (workoutType) {
            "Cardio" -> listOf(
                ExerciseData("Jogging", 10000, R.drawable.joggif),
                ExerciseData("Squats", 10000, R.drawable.squatgif),
                ExerciseData("Jumping Jacks", 10000, R.drawable.gifjacks),
                ExerciseData("Burpees", 10000, R.drawable.burpees)
            )
            "Abs" -> listOf(
                ExerciseData("Crunches", 10000, R.drawable.crunch),
                ExerciseData("Sit-ups", 10000, R.drawable.situp),
                ExerciseData("Leg Raises", 10000, R.drawable.legraise),
                ExerciseData("Plank", 10000, R.drawable.plank)
            )
            "Chest/Shoulders" -> listOf(
                ExerciseData("Dumbbell Chest Fly", 10000, R.drawable.fly2),
                ExerciseData("Raised Dumbbell Bench Press", 10000, R.drawable.benchpress),
                ExerciseData("Dumbbell Front Raise", 10000, R.drawable.frontraise3),
                ExerciseData("Dumbbell Side Raise", 10000, R.drawable.sideraise)
            )
            "Legs" -> listOf(
                ExerciseData("Lunges", 10000, R.drawable.lunge),
                ExerciseData("Squats", 10000, R.drawable.squatgif),
                ExerciseData("Calf Raises", 10000, R.drawable.calfraises),
                ExerciseData("Side Leg Lifts", 10000, R.drawable.leglift)
            )
            else -> emptyList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            workoutType = it.getString(ARG_WORKOUT_TYPE) ?: "Cardio"
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvExerciseName = view.findViewById(R.id.tvExerciseName)

        val auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val userId = auth.currentUser?.uid
        btnSkip.setOnClickListener { skipExercise() }
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        weight = documentSnapshot.getDouble("weight")
                        height = documentSnapshot.getDouble("height")
                    }
                }
        }


        btnStart.setOnClickListener { startExercise() }

    }
    private fun skipExercise() {
        // Cancel the current timer and break timer
        currentTimer?.cancel()
        currentBreakTimer?.cancel()

        if (currentExerciseIndex + 1 < exercises.size) {
            currentExerciseIndex++
            startExercise()
        } else {
            // If it's the last exercise, finish the workout
            // Workout finished
            val totalTime = tvTotalTime.text.toString()
            val workoutSummaryFragment = WorkoutSummary.newInstance(totalTime, totalCaloriesBurned)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, workoutSummaryFragment)
                .commit()
        }
    }




    private fun calculateCaloriesBurned(weight: Double, height: Double, durationSeconds: Long, exercise: ExerciseData): Double {
        val metValue = when (exercise.name) {
            "Jogging" -> 7.0
            "Squats" -> 4.0
            "Jumping Jacks" -> 8.0
            "Burpees" -> 9.5
            "Crunches" -> 3.8
            "Sit-ups" -> 4.0
            "Leg Raises" -> 3.5
            "Plank" -> 2.0
            "Dumbbell Chest Fly" -> 3.0
            "Raised Dumbbell Bench Press" -> 5.0
            "Dumbbell Front Raise" -> 3.5
            "Dumbbell Side Raise" -> 3.5
            "Lunges" -> 5.0
            "Calf Raises" -> 2.8
            "Side Leg Lifts" -> 3.0
            else -> 1.0 // Resting MET value
        }

        val durationHours = durationSeconds / 3600.0
        return metValue * weight * durationHours
    }

    private fun startExercise() {
        val currentExercise = exercises[currentExerciseIndex]


        // Update the exercise name
        tvExerciseName.text = currentExercise.name

        Glide.with(this)
            .asGif()
            .load(currentExercise.gifResource)
            .into(imgExercise)

        // Cancel the previous timer if it exists
        currentTimer?.cancel()

        val timer = MyCounter(currentExercise.duration, 1000)
        timer.start()

        // Update the reference to the current timer
        currentTimer = timer

        if (!timerStarted) {
            startTimer()
            timerStarted = true
        }
    }



    private fun startTimer() {
        startTime = SystemClock.uptimeMillis()
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                if (currentExerciseIndex == exercises.size) {
                    handler.removeCallbacks(this)
                }
                if (isAdded && tvTotalTime != null) {
                    timeInMilliseconds = SystemClock.uptimeMillis() - startTime
                    updatedTime = timeSwapBuff + timeInMilliseconds
                    secs = (updatedTime / 1000).toInt()
                    mins = secs / 60
                    secs %= 60
                    milliseconds = (updatedTime % 1000).toInt()
                    tvTotalTime.text = "$mins:" + String.format("%02d", secs) + ":" + String.format("%03d", milliseconds)
                }
                handler.postDelayed(this, 0)
            }
        })
    }

    inner class MyCounter(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            println("Timer Completed.")
            if (isAdded && tvTotalTime != null && tvShortTimer != null) {
                tvShortTimer.text = "Timer Completed."
            }

            if (weight != null && height != null) {
                val caloriesBurned = calculateCaloriesBurned(weight!!, height!!, exercises[currentExerciseIndex].duration / 1000, exercises[currentExerciseIndex])
                totalCaloriesBurned += caloriesBurned
                if (isAdded && tvCaloriesWorkout != null) {
                    tvCaloriesWorkout.text = totalCaloriesBurned.toString()
                }
            }

            // Add a 15-second break between exercises
            val breakDuration = 15000L
            val breakTimer = object : CountDownTimer(breakDuration, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (isAdded && tvTotalTime != null && tvShortTimer != null) {
                        tvShortTimer.text = "Break: ${(millisUntilFinished / 1000)}"
                    }
                }

                override fun onFinish() {
                    currentExerciseIndex++
                    if (currentExerciseIndex < exercises.size) {
                        startExercise()
                    } else {
                        // Workout finished
                        val totalTime = tvTotalTime.text.toString()
                        val workoutSummaryFragment = WorkoutSummary.newInstance(totalTime, totalCaloriesBurned)
                        parentFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, workoutSummaryFragment)
                            .commit()
                    }
                }
            }
            breakTimer.start()
            currentBreakTimer = breakTimer
        }

        override fun onTick(millisUntilFinished: Long) {
            if (isAdded && tvTotalTime != null && tvShortTimer != null) {
                tvShortTimer.text = (millisUntilFinished / 1000).toString() + ""
                println("Timer  : " + millisUntilFinished / 1000)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    companion object {
        private const val ARG_WORKOUT_TYPE = "workout_type"
        @JvmStatic
        fun newInstance(workoutType: String) =
            Workout().apply {
                arguments = Bundle().apply {
                    putString(ARG_WORKOUT_TYPE, workoutType)
                }
            }
    }
}