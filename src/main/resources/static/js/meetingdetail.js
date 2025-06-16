// ✅ 전역 변수
let ws = null;
const userId = window.userId;
const groupId = window.groupId;

// --- 신고 모달 전역 변수 ---
let currentTargetType = null;
let currentTargetId = null;
let currentReportedUserId = null;
let currentReportCategoryId = null;

// ✅ 로그인 유저 정보 (가짜 데이터)
const currentLoggedInUser = {
  name: '프로트런트',
  avatar: 'https://i.pravatar.cc/150?u=me'
};

// ✅ 모임 방 정보 (가짜 데이터)
const meetingDate = new Date();
meetingDate.setHours(meetingDate.getHours() + 1);
meetingDate.setMinutes(meetingDate.getMinutes() + 30);

const room = {
  title: '감자탕 맛있다 ~ 모임',
  hostName: '장규진 귀엽다',
  hostAvatar: 'https://i.pravatar.cc/150?u=jangkyujin',
  foodImage: 'https://images.unsplash.com/photo-1627041541484-2353f5556658?q=80&w=1964&auto=format&fit=crop',
  content: '맛있는 감자탕 먹어용\n고기 먹고 라면 먹고 볶음밥까지!',
  tags: ['식사'],
  participants: [
    { name: '장규진 귀엽다', avatar: 'https://i.pravatar.cc/150?u=jangkyujin' },
    { name: '이순신', avatar: 'https://i.pravatar.cc/150?u=leesoonsin' },
    { name: '김프로', avatar: 'https://i.pravatar.cc/150?u=kimpro' },
  ],
  age_min: 20,
  age_max: 29,
  group_date_obj: meetingDate,
  members_max: 4,
  place_name: '참이맛 감자탕'
};

let isChatJoined = room.participants.some(p => p.name === currentLoggedInUser.name);
let lastMessageInfo = null;

// ✅ DOM 요소
const detailCardContainer = document.getElementById('roomDetailCard');
const inlineChatWrapper = document.getElementById('inline-chat-wrapper');
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

// ✅ 초기화
function displayRoomDetails() {
  const allTags = [...room.tags];
  if (room.age_min >= 20 && room.age_max < 30) allTags.push('20대');
  else if (room.age_min >= 30 && room.age_max < 40) allTags.push('30대');

  detailCardContainer.innerHTML = `
    <div class="live-card">
      <div class="card-top-banner">
        <img src="${room.foodImage}" alt="모임 대표 이미지" class="food-image">
        <div class="host-info">
          <img src="${room.hostAvatar}" alt="${room.hostName}">
          <span>${room.hostName}</span>
        </div>
      </div>
      <div class="card-content">
        <div class="tag-list">${allTags.map(tag => `<span>${tag}</span>`).join('')}</div>
        <h3>${room.title}</h3>
        <div class="content-body">
          <div class="left-col">${room.content}</div>
          <div class="right-col">
            <div id="detailMap"></div>
            <ul class="details-list">
              <li><span class="label">모임 시간</span> <span>${formatTime(room.group_date_obj)}</span></li>
              <li><span class="label">남은 시간</span> <span>${getTimeRemaining(room.group_date_obj)}</span></li>
              <li><span class="label">현재 인원</span> <span>${room.participants.length} / ${room.members_max}</span></li>
            </ul>
          </div>
        </div>
        <div class="card-actions" id="cardActions"></div>
      </div>
    </div>
  `;

  updateMainButtons();
}

