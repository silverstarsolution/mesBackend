package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.request.ProduceOrderRequest;
import com.mes.mesBackend.dto.request.RecycleRequest;
import com.mes.mesBackend.dto.response.ProduceOrderDetailResponse;
import com.mes.mesBackend.dto.response.ProduceOrderResponse;
import com.mes.mesBackend.dto.response.RecycleResponse;
import com.mes.mesBackend.entity.enumeration.OrderState;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.ProduceOrderService;
import com.mes.mesBackend.service.RecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

// 재사용 유형
@RequestMapping(value = "/recycles")
@Tag(name = "recycle", description = "재사용 유형 API")
@RestController
@SecurityRequirement(name = "Authorization")
@Slf4j
@RequiredArgsConstructor
public class RecycleController {

    private final RecycleService recycleService;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(ProduceOrderController.class);
    private CustomLogger cLogger;

    // 재사용 유형 생성
    @Operation(summary = "재사용 유형 생성", description = "")
    @PostMapping
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "400", description = "bad request"),
                    @ApiResponse(responseCode = "404", description = "not found resource")
            }
    )
    public ResponseEntity<RecycleResponse> createRecycle(
            @RequestBody @Valid RecycleRequest request,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        RecycleResponse response = recycleService.createRecycle(request);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is created the " + response.getRecycleId() + " from createRecycle.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 재사용 유형 단일 조회
    @GetMapping("/{recycle-id}")
    @ResponseBody()
    @Operation(summary = "재사용 유형 단일 조회", description = "")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource")
            }
    )
    public ResponseEntity<RecycleResponse> getRecycle(
            @PathVariable(value = "recycle-id") @Parameter(description = "재사용 id") Long recycleId,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        RecycleResponse response = recycleService.getRecycle(recycleId);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the " + response.getRecycleId() + " from getRecycle.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 재사용 유형 리스트 조회
    @GetMapping
    @ResponseBody
    @Operation(
            summary = "재사용 유형 리스트 조회"
    )
    public ResponseEntity<List<RecycleResponse>> getRecycles(
            @RequestParam(required = false) @Parameter(description = "공정 id") Long workProcessId,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        List<RecycleResponse> responseList = recycleService.getRecycles(workProcessId);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getRecycles.");
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    // 재사용 유형 수정
    @PatchMapping("/{recycle-id}")
    @ResponseBody()
    @Operation(summary = "재사용 유형 수정", description = "")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<RecycleResponse> updateRecycle(
            @PathVariable(value = "recycle-id") @Parameter(description = "재사용 id") Long recycleId,
            @RequestBody @Valid RecycleRequest request,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        RecycleResponse response = recycleService.modifyRecycle(recycleId, request);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is modified the " + response.getRecycleId() + " from updateProduceOrder.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 재사용 유형 삭제
    @DeleteMapping("/{recycle-id}")
    @ResponseBody()
    @Operation(summary = "재사용 유형 삭제", description = "")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "no content"),
                    @ApiResponse(responseCode = "404", description = "not found resource")
            }
    )
    public ResponseEntity<Void> deleteRecycle(
            @PathVariable(value = "recycle-id") @Parameter(description = "재사용 id") Long recycleId,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        recycleService.deleteRecycle(recycleId);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is deleted the " + recycleId + " from deleteRecycle.");
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
