package com.ync.intranet.scheduler;

import com.ync.intranet.service.ScheduleIntranetService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 회의/출장 일정 상태 자동 업데이트 배치 작업
 *
 * 매 5분마다 실행되어 회의/출장 일정의 상태를 현재 시간 기준으로 업데이트합니다.
 * - RESERVED: 시작 전
 * - IN_PROGRESS: 진행 중
 * - COMPLETED: 완료
 */
@Component
public class ScheduleStatusUpdateTask {

    private final ScheduleIntranetService scheduleService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ScheduleStatusUpdateTask(ScheduleIntranetService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 매 5분마다 회의/출장 일정 상태 업데이트
     * 매 5분의 0초에 실행
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void updateMeetingStatuses() {
        String now = LocalDateTime.now().format(formatter);
        System.out.println("[" + now + "] 회의/출장 일정 상태 업데이트 시작");

        try {
            int updateCount = scheduleService.updateMeetingStatuses();
            System.out.println("[" + now + "] 회의/출장 일정 상태 업데이트 완료 - 업데이트된 일정: " + updateCount + "건");
        } catch (Exception e) {
            System.err.println("[" + now + "] 회의/출장 일정 상태 업데이트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 매일 자정에 회의/출장 일정 상태 전체 업데이트
     * cron: 0 0 0 * * * (매일 00:00:00에 실행)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyUpdateMeetingStatuses() {
        String now = LocalDateTime.now().format(formatter);
        System.out.println("[" + now + "] 회의/출장 일정 일일 전체 업데이트 시작");

        try {
            int updateCount = scheduleService.updateMeetingStatuses();
            System.out.println("[" + now + "] 회의/출장 일정 일일 전체 업데이트 완료 - 업데이트된 일정: " + updateCount + "건");
        } catch (Exception e) {
            System.err.println("[" + now + "] 회의/출장 일정 일일 전체 업데이트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
