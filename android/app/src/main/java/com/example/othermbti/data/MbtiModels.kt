package com.example.othermbti.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class User(
    val uid: String = "USER_MOCK_101",
    val nickname: String = "김타인",
    val selfMbti: String = "INTJ",
    val profileImg: String = ""
)

data class Evaluation(
    val evaluationId: String = "",
    val targetUid: String = "",
    val evaluatorName: String = "",
    val scores: Map<String, Int> = emptyMap(),
    val resultMbti: String = "",
    val selectedKeywords: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class KeywordRank(
    val keyword: String,
    val count: Int
)

data class MbtiChemistry(
    val friendMbti: String,
    val matchScore: Int,
    val matchGrade: String,
    val matchDescription: String,
    val badgeBgColorHex: Long
)

data class MbtiGapAnalytics(
    val totalCount: Int,
    val perceivedMbti: String,
    val pctE: Int, val pctI: Int,
    val pctS: Int, val pctN: Int,
    val pctT: Int, val pctF: Int,
    val pctJ: Int, val pctP: Int,
    val topKeywords: List<KeywordRank>,
    val maxGapDimension: String,
    val bestMatchMbti: String = "ENFP",
    val bestMatchScore: Int = 98,
    val bestMatchGrade: String = "💖 천생연분 찰떡",
    val bestMatchDesc: String = "서로의 부족함을 메워주는 최상의 시너지 케미!"
)

fun calculateMbtiCompatibility(selfMbti: String, friendMbti: String): MbtiChemistry {
    if (selfMbti.isBlank() || friendMbti.isBlank()) {
        return MbtiChemistry(friendMbti, 50, "🌤️ 무난무난", "서로를 맞춰가는 관계입니다.", 0xFFEDE9FE)
    }

    val bestPairs = mapOf(
        "INTJ" to listOf("ENFP", "ENTP"),
        "INTP" to listOf("ENTJ", "ESTJ"),
        "ENTJ" to listOf("INTP", "INFP"),
        "ENTP" to listOf("INFJ", "INTJ"),
        "INFJ" to listOf("ENTP", "ENFP"),
        "INFP" to listOf("ENFJ", "ENTJ"),
        "ENFJ" to listOf("INFP", "ISFP"),
        "ENFP" to listOf("INTJ", "INFJ"),
        "ISTJ" to listOf("ESTP", "ESFP"),
        "ISFJ" to listOf("ESFP", "ESTP"),
        "ESTJ" to listOf("ISTP", "INTP"),
        "ESFJ" to listOf("ISFP", "ISTP"),
        "ISTP" to listOf("ESTJ", "ENTJ"),
        "ISFP" to listOf("ESFJ", "ENFJ"),
        "ESTP" to listOf("ISTJ", "ISFJ"),
        "ESFP" to listOf("ISFJ", "ISTJ")
    )

    if (bestPairs[selfMbti]?.contains(friendMbti) == true) {
        return MbtiChemistry(
            friendMbti = friendMbti,
            matchScore = 98,
            matchGrade = "💖 천생연분 찰떡",
            matchDescription = "서로의 부족함을 메워주는 최상의 시너지 케미!",
            badgeBgColorHex = 0xFFFCE7F3
        )
    }

    var matchCount = 0
    for (i in 0 until 4) {
        if (i < selfMbti.length && i < friendMbti.length && selfMbti[i] == friendMbti[i]) {
            matchCount++
        }
    }

    return when (matchCount) {
        4 -> MbtiChemistry(friendMbti, 92, "💖 도플갱어 케미", "말하지 않아도 통하는 내 분신 같은 사이!", 0xFFFCE7F3)
        3 -> MbtiChemistry(friendMbti, 85, "🤝 겉차속따 든든", "비슷함 속에서도 신선한 자극을 주는 안정적인 케미!", 0xFFEDE9FE)
        2 -> MbtiChemistry(friendMbti, 70, "🌤️ 무난무난 티키타카", "서로의 차이를 인정할 때 더 재미있는 케미!", 0xFFE0F2FE)
        1 -> MbtiChemistry(friendMbti, 58, "⚡ 색다른 자극 콤비", "생각하는 방식은 달라도 호기심을 유발하는 관계!", 0xFFFEF08A)
        else -> MbtiChemistry(friendMbti, 45, "⚡ 삐걱 톰과제리", "투닥투닥거리면서 정드는 톰과 제리 케미!", 0xFFFEE2E2)
    }
}

class MbtiRepository(context: android.content.Context? = null) {
    private val prefs = context?.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)

    private val _user = MutableStateFlow(
        User(
            nickname = prefs?.getString("nickname", "나") ?: "나",
            selfMbti = prefs?.getString("selfMbti", "INTJ") ?: "INTJ"
        )
    )
    val user: StateFlow<User> = _user.asStateFlow()

    private val _evaluations = MutableStateFlow<List<Evaluation>>(emptyList())
    val evaluations: StateFlow<List<Evaluation>> = _evaluations.asStateFlow()

    private var db: FirebaseFirestore? = null

    init {
        seedInitialDemoData()
        initFirestoreLiveSync()
    }


    private fun initFirestoreLiveSync() {
        try {
            db = FirebaseFirestore.getInstance()
            db?.collection("evaluations")
                ?.whereEqualTo("target_uid", _user.value.uid)
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val liveList = mutableListOf<Evaluation>()
                    for (doc in snapshot.documents) {
                        val evalId = doc.getString("evaluation_id") ?: doc.id
                        val targetUid = doc.getString("target_uid") ?: ""
                        val name = doc.getString("evaluator_name") ?: "익명"
                        val resMbti = doc.getString("result_mbti") ?: ""
                        val keywords = (doc.get("selected_keywords") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        val created = doc.getLong("created_at") ?: System.currentTimeMillis()
                        
                        val rawScores = doc.get("scores") as? Map<*, *>
                        val scores = mutableMapOf<String, Int>()
                        rawScores?.forEach { (k, v) ->
                            if (k is String && v is Number) {
                                scores[k] = v.toInt()
                            }
                        }

                        liveList.add(
                            Evaluation(
                                evaluationId = evalId,
                                targetUid = targetUid,
                                evaluatorName = name,
                                scores = scores,
                                resultMbti = resMbti,
                                selectedKeywords = keywords,
                                createdAt = created
                            )
                        )
                    }
                    if (liveList.isNotEmpty()) {
                        _evaluations.value = liveList
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun seedInitialDemoData() {
        val now = System.currentTimeMillis()
        _evaluations.value = listOf(
            Evaluation(
                evaluationId = "EVAL_1",
                targetUid = _user.value.uid,
                evaluatorName = "김철수 (고교동창)",
                scores = mapOf("E" to 1, "I" to 2, "S" to 1, "N" to 2, "T" to 0, "F" to 3, "J" to 2, "P" to 1),
                resultMbti = "INFJ",
                selectedKeywords = listOf("공감 능력 대폭발", "약속 시각 칼준수", "평화주의 힐러"),
                createdAt = now - 86400000 * 2
            ),
            Evaluation(
                evaluationId = "EVAL_2",
                targetUid = _user.value.uid,
                evaluatorName = "이영희 (직장동료)",
                scores = mapOf("E" to 3, "I" to 0, "S" to 2, "N" to 1, "T" to 1, "F" to 2, "J" to 3, "P" to 0),
                resultMbti = "ESFJ",
                selectedKeywords = listOf("술자리 핵인싸", "일할 땐 차가움", "계획표 수집가"),
                createdAt = now - 86400000
            ),
            Evaluation(
                evaluationId = "EVAL_3",
                targetUid = _user.value.uid,
                evaluatorName = "박민수 (대학동아리)",
                scores = mapOf("E" to 2, "I" to 1, "S" to 0, "N" to 3, "T" to 0, "F" to 3, "J" to 1, "P" to 2),
                resultMbti = "ENFP",
                selectedKeywords = listOf("술자리 핵인싸", "아이디어 뇌절왕", "즉흥 번개 러버"),
                createdAt = now - 3600000 * 5
            ),
            Evaluation(
                evaluationId = "EVAL_4",
                targetUid = _user.value.uid,
                evaluatorName = "최지은",
                scores = mapOf("E" to 2, "I" to 1, "S" to 1, "N" to 2, "T" to 2, "F" to 1, "J" to 2, "P" to 1),
                resultMbti = "ENTJ",
                selectedKeywords = listOf("일할 땐 차가움", "약속 시각 칼준수", "솔직 팩폭 폭격기"),
                createdAt = now - 3600000 * 2
            ),
            Evaluation(
                evaluationId = "EVAL_5",
                targetUid = _user.value.uid,
                evaluatorName = "익명 친구",
                scores = mapOf("E" to 3, "I" to 0, "S" to 0, "N" to 3, "T" to 0, "F" to 3, "J" to 2, "P" to 1),
                resultMbti = "ENFJ",
                selectedKeywords = listOf("술자리 핵인싸", "공감 능력 대폭발", "칼답 리액션 부자"),
                createdAt = now - 1800000
            )
        )
    }

    fun updateUser(newNickname: String, newSelfMbti: String) {
        _user.value = _user.value.copy(nickname = newNickname, selfMbti = newSelfMbti)
        prefs?.edit()?.putString("nickname", newNickname)?.putString("selfMbti", newSelfMbti)?.apply()
    }


    fun addEvaluation(evaluation: Evaluation) {
        _evaluations.value = _evaluations.value + evaluation
    }

    fun calculateAnalytics(): MbtiGapAnalytics {
        val evals = _evaluations.value
        if (evals.isEmpty()) {
            return MbtiGapAnalytics(
                totalCount = 0,
                perceivedMbti = "미집계",
                pctE = 50, pctI = 50, pctS = 50, pctN = 50,
                pctT = 50, pctF = 50, pctJ = 50, pctP = 50,
                topKeywords = emptyList(),
                maxGapDimension = "데이터 없음"
            )
        }

        var sumE = 0; var sumI = 0
        var sumS = 0; var sumN = 0
        var sumT = 0; var sumF = 0
        var sumJ = 0; var sumP = 0
        val keywordMap = mutableMapOf<String, Int>()

        evals.forEach { ev ->
            sumE += ev.scores["E"] ?: 0
            sumI += ev.scores["I"] ?: 0
            sumS += ev.scores["S"] ?: 0
            sumN += ev.scores["N"] ?: 0
            sumT += ev.scores["T"] ?: 0
            sumF += ev.scores["F"] ?: 0
            sumJ += ev.scores["J"] ?: 0
            sumP += ev.scores["P"] ?: 0

            ev.selectedKeywords.forEach { kw ->
                keywordMap[kw] = (keywordMap[kw] ?: 0) + 1
            }
        }

        val totalEI = (sumE + sumI).coerceAtLeast(1)
        val totalSN = (sumS + sumN).coerceAtLeast(1)
        val totalTF = (sumT + sumF).coerceAtLeast(1)
        val totalJP = (sumJ + sumP).coerceAtLeast(1)

        val pctE = (sumE * 100) / totalEI
        val pctI = 100 - pctE
        val pctS = (sumS * 100) / totalSN
        val pctN = 100 - pctS
        val pctT = (sumT * 100) / totalTF
        val pctF = 100 - pctT
        val pctJ = (sumJ * 100) / totalJP
        val pctP = 100 - pctJ

        val perceivedMbti = buildString {
            append(if (pctE >= pctI) "E" else "I")
            append(if (pctS >= pctN) "S" else "N")
            append(if (pctT >= pctF) "T" else "F")
            append(if (pctJ >= pctP) "J" else "P")
        }

        val topKeywords = keywordMap.entries
            .sortedByDescending { it.value }
            .map { KeywordRank(it.key, it.value) }

        val self = _user.value.selfMbti
        val selfIsE = self.contains("E")
        val selfIsS = self.contains("S")
        val selfIsT = self.contains("T")
        val selfIsJ = self.contains("J")

        val gapEI = if (selfIsE) abs(100 - pctE) else pctE
        val gapSN = if (selfIsS) abs(100 - pctS) else pctS
        val gapTF = if (selfIsT) abs(100 - pctT) else pctT
        val gapJP = if (selfIsJ) abs(100 - pctJ) else pctJ

        val gaps = listOf(
            "E vs I" to gapEI,
            "S vs N" to gapSN,
            "T vs F" to gapTF,
            "J vs P" to gapJP
        ).sortedByDescending { it.second }

        val maxGapStr = "${gaps[0].first} (${gaps[0].second}%)"

        val bestChem = calculateMbtiCompatibility(self, "ENFP")

        return MbtiGapAnalytics(
            totalCount = evals.size,
            perceivedMbti = perceivedMbti,
            pctE = pctE, pctI = pctI,
            pctS = pctS, pctN = pctN,
            pctT = pctT, pctF = pctF,
            pctJ = pctJ, pctP = pctP,
            topKeywords = topKeywords,
            maxGapDimension = maxGapStr,
            bestMatchMbti = bestChem.friendMbti,
            bestMatchScore = bestChem.matchScore,
            bestMatchGrade = bestChem.matchGrade,
            bestMatchDesc = bestChem.matchDescription
        )
    }
}
