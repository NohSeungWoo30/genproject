console.log("📦 meetingdetail.js 로딩됨");


// ✅ 전역 변수
let ws = null;
const userId = window.userId;
/*
const groupId = window.groupId;
*/
let isChatJoined = false;
let lastMessageInfo = null;
window.room = window.room || null;


// --- 신고 모달 전역 변수 ---
let currentTargetType = null;
let currentTargetId = null;
let currentReportedUserId = null;
let currentReportCategoryId = null;


// ✅ DOM 요소
const detailCardContainer = document.querySelector('#group-detail-modal #roomDetailCard');
const inlineChatWrapper = document.querySelector('#group-detail-modal #inline-chat-wrapper');
const chatRoomTitleText = document.getElementById('chatRoomTitleText');
const chatMessages = document.getElementById('chatMessages');
const chatInput = document.getElementById('chatInput');
const sendBtn = document.getElementById('sendBtn');
const roomBtn = document.getElementById('roomBtn');
const roomPanel = document.getElementById('roomPanel');
const roomCloseBtn = document.getElementById('roomCloseBtn');
const participantsList = document.getElementById('participantsList');
const roomPanelFooter = document.getElementById('roomPanelFooter');
const profilePanel = document.getElementById('profilePanel');
const profileCloseBtn = document.getElementById('profileCloseBtn');
const profilePanelAvatar = document.getElementById('profilePanelAvatar');
const profilePanelNickname = document.getElementById('profilePanelNickname');



function applyGroupButtonUI() {
  console.log('[BTN] applyGroupButtonUI');   // ← ②

  const btn  = document.getElementById('open-filter-btn');
  if (!btn) return;

  // ↙︎ 방어: room 또는 groupIdx 없으면 실행하지 말고 리턴
  if (!window.room || !window.room.groupIdx) return;
  const icon = btn.querySelector('ion-icon');
  const text = btn.querySelector('span');

  icon.setAttribute('name', 'chatbubble-ellipses-outline');
  text.textContent = '그룹방';
  btn.onclick = () => {
    document.getElementById('group-detail-modal')?.classList.remove('hidden');
    displayRoomDetails();
    showInlineChat(false);
    connectWebSocket();
  };
}


// ✅ 초기화
function displayRoomDetails() {
  if (!window.room) return;

  // 구조 분해로 깔끔하게 꺼내 씀
  const {
    title, content, groupImgUrl,
    hostNickname, hostAvatar,
    placeAddress, placeName,
    membersMin, membersMax, partyMember,
    groupDate
  } = room;

  detailCardContainer.innerHTML = /* html */`
    <div class="live-card">
      <div class="card-top-banner">
        <img src="${groupImgUrl}" alt="대표 이미지" class="food-image">
        <div class="host-info">
          <img src="${hostAvatar || '/img/default-avatar.jpg'}" alt="${hostNickname}">
          <span>${hostNickname}</span>
        </div>
      </div>

      <div class="card-content">
        <h3>${title}</h3>

        <div class="content-body">
          <div class="left-col">
            ${content ?? ''}
          </div>

          <!-- ★ 지도 div 사라지고, 주소·장소·인원으로 대체 -->
          <div class="right-col">
            <ul class="details-list">
              <li><span class="label">주소</span> <span>${placeAddress}</span></li>
              <li><span class="label">장소명</span> <span>${placeName}</span></li>
              <li><span class="label">모임 시간</span> <span>${formatTime(groupDate)}</span></li>
              <li><span class="label">인원</span> <span>${partyMember} / ${membersMin}~${membersMax}</span></li>
            </ul>
          </div>
        </div>
        <div class="card-actions" id="cardActions"></div>
      </div>
    </div>`;

    updateMainButtons();

}

window.displayRoomDetails = displayRoomDetails;
console.log("✅ displayRoomDetails 함수 전역 등록 완료");




if (!window.groupId && room && room.groupIdx) {
  window.groupId = room.groupIdx;
  console.log("✅ 동적으로 groupId 설정됨:", window.groupId);
}

