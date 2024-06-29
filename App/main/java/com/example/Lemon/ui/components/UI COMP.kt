package com.example.arcus.ui.components

import android.graphics.Paint
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arcus.R
import com.example.arcus.domain.HourlyForecast
import com.example.arcus.domain.PrecipitationProbability
import com.example.arcus.domain.hourStringInTwelveHourFormat
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AutofillSuggestion(
    title: String,
    subText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = { DefaultLeadingIcon() }
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon()
        Spacer(modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = subText,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}


@Composable
private fun DefaultLeadingIcon() {
    Icon(
        modifier = Modifier
            .size(40.dp)
            .drawBehind {
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    center = center,
                    radius = size.minDimension / 1.7f
                )
            },
        imageVector = Icons.Filled.LocationOn,
        tint = Color.White,
        contentDescription = null
    )
}

data class HourlyForecast(
    val dateTime: LocalDateTime,
    @DrawableRes val weatherIconResId: Int,
    val temperature: Int
)

@ExperimentalMaterial3Api
@Composable
fun WeatherCard(
    nameOfLocation: String,
    shortDescription: String? = null,
    @DrawableRes shortDescriptionIcon: Int? = null,
    weatherInDegrees: String,
    hourlyForecasts: List<HourlyForecast>? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedWeather = remember(weatherInDegrees) { "$weatherInDegrees°" }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(16.dp) // Add padding for better spacing
            .fillMaxWidth(), // Ensure it fills the width of its parent
        horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
    ) {
        LocationName(nameOfLocation)
        Spacer(Modifier.height(8.dp)) // Add space between elements
        WeatherDegree(formattedWeather)

        shortDescription?.let { description ->
            Spacer(Modifier.height(8.dp)) // Add space before the description
            if (shortDescriptionIcon != null) {
                ShortWeatherDescriptionWithIconRow(
                    shortDescription = description,
                    iconRes = shortDescriptionIcon
                )
            } else {

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        hourlyForecasts?.let { forecasts ->
            Spacer(Modifier.height(8.dp)) // Add space before the forecast row
            HourlyForecastScrollableRow(forecasts)
        }
    }
}


@Composable
fun LocationName(nameOfLocation: String) {
    Text(
        text = nameOfLocation,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun WeatherDegree(formattedWeather: String) {
    Text(
        text = formattedWeather,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun HourlyForecastScrollableRow(hourlyForecasts: List<HourlyForecast>) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        hourlyForecasts.forEach { forecast ->
            HourlyForecastCard(forecast)
        }
    }
}

@Composable
fun HourlyForecastCard(forecast: HourlyForecast) {
    ForecastCard(
        dateTime = forecast.dateTime,
        iconResId = forecast.weatherIconResId,
        temperature = forecast.temperature,
        modifier = Modifier.size(100.dp).padding(4.dp)
    )
}

@Composable
private fun ForecastCard(
    dateTime: LocalDateTime,
    @DrawableRes iconResId: Int,
    temperature: Int,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier, elevation = CardDefaults.elevatedCardElevation()) {
        ForecastCardContent(dateTime, iconResId, temperature)
    }
}

@Composable
private fun ForecastCardContent(dateTime: LocalDateTime, iconResId: Int, temperature: Int) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HourText(dateTime)
        WeatherIcon(iconResId)
        TemperatureText(temperature)
    }
}

