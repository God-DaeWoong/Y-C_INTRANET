package com.ync.intranet.controller;

import com.ync.intranet.domain.ApprovalLineIntranet;
import com.ync.intranet.domain.DocumentIntranet;
import com.ync.intranet.domain.MemberIntranet;
import com.ync.intranet.domain.ScheduleIntranet;
import com.ync.intranet.mapper.ApprovalLineIntranetMapper;
import com.ync.intranet.mapper.DocumentIntranetMapper;
import com.ync.intranet.mapper.ScheduleIntranetMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 최근 활동 API 컨트롤러
 */
@RestController
@RequestMapping("/api/intranet/activity")
public class ActivityController {

    private final DocumentIntranetMapper documentMapper;
    private final ApprovalLineIntranetMapper approvalLineMapper;
    private final ScheduleIntranetMapper scheduleMapper;

    public ActivityController(DocumentIntranetMapper documentMapper,
                              ApprovalLineIntranetMapper approvalLineMapper,
                              ScheduleIntranetMapper scheduleMapper) {
        this.documentMapper = documentMapper;
        this.approvalLineMapper = approvalLineMapper;
        this.scheduleMapper = scheduleMapper;
    }

    /**
     * 최근 활동 전체 조회
     * GET /api/intranet/activity/recent
     */
    @GetMapping("/recent")
    public Map<String, Object> getRecentActivity(HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        if (memberId == null) {
            return Map.of("success", false, "message", "로그인이 필요합니다");
        }

        // 1. 최근 작성한 문서 5건
        List<DocumentIntranet> recentDocuments = documentMapper.findByAuthorId(memberId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // 2. 최근 처리한 결재 5건 (승인 또는 반려)
        List<ApprovalLineIntranet> recentApprovals = approvalLineMapper.findByApproverId(memberId)
                .stream()
                .filter(a -> !a.getDecision().equals("PENDING"))  // 처리 완료된 것만
                .limit(5)
                .collect(Collectors.toList());

        // 3. 최근 일정/휴가 5건
        List<ScheduleIntranet> recentSchedules = scheduleMapper.findByMemberId(memberId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        return Map.of(
                "success", true,
                "recentDocuments", recentDocuments,
                "recentApprovals", recentApprovals,
                "recentSchedules", recentSchedules
        );
    }
}
