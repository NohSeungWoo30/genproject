
document.addEventListener('DOMContentLoaded', function() {
    // 'card' 클래스를 가진 모든 요소를 가져옵니다.
    const cards = document.querySelectorAll('.card');

    // 각 카드에 클릭 이벤트 리스너를 추가합니다.
    cards.forEach(card => {
        card.addEventListener('click', function() {
            // 클릭된 카드에서 data-group-idx 속성 값을 가져옵니다.
            const groupIdx = this.dataset.groupIdx; // dataset으로 data-xxx 속성에 접근

            // groupIdx 값이 유효한지 확인합니다.
            if (groupIdx) {
                // 상세 페이지 URL을 생성하고 페이지를 이동합니다.
                // 예: /groups/detail?groupId=123
                window.location.href = '/group/detail?groupId=' + groupIdx;
            } else {
                console.warn('groupIdx not found for this card.');
            }
        });
    });
});
