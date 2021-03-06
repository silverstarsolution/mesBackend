package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.response.WorkOrderUserResponse;
import com.mes.mesBackend.entity.enumeration.OrderState;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.WorkOrderUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.mes.mesBackend.helper.Constants.MONGO_TEMPLATE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

// 8-2. 작업자 투입 수정
@RequestMapping("/work-order-users")
@Tag(name = "work-order-user", description = "8-2. 작업자 투입 수정 API")
@RestController
@SecurityRequirement(name = AUTHORIZATION)
@RequiredArgsConstructor
public class WorkOrderUserController {
    private final WorkOrderUserService workOrderUserService;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(WorkOrderUserController.class);
    private CustomLogger cLogger;

    // 작업자 투입 리스트 검색 조회, 검색조건: 작업공정 id, 제조오더번호, 품목계정 id, 지시상태, 작업기간 fromDate~toDate, 수주번호
    @GetMapping
    @ResponseBody
    @Operation(
            summary = "작업자 투입 리스트 검색 조회",
            description = "검색조건: 작업공정 id, 제조오더번호, 품목계정 id, 지시상태, 작업기간 fromDate~toDate, 수주번호"
    )
    public ResponseEntity<List<WorkOrderUserResponse>> getWorkOrderUsers(
            @RequestParam(required = false) @Parameter(description = "작업공정 id") Long workProcessId,
            @RequestParam(required = false) @Parameter(description = "제조오더번호") String produceOrderNo,
            @RequestParam(required = false) @Parameter(description = "품목계정 id") Long itemAccountId,
            @RequestParam(required = false) @Parameter(description = "지시상태 [완료: COMPLETION, 진행중: ONGOING, 예정: SCHEDULE, 취소: CANCEL]") OrderState orderState,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "작업기간 fromDate") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "작업기간 toDate") LocalDate toDate,
            @RequestParam(required = false) @Parameter(description = "수주번호") String contractNo,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        List<WorkOrderUserResponse> workOrderUsers = workOrderUserService.getWorkOrderUsers(workProcessId, produceOrderNo, itemAccountId, orderState, fromDate, toDate, contractNo);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getWorkOrderUsers.");
        return new ResponseEntity<>(workOrderUsers, OK);
    }

    // 작업자 투입 단일 조회
    @GetMapping("/{work-order-id}")
    @ResponseBody()
    @Operation(summary = "작업자 투입 단일 조회", description = "")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource")
            }
    )
    public ResponseEntity<WorkOrderUserResponse> getWorkOrderUser(
            @PathVariable(value = "work-order-id") @Parameter(description = "작업지시 id") Long workOrderId,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        WorkOrderUserResponse workOrderUser = workOrderUserService.getWorkOrderUserResponseOrThrow(workOrderId);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the " + workOrderUser.getId() + " from getWorkOrderUser.");
        return new ResponseEntity<>(workOrderUser, OK);
    }

    // 작업자 투입 수정
    @PutMapping(value = "/{work-order-id}")
    @ResponseBody
    @Operation(summary = "작업자 투입 수정", description = "작업자, 시작일시, 종료일시 수정가능")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<WorkOrderUserResponse> updateWorkOrderUser(
            @PathVariable(value = "work-order-id") @Parameter(description = "작업지시 id") Long workOrderId,
            @RequestParam(required = false) @Parameter(description = "작업자 id") Long userId,
            @RequestParam(required = false) @Parameter(description = "시작일시 / yyyy-MM-dd HH:mm") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startDate,
            @RequestParam(required = false) @Parameter(description = "종료일시 / yyyy-MM-dd HH:mm") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endDate,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException, BadRequestException {
        WorkOrderUserResponse workOrderUser = workOrderUserService.updateWorkOrderUser(workOrderId, userId, startDate, endDate);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is update the " + workOrderUser.getId() + " from updateWorkOrderUser.");
        return new ResponseEntity<>(workOrderUser, OK);
    }
}
