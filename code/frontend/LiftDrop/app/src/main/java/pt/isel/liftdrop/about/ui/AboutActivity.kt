package pt.isel.liftdrop.about.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pt.isel.liftdrop.R
import pt.isel.liftdrop.about.model.Author
import androidx.core.net.toUri


class AboutActivity : ComponentActivity() {

    companion object {
        fun navigate(origin: Activity) {
            with(origin) {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AboutScreen(
                onBackRequest = { finish() },
                onSendEmailRequested =  ::onOpenSendEmail,
                onUrlRequested = ::openUrl,
                listOfAuthor = listOfAuthor
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
    }


    private fun onOpenSendEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "About the LiftDrop App")
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(ContentValues.TAG, "Failed to send email", e)
            Toast
                .makeText(
                    this,
                    R.string.activity_info_no_suitable_app,
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }

    private fun openUrl(url: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("LiftDrop", "Failed to open URL", e)
            Toast
                .makeText(
                    this,
                    R.string.activity_info_no_suitable_app,
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }
}

private val listOfAuthor: List<Author> = listOf(
    Author(
        "João Ramos",
        "A49424@alunos.isel.pt",
        "https://github.com/JohnnyR2003"
    ),
    Author(
        "Gonçalo Morais",
        "A49502@alunos.isel.pt",
        "https://github.com/Goncalo-Morais"
    )
)