-- ========================================
-- 신의섭 데이터 삭제 (간단 버전)
-- ========================================

-- 1. 결재선 삭제
DELETE FROM approval_lines_intranet
WHERE approver_id = (SELECT id FROM members_intranet WHERE name = '신의섭')
   OR author_id = (SELECT id FROM members_intranet WHERE name = '신의섭');

-- 2. 문서 삭제
DELETE FROM documents_intranet
WHERE author_id = (SELECT id FROM members_intranet WHERE name = '신의섭');

-- 3. 일정 삭제
DELETE FROM schedules_intranet
WHERE member_id = (SELECT id FROM members_intranet WHERE name = '신의섭');

-- 4. 커밋
COMMIT;
