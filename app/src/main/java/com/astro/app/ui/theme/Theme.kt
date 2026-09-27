package com.astro.app.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.astro.app.ui.AstroViewModel
@Composable fun AstroTheme(vm:AstroViewModel,content:@Composable()->Unit){val mode by vm.theme.collectAsState();val accent=Color(vm.accent.collectAsState().value);val dark=when(mode){"dark"->true;"light"->false;else->isSystemInDarkTheme()};val scheme=if(dark)darkColorScheme(primary=accent,secondary=accent.copy(alpha=.72f))else lightColorScheme(primary=accent,secondary=accent.copy(alpha=.72f));MaterialTheme(colorScheme=scheme){content()}}
