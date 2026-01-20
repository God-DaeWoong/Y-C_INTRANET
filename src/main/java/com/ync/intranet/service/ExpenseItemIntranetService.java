package com.ync.intranet.service;

import com.ync.intranet.domain.ExpenseItemIntranet;
import com.ync.intranet.domain.ExpenseItemReadStatus;
import com.ync.intranet.domain.MemberIntranet;
import com.ync.intranet.dto.ExpenseStatsDto;
import com.ync.intranet.dto.UnreadExpenseDto;
import com.ync.intranet.dto.WelfareSummaryDto;
import com.ync.intranet.dto.WelfareUsageDto;
import com.ync.intranet.mapper.DepartmentIntranetMapper;
import com.ync.intranet.mapper.ExpenseItemIntranetMapper;
import com.ync.intranet.mapper.ExpenseItemReadStatusMapper;
import com.ync.intranet.mapper.ExpenseReportIntranetMapper;
import com.ync.intranet.mapper.MemberIntranetMapper;
import com.ync.schedule.domain.ExpenseItem;
import com.ync.schedule.mapper.ExpenseItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 경비 항목 서비스 (인트라넷)
 */
@Service
@Transactional(readOnly = true)
public class ExpenseItemIntranetService {

    private final ExpenseItemIntranetMapper expenseItemMapper;
    private final ExpenseReportIntranetMapper expenseReportMapper;
    private final MemberIntranetMapper memberMapper;
    private final ExpenseItemReadStatusMapper readStatusMapper;
    private final DepartmentIntranetMapper departmentMapper;
    private final ExpenseItemMapper scheduleExpenseItemMapper;

    public ExpenseItemIntranetService(ExpenseItemIntranetMapper expenseItemMapper,
                                      ExpenseReportIntranetMapper expenseReportMapper,
                                      MemberIntranetMapper memberMapper,
                                      ExpenseItemReadStatusMapper readStatusMapper,
                                      DepartmentIntranetMapper departmentMapper,
                                      ExpenseItemMapper scheduleExpenseItemMapper) {
        this.expenseItemMapper = expenseItemMapper;
        this.expenseReportMapper = expenseReportMapper;
        this.memberMapper = memberMapper;
        this.readStatusMapper = readStatusMapper;
        this.departmentMapper = departmentMapper;
        this.scheduleExpenseItemMapper = scheduleExpenseItemMapper;
    }

    /**
     * 경비 항목 조회 (ID)
     */
    public ExpenseItemIntranet getExpenseItemById(Long id) {
        return expenseItemMapper.findById(id);
    }

    /**
     * EXPENSE_READ_ID로 경비 항목 조회 (미확인 보고서 상세보기용)
     * 조인조건: EXPENSE_ITEM_READ_STATUS.ID = EXPENSE_ITEMS_INTRANET.EXPENSE_READ_ID
     */
    public List<ExpenseItemIntranet> getExpenseItemsByReadId(Long readStatusId) {
        return expenseItemMapper.findByExpenseReadId(readStatusId);
    }

    /**
     * 경비보고서의 모든 항목 조회
     */
    public List<ExpenseItemIntranet> getExpenseItemsByReportId(Long expenseReportId) {
        return expenseItemMapper.findByExpenseReportId(expenseReportId);
    }

    /**
     * 카테고리별 항목 조회
     */
    public List<ExpenseItemIntranet> getExpenseItemsByCategory(String category) {
        return expenseItemMapper.findByCategory(category);
    }

    /**
     * 전체 경비 항목 조회
     */
    public List<ExpenseItemIntranet> getAllExpenseItems() {
        return expenseItemMapper.findAll();
    }

    /**
     * 경비 항목 생성
     */
    @Transactional
    public ExpenseItemIntranet createExpenseItem(ExpenseItemIntranet expenseItem) {
        expenseItemMapper.insert(expenseItem);

        // 경비보고서의 총 금액 업데이트 (expenseReportId가 있을 경우만)
        if (expenseItem.getExpenseReportId() != null) {
            updateReportTotalAmount(expenseItem.getExpenseReportId());
        }

        return expenseItem;
    }

    /**
     * 경비 항목 일괄 생성
     */
    @Transactional
    public void createExpenseItems(List<ExpenseItemIntranet> expenseItems) {
        if (expenseItems == null || expenseItems.isEmpty()) {
            return;
        }

        expenseItemMapper.insertBatch(expenseItems);

        // 경비보고서의 총 금액 업데이트
        if (!expenseItems.isEmpty()) {
            updateReportTotalAmount(expenseItems.get(0).getExpenseReportId());
        }
    }

