// script.js

// ──────────────────────────────────────────────────────
    // --- 전역 변수 선언 ---
    let map = null; // 지도 객체
    let markers = []; // 마커 배열
    let placeResultList = null; // placeResultList 요소를 저장할 변수

    // 폼 필드 참조 (initMap에서 할당)
    let meetingPlaceNameInput = null; <!-- 장소명 -->
    let meetingPlaceLinkDisplay = null; <!-- 연결 링크 -->
    let meetingPlaceLinkInput = null; <!-- 히든 링크 -->
    let meetingPlaceCategoryInput = null; <!-- 분류 -->
    let meetingPlaceContentInput = null; <!-- 설명 -->
    let meetingPlaceAddressInput = null; <!-- 주소(도로명) -->
    let meetingPlaceLatInput = null;
    let meetingPlaceLngInput = null;
    let searchPlaceInput = null;
    let searchPlaceButton = null;

    // --- 헬퍼 함수 ---

    // 마커 지우는 함수
    function clearMarkers() {
        for (let i = 0; i < markers.length; i++) {
            markers[i].setMap(null);
        }
        markers = [];
    }

    // 장소 선택 시 폼 필드 업데이트 함수
    function selectPlaceFromSearch(title, link, category, description, address, lat, lng) {
        console.log("선택된 장소:", title, address, lat, lng);
        meetingPlaceNameInput.value = title; // 'name' 대신 'title' 사용

        // ===== 링크 필드 업데이트 로직 (링크값 저장하는 A, Input 업데이트) =====
        if (link) {
            meetingPlaceLinkDisplay.href = link;
            meetingPlaceLinkDisplay.textContent = link;
            meetingPlaceLinkDisplay.style.display = 'inline-block'; // <a> 태그 표시
            meetingPlaceLinkInput.style.display = 'none'; // input 태그 숨김
        } else {
            // 링크가 없을 경우 <a>는 숨기고 input을 보여줌 (비어있는 input 상태)
            meetingPlaceLinkDisplay.href = '#'; // 링크가 없을 때 #으로 설정 (클릭해도 이동 안 함)
            meetingPlaceLinkDisplay.textContent = ''; // <a> 태그 텍스트 초기화
            meetingPlaceLinkDisplay.style.display = 'none'; // <a> 태그 숨김

            meetingPlaceLinkInput.style.display = 'inline-block'; // input 태그 표시
            meetingPlaceLinkInput.value = '링크 없음'; // input에 "링크 없음" 표시
        }

        meetingPlaceLinkInput.value = link;
        meetingPlaceCategoryInput.value = category;
        meetingPlaceContentInput.value = description;
        meetingPlaceAddressInput.value = address;
        meetingPlaceLatInput.value = lat;
        meetingPlaceLngInput.value = lng;
        alert(`선택된 장소: ${title}\n주소: ${address}\n위도: ${lat}, 경도: ${lng}`);
        map.setCenter(new naver.maps.LatLng(lat, lng)); // 선택된 장소로 지도 중심 이동
    }

    // --- 지도 및 UI 초기화 함수 (네이버 API callback으로 호출) ---
    function initMap() {
        // DOM 요소 참조 가져오기
        placeResultList = document.getElementById('placeResultList');
        meetingPlaceNameInput = document.getElementById('meetingPlaceName');
        meetingPlaceLinkDisplay = document.getElementById('meetingPlaceLinkDisplay');
        meetingPlaceLinkInput = document.getElementById('meetingPlaceLink');
        meetingPlaceCategoryInput = document.getElementById('meetingPlaceCategory');
        meetingPlaceContentInput = document.getElementById('meetingPlaceContent');
        meetingPlaceAddressInput = document.getElementById('meetingPlaceAddress');
        meetingPlaceLatInput = document.getElementById('meetingPlaceLat');
        meetingPlaceLngInput = document.getElementById('meetingPlaceLng');
        searchPlaceInput = document.getElementById('searchPlaceInput'); <!--지도 장소 검색용 필드-->
        searchPlaceButton = document.getElementById('searchPlaceButton'); <!--지도 장소 검색버튼-->

        // 지도 초기화
        map = new naver.maps.Map('map', {
            center: new naver.maps.LatLng(37.5665, 126.9780), // 서울 시청으로 초기화
            zoom: 15,
            zoomControl: true,
            zoomControlOptions: {
                position: naver.maps.Position.TOP_RIGHT
            }
        });

        // --- 이벤트 리스너 설정 ---

        // 검색 버튼 클릭 이벤트 리스너
        if (searchPlaceButton) {
            searchPlaceButton.addEventListener('click', function() {
                const query = searchPlaceInput.value; // 올바른 변수 사용
                if (query) {
                    searchPlace(query);
                } else {
                    alert('검색어를 입력해주세요.');
                }
            });
        }

        // 검색 결과 목록 (placeResultList) 클릭 이벤트 위임 리스너
        if (placeResultList) {
            placeResultList.addEventListener('click', function(event) {
                const clickedLi = event.target.closest('li.place-result-item'); // 가장 가까운 li.place-result-item 찾기
                if (clickedLi) {
                // data-* 속성에서 값을 가져올 때, 없을 경우 빈 문자열로 초기화하여 안전성 확보
                    const title = clickedLi.dataset.title || '';
                    const link = clickedLi.dataset.link || '';
                    const category = clickedLi.dataset.category || '';
                    const description = clickedLi.dataset.description || '';
                    const address = clickedLi.dataset.address || '';
                    const lat = parseFloat(clickedLi.dataset.lat);
                    const lng = parseFloat(clickedLi.dataset.lng);
                    selectPlaceFromSearch(title, link, category, description, address, lat, lng);
                }
            });
        }

        // --- 기타 UI 초기화 (DOMContentLoaded에 있던 내용들을 initMap으로 이동) ---

        // 카테고리 스크립트
        const mainCategorySelect = document.getElementById('groupCategoryMainIdx');
        const subCategorySelect = document.getElementById('groupCategorySubIdx');

        mainCategorySelect.addEventListener('change', function() {
            const selectedMainCategoryIdx = this.value;
            subCategorySelect.innerHTML = '<option value="">-- 소분류 선택 --</option>';
            subCategorySelect.disabled = true;

            if (selectedMainCategoryIdx) {
                fetch(`/group/api/sub-categories?mainCategoryIdx=${selectedMainCategoryIdx}`)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Network response was not ok ' + response.statusText);
                        }
                        return response.json();
                    })
                    .then(subCategories => {
                        subCategories.forEach(category => {
                            const option = document.createElement('option');
                            option.value = category.categorySubIdx;
                            option.textContent = category.categorySubName;
                            subCategorySelect.appendChild(option);
                        });
                        subCategorySelect.disabled = false;
                    })
                    .catch(error => {
                        console.error('Error fetching sub categories:', error);
                        alert('서브 카테고리를 불러오는 데 실패했습니다.');
                        subCategorySelect.disabled = true;
                    });
            } else {
                subCategorySelect.disabled = true;
            }
        });

        // 폼 제출 시 유효성 검사
        const form = document.querySelector('form');
        if (form) {
            form.addEventListener('submit', function(e) {
                const genderRadios = document.getElementsByName('genderLimit');
                let isGenderSelected = false;
                for (const radio of genderRadios) {
                    if (radio.checked) {
                        isGenderSelected = true;
                        break;
                    }
                }

                if (!isGenderSelected) {
                    e.preventDefault();
                    alert('성별 제한을 선택해주세요.');
                    return;
                }

                const minMembersSelect = document.getElementById('membersMin');
                const maxMembersSelect = document.getElementById('membersMax');

                const minMembers = parseInt(minMembersSelect.value);
                const maxMembers = parseInt(maxMembersSelect.value);

                if (minMembersSelect.value === "" || maxMembersSelect.value === "") {
                    e.preventDefault();
                    alert('최소/최대 멤버 수를 선택해주세요.');
                    return;
                }

                if (minMembers > maxMembers) {
                    e.preventDefault();
                    alert('최소 멤버 수는 최대 멤버 수보다 클 수 없습니다.');
                    return;
                }
            });
        }


        // 나이 슬라이더 스크립트
        const ageSlider = document.getElementById('age-slider');
        const ageMinInput = document.getElementById('ageMin');
        const ageMaxInput = document.getElementById('ageMax');
        const ageMinDisplay = document.getElementById('ageMinDisplay');
        const ageMaxDisplay = document.getElementById('ageMaxDisplay');

        if (ageSlider && ageMinInput && ageMaxInput && ageMinDisplay && ageMaxDisplay) {
            noUiSlider.create(ageSlider, {
                start: [parseInt(ageMinInput.value), parseInt(ageMaxInput.value)],
                connect: true,
                range: {
                    'min': 0,
                    'max': 100
                },
                step: 1,
                format: {
                    to: function (value) {
                        return parseInt(value);
                    },
                    from: function (value) {
                        return parseInt(value);
                    }
                }
            });

            ageSlider.noUiSlider.on('update', function (values, handle) {
                const minAge = values[0];
                const maxAge = values[1];
                ageMinInput.value = minAge;
                ageMaxInput.value = maxAge;
                ageMinDisplay.textContent = `${minAge}세`;
                ageMaxDisplay.textContent = `${maxAge}세`;
            });

            // 폼 로드 시 숨겨진 input 값과 화면 표시 동기화 (초기화)
            ageMinDisplay.textContent = `${ageMinInput.value}세`;
            ageMaxDisplay.textContent = `${ageMaxInput.value}세`;
        }


        // 최소/최대 멤버 수 옵션 동적 생성
        const minMembersSelect = document.getElementById('membersMin');
        const maxMembersSelect = document.getElementById('membersMax');

        if (minMembersSelect && maxMembersSelect) {
            const membersCountOptions = [2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30];

            function populateSelectOptions(selectElement) {
                membersCountOptions.forEach(count => {
                    const option = document.createElement('option');
                    option.value = count;
                    option.textContent = `${count}명`;
                    selectElement.appendChild(option);
                });
            }

            populateSelectOptions(minMembersSelect);
            populateSelectOptions(maxMembersSelect);
        }
    } // --- initMap 함수 끝 ---


    // --- 키워드를 이용해 장소 검색 (백엔드 프록시 경유) ---
    async function searchPlace(query) {
        clearMarkers();
        placeResultList.innerHTML = ''; // 기존 검색 결과 초기화

        // 1. 현재 지도 화면의 영역(Bounds) 정보 가져오기
        const bounds = map.getBounds();
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
                map.setCenter(firstItemPoint);

                const currentBounds = map.getBounds(); // 현재 지도의 가시 영역 (Bounds) 가져오기
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
                            map: map,
                            title: title
                        });
                        markers.push(marker);

                        const li = document.createElement('li');
                        li.innerHTML = `<strong>${title}</strong><br>${address}`;
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
                    placeResultList.innerHTML = '<li>현재 지도 화면 내에 검색 결과가 없습니다.</li>';
                }

            } else {
                placeResultList.innerHTML = '<li>검색 결과가 없습니다.</li>';
            }
        } catch (error) {
            console.error('검색 중 오류 발생:', error);
            alert('장소 검색에 실패했습니다.');
        }
    } // async function searchPlace 끝

    // --- 좌표를 이용해 장소 검색 (역지오코딩) ---
    // 이 함수는 현재 HTML에서 직접 호출되지 않지만, 유지해 두었습니다.
    function searchPlaceByCoordinates(lat, lng) {
        if (typeof naver.maps.Service === 'undefined' || typeof naver.maps.Service.reverseGeocode === 'undefined') {
            console.error("네이버 지도 Geocoder 서비스 또는 요청 상수가 아직 완전히 로드되지 않았습니다.");
            return;
        }

        const coordinate = new naver.maps.LatLng(lat, lng);
        naver.maps.Service.reverseGeocode({
            coords: coordinate,
            orders: [
                naver.maps.Service.Order.ROAD_ADDR,
                naver.maps.Service.Order.ADDR
            ]
        }, function(status, response) {
            if (status === naver.maps.Service.Status.OK) {
                let address = response.v2.address.roadAddress || response.v2.address.jibunAddress;
                let placeName = `선택된 위치 (${address})`;

                clearMarkers(); // 기존 마커 제거
                let newMarker = new naver.maps.Marker({
                    position: coordinate,
                    map: map
                });
                markers.push(newMarker);

                meetingPlaceNameInput.value = placeName;
                meetingPlaceAddressInput.value = address;
                meetingPlaceLatInput.value = lat;
                meetingPlaceLngInput.value = lng;

                alert(`선택된 장소: ${address}\n위도: ${lat}, 경도: ${lng}`);

            } else {
                alert('역지오코딩 실패: ' + response.status);
                console.error('역지오코딩 실패:', status, response);
            }
        });
    }

