-- 최근 업로드된 첨부파일 조회
SELECT 
    id,
    document_id,
    file_name,
    file_size,
    file_type,
    uploaded_by,
    uploaded_at
FROM attachments_intranet
ORDER BY uploaded_at DESC
FETCH FIRST 20 ROWS ONLY;

-- Excel 파일만 조회
SELECT 
    id,
    document_id,
    file_name,
    file_size,
    file_type,
    uploaded_by,
    uploaded_at
FROM attachments_intranet
WHERE LOWER(file_name) LIKE '%.xls%'
ORDER BY uploaded_at DESC;

-- 특정 문서의 첨부파일 조회 (document_id를 실제 값으로 바꿔서 실행)
-- SELECT * FROM attachments_intranet WHERE document_id = ?;
