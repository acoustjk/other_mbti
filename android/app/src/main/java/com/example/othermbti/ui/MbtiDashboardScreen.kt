package com.example.othermbti.ui

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.othermbti.data.Evaluation
import com.example.othermbti.data.MbtiGapAnalytics
import com.example.othermbti.data.MbtiRepository
import com.example.othermbti.data.User

// Design Tokens
val DeepDarkBg = Color(0xFF0F172A)
val CardDarkBg = Color(0xFF1E293B)
val KakaoYellow = Color(0xFFFEE500)
val KakaoBrown = Color(0xFF3C1E1E)
val PrimaryPurple = Color(0xFF8B5CF6)
val PrimaryIndigo = Color(0xFF6366F1)
val AccentCyan = Color(0xFF06B6D4)
val AccentPink = Color(0xFFEC4899)
val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MbtiDashboardScreen(
    repository: MbtiRepository,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val user by repository.user.collectAsState()
    val evaluations by repository.evaluations.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val analytics = remember(user, evaluations) {
        repository.calculateAnalytics()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(), // Ensures screen stays inside status bar & navigation bar
        containerColor = DeepDarkBg,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                color = Color.Transparent
            ) {
                Button(
                    onClick = { onShowToast("MBTI 갭 리포트 요약 카드가 저장되었습니다.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "인스타 스토리 / 카톡 결과 이미지 저장",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "내 MBTI 갭 리포트",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "지인들이 바라본 나 vs 내가 아는 나",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = Color.White
                        )
                    }
                }
            }

            // Profile Card
            item {
                ProfileSummaryCard(user = user, perceivedMbti = analytics.perceivedMbti)
            }

            // Share Banner Card with Real Android Share Intent
            item {
                ShareBannerCard(
                    userNickname = user.nickname,
                    onShareKakao = {
                        val shareMessage = "[${user.nickname}]이가 보는 내 MBTI는? 1분만에 평가해줘!\n내가 아는 나 vs 친구들이 보는 나의 MBTI Gap을 솔직하게 평가해주세요.\n👉 https://othermbti-app-2026.surge.sh/test?target=${user.uid}"
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "[${user.nickname}]의 MBTI 평가 요청")
                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                        }

                        try {
                            val kakaoIntent = Intent(sendIntent).apply {
                                setPackage("com.kakao.talk")
                            }
                            context.startActivity(kakaoIntent)
                        } catch (e: Exception) {
                            val chooser = Intent.createChooser(sendIntent, "카카오톡 또는 친구에게 공유하기")
                            context.startActivity(chooser)
                        }
                    },
                    onCopyLink = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("MBTI Test Link", "https://othermbti-app-2026.surge.sh/test?target=${user.uid}")
                        clipboard.setPrimaryClip(clip)
                        onShowToast("평가 링크가 클립보드에 복사되었습니다!")
                    }
                )
            }

            // Quick Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Person,
                        iconBg = PrimaryPurple.copy(alpha = 0.2f),
                        iconTint = PrimaryPurple,
                        label = "총 참여 지인 수",
                        value = "${analytics.totalCount}명"
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Star,
                        iconBg = KakaoYellow.copy(alpha = 0.2f),
                        iconTint = KakaoYellow,
                        label = "가장 큰 MBTI 차이",
                        value = analytics.maxGapDimension
                    )
                }
            }

            // Gap Analysis Charts
            item {
                GapAnalyticsCard(userSelfMbti = user.selfMbti, analytics = analytics)
            }

            // Signature Keywords TOP 3
            item {
                KeywordsCard(topKeywords = analytics.topKeywords)
            }

            // Realtime Evaluators History
            item {
                EvaluatorsListCard(evaluations = evaluations)
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditDialog = false },
            onSave = { newNick, newSelf ->
                repository.updateUser(newNick, newSelf)
                showEditDialog = false
                onShowToast("프로필 및 내 MBTI가 저장되었습니다.")
            }
        )
    }
}

