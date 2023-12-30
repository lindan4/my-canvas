package com.example.mycanvas

import android.app.Dialog
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : ComponentActivity() {

    private var drawingView: DrawingActivity? = null

    private var currentImageColorButton: ImageButton? = null

    private var brushSelectionButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.a_draw_view)
        drawingView?.setSizeForBrush(20.toFloat())

        val colorLinearLayout: LinearLayout = findViewById(R.id.paint_colour_selection)

        currentImageColorButton = colorLinearLayout[0] as ImageButton
        currentImageColorButton!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))

        drawingView?.setColor(currentImageColorButton!!.tag.toString());

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

    fun paintClicked(view: View) {
        if (view !== currentImageColorButton) {
            val viewAsImgButton = view as ImageButton
            currentImageColorButton?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_normal))
            viewAsImgButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))
            drawingView?.setColor(viewAsImgButton.tag.toString());
            currentImageColorButton = viewAsImgButton
        }
    }
}
