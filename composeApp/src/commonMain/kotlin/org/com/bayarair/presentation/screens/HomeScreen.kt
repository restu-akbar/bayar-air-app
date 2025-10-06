package org.com.bayarair.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.com.bayarair.data.dto.BarChart
import org.com.bayarair.data.dto.MeterRecord
import org.com.bayarair.data.dto.PieChart
import org.com.bayarair.presentation.component.LoadingOverlay
import org.com.bayarair.presentation.component.MarqueeText
import org.com.bayarair.presentation.navigation.root
import org.com.bayarair.presentation.theme.inactiveButtonText
import org.com.bayarair.presentation.viewmodel.HomeViewModel
import org.com.bayarair.presentation.viewmodel.ProfileViewModel
import org.com.bayarair.presentation.viewmodel.RecordHistoryShared
import org.com.bayarair.presentation.viewmodel.StatsShared
import org.com.bayarair.presentation.viewmodel.UserShared
import org.com.bayarair.utils.DateUtils
import org.koin.compose.koinInject
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private enum class RefreshOwner { NONE, TOP, CHART, HISTORY }

@OptIn(ExperimentalMaterial3Api::class)
object HomeScreen : Screen {
    @OptIn(ExperimentalTime::class)
    @Composable
    override fun Content() {
        val vm: HomeViewModel = koinScreenModel<HomeViewModel>()
        val vmProfile: ProfileViewModel = koinScreenModel<ProfileViewModel>()

        val state by vm.state.collectAsState()
        val profileState by vmProfile.state.collectAsState()

        var graph by rememberSaveable { mutableStateOf(true) }

        var refreshOwner by rememberSaveable { mutableStateOf(RefreshOwner.NONE) }

        val isTopRefreshing = (refreshOwner == RefreshOwner.TOP)

        val historyShared: RecordHistoryShared = koinInject()
        val history by historyShared.history.collectAsState()

        val userShared: UserShared = koinInject()
        val user by userShared.user.collectAsState()

        val statsShared: StatsShared = koinInject()
        val pieChart by statsShared.pieChart.collectAsState()
        val barChart by statsShared.barChart.collectAsState()

        LaunchedEffect(profileState.loading, state.loading) {
            if (!profileState.loading && !state.loading) {
                refreshOwner = RefreshOwner.NONE
            }
        }

        val globalPtr = rememberPullToRefreshState()

        LaunchedEffect(Unit) {
            if (pieChart == null || barChart == null) {
                vm.init(month = currentMonth(), force = true, isPieChart = state.switcher)
            }
            vmProfile.getUser()
        }
        LaunchedEffect(state.switcher) {
            if (!state.switcher) vm.loadHistory()
        }

        Scaffold { innerPadding ->
            if (pieChart !== null || user !== null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                        PullToRefreshBox(
                            isRefreshing = isTopRefreshing,
                            onRefresh = {
                                refreshOwner = RefreshOwner.TOP
                                vmProfile.getUser(true)
                                if (state.switcher) {
                                    if (graph) {
                                        vm.getPieChartData(force = true, month = currentMonth())
                                    } else {
                                        val yearNow = Clock.System.now()
                                            .toLocalDateTime(TimeZone.currentSystemDefault()).year
                                        vm.getBarChartData(force = true, year = yearNow)
                                    }
                                } else {
                                    vm.loadHistory(force = true)
                                }
                            },
                            state = globalPtr,
                            modifier = Modifier
                                .zIndex(1f)
                        ) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                    text = "Bayar Air Dashboard",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(4.dp))
                                @OptIn(ExperimentalFoundationApi::class)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RectangleShape),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MarqueeText(
                                        text = "Selamat Datang, ${user?.name ?: "User"} !",
                                        modifier = Modifier.weight(1f),
                                        speedDpPerSec = 50f
                                    )
                                }
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = { vm.switchStats(true) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (state.switcher)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.secondary,
                                            contentColor = if (state.switcher) Color.Black
                                            else MaterialTheme.colorScheme.inactiveButtonText,
                                        )
                                    ) {
                                        Icon(Icons.Default.QueryStats, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Statistik")
                                    }
                                    Spacer(Modifier.size(12.dp))
                                    Button(
                                        onClick = { vm.switchStats(false) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!state.switcher)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.secondary,
                                            contentColor = if (!state.switcher) Color.Black
                                            else MaterialTheme.colorScheme.inactiveButtonText,
                                        )
                                    ) {
                                        Icon(Icons.Default.History, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("History")
                                    }
                                }
                            }
                        }
                        if (state.switcher) {
                            ChartSwitcher(
                                vm = vm,
                                graph = graph,
                                onGraphChanged = { graph = it },
                                isOwnerChart = (refreshOwner == RefreshOwner.CHART),
                                onLocalRefreshStart = { refreshOwner = RefreshOwner.CHART },
                                pieChart = pieChart,
                                barChart = barChart
                            )
                        } else {
                            if (state.loading) {
                                LoadingOverlay()
                            } else {
                                HistorySection(
                                    records = history,
                                    isRefreshing = (refreshOwner == RefreshOwner.HISTORY) && state.loading,
                                    onRefresh = {
                                        refreshOwner = RefreshOwner.HISTORY
                                        vm.loadHistory(force = true)
                                    })

                            }
                        }
                    }

                }
            } else {
                LoadingOverlay()
            }
        }
    }
}