@Composable
fun ProfileSummaryCard(user: User, perceivedMbti: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AccentPink, PrimaryPurple))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.nickname.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = user.nickname,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "내가 생각한 MBTI: ${user.selfMbti}",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = KakaoYellow.copy(alpha = 0.2f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(KakaoYellow, KakaoYellow)))
                ) {
                    Text(
                        text = "지인이 본 MBTI: $perceivedMbti",
                        color = KakaoYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShareBannerCard(
    userNickname: String,
    onShareKakao: () -> Unit,
    onCopyLink: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Send, contentDescription = null, tint = KakaoYellow)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "친구들에게 평가 요청하기",
                        color = KakaoYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "링크를 공유하고 솔직한 MBTI 진단을 받아보세요!",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onShareKakao,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KakaoYellow, contentColor = KakaoBrown),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("카톡 공유", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCopyLink,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("링크 복사", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = label, color = TextMuted, fontSize = 11.sp)
                Text(text = value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun GapAnalyticsCard(userSelfMbti: String, analytics: MbtiGapAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 MBTI 지표별 Gap 분석",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            GapDimensionRow(
                title = "외향 (E) vs 내향 (I)",
                perceivedLabel = "E ${analytics.pctE}% / I ${analytics.pctI}%",
                percentage = analytics.pctE,
                selfIsRight = userSelfMbti.contains("I")
            )

            GapDimensionRow(
                title = "감각 (S) vs 직관 (N)",
                perceivedLabel = "S ${analytics.pctS}% / N ${analytics.pctN}%",
                percentage = analytics.pctS,
                selfIsRight = userSelfMbti.contains("N")
            )

            GapDimensionRow(
                title = "사고 (T) vs 감정 (F)",
                perceivedLabel = "T ${analytics.pctT}% / F ${analytics.pctF}%",
                percentage = analytics.pctT,
                selfIsRight = userSelfMbti.contains("F")
            )

            GapDimensionRow(
                title = "판단 (J) vs 인식 (P)",
                perceivedLabel = "J ${analytics.pctJ}% / P ${analytics.pctP}%",
                percentage = analytics.pctJ,
                selfIsRight = userSelfMbti.contains("P")
            )
        }
    }
}

@Composable
fun GapDimensionRow(
    title: String,
    perceivedLabel: String,
    percentage: Int,
    selfIsRight: Boolean
) {
    val animatedWidth by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "width"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = perceivedLabel, color = AccentCyan, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF334155))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedWidth)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(PrimaryIndigo, PrimaryPurple)))
            )
        }
    }
}

@Composable
fun KeywordsCard(topKeywords: List<com.example.othermbti.data.KeywordRank>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🏷️ 지인들이 꼽은 내 대표 모습 (TOP 3)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            if (topKeywords.isEmpty()) {
                Text("아직 수집된 키워드가 없습니다.", color = TextMuted, fontSize = 13.sp)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(topKeywords.take(5)) { index, rank ->
                        val chipBg = when (index) {
                            0 -> PrimaryPurple.copy(alpha = 0.3f)
                            1 -> PrimaryIndigo.copy(alpha = 0.3f)
                            else -> KakaoYellow.copy(alpha = 0.2f)
                        }
                        val chipText = if (index == 2) KakaoYellow else Color.White

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = chipBg,
                            border = BorderStroke(1.dp, chipText.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("${index + 1}위", color = chipText, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                Text(rank.keyword, color = chipText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("(${rank.count}회)", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluatorsListCard(evaluations: List<Evaluation>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "💬 실시간 지인 응답 목록",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            if (evaluations.isEmpty()) {
                Text("아직 참여한 지인이 없습니다.", color = TextMuted, fontSize = 13.sp)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    evaluations.reversed().forEach { ev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), shape = RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ev.evaluatorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryPurple.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    ev.resultMbti,
                                    color = PrimaryPurple,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nick by remember { mutableStateOf(user.nickname) }
    var selectedMbti by remember { mutableStateOf(user.selfMbti) }
    val mbtiList = listOf(
        "INTJ", "INTP", "ENTJ", "ENTP",
        "INFJ", "INFP", "ENFJ", "ENFP",
        "ISTJ", "ISFJ", "ESTJ", "ESFJ",
        "ISTP", "ISFP", "ESTP", "ESFP"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로필 및 내 MBTI 설정", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nick,
                    onValueChange = { nick = it },
                    label = { Text("닉네임") },
                    singleLine = true
                )

                Text("내가 생각하는 내 MBTI 선택", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    mbtiList.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { type ->
                                FilterChip(
                                    selected = (selectedMbti == type),
                                    onClick = { selectedMbti = type },
                                    label = { Text(type, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(nick, selectedMbti) }) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
