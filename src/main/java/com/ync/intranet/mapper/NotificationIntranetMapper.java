package com.ync.intranet.mapper;

import com.ync.intranet.domain.NotificationIntranet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 알림 Mapper (인트라넷)
 */
@Mapper
public interface NotificationIntranetMapper {

    /**
     * 알림 생성
     */
    void insert(NotificationIntranet notification);

    /**
     * 사용자별 알림 조회 (최근 20개)
     */
    List<NotificationIntranet> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 읽지 않은 알림 개수
     */
    int countUnreadByMemberId(@Param("memberId") Long memberId);

    /**
     * 알림 읽음 처리
     */
    void markAsRead(@Param("id") Long id);

    /**
     * 전체 읽음 처리
     */
    void markAllAsRead(@Param("memberId") Long memberId);

    /**
     * 알림 삭제
     */
    void deleteById(@Param("id") Long id);

    /**
     * 오래된 읽은 알림 삭제 (7일 이상)
     */
    void deleteOldReadNotifications();
}
