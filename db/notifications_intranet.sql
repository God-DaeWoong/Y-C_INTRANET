-- 알림 테이블 생성
CREATE TABLE notifications_intranet (
    id NUMBER PRIMARY KEY,
    member_id NUMBER NOT NULL,
    notification_type VARCHAR2(50) NOT NULL,
    title VARCHAR2(200) NOT NULL,
    content VARCHAR2(1000),
    link_url VARCHAR2(500),
    is_read NUMBER(1) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    read_at TIMESTAMP,
    CONSTRAINT fk_notif_member FOREIGN KEY (member_id)
        REFERENCES members_intranet(id) ON DELETE CASCADE
);

-- 시퀀스 생성
CREATE SEQUENCE notifications_intranet_seq
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- 인덱스 생성 (성능 최적화)
CREATE INDEX idx_notif_member_read ON notifications_intranet(member_id, is_read);
CREATE INDEX idx_notif_created ON notifications_intranet(created_at DESC);

-- 코멘트 추가
COMMENT ON TABLE notifications_intranet IS '사용자 알림 테이블';
COMMENT ON COLUMN notifications_intranet.id IS '알림 ID';
COMMENT ON COLUMN notifications_intranet.member_id IS '알림 받을 사용자 ID';
COMMENT ON COLUMN notifications_intranet.notification_type IS '알림 유형 (APPROVAL_REQUEST, APPROVAL_APPROVED, APPROVAL_REJECTED, SCHEDULE_REMINDER 등)';
COMMENT ON COLUMN notifications_intranet.title IS '알림 제목';
COMMENT ON COLUMN notifications_intranet.content IS '알림 내용';
COMMENT ON COLUMN notifications_intranet.link_url IS '클릭 시 이동할 URL';
COMMENT ON COLUMN notifications_intranet.is_read IS '읽음 여부 (0: 안읽음, 1: 읽음)';
COMMENT ON COLUMN notifications_intranet.created_at IS '생성일시';
COMMENT ON COLUMN notifications_intranet.read_at IS '읽은 일시';
