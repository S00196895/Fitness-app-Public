package bart.saj.myapplication

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import bart.saj.myapplication.databinding.ActivityMainBinding
import java.text.DecimalFormat


class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    class SharedViewModel : ViewModel() {

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(WelcomePage())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragment(Home())
                R.id.exercise -> replaceFragment(Exercise())
                R.id.maprun -> replaceFragment(MapRun())
                R.id.profile -> replaceFragment(Profile())

                else ->{

                }
            }
            true
        }
    }

     fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
}