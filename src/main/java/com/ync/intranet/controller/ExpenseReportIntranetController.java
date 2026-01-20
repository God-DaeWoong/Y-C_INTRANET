package com.ync.intranet.controller;

import com.ync.intranet.domain.ExpenseItemIntranet;
import com.ync.intranet.domain.ExpenseReportIntranet;
import com.ync.intranet.dto.ExcelDownloadRequest;
import com.ync.intranet.dto.ExpenseStatsDto;
import com.ync.intranet.dto.UnreadExpenseDto;
import com.ync.intranet.dto.WelfareSummaryDto;
import com.ync.intranet.service.ExpenseExcelService;
import com.ync.intranet.service.ExpenseItemIntranetService;
import com.ync.intranet.service.ExpenseReportIntranetService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 경비보고서 Controller (인트라넷)
 */
@RestController
@RequestMapping("/api/intranet/expense-reports")
public class ExpenseReportIntranetController {

    private final ExpenseReportIntranetService expenseReportService;
    private final ExpenseItemIntranetService expenseItemService;
    private final ExpenseExcelService expenseExcelService;

    public ExpenseReportIntranetController(ExpenseReportIntranetService expenseReportService,
                                            ExpenseItemIntranetService expenseItemService,
                                            ExpenseExcelService expenseExcelService) {
        this.expenseReportService = expenseReportService;
        this.expenseItemService = expenseItemService;
        this.expenseExcelService = expenseExcelService;
    }