@Composable
fun HourText(dateTime: LocalDateTime) {
    Text(
        text = dateTime.hourStringInTwelveHourFormat(),
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun WeatherIcon(@DrawableRes iconResId: Int) {
    Icon(
        painter = painterResource(id = iconResId),
        contentDescription = "Weather Icon",
        modifier = Modifier.size(40.dp),
        tint = Color.Unspecified
    )
}

@Composable
fun ShortWeatherDescriptionWithIconRow(
    shortDescription: String,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WeatherIcon(iconResId = iconRes) // Reuse your WeatherIcon composable
        Spacer(modifier = Modifier.width(4.dp)) // Add a small space between icon and text
        Text(
            text = shortDescription,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Normal
        )
    }
}




@Composable
fun TemperatureText(temperature: Int) {
    Text(
        text = "$temperature°",
        style = MaterialTheme.typography.labelLarge
    )
}

fun LocalDateTime.hourStringInTwelveHourFormat(): String {
    val adjustedHour = this.hour % 12
    return when {
        adjustedHour == 0 -> "12 AM"
        this.hour < 12 -> "$adjustedHour AM"
        else -> "$adjustedHour PM"
    }
}


@Composable
fun HourlyForecastCard(
    hourlyForecasts: List<HourlyForecast>,
    precipitationProbabilities: List<PrecipitationProbability>,
    modifier: Modifier = Modifier
) {


    var isGraphicalView by remember { mutableStateOf(false) }
    var selectedForecast by remember { mutableStateOf<HourlyForecast?>(null) }

    Box(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = rememberNestClockFarsightAnalog(),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Text(
                    text = "Hourly Forecast",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (isGraphicalView) {
                EnhancedLineChart(
                    dataPoints = hourlyForecasts.map { it.temperature.toFloat() },
                    timePoints = hourlyForecasts.map { it.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")) }, // Format the time in 24-hour format
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                )

            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 10.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(hourlyForecasts.zip(precipitationProbabilities)) { (forecast, probability) ->
                        HourlyForecastItem(
                            dateTime = forecast.dateTime,
                            iconResId = forecast.weatherIconResId,
                            temperature = forecast.temperature,
                            precipitationProbability = probability.probabilityPercentage,
                            onItemClick = { selectedForecast = forecast }
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = { isGraphicalView = !isGraphicalView },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
        ) {
            Icon(
                imageVector = if (isGraphicalView) {
                    rememberFeaturedPlayList()
                } else {
                    rememberAutoGraph()
                },
                contentDescription = if (isGraphicalView) {
                    "Switch to list view"
                } else {
                    "Switch to graphical view"
                }
            )
        }
    }

    selectedForecast?.let { forecast ->
        val probability = precipitationProbabilities.find { it.dateTime == forecast.dateTime }
        val weatherDescriptionMap = mapOf(
            R.drawable.ic_day_clear to "Clear Sky",
            R.drawable.ic_day_few_clouds to "Few Clouds",
            R.drawable.ic_day_rain to "Rainy",
            R.drawable.ic_day_thunderstorms to "Thunderstorms",
            R.drawable.ic_day_snow to "Snow",
            R.drawable.ic_mist to "Fog"
        )
        HourlyForecastDetailsPopup(
            dateTime = forecast.dateTime,
            iconResId = forecast.weatherIconResId,
            temperature = forecast.temperature,
            precipitationProbability = probability?.probabilityPercentage ?: 0,
            weatherDescriptionMap = weatherDescriptionMap,
            onDismiss = { selectedForecast = null }
        )

    }
}


@Composable
fun HourlyForecastDetailsPopup(
    dateTime: LocalDateTime,
    @DrawableRes iconResId: Int,
    temperature: Int,
    precipitationProbability: Int,
    weatherDescriptionMap: Map<Int, String>,
    onDismiss: () -> Unit
) {
    val weatherDescription = weatherDescriptionMap[iconResId] ?: "Clear Sky"
    val animationSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = animationSpec
    )
    val animatedProgress by animateFloatAsState(
        targetValue = precipitationProbability / 100f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 16.dp)
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .alpha(animatedAlpha)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    modifier = Modifier.size(96.dp),
                    imageVector = ImageVector.vectorResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Weather: $weatherDescription", // Display weather description
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Temperature: ${temperature}°",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Precipitation Probability",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )

            }
        }
    }
}

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun LineeChart(
    dataPoints: List<Float>,
    timePoints: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color,
    textColor: Color
) {
    val maxValue = dataPoints.maxOrNull() ?: 1f
    val minValue = dataPoints.minOrNull() ?: 0f

    Canvas(modifier = modifier
        .height(200.dp)
        .fillMaxWidth()) {
        val space = 10f
        val width = size.width - space * 2
        val height = size.height - space * 2
        val stepX = width / (dataPoints.size - 1)

        for (i in dataPoints.indices) {
            val x = space + i * stepX
            val y = space + (1 - (dataPoints[i] - minValue) / (maxValue - minValue)) * height
            if (i > 0) {
                val prevX = space + (i - 1) * stepX
                val prevY = space + (1 - (dataPoints[i - 1] - minValue) / (maxValue - minValue)) * height
                drawLine(
                    color = lineColor,
                    start = Offset(prevX, prevY),
                    end = Offset(x, y),
                    strokeWidth = 4.dp.toPx()
                )
            }
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    timePoints[i],
                    x,
                    size.height - 10.dp.toPx(), // Adjusted for better visibility
                    Paint().apply {
                        color = textColor.toArgb()
                        textSize = 14.sp.toPx() // Increased text size
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
@Composable
fun LineChart2(
    dataPoints: List<Float>,
    timePoints: List<String>, // List of time points in 24-hour format
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val maxValue = dataPoints.maxOrNull() ?: 1f
    val minValue = dataPoints.minOrNull() ?: 0f
    val points = remember(dataPoints) {
        dataPoints.map { it -> (it - minValue) / (maxValue - minValue) }
    }

    BoxWithConstraints(modifier = modifier) {
        val chartHeight = constraints.maxHeight
        val chartWidth = constraints.maxWidth

        val spacing = 32.dp.dpToPx()// Increase spacing between points
        val distance = (chartWidth - 2 * spacing) / (points.size - 1)

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between points
        ) {
            itemsIndexed(points) { index, point ->
                Canvas(
                    modifier = Modifier
                        .height(chartHeight.dp)
                        .width(distance.dp)
                ) {
                    val x1 = 0f
                    val y1 = size.height - (point * size.height)
                    val x2 = size.width
                    val y2 = if (index == points.lastIndex) {
                        y1
                    } else {
                        size.height - (points[index + 1] * size.height)
                    }

                    drawLine(
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        color = lineColor,
                        strokeWidth = 4.dp.toPx()
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            dataPoints[index].toString(),
                            x1,
                            y1 - 15.dp.toPx(),
                            Paint().apply {
                                color = textColor.toArgb()
                                textSize = 12.sp.toPx()
                                textAlign = Paint.Align.CENTER
                            }
                        )

                        // Draw time label in 24-hour format
                        drawText(
                            timePoints[index],
                            x1,
                            size.height + 20.dp.toPx(),
                            Paint().apply {
                                color = textColor.toArgb()
                                textSize = 12.sp.toPx()
                                textAlign = Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedLineChart1(
    dataPoints: List<Float>,
    timePoints: List<String>, // List of time points in 24-hour format
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val maxValue = dataPoints.maxOrNull() ?: 1f
    val minValue = dataPoints.minOrNull() ?: 0f
    val points = remember(dataPoints) {
        dataPoints.map { it -> (it - minValue) / (maxValue - minValue) }
    }

    BoxWithConstraints(modifier = modifier) {
        val chartHeight = constraints.maxHeight.toFloat()
        val chartWidth = constraints.maxWidth.toFloat()

        val spacing = 32.dp.dpToPx() // Increase spacing between points
        val distance = (chartWidth - 2 * spacing) / (points.size - 1)

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between points
        ) {
            itemsIndexed(points) { index, point ->
                Canvas(
                    modifier = Modifier
                        .height(chartHeight.dp)
                        .width(distance.dp)
                ) {
                    val x1 = 0f
                    val y1 = chartHeight - (point * chartHeight)
                    val x2 = distance
                    val y2 = if (index == points.lastIndex) {
                        y1
                    } else {
                        chartHeight - (points[index + 1] * chartHeight)
                    }

                    drawLine(
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        color = lineColor,
                        strokeWidth = 4.dp.toPx()
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            dataPoints[index].toString(),
                            x1,
                            y1 - 15.dp.toPx(),
                            Paint().apply {
                                color = textColor.toArgb()
                                textSize = 12.sp.toPx()
                                textAlign = Paint.Align.CENTER
                            }
                        )

                        // Draw time label in 24-hour format
                        drawText(
                            timePoints[index],
                            x1,
                            chartHeight + 20.dp.toPx(),
                            Paint().apply {
                                color = textColor.toArgb()
                                textSize = 12.sp.toPx()
                                textAlign = Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart1(
    dataPoints: List<Float>,
    timePoints: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color,
    textColor: Color
) {
    val maxValue = dataPoints.maxOrNull() ?: 1f
    val minValue = dataPoints.minOrNull() ?: 0f
    val points = remember(dataPoints) {
        dataPoints.map { it -> (it - minValue) / (maxValue - minValue) }
    }

    BoxWithConstraints(modifier = modifier) {
        val chartHeight = constraints.maxHeight
        val chartWidth = constraints.maxWidth

        val spacing = 32.dp.dpToPx() // Adjust spacing as needed
        val distance = (chartWidth - 2 * spacing) / (points.size - 1)

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between points
        ) {
            itemsIndexed(points) { index, point ->
                Canvas(
                    modifier = Modifier
                        .height(chartHeight.dp)
                        .width(distance.dp)
                ) {
                    val x1 = 0f
                    val y1 = size.height - (point * size.height)
                    val x2 = size.width
                    val y2 = if (index == points.lastIndex) {
                        y1
                    } else {
                        size.height - (points[index + 1] * size.height)
                    }

                    drawLine(
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        color = lineColor,
                        strokeWidth = 4.dp.toPx()
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            timePoints[index],
                            size.width / 2,
                            size.height + 20.dp.toPx(),
                            Paint().apply {
                                color = textColor.toArgb()
                                textSize = 12.sp.toPx()
                                textAlign = Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart(
    dataPoints: List<Float>,
    timePoints: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color,
    textColor: Color
) {
    val maxValue = dataPoints.maxOrNull() ?: 1f
    val minValue = dataPoints.minOrNull() ?: 0f

    Canvas(modifier = modifier
        .height(200.dp)
        .fillMaxWidth()) {
        val space = 10f
        val width = size.width - space * 2
        val height = size.height - space * 2
        val stepX = width / (dataPoints.size - 1)

        for (i in dataPoints.indices) {
            val x = space + i * stepX
            val y = space + (1 - (dataPoints[i] - minValue) / (maxValue - minValue)) * height
            if (i > 0) {
                val prevX = space + (i - 1) * stepX
                val prevY = space + (1 - (dataPoints[i - 1] - minValue) / (maxValue - minValue)) * height
                drawLine(
                    color = lineColor,
                    start = Offset(prevX, prevY),
                    end = Offset(x, y),
                    strokeWidth = 4.dp.toPx()
                )
            }
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    timePoints[i],
                    x,
                    size.height - 10.dp.toPx(), // Adjusted for better visibility
                    Paint().apply {
                        color = textColor.toArgb()
                        textSize = 14.sp.toPx() // Increased text size
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
fun EnhancedLineChart(
    dataPoints: List<Float>,
    timePoints: List<String>, // List of time points in 24-hour format
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val maxValue = dataPoints.maxOrNull() ?: 1f
    val minValue = dataPoints.minOrNull() ?: 0f
    val points = remember(dataPoints) {
        dataPoints.map { it -> (it - minValue) / (maxValue - minValue) }
    }

    BoxWithConstraints(modifier = modifier) {
        val chartHeight = constraints.maxHeight
        val chartWidth = constraints.maxWidth

        val spacing = 32.dp.dpToPx()// Increase spacing between points
        val distance = (chartWidth - 2 * spacing) / (points.size - 1)

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between points
        ) {
            itemsIndexed(points) { index, point ->
                Canvas(
                    modifier = Modifier
                        .height(chartHeight.dp)
                        .width(distance.dp)
                ) {
                    val x1 = 0f
                    val y1 = size.height - (point * size.height)
                    val x2 = size.width
                    val y2 = if (index == points.lastIndex) {
                        y1
                    } else {
                        size.height - (points[index + 1] * size.height)
                    }

                    drawLine(
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        color = lineColor,
                        strokeWidth = 4.dp.toPx()
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            dataPoints[index].toString(),
                            x1,
                            y1 - 15.dp.toPx(),
                            Paint().apply {
                                color = textColor.toArgb()
                                textSize = 12.sp.toPx()
                                textAlign = Paint.Align.CENTER
                            }
                        )

                        // Draw time label in 24-hour format
                        drawText(
                            timePoints[index],
                            x1,
                            size.height + 20.dp.toPx(),
                            Paint().apply {
                                color = textColor.toArgb()
                                textSize = 12.sp.toPx()
                                textAlign = Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}




@Composable
private fun HourlyForecastItem(
    modifier: Modifier = Modifier,
    dateTime: LocalDateTime,
    @DrawableRes iconResId: Int,
    temperature: Int,
    precipitationProbability: Int,
    onItemClick: () -> Unit
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onItemClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dateTime.hourStringInTwelveHourFormat,
                style = MaterialTheme.typography.labelLarge
            )
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = ImageVector.vectorResource(id = iconResId),
                contentDescription = null,
                tint = Color.Unspecified
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = rememberThermometer() ,
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Text(
                    text = "${temperature}°",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = rememberRainy(),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Text(
                    text = "$precipitationProbability%",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}


@Composable
fun rememberRainy(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "rainy",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.Blue),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(23.208f, 36.458f)
                quadToRelative(-0.458f, 0.25f, -1f, 0.063f)
                quadToRelative(-0.541f, -0.188f, -0.791f, -0.646f)
                lineToRelative(-2.75f, -5.5f)
                quadToRelative(-0.25f, -0.5f, -0.084f, -1.042f)
                quadToRelative(0.167f, -0.541f, 0.667f, -0.791f)
                quadToRelative(0.5f, -0.209f, 1.021f, -0.042f)
                quadToRelative(0.521f, 0.167f, 0.771f, 0.667f)
                lineToRelative(2.75f, 5.5f)
                quadToRelative(0.25f, 0.5f, 0.083f, 1.021f)
                quadToRelative(-0.167f, 0.52f, -0.667f, 0.77f)
                close()
                moveToRelative(10f, -0.041f)
                quadToRelative(-0.458f, 0.25f, -1f, 0.083f)
                quadToRelative(-0.541f, -0.167f, -0.791f, -0.667f)
                lineToRelative(-2.75f, -5.5f)
                quadToRelative(-0.25f, -0.5f, -0.084f, -1.021f)
                quadToRelative(0.167f, -0.52f, 0.667f, -0.77f)
                reflectiveQuadToRelative(1.021f, -0.063f)
                quadToRelative(0.521f, 0.188f, 0.771f, 0.646f)
                lineToRelative(2.75f, 5.5f)
                quadToRelative(0.25f, 0.5f, 0.083f, 1.042f)
                quadToRelative(-0.167f, 0.541f, -0.667f, 0.75f)
                close()
                moveToRelative(-20f, 0.041f)
                quadToRelative(-0.458f, 0.209f, -1f, 0.042f)
                quadToRelative(-0.541f, -0.167f, -0.791f, -0.625f)
                lineToRelative(-2.75f, -5.5f)
                quadToRelative(-0.25f, -0.5f, -0.063f, -1.042f)
                quadToRelative(0.188f, -0.541f, 0.688f, -0.791f)
                quadToRelative(0.458f, -0.209f, 1f, -0.042f)
                quadToRelative(0.541f, 0.167f, 0.791f, 0.625f)
                lineToRelative(2.75f, 5.542f)
                quadToRelative(0.25f, 0.5f, 0.063f, 1.021f)
                quadToRelative(-0.188f, 0.52f, -0.688f, 0.77f)
                close()
                moveToRelative(-1f, -10.416f)
                quadToRelative(-3.625f, 0f, -6.208f, -2.584f)
                quadToRelative(-2.583f, -2.583f, -2.583f, -6.25f)
                quadToRelative(0f, -3.291f, 2.312f, -5.875f)
                quadTo(8.042f, 8.75f, 11.5f, 8.458f)
                quadToRelative(1.333f, -2.333f, 3.562f, -3.708f)
                quadTo(17.292f, 3.375f, 20f, 3.375f)
                quadToRelative(3.75f, 0f, 6.375f, 2.396f)
                reflectiveQuadToRelative(3.208f, 5.979f)
                quadToRelative(3.125f, 0.167f, 5.084f, 2.25f)
                quadToRelative(1.958f, 2.083f, 1.958f, 4.875f)
                quadToRelative(0f, 2.958f, -2.104f, 5.063f)
                quadToRelative(-2.104f, 2.104f, -5.063f, 2.104f)
                close()
            }
        }.build()
    }
}

@Composable
fun rememberThermometer(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "thermometer",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(20f, 34.667f)
                quadToRelative(-3.167f, 0f, -5.396f, -2.209f)
                quadToRelative(-2.229f, -2.208f, -2.229f, -5.375f)
                quadToRelative(0f, -2f, 0.958f, -3.708f)
                quadToRelative(0.959f, -1.708f, 2.625f, -2.792f)
                verticalLineTo(9.292f)
                quadToRelative(0f, -1.625f, 1.188f, -2.813f)
                quadTo(18.333f, 5.292f, 20f, 5.292f)
                reflectiveQuadToRelative(2.833f, 1.187f)
                quadTo(24f, 7.667f, 24f, 9.292f)
                verticalLineToRelative(11.291f)
                quadToRelative(1.708f, 1.084f, 2.646f, 2.792f)
                quadToRelative(0.937f, 1.708f, 0.937f, 3.708f)
                quadToRelative(0f, 3.167f, -2.208f, 5.375f)
                quadToRelative(-2.208f, 2.209f, -5.375f, 2.209f)
                close()
                moveToRelative(-1.375f, -18.292f)
                horizontalLineToRelative(2.75f)
                verticalLineTo(9.292f)
                quadToRelative(0f, -0.542f, -0.396f, -0.938f)
                quadToRelative(-0.396f, -0.396f, -0.979f, -0.396f)
                reflectiveQuadToRelative(-0.979f, 0.396f)
                quadToRelative(-0.396f, 0.396f, -0.396f, 0.938f)
                close()
            }
        }.build()
    }
}





@Composable
fun rememberAutoGraph(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "auto_graph",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(6.208f, 33.042f)
                quadToRelative(-0.416f, -0.417f, -0.416f, -0.98f)
                quadToRelative(0f, -0.562f, 0.416f, -1.02f)
                lineToRelative(10.417f, -10.375f)
                quadToRelative(0.208f, -0.209f, 0.437f, -0.292f)
                quadToRelative(0.23f, -0.083f, 0.48f, -0.083f)
                quadToRelative(0.25f, 0f, 0.479f, 0.083f)
                quadToRelative(0.229f, 0.083f, 0.437f, 0.292f)
                lineToRelative(5.667f, 5.708f)
                lineToRelative(11.208f, -12.583f)
                quadToRelative(0.334f, -0.375f, 0.896f, -0.417f)
                quadToRelative(0.563f, -0.042f, 0.979f, 0.375f)
                quadToRelative(0.334f, 0.333f, 0.354f, 0.854f)
                quadToRelative(0.021f, 0.521f, -0.312f, 0.896f)
                lineTo(25.042f, 29.292f)
                quadToRelative(-0.209f, 0.208f, -0.438f, 0.312f)
                quadToRelative(-0.229f, 0.104f, -0.479f, 0.104f)
                quadToRelative(-0.292f, 0f, -0.542f, -0.083f)
                quadToRelative(-0.25f, -0.083f, -0.458f, -0.25f)
                lineToRelative(-5.583f, -5.625f)
                lineToRelative(-9.334f, 9.292f)
                quadToRelative(-0.416f, 0.458f, -0.979f, 0.458f)
                quadToRelative(-0.562f, 0f, -1.021f, -0.458f)
                close()
                moveToRelative(-0.083f, -12.084f)
                quadToRelative(-0.167f, 0f, -0.333f, -0.104f)
                quadToRelative(-0.167f, -0.104f, -0.25f, -0.312f)
                lineTo(4.792f, 19f)
                lineToRelative(-1.542f, -0.708f)
                quadToRelative(-0.208f, -0.125f, -0.292f, -0.292f)
                quadToRelative(-0.083f, -0.167f, -0.083f, -0.333f)
                quadToRelative(0f, -0.167f, 0.083f, -0.334f)
                quadToRelative(0.084f, -0.166f, 0.292f, -0.291f)
                lineToRelative(1.542f, -0.709f)
                lineToRelative(0.75f, -1.583f)
                quadToRelative(0.083f, -0.167f, 0.25f, -0.271f)
                quadToRelative(0.166f, -0.104f, 0.333f, -0.104f)
                reflectiveQuadToRelative(0.354f, 0.104f)
                quadToRelative(0.188f, 0.104f, 0.271f, 0.271f)
                lineToRelative(0.708f, 1.583f)
                lineToRelative(1.584f, 0.709f)
                quadToRelative(0.333f, 0.166f, 0.333f, 0.625f)
                quadToRelative(0f, 0.458f, -0.333f, 0.625f)
                lineTo(7.458f, 19f)
                lineToRelative(-0.708f, 1.542f)
                quadToRelative(-0.083f, 0.208f, -0.271f, 0.312f)
                quadToRelative(-0.187f, 0.104f, -0.354f, 0.104f)
                close()
                moveToRelative(19f, -3.333f)
                quadToRelative(-0.208f, 0f, -0.375f, -0.104f)
                reflectiveQuadToRelative(-0.25f, -0.313f)
                lineToRelative(-0.708f, -1.541f)
                lineToRelative(-1.542f, -0.709f)
                quadToRelative(-0.25f, -0.125f, -0.333f, -0.291f)
                quadToRelative(-0.084f, -0.167f, -0.084f, -0.334f)
                quadToRelative(0f, -0.166f, 0.084f, -0.333f)
                quadToRelative(0.083f, -0.167f, 0.333f, -0.292f)
                lineTo(23.792f, 13f)
                lineToRelative(0.708f, -1.583f)
                quadToRelative(0.083f, -0.167f, 0.271f, -0.271f)
                quadToRelative(0.187f, -0.104f, 0.354f, -0.104f)
                quadToRelative(0.167f, 0f, 0.333f, 0.104f)
                quadToRelative(0.167f, 0.104f, 0.25f, 0.271f)
                lineToRelative(0.75f, 1.583f)
                lineToRelative(1.542f, 0.708f)
                quadToRelative(0.208f, 0.125f, 0.292f, 0.292f)
                quadToRelative(0.083f, 0.167f, 0.083f, 0.333f)
                quadToRelative(0f, 0.167f, -0.083f, 0.334f)
                quadToRelative(-0.084f, 0.166f, -0.292f, 0.291f)
                lineToRelative(-1.542f, 0.709f)
                lineToRelative(-0.75f, 1.541f)
                quadToRelative(-0.083f, 0.209f, -0.25f, 0.313f)
                quadToRelative(-0.166f, 0.104f, -0.333f, 0.104f)
                close()
                moveTo(14f, 12.583f)
                quadToRelative(-0.167f, 0f, -0.354f, -0.104f)
                quadToRelative(-0.188f, -0.104f, -0.271f, -0.312f)
                lineToRelative(-0.958f, -2.084f)
                lineToRelative(-2.084f, -0.958f)
                quadToRelative(-0.208f, -0.125f, -0.312f, -0.292f)
                quadToRelative(-0.104f, -0.166f, -0.104f, -0.333f)
                reflectiveQuadToRelative(0.104f, -0.354f)
                quadToRelative(0.104f, -0.188f, 0.312f, -0.271f)
                lineToRelative(2.084f, -0.958f)
                lineToRelative(0.958f, -2.125f)
                quadToRelative(0.083f, -0.167f, 0.271f, -0.271f)
                quadToRelative(0.187f, -0.104f, 0.354f, -0.104f)
                quadToRelative(0.167f, 0f, 0.333f, 0.104f)
                quadToRelative(0.167f, 0.104f, 0.292f, 0.271f)
                lineToRelative(0.958f, 2.125f)
                lineToRelative(2.084f, 0.958f)
                quadToRelative(0.208f, 0.083f, 0.312f, 0.271f)
                quadToRelative(0.104f, 0.187f, 0.104f, 0.354f)
                quadToRelative(0f, 0.167f, -0.104f, 0.333f)
                quadToRelative(-0.104f, 0.167f, -0.312f, 0.292f)
                lineToRelative(-2.084f, 0.958f)
                lineToRelative(-0.958f, 2.084f)
                quadToRelative(-0.125f, 0.208f, -0.292f, 0.312f)
                quadToRelative(-0.166f, 0.104f, -0.333f, 0.104f)
                close()
            }
        }.build()
    }
}

@Composable
fun rememberFeaturedPlayList(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "featured_play_list",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(11.375f, 21.208f)
                horizontalLineTo(23.5f)
                quadToRelative(0.542f, 0f, 0.917f, -0.396f)
                quadToRelative(0.375f, -0.395f, 0.375f, -0.937f)
                reflectiveQuadToRelative(-0.375f, -0.937f)
                quadToRelative(-0.375f, -0.396f, -0.917f, -0.396f)
                horizontalLineTo(11.375f)
                quadToRelative(-0.542f, 0f, -0.937f, 0.396f)
                quadToRelative(-0.396f, 0.395f, -0.396f, 0.937f)
                reflectiveQuadToRelative(0.396f, 0.937f)
                quadToRelative(0.395f, 0.396f, 0.937f, 0.396f)
                close()
                moveToRelative(0f, -5.208f)
                horizontalLineTo(23.5f)
                quadToRelative(0.542f, 0f, 0.917f, -0.396f)
                reflectiveQuadToRelative(0.375f, -0.937f)
                quadToRelative(0f, -0.542f, -0.375f, -0.917f)
                reflectiveQuadToRelative(-0.917f, -0.375f)
                horizontalLineTo(11.375f)
                quadToRelative(-0.542f, 0f, -0.937f, 0.375f)
                quadToRelative(-0.396f, 0.375f, -0.396f, 0.917f)
                quadToRelative(0f, 0.583f, 0.396f, 0.958f)
                quadToRelative(0.395f, 0.375f, 0.937f, 0.375f)
                close()
                moveTo(6.25f, 33.083f)
                quadToRelative(-1.083f, 0f, -1.854f, -0.791f)
                quadToRelative(-0.771f, -0.792f, -0.771f, -1.834f)
                verticalLineTo(9.542f)
                quadToRelative(0f, -1.042f, 0.771f, -1.834f)
                quadToRelative(0.771f, -0.791f, 1.854f, -0.791f)
                horizontalLineToRelative(27.5f)
                quadToRelative(1.083f, 0f, 1.854f, 0.791f)
                quadToRelative(0.771f, 0.792f, 0.771f, 1.834f)
                verticalLineToRelative(20.916f)
                quadToRelative(0f, 1.042f, -0.771f, 1.834f)
                quadToRelative(-0.771f, 0.791f, -1.854f, 0.791f)
                close()
                moveToRelative(0f, -2.625f)
                verticalLineTo(9.542f)
                verticalLineToRelative(20.916f)
                close()
                moveToRelative(0f, 0f)
                horizontalLineToRelative(27.5f)
                verticalLineTo(9.542f)
                horizontalLineTo(6.25f)
                verticalLineToRelative(20.916f)
                close()
            }
        }.build()
    }
}

@Composable
fun rememberNestClockFarsightAnalog(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "nest_clock_farsight_analog",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(21.333f, 20.417f)
                lineToRelative(4.125f, 4.166f)
                quadToRelative(0.417f, 0.375f, 0.396f, 0.917f)
                quadToRelative(-0.021f, 0.542f, -0.396f, 0.917f)
                quadToRelative(-0.416f, 0.416f, -0.958f, 0.416f)
                reflectiveQuadToRelative(-0.917f, -0.416f)
                lineToRelative(-3.958f, -3.959f)
                quadToRelative(-0.5f, -0.458f, -0.708f, -1.041f)
                quadToRelative(-0.209f, -0.584f, -0.209f, -1.209f)
                verticalLineToRelative(-5.5f)
                quadToRelative(0f, -0.541f, 0.375f, -0.937f)
                reflectiveQuadToRelative(0.917f, -0.396f)
                quadToRelative(0.542f, 0f, 0.938f, 0.396f)
                quadToRelative(0.395f, 0.396f, 0.395f, 0.937f)
                close()
                moveTo(20f, 6.208f)
                quadToRelative(0.542f, 0f, 0.938f, 0.396f)
                quadToRelative(0.395f, 0.396f, 0.395f, 0.938f)
                verticalLineToRelative(0.833f)
                quadToRelative(0f, 0.542f, -0.395f, 0.938f)
                quadToRelative(-0.396f, 0.395f, -0.938f, 0.395f)
                quadToRelative(-0.542f, 0f, -0.917f, -0.395f)
                quadToRelative(-0.375f, -0.396f, -0.375f, -0.938f)
                verticalLineToRelative(-0.833f)
                quadToRelative(0f, -0.542f, 0.375f, -0.938f)
                quadToRelative(0.375f, -0.396f, 0.917f, -0.396f)
                close()
                moveTo(33.792f, 20f)
                quadToRelative(0f, 0.542f, -0.396f, 0.917f)
                reflectiveQuadToRelative(-0.938f, 0.375f)
                horizontalLineToRelative(-0.833f)
                quadToRelative(-0.542f, 0f, -0.937f, -0.375f)
                quadToRelative(-0.396f, -0.375f, -0.396f, -0.917f)
                reflectiveQuadToRelative(0.396f, -0.938f)
                quadToRelative(0.395f, -0.395f, 0.937f, -0.395f)
                horizontalLineToRelative(0.833f)
                quadToRelative(0.542f, 0f, 0.938f, 0.395f)
                quadToRelative(0.396f, 0.396f, 0.396f, 0.938f)
                close()
                moveTo(20f, 30.292f)
                quadToRelative(0.542f, 0f, 0.938f, 0.396f)
                quadToRelative(0.395f, 0.395f, 0.395f, 0.937f)
                verticalLineToRelative(0.833f)
                quadToRelative(0f, 0.542f, -0.395f, 0.938f)
                quadToRelative(-0.396f, 0.396f, -0.938f, 0.396f)
                quadToRelative(-0.542f, 0f, -0.917f, -0.396f)
                reflectiveQuadToRelative(-0.375f, -0.938f)
                verticalLineToRelative(-0.833f)
                quadToRelative(0f, -0.542f, 0.375f, -0.937f)
                quadToRelative(0.375f, -0.396f, 0.917f, -0.396f)
                close()
                moveTo(9.708f, 20f)
                quadToRelative(0f, 0.542f, -0.396f, 0.917f)
                quadToRelative(-0.395f, 0.375f, -0.937f, 0.375f)
                horizontalLineToRelative(-0.833f)
                quadToRelative(-0.542f, 0f, -0.938f, -0.375f)
                quadToRelative(-0.396f, -0.375f, -0.396f, -0.917f)
                reflectiveQuadToRelative(0.396f, -0.938f)
                quadToRelative(0.396f, -0.395f, 0.938f, -0.395f)
                horizontalLineToRelative(0.833f)
                quadToRelative(0.542f, 0f, 0.937f, 0.395f)
                quadToRelative(0.396f, 0.396f, 0.396f, 0.938f)
                close()
                moveTo(20f, 36.375f)
                quadToRelative(-3.375f, 0f, -6.375f, -1.292f)
                quadToRelative(-3f, -1.291f, -5.208f, -3.521f)
                quadToRelative(-2.209f, -2.229f, -3.5f, -5.208f)
                quadTo(3.625f, 23.375f, 3.625f, 20f)
                quadToRelative(0f, -3.417f, 1.292f, -6.396f)
                quadToRelative(1.291f, -2.979f, 3.521f, -5.208f)
                quadToRelative(2.229f, -2.229f, 5.208f, -3.5f)
                reflectiveQuadTo(20f, 3.625f)
                quadToRelative(3.417f, 0f, 6.396f, 1.292f)
                quadToRelative(2.979f, 1.291f, 5.208f, 3.5f)
                quadToRelative(2.229f, 2.208f, 3.5f, 5.187f)
                reflectiveQuadTo(36.375f, 20f)
                quadToRelative(0f, 3.375f, -1.292f, 6.375f)
                quadToRelative(-1.291f, 3f, -3.5f, 5.208f)
                quadToRelative(-2.208f, 2.209f, -5.187f, 3.5f)
                quadToRelative(-2.979f, 1.292f, -6.396f, 1.292f)
                close()
                moveToRelative(0f, -2.625f)
                quadToRelative(5.75f, 0f, 9.75f, -4.021f)
                reflectiveQuadToRelative(4f, -9.729f)
                quadToRelative(0f, -5.75f, -4f, -9.75f)
                reflectiveQuadToRelative(-9.75f, -4f)
                quadToRelative(-5.708f, 0f, -9.729f, 4f)
                quadToRelative(-4.021f, 4f, -4.021f, 9.75f)
                quadToRelative(0f, 5.708f, 4.021f, 9.729f)
                quadTo(14.292f, 33.75f, 20f, 33.75f)
                close()
                moveTo(20f, 20f)
                close()
            }
        }.build()
    }
}

@Composable
fun SingleWeatherDetailCard(
    @DrawableRes iconResId: Int,
    name: String,
    value: String,
    modifier: Modifier = Modifier
) {
    WeatherDetailCard(modifier) {
        WeatherDetailRow(iconResId, name, value)
    }
}

@Composable
fun WeatherDetailCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        content()
    }
}

@Composable
fun WeatherDetailRow(
    @DrawableRes iconResId: Int,
    name: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        WeatherIcon2(iconResId)
        WeatherDetails(name, value)
    }
}

@Composable
fun WeatherIcon2(@DrawableRes iconResId: Int) {
    Icon(
        painter = painterResource(id = iconResId),
        contentDescription = "Weather Icon",
        modifier = Modifier.size(40.dp),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun WeatherDetails(name: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(1f),
        horizontalAlignment = Alignment.End
    ) {
        WeatherDetailName(name)
        WeatherDetailValue(value)
    }
}

@Composable
fun WeatherDetailName(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun WeatherDetailValue(value: String) {
    Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1
    )
}


@Composable
fun TypingAnimatedText(
    text: String,
    modifier: Modifier = Modifier,
    delayBetweenEachChar: Long = 50L,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val currentIndex = animateTyping(text = text, delayBetweenEachChar = delayBetweenEachChar)

    DisplayText(
        text = text.substring(0, currentIndex),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
private fun animateTyping(text: String, delayBetweenEachChar: Long): Int {
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(text) {
        text.forEach { _ ->
            delay(delayBetweenEachChar)
            currentIndex++
        }
    }

    return currentIndex
}

@Composable
private fun DisplayText(
    text: String,
    modifier: Modifier,
    color: Color,
    fontSize: TextUnit,
    fontStyle: FontStyle?,
    fontWeight: FontWeight?,
    fontFamily: FontFamily?,
    letterSpacing: TextUnit,
    textDecoration: TextDecoration?,
    textAlign: TextAlign?,
    lineHeight: TextUnit,
    overflow: TextOverflow,
    softWrap: Boolean,
    maxLines: Int,
    minLines: Int,
    onTextLayout: (TextLayoutResult) -> Unit,
    style: TextStyle
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}
