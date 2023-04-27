package bart.saj.myapplication

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_nutrition.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profile : Fragment() {
    // TODO: Rename and change types of parameters

    //private lateinit var email: String
    private var email: String? = null
    private var param1: String? = null
    private var param2: String? = null

    var btnNutrition: Button? = null
    var visitLogin: Button? = null
    var visitInfo: Button? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    companion object {
        fun newInstance(email: String): Profile {
            val fragment = Profile()
            fragment.email = email
            return fragment
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visitInfo = getView()?.findViewById<Button>(R.id.btnVisitTutorial)
        visitInfo?.setOnClickListener{
            (context as MainActivity).replaceFragment(Tutorial())
        }

        visitLogin = getView()?.findViewById<Button>(R.id.visitLogin)
        visitLogin?.setOnClickListener {
            (context as MainActivity).replaceFragment(Login())
        }
        val btnChange = view.findViewById<Button>(R.id.btnChange)
        btnChange.setOnClickListener {
            showUpdateDialog()
        }
        val showBmiButton = view.findViewById<Button>(R.id.showBmiButton)
        showBmiButton.setOnClickListener {
            showBmiPopup()
        }
        val user = mAuth.currentUser
        val userId = user?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val weight = documentSnapshot.getDouble("weight")
                        val height = documentSnapshot.getDouble("height")

                        // Setting the text of the TextViews to display the weight and height
                        weightTextView2.text = "$weight kg"
                        heightTextView2.text = "$height cm"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
        fetchTotalData()
    }
    private fun showBmiPopup() {
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

                        val bmiCategory = when {
                            bmi == null -> "Invalid data"
                            bmi < 18.5 -> "Underweight"
                            bmi >= 18.5 && bmi < 25 -> "Healthy weight"
                            bmi >= 25 && bmi < 30 -> "Overweight"
                            else -> "Obese"
                        }

                        val message = String.format("Your BMI is: %.1f\nCategory: %s", bmi, bmiCategory)
                        AlertDialog.Builder(requireContext())
                            .setTitle("BMI Result")
                            .setMessage(message)
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val userEmail: TextView = view.findViewById(R.id.tvUserName)
        val user = mAuth.currentUser

        if (user != null) {
            userEmail.text = user.email ?: "null"
        } else {
            userEmail.text = "null"
        }

        return view
    }
    private fun fetchTotalData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val db = FirebaseFirestore.getInstance()
        val stepsRef = db.collection("steps").document(userId!!)
        stepsRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                var totalSteps = 0
                var totalCalories = 0.0

                for (key in documentSnapshot.data!!.keys) {
                    if (key != "date") {
                        val dataArray = documentSnapshot.get(key) as ArrayList<*>
                        for (data in dataArray) {
                            val dataMap = data as HashMap<*, *>
                            totalSteps += (dataMap["stepCount"] as? Number)?.toInt() ?: 0
                            totalCalories += (dataMap["caloriesBurned"] as? Number)?.toDouble() ?: 0.0
                        }
                    }
                }

                val totalStepsTextView: TextView? = view?.findViewById(R.id.tvTodaySteps)
                val totalCaloriesTextView: TextView? = view?.findViewById(R.id.tvTodayCalories)
                totalStepsTextView?.text = "Total Steps: $totalSteps"
                totalCaloriesTextView?.text = "Total Calories: ${"%.1f".format(totalCalories)}"
            } else {
                Log.d(TAG, "No steps document found for user: $userId")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error getting steps document for user: $userId", e)
        }
    }


    private fun showUpdateDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Update Weight and Height")

        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.update_weight_height_dialog, null)

        val etWeight = dialogLayout.findViewById<EditText>(R.id.etWeight)
        val etHeight = dialogLayout.findViewById<EditText>(R.id.etHeight)

        builder.setView(dialogLayout)

        builder.setPositiveButton("Update") { _, _ ->
            val newWeight = etWeight.text.toString().toDoubleOrNull()
            val newHeight = etHeight.text.toString().toDoubleOrNull()

            if (newWeight != null && newHeight != null) {
                updateWeightHeight(newWeight, newHeight)
            } else {
                Toast.makeText(requireContext(), "Please enter valid values", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun updateWeightHeight(newWeight: Double, newHeight: Double) {
        val user = mAuth.currentUser
        val userId = user?.uid

        if (userId != null) {
            val userData = hashMapOf("weight" to newWeight, "height" to newHeight) as Map<String, Any>
            db.collection("users").document(userId).update(userData)
                .addOnSuccessListener {
                    weightTextView2.text = "$newWeight kg"
                    heightTextView2.text = "$newHeight cm"

                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Update failed with ", exception)
                }
        }
    }




}