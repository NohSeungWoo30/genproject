// script.js

// ──────────────────────────────────────────────────────
// 태그별 상단 배경색 맵 (공통)
const tagColorMap = {

  '운동/액티비티': '#FFDCE0',
  '문화/예술':     '#D0E8FF',
  '지식/스터디':   '#D0E8FF',
  '음식/카페':     '#FFF5D7',
  '여행/탐험':     '#E1FDD8',
  '게임/오락':     '#F0DBFF',
  '반려동물/자연':  '#CCEFFF',
  '만들기/공예':   '#FFE1F5',
  '소셜/친목':     '#E6E6FA',
  '기타':          '#E0E0E0'
};

// ──────────────────────────────────────────────────────
// mypage.html 전용 로직
if (window.location.pathname.endsWith('mypage.html') || window.location.pathname.endsWith('/')) {
  // 요소 참조
  const roomImageInput       = document.getElementById('roomImageInput');
  const liveRoomImage        = document.getElementById('liveRoomImage');
  const liveRoomPlaceholder  = document.getElementById('liveRoomPlaceholder');
  const tagIcons             = document.querySelectorAll('.tag-icon');
  let   selectedCategories   = [];
  const titleInput           = document.getElementById('titleInput');
  const descriptionInput     = document.getElementById('descriptionInput');
  const locationInput        = document.getElementById('locationInput');
  const meetingDatetimeInput = document.getElementById('meetingDatetimeInput');
  const remainingTimeDisplay = document.getElementById('remainingTimeDisplay');
  const liveMeetingTime      = document.getElementById('liveMeetingTime');
  const liveRemainingTime    = document.getElementById('liveRemainingTime');

  // 나이 슬라이더
  const ageRange             = document.getElementById('ageRange');
  const selectedAgeDisplay   = document.getElementById('selectedAgeDisplay');
  const ageLabelRight        = document.getElementById('ageLabelRight');
  let   selectedAge          = ageRange.value;

  // 최대 인원 및 기존 카운트 입력(legacy)
  const maxCountInputElem    = document.getElementById('maxCountInput');
  const countRangeInputElem  = document.getElementById('currentCountInput');

  // 미리보기 요소
  const liveTagsContainer    = document.getElementById('liveTagsContainer');
  const liveAgeContainer     = document.getElementById('liveAgeContainer');
  const liveTitle            = document.getElementById('liveTitle');
  const liveDescription      = document.getElementById('liveDescription');
  const createRoomButton     = document.getElementById('createRoomButton');
  const liveHeaderBackground = document.getElementById('liveHeaderBackground');
  let   liveMap, liveGeocoder, liveMarker, liveCoords = null;

  // 태그 선택
  tagIcons.forEach(icon => {
    icon.style.backgroundColor = '#e0e0e0';
    icon.style.color = '#555';
    icon.addEventListener('click', () => {
      const value = icon.dataset.value;
      if (selectedCategories.includes(value)) {
        selectedCategories = selectedCategories.filter(v => v !== value);
        icon.classList.remove('selected');
        icon.style.backgroundColor = '#e0e0e0';
        icon.style.color = '#555';
      } else {
        selectedCategories.push(value);
        icon.classList.add('selected');
        icon.style.backgroundColor = '#4285f4';
        icon.style.color = '#fff';
      }
      updateLivePreview();
    });
  });

  // 이미지 업로드 처리
  roomImageInput.addEventListener('change', () => {
    const file = roomImageInput.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = e => {
        liveRoomImage.src = e.target.result;
        liveRoomPlaceholder.classList.add('hidden');
        liveRoomImage.classList.remove('hidden');
      };
      reader.readAsDataURL(file);
    }
    updateLivePreview();
  });

  // 네이버 지도 초기화
  function initNaverMap() {
    liveGeocoder = new naver.maps.Service.Geocoder();
    liveMap       = new naver.maps.Map('map', { center: new naver.maps.LatLng(37.5563, 126.9723), zoom: 10 });
    liveMarker    = new naver.maps.Marker({ position: liveMap.getCenter(), map: liveMap });
    const smallMap = new naver.maps.Map('liveSmallMap', { center: liveMap.getCenter(), zoom: 10 });
    new naver.maps.Marker({ position: smallMap.getCenter(), map: smallMap });
  }
  window.addEventListener('DOMContentLoaded', () => { initNaverMap(); updateLivePreview(); });

  // 주소 → 지오코딩
  function geocodeAndUpdateMap(address) {
    if (!address) return;
    liveGeocoder.geocode({ address }, (status, response) => {
      if (status === naver.maps.Service.Status.OK) {
        const item   = response.v2.addresses[0];
        const coords = new naver.maps.LatLng(item.y, item.x);
        liveMap.setCenter(coords);
        liveMarker.setPosition(coords);
        liveCoords = { lat: item.y, lng: item.x };
        const smallMap = new naver.maps.Map('liveSmallMap', { center: coords, zoom: 10 });
        new naver.maps.Marker({ position: coords, map: smallMap });
        updateLivePreview();
      }
    });
  }
  locationInput.addEventListener('keydown', e => { if (e.key === 'Enter') geocodeAndUpdateMap(locationInput.value.trim()); });
  locationInput.addEventListener('blur', () => geocodeAndUpdateMap(locationInput.value.trim()));

  // 남은 시간 계산
  function calculateRemainingTime(dt) {
    const diff = new Date(dt) - new Date();
    if (diff <= 0) return '시간 지남';
    const day    = Math.floor(diff / (1000*60*60*24));
    const hour   = Math.floor((diff / (1000*60*60)) % 24);
    const minute = Math.floor((diff / (1000*60)) % 60);
    return `${day}일 ${hour}시간 ${minute}분 남음`;
  }

  // 나이 슬라이더 변화
  ageRange.addEventListener('input', () => {
    selectedAge = ageRange.value;
    selectedAgeDisplay.textContent = `선택 나이: ${selectedAge}`;
    ageLabelRight.textContent = (selectedAge === ageRange.max) ? '누구나' : `${selectedAge}세`;
    updateLivePreview();
  });

  // 실시간 미리보기 업데이트
  function updateLivePreview() {
    liveHeaderBackground.style.backgroundColor =
      selectedCategories.length ? tagColorMap[selectedCategories[0]] : '#FFFBED';
    if (liveRoomImage.src && !liveRoomImage.classList.contains('hidden')) {
      liveRoomPlaceholder.classList.add('hidden');
      liveRoomImage.classList.remove('hidden');
    }
    liveTagsContainer.innerHTML = '';
    selectedCategories.forEach(val => {
      const chip = document.createElement('div');
      chip.className = 'tag-chip';
      chip.textContent = val;
      chip.style.backgroundColor = tagColorMap[val] || '#ccc';
      chip.style.color = '#fff';
      liveTagsContainer.appendChild(chip);
    });
    liveAgeContainer.innerHTML = '';
    if (selectedAge) {
      const ageChip = document.createElement('div');
      ageChip.className = 'age-chip';
      ageChip.textContent = `${selectedAge}세`;
      liveAgeContainer.appendChild(ageChip);
    }
    liveTitle.textContent       = titleInput.value || '방 제목';
    liveDescription.textContent = descriptionInput.value || '설명 내용이 여기에 표시됩니다.';
    if (meetingDatetimeInput.value) {
      liveMeetingTime.textContent   = meetingDatetimeInput.value.replace('T', ' ');
      liveRemainingTime.textContent = calculateRemainingTime(meetingDatetimeInput.value);
      remainingTimeDisplay.value    = liveRemainingTime.textContent;
    }
  }
  [titleInput, descriptionInput, meetingDatetimeInput].forEach(el => el.addEventListener('input', updateLivePreview));

  // 방 생성 버튼 클릭 이벤트
  createRoomButton.addEventListener('click', () => {
    if (!titleInput.value.trim()) {
      alert('방 제목은 반드시 입력해야 합니다.');
      return;
    }
    let currentCount, maxCount;
    if (maxCountInputElem) {
      maxCount = parseInt(maxCountInputElem.value);
      if (isNaN(maxCount) || maxCount < 1) {
        alert('최대 인원을 1명 이상으로 설정해주세요.');
        return;
      }
      currentCount = 1;
    } else if (countRangeInputElem) {
      const parts = (countRangeInputElem.value || '').split('/').map(s => s.trim());
      if (parts.length >= 2) {
        currentCount = parseInt(parts[0]) || 1;
        maxCount     = parseInt(parts[1]) || currentCount;
      } else {
        currentCount = 1;
        maxCount     = parseInt(parts[0]) || 1;
      }
    } else {
      currentCount = 1;
      maxCount     = 1;
    }

    const newRoom = {
      id: Date.now(),
      image: liveRoomImage.src || '',
      categoryTags: [...selectedCategories],
      age: selectedAge,
      title: titleInput.value.trim(),
      description: descriptionInput.value.trim(),
      location: locationInput.value.trim(),
      coords: liveCoords,
      meetingTime: meetingDatetimeInput.value,
      remainingTime: calculateRemainingTime(meetingDatetimeInput.value),
      maxCount,
      currentCount
    };

    const rooms = JSON.parse(localStorage.getItem('rooms') || '[]');
    rooms.push(newRoom);
    localStorage.setItem('rooms', JSON.stringify(rooms));
    localStorage.setItem('latestRoom', JSON.stringify(newRoom));
    window.location.href = 'Meeting-list.html';
  });
}

