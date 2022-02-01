package by.kos.quizzapp

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView


class LearnMaterialsActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_materials)
        var fullscreen = false


        playerView = findViewById<PlayerView>(R.id.simpleExoPlayerView) as PlayerView
        val fullscreenButton = playerView.findViewById<ImageView>(R.id.exo_fullscreen_icon)

        fullscreenButton.setOnClickListener {
            if (fullscreen) {
                fullscreenButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@LearnMaterialsActivity,
                        R.drawable.ic_fullscreen_open
                    )
                )
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                if (supportActionBar != null) {
                    supportActionBar!!.show()
                }
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                val params = playerView.layoutParams as RelativeLayout.LayoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = (200 * applicationContext.resources.displayMetrics.density).toInt()
                playerView.layoutParams = params
                fullscreen = false
            } else {
                fullscreenButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@LearnMaterialsActivity,
                        R.drawable.ic_fullscreen_close
                    )
                )
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                if (supportActionBar != null) {
                    supportActionBar!!.hide()
                }
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                val params = playerView.layoutParams as RelativeLayout.LayoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                playerView.layoutParams = params
                fullscreen = true
            }
        }
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        player.setMediaItem(
            MediaItem.fromUri(
                Uri.parse(
                    "android.resource://"
                            + packageName + "/" + R.raw.jack
                )
            )
        )
        player.prepare();

    }

    override fun onPause() {
        super.onPause()
        player.stop();
    }

    override fun onStop() {
        super.onStop()
        player.stop();
    }
}