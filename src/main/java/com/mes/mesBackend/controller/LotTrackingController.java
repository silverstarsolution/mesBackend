package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.response.LotTrackingResponse;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.LotTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 7-2. LOT Tracking
@RequestMapping(value = "/lot-trackings")
@Tag(name = "lot-tracking", description = "7-2. LOT Tracking API")
@RestController
@SecurityRequirement(name = "Authorization")
@RequiredArgsConstructor
public class LotTrackingController {
    private final LotTrackService lotTrackService;
    private final LogService logService;
    private Logger logger = LoggerFactory.getLogger(LotTrackingController.class);
    private CustomLogger cLogger;

    // LOT Tracking
    // 검색조건: LOT 번호, 추적유형, 품명|품번
    @GetMapping
    @ResponseBody
    @Operation(
            summary = "LOT Tracking",
            description = "검색조건: LOT 번호, 추적유형, 품명|품번"
    )
    public ResponseEntity<List<LotTrackingResponse>> getLotTrackings(
            @RequestParam(required = false) @Parameter(description = "LOT 번호") String lotNo,
            @RequestParam(required = false) @Parameter(description = "추적유형") Boolean trackingType,
            @RequestParam(required = false) @Parameter(description = "품명|품목") String itemNoAndItemName,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        List<LotTrackingResponse> lotTrackingResponses =
                lotTrackService.getTrackings(lotNo, trackingType, itemNoAndItemName);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getTrackings.");
        return new ResponseEntity<>(lotTrackingResponses, HttpStatus.OK);
    }

}
