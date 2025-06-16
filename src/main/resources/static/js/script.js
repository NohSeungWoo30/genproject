document.addEventListener('DOMContentLoaded', () => {
  const tagColorMap = {
    '식사': '#FFA07A',
    '운동/액티비티': '#F08080',
    '문화/예술': '#87CEEB',
    '지식/스터디': '#ADD8E6',
    '음식/카페': '#FFDAB9',
    '여행/탐험': '#98FB98',
    '게임/오락': '#DDA0DD',
    '반려동물/자연': '#AFEEEE',
    '만들기/공예': '#FFB6C1',
    '소셜/친목': '#FFE4E1',
    '기타': '#D3D3D3'
  };
  const headerColorMap = {
    '식사': '#FFE4B5',
    '운동/액티비티': '#FFDCE0',
    '문화/예술': '#D0E8FF',
    '지식/스터디': '#D1E7DD',
    '음식/카페': '#FFF5D7',
    '여행/탐험': '#E1FDD8',
    '게임/오락': '#F0DBFF',
    '반려동물/자연': '#CCEFFF',
    '만들기/공예': '#FFE1F5',
    '소셜/친목': '#E6E6FA',
    '기타': '#E0E0E0'
  };

  if (document.getElementById('mainContainer')) {
    // 변수 선언
    const form = document.querySelector('form'); // 폼 요소

    // 히든 요소
    const ownerIdxInput = document.getElementById('ownerIdx');
    const selectedCategoryIdInput = document.getElementById('selectedCategoryId'); // 카테고리 hidden input
    const ageMinInput = document.getElementById('ageMin'); // 나이 min제한
    const ageMaxInput = document.getElementById('ageMax'); // 나이 max제한
    const minPersonInput = document.getElementById('minPerson'); // 인원 최소
    const maxPersonInput = document.getElementById('maxPerson'); // 인원 최대

    const roomImageInput = document.getElementById('roomImageInput');
    const liveRoomImage = document.getElementById('liveRoomImage');
    const liveRoomPlaceholder = document.getElementById('liveRoomPlaceholder');
    const tagIcons = document.querySelectorAll('.tag-icon');
    const titleInput = document.getElementById('titleInput');
    const descriptionInput = document.getElementById('descriptionInput');
    const locationInput = document.getElementById('locationInput');
    const meetingDatetimeInput = document.getElementById('meetingDatetimeInput');
    const remainingTimeDisplay = document.getElementById('remainingTimeDisplay');
    const createRoomButton = document.getElementById('createRoomButton');
    const cancelButton = document.getElementById('cancelButton');
    const minAgeDisplay = document.getElementById('minAgeDisplay');
    const maxAgeDisplay = document.getElementById('maxAgeDisplay');
    const minPersonDisplay = document.getElementById('minPersonDisplay');
    const maxPersonDisplay = document.getElementById('maxPersonDisplay');
    const placeResultList = document.getElementById('placeResultList');
    /* 지도 검색결과 리스트 클릭 시 아래 input으로 들어갈 UI */
    const meetingPlaceNameInput = document.getElementById('meetingPlaceName');
    const meetingPlaceCategoryInput = document.getElementById('meetingPlaceCategory');
    const meetingPlaceAddressInput = document.getElementById('meetingPlaceAddress');
    const meetingPlaceLinkInput = document.getElementById('meetingPlaceLink');
    const meetingPlaceLatInput = document.getElementById('meetingPlaceLatInput');
    const meetingPlaceLngInput = document.getElementById('meetingPlaceLngInput');
    const genderRadios = document.getElementsByName('genderLimit');

    // 슬라이더 관련
    const ageSlider = document.getElementById('age-slider');
    const ageTicks = document.querySelector('.age-ticks');
    const personSlider = document.getElementById('person-slider');
    const personTicks = document.querySelector('.person-ticks');

    // 미리보기 영역
    const liveHeaderBackground = document.getElementById('liveHeaderBackground');
    const liveTagsContainer = document.getElementById('liveTagsContainer');
    const liveAgeContainer = document.getElementById('liveAgeContainer');
    const livePersonContainer = document.getElementById('livePersonContainer');
    const liveTitle = document.getElementById('liveTitle');
    const liveDescription = document.getElementById('liveDescription');
    const liveMeetingTime = document.getElementById('liveMeetingTime');
    const liveRemainingTime = document.getElementById('liveRemainingTime');
    const livePerson = document.getElementById('livePerson');

    // 태그 선택 상태
    let selectedCategories = [];
    let liveMap, smallMap, liveMarker, smallMapMarker;
    let markers = []; // 마커 배열
    let liveCoords = null;

    // 마커 지우는 함수
        function clearMarkers() {
            // markers 배열에 저장된 모든 마커를 지도에서 제거합니다.
            for (let i = 0; i < markers.length; i++) {
                markers[i].setMap(null);
            }
            // 배열 비우기
            markers = [];
        }

    // 장소 선택 시 폼 필드 업데이트 함수
        function selectPlaceFromSearch(title, link, category, address, lat, lng) {
        console.log("선택된 장소:", title, category, address, lat, lng);
                meetingPlaceNameInput.value = title; // 'name' 대신 'title' 사용
                meetingPlaceCategoryInput.value = category;
                meetingPlaceAddressInput.value = address;
                meetingPlaceLinkInput.value = link;
                meetingPlaceLatInput.value = lat;
                meetingPlaceLngInput.value = lng;
                alert(`선택된 장소: ${title}\n분류: ${category}\n주소: ${address}`);
                liveMap.setCenter(new naver.maps.LatLng(lat, lng)); // 선택된 장소로 지도 중심 이동

                // smallMap과 smallMapMarker 업데이트 (추가된 코드)
                smallMap.setCenter(new naver.maps.LatLng(lat, lng));
                smallMapMarker.setPosition(new naver.maps.LatLng(lat, lng));
            }

    // 네이버 지도 초기화
    function initNaverMap() {
      const defaultPosition = new naver.maps.LatLng(37.5665, 126.9780); // 서울 시청
      liveMap = new naver.maps.Map('map', { center: defaultPosition, zoom: 15 });
      liveMarker = new naver.maps.Marker({ position: defaultPosition, map: liveMap });
      smallMap = new naver.maps.Map('liveSmallMap', { center: defaultPosition, zoom: 14, draggable: false, scrollWheel: false });
      smallMapMarker = new naver.maps.Marker({ position: defaultPosition, map: smallMap });

        locationInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                liveMarker.setMap(null);
                const query = locationInput.value;
                if (query) {
                    searchPlace(query);
                } else {
                    alert('검색할 장소를 입력해주세요');
                    liveCoords = null;
                }
            }
        });
    }

    // --- 키워드를 이용해 장소 검색 (백엔드 프록시 경유) ---
        async function searchPlace(query) {
            clearMarkers();
            placeResultList.innerHTML = ''; // 기존 검색 결과 초기화

            // 1. 현재 지도 화면의 영역(Bounds) 정보 가져오기
            const bounds = liveMap.getBounds();
            console.log("bounds값 ", bounds);
            const southWest = bounds.getSW(); // 남서쪽 좌표
            const northEast = bounds.getNE(); // 북동쪽 좌표

            const southWestLat = southWest.lat();
            const southWestLng = southWest.lng();
            const northEastLat = northEast.lat();
            const northEastLng = northEast.lng();

            const displayCount = 100; // 더 많은 결과를 받아온 후 프론트에서 필터링합니다.

            // 2. 백엔드 API 호출 시 지도 영역 정보를 쿼리 파라미터로 전달
            const apiUrl = `/api/naver/places/by-coords?query=${encodeURIComponent(query)}&southwest_lat=${southWestLat}&southwest_lng=${southWestLng}&northeast_lat=${northEastLat}&northeast_lng=${northEastLng}&display=${displayCount}`;
            console.log("백엔드 프록시 API 요청 URL:", apiUrl);

            try {
                const response = await fetch(apiUrl);
                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
                }
                const data = await response.json();
                const items = data.items;

                if (items && items.length > 0) {
                    // 지도화면이동
                    const firstItemLat = parseFloat(items[0].mapy) / 1e7;
                    const firstItemLng = parseFloat(items[0].mapx) / 1e7;
                    const firstItemPoint = new naver.maps.LatLng(firstItemLat, firstItemLng);
                    liveMap.setCenter(firstItemPoint);

                    const currentBounds = liveMap.getBounds(); // 현재 지도의 가시 영역 (Bounds) 가져오기
                    console.log("현재 지도의 범위:", currentBounds);

                    let resultsInView = 0; // 화면 내에 표시된 결과 수 카운트

                    items.forEach((item) => {
                        const title = item.title.replace(/<[^>]*>/g, '').replace(/&amp;/g, '&');
                        const link = item.link || '';
                        const category = item.category || '';
                        const description = item.description || '';
                        const address = item.roadAddress || item.address || ''; <!--도로명주소 우선 없으면 지번 없으면 빈문자-->
                        const lat = parseFloat(item.mapy) / 1e7;
                        const lng = parseFloat(item.mapx) / 1e7;
                        const point = new naver.maps.LatLng(lat, lng);

                        // 현재 지도 화면 범위 내에 있는지 필터링
                        if (currentBounds.hasLatLng(point)) {
                            const marker = new naver.maps.Marker({
                                position: point,
                                map: liveMap,
                                title: title
                            });
                            markers.push(marker);

                            const li = document.createElement('li');
                            li.innerHTML = `<strong>${title}(${category})</strong><br>${address}`;
                            li.setAttribute('data-title', title);
                            li.setAttribute('data-link', link);
                            li.setAttribute('data-category', category);
                            li.setAttribute('data-description', description);
                            li.setAttribute('data-address', address);
                            li.setAttribute('data-lat', lat);
                            li.setAttribute('data-lng', lng);
                            li.classList.add('place-result-item');

                            placeResultList.appendChild(li);
                            resultsInView++;
                        } else {
                            console.log(`[화면 밖] ${title} - ${lat}, ${lng}`);
                        }
                    });

                    if (resultsInView === 0) {
                        placeResultList.innerHTML = '<li>현재 지도 화면 내에 검색 결과가 없습니다. 지도를 축소해서 검색 해보세요.</li>';
                    }

                } else {
                    placeResultList.innerHTML = '<li>검색 결과가 없습니다.</li>';
                }
            } catch (error) {
                console.error('검색 중 오류 발생:', error);
                alert('장소 검색에 실패했습니다.');
            }
        } // async function searchPlace 끝

        // 검색 결과 목록 (placeResultList) 클릭 이벤트 위임 리스너
        if (placeResultList) {
            placeResultList.addEventListener('click', function(event) {
                const clickedLi = event.target.closest('li.place-result-item'); // 가장 가까운 li.place-result-item 찾기
                if (clickedLi) {
                    // data-* 속성에서 값을 가져올 때, 없을 경우 빈 문자열로 초기화하여 안전성 확보
                    const title = clickedLi.dataset.title || '';
                    const link = clickedLi.dataset.link || '';
                    const category = clickedLi.dataset.category || '';
                    const address = clickedLi.dataset.address || '';
                    const lat = parseFloat(clickedLi.dataset.lat);
                    const lng = parseFloat(clickedLi.dataset.lng);
                    selectPlaceFromSearch(title, link, category, address, lat, lng);
                }
            });
        }

    /* 주소 검색 및 지도 업데이트 막음
    function geocodeAndUpdateMap(query) {
      if (!query) return;
      naver.maps.Service.geocode({ query }, (status, response) => {
        if (status === naver.maps.Service.Status.OK && response.v2.addresses.length > 0) {
          const item = response.v2.addresses[0];
          const coords = new naver.maps.LatLng(item.y, item.x);
          liveCoords = { lat: parseFloat(item.y), lng: parseFloat(item.x) };
          liveMap.setCenter(coords);
          liveMarker.setPosition(coords);
          smallMap.setCenter(coords);
          smallMapMarker.setPosition(coords);
        } else {
          alert('주소를 찾을 수 없습니다.');
          liveCoords = null;
        }
      });
    }

    // 위치 검색
            locationInput.addEventListener('keydown', (e) => {
              if (e.key === 'Enter') {
                e.preventDefault();
                geocodeAndUpdateMap(locationInput.value.trim());
              }
            });*/
    // 남은 시간 계산
    function calculateRemainingTime(datetimeStr) {
      if (!datetimeStr) return '';
      const diff = new Date(datetimeStr) - new Date();
      if (diff <= 0) return '마감';
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
      const minutes = Math.floor((diff / (1000 * 60)) % 60);
      return `${days}일 ${hours}시간 ${minutes}분 남음`;
    }

    // 슬라이더 세팅 (나이)
    if (ageSlider && !ageSlider.noUiSlider) {
      noUiSlider.create(ageSlider, {
        start: [parseInt(ageMinInput.value), parseInt(ageMaxInput.value)],
        step: 5,
        connect: true,
        range: { min: 20, max: 50 },
        format: {
          to: value => Math.round(value),
          from: value => Number(value)
        }
      });
      ageTicks.innerHTML = '';
      for (let v = 20; v <= 50; v += 5) {
        const tick = document.createElement('span');
        tick.textContent = v;
        ageTicks.appendChild(tick);
      }
      ageSlider.noUiSlider.on('update', function (values) {
        const minAge = values[0];
        const maxAge = values[1];
        ageMinInput.value = minAge; // hidden input 업데이트
        ageMaxInput.value = maxAge; // hidden input 업데이트
        minAgeDisplay.textContent = `${minAge}세`;
        maxAgeDisplay.textContent = `${maxAge}세`;
        console.log("나이 제한 :", minAge, maxAge);
        updateLivePreview();
      });
    }

    // 슬라이더 세팅 (인원)
    if (personSlider && !personSlider.noUiSlider) {
      noUiSlider.create(personSlider, {
        start: [parseInt(minPersonInput.value), parseInt(maxPersonInput.value)],
        step: 1,
        connect: true,
        range: { min: 2, max: 30 },
        format: {
          to: value => Math.round(value),
          from: value => Number(value)
        }
      });
      personTicks.innerHTML = '';
      for (let v = 2; v <= 30; v += 1) {
        const tick = document.createElement('span');
        tick.textContent = v;
        personTicks.appendChild(tick);
      }
      personSlider.noUiSlider.on('update', function (values) {
        const minPerson = values[0];
        const maxPerson = values[1];
        minPersonInput.value = minPerson; // hidden input 업데이트
        maxPersonInput.value = maxPerson; // hidden input 업데이트
        minPersonDisplay.textContent = `${minPerson}명`;
        maxPersonDisplay.textContent = `${maxPerson}명`;
        console.log("인원 제한 :", minPerson, maxPerson);
        updateLivePreview();
      });
    }

    // 실시간 미리보기 업데이트
    function updateLivePreview() {
      // 헤더 배경색
      const selectedCategoryName = selectedCategories.length > 0 ? selectedCategories[0].value : null;
        liveHeaderBackground.style.backgroundColor = selectedCategoryName
        ? headerColorMap[selectedCategories[0]]
        : '#FFFBED';

      // 태그 칩
      liveTagsContainer.innerHTML = '';
      selectedCategories.forEach(val => {
        const chip = document.createElement('div');
        chip.className = 'tag-chip';
        chip.textContent = val;
        chip.style.backgroundColor = tagColorMap[val] || '#999';
        liveTagsContainer.appendChild(chip);
      });

      // 나이 칩
      liveAgeContainer.innerHTML = '';
      const minAge = minAgeDisplay.textContent || '20';
      const maxAge = maxAgeDisplay.textContent || '50';
      const ageChip = document.createElement('div');
      ageChip.className = 'age-chip';
      ageChip.textContent = `${minAge} ~ ${maxAge}세`;
      liveAgeContainer.appendChild(ageChip);

      // 인원 칩
      livePersonContainer.innerHTML = '';
      const minPerson = minPersonDisplay.textContent || '2';
      const maxPerson = maxPersonDisplay.textContent || '12';
      const personChip = document.createElement('div');
      personChip.className = 'age-chip';
      personChip.textContent = `${minPerson} ~ ${maxPerson}명`;
      livePersonContainer.appendChild(personChip);

      // info-list의 인원 표기
      if (livePerson) {
        livePerson.textContent = `${minPerson} ~ ${maxPerson}명`;
      }

      // 제목, 설명
      liveTitle.textContent = titleInput.value.trim() || '방 제목';
      liveDescription.textContent = descriptionInput.value.trim() || '설명 내용이 여기에 표시됩니다.';

      // 시간
      const meetTime = meetingDatetimeInput.value;
      const remaining = calculateRemainingTime(meetTime);
      liveMeetingTime.textContent = meetTime ? meetTime.replace('T', ' ') : '미정';
      liveRemainingTime.textContent = remaining || '미정';
      remainingTimeDisplay.value = remaining;
    } // 실시간 미리보기 업데이트

    // 이벤트 바인딩 (input)
    [titleInput, descriptionInput, meetingDatetimeInput].forEach(el => {
      el.addEventListener('input', updateLivePreview);
    });
    tagIcons.forEach(icon => {
       icon.addEventListener('click', () => {
       const value = icon.dataset.value;
       const categoryId = icon.dataset.id; // 상단 카테고리 index
    // 1. 이미 선택된 아이콘이 있다면, 해당 아이콘의 'selected' 클래스 제거
        //    (이전에 선택된 것이 있다면 선택 해제)
        const currentlySelectedIcon = document.querySelector('.tag-icon.selected');
        if (currentlySelectedIcon && currentlySelectedIcon !== icon) {
          currentlySelectedIcon.classList.remove('selected');
          // 이전에 선택된 카테고리 값을 배열에서 제거 (선택 해제)
          selectedCategories = [];
          selectedCategoryIdInput.value = '';
        }

    // 2. 클릭된 아이콘의 선택 상태 토글 (단일 선택 로직)
        //    만약 클릭된 아이콘이 이미 'selected' 상태라면 (선택 해제),
        //    그렇지 않다면 'selected' 상태로 만들고 새 값으로 설정
        if (icon.classList.contains('selected')) { // 이미 선택되어 있었으면 해제
          icon.classList.remove('selected');
          selectedCategories = []; // 배열 비우기
          selectedCategoryIdInput.value = '';
        } else { // 선택되어 있지 않았으면 선택
          icon.classList.add('selected');
          selectedCategories = [ value ]; // 클릭된 값 하나만 배열에 넣기
          selectedCategoryIdInput.value = categoryId;
        }
        console.log("선택된 카테고리 이름:", value);
        console.log("선택된 카테고리 인덱스:", categoryId);
        console.log("Hidden ID :", selectedCategoryIdInput.value); // 확인용
        updateLivePreview();
        });
    });
    /* 모임 생성 카테고리 다중선택 막아둠
    tagIcons.forEach(icon => {
      icon.addEventListener('click', () => {
        const value = icon.dataset.value;
        if (selectedCategories.includes(value)) {
          selectedCategories = selectedCategories.filter(item => item !== value);
          icon.classList.remove('selected');
        } else {
          selectedCategories.push(value);
          icon.classList.add('selected');
        }
        updateLivePreview();
      });
    });*/

    /* 사진 업로드 막아둠
    roomImageInput.addEventListener('change', (event) => {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = e => {
          liveRoomImage.src = e.target.result;
          liveRoomPlaceholder.classList.add('hidden');
          liveRoomImage.classList.remove('hidden');
        };
        reader.readAsDataURL(file);
      } else {
        liveRoomImage.src = '';
        liveRoomPlaceholder.classList.remove('hidden');
        liveRoomImage.classList.add('hidden');
      }
    });*/

    // 폼 제출 시 유효성 검사 (AJAX 제출 전에 수행)
            if (createRoomButton) {
                createRoomButton.addEventListener('click', async (e) => {
                    e.preventDefault(); // 폼의 기본 제출 동작을 막습니다. (이제 AJAX로 직접 전송)

                    // 성별 제한 유효성 검사
                   const genderRadios = document.getElementsByName('genderLimit');
                   let isGenderSelected = false;
                   let selectedGender = '';
                   for (const radio of genderRadios) {
                       if (radio.checked) {
                       isGenderSelected = true;
                       selectedGender = radio.value;
                       break;
                      }
                    }
                     if (!isGenderSelected) {
                     alert('성별 제한을 선택해주세요.');
                     return;
                    }

                    // 나이 유효성 검사 (슬라이더에서 값을 가져옴)
                    const minAge = parseInt(ageMinInput.value);
                    const maxAge = parseInt(ageMaxInput.value);
                    if (isNaN(minAge) || isNaN(maxAge) || minAge > maxAge) {
                        alert('유효한 나이 범위를 설정해주세요.');
                        return;
                    }

                    // 인원 유효성 검사 (슬라이더에서 값을 가져옴)
                    const minMembers = parseInt(minPersonInput.value);
                    const maxMembers = parseInt(maxPersonInput.value);
                    if (isNaN(minMembers) || isNaN(maxMembers) || minMembers > maxMembers) {
                        alert('유효한 인원 수를 설정해주세요.');
                        return;
                    }
                    if (minMembers <= 1 || maxMembers <= 1) { // 1명 이하는 유효하지 않도록 수정
                                         alert('최소/최대 인원 수는 2명 이상이어야 합니다.');
                                         return;
                                    }


                    // 제목 유효성 검사
                    const titleInput = document.getElementById('titleInput');
                    if (titleInput.value.trim() === '') {
                        alert('방 제목을 입력해주세요.');
                        return;
                    }

                    // 설명 유효성 검사
                    const descriptionInput = document.getElementById('descriptionInput');
                    if (descriptionInput.value.trim() === '') {
                        alert('설명을 입력해주세요.');
                        return;
                    }

                    // 모임 장소 유효성 검사 (meetingPlaceName이 비어있는지 확인)
                    const meetingPlaceNameInput = document.getElementById('meetingPlaceName');
                    if (meetingPlaceNameInput.value.trim() === '') {
                        alert('모임 장소를 선택해주세요.');
                        return;
                    }

                    // 모임 날짜/시간 유효성 검사
                    const meetingDatetimeInput = document.getElementById('meetingDatetimeInput');
                    if (meetingDatetimeInput.value.trim() === '') {
                        alert('모임 날짜와 시간을 선택해주세요.');
                        return;
                    }
                    // 현재 시간보다 이전 날짜/시간 선택 방지 (선택 사항)
                    const selectedDateTime = new Date(meetingDatetimeInput.value);
                    const now = new Date();
                    if (selectedDateTime < now) {
                        alert('모임 날짜와 시간은 현재 시간보다 이전일 수 없습니다.');
                        return;
                    }

                    // 모든 유효성 검사 통과 후 데이터 수집
                    const formData = {
                        ownerIdx: parseInt(ownerIdxInput.value), // 반드시 숫자로 변환
                        groupCategoryMainIdx: selectedCategoryIdInput.value, // hidden input에서 가져옴
                        title: titleInput.value.trim(),
                        content: descriptionInput.value.trim(), // 백엔드 필드명에 맞춰 'content'로 변경
                        placeName: meetingPlaceNameInput.value.trim(),
                        placeCategory: meetingPlaceCategoryInput.value,
                        placeAddress: meetingPlaceAddressInput.value,
                        naverPlaceUrl: meetingPlaceLinkInput.value,
                        latitude: parseFloat(meetingPlaceLatInput.value), // 숫자로 변환
                        longitude: parseFloat(meetingPlaceLngInput.value), // 숫자로 변환
                        groupDate: meetingDatetimeInput.value, // 백엔드 필드명에 맞춰 'groupDate'로 변경 (ISO 8601 형식)
                        ageMin: minAge,
                        ageMax: maxAge,
                        membersMin: minMembers,
                        membersMax: maxMembers,
                        genderLimit: selectedGender
                    };

                    console.log("전송할 데이터:", formData); // 전송할 데이터 확인

                    // AJAX 요청 (Fetch API 사용)
                    fetch('/group/group_success', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json' // 서버로 JSON 데이터 전송
                        },
                        body: JSON.stringify(formData) // JavaScript 객체를 JSON 문자열로 변환
                    })
                    .then(response => {
                        if (!response.ok) { // HTTP 상태 코드가 200번대가 아니면 오류 처리
                            return response.text().then(text => { throw new Error(text) });
                        }
                        return response.json(); // 서버에서 JSON 응답을 기대
                    })
                    .then(data => {
                        console.log('서버 응답:', data);
                        // 성공적으로 방이 생성되었다면 다음 페이지로 리다이렉트
                        alert('방이 성공적으로 생성되었습니다!');
                        // 예시: data.groupId가 서버 응답에 있다면 상세 페이지로 이동
                        if (data && data.groupId) {
                            window.location.href = `/group/group_success`;
                        } else {
                            // 서버 응답에 groupId가 없으면 기본 성공 페이지로 이동
                            window.location.href = '/main';
                        }
                    })
                    .catch(error => {
                        console.error('방 생성 중 오류 발생:', error);
                        alert('방 생성에 실패했습니다: ' + error.message);
                    });
                });
            }
            /* 취소버튼 동작 */
            if (cancleButton) {
                cancleButton.addEventListener('click', async (e) => {
                    e.preventDefault();
                    window.location.href = '/main';
                });
            }

    // 페이지 로드 시 초기화
    initNaverMap();
    updateLivePreview();

  }
});
