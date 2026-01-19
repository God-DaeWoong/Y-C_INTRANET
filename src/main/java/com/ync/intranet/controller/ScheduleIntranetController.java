package com.ync.intranet.controller;

import com.ync.intranet.domain.ScheduleIntranet;
import com.ync.intranet.service.ScheduleIntranetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 일정/휴가 컨트롤러
 */
@RestController
@RequestMapping("/api/intranet/schedules")
@CrossOrigin(origins = "*")
public class ScheduleIntranetController {

    private final ScheduleIntranetService scheduleService;

    public ScheduleIntranetController(ScheduleIntranetService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 일정 목록 조회 (필터링 지원)
     * GET /api/intranet/schedules
     */
    @GetMapping
    public ResponseEntity<List<ScheduleIntranet>> getSchedules(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        try {
            System.out.println("[일정 조회] memberId=" + memberId + ", departmentId=" + departmentId +
                             ", divisionId=" + divisionId + ", startDate=" + startDate + ", endDate=" + endDate);

            List<ScheduleIntranet> schedules;

            // 필터링 조건에 따라 조회 (우선순위: member > department > division)
            if (memberId != null) {
                System.out.println("[일정 조회] 구성원별 조회: memberId=" + memberId);
                schedules = scheduleService.getSchedulesByMemberId(memberId);
            } else if (departmentId != null && startDate != null && endDate != null) {
                System.out.println("[일정 조회] 부서별 조회: departmentId=" + departmentId);
                schedules = scheduleService.getSchedulesByDepartmentAndDateRange(departmentId, startDate, endDate);
            } else if (divisionId != null && startDate != null && endDate != null) {
                System.out.println("[일정 조회] 본부별 조회: divisionId=" + divisionId);
                schedules = scheduleService.getSchedulesByDivisionAndDateRange(divisionId, startDate, endDate);
                System.out.println("[일정 조회] 본부별 조회 결과 개수: " + schedules.size());
            } else if (startDate != null && endDate != null) {
                System.out.println("[일정 조회] 기간별 조회");
                schedules = scheduleService.getSchedulesByDateRange(startDate, endDate);
            } else {
                System.out.println("[일정 조회] 전체 조회");
                schedules = scheduleService.getAllSchedules();
            }

            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일정 상세 조회
     * GET /api/intranet/schedules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSchedule(@PathVariable Long id) {
        try {
            ScheduleIntranet schedule = scheduleService.getScheduleById(id);
            if (schedule == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "일정을 찾을 수 없습니다."));
            }
            // 객체를 직접 반환 (JSON 자동 변환)
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 일정 생성
     * POST /api/intranet/schedules
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSchedule(
            @RequestBody ScheduleIntranet schedule,
            HttpSession session
    ) {
        try {
            // 세션에서 사용자 ID 가져오기
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            // 작성자 설정
            schedule.setMemberId(userId);

            // 일정 생성
            scheduleService.createSchedule(schedule);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "일정이 생성되었습니다."
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "일정 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 일정 수정
     * PUT /api/intranet/schedules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleIntranet schedule,
            HttpSession session
    ) {
        try {
            // 세션에서 사용자 ID 가져오기
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            // 기존 일정 확인
            ScheduleIntranet existingSchedule = scheduleService.getScheduleById(id);
            if (existingSchedule == null) {
                return ResponseEntity.notFound().build();
            }

            // 권한 확인 (본인의 일정만 수정 가능)
            if (!existingSchedule.getMemberId().equals(userId)) {
                return ResponseEntity.status(403)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }

            // ID 설정 및 수정
            schedule.setId(id);
            schedule.setMemberId(userId);
            scheduleService.updateSchedule(schedule);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "일정이 수정되었습니다."
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "일정 수정에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 일정 삭제
     * DELETE /api/intranet/schedules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(
            @PathVariable Long id,
            HttpSession session
    ) {
        try {
            // 세션에서 사용자 ID 가져오기
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            // 기존 일정 확인
            ScheduleIntranet existingSchedule = scheduleService.getScheduleById(id);
            if (existingSchedule == null) {
                return ResponseEntity.notFound().build();
            }

            // 권한 확인 (본인의 일정만 삭제 가능)
            if (!existingSchedule.getMemberId().equals(userId)) {
                return ResponseEntity.status(403)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }

            // 삭제
            scheduleService.deleteSchedule(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "일정이 삭제되었습니다."
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "일정 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 일정 취소 신청 (승인된 연차/반차)
     * POST /api/intranet/schedules/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> requestCancellation(
            @PathVariable Long id,
            HttpSession session
    ) {
        try {
            // 세션에서 사용자 ID 가져오기
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            // 기존 일정 확인
            ScheduleIntranet existingSchedule = scheduleService.getScheduleById(id);
            if (existingSchedule == null) {
                return ResponseEntity.notFound().build();
            }

            // 권한 확인 (본인의 일정만 취소 신청 가능)
            if (!existingSchedule.getMemberId().equals(userId)) {
                return ResponseEntity.status(403)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }

            // 결재 필요 일정 여부 확인 (연차/반차/휴일근무/공가/방범신청)
            if (!("VACATION".equals(existingSchedule.getScheduleType()) ||
                  "HALF_DAY".equals(existingSchedule.getScheduleType()) ||
                  "HOLIDAY_WORK".equals(existingSchedule.getScheduleType()) ||
                  "OFFICIAL_LEAVE".equals(existingSchedule.getScheduleType()) ||
                  "SECURITY_REQUEST".equals(existingSchedule.getScheduleType()))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "결재가 필요한 일정만 취소 신청이 가능합니다."));
            }

            // 승인 상태 확인
            if (!"APPROVED".equals(existingSchedule.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "승인된 일정만 취소 신청이 가능합니다."));
            }

            // 취소 신청 처리
            scheduleService.requestCancellation(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "취소 신청이 완료되었습니다."
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "취소 신청에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 취소 신청 철회
     * POST /api/intranet/schedules/{id}/withdraw-cancellation
     */
    @PostMapping("/{id}/withdraw-cancellation")
    public ResponseEntity<Map<String, Object>> withdrawCancellation(
            @PathVariable Long id,
            HttpSession session
    ) {
        try {
            // 세션에서 사용자 ID 가져오기
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            // 철회 처리
            scheduleService.withdrawCancellation(id, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "취소 신청이 철회되었습니다."
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "철회 처리에 실패했습니다: " + e.getMessage()));
        }
    }
}