@OptIn(ExperimentalTime::class, ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChartSwitcher(
    vm: HomeViewModel,
    pieChart: PieChart?,
    barChart: BarChart?,
    graph: Boolean,
    onGraphChanged: (Boolean) -> Unit,
    isOwnerChart: Boolean,
    onLocalRefreshStart: () -> Unit
) {
    val months = DateUtils.months
    val years = (2025..2030).toList()

    var monthMenuExpanded by remember { mutableStateOf(false) }
    var graphMenuExpanded by remember { mutableStateOf(false) }
    var selectedMonth by rememberSaveable { mutableStateOf(currentMonth()) }
    var selectedYear by rememberSaveable { mutableStateOf(years.first()) }

    val state by vm.state.collectAsState()

    val ptrState = rememberPullToRefreshState()
    val isChartRefreshing = isOwnerChart && state.loading

    val currentYear = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    }
    LaunchedEffect(graph) {
        if (!graph) vm.getBarChartData(force = true, year = currentYear)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box {
            Button(
                onClick = { monthMenuExpanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(6.dp),
            ) {
                Icon(
                    if (graph) Icons.Default.CalendarMonth else Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (graph)
                        months.first { it.first == selectedMonth }.second
                    else
                        selectedYear.toString()
                )
            }

            if (graph) {
                MonthDropdown(
                    expanded = monthMenuExpanded,
                    onDismissRequest = { monthMenuExpanded = false },
                    selectedMonth = selectedMonth,
                    onMonthSelected = { m ->
                        selectedMonth = m
                        vm.getPieChartData(force = true, month = selectedMonth)
                        monthMenuExpanded = false
                    },
                    months = months
                )
            } else {
                val cs = MaterialTheme.colorScheme
                DropdownMenu(
                    expanded = monthMenuExpanded,
                    onDismissRequest = { monthMenuExpanded = false },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(cs.surface)
                ) {
                    years.forEachIndexed { i, y ->
                        val selected = y == selectedYear
                        DropdownMenuItem(
                            text = { Text(y.toString()) },
                            onClick = {
                                selectedYear = y
                                vm.getBarChartData(force = true, year = selectedYear)
                                monthMenuExpanded = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (selected) cs.primary else Color.Transparent),
                            colors = MenuDefaults.itemColors(
                                textColor = if (selected) cs.primaryContainer else cs.onSurface,
                                leadingIconColor = if (selected) cs.primaryContainer else cs.onSurfaceVariant,
                                trailingIconColor = if (selected) cs.primaryContainer else cs.onSurfaceVariant,
                                disabledTextColor = cs.onSurface.copy(alpha = 0.38f)
                            )
                        )
                    }
                }
            }
        }

        Box {
            Button(
                onClick = { graphMenuExpanded = true },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (graph) "Pie Chart" else "Bar Chart")
            }

            DropdownMenu(
                expanded = graphMenuExpanded,
                onDismissRequest = { graphMenuExpanded = false },
                offset = DpOffset(0.dp, 4.dp),
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                val scheme = MaterialTheme.colorScheme
                val pieBg = if (graph) scheme.background else scheme.secondary
                val pieContent = if (graph) scheme.onBackground else scheme.onSecondary
                val barBg = if (!graph) scheme.background else scheme.secondary
                val barContent = if (!graph) scheme.onBackground else scheme.onSecondary

                DropdownMenuItem(
                    text = { Text("Pie Chart") },
                    onClick = {
                        onGraphChanged(true)
                        graphMenuExpanded = false
                    },
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(pieBg),
                    colors = MenuDefaults.itemColors(
                        textColor = pieContent,
                        leadingIconColor = pieContent,
                        trailingIconColor = pieContent
                    )
                )

                DropdownMenuItem(
                    text = { Text("Bar Chart") },
                    onClick = {
                        onGraphChanged(false)
                        graphMenuExpanded = false
                    },
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(barBg),
                    colors = MenuDefaults.itemColors(
                        textColor = barContent,
                        leadingIconColor = barContent,
                        trailingIconColor = barContent
                    )
                )
            }
        }
    }
    Spacer(Modifier.size(2.dp))
    PullToRefreshBox(
        isRefreshing = isChartRefreshing,
        onRefresh = {
            onLocalRefreshStart()
            if (graph) vm.getPieChartData(force = true, month = selectedMonth)
            else vm.getBarChartData(force = true, year = selectedYear)
        },
        state = ptrState,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = if (pieChart == null && graph) {
                Alignment.Center
            } else if (barChart == null && !graph) {
                Alignment.Center
            } else {
                Alignment.TopCenter
            }
        ) {
            when {
                graph && pieChart != null -> {
                    PieChartView(state.totalCust, pieChart)
                }

                !graph && barChart != null -> {
                    BarChartView(state.totalCust, barChart)
                }

                else -> {
                    LoadingOverlay()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
    months: List<Pair<Int, String>>,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .size(width = 320.dp, height = 150.dp)
        ) {
            items(months) { (m, name) ->
                val isSelected = m == selectedMonth
                val shortName = name.take(3)

                Button(
                    onClick = {
                        onMonthSelected(m)
                        onDismissRequest()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected)
                            Color.White
                        else
                            MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .width(70.dp)
                        .height(40.dp)
                ) {
                    Text(
                        text = shortName,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun currentMonth(): Int {
    return try {
        Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .month.number
    } catch (_: Throwable) {
        1
    }
}

@Composable
fun PieChartView(totalCust: Int, pieChart: PieChart) {
    val scheme = MaterialTheme.colorScheme
    val recordedByUser = (pieChart.totalUser).coerceAtLeast(0)
    val recordedByOthers = (pieChart.totalUserLain).coerceAtLeast(0)
    val remaining = (totalCust - recordedByUser - recordedByOthers).coerceAtLeast(0)
    val values = listOf(
        recordedByUser.toFloat(),
        recordedByOthers.toFloat(),
        remaining.toFloat()
    )
    val labels = listOf(
        "Pelanggan tercatat oleh anda",
        "Pelanggan tercatat oleh petugas lain",
        "Belum tercatat"
    )
    val total = values.sum().coerceAtLeast(0f)
    val colors = listOf(
        Color(0xFF0D47A1),
        Color(0xFF1565C0),
        Color(0xFF64B5F6),
    )
    val strokeWidth = 16.dp
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    var showTooltip by remember { mutableStateOf(false) }
    val formatter = remember { DecimalFormat("#.##") }
    val valueChange: Float = (pieChart.persentase.toFloat())
    val (icon, color) = when {
        valueChange > 0f -> Pair(Icons.Default.ArrowUpward, scheme.tertiaryContainer)
        valueChange < 0f -> Pair(Icons.Default.ArrowDownward, scheme.error)
        else -> Pair(Icons.Default.Remove, Color.Gray)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.primaryContainer),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val diameter = minOf(maxWidth, 180.dp)
                Box(
                    modifier = Modifier
                        .width(diameter)
                        .aspectRatio(1f)
                        .align(Alignment.Center)
                ) box@{
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(values, total, strokeWidth) {
                                val scope = this
                                detectTapGestures(
                                    onPress = { pos ->
                                        val sizePx = scope.size
                                        val center = Offset(sizePx.width / 2f, sizePx.height / 2f)
                                        val dx = pos.x - center.x
                                        val dy = pos.y - center.y
                                        val distance = hypot(dx, dy)
                                        val R = kotlin.math.min(sizePx.width, sizePx.height) / 2f
                                        val ringPx = with(scope) { strokeWidth.toPx() }

                                        val innerRadius = kotlin.math.max(0f, R - 1.5f * ringPx)
                                        val outerRadius = R + 0.5f * ringPx
                                        var found: Int? = null

                                        if (distance in innerRadius..outerRadius) {
                                            var tapAngle =
                                                Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                                                    .toFloat()
                                            if (tapAngle < 0f) tapAngle += 360f

                                            var normalizedAngle = (tapAngle + 90f) % 360f

                                            val totalSafe = if (total <= 0f) 1f else total
                                            var currentAngle = 0f
                                            for (i in values.indices) {
                                                val v = values[i]
                                                val sweepAngle = 360f * (v / totalSafe)
                                                val endAngle = currentAngle + sweepAngle

                                                if (sweepAngle > 0.0001f && normalizedAngle >= currentAngle && normalizedAngle < endAngle) {
                                                    found = i
                                                    break
                                                }

                                                currentAngle = endAngle
                                            }
                                        }

                                        if (found != null) {
                                            selectedIndex = found
                                            tapOffset = pos
                                            showTooltip = true
                                        } else {
                                            showTooltip = false
                                            selectedIndex = null
                                        }
                                        tryAwaitRelease()
                                    },
                                    onTap = { /* no-op */ }
                                )
                            }
                    ) {
                        val ring = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                        var startAngle = -90f
                        values.forEachIndexed { index, value ->
                            val sweepAngle =
                                360f * (if (total == 0f) 0f else (value / total)) * progress.value
                            val isSelected = selectedIndex == index
                            val drawRing = if (isSelected) {
                                Stroke(width = ring.width * 1.25f, cap = StrokeCap.Butt)
                            } else ring
                            drawArc(
                                color = colors.getOrElse(index) { scheme.secondary },
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = drawRing
                            )
                            startAngle += sweepAngle
                        }
                    }
                    run {
                        val pctTarget = if (total <= 0f) 0f else (remaining / total) * 100f
                        val shownPct = pctTarget * progress.value
                        Box(
                            modifier = Modifier.matchParentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${formatter.format(shownPct)}%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onPrimaryContainer
                                )
                                Text(
                                    text = "pelanggan belum tercatat",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = scheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    if (showTooltip && selectedIndex != null) {
                        val idx = selectedIndex ?: 0
                        val v = values[idx]
                        val pct = if (total == 0f) 0f else (v / total) * 100f
                        val tipText =
                            "${labels[idx]}: ${formatter.format(v)} (${formatter.format(pct)}%)"

                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val density = LocalDensity.current

                            // Ukur lebar tooltip (estimasi)
                            val tooltipWidthPx = with(density) { 200.dp.toPx() }
                            val tooltipHeightPx = with(density) { 50.dp.toPx() }

                            val maxWidthPx = with(density) { maxWidth.toPx() }
                            val maxHeightPx = with(density) { maxHeight.toPx() }

                            // Hitung posisi dengan boundary check
                            var offsetX = tapOffset.x - (tooltipWidthPx / 2)
                            var offsetY = tapOffset.y - tooltipHeightPx - 32f

                            // Cek batas kiri
                            if (offsetX < 16f) {
                                offsetX = 16f
                            }
                            // Cek batas kanan
                            if (offsetX + tooltipWidthPx > maxWidthPx - 16f) {
                                offsetX = maxWidthPx - tooltipWidthPx - 16f
                            }
                            // Cek batas atas
                            if (offsetY < 16f) {
                                offsetY =
                                    tapOffset.y + 32f // Tampilkan di bawah jika tidak muat di atas
                            }
                            // Cek batas bawah
                            if (offsetY + tooltipHeightPx > maxHeightPx - 16f) {
                                offsetY = maxHeightPx - tooltipHeightPx - 16f
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                x = offsetX.roundToInt(),
                                                y = offsetY.roundToInt()
                                            )
                                        }
                                        .widthIn(max = 200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(scheme.surface)
                                        .border(
                                            1.dp,
                                            scheme.outline.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = tipText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = scheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = DateUtils.months.firstOrNull { it.first == pieChart.bulan }?.second ?: "-",
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$recordedByUser",
                    style = MaterialTheme.typography.headlineLarge,
                    color = scheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${formatter.format(valueChange)}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
            Text(
                text = "Pelanggan Tercatat oleh anda",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun BarChartView(totalCust: Int, barChart: BarChart) {
    val scheme = MaterialTheme.colorScheme

    fun normalizeMonthLabel(raw: String): String {
        val s = raw.trim().lowercase()
        val map = mapOf(
            "1" to "Jan", "01" to "Jan", "jan" to "Jan", "januari" to "Jan", "january" to "Jan",
            "2" to "Feb", "02" to "Feb", "feb" to "Feb", "februari" to "Feb", "february" to "Feb",
            "3" to "Mar", "03" to "Mar", "mar" to "Mar", "maret" to "Mar", "march" to "Mar",
            "4" to "Apr", "04" to "Apr", "apr" to "Apr", "april" to "Apr",
            "5" to "Mei", "05" to "Mei", "mei" to "Mei", "may" to "Mei",
            "6" to "Jun", "06" to "Jun", "jun" to "Jun", "juni" to "Jun", "june" to "Jun",
            "7" to "Jul", "07" to "Jul", "jul" to "Jul", "juli" to "Jul", "july" to "Jul",
            "8" to "Aug", "08" to "Aug", "aug" to "Aug", "agustus" to "Aug", "august" to "Aug",
            "9" to "Sep", "09" to "Sep", "sep" to "Sep", "september" to "Sep",
            "10" to "Oct", "okt" to "Oct", "oct" to "Oct", "oktober" to "Oct", "october" to "Oct",
            "11" to "Nov", "nov" to "Nov", "november" to "Nov",
            "12" to "Des", "des" to "Des", "dec" to "Des", "desember" to "Des", "december" to "Des",
        )
        return map[s] ?: raw.take(3).replaceFirstChar { it.uppercase() }
    }

    val monthOrder =
        listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Des")

    val barsData: List<Bars> = remember(barChart) {
        val src = barChart.data.orEmpty()
        val totalsByLabel = src
            .groupBy { normalizeMonthLabel(it.bulan) }
            .mapValues { (_, items) -> items.sumOf { it.total.toDouble() } }

        monthOrder.map { label ->
            val v = totalsByLabel[label] ?: 0.0
            Bars(
                label = label,
                values = listOf(
                    Bars.Data(
                        label = "Total Pelanggan Keseluruhan",
                        value = v,
                        color = Brush.verticalGradient(
                            listOf(
                                Color(0xFF0095e6),
                                Color(0xFF4f79ff),
                            )
                        )
                    )
                )
            )
        }
    }

    val dataMax = remember(barsData) {
        barsData.maxOfOrNull { it.values.maxOfOrNull { d -> d.value } ?: 0.0 } ?: 0.0
    }

    fun calcYStep(maxValue: Double, targetSteps: Int = 5): Double {
        if (maxValue <= 0) return 1.0

        val rawStep = maxValue / targetSteps
        val magnitude = 10.0.pow(floor(log10(rawStep)))
        val residual = rawStep / magnitude

        val niceResidual = when {
            residual < 1.5 -> 1.0
            residual < 3 -> 2.0
            residual < 7 -> 5.0
            else -> 10.0
        }

        return (niceResidual * magnitude)
    }

    val yMax = ceil(maxOf(totalCust.toDouble(), dataMax)).toInt()
    val yStep = calcYStep(yMax.toDouble())

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Card(
            modifier = Modifier
                .height(400.dp)
                .fillMaxWidth()
                .border(2.dp, Color.Transparent, RoundedCornerShape(12.dp)),
            elevation = CardDefaults.elevatedCardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp)
            ) {
                if (barChart.data.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada data bar chart",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )
                    }
                } else {
                    ColumnChart(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 22.dp, end = 12.dp),
                        data = barsData,
                        maxValue = yMax.toDouble(),

                        barProperties = BarProperties(
                            cornerRadius = Bars.Data.Radius.Rectangle(
                                topRight = 6.dp,
                                topLeft = 6.dp
                            ),
                            spacing = 1.dp,
                            thickness = 20.dp
                        ),
                        indicatorProperties = HorizontalIndicatorProperties(
                            textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                            count = IndicatorCount.StepBased(stepBy = yStep),
                            position = IndicatorPosition.Horizontal.Start,
                            contentBuilder = { value -> value.toInt().toString() }
                        ),
                        gridProperties = GridProperties(),
                        labelProperties = LabelProperties(
                            enabled = true,
                            textStyle = TextStyle(fontSize = 12.sp, color = Color.Black)
                        ),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        animationMode = AnimationMode.Together(delayBuilder = { it * 100L }),
                        animationDelay = 300,
                        popupProperties = PopupProperties(
                            textStyle = TextStyle(
                                fontSize = 11.sp,
                                color = Color.White,
                            ),
                            contentBuilder = { _, _, value ->
                                val jumlah = value.toInt()
                                "$jumlah Pelanggan Tercatat"
                            },
                            containerColor = Color(0xff414141),
                        ),
                        labelHelperProperties = LabelHelperProperties(
                            textStyle = TextStyle(
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        ),
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
}

enum class PayFilter {
    ALL, SUDAH, BELUM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySection(
    records: List<MeterRecord>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val listState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow
    val ptrState = rememberPullToRefreshState()

    var query by rememberSaveable { mutableStateOf("") }
    var filterExpanded by remember { mutableStateOf(false) }
    var payFilter by rememberSaveable { mutableStateOf(PayFilter.ALL) }

    val scheme = MaterialTheme.colorScheme

    val filteredRecords = remember(records, query, payFilter) {
        val q = query.trim()
        records.asSequence()
            .filter { r ->
                if (q.isEmpty()) true else r.customer.name.contains(
                    q,
                    ignoreCase = true
                )
            }
            .filter { r ->
                when (payFilter) {
                    PayFilter.ALL -> true
                    PayFilter.SUDAH -> r.status == "sudah_bayar"
                    PayFilter.BELUM -> r.status == "belum_bayar"
                }
            }
            .toList()
    }

    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, state = ptrState) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val shape = RoundedCornerShape(6.dp)
                    val searchHeight = 48.dp
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(searchHeight)
                            .clip(shape)
                            .background(scheme.primaryContainer)
                            .padding(horizontal = 12.dp)
                    ) {
                        var focused by remember { mutableStateOf(false) }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = scheme.onPrimaryContainer
                            ),
                            cursorBrush = SolidColor(scheme.onPrimaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .onFocusChanged { focused = it.isFocused }
                                .padding(vertical = 12.dp)
                        ) { innerTextField ->
                            Box(Modifier.fillMaxWidth()) {
                                if (query.isEmpty() && !focused) {
                                    Text(
                                        "Cari nama pelanggan",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = scheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Box {
                        FilledIconButton(
                            onClick = { filterExpanded = true },
                            modifier = Modifier
                                .height(searchHeight)
                                .width(searchHeight)
                                .clip(RoundedCornerShape(6.dp)),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = scheme.primaryContainer,
                                contentColor = scheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter status")
                        }

                        DropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false },
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                            )
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua") },
                                onClick = { payFilter = PayFilter.ALL; filterExpanded = false },
                                leadingIcon = {
                                    if (payFilter == PayFilter.ALL) Icon(
                                        Icons.Default.Check,
                                        null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sudah Bayar") },
                                onClick = { payFilter = PayFilter.SUDAH; filterExpanded = false },
                                leadingIcon = {
                                    if (payFilter == PayFilter.SUDAH) Icon(
                                        Icons.Default.Check,
                                        null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Belum Bayar") },
                                onClick = { payFilter = PayFilter.BELUM; filterExpanded = false },
                                leadingIcon = {
                                    if (payFilter == PayFilter.BELUM) Icon(
                                        Icons.Default.Check,
                                        null
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (filteredRecords.isEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .fillParentMaxHeight(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = scheme.primaryContainer)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Belum ada history",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                item {
                    Surface(
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp,
                        color = scheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "History Pencatatan",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            // Divider pertama agar konten di bawah terasa nyatu
                            HorizontalDivider(thickness = 0.6.dp, color = scheme.outlineVariant)
                        }
                    }
                }

                itemsIndexed(filteredRecords, key = { _, it -> it.id }) { index, record ->
                    val isLast = index == filteredRecords.lastIndex

                    val shape = when {
                        isLast -> RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                        else -> RectangleShape
                    }

                    Surface(
                        shape = shape,
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp,
                        color = scheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val interaction = remember { MutableInteractionSource() }
                        val isPressed by interaction.collectIsPressedAsState()
                        val overlayAlpha by animateFloatAsState(
                            targetValue = if (isPressed) 0.16f else 0f,
                            animationSpec = tween(60),
                            label = "pressOverlayAlpha"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.99f else 1f,
                            animationSpec = tween(60),
                            label = "pressScale"
                        )
                        val scope = rememberCoroutineScope()

                        Column(
                            modifier = Modifier
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .clip(shape)
                                .drawWithContent {
                                    drawContent()
                                    if (overlayAlpha > 0f) {
                                        drawRect(
                                            color = scheme.onPrimaryContainer,
                                            alpha = overlayAlpha
                                        )
                                    }
                                }
                                .clickable(
                                    interactionSource = interaction,
                                    indication = ripple(
                                        bounded = true,
                                        radius = 240.dp,
                                        color = scheme.onPrimaryContainer
                                    ),
                                    onClick = {
                                        scope.launch {
                                            delay(100)
                                            navigator.root().push(
                                                RecordDetailScreen(record.receipt, record.id, true)
                                            )
                                        }
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = record.customer.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${record.meter} m³",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = record.customer.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                val (statusLabel, bgColor, fgColor) = when (record.status) {
                                    "sudah_bayar" -> Triple(
                                        "Sudah Bayar",
                                        scheme.tertiaryContainer,
                                        scheme.onTertiaryContainer
                                    )

                                    "belum_bayar" -> Triple(
                                        "Belum Bayar",
                                        scheme.error.copy(alpha = 0.15f),
                                        scheme.error
                                    )

                                    else -> Triple(
                                        "-",
                                        scheme.surfaceVariant,
                                        scheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = fgColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
