package com.ync.intranet.controller;

import com.ync.intranet.domain.NotificationIntranet;
import com.ync.intranet.mapper.NotificationIntranetMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 알림 컨트롤러
 */
@RestController
@RequestMapping("/api/intranet/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationIntranetMapper notificationMapper;

    public NotificationController(NotificationIntranetMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    /**
     * 내 알림 목록 조회
     * GET /api/intranet/notifications
     */
    @GetMapping
    public ResponseEntity<List<NotificationIntranet>> getMyNotifications(HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        List<NotificationIntranet> notifications = notificationMapper.findByMemberId(memberId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 개수
     * GET /api/intranet/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        int count = notificationMapper.countUnreadByMemberId(memberId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 알림 읽음 처리
     * POST /api/intranet/notifications/{id}/read
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        try {
            notificationMapper.markAsRead(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 전체 읽음 처리
     * POST /api/intranet/notifications/read-all
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            notificationMapper.markAllAsRead(memberId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("전체 읽음 처리 실패: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 알림 삭제
     * DELETE /api/intranet/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id, HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            notificationMapper.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("알림 삭제 실패: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 알림 생성 (내부 API - 다른 컨트롤러에서 호출용)
     * POST /api/intranet/notifications
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody NotificationIntranet notification) {
        try {
            notificationMapper.insert(notification);
            return ResponseEntity.ok(Map.of("success", true, "id", notification.getId()));
        } catch (Exception e) {
            log.error("알림 생성 실패: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
