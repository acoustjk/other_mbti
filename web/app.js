/**
 * 타인이 보는 나의 MBTI (Others' View of My MBTI)
 * Core Application Engine & Firebase Firestore Service Integration
 */

// --------------------------------------------------------------------------
// 1. Data Schema & Firebase State Management
// --------------------------------------------------------------------------
const STORAGE_KEY_USER = 'mbti_user_profile';
const STORAGE_KEY_EVALS = 'mbti_evaluations';

const MBTI_TYPES = [
  'INTJ', 'INTP', 'ENTJ', 'ENTP',
  'INFJ', 'INFP', 'ENFJ', 'ENFP',
  'ISTJ', 'ISFJ', 'ESTJ', 'ESFJ',
  'ISTP', 'ISFP', 'ESTP', 'ESFP'
];

const SIGNATURE_KEYWORDS = [
  '술자리 핵인싸', '일할 땐 차가움', '공감 능력 대폭발', '카톡 읽씹 장인',
  '약속 시각 칼준수', '아이디어 뇌절왕', '솔직 팩폭 폭격기', '칼답 리액션 부자',
  '평화주의 힐러', '즉흥 번개 러버', '계획표 수집가', '집돌이/집순이 결사대'
];

const QUESTIONS = [
  {
    id: 1,
    title: '1. 약속 자리에 나온 이 친구의 평소 행동 스타일은?',
    optA: { text: '여러 명 모인 자리에서도 분위기를 주도하며 먼저 말을 건넨다', trait: 'E' },
    optB: { text: '소수 대화나 1:1 자리에서 조용히 경청하며 깊게 대화한다', trait: 'I' }
  },
  {
    id: 2,
    title: '2. 주말이나 휴일에 에너지를 충전하는 이 친구의 모습은?',
    optA: { text: '밖 나가서 사람들을 만나거나 핫플을 돌아다녀야 힐링됨', trait: 'E' },
    optB: { text: '집에서 누워서 OTT 보거나 혼자만의 시간을 보낼 때 힐링됨', trait: 'I' }
  },
  {
    id: 3,
    title: '3. 대화 중 상상력이나 가상 대화를 던질 때 이 친구는?',
    optA: { text: '현실적이고 구체적인 최신 이슈나 실제 경험담 위주로 말한다', trait: 'S' },
    optB: { text: '“만약에 우주선이 추락하면?” 같은 엉뚱하고 가상적인 생각 연쇄', trait: 'N' }
  },
  {
    id: 4,
    title: '4. 여행 코스를 계획할 때 이 친구의 가치관은?',
    optA: { text: '검증된 맛집 후기, 실제 동선, 효율적인 가성비 꼼꼼 확인', trait: 'S' },
    optB: { text: '감성적인 뷰, 신선한 자극, 독창적인 영감을 주는 곳 위주', trait: 'N' }
  },
  {
    id: 5,
    title: '5. 내가 우울하거나 서운하다고 털어놓았을 때 이 친구의 첫 마디는?',
    optA: { text: '“어쩌다 그렇게 됐어? 무슨 일인데?” 상황 원인 파악 및 해결책 조언', trait: 'T' },
    optB: { text: '“헐 너무 힘들었겠다 ㅠㅠ 괜찮아?” 내 기분부터 200% 감정 공감', trait: 'F' }
  },
  {
    id: 6,
    title: '6. 갈등이나 논쟁이 일어났을 때 이 친구의 판단 기준은?',
    optA: { text: '객관적인 팩트와 논리적 시시비비가 가장 중요하다', trait: 'T' },
    optB: { text: '서로 상처받지 않는 관계와 마음의 안정이 가장 중요하다', trait: 'F' }
  },
  {
    id: 7,
    title: '7. 약속 시간 30분 전, 이 친구의 대기 상태는?',
    optA: { text: '이미 준비 다 마치고 이동 경로/소요 시간까지 계산해둠', trait: 'J' },
    optB: { text: '약속 시간 다 되어 부랴부랴 씻거나 “지금 출발!” 외치는 편', trait: 'P' }
  },
  {
    id: 8,
    title: '8. 갑작스러운 당일 ‘번개 모임’을 제안했을 때 반응은?',
    optA: { text: '“갑자기? 내 오늘 루틴 깨지는데...” 당황하거나 정중히 거절', trait: 'J' },
    optB: { text: '“개꿀! 재밌겠다 지금 바로 튀어감” 즉흥성 폭발', trait: 'P' }
  },
  {
    id: 9,
    title: '9. 단톡방에서 이 친구의 텍스트 리액션 스타일은?',
    optA: { text: '화려한 짤방과 ㅋㅋㅋㅋ 연발, 읽자마자 폭풍 칼답', trait: 'E' },
    optB: { text: '필요한 핵심만 명확히 답하거나 나중에 조용히 읽고 대답', trait: 'I' }
  },
  {
    id: 10,
    title: '10. 이 친구와 일이나 과제를 함께할 때 느낀 점은?',
    optA: { text: '규칙과 계획표를 세우고 철저히 지키며 추진함', trait: 'J' },
    optB: { text: '유연하게 영감이 떠오를 때 융통성 있게 처리함', trait: 'P' }
  }
];

// App State & Firebase References
let db = null;
let currentCanvasTemplate = 'insta'; // 'insta' (1080x1920) or 'square' (600x800)
let state = {
  user: {
    uid: 'USER_MOCK_101',
    nickname: '김타인',
    self_mbti: 'INTJ',
    profile_img: '',
    created_at: Date.now()
  },
  evaluations: [],
  survey: {
    currentStep: 0,
    answers: {},
    selectedKeywords: [],
    evaluatorName: '',
    isAnonymous: false
  }
};

// --------------------------------------------------------------------------
// 2. Initialization & Firebase Engine Setup
// --------------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
  initFirebase();
  loadStateFromStorage();
  if (state.evaluations.length === 0) {
    seedDemoEvaluations();
  }
  initUI();
  setupCanvasTemplateEvents();
  
  // Auto-switch to Friend Survey View if target URL parameter exists
  const urlParams = new URLSearchParams(window.location.search);
  const targetParam = urlParams.get('target');
  if (targetParam) {
    document.getElementById('btnWebMode').click();
  } else {
    renderAppView();
  }
});

function initFirebase() {
  const badgeText = document.getElementById('firebaseStatusText');
  const badgeEl = document.getElementById('firebaseStatusBadge');

  if (window.FirebaseService && window.FirebaseService.isConfigured()) {
    try {
      if (!firebase.apps.length) {
        firebase.initializeApp(window.FirebaseService.config);
      }
      db = firebase.firestore();
      
      badgeText.innerHTML = '<i class="fa-solid fa-cloud-check"></i> Firestore 라이브 연동됨';
      badgeEl.classList.add('active');

      setupFirestoreRealtimeListener();
      console.log('Firebase Firestore successfully initialized!');
    } catch(err) {
      console.warn('Firebase initialization warning:', err);
      badgeText.textContent = 'Firebase 설정 오류 (Mock 모드 전환)';
    }
  } else {
    badgeText.textContent = 'Firestore 규격 호환 (데모/Mock 모드)';
  }
}

function setupFirestoreRealtimeListener() {
  if (!db) return;
  db.collection(window.FirebaseService.collections.EVALUATIONS)
    .where('target_uid', '==', state.user.uid)
    .onSnapshot((snapshot) => {
      let liveEvals = [];
      snapshot.forEach(doc => {
        liveEvals.push({ evaluation_id: doc.id, ...doc.data() });
      });
      if (liveEvals.length > 0) {
        state.evaluations = liveEvals;
        renderAppView();
      }
    }, (error) => {
      console.error('Firestore Realtime error:', error);
      showToast('Firestore 권한 확인 필요 (Rules 설정)');
    });
}