// ① 모달 열기·닫기 함수
function openJoinConfirmModal() {
  const modal = document.getElementById('join-confirm-modal');
  if (!modal) return;
  modal.classList.remove('hidden');

  const okBtn = document.getElementById('joinConfirmBtn');
  const cancelBtn = document.getElementById('joinCancelBtn');

  // 중복 바인딩 방지용 기존 리스너 제거
  okBtn.replaceWith(okBtn.cloneNode(true));
  cancelBtn.replaceWith(cancelBtn.cloneNode(true));

  // 새 요소 다시 가져오기
  const ok = document.getElementById('joinConfirmBtn');
  const cancel = document.getElementById('joinCancelBtn');

  ok.onclick = () => {          // 확인 → 실제 joinChat 호출
    modal.classList.add('hidden');
    joinChat();
  };
  cancel.onclick = () => {      // 취소 → 그냥 닫기
    modal.classList.add('hidden');
  };
}



function updateMainButtons() {
  const cardActionsContainer = document.getElementById('cardActions');
  if (!cardActionsContainer) return;
  cardActionsContainer.innerHTML = '';

  if (isChatJoined) {
    if (room.hostNickname !== currentLoggedInUser.nickname) {
      const leaveBtn = document.createElement('button');
      leaveBtn.className = 'detail-btn delete';
      leaveBtn.textContent = '방 나가기';
      leaveBtn.onclick = leaveChat;
      cardActionsContainer.appendChild(leaveBtn);
    }
  } else {
    const joinBtn = document.createElement('button');
    joinBtn.className = 'detail-btn';
    joinBtn.textContent = '방 참여';
    joinBtn.onclick = openJoinConfirmModal;
    cardActionsContainer.appendChild(joinBtn);
  }
}
async function joinChat() {

  window.groupId = room?.groupIdx ?? groupData?.groupIdx ?? null;
  if (!window.groupId) console.warn("⚠ groupId 설정 실패");


  if (!isChatJoined) {
    if (!window.groupId || !window.userId) {
      alert("채팅방 정보 오류");
      return;
    }

    try {
      const response = await fetch( `/group/api/groups/${window.groupId}/join`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          groupId: window.groupId,
          userId: window.userId
        })
      });

      /* ───── 실패(401·403·500…) 처리 ───── */
            if (!response.ok) {
              // 서버가 text/plain 을 주므로 text()로 읽는다
              const msg = await response.text();

              switch (response.status) {
                case 401:
                  alert("로그인이 필요합니다.");
                  break;
                case 403:
                  alert(msg || "이용권 잔여 횟수가 없습니다.");
                  break;
                default:
                  alert(`오류(${response.status}) : ${msg}`);
              }
              return;           // 실패 시 이후 로직 중단
            }

      const groupData  = await response.json();
      console.log("🎯 join 후 groupData 다시 로드:", groupData);

      window.room = groupData;
      window.groupId = groupData.groupIdx;
      isChatJoined = true;

      localStorage.setItem('joinedGroupId', window.groupId);
      await updateFloatingButton();

      connectWebSocket();
      showInlineChat(true);
      displayRoomDetails();
    } catch (err) {
      console.error("❌ 참가 실패:", err);
    }
  }
}

