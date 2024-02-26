package com.example.mycanvas

import android.app.Dialog
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imgBackground: ImageView = findViewById(R.id.img_view_back)

                imgBackground.setImageURI(result.data?.data)
            }
    }

    private val requestPermissions: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
                permissions.entries.forEach{
                    val permissionName = it.key
                    val isPermissionGranted = it.value

                    Log.i("permission", "${permissionName} ${isPermissionGranted}" )

                    if (isPermissionGranted) {
                        when (permissionName) {
                            Manifest.permission.READ_MEDIA_IMAGES -> {
                                Toast.makeText(this, "Read storage permissions granted", Toast.LENGTH_LONG).show()
                                val imageSelectionIntent: Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                                openGalleryLauncher.launch(imageSelectionIntent)
                            }
                            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                                Toast.makeText(this, "Read storage permissions granted", Toast.LENGTH_LONG).show()
                                val imageSelectionIntent: Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                                openGalleryLauncher.launch(imageSelectionIntent)
                            }
                            Manifest.permission.WRITE_EXTERNAL_STORAGE  -> {
                                Toast.makeText(this, "Write storage permissions granted", Toast.LENGTH_LONG).show()
                                saveBitmapScope()
                            }

                        }
                    }
                    else {
                        when (permissionName) {
                            Manifest.permission.READ_MEDIA_IMAGES -> {
                                Toast.makeText(this, "Read storage permissions denied", Toast.LENGTH_LONG).show()
                            }
                            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                                Toast.makeText(this, "Read storage permissions denied", Toast.LENGTH_LONG).show()
                            }
                            Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                                Toast.makeText(this, "Write storage permissions denied", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
    }

//    private fun isReadStorageAllowed(): Boolean {
//        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
//        } else {
//            TODO("VERSION.SDK_INT < TIRAMISU")
//            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//        }
//
//        return result == PackageManager.PERMISSION_GRANTED
//    }

    private fun isWriteStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

//    private val writeCanvasToStorage() {
//
//    }


    private var drawingView: DrawingActivity? = null

    private var currentImageColorButton: ImageButton? = null

    private var undoButton: ImageButton? = null

    private var galleryButton: ImageButton? = null

    private var brushSelectionButton: ImageButton? = null

    private var saveButton: ImageButton? = null

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

        undoButton = findViewById(R.id.undo_selection)

        undoButton?.setOnClickListener {
            drawingView?.removeLastPath()
        }

        galleryButton = findViewById(R.id.gallery_selection)

        galleryButton?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                    showRationaleDialog("Storage permission","Storage permissions are needed to read images.")
                }
                else {
                    requestPermissions.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                }
            }
            else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showRationaleDialog("Storage permission","Storage permissions are needed to read images.")
                }
                else {
                    requestPermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                }
            }

        }

        saveButton = findViewById(R.id.save_selection)

        saveButton?.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // Execute coroutine
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showRationaleDialog("Storage permission","Storage permissions are needed to store images.")
                }
                else {
                    requestPermissions.launch((arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)))
                }
            }
            else {
                saveBitmapScope()

            }
        }
    }

    private fun saveBitmapScope() {
        lifecycleScope.launch {
            val flDrawingView: FrameLayout = findViewById(R.id.drawing_view_frame_layout)
            saveBitmapFile(getBitmapFromView(flDrawingView))
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

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bg = view.background

        if (bg != null) {
            bg.draw(canvas)
        }
        else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private suspend fun saveBitmapFile(bitmapToSave: Bitmap?): String {
        var result = ""

        withContext(Dispatchers.IO) {
            if (bitmapToSave != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    bitmapToSave.compress(Bitmap.CompressFormat.PNG, 90, bytes)


                    val f = File(externalCacheDir?.absoluteFile.toString() + File.separator + "canvas_" + System.currentTimeMillis() / 1000 + ".png")

                    val fo = FileOutputStream(f)

                    fo.write(bytes.toByteArray())
                    fo.close()

                    result = f.absolutePath

                    runOnUiThread {
                        if (result.isNotEmpty()) {
                            Toast.makeText(this@MainActivity, "File stored in $result", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(this@MainActivity, "Something went wrong with saving the file", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }

        return result
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
