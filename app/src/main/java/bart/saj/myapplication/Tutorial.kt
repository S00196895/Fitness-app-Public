package bart.saj.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Tutorial.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tutorial : Fragment() {

    private lateinit var tutorialImage: ImageView
    private lateinit var tutorialText: TextView
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button

    private val tutorialPages = listOf(
        TutorialPage(R.drawable.fitsum_high_resolution_color_logo, "Welcome to the tutorial page, reading through this guide should only" +
                "take around 3 minutes and will consist of 14 screens including this one"),
        TutorialPage(R.drawable.tutorial1, "The Navigation bar shows you which screen you are currently on. " +
                "You can visit the home page, Exercise page, Map my run page or the profile page from the navigation bar"),
        TutorialPage(R.drawable.tutorial2, "At the top of the Home screen you can see your " +
                "daily steps and calories burned"),
        TutorialPage(R.drawable.tutorial3, "The histogram either shows steps or calories burned in the past week " +
                "depending on the mode it is currently in. The mode can be toggled with the button. " +
                "You can also see previous weeks by swiping left or right on the histogram"),
        TutorialPage(R.drawable.tutorial4, "By pressing the hydration button you can increase " +
                "your daily water intake by 250ml (approx one glass of water). This will " +
                "allow you to track your daily water intake"),
        TutorialPage(R.drawable.tutorial5, "From the exercise menu you can select which list of exercises routine " +
                "you would like to start "),
        TutorialPage(R.drawable.tutorial6, "On the workout page you can start the exercise routine with the start " +
                "button and skip the exercises with the skip button. Each exercise lasts 45 seconds with a " +
                "15 second break in-between. Your calories burned will be tracked as well as the time exercising"),
        TutorialPage(R.drawable.tutorial6_5, "Once the exercise is complete the results page " +
                "will show you your total time exercising and total calories burned. You can go back" +
                "to the list of exercises with the click of a button"),
        TutorialPage(R.drawable.tutorial7, "On the map my run page, you can see the map of the world. " +
                "By pressing the start button and giving the application necessary permissions, the map will " +
                "start tracking your location and draw a path behind you. By pressing the pause button " +
                "The map y run functionality is disabled until start is clicked again."),
        TutorialPage(R.drawable.tutorial8, "On the profile page you can see your email at the top of the screen. " +
                "You can also access the tutorial again with the click of a button"),
        TutorialPage(R.drawable.tutorial9, "Here you will also see your lifetime steps and calories burned"),
        TutorialPage(R.drawable.tutorial10, "Your weight and height will also be displayed here which you can " +
                "change at any time"),
        TutorialPage(R.drawable.tutorial11, "The show BMI button will show you your BMI calculated using" +
                "your weight and height"),
        TutorialPage(R.drawable.tutorial12, "By clicking the log-in button on the profile page you will be " +
                "navigated to the log-in screen. Here you can type in your user credentials to log-in, or register" +
                "with a click of a button.")
    )

    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tutorial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tutorialImage = view.findViewById(R.id.tutorial_image)
        tutorialText = view.findViewById(R.id.tutorial_text)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnNext = view.findViewById(R.id.btnNext)

        updatePage()

        btnPrevious.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }

        btnNext.setOnClickListener {
            if (currentPage < tutorialPages.size - 1) {
                currentPage++
                updatePage()
            }
        }
    }

    private fun updatePage() {
        val tutorialPage = tutorialPages[currentPage]
        tutorialImage.setImageResource(tutorialPage.imageRes)
        tutorialText.text = tutorialPage.text

        btnPrevious.isEnabled = currentPage > 0
        btnNext.isEnabled = currentPage < tutorialPages.size - 1

        if (currentPage == 0) {
            btnPrevious.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray))
        } else {
            btnPrevious.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_primary))
        }

        if (currentPage == tutorialPages.size - 1) {
            btnNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray))
        } else {
            btnNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_primary))
        }
    }

    data class TutorialPage(val imageRes: Int, val text: String)
}