    /**
     * 경비 항목 수정
     */
    @Transactional
    public ExpenseItemIntranet updateExpenseItem(Long id, ExpenseItemIntranet expenseItem) {
        ExpenseItemIntranet existing = expenseItemMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("경비 항목을 찾을 수 없습니다: " + id);
        }

        existing.setMemberId(expenseItem.getMemberId());
        existing.setUsageDate(expenseItem.getUsageDate());
        existing.setDescription(expenseItem.getDescription());
        existing.setAccount(expenseItem.getAccount());
        existing.setAmount(expenseItem.getAmount());
        existing.setVendor(expenseItem.getVendor());
        existing.setCostCode(expenseItem.getCostCode());
        existing.setProjectCode(expenseItem.getProjectCode());
        existing.setNote(expenseItem.getNote());
        existing.setWelfareFlag(expenseItem.getWelfareFlag());

        expenseItemMapper.update(existing);

        // 경비보고서의 총 금액 업데이트
        updateReportTotalAmount(existing.getExpenseReportId());

        return existing;
    }

    /**
     * 경비 항목 삭제
     */
    @Transactional
    public void deleteExpenseItem(Long id) {
        ExpenseItemIntranet expenseItem = expenseItemMapper.findById(id);
        if (expenseItem == null) {
            throw new RuntimeException("경비 항목을 찾을 수 없습니다: " + id);
        }

        Long expenseReportId = expenseItem.getExpenseReportId();
        expenseItemMapper.deleteById(id);

        // 경비보고서의 총 금액 업데이트
        updateReportTotalAmount(expenseReportId);
    }

    /**
     * 경비보고서의 모든 항목 삭제
     */
    @Transactional
    public void deleteAllExpenseItems(Long expenseReportId) {
        expenseItemMapper.deleteByExpenseReportId(expenseReportId);

        // 경비보고서의 총 금액을 0으로 업데이트
        expenseReportMapper.updateTotalAmount(expenseReportId, BigDecimal.ZERO);
    }

    /**
     * 경비보고서의 총 금액 재계산 및 업데이트
     */
    private void updateReportTotalAmount(Long expenseReportId) {
        if (expenseReportId == null) {
            return; // expenseReportId가 null이면 업데이트하지 않음
        }

        List<ExpenseItemIntranet> items = expenseItemMapper.findByExpenseReportId(expenseReportId);

        BigDecimal totalAmount = items.stream()
                .map(ExpenseItemIntranet::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        expenseReportMapper.updateTotalAmount(expenseReportId, totalAmount);
    }

    /**
     * 복지비 사용 현황 조회 (입사일 기준으로 1년 미만/이상 구분)
     */
    public WelfareSummaryDto getWelfareUsageSummary(Long memberId, Integer year) {
        MemberIntranet member = memberMapper.findById(memberId);
        if (member == null) {
            throw new RuntimeException("멤버를 찾을 수 없습니다: " + memberId);
        }

        List<ExpenseItemIntranet> welfareExpenses = expenseItemMapper.findWelfareExpensesByMemberIdAndYear(memberId, year);

        // 입사일 기준으로 1년 이상인지 확인
        LocalDate hireDate = member.getHireDate();
        LocalDate currentDate = LocalDate.now();
        boolean isOneYearOrMore = false;

        if (hireDate != null) {
            Period period = Period.between(hireDate, currentDate);
            isOneYearOrMore = period.getYears() >= 1;
        }

        // 입사 1년 이상: 분기당 40만원 (연간 160만원)
        // 입사 1년 미만: 분기당 30만원 (연간 120만원)
        BigDecimal quarterBudget = isOneYearOrMore ? new BigDecimal("400000") : new BigDecimal("300000");
        BigDecimal annualBudget = quarterBudget.multiply(new BigDecimal("4"));

        // 연간 총 복지비 사용액 계산
        BigDecimal totalUsed = BigDecimal.ZERO;
        for (ExpenseItemIntranet expense : welfareExpenses) {
            totalUsed = totalUsed.add(expense.getAmount());
        }

        // Q1 -> Q2 -> Q3 -> Q4 순서로 사용액 할당
        List<WelfareUsageDto> quarters = new ArrayList<>();
        BigDecimal remainingToAllocate = totalUsed;

        for (int quarter = 1; quarter <= 4; quarter++) {
            BigDecimal quarterUsed = BigDecimal.ZERO;

            if (remainingToAllocate.compareTo(BigDecimal.ZERO) > 0) {
                if (remainingToAllocate.compareTo(quarterBudget) >= 0) {
                    quarterUsed = quarterBudget;
                    remainingToAllocate = remainingToAllocate.subtract(quarterBudget);
                } else {
                    quarterUsed = remainingToAllocate;
                    remainingToAllocate = BigDecimal.ZERO;
                }
            }

            BigDecimal quarterRemaining = quarterBudget.subtract(quarterUsed);

            WelfareUsageDto quarterDto = new WelfareUsageDto();
            quarterDto.setYear(year);
            quarterDto.setQuarter(quarter);
            quarterDto.setBudget(quarterBudget);
            quarterDto.setUsed(quarterUsed);
            quarterDto.setRemaining(quarterRemaining);

            quarters.add(quarterDto);
        }

        BigDecimal annualRemaining = annualBudget.subtract(totalUsed);

        WelfareSummaryDto summaryDto = new WelfareSummaryDto();
        summaryDto.setMemberId(memberId);
        summaryDto.setMemberName(member.getName());
        summaryDto.setYear(year);
        summaryDto.setQuarters(quarters);
        summaryDto.setAnnualBudget(annualBudget);
        summaryDto.setAnnualUsed(totalUsed);
        summaryDto.setAnnualRemaining(annualRemaining);
        return summaryDto;
    }

    /**
     * 경비 신청 (알림 생성 포함 + EXPENSE_ITEMS 테이블에 INSERT)
     *
     * 처리 순서:
     * 1. EXPENSE_ITEMS_INTRANET에서 항목들 조회
     * 2. EXPENSE_ITEMS 테이블에 INSERT (ID는 EXPENSE_ITEMS_INTRANET.ID 사용, YYYY/MM 포함)
     * 3. EXPENSE_ITEM_READ_STATUS INSERT (경영관리 Unit 팀원 수만큼, 동일 ID 사용)
     * 4. EXPENSE_ITEMS_INTRANET.EXPENSE_READ_ID 업데이트
     *
     * @param expenseItemIds 경비 항목 ID 목록
     * @param submitterId 신청자 ID
     * @param yyyy 신청 년도 (화면에서 선택한 년도, 예: "2026")
     * @param mm 신청 월 (화면에서 선택한 월, 예: "02")
     */
    @Transactional
    public void submitExpenseItems(List<Long> expenseItemIds, Long submitterId, String yyyy, String mm) {
        if (expenseItemIds == null || expenseItemIds.isEmpty()) {
            throw new RuntimeException("신청할 경비 항목이 없습니다.");
        }

        // EXPENSE_ITEMS_INTRANET에서 항목들 조회
        List<ExpenseItemIntranet> intranetItems = new ArrayList<>();
        for (Long id : expenseItemIds) {
            ExpenseItemIntranet item = expenseItemMapper.findById(id);
            if (item != null) {
                System.out.println("▶ EXPENSE_ITEMS_INTRANET 조회 결과");
                System.out.println("ID           = " + item.getId());
                System.out.println("MEMBER_ID    = " + item.getMemberId());
                System.out.println("USAGE_DATE   = " + item.getUsageDate());
                System.out.println("ACCOUNT      = " + item.getAccount());
                System.out.println("AMOUNT       = " + item.getAmount());
                System.out.println("WELFARE_FLAG = " + item.getWelfareFlag());
                System.out.println("-----------------------------");
            	
                intranetItems.add(item);
            }
        }

        if (intranetItems.isEmpty()) {
            throw new RuntimeException("조회된 경비 항목이 없습니다.");
        }

        // 대표 ID로 첫 번째 항목의 ID 사용 (READ_STATUS에서 공통으로 사용)
        Long representativeId = intranetItems.get(0).getId();

        // EXPENSE_ITEMS 테이블에 INSERT (ID는 EXPENSE_ITEMS_INTRANET.ID 그대로 사용)
        for (ExpenseItemIntranet intranetItem : intranetItems) {
            ExpenseItem scheduleItem = new ExpenseItem();
            scheduleItem.setId(intranetItem.getId()); // INTRANET ID 그대로 사용
            scheduleItem.setMemberId(intranetItem.getMemberId());
            // LocalDate를 "yyyy-MM-dd" 형식의 String으로 변환
            if (intranetItem.getUsageDate() != null) {
                scheduleItem.setUsageDateStr(intranetItem.getUsageDate().toString()); // "yyyy-MM-dd" 형식
            }
            scheduleItem.setAccount(intranetItem.getAccount());
            scheduleItem.setAmount(intranetItem.getAmount());
            scheduleItem.setWelfareFlag(intranetItem.getWelfareFlag());
            scheduleItem.setYyyy(yyyy);  // 화면에서 선택한 년도
            scheduleItem.setMm(mm);      // 화면에서 선택한 월

            scheduleExpenseItemMapper.insertWithId(scheduleItem); // ID 직접 지정하여 INSERT
        }

        // 경영관리 Unit 소속 팀원 찾기
        List<MemberIntranet> managementMembers = findManagementTeamMembers();

        // EXPENSE_ITEM_READ_STATUS INSERT
        // ID = MAX(ID) + 1 로 1회 계산하여 모든 ROW에 동일한 ID 사용
        // 예: MAX(ID)=8, 팀원 3명 → ID=9, 9, 9
        if (!managementMembers.isEmpty()) {
            // MAX(ID) + 1 계산 (1회)
            Long readStatusId = readStatusMapper.getNextId();

            for (MemberIntranet member : managementMembers) {
                ExpenseItemReadStatus readStatus = new ExpenseItemReadStatus();
                readStatus.setId(readStatusId);  // 계산된 동일한 ID 사용
                readStatus.setExpenseItemId(representativeId);
                readStatus.setReaderMemberId(member.getId());
                readStatus.setReadYn("N");
                readStatusMapper.insertWithId(readStatus);  // ID 직접 지정하여 INSERT
            }

            // EXPENSE_ITEMS_INTRANET.EXPENSE_READ_ID 업데이트
            // INSERT에 사용한 동일한 ID로 업데이트
            expenseItemMapper.updateExpenseReadIdBatch(expenseItemIds, readStatusId);
        }
    }

    /**
     * 경영관리 Unit 팀원 조회
     */
    private List<MemberIntranet> findManagementTeamMembers() {
        // "경영관리 Unit" 부서 찾기
        com.ync.intranet.domain.DepartmentIntranet managementUnit = departmentMapper.findByName("경영관리 Unit");

        if (managementUnit == null) {
            // 경영관리 Unit이 없으면 빈 리스트 반환
            return new ArrayList<>();
        }

        // 경영관리 Unit 소속 멤버만 조회
        List<MemberIntranet> unitMembers = memberMapper.findByDepartmentId(managementUnit.getId());

        return unitMembers;
    }

    /**
     * 미확인 경비 조회
     * 조인조건: EXPENSE_ITEM_READ_STATUS.ID = EXPENSE_ITEMS_INTRANET.EXPENSE_READ_ID
     */
    public List<UnreadExpenseDto> getUnreadExpenses(Long readerMemberId) {
        List<ExpenseItemReadStatus> unreadList = readStatusMapper.findUnreadByReaderMemberId(readerMemberId);

        // EXPENSE_ITEM_READ_STATUS.ID로 그룹핑 (동일 ID = 같은 경비 신청 건)
        Map<Long, List<ExpenseItemReadStatus>> groupedByReadStatusId = unreadList.stream()
                .collect(Collectors.groupingBy(ExpenseItemReadStatus::getId));

        List<UnreadExpenseDto> result = new ArrayList<>();

        for (Map.Entry<Long, List<ExpenseItemReadStatus>> entry : groupedByReadStatusId.entrySet()) {
            Long readStatusId = entry.getKey();
            // EXPENSE_ITEMS_INTRANET.EXPENSE_READ_ID = readStatusId로 조회
            List<ExpenseItemIntranet> expenseItems = expenseItemMapper.findByExpenseReadId(readStatusId);

            if (expenseItems != null && !expenseItems.isEmpty()) {
                ExpenseItemIntranet firstItem = expenseItems.get(0);
                UnreadExpenseDto dto = new UnreadExpenseDto();
                dto.setExpenseItemId(readStatusId);  // READ_STATUS.ID를 키로 사용
                dto.setSubmitterName(firstItem.getMember().getName());
                dto.setSubmitterDepartment(firstItem.getMember().getDepartmentName());
                dto.setSubmittedAt(firstItem.getCreatedAt());
                dto.setItems(expenseItems);
                dto.setItemCount(expenseItems.size());
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 경비 상세 읽음 처리
     * @param readStatusId EXPENSE_ITEM_READ_STATUS.ID
     * @param readerMemberId 읽은 사용자 ID
     */
    @Transactional
    public void markExpenseAsRead(Long readStatusId, Long readerMemberId) {
        readStatusMapper.markAsReadById(readStatusId, readerMemberId);
    }

    /**
     * 미확인 경비 개수 조회
     */
    public int getUnreadCount(Long readerMemberId) {
        return readStatusMapper.countUnreadByReaderMemberId(readerMemberId);
    }

    /**
     * 올해/이번달 총 경비 통계 조회 (EXPENSE_ITEMS 테이블 기준)
     */
    public ExpenseStatsDto getExpenseStats(String period, Long parentDeptId, Long deptId, Long memberId) {
        LocalDate now = LocalDate.now();
        LocalDate startDate, endDate;

        if ("year".equals(period)) {
            startDate = LocalDate.of(now.getYear(), 1, 1);
            endDate = LocalDate.of(now.getYear(), 12, 31);
        } else { // month
            startDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
            endDate = LocalDate.of(now.getYear(), now.getMonthValue(), now.lengthOfMonth());
        }

        // EXPENSE_ITEMS 테이블에서 필터링된 경비 항목 조회
        List<com.ync.schedule.dto.ExpenseItemDto> items;
        if (memberId != null) {
            items = scheduleExpenseItemMapper.findByMemberIdAndDateRange(memberId, startDate, endDate);
        } else {
            items = scheduleExpenseItemMapper.findByDateRange(startDate, endDate);

            // 부서 필터링
            if (parentDeptId != null || deptId != null) {
                List<MemberIntranet> filteredMembers = filterMembersByDepartment(parentDeptId, deptId);
                List<Long> memberIds = filteredMembers.stream().map(MemberIntranet::getId).collect(Collectors.toList());
                items = items.stream()
                        .filter(item -> memberIds.contains(item.getMemberId()))
                        .collect(Collectors.toList());
            }
        }

        // 총계 계산
        // 올해: member_id + YYYY DISTINCT (해당 연도 내 신청자 수)
        // 이번달: member_id + YYYY + MM DISTINCT (해당 월 내 신청자 수)
        // → startDate~endDate 범위로 이미 필터링된 상태에서 member_id DISTINCT
        int totalCount = (int) items.stream()
                .map(com.ync.schedule.dto.ExpenseItemDto::getMemberId)
                .filter(id -> id != null)
                .distinct()
                .count();
        BigDecimal totalAmount = items.stream()
                .map(com.ync.schedule.dto.ExpenseItemDto::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 카테고리별 통계 계산
        Map<String, ExpenseStatsDto.CategoryStats> categoryMap = new HashMap<>();

        for (com.ync.schedule.dto.ExpenseItemDto item : items) {
            String key = item.getAccount() + "_" + (item.getWelfareFlag() != null ? item.getWelfareFlag() : "N");

            ExpenseStatsDto.CategoryStats stats = categoryMap.get(key);
            if (stats == null) {
                stats = new ExpenseStatsDto.CategoryStats();
                stats.setCategory(item.getAccount());
                stats.setWelfareFlag(item.getWelfareFlag() != null ? item.getWelfareFlag() : "N");
                stats.setCount(0);
                stats.setAmount(BigDecimal.ZERO);
                categoryMap.put(key, stats);
            }

            stats.setCount(stats.getCount() + 1);
            if (item.getAmount() != null) {
                stats.setAmount(stats.getAmount().add(item.getAmount()));
            }
        }

        // 퍼센티지 계산
        List<ExpenseStatsDto.CategoryStats> categoryStatsList = new ArrayList<>(categoryMap.values());
        for (ExpenseStatsDto.CategoryStats stats : categoryStatsList) {
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                double percentage = stats.getAmount()
                        .divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
                stats.setPercentage(percentage);
            } else {
                stats.setPercentage(0.0);
            }
        }

        ExpenseStatsDto resultDto = new ExpenseStatsDto();
        resultDto.setTotalCount(totalCount);
        resultDto.setTotalAmount(totalAmount);
        resultDto.setCategoryStats(categoryStatsList);
        return resultDto;
    }

    /**
     * 부서별 멤버 필터링
     */
    private List<MemberIntranet> filterMembersByDepartment(Long parentDeptId, Long deptId) {
        List<MemberIntranet> allMembers = memberMapper.findAll();

        if (deptId != null) {
            // 특정 팀 선택
            return allMembers.stream()
                    .filter(m -> deptId.equals(m.getDepartmentId()))
                    .collect(Collectors.toList());
        } else if (parentDeptId != null) {
            // 본부 선택 - 해당 본부 소속 팀들의 멤버들
            List<com.ync.intranet.domain.DepartmentIntranet> childDepts = departmentMapper.findByParentId(parentDeptId);
            List<Long> childDeptIds = childDepts.stream()
                    .map(com.ync.intranet.domain.DepartmentIntranet::getId)
                    .collect(Collectors.toList());

            return allMembers.stream()
                    .filter(m -> childDeptIds.contains(m.getDepartmentId()))
                    .collect(Collectors.toList());
        }

        return allMembers;
    }
}