function loadStateFromStorage() {
  const savedUser = localStorage.getItem(STORAGE_KEY_USER);
  if (savedUser) {
    try { state.user = JSON.parse(savedUser); } catch(e) {}
  }
  const savedEvals = localStorage.getItem(STORAGE_KEY_EVALS);
  if (savedEvals) {
    try { state.evaluations = JSON.parse(savedEvals); } catch(e) {}
  }
}

function saveStateToStorage() {
  localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(state.user));
  localStorage.setItem(STORAGE_KEY_EVALS, JSON.stringify(state.evaluations));

  if (db) {
    db.collection(window.FirebaseService.collections.USERS)
      .doc(state.user.uid)
      .set(state.user, { merge: true })
      .catch(e => console.error('Firestore user save error:', e));
  }
}

function seedDemoEvaluations() {
  const demoData = [
    {
      evaluation_id: 'EVAL_1',
      target_uid: state.user.uid,
      evaluator_name: '김철수 (고교동창)',
      scores: { E: 1, I: 2, S: 1, N: 2, T: 0, F: 3, J: 2, P: 1 },
      result_mbti: 'INFJ',
      selected_keywords: ['공감 능력 대폭발', '약속 시각 칼준수', '평화주의 힐러'],
      created_at: Date.now() - 86400000 * 2
    },
    {
      evaluation_id: 'EVAL_2',
      target_uid: state.user.uid,
      evaluator_name: '이영희 (직장동료)',
      scores: { E: 3, I: 0, S: 2, N: 1, T: 1, F: 2, J: 3, P: 0 },
      result_mbti: 'ESFJ',
      selected_keywords: ['술자리 핵인싸', '일할 땐 차가움', '계획표 수집가'],
      created_at: Date.now() - 86400000
    },
    {
      evaluation_id: 'EVAL_3',
      target_uid: state.user.uid,
      evaluator_name: '박민수 (대학동아리)',
      scores: { E: 2, I: 1, S: 0, N: 3, T: 0, F: 3, J: 1, P: 2 },
      result_mbti: 'ENFP',
      selected_keywords: ['술자리 핵인싸', '아이디어 뇌절왕', '즉흥 번개 러버'],
      created_at: Date.now() - 3600000 * 5
    },
    {
      evaluation_id: 'EVAL_4',
      target_uid: state.user.uid,
      evaluator_name: '최지은',
      scores: { E: 2, I: 1, S: 1, N: 2, T: 2, F: 1, J: 2, P: 1 },
      result_mbti: 'ENTJ',
      selected_keywords: ['일할 땐 차가움', '약속 시각 칼준수', '솔직 팩폭 폭격기'],
      created_at: Date.now() - 3600000 * 2
    },
    {
      evaluation_id: 'EVAL_5',
      target_uid: state.user.uid,
      evaluator_name: '익명 친구',
      scores: { E: 3, I: 0, S: 0, N: 3, T: 0, F: 3, J: 2, P: 1 },
      result_mbti: 'ENFJ',
      selected_keywords: ['술자리 핵인싸', '공감 능력 대폭발', '칼답 리액션 부자'],
      created_at: Date.now() - 1800000
    }
  ];
  state.evaluations = demoData;
  saveStateToStorage();
}

// --------------------------------------------------------------------------
// 3. UI Navigation & Event Listeners
// --------------------------------------------------------------------------
function initUI() {
  const btnAppMode = document.getElementById('btnAppMode');
  const btnWebMode = document.getElementById('btnWebMode');
  const appViewSection = document.getElementById('appViewSection');
  const webViewSection = document.getElementById('webViewSection');

  btnAppMode.addEventListener('click', () => {
    btnAppMode.classList.add('active');
    btnWebMode.classList.remove('active');
    appViewSection.classList.add('active');
    webViewSection.classList.remove('active');
    renderAppView();
  });

  btnWebMode.addEventListener('click', () => {
    btnWebMode.classList.add('active');
    btnAppMode.classList.remove('active');
    webViewSection.classList.add('active');
    appViewSection.classList.remove('active');
    startSurvey();
  });

  // Reset Demo
  document.getElementById('btnResetDemo').addEventListener('click', () => {
    seedDemoEvaluations();
    renderAppView();
    showToast('데모 평가 데이터 5건이 초기화되었습니다!');
  });

  // Share Actions with Rewarded Interstitial Ad Integration
  document.getElementById('btnKakaoShare').addEventListener('click', () => {
    showRewardedAdThenExecute(() => {
      sendKakaoRichFeedCard(state.user.nickname, state.user.uid);
    });
  });

  document.getElementById('btnCloseKakaoModal').addEventListener('click', () => {
    document.getElementById('modalKakaoShare').classList.add('hidden');
  });

  const btnSendKakaoFeed = document.getElementById('btnSendKakaoFeed');
  if (btnSendKakaoFeed) {
    btnSendKakaoFeed.addEventListener('click', () => {
      sendKakaoRichFeedCard(state.user.nickname, state.user.uid);
    });
  }

  document.getElementById('btnSimulateFriendClick').addEventListener('click', () => {
    document.getElementById('modalKakaoShare').classList.add('hidden');
    btnWebMode.click();
  });

  document.getElementById('btnCopyLink').addEventListener('click', () => {
    showRewardedAdThenExecute(() => {
      document.getElementById('modalKakaoShare').classList.remove('hidden');
    });
  });


  // Profile Edit Modal
  const modalProfile = document.getElementById('modalProfile');
  document.getElementById('btnEditProfile').addEventListener('click', () => {
    document.getElementById('editNickname').value = state.user.nickname;
    renderMbtiGrid();
    modalProfile.classList.remove('hidden');
  });

  document.getElementById('btnCloseProfileModal').addEventListener('click', () => {
    modalProfile.classList.add('hidden');
  });

  document.getElementById('btnSaveProfile').addEventListener('click', () => {
    const nick = document.getElementById('editNickname').value.trim();
    if (nick) state.user.nickname = nick;
    saveStateToStorage();
    modalProfile.classList.add('hidden');
    renderAppView();
    showToast('프로필 및 내 MBTI가 저장되었습니다.');
  });

  // Export Image Card Modal
  const modalExport = document.getElementById('modalExportCard');
  document.getElementById('btnExportCard').addEventListener('click', () => {
    generateExportCanvas();
    modalExport.classList.remove('hidden');
  });

  document.getElementById('btnCloseExportModal').addEventListener('click', () => {
    modalExport.classList.add('hidden');
  });

  document.getElementById('btnDownloadImage').addEventListener('click', () => {
    const canvas = document.getElementById('cardCanvas');
    const link = document.createElement('a');
    const tplName = currentCanvasTemplate === 'insta' ? 'Instagram_Story' : 'Card_Report';
    link.download = `${state.user.nickname}_MBTI_Gap_${tplName}.png`;
    link.href = canvas.toDataURL('image/png');
    link.click();
    showToast('고화질 요약 카드가 이미지로 저장되었습니다!');
  });

  // Viral Loop CTA
  document.getElementById('btnViralCreate').addEventListener('click', () => {
    document.getElementById('btnAppMode').click();
    showToast('나만의 MBTI 평가 대시보드로 이동했습니다!');
  });

  // Survey controls
  document.getElementById('btnPrevQuestion').addEventListener('click', handleSurveyPrev);
  document.getElementById('btnNextQuestion').addEventListener('click', handleSurveyNext);
  document.getElementById('btnSubmitSurvey').addEventListener('click', handleSurveySubmit);
}

