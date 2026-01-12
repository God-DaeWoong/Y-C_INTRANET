-- 네이버웍스 SSO 개선을 위한 컬럼 추가
-- members_intranet 테이블에 naverworks_user_id, last_login_at 컬럼 추가

-- 1. naverworks_user_id 컬럼 추가 (네이버웍스 고유 ID)
ALTER TABLE members_intranet
ADD naverworks_user_id VARCHAR2(100);

-- 2. naverworks_user_id에 UNIQUE 제약조건 추가
ALTER TABLE members_intranet
ADD CONSTRAINT uk_naverworks_user_id UNIQUE (naverworks_user_id);

-- 3. last_login_at 컬럼 추가 (마지막 로그인 시간)
ALTER TABLE members_intranet
ADD last_login_at TIMESTAMP;

-- 4. 인덱스 생성 (조회 성능 향상)
CREATE INDEX idx_naverworks_user_id ON members_intranet(naverworks_user_id);
CREATE INDEX idx_last_login_at ON members_intranet(last_login_at);

-- 5. 컬럼 확인
SELECT column_name, data_type, nullable, data_default
FROM user_tab_columns
WHERE table_name = 'MEMBERS_INTRANET'
  AND column_name IN ('NAVERWORKS_USER_ID', 'LAST_LOGIN_AT')
ORDER BY column_id;

COMMIT;
