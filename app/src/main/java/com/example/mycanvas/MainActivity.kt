package com.example.mycanvas

import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private var drawingView: DrawingActivity? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.a_draw_view)
        drawingView?.setSizeForBrush(20.toFloat())
    }
}
