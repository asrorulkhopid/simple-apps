package com.dicoding.courseschedule.ui.add

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.dicoding.courseschedule.R
import com.dicoding.courseschedule.data.Course
import com.dicoding.courseschedule.util.TimePickerFragment
import com.google.android.material.textfield.TextInputEditText

class AddCourseActivity : AppCompatActivity(), TimePickerFragment.DialogTimeListener {

    private lateinit var addCourseViewModel: AddCourseViewModel
    private lateinit var edName : TextInputEditText
    private lateinit var spinnerDay : Spinner
    private lateinit var tvStartTime : TextView
    private lateinit var tvEndTime : TextView
    private lateinit var ibStartTime: ImageButton
    private lateinit var ibEndTime: ImageButton
    private lateinit var edLecturer : TextInputEditText
    private lateinit var edNote : TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)
        supportActionBar?.title = resources.getString(R.string.add_course)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        init()

        popUpTimePicker()
        addCourseViewModel.saved.observe(this){ event ->
            event.getContentIfNotHandled()?.let { isSaved ->
                if (isSaved) {
                    Toast.makeText(this,"Successfully added the course",Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this,"Failed to add course, Please make sure to fill in the course name and time",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun init(){
        val factory = AddCourseViewModelFactory.createFactory(this)
        addCourseViewModel = ViewModelProvider(this, factory).get(AddCourseViewModel::class.java)
        edName = findViewById(R.id.ed_course_name)
        spinnerDay = findViewById(R.id.spinner_day)
        tvStartTime = findViewById(R.id.tv_start_time)
        tvEndTime = findViewById(R.id.tv_end_time)
        ibStartTime = findViewById(R.id.ib_start_time)
        ibEndTime = findViewById(R.id.ib_end_time)
        edLecturer = findViewById(R.id.ed_lecturer)
        edNote = findViewById(R.id.ed_note)
    }

    private fun popUpTimePicker(){
        ibStartTime.setOnClickListener{
            pickTime(START_TIME)
        }
        ibEndTime.setOnClickListener{
            pickTime(END_TIME)
        }
    }

    private fun pickTime(tag: String){
        val timePickerFragment = TimePickerFragment()
        timePickerFragment.show(supportFragmentManager,tag )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return true
    }
    
    private fun getData() : Course{
        val courseName = edName.text.toString()
        val day = spinnerDay.selectedItemPosition
        val startTime = tvStartTime.text.toString()
        val endTime = tvEndTime.text.toString()
        val lecturer = edLecturer.text.toString()
        val note = edNote.text.toString()
        return Course(
            courseName = courseName,
            day = day,
            startTime = startTime,
            endTime = endTime,
            lecturer = lecturer,
            note = note
        )
    }
    

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_insert -> {
                val (id,courseName,day,startTime,endTime,lecturer,note) = getData()
                addCourseViewModel.insertCourse(courseName,day,startTime,endTime,lecturer,note)
            }
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }

    override fun onDialogTimeSet(tag: String?, hour: Int, minute: Int) {
        val hour = if(hour.toString().length == 1) "0$hour" else hour
        val minute = if(minute.toString().length == 1) "0$minute" else minute
        if(tag == START_TIME){
            tvStartTime.text = "$hour:$minute"
        }
        if(tag == END_TIME){
            tvEndTime.text = "$hour:$minute"
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object{
        const val START_TIME = "start_time"
        const val END_TIME = "end_time"
    }
}