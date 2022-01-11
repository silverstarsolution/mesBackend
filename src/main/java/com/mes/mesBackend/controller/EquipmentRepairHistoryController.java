package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.response.EquipmentRepairHistoryResponse;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.EquipmentBreakdownService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// 17-3. 설비 수리내역 조회
@Tag(name = "equipment-repair-history", description = "17-3. 설비 수리내역 조회 API")
@RequestMapping("/equipment-repair-histories")
@RestController
@SecurityRequirement(name = "Authorization")
@RequiredArgsConstructor
public class EquipmentRepairHistoryController {
    private final EquipmentBreakdownService equipmentBreakdownService;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(EquipmentBreakdownRepairController.class);
    private CustomLogger cLogger;

    // 설비 수리내역 리스트 조회, 검색조건: 작업장 id, 설비유형, 수리항목, 작업기간 fromDate~toDate
    @Operation(summary = "설비 수리내역 리스트 조회", description = "검색조건: 작업장 id, 설비유형, 수리항목, 작업기간 fromDate~toDate")
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<EquipmentRepairHistoryResponse>> getEquipmentRepairHistories(
            @RequestParam(required = false) @Parameter(description = "작업장 id") Long workCenterId,
            @RequestParam(required = false) @Parameter(description = "설비유형") String equipmentType,
            @RequestParam(required = false) @Parameter(description = "수리항목") String repairItem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "작업기간 fromDate") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "작업기간 toDate") LocalDate toDate,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        List<EquipmentRepairHistoryResponse> repairHistoryResponses = equipmentBreakdownService.getEquipmentRepairHistories(workCenterId, equipmentType, repairItem, fromDate, toDate);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getEquipmentRepairHistories.");
        return new ResponseEntity<>(repairHistoryResponses, HttpStatus.OK);
    }
}
