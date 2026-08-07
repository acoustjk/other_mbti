package com.example.othermbti.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.othermbti.data.Evaluation
import com.example.othermbti.data.KeywordRank
import com.example.othermbti.data.MbtiRepository
import com.example.othermbti.data.User

// Cute 2030 Pastel Theme Palette
val DeepDarkBg = Color(0xFFFAF5FF) // Soft Lavender-Pink Cloud
val CardDarkBg = Color(0xFFFFFFFF) // Soft White Card
val KakaoYellow = Color(0xFFFEF08A) // Soft Butter Yellow
val KakaoBrown = Color(0xFF713F12)
val PrimaryPurple = Color(0xFFA78BFA) // Pastel Lavender
val PrimaryIndigo = Color(0xFF818CF8) // Soft Pastel Indigo
val AccentCyan = Color(0xFF38BDF8) // Pastel Sky Blue
val AccentPink = Color(0xFFF472B6) // Pastel Rose Pink
val TextMuted = Color(0xFF64748B)
val TextMain = Color(0xFF1E1B4B) // Soft Deep Indigo Text

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
    var showExportDialog by remember { mutableStateOf(false) }
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var showAdDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun runWithAd(action: () -> Unit) {
        pendingAction = action
        showAdDialog = true
    }

    val analytics = remember(user, evaluations) {
        repository.calculateAnalytics()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        containerColor = DeepDarkBg,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                color = Color.Transparent
            ) {
                Button(
                    onClick = { runWithAd { showExportDialog = true } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🌸 인스타 스토리 9:16 / 파스텔 카드 공유",
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
                            text = "🌸 모두의 MBTI",
                            color = TextMain,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "지인들이 바라본 나 vs 내가 아는 내 MBTI 갭 리포트",
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                    }

                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEDE9FE))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = PrimaryPurple
                        )
                    }
                }
            }

            // Profile Card
            item {
                ProfileSummaryCard(user = user, perceivedMbti = analytics.perceivedMbti)
            }

            // Share Banner Card with Direct Kakao Chooser & Link Copy Bottom Sheet (Ad Rewarded)
            item {
                ShareBannerCard(
                    userNickname = user.nickname,
                    onShareKakao = {
                        runWithAd {
                            val shareTitle = "🌸 [모두의 MBTI] ${user.nickname} 님의 MBTI 1분 평가 초댓장!"
                            val shareDesc = "내가 아는 나 vs 친구들이 보는 나의 MBTI Gap!"
                            val shareMessage = "$shareTitle\n$shareDesc\n\n👉 1분만에 평가하러 가기 🐣"



                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                setPackage("com.kakao.talk")
                            }

                            try {
                                val kakaoChooser = Intent.createChooser(sendIntent, "💛 카카오톡 친구 / 채팅방 선택").apply {
                                    putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(sendIntent))
                                }
                                context.startActivity(kakaoChooser)
                                onShowToast("카카오톡 친구 선택 팝업을 엽니다!")
                            } catch (e: Exception) {
                                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                                }
                                val chooser = Intent.createChooser(fallbackIntent, "카카오톡 친구 선택")
                                context.startActivity(chooser)
                            }
                        }
                    },
                    onCopyLink = {
                        runWithAd {
                            showShareBottomSheet = true
                        }
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
                        iconBg = PrimaryPurple.copy(alpha = 0.15f),
                        iconTint = PrimaryPurple,
                        label = "총 참여 지인 수",
                        value = "${analytics.totalCount}명"
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Star,
                        iconBg = Color(0xFFFEF08A),
                        iconTint = Color(0xFFA16207),
                        label = "가장 큰 MBTI 차이",
                        value = analytics.maxGapDimension
                    )
                }
            }

            // Gap Analytics Charts
            item {
                GapAnalyticsCard(userSelfMbti = user.selfMbti, analytics = analytics)
            }

            // Signature Keywords TOP 3
            item {
                KeywordsCard(topKeywords = analytics.topKeywords)
            }

            // MBTI Chemistry & Compatibility Radar Card
            item {
                MbtiChemistryCard(userSelfMbti = user.selfMbti, analytics = analytics)
            }

            // Realtime Evaluators History
            item {
                EvaluatorsListCard(userSelfMbti = user.selfMbti, evaluations = evaluations)
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

    if (showExportDialog) {
        ExportStoryDialog(
            userNickname = user.nickname,
            perceivedMbti = analytics.perceivedMbti,
            targetUid = user.uid,
            onDismiss = { showExportDialog = false },
            onShareInstagram = {
                val imageUri = generateStoryImageUri(context, user.nickname, user.selfMbti, analytics.perceivedMbti, user.uid)
                if (imageUri != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    try {
                        val instaIntent = Intent(shareIntent).apply {
                            setPackage("com.instagram.android")
                        }
                        context.startActivity(instaIntent)
                        onShowToast("9:16 파스텔 스토리 이미지를 인스타그램 앱으로 전달합니다!")
                    } catch (e: Exception) {
                        val chooser = Intent.createChooser(shareIntent, "📸 9:16 인스타 스토리 / 이미지 카드로 공유")
                        context.startActivity(chooser)
                    }
                } else {
                    val storyUrl = "https://othermbti-app-2026.surge.sh/test?target=${user.uid}"
                    val shareText = "🌸 [${user.nickname}] 님의 MBTI Gap 리포트!\n남이 본 내 MBTI: ${analytics.perceivedMbti}\n👉 나도 1분만에 평가해주기: $storyUrl"
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    val chooser = Intent.createChooser(sendIntent, "인스타그램 또는 공유 앱 선택")
                    context.startActivity(chooser)
                }
                showExportDialog = false
            }
        )
    }

    if (showShareBottomSheet) {
        ShareBottomSheetDialog(
            userNickname = user.nickname,
            targetUid = user.uid,
            onDismiss = { showShareBottomSheet = false },
            onShareSystemChooser = {
                val shareTitle = "🌸 [모두의 MBTI] ${user.nickname} 님의 MBTI 1분 평가 초댓장!"
                val shareDesc = "내가 아는 나 vs 친구들이 보는 나의 MBTI Gap!"
                val shareMessage = "$shareTitle\n$shareDesc\n\n👉 1분만에 평가하러 가기 🐣"



                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                }
                val chooser = Intent.createChooser(sendIntent, "초대 링크 공유")
                context.startActivity(chooser)
            },
            onCopyLink = {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("MBTI Test Link", "https://othermbti-app-2026.surge.sh/test?target=${user.uid}")
                clipboard.setPrimaryClip(clip)
                onShowToast("평가 링크가 클립보드에 복사되었습니다!")
            }
        )
    }

    if (showAdDialog) {
        AdRewardDialog(
            onDismiss = { showAdDialog = false },
            onAdCompleted = {
                showAdDialog = false
                pendingAction?.invoke()
                pendingAction = null
            }
        )
    }
}