function setupCanvasTemplateEvents() {
  const btnInsta = document.getElementById('btnTplInsta');
  const btnSquare = document.getElementById('btnTplSquare');
  if (!btnInsta || !btnSquare) return;

  btnInsta.addEventListener('click', () => {
    currentCanvasTemplate = 'insta';
    btnInsta.classList.add('active');
    btnSquare.classList.remove('active');
    generateExportCanvas();
  });

  btnSquare.addEventListener('click', () => {
    currentCanvasTemplate = 'square';
    btnSquare.classList.add('active');
    btnInsta.classList.remove('active');
    generateExportCanvas();
  });
}

// --------------------------------------------------------------------------
// 4. Analytics Engine (MBTI Calculation & Gap Scoring)
// --------------------------------------------------------------------------
function calculateAggregatedMbti() {
  if (state.evaluations.length === 0) {
    return {
      total: 0,
      perceivedMbti: '미집계',
      pcts: { pctE: 50, pctI: 50, pctS: 50, pctN: 50, pctT: 50, pctF: 50, pctJ: 50, pctP: 50 },
      topKeywords: [],
      maxGapDimension: '데이터 없음'
    };
  }

  let totalE = 0, totalI = 0;
  let totalS = 0, totalN = 0;
  let totalT = 0, totalF = 0;
  let totalJ = 0, totalP = 0;
  let keywordCounts = {};

  state.evaluations.forEach(ev => {
    totalE += (ev.scores.E || 0);
    totalI += (ev.scores.I || 0);
    totalS += (ev.scores.S || 0);
    totalN += (ev.scores.N || 0);
    totalT += (ev.scores.T || 0);
    totalF += (ev.scores.F || 0);
    totalJ += (ev.scores.J || 0);
    totalP += (ev.scores.P || 0);

    if (Array.isArray(ev.selected_keywords)) {
      ev.selected_keywords.forEach(kw => {
        keywordCounts[kw] = (keywordCounts[kw] || 0) + 1;
      });
    }
  });

  const sumEI = totalE + totalI || 1;
  const sumSN = totalS + totalN || 1;
  const sumTF = totalT + totalF || 1;
  const sumJP = totalJ + totalP || 1;

  const pctE = Math.round((totalE / sumEI) * 100);
  const pctI = 100 - pctE;
  const pctS = Math.round((totalS / sumSN) * 100);
  const pctN = 100 - pctS;
  const pctT = Math.round((totalT / sumTF) * 100);
  const pctF = 100 - pctT;
  const pctJ = Math.round((totalJ / sumJP) * 100);
  const pctP = 100 - pctJ;

  const perceivedMbti =
    (pctE >= pctI ? 'E' : 'I') +
    (pctS >= pctN ? 'S' : 'N') +
    (pctT >= pctF ? 'T' : 'F') +
    (pctJ >= pctP ? 'J' : 'P');

  const sortedKeywords = Object.entries(keywordCounts)
    .sort((a, b) => b[1] - a[1])
    .map(([kw, count]) => ({ keyword: kw, count }));

  const self = state.user.self_mbti;
  const selfIsE = self.includes('E');
  const selfIsS = self.includes('S');
  const selfIsT = self.includes('T');
  const selfIsJ = self.includes('J');

  const gapEI = selfIsE ? Math.abs(100 - pctE) : pctE;
  const gapSN = selfIsS ? Math.abs(100 - pctS) : pctS;
  const gapTF = selfIsT ? Math.abs(100 - pctT) : pctT;
  const gapJP = selfIsJ ? Math.abs(100 - pctJ) : pctJ;

  const gapArray = [
    { dim: 'E vs I', val: gapEI },
    { dim: 'S vs N', val: gapSN },
    { dim: 'T vs F', val: gapTF },
    { dim: 'J vs P', val: gapJP }
  ];

  gapArray.sort((a, b) => b.val - a.val);

  return {
    total: state.evaluations.length,
    perceivedMbti,
    pcts: { pctE, pctI, pctS, pctN, pctT, pctF, pctJ, pctP },
    topKeywords: sortedKeywords,
    maxGapDimension: `${gapArray[0].dim} (${gapArray[0].val}%)`
  };
}

// --------------------------------------------------------------------------
// 5. Render App View (Dashboard)
// --------------------------------------------------------------------------
function renderAppView() {
  const analytics = calculateAggregatedMbti();

  document.getElementById('userNameDisplay').textContent = state.user.nickname;
  document.getElementById('userAvatar').textContent = state.user.nickname.charAt(0);
  document.getElementById('selfMbtiDisplay').textContent = state.user.self_mbti;
  document.getElementById('perceivedMbtiDisplay').textContent = analytics.perceivedMbti;
  document.getElementById('evaluatorCount').textContent = `${analytics.total}명`;
  document.getElementById('topGapDimension').textContent = analytics.maxGapDimension;
  document.getElementById('kakaoMsgTitle').textContent = `[${state.user.nickname}]이가 보는 내 MBTI는? 1분만에 평가해줘!`;

  const self = state.user.self_mbti;
  document.getElementById('markerEI').style.left = self.includes('E') ? '10%' : '90%';
  document.getElementById('markerSN').style.left = self.includes('S') ? '10%' : '90%';
  document.getElementById('markerTF').style.left = self.includes('T') ? '10%' : '90%';
  document.getElementById('markerJP').style.left = self.includes('J') ? '10%' : '90%';

  document.getElementById('barEI').style.width = `${analytics.pcts.pctE}%`;
  document.getElementById('labelEI').textContent = `지인 응답: E ${analytics.pcts.pctE}% / I ${analytics.pcts.pctI}%`;

  document.getElementById('barSN').style.width = `${analytics.pcts.pctS}%`;
  document.getElementById('labelSN').textContent = `지인 응답: S ${analytics.pcts.pctS}% / N ${analytics.pcts.pctN}%`;

  document.getElementById('barTF').style.width = `${analytics.pcts.pctT}%`;
  document.getElementById('labelTF').textContent = `지인 응답: T ${analytics.pcts.pctT}% / F ${analytics.pcts.pctF}%`;

  document.getElementById('barJP').style.width = `${analytics.pcts.pctJ}%`;
  document.getElementById('labelJP').textContent = `지인 응답: J ${analytics.pcts.pctJ}% / P ${analytics.pcts.pctP}%`;

  const kwContainer = document.getElementById('keywordsCloud');
  kwContainer.innerHTML = '';
  if (analytics.topKeywords.length === 0) {
    kwContainer.innerHTML = '<p class="text-muted">아직 수집된 키워드가 없습니다.</p>';
  } else {
    analytics.topKeywords.slice(0, 5).forEach((item, idx) => {
      const chip = document.createElement('div');
      chip.className = `keyword-chip rank-${idx + 1}`;
      chip.innerHTML = `<span class="rank">${idx + 1}위</span> ${item.keyword} (${item.count}회)`;
      kwContainer.appendChild(chip);
    });
  }

  // Chemistry Card
  const bestChem = calculateMbtiCompatibilityWeb(state.user.self_mbti, 'ENFP');
  const elemSelf = document.getElementById('chemSelfMbti');
  if (elemSelf) elemSelf.textContent = state.user.self_mbti;
  const elemGrade = document.getElementById('chemGrade');
  if (elemGrade) elemGrade.textContent = bestChem.grade;
  const elemTarget = document.getElementById('chemTargetMbti');
  if (elemTarget) elemTarget.textContent = `${bestChem.targetMbti || 'ENFP'} (${bestChem.score}%)`;
  const elemDesc = document.getElementById('chemDesc');
  if (elemDesc) elemDesc.textContent = bestChem.desc;

  const listContainer = document.getElementById('evaluatorsList');
  listContainer.innerHTML = '';
  if (state.evaluations.length === 0) {
    listContainer.innerHTML = '<p class="text-muted">아직 참여한 지인이 없습니다.</p>';
  } else {
    state.evaluations.slice().reverse().forEach(ev => {
      const chem = calculateMbtiCompatibilityWeb(state.user.self_mbti, ev.result_mbti);
      const item = document.createElement('div');
      item.className = 'evaluator-item';
      const timeStr = new Date(ev.created_at).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
      item.innerHTML = `
        <div style="display: flex; flex-direction: column;">
          <span class="evaluator-name"><i class="fa-solid fa-user-tag"></i> ${ev.evaluator_name} <small style="color:#94A3B8;">(${timeStr})</small></span>
          <small style="color: #EC4899; font-weight: 700; font-size: 0.75rem;">${chem.grade}</small>
        </div>
        <span class="evaluator-result-tag">${ev.result_mbti} (${chem.score}%)</span>
      `;
      listContainer.appendChild(item);
    });
  }
}

