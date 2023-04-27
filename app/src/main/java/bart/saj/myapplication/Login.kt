package bart.saj.myapplication

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var db: FirebaseFirestore
class Login : Fragment() {
    // TODO: Rename and change types of parameters

    val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        arguments?.let {

        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        btnWantRegister?.setOnClickListener{
            showRegistrationDialog()
        }

        loginButton.setOnClickListener{
            doLogin()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Login().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun showRegistrationDialog() {
        val inflater = layoutInflater
        val registerView = inflater.inflate(R.layout.dialog_register, null)

        val emailEditText = registerView.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = registerView.findViewById<EditText>(R.id.passwordEditText)
        val weightEditText = registerView.findViewById<EditText>(R.id.weightEditText)
        val heightEditText = registerView.findViewById<EditText>(R.id.heightEditText)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Register")
            .setView(registerView)
            .setPositiveButton("Register") { _, _ ->
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                val weight = weightEditText.text.toString().toDoubleOrNull()
                val height = heightEditText.text.toString().toDoubleOrNull()

                if (email.isNotBlank() && password.isNotBlank() && weight != null && height != null) {
                    doRegister(email, password, weight, height)
                } else {
                    Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }
    private fun doRegister(email: String, password: String, weight: Double, height: Double) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign up success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = mAuth.currentUser
                    storeWeightAndHeight(weight, height)

                } else {
                    // If sign up fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }
            }
    }

    private fun doLogin() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = mAuth.currentUser
                    if (user != null) {
                        /*val profile = Profile.newInstance(email)
                        activity?.supportFragmentManager?.beginTransaction()
                            ?.replace(R.id.profiles, profile)
                            ?.commit()*/
                        //Please God work
                        val bundle = Bundle()
                        bundle.putString("data", email)
                        val profileFragment = Profile()
                        profileFragment.arguments = bundle
                        fragmentManager?.beginTransaction()?.replace(R.id.frame_layout, profileFragment)?.commit()
                    } else {
                        // User is signed out, show the signed-out UI
                        // For example, you can show a sign-in button or a message indicating that the user is not signed in
                    }
                  // updateUI(user, email)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                  // updateUI(null)
                }
            }
    }
    private fun storeWeightAndHeight(weight: Double, height: Double) {
        val user = mAuth.currentUser
        val userId = user?.uid
        if (userId != null) {
            val data = hashMapOf(
                "weight" to weight,
                "height" to height
            )
            db.collection("users").document(userId).set(data)
                .addOnSuccessListener {
                    Log.d(TAG, "Weight and height successfully written!")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing weight and height", e)
                }
        }
    }

}