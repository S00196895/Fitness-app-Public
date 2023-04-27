package bart.saj.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Fruits.newInstance] factory method to
 * create an instance of this fragment.
 */
data class Fruit(val name: String, val quantity: Int, val image: String)

class FruitViewModel : ViewModel() {
    val fruit = MutableLiveData<Fruit>()

    fun addFruit(name: String, quantity: Int, image: String) {
        val newFruit = Fruit(name, quantity, image)
        fruit.value = newFruit
    }
}

class Fruits : Fragment() {
    private lateinit var fruitViewModel: FruitViewModel
    private lateinit var adapter: ArrayAdapter<*>
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
       /* adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.fruits))
        lv_listView.adapter = adapter
        lv_listview.onItemClickListener = AdapterView.OnItemClickListener(parent, view, position, id ->
            Toast.makeText(applicationContext, parent?.getItemAtPosition(position).toString(), ))*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fruits, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Fruits.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                Fruits().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}