function calculateMbtiCompatibilityWeb(selfMbti, friendMbti) {
  if (!selfMbti || !friendMbti) {
    return { score: 50, grade: "🌤️ 무난무난", desc: "서로를 맞춰가는 관계입니다.", targetMbti: "ENFP" };
  }

  const bestPairs = {
    "INTJ": "ENFP", "INTP": "ENTJ", "ENTJ": "INTP", "ENTP": "INFJ",
    "INFJ": "ENTP", "INFP": "ENFJ", "ENFJ": "INFP", "ENFP": "INTJ",
    "ISTJ": "ESTP", "ISFJ": "ESFP", "ESTJ": "ISTP", "ESFJ": "ISFP",
    "ISTP": "ESTJ", "ISFP": "ESFJ", "ESTP": "ISTJ", "ESFP": "ISFJ"
  };

  const bestTarget = bestPairs[selfMbti] || "ENFP";

  if (bestTarget === friendMbti) {
    return { score: 98, grade: "💖 천생연분 찰떡", desc: "서로의 부족함을 메워주는 최상의 시너지 케미!", targetMbti: bestTarget };
  }

  let matchCount = 0;
  for (let i = 0; i < 4; i++) {
    if (selfMbti[i] === friendMbti[i]) matchCount++;
  }

  switch (matchCount) {
    case 4: return { score: 92, grade: "💖 도플갱어 케미", desc: "말하지 않아도 통하는 내 분신 같은 사이!", targetMbti: bestTarget };
    case 3: return { score: 85, grade: "🤝 겉차속따 든든", desc: "비슷함 속에서도 신선한 자극을 주는 안정적인 케미!", targetMbti: bestTarget };
    case 2: return { score: 70, grade: "🌤️ 무난무난 티키타카", desc: "서로의 차이를 인정할 때 더 재미있는 케미!", targetMbti: bestTarget };
    case 1: return { score: 58, grade: "⚡ 색다른 자극 콤비", desc: "생각하는 방식은 달라도 호기심을 유발하는 관계!", targetMbti: bestTarget };
    default: return { score: 45, grade: "⚡ 삐걱 톰과제리", desc: "투닥투닥거리면서 정드는 톰과 제리 케미!", targetMbti: bestTarget };
  }
}


function renderMbtiGrid() {
  const container = document.getElementById('mbtiGrid');
  container.innerHTML = '';
  MBTI_TYPES.forEach(type => {
    const btn = document.createElement('button');
    btn.className = `mbti-btn ${state.user.self_mbti === type ? 'selected' : ''}`;
    btn.textContent = type;
    btn.addEventListener('click', () => {
      state.user.self_mbti = type;
      document.querySelectorAll('.mbti-btn').forEach(b => b.classList.remove('selected'));
      btn.classList.add('selected');
    });
    container.appendChild(btn);
  });
}

// --------------------------------------------------------------------------
// 6. Friend Web Survey Logic & Firestore Writing
// --------------------------------------------------------------------------
function startSurvey() {
  state.survey = {
    currentStep: 0,
    answers: {},
    selectedKeywords: [],
    evaluatorName: '',
    isAnonymous: false
  };

  document.getElementById('surveyTargetName').textContent = state.user.nickname;
  document.getElementById('surveyTargetAvatar').textContent = state.user.nickname.charAt(0);
  document.getElementById('completeTargetName').textContent = state.user.nickname;

  document.getElementById('surveyCard').classList.remove('hidden');
  document.getElementById('completionCard').classList.add('hidden');

  renderSurveyStep();
}

function renderSurveyStep() {
  const step = state.survey.currentStep;

  const questionBox = document.getElementById('questionBox');
  const keywordsBox = document.getElementById('keywordsSelectBox');
  const infoBox = document.getElementById('evaluatorInfoBox');
  const navBtns = document.getElementById('surveyNavBtns');

  const btnPrev = document.getElementById('btnPrevQuestion');
  const btnNext = document.getElementById('btnNextQuestion');

  btnPrev.disabled = (step === 0);

  if (step < 10) {
    questionBox.classList.remove('hidden');
    keywordsBox.classList.add('hidden');
    infoBox.classList.add('hidden');
    navBtns.classList.remove('hidden');

    const q = QUESTIONS[step];
    document.getElementById('questionStepText').textContent = `문항 ${step + 1} / 10`;
    document.getElementById('progressPercent').textContent = `${(step + 1) * 9}%`;
    document.getElementById('progressFill').style.width = `${(step + 1) * 9}%`;
    document.getElementById('questionTitle').textContent = q.title;

    const optGroup = document.getElementById('optionsGroup');
    optGroup.innerHTML = '';

    const currentAns = state.survey.answers[q.id];

    const cardA = document.createElement('div');
    cardA.className = `option-card ${currentAns === q.optA.trait ? 'selected' : ''}`;
    cardA.innerHTML = `<span>${q.optA.text}</span><div class="option-radio"></div>`;
    cardA.addEventListener('click', () => {
      state.survey.answers[q.id] = q.optA.trait;
      renderSurveyStep();
    });

    const cardB = document.createElement('div');
    cardB.className = `option-card ${currentAns === q.optB.trait ? 'selected' : ''}`;
    cardB.innerHTML = `<span>${q.optB.text}</span><div class="option-radio"></div>`;
    cardB.addEventListener('click', () => {
      state.survey.answers[q.id] = q.optB.trait;
      renderSurveyStep();
    });

    optGroup.appendChild(cardA);
    optGroup.appendChild(cardB);

    btnNext.innerHTML = '다음 <i class="fa-solid fa-chevron-right"></i>';
  } else if (step === 10) {
    questionBox.classList.add('hidden');
    keywordsBox.classList.remove('hidden');
    infoBox.classList.add('hidden');
    navBtns.classList.remove('hidden');

    document.getElementById('questionStepText').textContent = '특징 키워드 선택';
    document.getElementById('progressPercent').textContent = '95%';
    document.getElementById('progressFill').style.width = '95%';

    renderKeywordsGrid();
    btnNext.innerHTML = '다음 (이름 입력) <i class="fa-solid fa-chevron-right"></i>';
  } else if (step === 11) {
    questionBox.classList.add('hidden');
    keywordsBox.classList.add('hidden');
    infoBox.classList.remove('hidden');
    navBtns.classList.add('hidden');

    document.getElementById('questionStepText').textContent = '평가 완료 단계';
    document.getElementById('progressPercent').textContent = '100%';
    document.getElementById('progressFill').style.width = '100%';
  }
}