async function updateFloatingButton () {
  console.log('🔍 updateFloatingButton 실행됨');

  const btn = document.getElementById('open-filter-btn');
  if (!btn) return;

  /* ① ───── setToMatching 정의(함수 선언) ───── */
  const setToMatching = () => {
    console.log('[BTN] setToMatching');          // ← 한 번만 찍히면 성공
    const icon = btn.querySelector('ion-icon');
    const text = btn.querySelector('span');
    icon.setAttribute('name', 'options-outline');
    text.textContent = '간편 매칭';
    btn.onclick = () =>
      document.getElementById('filter-modal')?.classList.remove('hidden');
  };

  /* ② ───── ‘기본값’ 으로 한 번만 호출 ───── */
  setToMatching();      // ★ 여기 1회만!

  /* ③ ───── 서버 & 로컬스토리지 확인 ───── */
  try {
    /* 1) 로컬스토리지 우선 */
    const stored = localStorage.getItem('joinedGroupId');
    if (stored) {
      const r = await fetch(`/group/api/groups/${stored}`);
      if (r.ok) {
        window.room  = await r.json();
        window.groupId = room.groupIdx;
        isChatJoined = true;
        applyGroupButtonUI();       // ← 그룹방 UI 로 덮어쓰기
        return;                     // 더 이상 진행 X
      }
        localStorage.removeItem('joinedGroupId');

    }

    /* 2) 서버에 현재 참가 방 질의 */
    const res = await fetch(`/group/api/current-group?userId=${window.userId}`);
    console.log('📡 status', res.status);

    if (res.status === 200) {
      const group = await res.json();
      if (group && group.groupIdx) {
        window.room    = group;
        window.groupId = group.groupIdx;
        isChatJoined   = true;
        localStorage.setItem('joinedGroupId', group.groupIdx);
        applyGroupButtonUI();       // ← 그룹방 UI 로 교체

      }
    }

    /* 3) 204이거나 데이터 없으면 그대로 ‘간편 매칭’ 유지 */
  } catch (err) {
    console.warn('❌ current-group 요청 실패', err);
    // 네트워크 장애 시에도 기존 UI(간편 매칭) 유지
  }
}




async function leaveChat() {
  try {
    // 1. 서버에 “나가기” 요청
    const res = await fetch(
      `/group/api/groups/${window.groupId}/leave?userId=${window.userId}`,
      { method: 'POST' }
    );
    const result = await res.json();
    console.log('🚪 나가기 성공:', result);

    // 2. 클라이언트 상태 초기화
    localStorage.removeItem('joinedGroupId');
    updateFloatingButton();
    isChatJoined = false;

    // 3. participants 배열에서 내 정보 제거
    if (room && Array.isArray(room.participants)) {
      room.participants = room.participants.filter(
        p => p.nickname !== currentLoggedInUser.nickname
      );
    }

    // 4. ★ DB에서 최신 값 다시 받아오기 ★
    const fresh = await fetch(`/group/api/groups/detail/${window.groupId}`)
                          .then(r => r.json());
    window.room = fresh;

    // 5. 화면 갱신
    displayRoomDetails();
    inlineChatWrapper.style.display = 'none';
    roomPanel.classList.remove('active');
    if (ws) ws.close();

  } catch (e) {
    console.error('❌ 나가기 실패:', e);
  }
}


function connectWebSocket() {

  if (!window.groupId) {
    alert("⚠ 그룹 ID가 없습니다. 채팅방을 열 수 없습니다.");
    return;
  }


  const groupId = window.groupId; // 이걸 함수 안에서 다시 선언

  const url = `ws://${location.hostname}:8080/ws/chat?groupId=${encodeURIComponent(groupId)}`;
  ws = new WebSocket(url);

  ws.onopen = () => console.log('✅ WebSocket 연결됨');

  ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log("[웹소켓 수신]", data);

    // 디버깅용 콘솔
      console.log("[수신]", {
        from: data.from,
        msg: data.msg,
        dataUserId: data.userId,
        localUserId: window.userId,
        isSelf: String(data.userId) === String(window.userId)
      });


    if (data.type === 'IDENTIFY') return;
      if (data.type === 'DELETE') {
        // 삭제 메시지 처리
        addMessage({
          messageId: data.messageId,
          isDeleted: 'Y',
          userIdx: data.userIdx
        });
        return;
      }


      if (data.type === 'EDIT') {
        const target = document.getElementById(`message-${data.messageId}`);
        const edited = String(data.isEdited).toUpperCase() === 'Y';
        if (target) {
          const bubble = target.querySelector('.bubble');
          if (bubble) {
            bubble.innerHTML = `
              ${escapeHTML(data.newContent)}
              ${edited ? '<span class="edited-label">(수정됨)</span>' : ''}
            `;
          }
        }
        return;
      }

      const isSystem = data.userIdx === 1 || data.userId === 'system';

      if (isSystem) {
         addMessage({
           type: 'system',
           content: data.msg
         });
         return;
      }

      // 일반 메시지 처리
      if (data.from && data.msg) {
        addMessage({
          type: String(data.userIdx) === String(window.userId) ? 'self' : 'other',
          avatar: data.avatar || 'https://placehold.co/40x40',
          nickname: data.from,
          content: data.msg,
          date: new Date(data.sentAt || Date.now()),
          messageId: data.messageId,
          userIdx: data.userIdx,
          isDeleted: data.isDeleted,
          isEdited: data.isEdited
        });
      }
    };

  ws.onclose = () => console.log('❌ WebSocket 연결 종료');
}