fun generateStoryImageUri(context: android.content.Context, userNickname: String, selfMbti: String, perceivedMbti: String, targetUid: String): Uri? {
    try {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = android.graphics.Color.parseColor("#FFF5F8")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.color = android.graphics.Color.parseColor("#EDE9FE")
        val headerRect = RectF(80f, 100f, 530f, 160f)
        canvas.drawRoundRect(headerRect, 30f, 30f, paint)

        paint.color = android.graphics.Color.parseColor("#6D28D9")
        paint.textSize = 26f
        paint.isFakeBoldText = true
        canvas.drawText("🌸 MBTI GAP INSIGHT REPORT", 110f, 140f, paint)

        paint.color = android.graphics.Color.parseColor("#2E1065")
        paint.textSize = 48f
        paint.isFakeBoldText = true
        canvas.drawText("$userNickname 님의 MBTI 갭", 195f, 275f, paint)

        paint.color = android.graphics.Color.parseColor("#FCE7F3")
        val selfBox = RectF(100f, 395f, 480f, 565f)
        canvas.drawRoundRect(selfBox, 28f, 28f, paint)

        paint.color = android.graphics.Color.parseColor("#DB2777")
        paint.textSize = 24f
        paint.isFakeBoldText = false
        canvas.drawText("내가 생각한 나 (Self)", 130f, 435f, paint)

        paint.textSize = 64f
        paint.isFakeBoldText = true
        canvas.drawText(selfMbti, 130f, 520f, paint)

        paint.color = android.graphics.Color.parseColor("#FEF08A")
        val friendBox = RectF(580f, 395f, 960f, 565f)
        canvas.drawRoundRect(friendBox, 28f, 28f, paint)

        paint.color = android.graphics.Color.parseColor("#854D0E")
        paint.textSize = 24f
        paint.isFakeBoldText = false
        canvas.drawText("지인이 본 나 (Friends)", 610f, 435f, paint)

        paint.textSize = 64f
        paint.isFakeBoldText = true
        canvas.drawText(perceivedMbti, 610f, 520f, paint)

        paint.color = android.graphics.Color.parseColor("#FEF08A")
        val ctaBox = RectF(60f, 1680f, 1020f, 1810f)
        canvas.drawRoundRect(ctaBox, 40f, 40f, paint)

        paint.color = android.graphics.Color.parseColor("#713F12")
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText("🌸 [모두의 MBTI] 1분만에 평가하러 가기 🐣", 120f, 1735f, paint)

        paint.textSize = 24f
        paint.color = android.graphics.Color.parseColor("#854D0E")
        paint.isFakeBoldText = false
        canvas.drawText("👉 링크 눌러 [$userNickname] 님의 MBTI 갭 평가 참여하기", 120f, 1780f, paint)


        val imagesFolder = File(context.cacheDir, "shared_images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "mbti_story_card.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()

        return FileProvider.getUriForFile(
            context,
            "com.example.othermbti.fileprovider",
            file
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

@Composable
fun ProfileSummaryCard(user: User, perceivedMbti: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "내가 생각한 MBTI: ${user.selfMbti}",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF08A),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFFACC15), Color(0xFFFACC15))))
                ) {
                    Text(
                        text = "지인이 본 MBTI: $perceivedMbti",
                        color = Color(0xFF854D0E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE7F3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = AccentPink)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "🎀 친구들에게 평가 요청하기",
                        color = Color(0xFF9D174D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "스폰서 광고 시청 후 카카오톡 전용 선택 팝업이 활성화됩니다!",
                        color = Color(0xFFBE185D),
                        fontSize = 12.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onShareKakao,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE500), contentColor = KakaoBrown),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("카톡 공유", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCopyLink,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9D174D))
                ) {
                    Text("링크 복사", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = label, color = TextMuted, fontSize = 11.sp)
                Text(text = value, color = TextMain, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun GapAnalyticsCard(userSelfMbti: String, analytics: com.example.othermbti.data.MbtiGapAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 MBTI 지표별 Gap 분석",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            DimensionBarRow(
                title = "외향 (E) vs 내향 (I)",
                selfChar = 'E',
                userSelfMbti = userSelfMbti,
                pctVal = analytics.pctE,
                labelLeft = "E ${analytics.pctE}%",
                labelRight = "I ${analytics.pctI}%"
            )

            DimensionBarRow(
                title = "감각 (S) vs 직관 (N)",
                selfChar = 'S',
                userSelfMbti = userSelfMbti,
                pctVal = analytics.pctS,
                labelLeft = "S ${analytics.pctS}%",
                labelRight = "N ${analytics.pctN}%"
            )

            DimensionBarRow(
                title = "사고 (T) vs 감정 (F)",
                selfChar = 'T',
                userSelfMbti = userSelfMbti,
                pctVal = analytics.pctT,
                labelLeft = "T ${analytics.pctT}%",
                labelRight = "F ${analytics.pctF}%"
            )

            DimensionBarRow(
                title = "판단 (J) vs 인식 (P)",
                selfChar = 'J',
                userSelfMbti = userSelfMbti,
                pctVal = analytics.pctJ,
                labelLeft = "J ${analytics.pctJ}%",
                labelRight = "P ${analytics.pctP}%"
            )
        }
    }
}