function renderKeywordsGrid() {
  const container = document.getElementById('keywordOptionsGrid');
  container.innerHTML = '';
  SIGNATURE_KEYWORDS.forEach(kw => {
    const isSel = state.survey.selectedKeywords.includes(kw);
    const pill = document.createElement('div');
    pill.className = `kw-select-pill ${isSel ? 'selected' : ''}`;
    pill.textContent = kw;
    pill.addEventListener('click', () => {
      if (isSel) {
        state.survey.selectedKeywords = state.survey.selectedKeywords.filter(k => k !== kw);
      } else {
        if (state.survey.selectedKeywords.length >= 3) {
          showToast('키워드는 최대 3개까지 선택할 수 있습니다.');
          return;
        }
        state.survey.selectedKeywords.push(kw);
      }
      renderKeywordsGrid();
    });
    container.appendChild(pill);
  });
}

function handleSurveyPrev() {
  if (state.survey.currentStep > 0) {
    state.survey.currentStep--;
    renderSurveyStep();
  }
}

function handleSurveyNext() {
  const step = state.survey.currentStep;
  if (step < 10) {
    const qId = QUESTIONS[step].id;
    if (!state.survey.answers[qId]) {
      showToast('답변을 하나 선택해주세요!');
      return;
    }
  }
  if (step === 10 && state.survey.selectedKeywords.length === 0) {
    showToast('최소 1개 이상의 키워드를 선택해주세요!');
    return;
  }

  state.survey.currentStep++;
  renderSurveyStep();
}

function handleSurveySubmit() {
  const nameInput = document.getElementById('evaluatorNameInput').value.trim();
  const isAnon = document.getElementById('chkAnonymous').checked;

  let finalName = '익명';
  if (!isAnon && nameInput) {
    finalName = nameInput;
  } else if (!isAnon && !nameInput) {
    finalName = '익명 지인';
  }

  let scores = { E: 0, I: 0, S: 0, N: 0, T: 0, F: 0, J: 0, P: 0 };
  Object.values(state.survey.answers).forEach(trait => {
    if (scores[trait] !== undefined) {
      scores[trait]++;
    }
  });

  const resMbti =
    (scores.E >= scores.I ? 'E' : 'I') +
    (scores.S >= scores.N ? 'S' : 'N') +
    (scores.T >= scores.F ? 'T' : 'F') +
    (scores.J >= scores.P ? 'J' : 'P');

  // Extract target UID from URL parameter if present
  const urlParams = new URLSearchParams(window.location.search);
  const targetUidFromUrl = urlParams.get('target');

  const newEval = {
    evaluation_id: 'EVAL_' + Date.now(),
    target_uid: targetUidFromUrl || state.user.uid,
    evaluator_name: finalName,
    scores: scores,
    result_mbti: resMbti,
    selected_keywords: state.survey.selectedKeywords,
    created_at: Date.now()
  };

  state.evaluations.push(newEval);
  saveStateToStorage();

  // Write to Firestore DB
  if (db) {
    db.collection(window.FirebaseService.collections.EVALUATIONS)
      .add(newEval)
      .then(docRef => {
        console.log('Evaluation successfully written to Firestore ID:', docRef.id);
        showToast('Firestore DB에 평가 결과가 저장되었습니다!');
      })
      .catch(err => {
        console.error('Firestore write error:', err);
        showToast('Firestore 저장 오류! (Firebase Console -> Firestore Rules 확인 필요)');
      });
  }

  document.getElementById('surveyCard').classList.add('hidden');
  document.getElementById('completionCard').classList.remove('hidden');
}

// --------------------------------------------------------------------------
// 7. Canvas Image Card Generator (9:16 Instagram Story & 3:4 Feed Cards)
// --------------------------------------------------------------------------
function generateExportCanvas() {
  const canvas = document.getElementById('cardCanvas');
  const ctx = canvas.getContext('2d');
  const analytics = calculateAggregatedMbti();

  if (currentCanvasTemplate === 'insta') {
    canvas.width = 1080;
    canvas.height = 1920;
    draw916InstagramStoryCanvas(canvas, ctx, analytics);
  } else {
    canvas.width = 600;
    canvas.height = 800;
    draw34SquareCardCanvas(canvas, ctx, analytics);
  }
}

