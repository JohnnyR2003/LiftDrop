package pt.isel.liftdrop.about.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.liftdrop.R
import pt.isel.liftdrop.ui.TopBar
import pt.isel.liftdrop.ui.theme.LiftDropTheme
import pt.isel.liftdrop.about.model.Author
import androidx.core.net.toUri


@Composable
fun AboutScreen(
    onBackRequest: () -> Unit,
    onSendEmailRequested: (String) -> Unit = { },
    onUrlRequested: (Uri) -> Unit = { },
    listOfAuthor: List<Author>
) {
    LiftDropTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("AboutView"),
            topBar = { TopBar(onBackRequested = { onBackRequest() }) }
        ) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                Row(horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(id = R.string.about_liftdrop),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                repeat(listOfAuthor.size) {
                    AuthorView(
                        onOpenSendEmailRequested = onSendEmailRequested,
                        onOpenUrlRequested = onUrlRequested,
                        listOfAuthor[it]
                    )
                }
            }
        }
    }
}

@Composable
fun AuthorView(
    onOpenSendEmailRequested: (String) -> Unit = { },
    onOpenUrlRequested: (Uri) -> Unit = { },
    author: Author
) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
        Text(
            text = author.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
        Button(colors = ButtonDefaults.buttonColors(),onClick = {
            onOpenSendEmailRequested(author.email)
        }) {
            Image(
                painter = painterResource(id = R.drawable.email_logo),
                contentDescription = null,
                modifier = Modifier.sizeIn(25.dp, 25.dp, 50.dp, 50.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(colors = ButtonDefaults.buttonColors(),onClick = {
            onOpenUrlRequested(author.github.toUri())
        }) {
            Image(
                painter = painterResource(id = R.drawable.github_logo),
                contentDescription = null,
                modifier = Modifier.sizeIn(25.dp, 25.dp, 50.dp, 50.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutPreview() {
    val listOfAuthor: List<Author> = listOf(
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
    AboutScreen(
        onBackRequest = {},
        onSendEmailRequested = {},
        onUrlRequested = {},
        listOfAuthor
    )
}