function sendMessage() {
  const text = chatInput.value.trim();
  if (!text || !ws || ws.readyState !== WebSocket.OPEN) return;

  const payload = {
    type: "CHAT",
    msg: text,
    userId: window.userId  // ✅ 클라이언트가 보낼 수 있으면 넣고, 없어도 서버에서 인증으로 커버 가능
  };

  console.log("📤 보내는 메시지:", payload);
  ws.send(JSON.stringify(payload));
  chatInput.value = '';
}

function addSystemMessage(text) {
  const msgDiv = document.createElement('div');
  msgDiv.className = 'system-message';
  msgDiv.textContent = text;
  chatMessages.appendChild(msgDiv);
}

function addMessage(msgData) {
  const {
    type,
    avatar,
    nickname,
    content,
    date,
    messageId,
    userIdx,
    isDeleted,
    isEdited
  } = msgData;

  const edited = String(isEdited).toUpperCase() === 'Y';

  // 시스템 메시지 처리
  if (type === 'system') {
    const group = document.createElement('div');
    group.className = 'message-group';
    const systemDiv = document.createElement('div');
    systemDiv.className = 'system-message';
    systemDiv.innerText = content;
    group.appendChild(systemDiv);
    chatMessages.appendChild(group);
    chatMessages.scrollTop = chatMessages.scrollHeight;
    return;
  }

  // 삭제된 메시지 처리
  if (isDeleted === 'Y') {
    let target = document.getElementById(`message-${messageId}`);
    const editedText = edited ? ' <span class="edited-label">(수정됨)</span>' : '';
    if (!target) {
      const group = document.createElement('div');
      group.className = 'message-group';
      group.id = `message-${messageId}`
      const messageDiv = document.createElement('div');
      messageDiv.className = `message ${type} deleted`;
      messageDiv.innerHTML =`
        <div class="content-container">
          <div class="bubble-container">
            <div class="bubble">삭제된 메시지입니다.${editedText}</div>
            <span class="timestamp"></span>
          </div>
        </div>`;
      group.appendChild(messageDiv);
      chatMessages.appendChild(group);
      chatMessages.scrollTop = chatMessages.scrollHeight;
      return;
    }
    const bubble = target.querySelector('.bubble');
    if (bubble) {
      bubble.innerHTML = `삭제된 메시지입니다.${editedText}`;
      bubble.classList.add('deleted');
    }
    return;
  }

  // 일반 메시지
  const group = document.createElement('div');
  group.className = 'message-group';
  group.id = `message-${messageId}`;



  const messageDiv = document.createElement('div');
  messageDiv.className = `message ${type}`;
  /*messageDiv.innerHTML =
    <img class="avatar" src="${avatar}" alt="avatar">
    <div class="content-container">
      <span class="nickname">${nickname}</span>
      <div class="bubble-container">
        <div class="bubble">
          ${escapeHTML(content)}${edited ? ' <span class="edited-label">(수정됨)</span>' : ''}
        </div>
        <span class="timestamp">${formatTime(date)}</span>
      </div>
    </div>
  ;*/

  messageDiv.innerHTML =`
      ${type === 'self'
           ? ''
           : `<img class="avatar" src="${avatar}" alt="avatar">`
        }
      <div class="content-container">
        <span class="nickname">${nickname}</span>
        <div class="bubble-container">
          <div class="bubble">
            ${escapeHTML(content)}${edited ? ' <span class="edited-label">(수정됨)</span>' : ''}
          </div>
          <span class="timestamp">${formatTime(date)}</span>
        </div>
      </div>`;
  group.appendChild(messageDiv);
  chatMessages.appendChild(group);
  chatMessages.scrollTop = chatMessages.scrollHeight;

  addContextMenuHandler(messageDiv, msgData);
}