function draw916InstagramStoryCanvas(canvas, ctx, analytics) {
  // Background Pastel Soft Gradient
  const bgGrad = ctx.createLinearGradient(0, 0, 1080, 1920);
  bgGrad.addColorStop(0, '#FFF5F8');
  bgGrad.addColorStop(0.35, '#F3E8FF');
  bgGrad.addColorStop(0.7, '#FAF5FF');
  bgGrad.addColorStop(1, '#FFF9FA');
  ctx.fillStyle = bgGrad;
  ctx.fillRect(0, 0, 1080, 1920);

  // Soft Cute Ambient Glow Orbs
  const g1 = ctx.createRadialGradient(900, 250, 0, 900, 250, 500);
  g1.addColorStop(0, 'rgba(252, 231, 243, 0.7)');
  g1.addColorStop(1, 'transparent');
  ctx.fillStyle = g1;
  ctx.beginPath(); ctx.arc(900, 250, 500, 0, Math.PI * 2); ctx.fill();

  const g2 = ctx.createRadialGradient(180, 950, 0, 180, 950, 450);
  g2.addColorStop(0, 'rgba(221, 214, 254, 0.6)');
  g2.addColorStop(1, 'transparent');
  ctx.fillStyle = g2;
  ctx.beginPath(); ctx.arc(180, 950, 450, 0, Math.PI * 2); ctx.fill();

  const g3 = ctx.createRadialGradient(950, 1750, 0, 950, 1750, 400);
  g3.addColorStop(0, 'rgba(254, 240, 138, 0.6)');
  g3.addColorStop(1, 'transparent');
  ctx.fillStyle = g3;
  ctx.beginPath(); ctx.arc(950, 1750, 400, 0, Math.PI * 2); ctx.fill();

  // Top Category Pill & Header
  ctx.fillStyle = '#EDE9FE';
  ctx.strokeStyle = '#C4B5FD';
  ctx.lineWidth = 3;
  ctx.roundRect(80, 100, 450, 60, 30);
  ctx.fill(); ctx.stroke();

  ctx.fillStyle = '#6D28D9';
  ctx.font = 'bold 24px "Noto Sans KR", sans-serif';
  ctx.fillText('🌸 MBTI GAP INSIGHT REPORT', 115, 139);

  ctx.fillStyle = '#A78BFA';
  ctx.font = 'extrabold 22px "Outfit", sans-serif';
  ctx.fillText('INSTAGRAM STORY EDITION', 80, 198);

  // User Profile Summary Banner
  ctx.fillStyle = '#F472B6';
  ctx.beginPath();
  ctx.arc(130, 280, 46, 0, Math.PI * 2);
  ctx.fill();

  ctx.fillStyle = '#FFFFFF';
  ctx.font = 'bold 44px "Noto Sans KR", sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(state.user.nickname.charAt(0), 130, 296);
  ctx.textAlign = 'left';

  ctx.fillStyle = '#2E1065';
  ctx.font = 'extrabold 48px "Noto Sans KR", sans-serif';
  ctx.fillText(`${state.user.nickname} 님의 MBTI 갭`, 195, 275);

  ctx.fillStyle = '#64748B';
  ctx.font = '24px "Noto Sans KR", sans-serif';
  ctx.fillText(`👥 총 ${analytics.total}명의 지인이 솔직하게 응답함`, 195, 310);

  // Main MBTI Comparison Box (1000 x 260) - Soft White Glass Card
  ctx.fillStyle = 'rgba(255, 255, 255, 0.9)';
  ctx.strokeStyle = 'rgba(244, 114, 182, 0.3)';
  ctx.lineWidth = 3;
  ctx.roundRect(60, 360, 960, 240, 36);
  ctx.fill(); ctx.stroke();

  // Self MBTI Box (Pastel Pink)
  ctx.fillStyle = '#FCE7F3';
  ctx.strokeStyle = '#F472B6';
  ctx.lineWidth = 2;
  ctx.roundRect(100, 395, 380, 170, 28);
  ctx.fill(); ctx.stroke();

  ctx.fillStyle = '#DB2777';
  ctx.font = 'bold 24px "Noto Sans KR", sans-serif';
  ctx.fillText('내가 생각한 나 (Self)', 130, 435);

  ctx.fillStyle = '#9D174D';
  ctx.font = 'extrabold 64px "Outfit", sans-serif';
  ctx.fillText(state.user.self_mbti, 130, 520);

  // VS Divider
  ctx.fillStyle = '#A78BFA';
  ctx.font = 'black 32px "Outfit", sans-serif';
  ctx.fillText('VS', 515, 490);

  // Perceived MBTI Box (Pastel Yellow)
  ctx.fillStyle = '#FEF08A';
  ctx.strokeStyle = '#FACC15';
  ctx.lineWidth = 2;
  ctx.roundRect(580, 395, 380, 170, 28);
  ctx.fill(); ctx.stroke();

  ctx.fillStyle = '#854D0E';
  ctx.font = 'bold 24px "Noto Sans KR", sans-serif';
  ctx.fillText('지인이 본 나 (Friends)', 610, 435);

  ctx.fillStyle = '#713F12';
  ctx.font = 'extrabold 64px "Outfit", sans-serif';
  ctx.fillText(analytics.perceivedMbti, 610, 520);

  // Huge Gap Banner (Pastel Gradient)
  const gapGrad = ctx.createLinearGradient(60, 630, 1020, 630);
  gapGrad.addColorStop(0, '#A78BFA');
  gapGrad.addColorStop(1, '#F472B6');
  ctx.fillStyle = gapGrad;
  ctx.roundRect(60, 630, 960, 100, 28);
  ctx.fill();

  ctx.fillStyle = '#FFFFFF';
  ctx.font = 'black 32px "Noto Sans KR", sans-serif';
  ctx.fillText(`🔥 가장 큰 MBTI 차이 : ${analytics.maxGapDimension}`, 100, 692);

  // TOP 3 Signature Keywords Section
  ctx.fillStyle = '#2E1065';
  ctx.font = 'extrabold 32px "Noto Sans KR", sans-serif';
  ctx.fillText('🎀 지인들이 선택한 내 대표 모습 TOP 3', 60, 785);

  let kwY = 820;
  const rankColors = ['#F472B6', '#A78BFA', '#38BDF8'];
  const rankBgs = ['#FCE7F3', '#EDE9FE', '#E0F2FE'];
  const rankBadges = ['🥇 1위', '🥈 2위', '🥉 3위'];

  if (analytics.topKeywords.length === 0) {
    ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
    ctx.roundRect(60, kwY, 960, 90, 24);
    ctx.fill();
    ctx.fillStyle = '#64748B';
    ctx.font = '26px "Noto Sans KR", sans-serif';
    ctx.fillText('아직 수집된 키워드가 없습니다.', 100, kwY + 54);
  } else {
    analytics.topKeywords.slice(0, 3).forEach((item, idx) => {
      ctx.fillStyle = 'rgba(255, 255, 255, 0.9)';
      ctx.strokeStyle = rankColors[idx];
      ctx.lineWidth = 2;
      ctx.roundRect(60, kwY, 960, 96, 24);
      ctx.fill(); ctx.stroke();

      ctx.fillStyle = rankBgs[idx];
      ctx.roundRect(85, kwY + 18, 110, 60, 16);
      ctx.fill();

      ctx.fillStyle = rankColors[idx];
      ctx.font = 'extrabold 26px "Noto Sans KR", sans-serif';
      ctx.fillText(rankBadges[idx], 100, kwY + 58);

      ctx.fillStyle = '#1E1B4B';
      ctx.font = 'bold 30px "Noto Sans KR", sans-serif';
      ctx.fillText(item.keyword, 225, kwY + 58);

      ctx.fillStyle = '#64748B';
      ctx.font = '24px "Noto Sans KR", sans-serif';
      ctx.fillText(`${item.count}회 지목`, 830, kwY + 58);

      kwY += 114;
    });
  }

  // 4-Axis Percentage Gauges Section
  ctx.fillStyle = '#2E1065';
  ctx.font = 'extrabold 32px "Noto Sans KR", sans-serif';
  ctx.fillText('📊 MBTI 지표별 지인 응답 비율', 60, 1205);

  const dimensions = [
    { title: '외향 (E) vs 내향 (I)', perceived: `E ${analytics.pcts.pctE}% / I ${analytics.pcts.pctI}%`, val: analytics.pcts.pctE },
    { title: '감각 (S) vs 직관 (N)', perceived: `S ${analytics.pcts.pctS}% / N ${analytics.pcts.pctN}%`, val: analytics.pcts.pctS },
    { title: '사고 (T) vs 감정 (F)', perceived: `T ${analytics.pcts.pctT}% / F ${analytics.pcts.pctF}%`, val: analytics.pcts.pctT },
    { title: '판단 (J) vs 인식 (P)', perceived: `J ${analytics.pcts.pctJ}% / P ${analytics.pcts.pctP}%`, val: analytics.pcts.pctJ }
  ];

  let dimY = 1240;
  dimensions.forEach(d => {
    ctx.fillStyle = 'rgba(255, 255, 255, 0.85)';
    ctx.roundRect(60, dimY, 960, 88, 24);
    ctx.fill();

    ctx.fillStyle = '#1E1B4B';
    ctx.font = 'bold 24px "Noto Sans KR", sans-serif';
    ctx.fillText(d.title, 90, dimY + 38);

    ctx.fillStyle = '#7C3AED';
    ctx.font = 'extrabold 24px "Outfit", sans-serif';
    ctx.fillText(d.perceived, 700, dimY + 38);

    // Track
    ctx.fillStyle = '#F3E8FF';
    ctx.roundRect(90, dimY + 50, 880, 18, 9);
    ctx.fill();

    // Fill Bar
    ctx.fillStyle = '#A78BFA';
    const fillW = Math.max(20, Math.min(880, (d.val / 100) * 880));
    ctx.roundRect(90, dimY + 50, fillW, 18, 9);
    ctx.fill();

    dimY += 105;
  });

  // Bottom Instagram Story Sticker CTA (Pastel Butter Yellow Pill)
  ctx.fillStyle = '#FEF08A';
  ctx.strokeStyle = '#FACC15';
  ctx.lineWidth = 3;
  ctx.roundRect(60, 1680, 960, 130, 40);
  ctx.fill(); ctx.stroke();

  ctx.fillStyle = '#713F12';
  ctx.font = 'black 34px "Noto Sans KR", sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('🐣 "너도 나 어떤지 1분만에 평가해줘!"', 540, 1735);

  ctx.fillStyle = '#A16207';
  ctx.font = 'bold 24px "Outfit", sans-serif';
  ctx.fillText(`https://othermbti-app-2026.surge.sh/test?target=${state.user.uid}`, 540, 1780);
  ctx.textAlign = 'left';
}


  ctx.fillStyle = '#FEE500';
  ctx.font = 'bold 24px "Noto Sans KR", sans-serif';
  ctx.fillText('지인이 본 나 (Friends)', 610, 435);

  ctx.fillStyle = '#FFFFFF';
  ctx.font = 'extrabold 64px "Outfit", sans-serif';
  ctx.fillText(analytics.perceivedMbti, 610, 520);

  // Huge Gap Banner
  const gapGrad = ctx.createLinearGradient(60, 630, 1020, 630);
  gapGrad.addColorStop(0, '#8B5CF6');
  gapGrad.addColorStop(1, '#EC4899');
  ctx.fillStyle = gapGrad;
  ctx.roundRect(60, 630, 960, 100, 24);
  ctx.fill();

  ctx.fillStyle = '#FFFFFF';
  ctx.font = 'black 32px "Noto Sans KR", sans-serif';
  ctx.fillText(`🔥 가장 큰 MBTI 차이 : ${analytics.maxGapDimension}`, 100, 692);

  // TOP 3 Signature Keywords Section
  ctx.fillStyle = '#FEE500';
  ctx.font = 'extrabold 32px "Noto Sans KR", sans-serif';
  ctx.fillText('🏆 지인들이 선택한 내 대표 모습 TOP 3', 60, 785);

  let kwY = 820;
  const rankColors = ['#FEE500', '#E2E8F0', '#FDBA74'];
  const rankBadges = ['🥇 1위', '🥈 2위', '🥉 3위'];

  if (analytics.topKeywords.length === 0) {
    ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
    ctx.roundRect(60, kwY, 960, 90, 20);
    ctx.fill();
    ctx.fillStyle = '#94A3B8';
    ctx.font = '26px "Noto Sans KR", sans-serif';
    ctx.fillText('아직 수집된 키워드가 없습니다.', 100, kwY + 54);
  } else {
    analytics.topKeywords.slice(0, 3).forEach((item, idx) => {
      ctx.fillStyle = 'rgba(255, 255, 255, 0.07)';
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.15)';
      ctx.lineWidth = 1;
      ctx.roundRect(60, kwY, 960, 96, 20);
      ctx.fill(); ctx.stroke();

      ctx.fillStyle = rankColors[idx];
      ctx.font = 'extrabold 28px "Noto Sans KR", sans-serif';
      ctx.fillText(rankBadges[idx], 95, kwY + 58);

      ctx.fillStyle = '#FFFFFF';
      ctx.font = 'bold 30px "Noto Sans KR", sans-serif';
      ctx.fillText(item.keyword, 220, kwY + 58);

      ctx.fillStyle = '#94A3B8';
      ctx.font = '24px "Noto Sans KR", sans-serif';
      ctx.fillText(`${item.count}회 지인 지목`, 830, kwY + 58);

      kwY += 114;
    });
  }

  // 4-Axis Percentage Gauges Section
  ctx.fillStyle = '#FFFFFF';
  ctx.font = 'extrabold 32px "Noto Sans KR", sans-serif';
  ctx.fillText('📊 MBTI 지표별 지인 응답 비율', 60, 1205);

  const dimensions = [
    { title: '외향 (E) vs 내향 (I)', perceived: `E ${analytics.pcts.pctE}% / I ${analytics.pcts.pctI}%`, val: analytics.pcts.pctE },
    { title: '감각 (S) vs 직관 (N)', perceived: `S ${analytics.pcts.pctS}% / N ${analytics.pcts.pctN}%`, val: analytics.pcts.pctS },
    { title: '사고 (T) vs 감정 (F)', perceived: `T ${analytics.pcts.pctT}% / F ${analytics.pcts.pctF}%`, val: analytics.pcts.pctT },
    { title: '판단 (J) vs 인식 (P)', perceived: `J ${analytics.pcts.pctJ}% / P ${analytics.pcts.pctP}%`, val: analytics.pcts.pctJ }
  ];

  let dimY = 1240;
  dimensions.forEach(d => {
    ctx.fillStyle = 'rgba(15, 23, 42, 0.6)';
    ctx.roundRect(60, dimY, 960, 88, 20);
    ctx.fill();

    ctx.fillStyle = '#F1F5F9';
    ctx.font = 'bold 24px "Noto Sans KR", sans-serif';
    ctx.fillText(d.title, 90, dimY + 38);

    ctx.fillStyle = '#38BDF8';
    ctx.font = 'extrabold 24px "Outfit", sans-serif';
    ctx.fillText(d.perceived, 700, dimY + 38);

    // Track
    ctx.fillStyle = 'rgba(255, 255, 255, 0.12)';
    ctx.roundRect(90, dimY + 50, 880, 18, 9);
    ctx.fill();

    // Fill Bar
    ctx.fillStyle = '#8B5CF6';
    const fillW = Math.max(20, Math.min(880, (d.val / 100) * 880));
    ctx.roundRect(90, dimY + 50, fillW, 18, 9);
    ctx.fill();

    dimY += 105;
  });

  // Bottom Instagram Story Sticker CTA (Floating Pill)
  ctx.fillStyle = '#FEE500';
  ctx.roundRect(60, 1680, 960, 130, 36);
  ctx.fill();

  ctx.fillStyle = '#111827';
  ctx.font = 'black 34px "Noto Sans KR", sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('👉 "너도 나 어떤지 1분만에 평가해줘!"', 540, 1735);

  ctx.fillStyle = '#475569';
  ctx.font = 'bold 24px "Outfit", sans-serif';
  ctx.fillText(`https://othermbti-app-2026.surge.sh/test?target=${state.user.uid}`, 540, 1780);
  ctx.textAlign = 'left';
}

