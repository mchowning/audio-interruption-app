package com.mattchowning.testapplication

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mattchowning.testapplication.ui.theme.TestApplicationTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.NonCancellable.start
import java.nio.channels.Selector
import java.util.Locale
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        setContent {
            TestApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {

                        var selectedSound by rememberSaveable { mutableStateOf(sounds.first()) }
//                        Selector(
//                            name = "Sound",
//                            options = sounds,
//                            selected = selectedSound,
//                            onChange = { selectedSound = it }
//                        )

                        var selectedContentType by rememberSaveable { mutableStateOf(contentTypes.first()) }
                        Selector(
                            name = "Content Type",
                            options = contentTypes,
                            selected = selectedContentType,
                            onChange = { selectedContentType = it },
                        )

                        var selectedUsageType by rememberSaveable { mutableStateOf(usageTypes.first()) }
                        Selector(
                            name = "Usage Type",
                            options = usageTypes,
                            selected = selectedUsageType,
                            onChange = { selectedUsageType = it },
                        )

                        val audioFocusGainTransientMayDuck = focusTypes[2]
                        var selectedFocusType by rememberSaveable { mutableStateOf(audioFocusGainTransientMayDuck) }
                        Selector(
                            name = "Focus Type",
                            options = focusTypes,
                            selected = selectedFocusType,
                            onChange = { selectedFocusType = it },
                        )

                        val currentContext = LocalContext.current
                        Button(
                            onClick = {
                                val audioAttributes = AudioAttributes.Builder()
                                    .setContentType(selectedContentType.second)
                                    .setUsage(selectedUsageType.second)
                                    .build()


                                val audioFocusRequest = AudioFocusRequest.Builder(selectedFocusType.second)
                                    .setAudioAttributes(audioAttributes)
                                    .build()

                                audioManager.requestAudioFocus(audioFocusRequest)
                                MediaPlayer.create(
                                    currentContext,
                                    selectedSound.second,
                                    audioAttributes,
                                    0
                                ).apply {
                                    setOnCompletionListener {
                                        audioManager.abandonAudioFocusRequest(audioFocusRequest)
                                    }
                                    start()

                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Play")
                        }

                        Divider()

                        Button(
                            onClick = {
                                val audioFocusRequest = AudioFocusRequest.Builder(selectedFocusType.second).build()
                                audioManager.requestAudioFocus(audioFocusRequest)
                                audioManager.abandonAudioFocusRequest(audioFocusRequest)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Request and immediately abandon focus",
                                textAlign = TextAlign.Center
                            )
                        }

                        Divider()

                        Text(
                            text = "Number of events:",
                            Modifier.padding(horizontal = 16.dp)
                        )
                        var numToSpam by rememberSaveable { mutableStateOf(10) }
                        val changeAmount = 5
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Spacer(modifier = Modifier.width(24.dp))
                            Button(
                                onClick = { numToSpam -= changeAmount }
                            ) { Text("-") }

                            Text(text = "$numToSpam")

                            Button(
                                onClick = { numToSpam += changeAmount }
                            ) { Text("+") }
                        }

                        var endWith by rememberSaveable { mutableStateOf(FocusState.Loss) }
                        Text(
                            text = "End with:",
                            Modifier.padding(horizontal = 16.dp)
                        )
                        FocusState.values().forEach {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                                    .selectable(
                                        selected = endWith == it,
                                        onClick = { endWith = it }
                                    )
                                    .padding(horizontal = 24.dp),
                            ) {
                                RadioButton(
                                    selected = endWith == it,
                                    onClick = { endWith = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = it.name)
                            }
                        }

                        Button(
                            onClick = {
                                val audioFocusRequest = AudioFocusRequest.Builder(selectedFocusType.second).build()

                                // Spam all but last focus event
                                repeat(numToSpam - 1) {
                                    if (Random.nextBoolean()) {
                                        audioManager.requestAudioFocus(audioFocusRequest)
                                    } else {
                                        audioManager.abandonAudioFocusRequest(audioFocusRequest)
                                    }
                                }

                                // End with the selected ending focus event
                                when (endWith) {
                                    FocusState.Gain -> audioManager.requestAudioFocus(audioFocusRequest)
                                    FocusState.Loss -> audioManager.abandonAudioFocusRequest(audioFocusRequest)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Spam $numToSpam random* focus requests ending with a ${endWith.toString().uppercase()} request",
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = "* 'random' means 50/50 chance of gain or loss. All requests will have the selected content, usage, and focus types.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                            ,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun <T> Selector(
        name: String,
        options: List<Pair<String, T>>,
        selected: Pair<String, T>,
        onChange: (Pair<String, T>) -> Unit = {}
    ) {

        var expanded by remember { mutableStateOf(false) }
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(if (expanded) {
                        R.drawable.baseline_arrow_drop_up_24
                    } else {
                        R.drawable.baseline_arrow_drop_down_24
                    }),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("$name: ${selected.first}")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach {

                    DropdownMenuItem(
                        text = { Text(it.first) },
                        onClick = {
                            onChange(it)
                            expanded = false
                        },
                    )
                }
            }
        }

    }
}

private enum class FocusState { Loss, Gain }

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestApplicationTheme {
        Greeting("Android")
    }
}

val contentTypes = listOf(
    Pair("Unknown", AudioAttributes.CONTENT_TYPE_UNKNOWN),
    Pair("Speech", AudioAttributes.CONTENT_TYPE_SPEECH),
    Pair("Music", AudioAttributes.CONTENT_TYPE_MUSIC),
    Pair("Movie", AudioAttributes.CONTENT_TYPE_MOVIE),
    Pair("Sonification", AudioAttributes.CONTENT_TYPE_SONIFICATION),
)

val usageTypes = listOf(
    Pair("Media", AudioAttributes.USAGE_MEDIA),
    Pair("Voice Communication", AudioAttributes.USAGE_VOICE_COMMUNICATION),
    Pair("Voice Communication Signalling", AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING),
    Pair("Alarm", AudioAttributes.USAGE_ALARM),
    Pair("Notification", AudioAttributes.USAGE_NOTIFICATION),
    Pair("Notification Ringtone", AudioAttributes.USAGE_NOTIFICATION_RINGTONE),
    Pair("Notification Event", AudioAttributes.USAGE_NOTIFICATION_EVENT),
    Pair("Assistance Accessiblity", AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY),
    Pair("Assistance Navigation Guidance", AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE),
    Pair("Assistance Sonification", AudioAttributes.USAGE_ASSISTANCE_SONIFICATION),
    Pair("Game", AudioAttributes.USAGE_GAME),
    // Pair("Virtual Source", AudioAttributes.USAGE_VIRTUAL_SOURCE),
    Pair("Assistant", AudioAttributes.USAGE_ASSISTANT),
    // Pair("Call Assistant", AudioAttributes.USAGE_CALL_ASSISTANT),
)

val focusTypes = listOf(
    Pair("Gain", AudioManager.AUDIOFOCUS_GAIN),
    Pair("Gain Transient", AudioManager.AUDIOFOCUS_GAIN_TRANSIENT),
    Pair("Gain Transient May Duck", AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK),
    Pair("Gain Transient Exclusive", AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE),
)

val sounds = listOf<Pair<String, Int>>(
    Pair("Chord", R.raw.piano_chord),
    Pair("Octave Melody", R.raw.piano_octave_melody),
    Pair("Scary Piano", R.raw.scary_piano),
)