function showInlineChat(isFirstTime) {
  chatRoomTitleText.textContent = room.title;
  if (isFirstTime) {
    chatMessages.innerHTML = '';
    lastMessageInfo = null;
/*
    addSystemMessage(`${currentLoggedInUser.nickname}님이 채팅방에 입장하셨습니다.`);
*/

  // ✅ 초기 채팅 메시지 불러오기
  fetch(`/api/chat/messages?groupId=${groupId}`)

    .then(res => res.json())
    .then(messages => {
      messages.forEach(msg => {
        addMessage({
          type: isSystem
                ? 'system'
                : String(msg.userIdx) === String(userId) ? 'self' : 'other',
              avatar: msg.avatar || 'https://placehold.co/40x40',
              nickname: msg.nickname,
              content: msg.content,
              date: new Date(msg.sentAt),
              messageId: msg.messageIdx,
              userIdx: msg.userIdx,
              isEdited: msg.isEdited,
              isDeleted: msg.isDeleted
        });
      });
    })
    .catch(err => {
      console.error("초기 채팅 메시지 불러오기 실패:", err);
    });
  }

inlineChatWrapper.style.display = 'block';


setupChatEvents();
}

function getTimeRemaining(endtime) {
  const total = Date.parse(endtime) - Date.parse(new Date());
  const hours = Math.floor((total / (1000 * 60 * 60)) % 24);
  const minutes = Math.floor((total / 1000 / 60) % 60);
  if (total <= 0) return '모임 시간 종료';
  return `${hours > 0 ? hours + '시간 ' : ''}${minutes}분`;
}

function formatTime(date) {
  const d = new Date(date); // ← 이걸 추가
  const month = d.getMonth() + 1;
  const day = d.getDate();
  const hours = d.getHours();
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const ampm = hours >= 12 ? '오후' : '오전';
  const displayHour = hours % 12 || 12;
  return `${month}월 ${day}일 ${ampm} ${displayHour}시 ${minutes}분`;
}
// ✅ 이벤트 연결 ──────────────────────────
if (sendBtn)   sendBtn.addEventListener('click', sendMessage);
if (chatInput) chatInput.addEventListener('keypress', e => {
  if (e.key === 'Enter') sendMessage();
});
// ▼ ▼ 1) roomBtn이 있을 때만 사이드패널 열기 ▼ ▼
if (roomBtn) {
  roomBtn.addEventListener('click', () => {
    if (!room || !Array.isArray(room.participants)) return;

    participantsList.innerHTML = room.participants.map(p => {
      let tag = p.nickname;
      if (p.nickname === room.hostNickname) tag += ' (방장)';
      return `
            <div class="participant" data-nickname="${p.nickname}" data-avatar="${p.avatar}">
              <img src="${p.avatar}" alt="${p.nickname}">
              <span class="name">${tag}</span>
            </div>
          `;
        })
        .join('');

    updateSidePanelFooter();
    roomPanel.classList.add('active');
  });
}
if (roomCloseBtn) {
  roomCloseBtn.addEventListener('click', () => roomPanel.classList.remove('active'));
}
chatMessages.addEventListener('click', (e) => {
  if (e.target.classList.contains('avatar')) {
    showProfile(e.target.dataset.nickname, e.target.dataset.avatar);
  }
});
if (participantsList) {
  participantsList.addEventListener('click', (e) => {
    const participant = e.target.closest('.participant');
    if (participant) {
      showProfile(participant.dataset.nickname, participant.dataset.avatar);
    }
  });
}
if (profileCloseBtn) {
  profileCloseBtn.addEventListener('click', () => {
    profilePanel.classList.remove('active');
  });
}
function showProfile(nickname, avatarSrc) {
  profilePanelAvatar.src = avatarSrc;
  profilePanelNickname.textContent = nickname;
  profilePanel.classList.add('active');
}

