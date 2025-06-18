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
        if ($(this).is(':disabled')) return; // Do nothing if button is disabled

        // 1. Collect and display the applied filter information
        const selectedTags = $('.tag-item.active').map((_, el) => $(el).data('tag')).get();
        const ageRange = ageSlider.noUiSlider.get();
        const gender = $('input[name="gender"]:checked').val();

        let filtersHtml = '';
        selectedTags.forEach(tag => filtersHtml += `<span class="applied-filter-tag">${tag}</span>`);
        filtersHtml += `<span class="applied-filter-tag">${ageRange[0]}-${ageRange[1]}세</span>`;
        filtersHtml += `<span class="applied-filter-tag">${gender}</span>`;
        $('#applied-filters-display').html(filtersHtml);

        // 2. Generate and render mock result data
        // NOTE: In a real application, this would be an API call
        const now = new Date();
        const sampleRooms = [
            { name: '저녁 함께 달릴 사람!', members: '5/8명', dateTime: new Date(now.getTime() + 3 * 60 * 60 * 1000) },
            { name: '주말 보드게임 모임', members: '3/6명', dateTime: new Date(now.getTime() + 25 * 60 * 60 * 1000) },
            { name: '코딩 스터디 모집해요', members: '2/5명', dateTime: new Date(now.getTime() + 30 * 60 * 1000) },
            { name: '맛집 탐방하실 분 (마감)', members: '8/8명', dateTime: new Date(now.getTime() - 1 * 60 * 60 * 1000) },
        ];

        $('#results-list').empty(); // Clear previous results
        sampleRooms.forEach(room => {
            const meetingDate = new Date(room.dateTime);
            const timeStatus = calculateTimeStatus(meetingDate);
            const meetingTimeFormatted = formatMeetingTime(meetingDate);

            let timeStatusHtml;
            if (timeStatus === '모집 마감') {
                timeStatusHtml = `<div class="time-status text-gray-500">${timeStatus}</div>`;
            } else if (timeStatus === '마감 임박') {
                timeStatusHtml = `
                    <div class="time-status flex items-center justify-end gap-1 text-gray-800">
                        <ion-icon name="flame-outline" class="text-rose-500"></ion-icon>
                        <span>${timeStatus}</span>
                    </div>`;
            } else {
                timeStatusHtml = `<div class="time-status text-gray-800">${timeStatus}</div>`;
            }

            // Construct the HTML for each result item
            const roomHtml = `
              <div class="result-item-card" data-title="${room.name}">
                <div class="flex items-start gap-4">
                  <div class="card-img-placeholder"></div>
                  <div class="flex-1">
                    <h3 class="font-bold text-lg mb-1">${room.name}</h3>
                    <div class="flex items-center gap-1 text-gray-500 text-sm">
                      <ion-icon name="people"></ion-icon>
                      <span>${room.members}</span>
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

        // 3. Switch views from filters to results
        $filterContent.addClass('hidden');
        $resultsContent.removeClass('hidden');
    });

    // Open detail modal when a result item is clicked
    $('#results-list').on('click', '.result-item-card', function() {
        const title = $(this).data('title');
        openRoomDetail(title);
    });

    // Close the detail modal
    $('#close-room-detail-btn').on('click', closeRoomDetail);
});
