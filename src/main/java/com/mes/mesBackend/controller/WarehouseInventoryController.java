package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.response.WarehouseInventoryResponse;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.WarehouseInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(name = "warehouse-inventory", description = "재고현황 API")
@RequestMapping("/warehouse-inventory")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = AUTHORIZATION)
public class WarehouseInventoryController {
    private final WarehouseInventoryService warehouseInventoryService;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(HeaderController.class);
    private CustomLogger cLogger;

    // 창고재고조회
    @GetMapping()
    @ResponseBody
    @Operation(summary = "창고 재고 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
            }
    )
    public ResponseEntity<List<WarehouseInventoryResponse>> getHeaders(
            @RequestParam(required = false) @Parameter(description = "창고 ID") Long warehouseId,
            @RequestParam(required = false) @Parameter(description = "품목 ID") Long itemId,
            @RequestParam(required = false) @Parameter(description = "품목계정 ID") Long itemAccountId,
            @RequestParam(required = false) @Parameter(description = "품목그룹 ID") Long itemGroupId,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        List<WarehouseInventoryResponse> responseList = warehouseInventoryService.getWarehouseInvetoryList(warehouseId, itemId, itemAccountId, itemGroupId);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the List from getWarehouseInventoryList.");
        return new ResponseEntity<>(responseList, OK);
    }
}
