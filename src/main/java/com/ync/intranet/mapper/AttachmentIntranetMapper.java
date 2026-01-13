package com.ync.intranet.mapper;

import com.ync.intranet.domain.AttachmentIntranet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 첨부파일 Mapper (인트라넷)
 */
@Mapper
public interface AttachmentIntranetMapper {

    /**
     * 첨부파일 등록
     * @param attachment 첨부파일 정보
     */
    void insert(AttachmentIntranet attachment);

    /**
     * ID로 첨부파일 조회
     * @param id 첨부파일 ID
     * @return 첨부파일 정보
     */
    AttachmentIntranet findById(@Param("id") Long id);

    /**
     * 문서별 첨부파일 목록 조회
     * @param documentId 문서 ID
     * @return 첨부파일 목록
     */
    List<AttachmentIntranet> findByDocumentId(@Param("documentId") Long documentId);

    /**
     * 첨부파일 삭제
     * @param id 첨부파일 ID
     */
    void deleteById(@Param("id") Long id);

    /**
     * 문서의 모든 첨부파일 삭제
     * @param documentId 문서 ID
     */
    void deleteByDocumentId(@Param("documentId") Long documentId);
}
