package com.example.taller_2.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.taller_2.R
import com.example.taller_2.ui.theme.AppTheme

/**
 * Botones para iniciar/detener el recorrido y para borrarlo.
 *
 * @param isRecording indica si el recorrido se está grabando.
 * @param routeSize cantidad de puntos del recorrido, se muestra como insignia en el botón de borrar.
 */
@Composable
fun RouteControls(
    isRecording: Boolean,
    routeSize: Int,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            FilledIconButton(
                onClick = { if (isRecording) onStopClick() else onStartClick() },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRecording) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary,
                    contentColor = if (isRecording) MaterialTheme.colorScheme.onTertiary
                    else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(
                        if (isRecording) R.string.stop_route else R.string.start_route
                    )
                )
            }
            Spacer(Modifier.width(16.dp))
            BadgedBox(
                badge = {
                    if (routeSize > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ) {
                            Text("$routeSize")
                        }
                    }
                }
            ) {
                OutlinedIconButton(
                    onClick = onClearClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = stringResource(R.string.clear_route),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
fun RouteControlsPreview() {
    AppTheme {
        RouteControls(
            isRecording = true,
            routeSize = 6,
            onStartClick = {},
            onStopClick = {},
            onClearClick = {}
        )
    }
}
