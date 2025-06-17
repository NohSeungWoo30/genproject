/* 스크립트: region 토글, 듀얼 슬라이더, tag 관리*/
 // region selector 토글
  const btnRegion = document.getElementById('btn-region');
  const panelRegion = document.getElementById('filter-region-panel');
  btnRegion.addEventListener('click', () => {
    panelRegion.style.display = panelRegion.style.display === 'none' ? 'block' : 'none';
  });

  // 캘린더 달력
  const dateSelect = document.getElementById('date-input');

  // 듀얼 슬라이더 (나이)
  const sliderMin = document.getElementById('age-min');
  const sliderMax = document.getElementById('age-max');
  const ageValueText = document.getElementById('age-value');
  const STEP = 5;

  function updateAgeText() {
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
    updateAgeText();
  });

  sliderMax.addEventListener('input', () => {
    let minVal = parseInt(sliderMin.value);
    let maxVal = parseInt(sliderMax.value);
    if (maxVal <= minVal) {
      maxVal = minVal + STEP;
      sliderMax.value = maxVal;
    }
    updateAgeText();
  });

  updateAgeText();

  // 태그 관리(지역 필터에서 선택된 항목)

  const sidoListEl = document.getElementById('sido-list');
  const sigunguListEl = document.getElementById('sigungu-list');
  const dongListEl = document.getElementById('dong-list');
  const selectedTagsEl = document.getElementById('selected-tags');
  const searchInput = document.getElementById('search-input');

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

  searchInput.addEventListener('input', () => {
    const query = searchInput.value.trim();
    if (!query) {
      renderSidoList();
      return;
    }
    sidoListEl.innerHTML = '';
    Object.keys(regionData).forEach(sido => {
      if (sido.includes(query)) {
        const li = document.createElement('li');
        li.textContent = sido;
        li.addEventListener('click', () => selectSido(sido));
        sidoListEl.appendChild(li);
      } else {
        let match = false;
        Object.keys(regionData[sido]).forEach(sigungu => {
          if (sigungu.includes(query)) match = true;
          regionData[sido][sigungu].forEach(dong => {
            if (dong.includes(query)) match = true;
          });
        });
        if (match) {
          const li = document.createElement('li');
          li.textContent = sido;
          li.addEventListener('click', () => selectSido(sido));
          sidoListEl.appendChild(li);
        }
      }
    });
  });

  // 초기 지역 렌더링
  renderSidoList();
  sigunguListEl.innerHTML = '<li>시/도를 먼저 선택하세요</li>';
  dongListEl.innerHTML = '<li>시/구군을 먼저 선택하세요</li>';