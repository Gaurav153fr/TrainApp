package com.example.apktest.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.intellij.lang.annotations.JdkConstants

class MainWidget: GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                TrainWidgetContent()
            }
        }
    }
}

@Composable
fun TrainWidgetContent() {
    val stations: List<StationData> = listOf(
        StationData("Durg", "19:00", false, 1),
        StationData("Bhilai Nagar", "19:00", true, 1),
        StationData("Bhilai PH", "19:00", false, 1),
        StationData("Bhilai", "19:00", false, 1)

    )
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp)
    ) {

        Row(modifier = GlanceModifier.fillMaxWidth()) {

            for(station in stations){
                StationCard(station =station,modifier = GlanceModifier.defaultWeight())
            }
        }
    }
}



@Composable
fun StationCard(modifier:GlanceModifier,station: StationData){
    val kingBlue = ColorProvider(
        day = Color(0xFF4169E1),
        night = Color(0xFF5C82FF) // Optional lighter variant for dark mode
    )
    var color = if(station.isTrain) kingBlue else GlanceTheme.colors.outline
    Column( modifier = modifier,  horizontalAlignment = Alignment.CenterHorizontally) {

        Text(station.name, style = TextStyle(color = GlanceTheme.colors.onBackground))
        Box(
            modifier = GlanceModifier.fillMaxWidth().height(16.dp)
                .background(GlanceTheme.colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier.width(16.dp).height(16.dp)
                    .background(color)
            ) { }

        }

        Text(station.time, style = TextStyle(color = GlanceTheme.colors.primary))
    }
}



data class StationData(
    val name:String,
    val time:String,
    val isTrain:Boolean,
    val platformNo: Int

)