@Composable
fun DimensionBarRow(
    title: String,
    selfChar: Char,
    userSelfMbti: String,
    pctVal: Int,
    labelLeft: String,
    labelRight: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = "$labelLeft / $labelRight", color = PrimaryPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFFEDE9FE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pctVal / 100f)
                    .clip(RoundedCornerShape(7.dp))
                    .background(PrimaryPurple)
            )
        }
    }
}

@Composable
fun KeywordsCard(topKeywords: List<KeywordRank>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏷️ 지인들이 꼽은 내 대표 모습 TOP 3",
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (topKeywords.isEmpty()) {
                Text(text = "아직 수집된 키워드가 없습니다.", color = TextMuted, fontSize = 13.sp)
            } else {
                topKeywords.take(3).forEachIndexed { idx, item ->
                    val rankColor = when (idx) {
                        0 -> AccentPink
                        1 -> PrimaryPurple
                        else -> AccentCyan
                    }
                    val bgPastel = when (idx) {
                        0 -> Color(0xFFFCE7F3)
                        1 -> Color(0xFFEDE9FE)
                        else -> Color(0xFFE0F2FE)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgPastel)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}위  ${item.keyword}",
                            color = TextMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${item.count}회 지목",
                            color = rankColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MbtiChemistryCard(userSelfMbti: String, analytics: com.example.othermbti.data.MbtiGapAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💘 MBTI 케미 & 궁합 매칭 레이더",
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFCE7F3)
                ) {
                    Text(
                        text = "찰떡 콤비 분석",
                        color = AccentPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFCE7F3), Color(0xFFEDE9FE))))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "내 MBTI ($userSelfMbti) 의 최고의 짝꿍", color = TextMuted, fontSize = 12.sp)
                            Text(text = analytics.bestMatchGrade, color = Color(0xFF9D174D), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryPurple
                        ) {
                            Text(
                                text = "${analytics.bestMatchMbti} (${analytics.bestMatchScore}%)",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Text(
                        text = analytics.bestMatchDesc,
                        color = TextMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EvaluatorsListCard(userSelfMbti: String, evaluations: List<Evaluation>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "💬 실시간 지인 응답 목록 & 궁합",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            if (evaluations.isEmpty()) {
                Text(text = "아직 참여한 지인이 없습니다.", color = TextMuted, fontSize = 13.sp)
            } else {
                evaluations.reversed().forEach { ev ->
                    val chemistry = com.example.othermbti.data.calculateMbtiCompatibility(userSelfMbti, ev.resultMbti)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFAF5FF))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = ev.evaluatorName, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = chemistry.matchGrade, color = AccentPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryPurple.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${ev.resultMbti} (${chemistry.matchScore}%)",
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold,
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
        title = { Text("프로필 및 내 MBTI 설정", fontWeight = FontWeight.Bold, color = TextMain) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = nick,
                    onValueChange = { nick = it },
                    label = { Text("닉네임") },
                    singleLine = true
                )

                Text("내가 생각하는 내 MBTI", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextMain)

                val rows = mbtiList.chunked(4)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { item ->
                                val isSel = (item == selectedMbti)
                                Button(
                                    onClick = { selectedMbti = item },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSel) PrimaryPurple else Color(0xFFEDE9FE),
                                        contentColor = if (isSel) Color.White else TextMain
                                    ),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Text(item, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(nick, selectedMbti) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextMuted)
            }
        }
    )
}