// ──────────────────────────────────────────────────────
// list.html 전용: 목록 렌더링
if (window.location.pathname.endsWith('Meeting-list.html')) {
  document.addEventListener('DOMContentLoaded', () => {
    const listContainer = document.getElementById('roomsListContainer');
    const rooms = JSON.parse(localStorage.getItem('rooms') || '[]');
    listContainer.innerHTML = rooms.length
      ? rooms.map(room => `
          <div class="live-card" onclick="(function(){ localStorage.setItem('latestRoom', JSON.stringify(${JSON.stringify({id:room.id})})); window.location.href='detail.html';})()">
            <div class="preview-header-background" style="background:${tagColorMap[room.categoryTags[0]] || '#FFFBED'}">
              <div class="room-image-container">${room.image
                ? `<img class="room-image" src="${room.image}" />`
                : `<div class="image-preview-placeholder"></div>`}
              </div>
            </div>
            <h2 class="title">${room.title}</h2>
            <p>${room.currentCount} / ${room.maxCount}명</p>
          </div>`
        ).join('')
      : '<p>등록된 모임이 없습니다.</p>';
  });
}

// ──────────────────────────────────────────────────────
// detail.html 전용: 상세 + 참여/나가기
if (window.location.pathname.endsWith('detail.html')) {
  document.addEventListener('DOMContentLoaded', () => {
    let room = JSON.parse(localStorage.getItem('latestRoom') || '{}');
    const cardContainer = document.getElementById('roomDetailCard');
    cardContainer.innerHTML = `
      <div class="preview-header-background" style="background:${tagColorMap[room.categoryTags[0]] || '#FFFBED'}">
        <div class="room-image-container">
          ${room.image
            ? `<img class="room-image" src="${room.image}" />`
            : `<div class="image-preview-placeholder"></div>`}
        </div>
      </div>
      <div class="detail-info">
        <h2>${room.title}</h2>
        <p>${room.description}</p>
        <p>${room.categoryTags.join(', ')}</p>
        <p>${room.age}세</p>
        <p>${room.meetingTime.replace('T',' ')}</p>
        <p>${room.remainingTime}</p>
        <p id="countInfo">${room.currentCount} / ${room.maxCount}명</p>
      </div>
      <div id="detailMap" class="map-area"></div>
      <button id="joinDetailButton">방 참여</button>
    `;

    const detailMap = new naver.maps.Map('detailMap', { center: new naver.maps.LatLng(room.coords.lat, room.coords.lng), zoom: 14 });
    new naver.maps.Marker({ position: detailMap.getCenter(), map: detailMap });

    const joinBtn = document.getElementById('joinDetailButton');
    let hasJoined = false;
    joinBtn.addEventListener('click', () => {
      const rooms = JSON.parse(localStorage.getItem('rooms') || '[]');
      const idx = rooms.findIndex(r => r.id === room.id);
      if (!hasJoined && rooms[idx].currentCount < rooms[idx].maxCount) {
        rooms[idx].currentCount++;
        hasJoined = true;
        joinBtn.textContent = '나가기';
      } else if (hasJoined) {
        rooms[idx].currentCount--;
        hasJoined = false;
        joinBtn.textContent = '방 참여';
      } else {
        return alert('참가 인원이 가득 찼습니다.');
      }
      localStorage.setItem('rooms', JSON.stringify(rooms));
      room = rooms[idx];
      document.getElementById('countInfo').textContent = `${room.currentCount} / ${room.maxCount}명`;
    });
  });
}
