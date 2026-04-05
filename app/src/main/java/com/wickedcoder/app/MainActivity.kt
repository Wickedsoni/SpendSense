package com.wickedcoder.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.wickedcoder.app.core.datastore.OnboardingPreferences
import com.wickedcoder.app.ui.navigation.SpendSenseNavGraph
import com.wickedcoder.app.ui.onboarding.OnboardingScreen
import com.wickedcoder.app.ui.theme.SpendSenseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var onboardingPrefs: OnboardingPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendSenseTheme {
                val isComplete by onboardingPrefs.isComplete
                    .collectAsStateWithLifecycle(initialValue = null)

                when (isComplete) {
                    null  -> Box(Modifier.fillMaxSize()) // blank ~1 frame while DataStore cold-starts
                    false -> OnboardingScreen()
                    true  -> SpendSenseNavGraph()
                }
            }
        }
    }
}
