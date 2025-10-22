@file:OptIn(ExperimentalMaterial3Api::class)

package org.com.bayarair.presentation.screens


import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.com.bayarair.data.dto.Faq
import org.com.bayarair.presentation.component.LoadingOverlay
import org.com.bayarair.presentation.navigation.root
import org.com.bayarair.presentation.viewmodel.FaqViewModel


object FaqScreen : Screen {
    @Composable
    override fun Content() {
        val rootNav = LocalNavigator.currentOrThrow.root()
        val vm: FaqViewModel = koinScreenModel()
        val state by vm.state.collectAsState()
        val faqs = state.faqs

        LaunchedEffect(Unit) {
            vm.getFaq()
        }

        var search by rememberSaveable { mutableStateOf("") }

        var expandedSet by rememberSaveable { mutableStateOf(setOf<Int>()) }

        val filtered by remember(search, faqs) {
            derivedStateOf {
                val q = search.trim()
                if (q.isBlank()) faqs
                else {
                    val needle = q.lowercase()
                    faqs.filter {
                        it!!.question.contains(needle, ignoreCase = true) ||
                                it.answer.contains(needle, ignoreCase = true)
                    }
                }
            }
        }

        val bg = MaterialTheme.colorScheme.background
        val text = MaterialTheme.colorScheme.onBackground

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                            IconButton(onClick = { rootNav.popUntil { it is ProfileScreen } }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = bg,
                        navigationIconContentColor = text
                    ),
                )
            },
            containerColor = bg,
        ) { inner ->
            if (state.loading) {
                LoadingOverlay()
            } else {
                val ptrState = rememberPullToRefreshState()

                PullToRefreshBox(
                    isRefreshing = state.loading,
                    onRefresh = { vm.getFaq(force = true) },
                    state = ptrState,
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bg)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            Text(
                                text = "Kelola tagihan air jadi lebih mudah\ndengan Bayar Air 💧",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 30.sp
                                ),
                                color = text
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Aplikasi ini memudahkan Anda mencatat pemakaian air dan memantau tagihan secara terintegrasi.\n\nTemukan jawaban dari pertanyaan umum Anda di bawah ini.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = text
                            )
                            Spacer(Modifier.height(16.dp))

                            var isFocused by remember { mutableStateOf(false) }
                            BasicTextField(
                                value = search,
                                onValueChange = { search = it },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .shadow(4.dp, RoundedCornerShape(24.dp), clip = false)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(24.dp)
                                    )
                                    .padding(horizontal = 14.dp)
                                    .onFocusChanged { isFocused = it.isFocused },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                            ) { innerTextField ->
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Search, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (search.isEmpty() && !isFocused) {
                                            Text(
                                                "Cari FAQ",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        innerTextField()
                                    }
                                    if (search.isNotBlank()) {
                                        Spacer(Modifier.width(8.dp))
                                        IconButton(onClick = { search = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                        }

                        if (filtered.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Tidak ada hasil untuk “$search”. Coba kata kunci lain.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = text
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(
                                items = filtered,
                                key = { idx, item ->
                                    item!!.question.hashCode()
                                }
                            ) { index, item ->
                                val isExpanded = expandedSet.contains(index)
                                FaqRow(
                                    item = item,
                                    expanded = isExpanded,
                                    onToggle = {
                                        expandedSet =
                                            if (isExpanded) expandedSet - index else expandedSet + index
                                    },
                                    bubbleColor = bg,
                                    bubbleText = text
                                )
                                if (index != filtered.lastIndex) {
                                    HorizontalDivider(
                                        thickness = DividerDefaults.Thickness,
                                        color = MaterialTheme.colorScheme.outlineVariant
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

@Composable
private fun FaqRow(
    item: Faq?,
    expanded: Boolean,
    onToggle: () -> Unit,
    bubbleColor: Color,
    bubbleText: Color
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chev-rotate")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onToggle,
                onClickLabel = if (expanded) "Collapse answer" else "Expand answer"
            )
            .padding(vertical = 14.dp)
            .animateContentSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item!!.question,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.rotate(rotation)
            )
        }

        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = bubbleColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
            ) {
                item?.answer?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = bubbleText
                    )
                }
            }
        }
    }
}
