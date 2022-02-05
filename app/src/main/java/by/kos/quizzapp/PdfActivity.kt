package by.kos.quizzapp

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import by.kos.quizzapp.databinding.ActivityPdfBinding
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.log

class PdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfBinding
    private lateinit var pdfView: PDFView
    private lateinit var fabExit: FloatingActionButton
    private lateinit var fabZoomIn: FloatingActionButton
    private lateinit var fabZoomOut: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pdfView = binding.pdfView
        fabExit = binding.fabExit
        fabZoomIn = binding.fabZoomIn
        fabZoomOut = binding.fabZoomOut
        pdfView.fromAsset("pocket.pdf").load()
        var zoom = pdfView.zoom
        Log.i("zoom", pdfView.zoom.toString())
        fabZoomIn.setOnClickListener {
            if (zoom >= 3.0f) {
                zoom = 3.0f
            } else {
                zoom += 0.2f
            }

            pdfView.zoomWithAnimation(zoom)
        }

        fabZoomOut.setOnClickListener {
            if (zoom <= 0.2f) {
                zoom = 0.2f
            } else {
                zoom -= 0.2f
            }

            pdfView.zoomWithAnimation(zoom)
        }

        fabExit.setOnClickListener {
            onBackPressed()
        }

    }
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.txt_confirm))
        builder.setMessage(getString(R.string.exit_main_question))

        builder.setPositiveButton(getString(R.string.exit_yes)) { dialog, which ->
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        builder.setNegativeButton(getString(R.string.exit_no)) { dialog, which ->
        }
        builder.show()
    }
}