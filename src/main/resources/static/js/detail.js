
document.addEventListener('DOMContentLoaded', () => {
    // --- 1. 데이터 준비 (시안에 맞게 수정) ---
    const currentLoggedInUser = { name: '프로트런트', avatar: 'https://i.pravatar.cc/150?u=me' };

    // 모임 시간을 동적으로 설정 (현재로부터 1시간 30분 뒤)
    const meetingDate = new Date();
    meetingDate.setHours(meetingDate.getHours() + 1);
    meetingDate.setMinutes(meetingDate.getMinutes() + 30);

    const room = {
        title: '감자탕 맛있다 ~ 모임',
        hostName: '장규진 귀엽다',
        hostAvatar: 'https://i.pravatar.cc/150?u=jangkyujin',
        foodImage: 'https://images.unsplash.com/photo-1627041541484-2353f5556658?q=80&w=1964&auto=format&fit=crop',
        content: `맛있는 감자탕 먹어용
아들,아들 감칠맛 쩌는
고기 먹고, 국물 머금은
시래기 먹다가, 어느 정도
먹었을때 라면 사리 넣어
서 또 다른 재미를 챙겨주고
볶음밥으로 마무리~~`,
        tags: ['식사'],
        participants: [
            { name: '장규진 귀엽다', avatar: 'https://i.pravatar.cc/150?u=jangkyujin' },
            { name: '이순신', avatar: 'https://i.pravatar.cc/150?u=leesoonsin' },
            { name: '김프로', avatar: 'https://i.pravatar.cc/150?u=kimpro' },
        ],
        age_min: 20,
        age_max: 29,
        group_date_obj: meetingDate, // Date 객체로 저장
        members_max: 4,
        place_name: '참이맛 감자탕',
    };

    // --- 2. UI 요소 가져오기 ---
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
    let isChatJoined = room.participants.some(p => p.name === currentLoggedInUser.name);
    let lastMessageInfo = null;

    // --- 3. 함수 정의 ---
    function formatTime(date) {
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const hours = date.getHours();
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const ampm = hours >= 12 ? '오후' : '오전';
        const displayHour = hours % 12 || 12;
        return `${month}월 ${day}일 ${ampm} ${displayHour}시 ${minutes}분`;
    }

    function getTimeRemaining(endtime) {
        const total = Date.parse(endtime) - Date.parse(new Date());
        const hours = Math.floor((total / (1000 * 60 * 60)) % 24);
        const minutes = Math.floor((total / 1000 / 60) % 60);

        if (total <= 0) {
            return "모임 시간 종료";
        }
        return `${hours > 0 ? hours + '시간 ' : ''}${minutes}분`;
    }

    function displayRoomDetails() {
        const allTags = [...room.tags];
        if(room.age_min >= 20 && room.age_max < 30) allTags.push('20대');
        else if(room.age_min >= 30 && room.age_max < 40) allTags.push('30대');

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
            </div>`;

        // 지도 생성 (Naver Maps API가 로드된 경우)
        setTimeout(() => { if (window.naver && window.naver.maps) { new naver.maps.Map('detailMap', { center: new naver.maps.LatLng(37.5665, 126.9780), zoom: 15 }); }}, 100);

        updateMainButtons();
    }

    function updateMainButtons() {
        const cardActionsContainer = document.getElementById('cardActions');
        if (!cardActionsContainer) return;

        cardActionsContainer.innerHTML = '';
        if (isChatJoined) {
             // 방장인 경우 버튼 없음, 방장이 아닌 참여자는 나가기 버튼
            if (room.hostName !== currentLoggedInUser.name) {
                const leaveBtn = document.createElement('button');
                leaveBtn.className = 'detail-btn delete';
                leaveBtn.textContent = '방 나가기';
                leaveBtn.onclick = leaveChat;
                cardActionsContainer.appendChild(leaveBtn);
            }
        } else {
            // 참여하지 않은 경우
            const joinBtn = document.createElement('button');
            joinBtn.className = 'detail-btn';
            joinBtn.textContent = '방 참여';
            joinBtn.onclick = joinChat;
            cardActionsContainer.appendChild(joinBtn);
        }
    }

    function updateSidePanelFooter() {
        roomPanelFooter.innerHTML = '';
        if (room.hostName === currentLoggedInUser.name) {
            const deleteBtn = document.createElement('button');
            deleteBtn.className = 'detail-btn delete';
            deleteBtn.textContent = '방 삭제하기';
            deleteBtn.style.width = '100%';
            deleteBtn.onclick = () => alert('방이 삭제되었습니다.');
            roomPanelFooter.appendChild(deleteBtn);
        } else if (isChatJoined) {
            const leaveBtn = document.createElement('button');
            leaveBtn.className = 'detail-btn delete';
            leaveBtn.textContent = '방 나가기';
            leaveBtn.style.width = '100%';
            leaveBtn.onclick = leaveChat;
            roomPanelFooter.appendChild(leaveBtn);
        }
    }

    function joinChat() {
        if (!isChatJoined) {
            room.participants.push(currentLoggedInUser);
            isChatJoined = room.participants.some(p => p.name === currentLoggedInUser.name);
            displayRoomDetails();
            showInlineChat(true);
        }
    }

    function leaveChat() {
        room.participants = room.participants.filter(p => p.name !== currentLoggedInUser.name);
        isChatJoined = false;
        displayRoomDetails();
        inlineChatWrapper.style.display = 'none';
        roomPanel.classList.remove('active');
    }

    function addSystemMessage(text) {
        const msgDiv = document.createElement('div');
        msgDiv.className = 'system-message';
        msgDiv.textContent = text;
        chatMessages.appendChild(msgDiv);
    }

    function addMessage(msgData) {
        const { type, avatar, nickname, content, date } = msgData;
        const simpleDateStr = `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`;
        const allSeparators = document.querySelectorAll('#chatMessages .date-separator');
        const lastDateSeparator = allSeparators.length > 0 ? allSeparators[allSeparators.length - 1] : null;

        if (!lastDateSeparator || lastDateSeparator.dataset.date !== simpleDateStr) {
            const sep = document.createElement('div');
            sep.className = 'date-separator';
            sep.dataset.date = simpleDateStr;
            sep.innerHTML = `<span class="text">${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일</span>`;
            chatMessages.appendChild(sep);
        }

        const hours = date.getHours(); const ampm = hours >= 12 ? '오후' : '오전';
        const hour12 = ((hours + 11) % 12) + 1; const minutes = String(date.getMinutes()).padStart(2, '0');
        const timeStr = `${ampm} ${hour12}:${minutes}`;

        const isNewSpeaker = !lastMessageInfo || lastMessageInfo.nickname !== nickname || lastMessageInfo.simpleDate !== simpleDateStr;
        if (lastMessageInfo && !isNewSpeaker && lastMessageInfo.timeStr === timeStr && lastMessageInfo.timestampElement) {
            lastMessageInfo.timestampElement.style.display = 'none';
        }

        const group = document.createElement('div');
        group.className = 'message-group';
        group.dataset.nickname = nickname;
        if (isNewSpeaker) group.classList.add('new-speaker');

        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;

        messageDiv.innerHTML = `
            <img src="${avatar}" class="avatar" data-nickname="${nickname}" data-avatar="${avatar}">
            <div class="content-container">
                <div class="nickname">${nickname}</div>
                <div class="bubble-container">
                    <div class="bubble">${content}</div>
                    <span class="timestamp">${timeStr}</span>
                </div>
            </div>`;

        if (type === 'self') {
            messageDiv.querySelector('.avatar').style.display = 'none';
        }

        group.appendChild(messageDiv);
        chatMessages.appendChild(group);
        chatMessages.scrollTop = chatMessages.scrollHeight;

        lastMessageInfo = {
            nickname: nickname,
            simpleDate: simpleDateStr,
            timeStr: timeStr,
            timestampElement: group.querySelector('.timestamp')
        };
    }

    function showInlineChat(isFirstTime) {
        chatRoomTitleText.textContent = room.title;
        if (isFirstTime) {
            chatMessages.innerHTML = '';
            lastMessageInfo = null;
            addSystemMessage(`${currentLoggedInUser.name}님이 채팅방에 입장하셨습니다.`);
        }
        inlineChatWrapper.style.display = 'block';
    }

    function sendMessage() {
        const text = chatInput.value.trim();
        if (!text) return;
        addMessage({
            type: 'self',
            avatar: currentLoggedInUser.avatar,
            nickname: currentLoggedInUser.name,
            content: text,
            date: new Date()
        });
        chatInput.value = '';
    }

    function showProfile(nickname, avatarSrc) {
        profilePanelAvatar.src = avatarSrc;
        profilePanelNickname.textContent = nickname;
        profilePanel.classList.add('active');
    }

    // --- 4. 이벤트 핸들러 연결 및 초기화 ---
    displayRoomDetails();
    // 남은 시간을 1분마다 업데이트
    setInterval(displayRoomDetails, 60000);

    sendBtn.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', e => { if (e.key === 'Enter') sendMessage(); });

    if (isChatJoined) {
        showInlineChat(false);
    }

    roomBtn.addEventListener('click', () => {
        participantsList.innerHTML = room.participants.map(p => {
            let nameTag = p.name;
            if (p.name === room.hostName) nameTag += ' (방장)';
            return `<div class="participant" data-nickname="${p.name}" data-avatar="${p.avatar}"><img src="${p.avatar}" alt="${p.name}"><span class="name">${nameTag}</span></div>`
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
  });
