package com.ync.intranet.mapper;

import com.ync.intranet.domain.DepartmentIntranet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 부서 Mapper (인트라넷)
 */
@Mapper
public interface DepartmentIntranetMapper {

    /**
     * 전체 부서 조회
     */
    List<DepartmentIntranet> findAll();

    /**
     * ID로 부서 조회
     */
    DepartmentIntranet findById(@Param("id") Long id);

    /**
     * 활성화된 부서만 조회
     */
    List<DepartmentIntranet> findByIsActiveTrue();

    /**
     * 상위 부서별 하위 부서 조회
     */
    List<DepartmentIntranet> findByParentId(@Param("parentId") Long parentId);

    /**
     * 최상위 부서 조회 (parentId가 null)
     */
    List<DepartmentIntranet> findRootDepartments();

    /**
     * 부서명으로 조회
     */
    DepartmentIntranet findByName(@Param("name") String name);

    /**
     * 부서 등록
     */
    void insert(DepartmentIntranet department);

    /**
     * 부서 수정
     */
    void update(DepartmentIntranet department);

    /**
     * 부서 삭제
     */
    void deleteById(@Param("id") Long id);

    /**
     * 부서 비활성화
     */
    void deactivate(@Param("id") Long id);
}
