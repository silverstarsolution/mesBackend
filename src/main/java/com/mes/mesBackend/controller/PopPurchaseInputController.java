package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.response.PopPurchaseOrderResponse;
import com.mes.mesBackend.dto.response.PopPurchaseRequestResponse;
import com.mes.mesBackend.entity.enumeration.OrderState;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.PopPurchaseInputService;
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

@RequestMapping("/pop/input-materials")
@Tag(name = "pop-input-material", description = "[pop] 1. 자재입고")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = AUTHORIZATION)
public class PopPurchaseInputController {
    private final PopPurchaseInputService popPurchaseInputService;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(PopPurchaseInputController.class);
    private CustomLogger cLogger;

    // 구매발주 등록이 완료 된 구매발주 리스트 GET
    // 완료된 구매발주 전부
    // response 추가: orderState
    @GetMapping
    @ResponseBody
    @Operation(summary = "(pop) 구매발주 리스트", description = "화면 상 자재입고 화면, 검색조건: 거래처 명, 상태값별 검색")
    public ResponseEntity<List<PopPurchaseOrderResponse>> getPurchaseOrders(
            @RequestParam(required = false) @Parameter(description = "거래처 명") String clientName,
            @RequestParam(required = false)
            @Parameter(description = "상태값 [완료: COMPLETION, 진행중: ONGOING, 예정: SCHEDULE, 취소: CANCEL]") OrderState orderState,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        List<PopPurchaseOrderResponse> popPurchaseOrders = popPurchaseInputService.getPurchaseOrders(clientName, orderState);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getPurchaseOrders.");
        return new ResponseEntity<>(popPurchaseOrders, OK);
    }

    // 구매발주에 등록 된 구매요청 리스트 GET
    //
    @GetMapping("/{id}/input-infos")
    @ResponseBody
    @Operation(summary = "(pop) 구매요청 리스트", description = "자재 목록 화면")
    public ResponseEntity<List<PopPurchaseRequestResponse>> getPurchaseRequests(
            @PathVariable(value = "id") @Parameter(description = "구매발주 id") Long id,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        List<PopPurchaseRequestResponse> popPurchaseRequests = popPurchaseInputService.getPurchaseRequests(id);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getPurchaseRequests.");
        return new ResponseEntity<>(popPurchaseRequests, OK);
    }

    // 구매요청에 대한 구매입고(request: 수량) POST
    @Operation(summary = "(pop) 구매입고", description = "구매입고")
    @PostMapping("/{id}/input-infos")
    public ResponseEntity createPurchaseInput(
            @PathVariable(value = "id") @Parameter(description = "구매요청 id") Long purchaseRequestId,
            @RequestParam @Parameter (description = "입고수량") int inputAmount,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException, BadRequestException {
        Long purchaseInputId = popPurchaseInputService.createPurchaseInput(purchaseRequestId, inputAmount);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is created the " + purchaseInputId + " from createPurchaseInput.");
        return new ResponseEntity<>(OK);
    }
}