function draw34SquareCardCanvas(canvas, ctx, analytics) {
  const grad = ctx.createLinearGradient(0, 0, 600, 800);
  grad.addColorStop(0, '#0F172A');
  grad.addColorStop(0.5, '#1E1B4B');
  grad.addColorStop(1, '#0F172A');
  ctx.fillStyle = grad;
  ctx.fillRect(0, 0, 600, 800);

  ctx.fillStyle = 'rgba(139, 92, 246, 0.15)';
  ctx.beginPath();
  ctx.arc(500, 100, 150, 0, Math.PI * 2);
  ctx.fill();

  ctx.fillStyle = 'rgba(254, 229, 0, 0.1)';
  ctx.beginPath();
  ctx.arc(80, 700, 120, 0, Math.PI * 2);
  ctx.fill();

  ctx.fillStyle = '#FEE500';
  ctx.font = 'bold 22px "Noto Sans KR", sans-serif';
  ctx.fillText('타인이 보는 나의 MBTI Gap 리포트', 40, 50);

  ctx.fillStyle = '#94A3B8';
  ctx.font = '14px "Noto Sans KR", sans-serif';
  ctx.fillText(`대상: ${state.user.nickname}님 (총 ${analytics.total}명 참여)`, 40, 75);

  ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.2)';
  ctx.lineWidth = 1;
  ctx.roundRect(40, 100, 520, 120, 16);
  ctx.fill();
  ctx.stroke();

  ctx.fillStyle = '#FFFFFF';
  ctx.font = 'bold 18px "Noto Sans KR", sans-serif';
  ctx.fillText(`내가 생각한 내 MBTI:`, 65, 145);

  ctx.fillStyle = '#EC4899';
  ctx.font = 'extrabold 24px "Outfit", sans-serif';
  ctx.fillText(state.user.self_mbti, 270, 147);

  ctx.fillStyle = '#FFFFFF';
  ctx.font = 'bold 18px "Noto Sans KR", sans-serif';
  ctx.fillText(`지인들이 평가한 MBTI:`, 65, 185);

  ctx.fillStyle = '#FEE500';
  ctx.font = 'extrabold 24px "Outfit", sans-serif';
  ctx.fillText(analytics.perceivedMbti, 270, 187);

  const dimensions = [
    { title: '외향(E) vs 내향(I)', perceived: `E ${analytics.pcts.pctE}% / I ${analytics.pcts.pctI}%`, val: analytics.pcts.pctE },
    { title: '감각(S) vs 직관(N)', perceived: `S ${analytics.pcts.pctS}% / N ${analytics.pcts.pctN}%`, val: analytics.pcts.pctS },
    { title: '사고(T) vs 감정(F)', perceived: `T ${analytics.pcts.pctT}% / F ${analytics.pcts.pctF}%`, val: analytics.pcts.pctT },
    { title: '판단(J) vs 인식(P)', perceived: `J ${analytics.pcts.pctJ}% / P ${analytics.pcts.pctP}%`, val: analytics.pcts.pctJ }
  ];

  let startY = 260;
  dimensions.forEach(d => {
    ctx.fillStyle = '#F8FAFC';
    ctx.font = 'bold 15px "Noto Sans KR", sans-serif';
    ctx.fillText(d.title, 40, startY);

    ctx.fillStyle = '#06B6D4';
    ctx.font = '13px "Noto Sans KR", sans-serif';
    ctx.fillText(d.perceived, 400, startY);

    ctx.fillStyle = 'rgba(255, 255, 255, 0.1)';
    ctx.roundRect(40, startY + 10, 520, 16, 8);
    ctx.fill();

    ctx.fillStyle = '#8B5CF6';
    const fillWidth = Math.max(16, Math.min(520, (d.val / 100) * 520));
    ctx.roundRect(40, startY + 10, fillWidth, 16, 8);
    ctx.fill();

    startY += 65;
  });

  ctx.fillStyle = '#FEE500';
  ctx.font = 'bold 16px "Noto Sans KR", sans-serif';
  ctx.fillText('🔥 지인들이 선택한 내 대표 특징 TOP 3', 40, 540);

  let kwY = 575;
  analytics.topKeywords.slice(0, 3).forEach((item, idx) => {
    ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
    ctx.roundRect(40, kwY, 520, 42, 10);
    ctx.fill();

    ctx.fillStyle = '#EC4899';
    ctx.font = 'bold 14px "Noto Sans KR", sans-serif';
    ctx.fillText(`${idx + 1}위`, 60, kwY + 26);

    ctx.fillStyle = '#FFFFFF';
    ctx.font = 'bold 15px "Noto Sans KR", sans-serif';
    ctx.fillText(item.keyword, 110, kwY + 26);

    ctx.fillStyle = '#94A3B8';
    ctx.font = '13px "Noto Sans KR", sans-serif';
    ctx.fillText(`${item.count}회 선택`, 480, kwY + 26);

    kwY += 52;
  });

  ctx.fillStyle = 'rgba(255, 255, 255, 0.3)';
  ctx.font = '13px "Noto Sans KR", sans-serif';
  ctx.fillText('타인이 보는 내 MBTI | https://othermbti-app-2026.surge.sh', 160, 770);
}