// ✅ 초기 실행
window.addEventListener('DOMContentLoaded', async () => {

  /* ① 로컬스토리지 먼저 → UI 고정 */
  const storedGroupId = localStorage.getItem('joinedGroupId');

  if (storedGroupId) {
    try {
      const res = await fetch(
        `/group/api/groups/${storedGroupId}`
      );
      if (!res.ok) throw new Error("방 정보 로딩 실패");

      const joinedRoom = await res.json();
      window.room = joinedRoom;
      window.groupId = joinedRoom.groupIdx;
      isChatJoined = true;

      applyGroupButtonUI();
      displayRoomDetails();
      showInlineChat(false);
      connectWebSocket();
    } catch (e) {
      console.warn("⚠ 저장된 방 로딩 실패, 초기화함");
      localStorage.removeItem('joinedGroupId');

      displayRoomDetails();
    }
  } else {
    displayRoomDetails(); // 기본 카드 렌더링
  }



  // ✅ 안전한 이벤트 연결 (null 체크)
  if (sendBtn) {
    sendBtn.addEventListener('click', sendMessage);
  }

  if (chatInput) {
    chatInput.addEventListener('keypress', e => {
      if (e.key === 'Enter') sendMessage();
    });
  }

  if (roomBtn) {
    roomBtn.addEventListener('click', () => {
      if (!room || !room.participants) return;

      participantsList.innerHTML = room.participants.map(p => {
        let nameTag = p.nickname;
        if (p.nickname === room.hostNickname) nameTag += ' (방장)';
        return `
          <div
            class="participant"
            data-nickname="${p.nickname}"
            data-avatar="${p.avatar}"
          >
            <img src="${p.avatar}" alt="${p.nickname}">
            <span class="name">${p.nickname}</span>
          </div>
        `;
      }).join('');
      updateSidePanelFooter();
      roomPanel.classList.add('active');
    });
  }

  if (roomCloseBtn) {
    roomCloseBtn.addEventListener('click', () => roomPanel.classList.remove('active'));
  }

  if (chatMessages) {
    chatMessages.addEventListener('click', (e) => {
      if (e.target.classList.contains('avatar')) {
        showProfile(e.target.dataset.nickname, e.target.dataset.avatar);
      }
    });
  }

  if (participantsList) {
    participantsList.addEventListener('click', (e) => {
      const participant = e.target.closest('.participant');
      if (participant) {
        showProfile(participant.dataset.nickname, participant.dataset.avatar);
      }
    });
  }

  if (profileCloseBtn) {
    profileCloseBtn.addEventListener('click', () => profilePanel.classList.remove('active'));
  }

  // ✅ 1분마다 갱신
  setInterval(displayRoomDetails, 60000);


  /* ② 그다음 서버에 한 번 더 물어봐서 동기화 */
    await updateFloatingButton();
});



function addContextMenuHandler(messageDiv, msgData) {
  const bubble = messageDiv.querySelector('.bubble');
  if (!bubble) return;

  console.log("[🎯 우클릭 핸들러 등록됨]", msgData);


  bubble.addEventListener('contextmenu', (e) => {
    e.preventDefault();

    // 🔸 기존 메뉴 제거
    document.querySelectorAll('.custom-context-menu').forEach(menu => menu.remove());

    const contextMenu = document.createElement('div');
    contextMenu.className = 'custom-context-menu';

    // ✅ 기본 위치
    let left = e.clientX;
    let top = e.clientY;

    // ✅ 화면 밖 방지 (기본 크기 150x80 기준)
    const maxW = 150;
    const maxH = 80;
    const vw = window.innerWidth;
    const vh = window.innerHeight;

    if (left + maxW > vw) left = vw - maxW - 8;
    if (top + maxH > vh) top = vh - maxH - 8;

    contextMenu.style.position = 'fixed'; // ✅ body 기준 위치 고정
    contextMenu.style.left = `${left}px`;
    contextMenu.style.top = `${top}px`;

    // 🔸 메뉴 구성
    const isSelf = msgData.type === 'self';
    contextMenu.innerHTML = isSelf
      ? `
          <div class="menu-item" onclick="handleEdit(${msgData.messageId})">수정</div>
          <div class="menu-item" onclick="handleDelete(${msgData.messageId})">삭제</div>
        `
      : `
          <div class="menu-item" onclick="handleReport(${msgData.messageId}, ${msgData.userIdx})">신고</div>
        `;

    document.body.appendChild(contextMenu);

    // 🔸 외부 클릭 시 제거
    const removeMenu = (ev) => {
      if (!contextMenu.contains(ev.target)) {
        contextMenu.remove();
        document.removeEventListener('click', removeMenu);
      }
    };
    document.addEventListener('click', removeMenu);
  });
}

