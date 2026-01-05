-- 기존 데이터 삭제 (외래키 제약조건이 없으므로 순서 상관없음)
DELETE FROM approvals;
DELETE FROM events;
DELETE FROM members;

-- 시퀀스 초기화
DROP SEQUENCE members_seq;
DROP SEQUENCE events_seq;
DROP SEQUENCE approvals_seq;

CREATE SEQUENCE members_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE events_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE approvals_seq START WITH 1 INCREMENT BY 1;

-- 팀원 데이터 삽입
-- 개발팀
INSERT INTO members (id, name, department, position, annual_leave_granted, is_active, created_at, updated_at)
VALUES (members_seq.NEXTVAL, '김철수', '개발팀', '팀장', 20, 1, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO members (id, name, department, position, annual_leave_granted, is_active, created_at, updated_at)
VALUES (members_seq.NEXTVAL, '이영희', '개발팀', '선임', 17, 1, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO members (id, name, department, position, annual_leave_granted, is_active, created_at, updated_at)
VALUES (members_seq.NEXTVAL, '박민수', '개발팀', '사원', 15, 1, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO members (id, name, department, position, annual_leave_granted, is_active, created_at, updated_at)
VALUES (members_seq.NEXTVAL, '최지원', '개발팀', '사원', 15, 1, SYSTIMESTAMP, SYSTIMESTAMP);

-- 기획팀
INSERT INTO members (id, name, department, position, annual_leave_granted, is_active, created_at, updated_at)
VALUES (members_seq.NEXTVAL, '정수민', '기획팀', '팀장', 20, 1, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO members (id, name, department, position, annual_leave_granted, is_active, created_at, updated_at)
VALUES (members_seq.NEXTVAL, '강태희', '기획팀', '대리', 16, 1, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO members (id, name, department, position, annual_leave_granted, is_active, created_at, updated_at)
VALUES (members_seq.NEXTVAL, '윤서연', '기획팀', '사원', 15, 1, SYSTIMESTAMP, SYSTIMESTAMP);

-- 디자인팀
INSERT INTO members (id, name, department, position, annual_leave_granted, is_active, created_at, updated_at)
VALUES (members_seq.NEXTVAL, '한지민', '디자인팀', '선임', 17, 1, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO members (id, name, department, position, annual_leave_granted, is_active, created_at, updated_at)
VALUES (members_seq.NEXTVAL, '송유진', '디자인팀', '사원', 15, 1, SYSTIMESTAMP, SYSTIMESTAMP);

-- 영업팀
INSERT INTO members (id, name, department, position, annual_leave_granted, is_active, created_at, updated_at)
VALUES (members_seq.NEXTVAL, '조현우', '영업팀', '부장', 22, 1, SYSTIMESTAMP, SYSTIMESTAMP);

-- 연차 및 일정 데이터 삽입
-- 김철수 (개발팀 팀장) - 2025년 1월 연차
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 1, 'LEAVE', '연차', TO_DATE('2025-01-15', 'YYYY-MM-DD'), TO_DATE('2025-01-15', 'YYYY-MM-DD'), 1, '개인 사유', '병원 방문', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 1, 'LEAVE', '연차', TO_DATE('2025-02-10', 'YYYY-MM-DD'), TO_DATE('2025-02-12', 'YYYY-MM-DD'), 3, '가족 여행', '제주도 여행', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

-- 이영희 (개발팀 선임) - 연차
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 2, 'LEAVE', '연차', TO_DATE('2025-01-20', 'YYYY-MM-DD'), TO_DATE('2025-01-20', 'YYYY-MM-DD'), 1, '개인 사유', '', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 2, 'LEAVE', '반차', TO_DATE('2025-03-05', 'YYYY-MM-DD'), TO_DATE('2025-03-05', 'YYYY-MM-DD'), 0.5, '오전 반차', '개인 업무', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

-- 박민수 (개발팀 사원) - 연차
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 3, 'LEAVE', '연차', TO_DATE('2025-01-08', 'YYYY-MM-DD'), TO_DATE('2025-01-10', 'YYYY-MM-DD'), 3, '설 연휴 연차', '', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 3, 'LEAVE', '연차', TO_DATE('2025-02-20', 'YYYY-MM-DD'), TO_DATE('2025-02-20', 'YYYY-MM-DD'), 1, '개인 사유', '', 'SUBMITTED', SYSTIMESTAMP, SYSTIMESTAMP);

-- 최지원 (개발팀 사원) - 연차
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 4, 'LEAVE', '반차', TO_DATE('2025-01-17', 'YYYY-MM-DD'), TO_DATE('2025-01-17', 'YYYY-MM-DD'), 0.5, '오후 반차', '병원 진료', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

-- 정수민 (기획팀 팀장) - 연차
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 5, 'LEAVE', '연차', TO_DATE('2025-03-03', 'YYYY-MM-DD'), TO_DATE('2025-03-07', 'YYYY-MM-DD'), 5, '해외 출장 후 휴가', '베트남', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

-- 강태희 (기획팀 대리) - 연차
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 6, 'LEAVE', '연차', TO_DATE('2025-02-03', 'YYYY-MM-DD'), TO_DATE('2025-02-04', 'YYYY-MM-DD'), 2, '개인 사유', '', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

-- 윤서연 (기획팀 사원) - 연차
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 7, 'LEAVE', '연차', TO_DATE('2025-01-22', 'YYYY-MM-DD'), TO_DATE('2025-01-24', 'YYYY-MM-DD'), 3, '가족 행사', '결혼식 참석', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

-- 한지민 (디자인팀 선임) - 연차
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 8, 'LEAVE', '반차', TO_DATE('2025-02-14', 'YYYY-MM-DD'), TO_DATE('2025-02-14', 'YYYY-MM-DD'), 0.5, '오후 반차', '개인 약속', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 8, 'LEAVE', '연차', TO_DATE('2025-03-10', 'YYYY-MM-DD'), TO_DATE('2025-03-11', 'YYYY-MM-DD'), 2, '개인 사유', '', 'DRAFT', SYSTIMESTAMP, SYSTIMESTAMP);

-- 송유진 (디자인팀 사원) - 연차
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 9, 'LEAVE', '연차', TO_DATE('2025-01-30', 'YYYY-MM-DD'), TO_DATE('2025-01-31', 'YYYY-MM-DD'), 2, '개인 사유', '이사', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

-- 조현우 (영업팀 부장) - 연차
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 10, 'LEAVE', '연차', TO_DATE('2025-02-17', 'YYYY-MM-DD'), TO_DATE('2025-02-21', 'YYYY-MM-DD'), 5, '휴가', '가족 여행', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

-- 일정 데이터 (SCHEDULE 타입)
INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 1, 'SCHEDULE', '회의', TO_DATE('2025-01-25', 'YYYY-MM-DD'), TO_DATE('2025-01-25', 'YYYY-MM-DD'), 0, '월간 개발팀 회의', '1월 스프린트 리뷰', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 5, 'SCHEDULE', '출장', TO_DATE('2025-02-25', 'YYYY-MM-DD'), TO_DATE('2025-02-28', 'YYYY-MM-DD'), 0, '베트남 출장', '신규 프로젝트 미팅', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO events (id, member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status, created_at, updated_at)
VALUES (events_seq.NEXTVAL, 10, 'SCHEDULE', '출장', TO_DATE('2025-03-15', 'YYYY-MM-DD'), TO_DATE('2025-03-16', 'YYYY-MM-DD'), 0, '거래처 방문', '계약 체결', 'APPROVED', SYSTIMESTAMP, SYSTIMESTAMP);

-- 결재 데이터 샘플 (SUBMITTED 상태 이벤트에 대한 결재)
INSERT INTO approvals (id, event_id, step_order, approver_id, decision, approval_comment, submitted_at, decided_at)
VALUES (approvals_seq.NEXTVAL, 6, 1, 1, NULL, NULL, SYSTIMESTAMP, NULL);

COMMIT;