@Composable
fun ExportStoryDialog(
    userNickname: String,
    perceivedMbti: String,
    targetUid: String,
    onDismiss: () -> Unit,
    onShareInstagram: () -> Unit
) {
    var selectedTemplate by remember { mutableStateOf("insta") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = AccentPink)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🌸 MBTI 갭 리포트 파스텔 공유", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextMain)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "원하는 파스텔 템플릿 크기를 선택하세요:",
                    color = TextMuted,
                    fontSize = 13.sp
                )

                // Option 1: 9:16 Insta Story
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTemplate = "insta" }
                        .border(
                            width = 2.dp,
                            color = if (selectedTemplate == "insta") PrimaryPurple else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedTemplate == "insta") Color(0xFFFCE7F3) else Color(0xFFFAF5FF)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (selectedTemplate == "insta") AccentPink else TextMuted)
                        Column {
                            Text("📱 9:16 파스텔 인스타 스토리 전용", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 14.sp)
                            Text("1080x1920 세로형 파스텔 템플릿 (강력 추천)", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }

                // Option 2: 3:4 Square Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTemplate = "square" }
                        .border(
                            width = 2.dp,
                            color = if (selectedTemplate == "square") PrimaryPurple else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedTemplate == "square") Color(0xFFFCE7F3) else Color(0xFFFAF5FF)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (selectedTemplate == "square") AccentPink else TextMuted)
                        Column {
                            Text("🖼️ 3:4 파스텔 피드 & 카톡 카드", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 14.sp)
                            Text("600x800 피드 게시 및 요약형 카드", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShareInstagram,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("📱 스토리에 공유하기", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = TextMuted)
            }
        }
    )
}

