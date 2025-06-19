document.addEventListener("DOMContentLoaded", function () {
  const userId = window.userId || 0;
  const socket = new SockJS("/ws");
  const stompClient = Stomp.over(socket);

  stompClient.debug = function (str) {
    console.log('📢 STOMP 디버그:', str);
  };

  stompClient.connect({}, function () {
    console.log("✅ STOMP 연결 성공");
    const topic = "/topic/notifications/" + userId;
    console.log("📡 구독 대상 토픽:", topic);

    stompClient.subscribe(topic, function (message) {
      console.log("📨 메시지 수신:", message);
      const data = JSON.parse(message.body);
      showNotification(data);
    });
  }, function (error) {
    console.error("❌ STOMP 연결 실패:", error);
  });

  function showNotification(data) {
    console.log("📨 받은 실시간 알림 payload:", data);

    if (window.pushNoti) {
      window.pushNoti(data.notiMessage, data.notiUrl, Number(data.notiIdx));


    } else {
      console.warn("🔕 pushNoti 함수가 정의되지 않음");
      alert("🔔 알림: " + data.message);
    }
  }

  console.log("userId from window:", window.userId);
});


// 토스트 알림 추가
function showToastNotification(msg) {

  const toast = document.createElement('div');
  toast.className = 'toast-notification';
  toast.innerHTML = msg;

  document.body.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('show');
  }, 10);

  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}