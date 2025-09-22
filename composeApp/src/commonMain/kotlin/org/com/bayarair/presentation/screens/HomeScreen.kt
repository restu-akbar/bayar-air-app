package org.com.bayarair.presentation.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import org.com.bayarair.data.model.MeterRecord
import org.com.bayarair.presentation.theme.activeButton
import org.com.bayarair.presentation.theme.activeButtonText
import org.com.bayarair.presentation.theme.inactiveButton
import org.com.bayarair.presentation.theme.inactiveButtonText
import org.com.bayarair.presentation.viewmodel.HomeViewModel

val columnGridProperties = GridProperties(
    enabled = true,
    xAxisProperties = GridProperties.AxisProperties(thickness = .2.dp, color = SolidColor(Color.Gray.copy(alpha = .6f))),
    yAxisProperties = GridProperties.AxisProperties(thickness = .2.dp, color = SolidColor(Color.Gray.copy(alpha = .6f))),
)

object HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm: HomeViewModel = koinScreenModel()
        val records by vm.records.collectAsState()
        val loading by vm.loading.collectAsState()
        val error by vm.error.collectAsState()






        val snackbarHost = remember { SnackbarHostState() }
        val rootNavigator = remember(navigator) {
            generateSequence(navigator) { it.parent }.last()
        }
        var name = "User" //
        var switcher by remember { mutableStateOf(true) } // History

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHost) }
        ) { padding ->
            Column(
            ) {
                Column(
                    modifier = Modifier
                        .padding(2.dp),
                ) {
                    Text(
                        text = "Bayar Air Dashboard",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Selamat Datang, $name !",
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = {
                                vm.loadHistory()
                                switcher = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (switcher)
                                    MaterialTheme.colorScheme.activeButton
                                else
                                    MaterialTheme.colorScheme.inactiveButton,
                                contentColor = if (switcher)
                                    MaterialTheme.colorScheme.activeButtonText
                                else
                                    MaterialTheme.colorScheme.inactiveButtonText,
                            )
                        ) {

                            Text("Statistik")
                        }

                        Button(
                            onClick = { switcher = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!switcher)
                                    MaterialTheme.colorScheme.activeButton
                                else
                                    MaterialTheme.colorScheme.inactiveButton,
                                contentColor = if (!switcher)
                                    MaterialTheme.colorScheme.activeButtonText
                                else
                                    MaterialTheme.colorScheme.inactiveButtonText,
                            )
                        ) {
                            Text("History")
                        }

                    }
                }
                if (switcher){
                    StatisticSection()
                }else{
                    HistorySection(records)
                }
            }

        }
    }
}

@Composable
fun HistorySection(records: List<MeterRecord>){//records: List<MeterRecord>
    val listState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow

    if (records.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Belum ada history", color = Color.Gray)
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(records) { records ->

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .clickable {
                            val record = null
                            navigator.push(StrukScreen(record))
                        }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${records.customer.name}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Column {
                                Text("Meter: ${records.meter}")
                                Text("Status: ${records.status}")
                                Text("Total: ${records.total_amount}")
                                Text("Tanggal: ${records.created_at_formatted}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticSection() {
    val data = remember {
        listOf(
            Bars(
                label = "Jan",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 12.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Feb",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 15.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Mar",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 20.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Apr",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 20.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Mei",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 15.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Jun",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 22.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Jul",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 23.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Aug",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 25.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Sep",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 30.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Oct",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 20.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Nov",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 22.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
            Bars(
                label = "Des",
                values = listOf(
                    Bars.Data(
                        label = "Pelanggan",
                        value = 20.0,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    ),
                )
            ),
        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Card(modifier=Modifier.height(270.dp).fillMaxWidth()
            .border(2.dp,Color.Transparent, RoundedCornerShape(12.dp)),
            elevation = CardDefaults.elevatedCardElevation(2.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)){
                ColumnChart(
                    modifier= Modifier
                        .fillMaxSize()
                        .padding(start = 22.dp, end = 12.dp)
                    ,
                    data = data,
                    barProperties = BarProperties(
                        cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
                        spacing = 1.dp,
                        thickness = 20.dp
                    ),
                    indicatorProperties = HorizontalIndicatorProperties(
                        textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                        count = IndicatorCount.CountBased(count = 4),
                        position = IndicatorPosition.Horizontal.Start,
                    ),
                    gridProperties = columnGridProperties,
                    labelProperties = LabelProperties(
                        enabled = true,
                        textStyle = TextStyle(fontSize = 12.sp, color = Color.Black)
                    ),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    animationMode = AnimationMode.Together(delayBuilder = {it*100L}),
                    animationDelay = 300,
                    popupProperties = PopupProperties(
                        textStyle = TextStyle(
                            fontSize = 11.sp,
                            color = Color.White,
                        ),
                        contentBuilder = { dataIndex, valueIndex, value ->
                            value.format(1) + " Million" + " - dataIdx: " + dataIndex + ", valueIdx: " + valueIndex
                        },
                        containerColor = Color(0xff414141),
                    ),
                    labelHelperProperties = LabelHelperProperties(textStyle = TextStyle(fontSize = 12.sp, color = Color.Black)),
                    onBarClick = { popupData ->
                        println(popupData.bar)
                    },
                    onBarLongClick = { popupData ->
                        println("long: ${popupData.bar}")
                    }
                )
            }
        }
    }

}