function handleEdit(messageId) {
  console.log("✏ 수정 요청:", messageId);
  document.querySelectorAll('.custom-context-menu').forEach(menu => menu.remove());

const messageEl = document.getElementById(`message-${messageId}`);
  if (!messageEl) return;

  const bubble = messageEl.querySelector('.bubble');
  if (!bubble) return;

  const originalContent = bubble.textContent;

  const input = document.createElement('textarea');
  input.className = 'edit-input';
  input.value = originalContent;

  const actions = document.createElement('div');
  actions.className = 'edit-actions';

  const saveBtn = document.createElement('button');
  saveBtn.className = 'save-btn';
  saveBtn.textContent = '💾 저장';

  const cancelBtn = document.createElement('button');
  cancelBtn.className = 'cancel-btn';
  cancelBtn.textContent = '❌ 취소';

  saveBtn.onclick = () => {
    const newContent = input.value.trim();
    if (newContent && newContent !== originalContent) {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
          type: 'EDIT',
          messageId,
          newContent,
          userId: window.userId
        }));
      }
    }
    restoreBubble(bubble, newContent || originalContent, true);
  };

  cancelBtn.onclick = () => {
    restoreBubble(bubble, originalContent, false);
  };

  actions.appendChild(saveBtn);
  actions.appendChild(cancelBtn);

  bubble.style.display = 'none';
  const container = bubble.parentNode;
  container.appendChild(input);
  container.appendChild(actions);
}

function restoreBubble(bubble, newContent, isEdited = true) {
  const container = bubble.parentNode;
  container.querySelectorAll('.edit-input, .edit-actions').forEach(el => el.remove());

  bubble.innerHTML = `
    ${escapeHTML(newContent)}
    ${isEdited ? '<span class="edited-label">(수정됨)</span>' : ''}
  `;
  bubble.style.display = '';
}


function handleDelete(messageId) {
  console.log("🗑 삭제 요청:", messageId);

  // context menu 닫기
  document.querySelectorAll('.custom-context-menu').forEach(menu => menu.remove());

  if (ws) {
    ws.send(JSON.stringify({
      type: "DELETE",
      messageId: messageId,
      userId: window.userId
    }));
  }
}

function handleReport(messageId, userIdx) {
  console.log("🚨 신고 요청:", messageId, userIdx);
  // 👉 너가 구현해둔 openReportModal 함수 호출
  openReportModal('CHAT', messageId, userIdx, 3);
}

// 신고 모달창

function openReportModal(type, id, userId, categoryId) {
  currentTargetType = type;
  currentTargetId = id;
  currentReportedUserId = userId;
  currentReportCategoryId = categoryId;

  const modal = document.getElementById('report-modal');
  if (modal) modal.style.display = 'block';
  console.log("🚨 openReportModal 실행됨:", { type, id, userId, categoryId });
}

function closeReportModal() {
  const modal = document.getElementById('report-modal');
  if (modal) modal.style.display = 'none';
}


