package bart.saj.myapplication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_nutrition.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Nutrition.newInstance] factory method to
 * create an instance of this fragment.
 */
class Nutrition : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private val TAG = "Nutrition"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        return inflater.inflate(R.layout.fragment_nutrition, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = mAuth.currentUser
        val userId = user?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val weight = documentSnapshot.getDouble("weight")
                        val height = documentSnapshot.getDouble("height")

                        val heightInMeters = height?.div(100)
                        val bmi = weight?.div((heightInMeters?.times(heightInMeters)) ?: 0.0)

                        // Setting the text of the TextViews to display the weight, height, and BMI
                        weightTextView.text = "$weight kg"
                        heightTextView.text = "$height cm"
                        bmiTextView.text = String.format("%.1f", bmi)

                        // Calculate the minimum and maximum healthy BMIs for a given height
                        val minHeightInMeters = 1.5
                        val maxHeightInMeters = 2.0
                        val minHealthyBMI = 18.5 * minHeightInMeters * minHeightInMeters
                        val maxHealthyBMI = 25.0 * maxHeightInMeters * maxHeightInMeters

                        if (bmi != null) {
                            if (bmi < 18.5) {
                                bmiRangeTextView.text = "Underweight"
                                bmiRangeTextView.setTextColor(Color.parseColor("#FF0000"))
                               // bmiImageView.setImageResource(R.drawable.ic_x)
                            } else if (bmi >= 18.5 && bmi < 25) {
                                bmiRangeTextView.text = "Healthy weight"
                                bmiRangeTextView.setTextColor(Color.parseColor("#00FF00"))
                                //bmiImageView.setImageResource(R.drawable.ic_checkmark)
                            } else if (bmi >= 25 && bmi < 30) {
                                bmiRangeTextView.text = "Overweight"
                                bmiRangeTextView.setTextColor(Color.parseColor("#FF0000"))
                               // bmiImageView.setImageResource(R.drawable.ic_minus)
                            } else {
                                bmiRangeTextView.text = "Obese"
                                bmiRangeTextView.setTextColor(Color.parseColor("#FF0000"))
                                //bmiImageView.setImageResource(R.drawable.ic_x)
                            }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Nutrition.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Nutrition().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}