if (!CanvasRenderingContext2D.prototype.roundRect) {
  CanvasRenderingContext2D.prototype.roundRect = function(x, y, w, h, r) {
    if (w < 2 * r) r = w / 2;
    if (h < 2 * r) r = h / 2;
    this.beginPath();
    this.moveTo(x + r, y);
    this.arcTo(x + w, y, x + w, y + h, r);
    this.arcTo(x + w, y + h, x, y + h, r);
    this.arcTo(x, y + h, x, y, r);
    this.arcTo(x, y, x + w, y, r);
    this.closePath();
    return this;
  };
}

function showToast(msg) {
  const toast = document.getElementById('toast');
  toast.textContent = msg;
  toast.classList.remove('hidden');
  setTimeout(() => {
    toast.classList.add('hidden');
  }, 3000);
}

function sendKakaoRichFeedCard(nickname, uid) {
  const shareUrl = `https://othermbti-app-2026.surge.sh/test?target=${uid}`;
  const title = `🌸 [모두의 MBTI] ${nickname} 님의 MBTI 1분 평가 초댓장!`;
  const description = `내가 아는 나 vs 친구들이 보는 나의 MBTI Gap! 솔직하게 평가해주세요.`;

  const imageUrl = `https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800&auto=format&fit=crop&q=80`;

  if (window.Kakao) {
    if (!window.Kakao.isInitialized()) {
      try {
        window.Kakao.init('b3a5198d070081d6833c829e30a5170d');
      } catch (e) {
        console.warn('Kakao init:', e);
      }
    }

    if (window.Kakao.isInitialized() && window.Kakao.Share) {
      try {
        window.Kakao.Share.sendDefault({
          objectType: 'feed',
          content: {
            title: title,
            description: description,
            imageUrl: imageUrl,
            link: {
              mobileWebUrl: shareUrl,
              webUrl: shareUrl,
            },
          },
          buttons: [
            {
              title: '1분만에 평가하러 가기 🐣',
              link: {
                mobileWebUrl: shareUrl,
                webUrl: shareUrl,
              },
            },
          ],
        });
        showToast('카카오톡 커스텀 리치 피드 카드가 전송되었습니다!');
        return;
      } catch (e) {
        console.warn('Kakao Share sendDefault fallback:', e);
      }
    }
  }

  // Web Share or Clipboard Fallback
  if (navigator.share) {
    navigator.share({
      title: title,
      text: `${title}\n${description}\n👉 ${shareUrl}`,
      url: shareUrl
    }).then(() => {
      showToast('카카오톡 / 메시지로 전송 완료되었습니다!');
    }).catch(() => {});
  } else {
    navigator.clipboard.writeText(`${title}\n${description}\n👉 ${shareUrl}`).then(() => {
      showToast('카톡 공유 문구가 클립보드에 복사되었습니다!');
    });
  }
}

function showRewardedAdThenExecute(callback) {
  const modalAd = document.getElementById('modalAdReward');
  const cdElem = document.getElementById('adCountdown');
  if (!modalAd || !cdElem) {
    if (typeof callback === 'function') callback();
    return;
  }

  let seconds = 3;
  cdElem.textContent = seconds;
  modalAd.classList.remove('hidden');

  const timer = setInterval(() => {
    seconds--;
    cdElem.textContent = seconds;
    if (seconds <= 0) {
      clearInterval(timer);
      modalAd.classList.add('hidden');
      if (typeof callback === 'function') {
        callback();
      }
    }
  }, 1000);
}


