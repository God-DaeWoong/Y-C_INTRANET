-- FILE_TYPE 컬럼 길이 확장 (50 -> 100)
-- Excel 파일의 Content-Type이 너무 길어서 발생하는 ORA-12899 에러 수정

ALTER TABLE attachments_intranet MODIFY file_type VARCHAR2(100);

-- 변경 확인
SELECT column_name, data_type, data_length
FROM user_tab_columns
WHERE table_name = 'ATTACHMENTS_INTRANET'
AND column_name = 'FILE_TYPE';