    /**
     * 전체 경비보고서 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllExpenseReports(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            List<ExpenseReportIntranet> reports = expenseReportService.getAllExpenseReports();
            return ResponseEntity.ok(Map.of("success", true, "data", reports));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 경비보고서 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getExpenseReportById(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            ExpenseReportIntranet report = expenseReportService.getExpenseReportById(id);
            if (report == null) {
                return ResponseEntity.notFound().build();
            }

            // 경비 항목도 함께 조회
            List<ExpenseItemIntranet> items = expenseItemService.getExpenseItemsByReportId(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("report", report);
            response.put("items", items);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 경비보고서 생성 (결재 요청 포함)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createExpenseReport(@RequestBody Map<String, Object> requestData,
                                                                    HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            // 경비보고서 데이터 파싱
            ExpenseReportIntranet expenseReport = new ExpenseReportIntranet();
            // requestData에서 필요한 값들을 설정
            // TODO: 실제 구현 시 데이터 파싱 로직 추가

            // 결재자 ID
            Long approverId = requestData.get("approverId") != null ?
                    Long.valueOf(requestData.get("approverId").toString()) : null;

            // 경비보고서 생성 (결재 문서 함께 생성)
            ExpenseReportIntranet created = expenseReportService.createExpenseReport(expenseReport, approverId);

            return ResponseEntity.ok(Map.of("success", true, "data", created));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "생성에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 경비보고서 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateExpenseReport(@PathVariable Long id,
                                                                    @RequestBody ExpenseReportIntranet expenseReport,
                                                                    HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            ExpenseReportIntranet updated = expenseReportService.updateExpenseReport(id, expenseReport);
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "수정에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 경비보고서 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteExpenseReport(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            expenseReportService.deleteExpenseReport(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "삭제되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 경비 항목 추가
     */
    @PostMapping("/{reportId}/items")
    public ResponseEntity<Map<String, Object>> addExpenseItem(@PathVariable Long reportId,
                                                               @RequestBody ExpenseItemIntranet expenseItem,
                                                               HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            expenseItem.setExpenseReportId(reportId);
            ExpenseItemIntranet created = expenseItemService.createExpenseItem(expenseItem);

            return ResponseEntity.ok(Map.of("success", true, "data", created));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "항목 추가에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 경비 항목 삭제
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> deleteExpenseItem(@PathVariable Long itemId, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            expenseItemService.deleteExpenseItem(itemId);
            return ResponseEntity.ok(Map.of("success", true, "message", "항목이 삭제되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 경비 항목 생성 (플랫 구조 - reportId를 body에서 받음)
     */
    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> createExpenseItemFlat(@RequestBody ExpenseItemIntranet expenseItem,
                                                                       HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            System.out.println("=== 경비 항목 생성 요청 ===");
            System.out.println("expenseItem: " + expenseItem);
            System.out.println("memberId: " + expenseItem.getMemberId());
            System.out.println("usageDate: " + expenseItem.getUsageDate());
            System.out.println("description: " + expenseItem.getDescription());
            System.out.println("account: " + expenseItem.getAccount());
            System.out.println("amount: " + expenseItem.getAmount());
            System.out.println("expenseReportId: " + expenseItem.getExpenseReportId());

            // expenseReportId가 없으면 null로 설정 (개별 항목 추가)
            // 나중에 경비보고서를 생성할 때 항목들을 포함시킬 수 있음

            ExpenseItemIntranet created = expenseItemService.createExpenseItem(expenseItem);
            System.out.println("생성된 항목 ID: " + created.getId());
            return ResponseEntity.ok(Map.of("success", true, "data", created));
        } catch (Exception e) {
            System.err.println("경비 항목 생성 실패:");
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            if (e.getCause() != null) {
                errorMessage += " - Cause: " + e.getCause().getMessage();
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "항목 추가에 실패했습니다: " + errorMessage));
        }
    }

    /**
     * 모든 경비 항목 조회 (플랫 리스트)
     */
    @GetMapping("/items")
    public ResponseEntity<List<ExpenseItemIntranet>> getAllExpenseItems(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            List<ExpenseItemIntranet> items = expenseItemService.getAllExpenseItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 경비 항목 개별 조회
     */
    @GetMapping("/items/{itemId}")
    public ResponseEntity<ExpenseItemIntranet> getExpenseItemById(@PathVariable Long itemId, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            ExpenseItemIntranet item = expenseItemService.getExpenseItemById(itemId);
            if (item == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * EXPENSE_READ_ID로 경비 항목 조회 (미확인 보고서 상세보기용)
     * 조인조건: EXPENSE_ITEM_READ_STATUS.ID = EXPENSE_ITEMS_INTRANET.EXPENSE_READ_ID
     */
    @GetMapping("/items/by-read-id/{readStatusId}")
    public ResponseEntity<List<ExpenseItemIntranet>> getExpenseItemsByReadId(@PathVariable Long readStatusId, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            List<ExpenseItemIntranet> items = expenseItemService.getExpenseItemsByReadId(readStatusId);
            if (items == null || items.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 경비 항목 수정
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ExpenseItemIntranet> updateExpenseItem(@PathVariable Long itemId,
                                                                  @RequestBody ExpenseItemIntranet expenseItem,
                                                                  HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            ExpenseItemIntranet updated = expenseItemService.updateExpenseItem(itemId, expenseItem);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 복지비 사용 현황 조회
     */
    @GetMapping("/items/welfare-usage/{memberId}/{year}")
    public ResponseEntity<WelfareSummaryDto> getWelfareUsage(@PathVariable Long memberId,
                                                               @PathVariable Integer year,
                                                               HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            WelfareSummaryDto summary = expenseItemService.getWelfareUsageSummary(memberId, year);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 엑셀 다운로드
     */
    @PostMapping("/items/excel")
    public ResponseEntity<byte[]> downloadExcel(@RequestBody ExcelDownloadRequest request, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            System.out.println("Excel download request received: " + request);
            byte[] excelData = expenseExcelService.generateExcel(request);

            // 파일명 생성 (한글 인코딩)
            String memberName = (request.getMember() != null) ? request.getMember() : "unknown";
            String yearMonth = (request.getYearMonth() != null) ? request.getYearMonth() : "unknown";
            String fileName = String.format("expense_report_%s_%s.xlsx", memberName, yearMonth);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + encodedFileName);
            headers.setContentLength(excelData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excel download error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 경비 신청 (알림 생성)
     * @param requestData expenseItemIds: 경비 항목 ID 목록, yyyy: 신청 년도, mm: 신청 월
     */
    @PostMapping("/items/submit")
    public ResponseEntity<Map<String, Object>> submitExpenseItems(@RequestBody Map<String, Object> requestData,
                                                                    HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            @SuppressWarnings("unchecked")
            List<Integer> itemIdInts = (List<Integer>) requestData.get("expenseItemIds");
            List<Long> expenseItemIds = itemIdInts.stream()
                    .map(Integer::longValue)
                    .toList();

            // 화면에서 선택한 년/월 값 (예: "2026", "02")
            String yyyy = (String) requestData.get("yyyy");
            String mm = (String) requestData.get("mm");

            expenseItemService.submitExpenseItems(expenseItemIds, userId, yyyy, mm);

            return ResponseEntity.ok(Map.of("success", true, "message", "경비가 신청되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "경비 신청에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 미확인 경비 조회
     */
    @GetMapping("/items/unread")
    public ResponseEntity<List<UnreadExpenseDto>> getUnreadExpenses(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            List<UnreadExpenseDto> unreadExpenses = expenseItemService.getUnreadExpenses(userId);
            return ResponseEntity.ok(unreadExpenses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 경비 상세 읽음 처리
     */
    @PostMapping("/items/{itemId}/mark-read")
    public ResponseEntity<Map<String, Object>> markExpenseAsRead(@PathVariable Long itemId,
                                                                   HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            expenseItemService.markExpenseAsRead(itemId, userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "읽음 처리되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "읽음 처리에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 미확인 경비 개수 조회
     */
    @GetMapping("/items/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            int count = expenseItemService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 올해/이번달 총 경비 통계 조회
     */
    @GetMapping("/items/stats")
    public ResponseEntity<ExpenseStatsDto> getExpenseStats(
            @RequestParam String period,
            @RequestParam(required = false) Long parentDeptId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long memberId,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            ExpenseStatsDto stats = expenseItemService.getExpenseStats(period, parentDeptId, deptId, memberId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
