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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {

                        var selectedSound by rememberSaveable { mutableStateOf(sounds.first()) }
                        Selector(
                            name = "Sound",
                            options = sounds,
                            selected = selectedSound,
                            onChange = { selectedSound = it }
                        )
                        Divider()

                        var selectedContentType by rememberSaveable { mutableStateOf(contentTypes.first()) }
                        Selector(
                            name = "Content Type",
                            options = contentTypes,
                            selected = selectedContentType,
                            onChange = { selectedContentType = it },
                        )
                        Divider()

                        var selectedUsageType by rememberSaveable { mutableStateOf(usageTypes.first()) }
                        Selector(
                            name = "Usage Type",
                            options = usageTypes,
                            selected = selectedUsageType,
                            onChange = { selectedUsageType = it },
                        )
                        Divider()

                        var selectedFocusType by rememberSaveable { mutableStateOf(focusTypes.first()) }
                        Selector(
                            name = "Focus Type",
                            options = focusTypes,
                            selected = selectedFocusType,
                            onChange = { selectedFocusType = it },
                        )
                        Divider()

                        val currentContext = LocalContext.current
                        Button(
                            onClick = {
                                val audioAttributes = AudioAttributes.Builder()
                                    .setContentType(selectedContentType.second)
                                    .setUsage(selectedUsageType.second)
                                    .build()


//                                AudioManager.requestAudioFocus(
//                                    null,
//                                    AudioManager.STREAM_MUSIC,
//                                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
//                                )

                                val audioFocusRequest = AudioFocusRequest.Builder(selectedFocusType.second)
                                    .setAudioAttributes(audioAttributes)
//                                        .setForceDucking(true)
//                                        .setAcceptsDelayedFocusGain(true)
//                                        .setWillPauseWhenDucked(true)
                                    .build()
                                audioManager.requestAudioFocus(audioFocusRequest)


                                MediaPlayer.create(
                                    currentContext,
                                    selectedSound.second,
                                    audioAttributes,
                                    0
                                )?.apply {
                                    start()
                                    setOnCompletionListener {
                                        audioManager.abandonAudioFocusRequest(audioFocusRequest)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Play")
                        }
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
            Text(
                text = "$name: ${selected.first}",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp)
            )
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


//val suppressableUsageTypes = listOf(
//    Pair("Notification", AudioAttributes.SUPPRESSIBLE_NOTIFICATION),
//    Pair("Call", AudioAttributes.SUPPRESSIBLE_CALL),
//    Pair("Never", AudioAttributes.SUPPRESSIBLE_NEVER),
//    Pair("Alarm", AudioAttributes.SUPPRESSIBLE_ALARM),
//    Pair("Media", AudioAttributes.SUPPRESSIBLE_MEDIA),
//    Pair("System", AudioAttributes.SUPPRESSIBLE_SYSTEM),
//)

// From android source
//static {
//    SUPPRESSIBLE_USAGES = new SparseIntArray();
//    SUPPRESSIBLE_USAGES.put(USAGE_NOTIFICATION,                      SUPPRESSIBLE_NOTIFICATION);
//    SUPPRESSIBLE_USAGES.put(USAGE_NOTIFICATION_RINGTONE,             SUPPRESSIBLE_CALL);
//    SUPPRESSIBLE_USAGES.put(USAGE_NOTIFICATION_COMMUNICATION_REQUEST,SUPPRESSIBLE_CALL);
//    SUPPRESSIBLE_USAGES.put(USAGE_NOTIFICATION_COMMUNICATION_INSTANT,SUPPRESSIBLE_NOTIFICATION);
//    SUPPRESSIBLE_USAGES.put(USAGE_NOTIFICATION_COMMUNICATION_DELAYED,SUPPRESSIBLE_NOTIFICATION);
//    SUPPRESSIBLE_USAGES.put(USAGE_NOTIFICATION_EVENT,                SUPPRESSIBLE_NOTIFICATION);
//    SUPPRESSIBLE_USAGES.put(USAGE_ASSISTANCE_ACCESSIBILITY,          SUPPRESSIBLE_NEVER);
//    SUPPRESSIBLE_USAGES.put(USAGE_VOICE_COMMUNICATION,               SUPPRESSIBLE_NEVER);
//    SUPPRESSIBLE_USAGES.put(USAGE_VOICE_COMMUNICATION_SIGNALLING,    SUPPRESSIBLE_NEVER);
//    SUPPRESSIBLE_USAGES.put(USAGE_ALARM,                             SUPPRESSIBLE_ALARM);
//    SUPPRESSIBLE_USAGES.put(USAGE_MEDIA,                             SUPPRESSIBLE_MEDIA);
//    SUPPRESSIBLE_USAGES.put(USAGE_ASSISTANCE_NAVIGATION_GUIDANCE,    SUPPRESSIBLE_MEDIA);
//    SUPPRESSIBLE_USAGES.put(USAGE_GAME,                              SUPPRESSIBLE_MEDIA);
//    SUPPRESSIBLE_USAGES.put(USAGE_ASSISTANT,                         SUPPRESSIBLE_MEDIA);
//    SUPPRESSIBLE_USAGES.put(USAGE_CALL_ASSISTANT,                    SUPPRESSIBLE_NEVER);
//    /** default volume assignment is STREAM_MUSIC, handle unknown usage as media */
//    SUPPRESSIBLE_USAGES.put(USAGE_UNKNOWN,                           SUPPRESSIBLE_MEDIA);
//    SUPPRESSIBLE_USAGES.put(USAGE_ASSISTANCE_SONIFICATION,           SUPPRESSIBLE_SYSTEM);
//}

val focusTypes = listOf(
    Pair("Gain", AudioManager.AUDIOFOCUS_GAIN),
    Pair("Gain Transient", AudioManager.AUDIOFOCUS_GAIN_TRANSIENT),
    Pair("Gain Transient Exclusive", AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE),
    Pair("Gain Transient May Duck", AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK),
)

val sounds = listOf<Pair<String, Int>>(
    Pair("Chord", R.raw.piano_chord),
    Pair("Octave Melody", R.raw.piano_octave_melody),
    Pair("Scary Piano", R.raw.scary_piano),
)