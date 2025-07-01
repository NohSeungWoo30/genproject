function formatTime(timestamp) {
  const date = new Date(timestamp);
  return date.toLocaleString(); // 예: 2025.06.13. 오후 2:30
}


// DOMContentLoaded 이후 실행
document.addEventListener('DOMContentLoaded', () => {
  // 수정된 로그인 여부 확인 방식
    const isLoggedIn = window.isLoggedIn === true;
    console.log("isLoggedIn:", window.isLoggedIn);

    const userId = window.userId || 0;

    if (!isLoggedIn) return;  // 로그인 안 되어 있으면 아무 작업 안 함

  // --- 상태 및 데이터 (예시 데이터. 실제 사용 시 WebSocket 또는 API로 대체) ---
  const state = {
    profileImg: 'https://placehold.co/80x80/2563eb/ffffff/png?text=User',
    notifications: [] // 서버에서 받은 알림 데이터가 이곳에 채워짐
  };

  // --- DOM 요소 캐싱 ---
  const navRight = document.getElementById('navRight');
  const templates = {
    navLoggedIn: document.getElementById('nav-loggedin-template'),
    notiCard: document.getElementById('noti-card-template'),
  };

  let dom = {}; // 동적 DOM 요소들 보관

  function renderNav() {
    const fragment = templates.navLoggedIn.content.cloneNode(true);
    fragment.querySelector('.profile-thumb img').src = state.profileImg;
    navRight.innerHTML = '';
    navRight.appendChild(fragment);

    // 팝오버 강제 닫힌 상태로 시작
    const popover = document.getElementById('noti-popover');
    if (popover) popover.style.display = 'none';

    dom = {
      bellBtn: document.getElementById('noti-bell'),
      popover: document.getElementById('noti-popover'),
      listBox: document.getElementById('noti-list'),
      badge: document.getElementById('noti-badge'),
      markAllBtn: document.getElementById('mark-all-read-btn'),
      deleteReadBtn: document.getElementById('delete-read-btn'),
      profileBtn: document.getElementById('profileBtn'),
    };
  }

  function renderNotiList() {
    if (!dom.listBox) return;
    dom.listBox.innerHTML = '';

    if (state.notifications.length === 0) {
      dom.listBox.innerHTML = '<div class="py-20 text-center text-gray-400">알림이 없습니다</div>';
      updateBadge();
      return;
    }

    const fragment = document.createDocumentFragment();
    state.notifications.forEach(n => {
      const card = templates.notiCard.content.cloneNode(true);
      const cardRoot = card.querySelector('.noti-card');

      cardRoot.dataset.id = n.id;
      cardRoot.dataset.read = !n.unread;
      cardRoot.dataset.href = n.href || '#';
      if (!n.unread) cardRoot.classList.add('read');

      const iconContainer = card.querySelector('.noti-icon');
      iconContainer.classList.add(n.iconClass);
      iconContainer.querySelector('ion-icon').name = n.icon;

      card.querySelector('.noti-msg').innerHTML = n.msg;
      card.querySelector('.noti-time').textContent = n.time;

      fragment.appendChild(card);
    });
    dom.listBox.appendChild(fragment);
    updateBadge();
  }

  function updateBadge() {
    if (!dom.badge) return;
    const unreadCount = state.notifications.filter(n => n.unread).length;
    if (unreadCount > 0) {
      dom.badge.style.display = 'block';
      dom.badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
    } else {
      dom.badge.style.display = 'none';
    }
  }

  function setupEventListeners() {
    document.addEventListener('click', (e) => {
      if (e.target.closest('#noti-bell')) {
        dom.popover.style.display = dom.popover.style.display === 'block' ? 'none' : 'block';
      } else if (!e.target.closest('.noti-bell-btn')) {
        dom.popover.style.display = 'none';
      }
    });

    dom.profileBtn.addEventListener('click', () => {
      window.location.href = '/mypage';
    });

    dom.listBox.addEventListener('click', (e) => {
      const card = e.target.closest('.noti-card');
      if (!card) return;
      const notiId = parseInt(card.dataset.id, 10);

      if (e.target.closest('.dismiss-btn')) {

        // 서버에 삭제 처리 요청
        fetch(`/api/notifications/${notiId}`, { method: 'DELETE' });

        state.notifications = state.notifications.filter(n => n.id !== notiId);
        renderNotiList();
        return;
      }

      const notification = state.notifications.find(n => n.id === notiId);
      if (notification && notification.unread) {

        //서버에 읽음 처리 요청
        fetch(`/api/notifications/${notiId}/read`, { method: 'POST' });

        notification.unread = false;
        renderNotiList();
      }
      window.location.href = card.dataset.href;
    });

    dom.markAllBtn.addEventListener('click', () => {
      state.notifications.forEach(n => {
          if (n.unread) {
            fetch(`/api/notifications/${n.id}/read`, { method: 'POST' });
            n.unread = false;
          }
        });
        renderNotiList();
    });

    dom.deleteReadBtn.addEventListener('click', () => {
      state.notifications.forEach(n => {
          if (!n.unread) {
            fetch(`/api/notifications/${n.id}`, { method: 'DELETE' });
          }
        });
        state.notifications = state.notifications.filter(n => n.unread);
        renderNotiList();
    });
  }

  window.pushNoti = (msg, href = '#', notiIdx = null) => {

  // 중복 여부 체크
    if (state.notifications.some(n => n.id === notiIdx)) {
      console.warn("중복 알림 차단됨", notiIdx);
      return;
    }

    state.notifications.unshift({
      id: notiIdx ?? Date.now(),  // 실시간이면 실제 ID, 아니면 임시값
      icon: 'sparkles-outline',
      iconClass: 'alert',
      msg,
      time: '방금 전',
      unread: true,
      href,
    });
    renderNotiList();

    // 여기서 토스트도 함께 출력
      if (typeof showToastNotification === 'function') {
        showToastNotification(msg);
      }
  };

  function init() {
    renderNav();

    async function fetchNotifications() {

      console.log("fetchNotifications() 호출됨");

      const res = await fetch('/api/notifications');
      const data = await res.json();

      console.log("응답 데이터:", data); // 서버에서 받은 알림 목록 확인

      state.notifications = data.map(n => ({
        id: n.notiIdx,
        msg: n.notiMessage,
        href: n.notiUrl,
        unread: n.isRead === 'N',
        time: formatTime(n.sentAt),
        icon: 'sparkles-outline',
        iconClass: 'alert'
      }));
      renderNotiList();
    }

    // 반드시 호출해야 함!
    fetchNotifications();

    setupEventListeners();
  }

  init();
});
