package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.retainedComponent
import org.example.project.root.DefaultRootComponent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val root = retainedComponent { componentContext ->
            DefaultRootComponent(componentContext = componentContext)
        }

        setContent {

            App(rootComponent = root)
        }
    }
}