$(function () {
    // --- Element Selection ---
    const $filterModal = $('#filter-modal');
    const $filterContent = $('#filter-content');
    const $resultsContent = $('#results-content');
    const $roomDetailModal = $('#room-detail-modal');

    // --- Modal Control Functions ---
    const openModal = () => {
        // Reset to the filter selection screen when opening
        $filterContent.removeClass('hidden');
        $resultsContent.addClass('hidden');
        $filterModal.removeClass('hidden');
    };
    const closeModal = () => $filterModal.addClass('hidden');
    const openRoomDetail = (title) => {
        $('#room-detail-title').text(title);
        $roomDetailModal.removeClass('hidden');
    }
    const closeRoomDetail = () => $roomDetailModal.addClass('hidden');

    // --- Time Calculation Functions ---
    // Formats the meeting time display (e.g., "오늘 14:30" or "6/14 18:00")
    function formatMeetingTime(date) {
        const today = new Date();
        const isToday = date.getDate() === today.getDate() &&
                        date.getMonth() === today.getMonth() &&
                        date.getFullYear() === today.getFullYear();
        const prefix = isToday ? '오늘' : `${date.getMonth() + 1}/${date.getDate()}`;
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${prefix} ${hours}:${minutes}`;
    }

    // Calculates the time remaining until a meeting
    function calculateTimeStatus(date) {
        const now = new Date();
        const diffMs = date - now;

        if (diffMs <= 0) return '모집 마감'; // Meeting time has passed
        const diffHours = Math.floor(diffMs / 3600000);
        if (diffHours > 0) return `${diffHours}시간 남음`;
        const diffMins = Math.floor(diffMs / 60000);
        if (diffMins > 0) return `${diffMins}분 남음`;
        return '마감 임박'; // Less than a minute left
    }

    // --- Filter UI Setup (Age Slider) ---
    const ageSlider = document.getElementById('age-slider');
    // Check if the slider element exists and hasn't been initialized
    if (ageSlider && !ageSlider.noUiSlider) {
        noUiSlider.create(ageSlider, {
            start: [20, 50],
            step: 5,
            connect: true, // Creates a colored bar between the handles
            range: { min: 20, max: 50 },
            format: { to: v => Math.round(v), from: v => Number(v) }
        });
        // Update display values when slider moves
        ageSlider.noUiSlider.on('update', (values) => {
            $('#minAgeDisplay').text(values[0]);
            $('#maxAgeDisplay').text(values[1]);
        });
    }

    // --- Filter Button Activation/Deactivation ---
    // Checks if at least one tag is selected to enable the apply button
    const checkButtonState = () => {
        const isTagSelected = $('.tag-item.active').length > 0;
        $('#matchButton').prop('disabled', !isTagSelected);
    };

    // Add click listener to tags to toggle their active state
    $('.tag-item').on('click', function () {
        $(this).toggleClass('active');
        checkButtonState();
    });
    // Initial check when the page loads
    checkButtonState();

    // --- Event Listeners ---
    $('#open-filter-btn').on('click', openModal);
    $('#close-filter-btn').on('click', closeModal);
    $('#back-to-filters-btn').on('click', () => {
        // Switch from results view back to filter view
        $resultsContent.addClass('hidden');
        $filterContent.removeClass('hidden');
    });

    // "Apply Filters" button click event
    $('#matchButton').on('click', function(e) {
        e.preventDefault();
        if ($(this).is(':disabled')) return;

        // 1. Collect filters
        const selectedCategoryIds = $('.tag-item.active').map((_, el) => $(el).data('id')).get();
        const ageRange = ageSlider.noUiSlider.get();
        const rawGender = $('input[name="gender"]:checked').val();

        // gender 문자열 → 코드값 변환
        const genderMap = {
            '누구나': 'A',
            '남자만': 'M',
            '여자만': 'F'
        };
        const gender = genderMap[rawGender];

        // 2. Display selected filters (문자 그대로)
        let filtersHtml = '';
        $('.tag-item.active').each((_, el) => {
            filtersHtml += `<span class="applied-filter-tag">${$(el).text()}</span>`;
        });
        filtersHtml += `<span class="applied-filter-tag">${ageRange[0]}-${ageRange[1]}세</span>`;
        filtersHtml += `<span class="applied-filter-tag">${rawGender}</span>`;
        $('#applied-filters-display').html(filtersHtml);

        // 3. Build query string with category ID list
        const queryParams = new URLSearchParams();
        selectedCategoryIds.forEach(id => queryParams.append('categories', id));
        queryParams.append('minAge', ageRange[0]);
        queryParams.append('maxAge', ageRange[1]);
        queryParams.append('gender', gender);

        // 4. Fetch request
        fetch(`/group/match?${queryParams.toString()}`)
            .then(res => res.json())
            .then(data => {
                $('#results-list').empty();

                data.forEach(room => {
                    const meetingDate = new Date(room.groupDate);
                    const timeStatus = calculateTimeStatus(meetingDate);
                    const meetingTimeFormatted = formatMeetingTime(meetingDate);

                    let timeStatusHtml = timeStatus === '모집 마감'
                        ? `<div class="time-status text-gray-500">${timeStatus}</div>`
                        : timeStatus === '마감 임박'
                            ? `<div class="time-status flex items-center justify-end gap-1 text-gray-800">
                                  <ion-icon name="flame-outline" class="text-rose-500"></ion-icon>
                                  <span>${timeStatus}</span>
                               </div>`
                            : `<div class="time-status text-gray-800">${timeStatus}</div>`;

                    const roomHtml = `
                      <div class="result-item-card" data-title="${room.title}" data-group-id="${room.groupIdx}">
                        <div class="flex items-start gap-4">
                          <div class="card-img-placeholder"></div>
                          <div class="flex-1">
                            <h3 class="font-bold text-lg mb-1">${room.title}</h3>
                            <div class="flex items-center gap-1 text-gray-500 text-sm">
                              <ion-icon name="people"></ion-icon>
                              <span>${room.partyMember} / ${room.membersMax}명</span>
                            </div>
                          </div>
                          <div class="text-right flex-shrink-0">
                            ${timeStatusHtml}
                            <div class="meeting-time text-sm text-gray-500 mt-1">${meetingTimeFormatted}</div>
                          </div>
                        </div>
                      </div>`;
                    $('#results-list').append(roomHtml);
                });

                $filterContent.addClass('hidden');
                $resultsContent.removeClass('hidden');
            })
            .catch(err => {
                console.error("매칭 결과 조회 실패:", err);
                alert("조건에 맞는 모임을 찾는 데 실패했습니다.");
            });
    });

    // 결과 목록에서 항목 클릭 시 상세 모달 오픈
    $('#results-list').on('click', '.result-item-card', function () {
        const groupId = $(this).data('group-id');

        // 필터 모달 먼저 닫기
        $('#filter-modal').addClass('hidden');

        // 1. 상세 모달 열기
        $('#group-detail-modal').removeClass('hidden');

        // 2. groupId 전역 저장
        window.groupId = groupId;

        // 3. 그룹 정보 요청
        fetch(`/group/api/groups/detail/${groupId}`)
            .then(res => {
                if (!res.ok) throw new Error("그룹 데이터 응답 실패");
                return res.json(); // 여기서 실제 groupData를 받음
            })
            .then(groupData => {
                console.log("groupData 수신", groupData);

                // 방 정보 저장
                window.room = groupData;

                // 참가 여부 판단해서 저장
                window.isChatJoined = groupData.participants?.some(p => p.name === window.currentLoggedInUser?.name);

                // 상세 내용 렌더링
               // 함수 존재 여부 확인 후 안전하게 실행
                 if (typeof displayRoomDetails === 'function') {
                   displayRoomDetails();
                 } else {
                   console.warn("⚠ displayRoomDetails 함수가 아직 정의되지 않았습니다.");
                 }
            });
    });

    // Close the detail modal
    $('#close-room-detail-btn').on('click', closeRoomDetail);
});


