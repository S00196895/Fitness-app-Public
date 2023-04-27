package bart.saj.myapplication

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_workout_summary.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WorkoutSummary.newInstance] factory method to
 * create an instance of this fragment.
 */
class WorkoutSummary : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workout_summary, container, false)

        // Get data from arguments
        val totalTime = arguments?.getString("total_time") ?: ""
        val totalCalories = arguments?.getDouble("total_calories") ?: 0.0

        // Set data to TextViews
        view.tvTotalWorkoutTime.text = "Total Time: $totalTime"
        view.tvTotalCaloriesBurned.text = "Total Calories Burned: " + String.format("%.2f", totalCalories)

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backToExerciseButton: Button? = view?.findViewById<Button>(R.id.backToExerciseButton)
        backToExerciseButton?.setOnClickListener {
            (context as MainActivity).replaceFragment(Exercise())
        }

    }
    companion object {
        @JvmStatic
        fun newInstance(totalTime: String, totalCalories: Double) =
            WorkoutSummary().apply {
                arguments = Bundle().apply {
                    putString("total_time", totalTime)
                    putDouble("total_calories", totalCalories)
                }
            }
    }
}