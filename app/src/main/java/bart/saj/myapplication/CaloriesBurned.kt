package bart.saj.myapplication

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CaloriesBurned(private val onComplete: (caloriesBurned: Double) -> Unit) {
    private val mAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    fun fetchUserWeightAndHeight(steps: Int) {
        val user = mAuth.currentUser
        val userId = user?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val weight = documentSnapshot.getDouble("weight")
                        val height = documentSnapshot.getDouble("height")
                        if (weight != null && height != null) {
                            val caloriesBurned = calculateCaloriesBurned(weight, height, steps)
                            onComplete(caloriesBurned)
                        } else {
                            Log.w(TAG, "Weight or height data missing for user: $userId")
                        }
                    } else {
                        Log.w(TAG, "User document not found for user: $userId")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error getting user document", e)
                }
        } else {
            Log.w(TAG, "User is not authenticated, cannot fetch weight and height")
        }
    }

    private fun calculateCaloriesBurned(weight: Double, height: Double, steps: Int): Double {
        val distanceInMeters = (height * steps) / 1000
        val walkingMet = 0.035 * weight
        val caloriesBurned = (distanceInMeters * walkingMet) / 1000
        return caloriesBurned
    }
}
