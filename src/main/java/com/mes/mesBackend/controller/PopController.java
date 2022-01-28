package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.response.PopWorkOrderDetailResponse;
import com.mes.mesBackend.dto.response.PopWorkOrderResponse;
import com.mes.mesBackend.dto.response.WorkProcessResponse;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.PopService;
import com.mes.mesBackend.service.WorkProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mes.mesBackend.helper.Constants.MONGO_TEMPLATE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

// pop
@RequestMapping("/pop")
@Tag(name = "pop", description = "pop API")
@RestController
@RequiredArgsConstructor
public class PopController {
    private final PopService popService;
    private final WorkProcessService workProcessService;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(PopController.class);
    private CustomLogger cLogger;

    @GetMapping("/work-processes")
    @ResponseBody
    @Operation(summary = "(pop)작업공정 전체 조회")
    public ResponseEntity<List<WorkProcessResponse>> getPopWorkProcesses() {
        List<WorkProcessResponse> workProcesses = workProcessService.getWorkProcesses();
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info( "viewed the list of from getPopWorkProcesses.");
        return new ResponseEntity<>(workProcesses, OK);
    }

    // 작업지시 정보 리스트 api, 조건: 작업자, 작업공정
    // 작업지시 목록(공정)
    @SecurityRequirement(name = AUTHORIZATION)
    @GetMapping("/work-orders")
    @ResponseBody
    @Operation(
            summary = "(pop) 작업지시 정보",
            description = "조건: 작업공정 id, 날짜(당일)"
    )
    public ResponseEntity<List<PopWorkOrderResponse>> getPopWorkOrders(
            @RequestParam @Parameter(description = "작업공정 id") Long workProcessId,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        List<PopWorkOrderResponse> popWorkOrderResponses = popService.getPopWorkOrders(workProcessId);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getPopWorkOrders.");
        return new ResponseEntity<>(popWorkOrderResponses, OK);
    }

    // 작업지시 상세 정보
    // 위에 해당 작업지시로 bomItemDetail 항목들 가져오기(품번, 품명, 계정, bom 수량, 예약수량)
    @SecurityRequirement(name = AUTHORIZATION)
    @GetMapping("/work-order-details")
    @ResponseBody
    @Operation(summary = "(pop) 작업지시 상세 정보", description = "")
    public ResponseEntity<List<PopWorkOrderDetailResponse>> getPopWorkOrderDetails(
            @RequestParam @Parameter(description = "lotMaster id") Long lotMasterId,
            @RequestParam @Parameter(description = "작업지시 id") Long workOrderId,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        List<PopWorkOrderDetailResponse> popWorkOrderDetailResponse = popService.getPopWorkOrderDetails(lotMasterId, workOrderId);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getPopWorkOrderDetails.");
        return new ResponseEntity<>(popWorkOrderDetailResponse, OK);
    }
}
