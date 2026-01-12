package com.ync.intranet.mapper;

import com.ync.intranet.domain.MemberIntranet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 사원 정보 Mapper (인트라넷)
 */
@Mapper
public interface MemberIntranetMapper {

    /**
     * 전체 사원 조회
     */
    List<MemberIntranet> findAll();

    /**
     * ID로 사원 조회
     */
    MemberIntranet findById(@Param("id") Long id);

    /**
     * 이메일로 사원 조회 (로그인용)
     */
    MemberIntranet findByEmail(@Param("email") String email);

    /**
     * 네이버웍스 사용자 ID로 조회
     */
    MemberIntranet findByNaverworksUserId(@Param("naverworksUserId") String naverworksUserId);

    /**
     * 활성화된 사원만 조회
     */
    List<MemberIntranet> findByIsActiveTrue();

    /**
     * 부서별 사원 조회
     */
    List<MemberIntranet> findByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 권한별 사원 조회
     */
    List<MemberIntranet> findByRole(@Param("role") String role);

    /**
     * 사원 등록
     */
    void insert(MemberIntranet member);

    /**
     * 사원 수정
     */
    void update(MemberIntranet member);

    /**
     * 비밀번호 변경
     */
    void updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 네이버웍스 로그인 정보 업데이트 (name, position, phone, last_login_at)
     */
    void updateNaverWorksLoginInfo(MemberIntranet member);

    /**
     * 사원 삭제 (물리 삭제)
     */
    void deleteById(@Param("id") Long id);

    /**
     * 사원 비활성화 (논리 삭제)
     */
    void deactivate(@Param("id") Long id);
}
