-- =====================================================
-- 17_add_new_schedule_types.sql
-- 새로운 일정 유형 추가를 위한 데이터베이스 스키마 변경
-- 작성일: 2026-01-16
-- 버전: 0.9.3
-- =====================================================

-- 목적:
-- 1. 휴일근무(HOLIDAY_WORK) 전용 날짜 필드 추가
-- 2. 공가(OFFICIAL_LEAVE), 방범신청(SECURITY_REQUEST) 지원
-- 3. start_date, end_date NULL 허용 (휴일근무 유형만 사용)

-- =====================================================
-- 1. 휴일근무 전용 날짜 필드 추가
-- =====================================================

-- 휴일근무일 (실제 근무한 휴일)
ALTER TABLE schedules_intranet
ADD holiday_work_date DATE NULL;

-- 대체휴무일 (휴일근무 대신 쉬는 날)
ALTER TABLE schedules_intranet
ADD substitute_holiday_date DATE NULL;

COMMENT ON COLUMN schedules_intranet.holiday_work_date IS '휴일근무일 (schedule_type=HOLIDAY_WORK 전용)';
COMMENT ON COLUMN schedules_intranet.substitute_holiday_date IS '대체휴무일 (schedule_type=HOLIDAY_WORK 전용)';

-- =====================================================
-- 2. 기존 날짜 필드 NULL 허용으로 변경
-- =====================================================

-- HOLIDAY_WORK 유형은 holiday_work_date와 substitute_holiday_date를 사용하므로
-- start_date와 end_date가 NULL일 수 있음
ALTER TABLE schedules_intranet
MODIFY start_date DATE NULL;

ALTER TABLE schedules_intranet
MODIFY end_date DATE NULL;

-- =====================================================
-- 새로운 일정 유형 설명
-- =====================================================

-- HOLIDAY_WORK (휴일근무):
--   - holiday_work_date, substitute_holiday_date 필수
--   - start_date, end_date는 NULL
--   - 결재 필요 (approver_id, document_id 사용)
--   - 휴가 차감 없음 (days_used = 0)

-- OFFICIAL_LEAVE (공가):
--   - start_date, end_date 필수
--   - holiday_work_date, substitute_holiday_date는 NULL
--   - 결재 필요 (approver_id, document_id 사용)
--   - 휴가 차감 없음 (days_used = 0)
--   - 연차 신청 폼 베이스 사용

-- SECURITY_REQUEST (방범신청):
--   - start_date, end_date 필수
--   - holiday_work_date, substitute_holiday_date는 NULL
--   - 결재 필요 (approver_id, document_id 사용)
--   - 휴가 차감 없음 (days_used = 0)
--   - 회의 신청 폼 베이스 사용

-- =====================================================
-- 검증 쿼리
-- =====================================================

-- 테이블 구조 확인
-- SELECT column_name, data_type, nullable
-- FROM user_tab_columns
-- WHERE table_name = 'SCHEDULES_INTRANET'
-- AND column_name IN ('START_DATE', 'END_DATE', 'HOLIDAY_WORK_DATE', 'SUBSTITUTE_HOLIDAY_DATE')
-- ORDER BY column_id;

-- 컬럼 코멘트 확인
-- SELECT column_name, comments
-- FROM user_col_comments
-- WHERE table_name = 'SCHEDULES_INTRANET'
-- AND column_name IN ('HOLIDAY_WORK_DATE', 'SUBSTITUTE_HOLIDAY_DATE');

COMMIT;
