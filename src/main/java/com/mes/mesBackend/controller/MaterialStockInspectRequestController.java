package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.request.MaterialStockInspectRequestRequest;
import com.mes.mesBackend.dto.response.MaterialStockInspectRequestResponse;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.repository.custom.MaterialStockInspectRequestRepositoryCustom;
import com.mes.mesBackend.service.MaterialWarehouseService;
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

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

import static com.mes.mesBackend.helper.Constants.MONGO_TEMPLATE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

//재고실사의뢰 등록
@Tag(name = "material-stockinspect-requests", description = "재고실사의뢰 API")
@RequestMapping(value = "/material-stockinspect-requests")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = AUTHORIZATION)
public class MaterialStockInspectRequestController {
    private final MaterialWarehouseService materialWarehouseService;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(MaterialStockInspectRequestRepositoryCustom.class);
    private CustomLogger cLogger;

    //재고실사의뢰 등록
    @PostMapping
    @ResponseBody
    @Operation(summary = "재고실사의뢰 등록")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<MaterialStockInspectRequestResponse> createMaterialStockInspect(
            @RequestBody @Valid MaterialStockInspectRequestRequest request,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException{
        MaterialStockInspectRequestResponse response = materialWarehouseService.createMaterialStockInspectRequest(request);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + "is created the " + response.getId() + " from createMaterialStockInspect.");
        return new ResponseEntity<>(response, OK);
    }
    //재고실사의뢰 조회
    @GetMapping
    @ResponseBody
    @Operation(summary = "재고실사의뢰 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<List<MaterialStockInspectRequestResponse>> getMaterialStockInspects(
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "시작날짜") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "종료날짜") LocalDate toDate,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException{
        List<MaterialStockInspectRequestResponse> responseList = materialWarehouseService.getMaterialStockInspectRequestList(fromDate, toDate);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getMaterialStockInspects.");
        return new ResponseEntity<>(responseList, OK);
    }
    //재고실사의뢰 단건조회
    @GetMapping("/{id}")
    @ResponseBody
    @Operation(summary = "재고실사의뢰 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<MaterialStockInspectRequestResponse> getMaterialStockInspect(
            @PathVariable(value = "id") @Parameter(description = "실사의뢰 ID") Long id,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException{
        MaterialStockInspectRequestResponse response = materialWarehouseService.getMaterialStockInspectRequest(id);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the " + id + " from getMaterialStockInspect.");
        return new ResponseEntity<>(response, OK);
    }
    //재고실사의뢰 수정
    @PatchMapping("/{id}")
    @ResponseBody()
    @Operation(summary = "재고실사의뢰 수정", description = "")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<MaterialStockInspectRequestResponse> updateMaterialStockInspect(
            @PathVariable(value = "id") @Parameter(description = "실사의뢰 id") Long id,
            @RequestBody @Valid MaterialStockInspectRequestRequest request,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        MaterialStockInspectRequestResponse response = materialWarehouseService.modifyMaterialStockInspect(id, request);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is modified the " + id + " from updateMaterialStockInspect.");
        return new ResponseEntity<>(response, OK);
    }
    //재고실사의뢰 삭제
    @DeleteMapping("/{id}")
    @ResponseBody()
    @Operation(summary = "재고실사의뢰 삭제", description = "")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "no content"),
                    @ApiResponse(responseCode = "404", description = "not found resource")
            }
    )
    public ResponseEntity deleteMaterialStockInspect(
            @PathVariable(value = "id") @Parameter(description = "재고실사의뢰 id") Long id,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        materialWarehouseService.deleteMaterialStockInspectRequest(id);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is deleted the " + id + " from deleteMaterialStockInspect.");
        return new ResponseEntity(NO_CONTENT);
    }
}