function updateMainButtons() {
  const cardActionsContainer = document.getElementById('cardActions');
  if (!cardActionsContainer) return;
  cardActionsContainer.innerHTML = '';

  if (isChatJoined) {
    if (room.hostName !== currentLoggedInUser.name) {
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
    joinBtn.onclick = joinChat;
    cardActionsContainer.appendChild(joinBtn);
  }
}

function joinChat() {
  if (!isChatJoined) {
    room.participants.push(currentLoggedInUser);
    isChatJoined = true;
    displayRoomDetails();
    showInlineChat(true);
    connectWebSocket();
  }
}

function leaveChat() {
  room.participants = room.participants.filter(p => p.name !== currentLoggedInUser.name);
  isChatJoined = false;
  displayRoomDetails();
  inlineChatWrapper.style.display = 'none';
  roomPanel.classList.remove('active');
  if (ws) ws.close();
}

function connectWebSocket() {
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
            bubble.innerHTML = `${escapeHTML(data.newContent)}${edited ? ' <span class="edited-label">(수정됨)</span>' : ''}`;
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

  ws.send(JSON.stringify({ msg: text }));
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
      group.id = `message-${messageId}`;
      const messageDiv = document.createElement('div');
      messageDiv.className = `message ${type} deleted`;
      messageDiv.innerHTML = `
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
  /*messageDiv.innerHTML = `
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
  `;*/

  messageDiv.innerHTML = `
      ${type === 'self' ? '' : `<img class="avatar" src="${avatar}" alt="avatar">`}
      <div class="content-container">
        <span class="nickname">${nickname}</span>
        <div class="bubble-container">
          <div class="bubble">
            ${escapeHTML(content)}${edited ? ' <span class="edited-label">(수정됨)</span>' : ''}
          </div>
          <span class="timestamp">${formatTime(date)}</span>
        </div>
      </div>
    `;
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
    addSystemMessage(`${currentLoggedInUser.name}님이 채팅방에 입장하셨습니다.`);

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
}

function getTimeRemaining(endtime) {
  const total = Date.parse(endtime) - Date.parse(new Date());
  const hours = Math.floor((total / (1000 * 60 * 60)) % 24);
  const minutes = Math.floor((total / 1000 / 60) % 60);
  if (total <= 0) return '모임 시간 종료';
  return `${hours > 0 ? hours + '시간 ' : ''}${minutes}분`;
}

function formatTime(date) {
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const ampm = hours >= 12 ? '오후' : '오전';
  const displayHour = hours % 12 || 12;
  return `${month}월 ${day}일 ${ampm} ${displayHour}시 ${minutes}분`;

}

// ✅ 이벤트 연결
sendBtn.addEventListener('click', sendMessage);
chatInput.addEventListener('keypress', e => { if (e.key === 'Enter') sendMessage(); });
roomBtn.addEventListener('click', () => {
  participantsList.innerHTML = room.participants.map(p => {
    let nameTag = p.name;
    if (p.name === room.hostName) nameTag += ' (방장)';
    return `<div class="participant" data-nickname="${p.name}" data-avatar="${p.avatar}">
      <img src="${p.avatar}" alt="${p.name}">
      <span class="name">${nameTag}</span>
    </div>`;
  }).join('');
  updateSidePanelFooter();
  roomPanel.classList.add('active');
});
roomCloseBtn.addEventListener('click', () => roomPanel.classList.remove('active'));
chatMessages.addEventListener('click', (e) => {
  if (e.target.classList.contains('avatar')) {
    showProfile(e.target.dataset.nickname, e.target.dataset.avatar);
  }
});
participantsList.addEventListener('click', (e) => {
  const participant = e.target.closest('.participant');
  if (participant) {
    showProfile(participant.dataset.nickname, participant.dataset.avatar);
  }
});
profileCloseBtn.addEventListener('click', () => profilePanel.classList.remove('active'));

function showProfile(nickname, avatarSrc) {
  profilePanelAvatar.src = avatarSrc;
  profilePanelNickname.textContent = nickname;
  profilePanel.classList.add('active');
}

// ✅ 초기 실행
window.addEventListener('DOMContentLoaded', () => {
  displayRoomDetails();
  setInterval(displayRoomDetails, 60000);
  if (isChatJoined) showInlineChat(false);
});



function addContextMenuHandler(messageDiv, msgData) {
  const bubble = messageDiv.querySelector('.bubble');
  if (!bubble) return;

  console.log("[🎯 우클릭 핸들러 등록됨]", msgData);


  bubble.addEventListener('contextmenu', (e) => {
    e.preventDefault();

    console.log("👉 우클릭 감지됨");


    // 🔸 기존 모든 context menu 제거
    document.querySelectorAll('.custom-context-menu').forEach(menu => menu.remove());

    // ✅ 사용자 구분
    const isSelf = msgData.type === 'self';

    // 🔸 메뉴 요소 생성
    const contextMenu = document.createElement('div');
    contextMenu.className = 'custom-context-menu';
    contextMenu.style.position = 'absolute';
    contextMenu.style.left = `${e.pageX}px`;
    contextMenu.style.top = `${e.pageY}px`;
    contextMenu.style.background = '#fff';
    contextMenu.style.border = '1px solid #ccc';
    contextMenu.style.boxShadow = '0 2px 6px rgba(0,0,0,0.15)';
    contextMenu.style.zIndex = 1000;

    if (isSelf) {
      contextMenu.innerHTML = `
        <div class="menu-item" onclick="handleEdit(${msgData.messageId})">✏ 수정</div>
        <div class="menu-item" onclick="handleDelete(${msgData.messageId})">🗑 삭제</div>
      `;
    } else {
      contextMenu.innerHTML = `
        <div class="menu-item" onclick="handleReport(${msgData.messageId}, ${msgData.userIdx})">🚨 신고</div>
      `;
    }

    // 🔸 메뉴 삽입
    document.body.appendChild(contextMenu);

    // 🔸 클릭 시 메뉴 제거
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

  bubble.innerHTML = `${escapeHTML(newContent)}${isEdited ? ' <span class="edited-label">(수정됨)</span>' : ''}`;

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