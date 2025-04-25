package pt.isel.liftdrop.ui

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.PanoramaPhotosphere
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import pt.isel.liftdrop.R

const val NavigateBackTag = "NavigateBack"
const val LogoutTag = "LogoutBack"
const val NavigateInfoTag = "NavigateInfoTag"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onBackRequested: (() -> Unit)? = null,
    onInfoRequested: (() -> Unit)? = null,
    onLogoutRequested: (() -> Unit)? = null,
) {
    TopAppBar(
        title = { stringResource(id = R.string.app_name) },
        navigationIcon = {
            if (onBackRequested != null) {
                IconButton(onClick = onBackRequested, modifier = Modifier.testTag(NavigateBackTag)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
            if(onLogoutRequested != null){
                IconButton(onClick = {onLogoutRequested()}, modifier = Modifier.testTag(LogoutTag)){
                    Icon(Icons.Default.Logout,contentDescription = null)
                }
            }
        },
        actions = {
            if (onInfoRequested != null) {
                IconButton(onClick = { onInfoRequested() },modifier = Modifier.testTag(NavigateInfoTag)) {
                    Icon(Icons.Default.Info, contentDescription = null)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(Color.White)
    )
}