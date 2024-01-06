package com.example.mycanvas

import android.app.Dialog
import android.os.Build
import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : ComponentActivity() {

    private val cameraResultLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){
            isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Camera permissions granted", Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(this, "Camera permissions denied", Toast.LENGTH_LONG).show()
            }
        }


    private val cameraAndLocationResultLauncher: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
                permissions.entries.forEach{
                    val permissionName = it.key
                    val isPermissionGranted = it.value

                    if (isPermissionGranted) {
                        when (permissionName) {
                            Manifest.permission.CAMERA -> {
                                Toast.makeText(this, "Camera permissions granted", Toast.LENGTH_LONG).show()

                            }
                            Manifest.permission.ACCESS_FINE_LOCATION -> {
                                Toast.makeText(this, "Location permissions granted", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    else {
                        when (permissionName) {
                            Manifest.permission.CAMERA -> {
                                Toast.makeText(this, "Camera permissions denied", Toast.LENGTH_LONG).show()

                            }
                            Manifest.permission.ACCESS_FINE_LOCATION -> {
                                Toast.makeText(this, "Location permissions denied", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
    }


    private var drawingView: DrawingActivity? = null

    private var currentImageColorButton: ImageButton? = null

    private var galleryButton: ImageButton? = null

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

        galleryButton = findViewById(R.id.gallery_selection)

        galleryButton?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showRationaleDialog("Camera permission","The camera cannot be used since camera permissions were denied.")
            }
            else {
                cameraAndLocationResultLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    private fun showRationaleDialog(
        title: String,
        message: String,
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
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
