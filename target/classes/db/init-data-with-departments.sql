-- 기존 데이터 삭제
DELETE FROM approvals;
DELETE FROM events;
DELETE FROM members;
DELETE FROM departments;

-- 시퀀스 초기화
DROP SEQUENCE departments_seq;
DROP SEQUENCE members_seq;
DROP SEQUENCE events_seq;
DROP SEQUENCE approvals_seq;

CREATE SEQUENCE departments_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE members_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE events_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE approvals_seq START WITH 1 INCREMENT BY 1;

-- ============================================
-- 부서 데이터 삽입 (상하위 구조)
-- ============================================

-- 최상위 부서 (본부)
INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '경영관리본부', NULL, 0, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '지능형인테리어 소프트웨어 연구소', NULL, 0, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '기업사업1본부', NULL, 0, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '기업사업2본부', NULL, 0, 1);

-- 하위 부서 (팀)
-- 경영관리본부 산하
INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '본부장', 1, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '경영관리 Unit', 1, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, 'CDP운영 Unit', 1, 1, 1);

-- 지능형인테리어 소프트웨어 연구소 산하
INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '연구소장', 2, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '전문위원', 2, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, 'AI에이전트기획', 2, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '혁신기술소싱Unit', 2, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, 'AI기반SW개발Unit', 2, 1, 1);

-- 기업사업1본부 산하
INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '본부장', 3, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '영업/구매 Unit', 3, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '생산/물류 Unit', 3, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '공통/FCM Unit', 3, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, 'UI/UX Unit', 3, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, 'SI사업 Unit', 3, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, 'ICT유통 Unit', 3, 1, 1);

-- 기업사업2본부 산하
INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, '본부장', 4, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, 'TI사업 Unit', 4, 1, 1);

INSERT INTO departments (id, name, parent_id, depth, is_active)
VALUES (departments_seq.NEXTVAL, 'TS사업 Unit', 4, 1, 1);

-- ============================================
-- 팀원 데이터 삽입 (department_id 사용)
-- ============================================

-- 경영관리 Unit (department_id = 5)
INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '김철수', 5, '본부장', 22, 1);

INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '이영희', 5, '팀장', 20, 1);

-- AI기반SW개발Unit (department_id = 10)
INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '박민수', 10, '팀장', 20, 1);

INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '최지원', 10, '선임', 17, 1);

INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '정수민', 10, '사원', 15, 1);

-- 공통/FCM Unit (department_id = 13)
INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '강태희', 13, '팀장', 20, 1);

INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '윤서연', 13, '대리', 16, 1);

-- UI/UX Unit (department_id = 14)
INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '한지민', 14, '선임', 17, 1);

INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '송유진', 14, '사원', 15, 1);

-- SI사업 Unit (department_id = 15)
INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '조현우', 15, '팀장', 20, 1);

INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '배수지', 15, '대리', 16, 1);

-- TI사업 Unit (department_id = 17)
INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '서민준', 17, '팀장', 20, 1);

-- TS사업 Unit (department_id = 18)
INSERT INTO members (id, name, department_id, position, annual_leave_granted, is_active)
VALUES (members_seq.NEXTVAL, '오지영', 18, '선임', 17, 1);

-- ============================================
-- 연차 및 일정 데이터 삽입
-- ============================================

-- 김철수 (개발팀 팀장, member_id = 1)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (1, 'LEAVE', '연차', TO_DATE('2025-01-15', 'YYYY-MM-DD'), TO_DATE('2025-01-15', 'YYYY-MM-DD'), 1, '개인 사유', '병원 방문', 'APPROVED');

INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (1, 'LEAVE', '연차', TO_DATE('2025-02-10', 'YYYY-MM-DD'), TO_DATE('2025-02-12', 'YYYY-MM-DD'), 3, '가족 여행', '제주도 여행', 'APPROVED');

-- 이영희 (개발팀 선임, member_id = 2)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (2, 'LEAVE', '연차', TO_DATE('2025-01-20', 'YYYY-MM-DD'), TO_DATE('2025-01-20', 'YYYY-MM-DD'), 1, '개인 사유', '', 'APPROVED');

INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (2, 'LEAVE', '반차', TO_DATE('2025-03-05', 'YYYY-MM-DD'), TO_DATE('2025-03-05', 'YYYY-MM-DD'), 0.5, '오전 반차', '개인 업무', 'APPROVED');

-- 박민수 (개발팀 사원, member_id = 3)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (3, 'LEAVE', '연차', TO_DATE('2025-01-08', 'YYYY-MM-DD'), TO_DATE('2025-01-10', 'YYYY-MM-DD'), 3, '설 연휴 연차', '', 'APPROVED');

INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (3, 'LEAVE', '연차', TO_DATE('2025-02-20', 'YYYY-MM-DD'), TO_DATE('2025-02-20', 'YYYY-MM-DD'), 1, '개인 사유', '', 'SUBMITTED');

-- 최지원 (개발팀 사원, member_id = 4)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (4, 'LEAVE', '반차', TO_DATE('2025-01-17', 'YYYY-MM-DD'), TO_DATE('2025-01-17', 'YYYY-MM-DD'), 0.5, '오후 반차', '병원 진료', 'APPROVED');

-- 정수민 (기획팀 팀장, member_id = 5)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (5, 'LEAVE', '연차', TO_DATE('2025-03-03', 'YYYY-MM-DD'), TO_DATE('2025-03-07', 'YYYY-MM-DD'), 5, '해외 출장 후 휴가', '베트남', 'APPROVED');

-- 강태희 (기획팀 대리, member_id = 6)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (6, 'LEAVE', '연차', TO_DATE('2025-02-03', 'YYYY-MM-DD'), TO_DATE('2025-02-04', 'YYYY-MM-DD'), 2, '개인 사유', '', 'APPROVED');

-- 윤서연 (기획팀 사원, member_id = 7)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (7, 'LEAVE', '연차', TO_DATE('2025-01-22', 'YYYY-MM-DD'), TO_DATE('2025-01-24', 'YYYY-MM-DD'), 3, '가족 행사', '결혼식 참석', 'APPROVED');

-- 한지민 (디자인팀 선임, member_id = 8)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (8, 'LEAVE', '연차', TO_DATE('2025-02-14', 'YYYY-MM-DD'), TO_DATE('2025-02-14', 'YYYY-MM-DD'), 0.5, '오후 반차', '개인 약속', 'APPROVED');

INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (8, 'LEAVE', '연차', TO_DATE('2025-03-10', 'YYYY-MM-DD'), TO_DATE('2025-03-11', 'YYYY-MM-DD'), 2, '개인 사유', '', 'DRAFT');

-- 송유진 (디자인팀 사원, member_id = 9)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (9, 'LEAVE', '연차', TO_DATE('2025-01-30', 'YYYY-MM-DD'), TO_DATE('2025-01-31', 'YYYY-MM-DD'), 2, '개인 사유', '이사', 'APPROVED');

-- 조현우 (국내영업팀 부장, member_id = 10)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (10, 'LEAVE', '연차', TO_DATE('2025-02-17', 'YYYY-MM-DD'), TO_DATE('2025-02-21', 'YYYY-MM-DD'), 5, '휴가', '가족 여행', 'APPROVED');

-- 배수지 (국내영업팀 대리, member_id = 11)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (11, 'LEAVE', '연차', TO_DATE('2025-03-12', 'YYYY-MM-DD'), TO_DATE('2025-03-14', 'YYYY-MM-DD'), 3, '개인 사유', '', 'DRAFT');

-- 서민준 (인사팀 팀장, member_id = 12)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (12, 'LEAVE', '반차', TO_DATE('2025-02-25', 'YYYY-MM-DD'), TO_DATE('2025-02-25', 'YYYY-MM-DD'), 0.5, '오전 반차', '개인 업무', 'APPROVED');

-- 오지영 (재무팀 선임, member_id = 13)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (13, 'LEAVE', '연차', TO_DATE('2025-01-27', 'YYYY-MM-DD'), TO_DATE('2025-01-28', 'YYYY-MM-DD'), 2, '개인 사유', '', 'APPROVED');

-- 일정 데이터 (SCHEDULE 타입)
INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (1, 'SCHEDULE', '회의', TO_DATE('2025-01-25', 'YYYY-MM-DD'), TO_DATE('2025-01-25', 'YYYY-MM-DD'), 0, '월간 개발팀 회의', '1월 스프린트 리뷰', 'APPROVED');

INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (5, 'SCHEDULE', '출장', TO_DATE('2025-02-25', 'YYYY-MM-DD'), TO_DATE('2025-02-28', 'YYYY-MM-DD'), 0, '베트남 출장', '신규 프로젝트 미팅', 'APPROVED');

INSERT INTO events (member_id, event_type, event_subtype, start_date, end_date, leave_amount, title, description, status)
VALUES (10, 'SCHEDULE', '출장', TO_DATE('2025-03-15', 'YYYY-MM-DD'), TO_DATE('2025-03-16', 'YYYY-MM-DD'), 0, '거래처 방문', '계약 체결', 'APPROVED');

-- ============================================
-- 결재 데이터 삽입
-- ============================================

-- SUBMITTED 상태 이벤트에 대한 결재 (박민수의 연차 신청)
INSERT INTO approvals (event_id, step_order, approver_id, decision, approval_comment, submitted_at, decided_at)
VALUES (6, 1, 1, NULL, NULL, SYSTIMESTAMP, NULL);

COMMIT;
