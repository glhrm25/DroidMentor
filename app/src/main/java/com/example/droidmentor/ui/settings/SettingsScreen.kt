package com.example.droidmentor.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import com.example.droidmentor.R
import com.example.droidmentor.ui.chat.components.TopBarBanner

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onDebouce: Duration = 10.milliseconds

) {
    val apiState by viewModel.apiKey.collectAsState()

    // O texto fica vazio ou preenchido com a chave final. 
    // O Compose sincroniza automaticamente a variável "text" quando apiState é lido do disco!
    var text by rememberSaveable(apiState) {
        mutableStateOf((apiState as? StateApi.ValueApiKey)?.apiKey ?: "")
    }
    val cond = apiState !is StateApi.LoadingApiKey


    Scaffold(
        topBar = { TopBarBanner(stringResource(R.string.menu_settings) ,onNavigateBack ) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Google Gemini API Key",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("API Key") },
                enabled =   cond,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.updateApiKey(text)
                    viewModel.saveApiKey()
                    onNavigateBack()
                },
                enabled = cond,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Key")
            }
        }
    }
}
