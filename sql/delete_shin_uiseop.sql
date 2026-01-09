-- ============================================
-- 신의섭 사용자 데이터 전체 삭제 스크립트
-- ============================================
-- 작성일: 2026-01-09
-- 주의: 이 스크립트는 신의섭 사용자와 연관된 모든 데이터를 삭제합니다.
-- ============================================

-- 1. 신의섭 사용자 ID 확인
SELECT id, email, name FROM members_intranet WHERE name = '신의섭';

-- ============================================
-- 아래 스크립트는 위에서 확인한 ID를 사용하여 실행
-- 예: 신의섭의 ID가 10이라고 가정
-- ============================================

-- 2. 결재선 삭제 (신의섭이 결재자인 경우)
DELETE FROM approval_lines_intranet
WHERE approver_id = (SELECT id FROM members_intranet WHERE name = '신의섭');

-- 3. 결재선 삭제 (신의섭이 작성한 문서의 결재선)
DELETE FROM approval_lines_intranet
WHERE document_id IN (
    SELECT id FROM documents_intranet
    WHERE author_id = (SELECT id FROM members_intranet WHERE name = '신의섭')
);

-- 4. 문서 삭제 (신의섭이 작성한 문서)
DELETE FROM documents_intranet
WHERE author_id = (SELECT id FROM members_intranet WHERE name = '신의섭');

-- 5. 일정 삭제 (신의섭의 일정)
DELETE FROM schedules_intranet
WHERE member_id = (SELECT id FROM members_intranet WHERE name = '신의섭');

-- 6. 경비 항목 삭제 (신의섭의 경비보고서 항목)
DELETE FROM expense_items_intranet
WHERE report_id IN (
    SELECT id FROM expense_reports_intranet
    WHERE requester_id = (SELECT id FROM members_intranet WHERE name = '신의섭')
);

-- 7. 경비 보고서 삭제 (신의섭의 경비보고서)
DELETE FROM expense_reports_intranet
WHERE requester_id = (SELECT id FROM members_intranet WHERE name = '신의섭');

-- 8. 부서장 해제 (신의섭이 부서장인 경우)
UPDATE departments_intranet
SET manager_id = NULL
WHERE manager_id = (SELECT id FROM members_intranet WHERE name = '신의섭');

-- 9. 사용자 삭제 (신의섭)
DELETE FROM members_intranet WHERE name = '신의섭';

-- 10. 삭제 결과 확인
SELECT 'members_intranet' as table_name, COUNT(*) as count FROM members_intranet WHERE name = '신의섭'
UNION ALL
SELECT 'schedules_intranet', COUNT(*) FROM schedules_intranet WHERE member_id IN (SELECT id FROM members_intranet WHERE name = '신의섭')
UNION ALL
SELECT 'documents_intranet', COUNT(*) FROM documents_intranet WHERE author_id IN (SELECT id FROM members_intranet WHERE name = '신의섭')
UNION ALL
SELECT 'expense_reports_intranet', COUNT(*) FROM expense_reports_intranet WHERE requester_id IN (SELECT id FROM members_intranet WHERE name = '신의섭');

-- ============================================
-- COMMIT 또는 ROLLBACK
-- ============================================
-- 확인 후 문제 없으면 COMMIT, 문제 있으면 ROLLBACK
-- COMMIT;
-- ROLLBACK;
