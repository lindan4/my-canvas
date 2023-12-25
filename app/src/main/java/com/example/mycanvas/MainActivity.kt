package com.example.mycanvas

import android.app.Dialog
import android.media.Image
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private var drawingView: DrawingActivity? = null

    private var brushSelectionButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.a_draw_view)
        drawingView?.setSizeForBrush(20.toFloat())

        brushSelectionButton = findViewById(R.id.brush_selection)

        brushSelectionButton?.setOnClickListener {
            showBrushSizeDialog()
        }
    }

    private fun showBrushSizeDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Select Brush Size")

        val smallButton: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        val mediumButton: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        val largeButton: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)

        smallButton.setOnClickListener {
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }

        mediumButton.setOnClickListener {
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        largeButton.setOnClickListener {
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }
}