function submitReport() {
  const reasonId = document.getElementById('report-reason').value;
  const comment = document.getElementById('report-comment').value;

  if (!reasonId || !comment.trim()) {
    alert("신고 사유와 내용을 모두 입력하세요.");
    return;
  }

  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

  fetch("/api/reports", {
    method: "POST",
    headers: {
      'Content-Type': 'application/json',
      [csrfHeader]: csrfToken
    },
    body: JSON.stringify({
      entityType: currentTargetType,
      entityId: Number(currentTargetId),
      reportCategoryId: Number(currentReportCategoryId),
      reportReasonId: Number(reasonId),
      reportedUserId: Number(currentReportedUserId),
      reportComment: comment
    })
  })
  .then(async res => {
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || "신고 실패");
    }
    return res.json();
  })
  .then(() => {
    alert("신고가 정상적으로 접수되었습니다.");
    closeReportModal();
  })
  .catch(err => alert("신고 중 오류: " + err.message));
}

  function escapeHTML(str) {
    return str.replace(/[&<>"']/g, tag => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;'
    }[tag]));
  }

function closeGroupDetailModal() {
  // 그룹 상세 모달 닫기
  document.getElementById('group-detail-modal')?.classList.add('hidden');

  // 💡 혹시라도 열려 있을 수 있는 필터 모달도 닫는다
  document.getElementById('filter-modal')?.classList.add('hidden');
}

function setupChatEvents() {
  // 기존 바인딩 제거 (중복 방지)
  sendBtn?.removeEventListener('click', sendMessage);
  chatInput?.removeEventListener('keypress', handleKeyPress);

  // 다시 연결
  sendBtn?.addEventListener('click', sendMessage);
  chatInput?.addEventListener('keypress', handleKeyPress);
}

function handleKeyPress(e) {
  if (e.key === 'Enter') {
    sendMessage();
  }
}

if (typeof displayRoomDetails === 'function') {
  console.log("✅ displayRoomDetails 전역 등록 확인됨");
} else {
  console.error("❌ displayRoomDetails 전역 등록 실패");
}


fetch('/group/api/current-group?userId=' + window.userId)
  .then(res => {
    console.log('status =', res.status);   // ← 200? 204? 500?
    return res.text();                     // 바디도 확인
  })
  .then(console.log);


/** 메인·리스트·디테일 페이지 어디서든 같은 방식으로 모달을 띄운다 */
document.addEventListener('open-group-detail', async ({ detail }) => {
  const { groupIdx } = detail;
  try {
    /* 최신 그룹 정보 가져오기 */
    const data = await fetch(`/group/api/groups/detail/${groupIdx}`)
                         .then(r => r.json());

    /* 전역 상태 갱신 – meetingdetail.js 가 이미 쓰는 변수들 */
    window.groupId   = groupIdx;
    window.room      = data;
    window.isChatJoined = false;   // 아직 입장 안한 상태

    /* 모달 콘텐츠 렌더링 */
    if (typeof displayRoomDetails === 'function') displayRoomDetails();

    /* 모달 열기 */
    document.getElementById('group-detail-modal')
            .classList.remove('hidden');
  } catch (err) {
    console.error('그룹 데이터 로드 실패', err);
    alert('모임 정보를 불러오지 못했습니다.');
  }
});


window.addEventListener('DOMContentLoaded', async () => {
  const filterBtn   = document.getElementById('open-filter-btn');
  const openRoomBtn = document.getElementById('open-room-btn');

  if (!filterBtn || !openRoomBtn) {
    console.warn("버튼 요소를 찾지 못했습니다.");
    return;   // 이제 함수 내부라 안전합니다
  }

  // (1) 참여 여부 동기화
  await updateFloatingButton();

  // (2) 토글
  if (window.isChatJoined) {
    openRoomBtn .style.display = 'flex';
    filterBtn  .style.display = 'none';
  } else {
    filterBtn  .style.display = 'flex';
    openRoomBtn .style.display = 'none';
  }

  // (3) 클릭 핸들러 바인딩
  filterBtn .addEventListener('click', openModal);
  openRoomBtn .addEventListener('click', () => {
    document.getElementById('group-detail-modal')?.classList.remove('hidden');
    displayRoomDetails();
  });
});