@Composable
fun ShareBottomSheetDialog(
    userNickname: String,
    targetUid: String,
    onDismiss: () -> Unit,
    onShareSystemChooser: () -> Unit,
    onCopyLink: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFCBD5E1))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = AccentPink)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🌸 [$userNickname] 님의 MBTI 초댓장 공유", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextMain)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("친구들에게 보낼 공유 옵션을 선택하세요:", color = TextMuted, fontSize = 13.sp)

                // Option 1: Message / System Chooser
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShareSystemChooser(); onDismiss() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE9FE))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 18.sp)
                        }
                        Column {
                            Text("💬 메시지 / 다른 공유 앱 선택", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 14.sp)
                            Text("SMS, 라인, 밴드 등 전송", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }

                // Option 2: Copy Link
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCopyLink(); onDismiss() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE7F3))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentPink),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📋", fontSize = 18.sp)
                        }
                        Column {
                            Text("📋 평가 링크 주소 복사", fontWeight = FontWeight.Bold, color = Color(0xFF9D174D), fontSize = 14.sp)
                            Text("클립보드에 초댓장 URL 저장", color = Color(0xFFBE185D), fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = TextMuted)
            }
        }
    )
}

// Google AdMob Safety Configuration
// IMPORTANT: Set IS_PRODUCTION_AD = false during testing/pre-release to avoid AdMob account ban due to invalid impressions.
const val IS_PRODUCTION_AD = false 

const val PROD_ADMOB_APP_ID = "ca-app-pub-5254974097452914~6238198724"
const val PROD_ADMOB_REWARDED_AD_UNIT_ID = "ca-app-pub-5254974097452914/7135247341"

// Google Official Safe Test Ad Unit ID for Rewarded Interstitial
const val TEST_ADMOB_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"

val ACTIVE_ADMOB_REWARDED_AD_UNIT_ID = if (IS_PRODUCTION_AD) PROD_ADMOB_REWARDED_AD_UNIT_ID else TEST_ADMOB_REWARDED_AD_UNIT_ID


@Composable
fun AdRewardDialog(
    onDismiss: () -> Unit,
    onAdCompleted: () -> Unit
) {

    var countdown by remember { mutableStateOf(3) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000L)
            countdown--
        }
        onAdCompleted()
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFEF08A)
                ) {
                    Text(
                        text = "🎬 스폰서 전면 광고 (3초)",
                        color = Color(0xFF713F12),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🎁", fontSize = 40.sp)
                    Text(
                        text = "MBTI 프리미엄 갭 분석 스폰서",
                        color = Color(0xFFFEF08A),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "스폰서 광고 시청 혜택으로 무료 갭 분석 리포트와 고화질 템플릿을 무제한 공유할 수 있습니다.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryPurple.copy(alpha = 0.3f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PrimaryPurple, AccentPink)))
                    ) {
                        Text(
                            text = "⏱️ 광고 시청 중... ${countdown}초 남음",
                            color = AccentPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}
