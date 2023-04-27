package bart.saj.myapplication

import android.os.Bundle
import android.text.Layout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_exercise.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Exercise.newInstance] factory method to
 * create an instance of this fragment.
 */
class Exercise : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var cardioLayout: LinearLayout? = null
    private var absLayout: LinearLayout? = null
    private var chestShouldersLayout: LinearLayout? = null
    private var legsLayout: LinearLayout? = null
    var textViewCardio: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       // val fragment = NextFragment()
        cardioLayout = getView()?.findViewById<LinearLayout>(R.id.cardioLayout)
        absLayout = getView()?.findViewById<LinearLayout>(R.id.absLayout)
        chestShouldersLayout = getView()?.findViewById<LinearLayout>(R.id.chestShouldersLayout)
        legsLayout = getView()?.findViewById<LinearLayout>(R.id.legsLayout)

        cardioLayout?.setOnClickListener {
            Toast.makeText(requireActivity(), "Cardio clicked", Toast.LENGTH_LONG).show()
            (context as MainActivity).replaceFragment(Workout.newInstance("Cardio"))
        }
        absLayout?.setOnClickListener {
            Toast.makeText(requireActivity(), "Abs clicked", Toast.LENGTH_LONG).show()
            (context as MainActivity).replaceFragment(Workout.newInstance("Abs"))
        }

        chestShouldersLayout?.setOnClickListener {
            Toast.makeText(requireActivity(), "Chest/Shoulders clicked", Toast.LENGTH_LONG).show()
            (context as MainActivity).replaceFragment(Workout.newInstance("Chest/Shoulders"))
        }

        legsLayout?.setOnClickListener {
            Toast.makeText(requireActivity(), "Legs clicked", Toast.LENGTH_LONG).show()
            (context as MainActivity).replaceFragment(Workout.newInstance("Legs"))
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_exercise, container, false)
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Exercise.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Exercise().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}