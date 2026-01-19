package com.ync.intranet.mapper;

import com.ync.intranet.domain.ScheduleIntranet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 일정/휴가 Mapper (XML 기반)
 */
@Mapper
public interface ScheduleIntranetMapper {

    /**
     * 일정 생성
     */
    int insert(ScheduleIntranet schedule);

    /**
     * 일정 수정
     */
    int update(ScheduleIntranet schedule);

    /**
     * 일정 삭제
     */
    int delete(Long id);

    /**
     * ID로 일정 조회
     */
    ScheduleIntranet findById(Long id);

    /**
     * 전체 일정 조회
     */
    List<ScheduleIntranet> findAll();

    /**
     * 특정 사용자의 일정 조회
     */
    List<ScheduleIntranet> findByMemberId(Long memberId);

    /**
     * 기간별 일정 조회
     */
    List<ScheduleIntranet> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 부서별 일정 조회
     */
    List<ScheduleIntranet> findByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 본부별 일정 조회
     * 본부(divisionId)에 속한 모든 부서의 일정을 조회
     */
    List<ScheduleIntranet> findByDivisionAndDateRange(
            @Param("divisionId") Long divisionId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 문서 ID로 일정 조회
     */
    List<ScheduleIntranet> findByDocumentId(Long documentId);

    /**
     * 방범신청 시간대 중복 체크
     * 같은 날짜에 시간대가 겹치는 승인된 방범신청 조회
     */
    List<ScheduleIntranet> findApprovedSecurityRequests(
            @Param("startDate") Date startDate,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );

    /**
     * 사용자별 일정 중복 체크
     * 같은 사용자가 같은 유형으로 날짜 범위가 겹치는 일정 조회
     */
    List<ScheduleIntranet> findDuplicateSchedules(
            @Param("memberId") Long memberId,
            @Param("scheduleType") String scheduleType,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("excludeId") Long excludeId
    );

    /**
     * 휴일근무 중복 체크
     * 같은 사용자가 휴일근무일 또는 대체휴무일이 겹치는 일정 조회
     */
    List<ScheduleIntranet> findDuplicateHolidayWork(
            @Param("memberId") Long memberId,
            @Param("holidayWorkDate") Date holidayWorkDate,
            @Param("substituteHolidayDate") Date substituteHolidayDate,
            @Param("excludeId") Long excludeId
    );
}
