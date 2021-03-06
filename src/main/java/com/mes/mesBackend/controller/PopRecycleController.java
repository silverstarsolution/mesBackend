package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.request.PopRecycleRequest;
import com.mes.mesBackend.dto.request.RecycleRequest;
import com.mes.mesBackend.dto.response.PopRecycleCreateResponse;
import com.mes.mesBackend.dto.response.PopRecycleResponse;
import com.mes.mesBackend.dto.response.RecycleLotResponse;
import com.mes.mesBackend.dto.response.RecycleResponse;
import com.mes.mesBackend.entity.enumeration.WorkProcessDivision;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.PopRecycleService;
import com.mes.mesBackend.service.RecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

import static com.mes.mesBackend.helper.Constants.MONGO_TEMPLATE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

@RequestMapping("/pop/recycles")
@Tag(name = "pop-recycle", description = "[pop] 8. 재사용")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = AUTHORIZATION)
public class PopRecycleController {
    private final PopRecycleService popRecycleService;
    private final RecycleService recycleService;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(PopRecycleController.class);
    private CustomLogger cLogger;

    //재사용 목록 조회
    @GetMapping
    @ResponseBody
    @Operation(summary = "(pop) 재사용 목록 조회", description = "검색 조건: 공정타입")
    public ResponseEntity<List<PopRecycleResponse>> getUseRecycles(
            @RequestParam @Parameter(description = "공정 구분값") WorkProcessDivision workProcessDivision ,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
       List<PopRecycleResponse> responseList = popRecycleService.getRecycles(workProcessDivision);
       cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
       cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getUseRecycles");
       return new ResponseEntity<>(responseList, OK);
    }

    // 재사용 유형 리스트 조회
    @GetMapping("/recycle-list")
    @ResponseBody
    @Operation(
            summary = "재사용 유형 리스트 조회"
    )
    public ResponseEntity<List<RecycleResponse>> getRecycles(
            @RequestParam(required = false) @Parameter(description = "공정 구분값") WorkProcessDivision workProcessDivision,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        List<RecycleResponse> responseList = recycleService.getRecycles(workProcessDivision);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getRecycles.");
        return new ResponseEntity<>(responseList, OK);
    }

    //재사용 등록
    @PostMapping
    @ResponseBody
    @Operation(
            summary = "재사용 등록"
    )
    public ResponseEntity<PopRecycleCreateResponse> createUseRecycle(
            @RequestBody @Valid PopRecycleRequest request,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException, BadRequestException{
        PopRecycleCreateResponse response = popRecycleService.createUseRecycle(request);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is created the RecycleLot");
        return new ResponseEntity<>(response, OK);
    }

    // 재사용 생성 LOT리스트 조회
    @GetMapping("/recycle-lot-list")
    @ResponseBody
    @Operation(
            summary = "재사용 생성 LOT리스트 조회"
    )
    public ResponseEntity<List<RecycleLotResponse>> getRecycleLots(
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "생성일자 fromDate") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "생성일자 toDate") LocalDate toDate,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        List<RecycleLotResponse> responseList = popRecycleService.getRecycleLots(fromDate, toDate);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getRecycles.");
        return new ResponseEntity<>(responseList, OK);
    }
}
