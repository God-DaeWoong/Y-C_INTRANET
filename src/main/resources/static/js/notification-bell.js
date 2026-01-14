// 알림 벨 공통 모듈

// 알림 드롭다운 토글
function toggleNotificationDropdown() {
    const dropdown = document.getElementById('notificationDropdown');
    const isVisible = dropdown.style.display === 'block';

    if (isVisible) {
        dropdown.style.display = 'none';
    } else {
        dropdown.style.display = 'block';
        loadNotifications();
    }
}

// 외부 클릭 시 드롭다운 닫기
document.addEventListener('click', (e) => {
    const wrapper = document.querySelector('.notification-wrapper');
    if (wrapper && !wrapper.contains(e.target)) {
        const dropdown = document.getElementById('notificationDropdown');
        if (dropdown) {
            dropdown.style.display = 'none';
        }
    }
});

// 읽지 않은 알림 개수 가져오기
async function loadUnreadCount() {
    try {
        const response = await fetch('/api/intranet/notifications/unread-count');
        const data = await response.json();

        const badge = document.getElementById('notificationBadge');
        if (badge) {
            if (data.count > 0) {
                badge.textContent = data.count > 99 ? '99+' : data.count;
                badge.style.display = 'block';
            } else {
                badge.style.display = 'none';
            }
        }
    } catch (error) {
        console.error('알림 개수 조회 실패:', error);
    }
}

// 알림 목록 가져오기
async function loadNotifications() {
    try {
        const response = await fetch('/api/intranet/notifications');
        const notifications = await response.json();

        const listContainer = document.getElementById('notificationList');
        if (!listContainer) return;

        if (!notifications || notifications.length === 0) {
            listContainer.innerHTML = '<div class="empty-notification">알림이 없습니다</div>';
            return;
        }

        listContainer.innerHTML = notifications.map(notif => `
            <div class="notification-item ${notif.isRead ? '' : 'unread'}">
                <div onclick="handleNotificationClick(${notif.id}, '${notif.linkUrl || ''}')" style="flex: 1; cursor: pointer;">
                    <div class="notification-title">${escapeHtml(notif.title)}</div>
                    <div class="notification-content">${escapeHtml(notif.content)}</div>
                    <div class="notification-time">${formatTimeAgo(notif.createdAt)}</div>
                </div>
                <button class="notification-delete-btn" onclick="event.stopPropagation(); deleteNotification(${notif.id})" title="삭제">✕</button>
            </div>
        `).join('');

    } catch (error) {
        console.error('알림 목록 조회 실패:', error);
    }
}

// 알림 클릭 처리
async function handleNotificationClick(notifId, linkUrl) {
    try {
        // 읽음 처리
        await fetch(`/api/intranet/notifications/${notifId}/read`, {
            method: 'POST'
        });

        // 페이지 이동
        if (linkUrl) {
            window.location.href = linkUrl;
        } else {
            // URL이 없으면 드롭다운만 닫고 새로고침
            document.getElementById('notificationDropdown').style.display = 'none';
            loadNotifications();
            loadUnreadCount();
        }

    } catch (error) {
        console.error('알림 읽음 처리 실패:', error);
    }
}

// 모두 읽음 처리
async function markAllAsRead() {
    try {
        await fetch('/api/intranet/notifications/read-all', {
            method: 'POST'
        });

        // 목록 새로고침
        loadNotifications();
        loadUnreadCount();

    } catch (error) {
        console.error('전체 읽음 처리 실패:', error);
    }
}

// 알림 삭제
async function deleteNotification(notifId) {
    if (!confirm('이 알림을 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(`/api/intranet/notifications/${notifId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            // 목록 새로고침
            loadNotifications();
            loadUnreadCount();
        } else {
            alert('알림 삭제에 실패했습니다.');
        }

    } catch (error) {
        console.error('알림 삭제 실패:', error);
        alert('알림 삭제 중 오류가 발생했습니다.');
    }
}

// 시간 표시 (예: "방금 전", "5분 전")
function formatTimeAgo(dateStr) {
    const now = new Date();
    const past = new Date(dateStr);
    const diffMs = now - past;
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) return '방금 전';
    if (diffMins < 60) return `${diffMins}분 전`;
    if (diffMins < 1440) return `${Math.floor(diffMins / 60)}시간 전`;

    const month = past.getMonth() + 1;
    const day = past.getDate();
    return `${month}월 ${day}일`;
}

// HTML 이스케이프
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 30초마다 알림 개수 갱신 (페이지 로드 시 자동 시작)
if (typeof window !== 'undefined') {
    // 페이지 로드 시 알림 개수 로드
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            loadUnreadCount();
            setInterval(loadUnreadCount, 30000);
        });
    } else {
        loadUnreadCount();
        setInterval(loadUnreadCount, 30000);
    }
}
