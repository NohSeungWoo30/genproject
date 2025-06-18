document.addEventListener('DOMContentLoaded', function() {
    // 필터 모달
    const filterModal = document.getElementById('filter-modal');
    const openFilterModalBtn = document.getElementById('open-filter-modal'); // 모달창 오픈 버튼
    const closeButton = document.getElementById('close-button');
    const applyFilterButton = document.getElementById('apply-filter-button');

    console.log("Open button:", openFilterModalBtn);
          console.log("Close button:", closeButton);
          console.log("Apply button:", applyFilterButton);
 // region selector 토글
  const btnRegion = document.getElementById('btn-region');
  const panelRegion = document.getElementById('filter-region-panel');
  btnRegion.addEventListener('click', () => {
    panelRegion.style.display = panelRegion.style.display === 'none' ? 'block' : 'none';
  });

  // 듀얼 슬라이더 (나이)
    const sliderMin = document.getElementById('age-min');
    const sliderMax = document.getElementById('age-max');
    const ageValueText = document.getElementById('age-value');
    const STEP = 5;
     // 인원 수
     const selectedCountTag = document.getElementById('count-select');
     // 카테고리
     const selectedCategoryTag = document.getElementById('category-input');

  // 캘린더 달력
  const selectedDate = document.getElementById('date-input');

    // 태그 관리(지역 필터에서 선택된 항목)
      const sidoListEl = document.getElementById('sido-list');
      const sigunguListEl = document.getElementById('sigungu-list');
      const dongListEl = document.getElementById('dong-list');
      const selectedTagsEl = document.getElementById('selected-tags');

  function updateAgeDisplay() {
    const minVal = parseInt(sliderMin.value);
    const maxVal = parseInt(sliderMax.value);
    ageValueText.textContent = `${minVal}세 - ${maxVal}세`;
  }

  sliderMin.addEventListener('input', () => {
    let minVal = parseInt(sliderMin.value);
    let maxVal = parseInt(sliderMax.value);
    if (minVal >= maxVal) {
      minVal = maxVal - STEP;
      sliderMin.value = minVal;
    }
    updateAgeDisplay();
  });

  sliderMax.addEventListener('input', () => {
    let minVal = parseInt(sliderMin.value);
    let maxVal = parseInt(sliderMax.value);
    if (maxVal <= minVal) {
      maxVal = minVal + STEP;
      sliderMax.value = maxVal;
    }
    updateAgeDisplay();
  });

  updateAgeDisplay();

          // "검색 필터 열기" 버튼 클릭 시 모달 열기
            openFilterModalBtn.addEventListener('click', function() {
                filterModal.classList.add('show-modal');
            });

        // 닫기 버튼 클릭 시 모달 닫기
            closeButton.addEventListener('click', function() {
                filterModal.classList.remove('show-modal');
            });

            // 모달 외부 클릭 시 모달 닫기
            window.addEventListener('click', function(event) {
                if (event.target == filterModal) {
                    filterModal.classList.remove('show-modal');
                }
            });

    // "필터 적용" 버튼 클릭 시 (여기서 실제 검색 로직을 구현)
        applyFilterButton.addEventListener('click', function() {
            // 1. 모달 닫기
            filterModal.classList.remove('show-modal');

            // 2. 선택된 필터 값 가져오기
            const selectedDateValue = selectedDate.value;
            /*const selectedRegion = currentSido || "전체 지역"; // 시도 시군구 동읍면*/
            let selectedRegionText = '';
            if (selectedFilters.size > 0) {
                    selectedRegionText = Array.from(selectedFilters).join(', ');
                } else {
                    selectedRegionText = "전체 지역"; // 아무것도 선택되지 않았다면 "전체 지역"
                }

            const minAge = sliderMin.value;
            const maxAge = sliderMax.value;
            const selectedCount = selectedCountTag.value;
            const selectedCategory = selectedCategoryTag.value; // 카테고리 ID

            console.log('선택된 필터 값:', {
                date: selectedDateValue,
                region: selectedRegionText,   // 최종 지역 정보
                minAge: minAge,
                maxAge: maxAge,
                count: selectedCount,
                category: selectedCategory
            });

            const parts = selectedCount.split('-');

            // 이 데이터를 사용하여 실제 검색 (예: AJAX 요청, 페이지 리로드 등)
            $.ajax({
                url: '/group/api/search',
                type: 'POST', // 'POST' 또는 'GET'
                contentType: 'application/json', // JSON
                data: JSON.stringify({ // JavaScript 객체를 JSON 문자열로 변환
                    groupDate: selectedDateValue,
                    region: selectedRegionText,
                    minAge: minAge,
                    maxAge: maxAge,
                    minParticipants: parseInt(parts[0]),
                    maxParticipants: parseInt(parts[1]),
                    categoryMainIdx: selectedCategory
                }),
                dataType: 'json', // 서버로부터 받을 응답 데이터 타입
                success: function(response) {
                    console.log('Success:', response);
                    // 서버에서 받은 검색 결과에 접근
                    const searchResults = response.searchResults;

                    const meetingGrid = document.querySelector('.meeting-grid');
                    meetingGrid.innerHTML = ''; // 기존 모임 카드 비우기

                    if (searchResults && searchResults.length > 0) {
                        searchResults.forEach(group => {

                        const imageUrl = (group.groupImgUrl && group.groupImgUrl.trim() !== '') ? group.groupImgUrl : '/img/noImage.jpg';
                        const categoryName = group.categoryMain ? group.categoryMain.categoryMainName : 'N/A';

                        const groupDate = new Date(group.groupDate); // ISO 8601 문자열 (YYYY-MM-DDTHH:mm:ss)로 온다면 Date 객체로 변환
                        const options = {
                            year: 'numeric', month: '2-digit', day: '2-digit',
                            weekday: 'short', // 요일 (Mon, Tue 등)
                            hour: '2-digit', minute: '2-digit', hour12: false // 24시간 형식
                        };
                        // 한국어 요일을 위해 'ko-KR' 로케일 사용
                        const formattedDate = groupDate.toLocaleDateString('ko-KR', options).replace(/\./g, '.').replace('요일', ''); // '.MM.dd(요일)' 형태

                        const groupCard = `
                            <div data-group-idx="${group.groupIdx}" class="card">
                                <div class="card-thumb">
                                    <img src="${imageUrl}" alt="${group.title} 이미지" />
                                </div>
                            <div class="card-info">
                                <div class="card-category">
                                    <span>${group.district || '지역 미지정'}</span> · <span>${categoryName}</span>
                                </div>
                                    <div class="card-title">${group.title}</div>
                                    <div class="card-date">${formattedDate}</div>
                                </div>
                            </div>
                            `;
                            meetingGrid.innerHTML += groupCard; // 새 카드 추가
                        });
                    }else {
                        console.log("검색 결과 없음.");
                        const meetingGrid = document.querySelector('.meeting-grid');
                        meetingGrid.innerHTML = '<p>해당 필터에 맞는 모임이 없습니다.</p>';
                    }
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    console.error('Error:', textStatus, errorThrown, jqXHR.responseText);
                }
            });
            /*alert('필터가 적용되었습니다! (콘솔 확인)');*/
        });


  let currentSido = null;
  let currentSigungu = null;
  const selectedDongs = new Set();
  const selectedParents = new Set();
  const selectedFilters = new Set();

  function renderSidoList() {
    sidoListEl.innerHTML = '';
    Object.keys(regionData).forEach(sido => {
      const li = document.createElement('li');
      li.textContent = sido;
      li.addEventListener('click', () => selectSido(sido));
      if (selectedParents.has(`${sido} 전체`)) li.classList.add('selected');
      else li.classList.remove('selected');
      sidoListEl.appendChild(li);
    });
  }

  function selectSido(sido) {
    currentSido = sido;
    currentSigungu = null;
    renderSidoList();
    renderSigunguList(sido);
    dongListEl.innerHTML = '<li>시/구군을 먼저 선택하세요</li>';
  }

  function renderSigunguList(sido) {
    sigunguListEl.innerHTML = '';
    const allSidoLi = document.createElement('li');
    allSidoLi.textContent = `${sido} 전체`;
    allSidoLi.addEventListener('click', () => selectEntireSido(sido));
    if (selectedParents.has(`${sido} 전체`)) allSidoLi.classList.add('selected');
    else allSidoLi.classList.remove('selected');
    sigunguListEl.appendChild(allSidoLi);

    Object.keys(regionData[sido]).forEach(sigungu => {
      const li = document.createElement('li');
      li.textContent = sigungu;
      li.addEventListener('click', () => selectSigungu(sido, sigungu));
      if (selectedParents.has(`${sigungu} 전체`)) li.classList.add('selected');
      else li.classList.remove('selected');
      sigunguListEl.appendChild(li);
    });
  }

  function selectSigungu(sido, sigungu) {
    currentSigungu = sigungu;
    selectedParents.clear();
    selectedParents.add(`${sigungu} 전체`);
    renderSidoList();
    renderSigunguList(sido);
    renderDongList(sido, sigungu);
    updateRegionTags();
  }

  function renderDongList(sido, sigungu) {
    dongListEl.innerHTML = '';
    const allSigunguLi = document.createElement('li');
    allSigunguLi.textContent = `${sigungu} 전체`;
    allSigunguLi.addEventListener('click', () => selectEntireSigungu(sido, sigungu));
    if (selectedParents.has(`${sigungu} 전체`)) allSigunguLi.classList.add('selected');
    else allSigunguLi.classList.remove('selected');
    dongListEl.appendChild(allSigunguLi);

    regionData[sido][sigungu].forEach(dong => {
      const li = document.createElement('li');
      li.textContent = dong;
      li.addEventListener('click', () => toggleDong(dong));
      if (selectedDongs.has(dong)) li.classList.add('selected');
      else li.classList.remove('selected');
      dongListEl.appendChild(li);
    });
  }

  function selectEntireSido(sido) {
    selectedParents.clear();
    selectedParents.add(`${sido} 전체`);
    selectedDongs.clear();
    renderSidoList();
    renderSigunguList(sido);
    dongListEl.innerHTML = '<li>시/구군을 먼저 선택하세요</li>';
    updateRegionTags();
  }

  function selectEntireSigungu(sido, sigungu) {
    selectedParents.clear();
    selectedParents.add(`${sigungu} 전체`);
    selectedDongs.clear();
    renderSidoList();
    renderSigunguList(sido);
    renderDongList(sido, sigungu);
    updateRegionTags();
  }

  function toggleDong(dong) {
    selectedParents.clear();
    if (selectedDongs.has(dong)) selectedDongs.delete(dong);
    else selectedDongs.add(dong);
    if (currentSido) renderSidoList();
    if (currentSido) renderSigunguList(currentSido);
    if (currentSido && currentSigungu) renderDongList(currentSido, currentSigungu);
    updateRegionTags();
  }

  function updateRegionTags() {
    // 기존 지역 태그 제거
    [...selectedFilters].forEach(label => {
      if (label.includes('전체') || selectedDongs.has(label)) {
        selectedFilters.delete(label);
      }
    });
    selectedParents.forEach(parent => selectedFilters.add(parent));
    selectedDongs.forEach(dong => selectedFilters.add(dong));
    renderSelectedTags();
  }

  function renderSelectedTags() {
    selectedTagsEl.innerHTML = '';
    selectedFilters.forEach(label => {
      const tag = document.createElement('div');
      tag.className = 'tag';
      tag.textContent = label;
      const removeBtn = document.createElement('span');
      removeBtn.className = 'remove-btn';
      removeBtn.textContent = '×';
      removeBtn.addEventListener('click', () => {
        selectedFilters.delete(label);
        renderSidoList();
        if (currentSido) renderSigunguList(currentSido);
        if (currentSido && currentSigungu) renderDongList(currentSido, currentSigungu);
        renderSelectedTags();
      });
      tag.appendChild(removeBtn);
      selectedTagsEl.appendChild(tag);
    });
  }



  // 초기 지역 렌더링
  renderSidoList();
  sigunguListEl.innerHTML = '<li>시/도를 먼저 선택하세요</li>';
  dongListEl.innerHTML = '<li>시/구군을 먼저 선택하세요</li>';

});