document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.card').forEach($card => {
    $card.addEventListener('click', () => {
      const groupIdx = $card.dataset.groupIdx;
      if (!groupIdx) return;

      /* ① 전체 새로고침 대신 커스텀 이벤트 발행 */
      document.dispatchEvent(
        new CustomEvent('open-group-detail', { detail: { groupIdx } })
      );
    });
  });
});