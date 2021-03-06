package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.request.DevelopmentRequest;
import com.mes.mesBackend.dto.request.DevelopmentStateRequest;
import com.mes.mesBackend.dto.response.DevelopmentResponse;
import com.mes.mesBackend.dto.response.DevelopmentStateReponse;
import com.mes.mesBackend.entity.enumeration.DevelopmentChildrenStatusType;
import com.mes.mesBackend.entity.enumeration.DevelopmentStatusType;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.DevelopmentService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static com.mes.mesBackend.helper.Constants.MONGO_TEMPLATE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

// 4-1. 개발 품목 등록
@Tag(name = "development", description = "개발 등록 API")
@RequestMapping(value = "/developments")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = AUTHORIZATION)
public class DevelopmentController {
    private final DevelopmentService developmentService;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(DevelopmentController.class);
    private CustomLogger cLogger;

    // 개발 품목 추가
    @PostMapping
    @ResponseBody
    @Operation(summary = "개발 품목 추가")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<DevelopmentResponse> createDevelopment(
            @RequestBody @Valid DevelopmentRequest request,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException, BadRequestException {
        DevelopmentResponse response = developmentService.createDevelopment(request);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is created the " + response.getId() + " from createDevelopment.");
        return new ResponseEntity<>(response, OK);
    }

    // 개발 품목 리스트 조회
    @GetMapping
    @ResponseBody
    @Operation(summary = "개발 품목 리스트 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<List<DevelopmentResponse>> getDevelopments(
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "시작날짜") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "종료날짜") LocalDate toDate,
            @RequestParam(required = false) @Parameter(description = "사용자 ID") Long userId,
            @RequestParam(required = false) @Parameter(description = "품번") String itemNoOrItemName,
            @RequestParam(required = false) @Parameter(description = "개발상태(상위) [COMPLETE_REPORT: 완료보고, ETC: 기타, (정의안된 나머지는 그대로]") DevelopmentStatusType status,
            @RequestParam(required = false)
            @Parameter(description =
                    "개발상태(하위) [ORDER : 수주, DEVELOPMENT_REQUEST : 개발의뢰서, VALIDATION_CHECK : 타당성 검토, DEVELOPMENT_PLAN : 개발계획서, DEVELOPMENT_START : 개발착수회의, DESIGN_PLAN : 설계, DESIGN_REVIEW : 디자인 리뷰회의, PRODUCT_VERIFICATION : 제품검증, PROTOTYPE_EVALUATION : 시제품 평가 회의, STANDARD_DRAWING : 규격도면, COMPLETE_REPORT : 완료보고, OTHER_DOCUMENT : 기타문서, MINUTES : 회의록]"
            ) DevelopmentChildrenStatusType childrenStatus,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        List<DevelopmentResponse> responseList = developmentService.getDevelopments(
                fromDate, toDate, userId, itemNoOrItemName, status, childrenStatus
        );
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is view list of development from getDevelopments.");
        return new ResponseEntity<>(responseList, OK);
    }

    //개발 품목 단건 조회
    @GetMapping("/{development-id}")
    @ResponseBody
    @Operation(summary = "개발 품목 단건 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<DevelopmentResponse> getDevelopment(
            @PathVariable(value = "development-id") @Parameter(description = "개발품목 ID") Long id,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        DevelopmentResponse response = developmentService.getDevelopment(id);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is view development : " + id + "from getDevelopment");
        return new ResponseEntity<>(response, OK);
    }

    //개발 품목 수정
    @PutMapping("/{development-id}")
    @ResponseBody
    @Operation(summary = "개발 품목 수정")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<DevelopmentResponse> modifyDevelopment(
            @PathVariable(value = "development-id") @Parameter(description = "개발품목 ID") Long id,
            @RequestBody @Valid DevelopmentRequest request,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException, BadRequestException {
        DevelopmentResponse response = developmentService.modifyDevelopment(id, request);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is modify development : " + id + "from modifyDevelopment");
        return new ResponseEntity<>(response, OK);
    }

    //개발 품목 삭제
    @DeleteMapping("/{development-id}")
    @ResponseBody
    @Operation(summary = "개발 품목 삭제")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity deleteDevelopment(
            @PathVariable(value = "development-id") @Parameter(description = "개발품목 ID") Long id,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        developmentService.deleteDevelopment(id);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is delete development : " + id + "from deleteDevelopment");
        return new ResponseEntity(NO_CONTENT);
    }

    // 개발 품목 진행 상태 추가
    @PostMapping("/{development-id}/development-states")
    @ResponseBody
    @Operation(summary = "개발 품목 진행 상태 추가")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<DevelopmentStateReponse> createDevelopmentState(
            @PathVariable(value = "development-id") @Parameter(description = "개발품목 ID") Long id,
            @RequestBody @Valid DevelopmentStateRequest request,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        DevelopmentStateReponse response = developmentService.createDevelopmentState(id, request);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is created the " + response.getId() + " from createDevelopmentState.");
        return new ResponseEntity<>(response, OK);
    }

    // 개발 품목 진행 상태 리스트 조회
    @GetMapping("/{development-id}/development-states")
    @ResponseBody
    @Operation(summary = "개발 품목 진행 상태 리스트 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<List<DevelopmentStateReponse>> getDevelopmentStateList(
            @PathVariable(value = "development-id") @Parameter(description = "개발품목 ID") Long id,
            @RequestParam(required = false) @Parameter(description = "개발상태(상위) [COMPLETE_REPORT: 완료보고, ETC: 기타, (정의안된 나머지는 그대로]") DevelopmentStatusType status,
            @RequestParam(required = false)
            @Parameter(description =
                    "개발상태(하위) [ORDER : 수주, DEVELOPMENT_REQUEST : 개발의뢰서, VALIDATION_CHECK : 타당성 검토, DEVELOPMENT_PLAN : 개발계획서, DEVELOPMENT_START : 개발착수회의, DESIGN_PLAN : 설계, DESIGN_REVIEW : 디자인 리뷰회의, PRODUCT_VERIFICATION : 제품검증, PROTOTYPE_EVALUATION : 시제품 평가 회의, STANDARD_DRAWING : 규격도면, COMPLETE_REPORT : 완료보고, OTHER_DOCUMENT : 기타문서, MINUTES : 회의록]"
            )
                    DevelopmentChildrenStatusType childrenStatus,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        List<DevelopmentStateReponse> responseList = developmentService.getDevelopmentStateList(id, status, childrenStatus);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is view list of development from getDevelopments.");
        return new ResponseEntity<>(responseList, OK);
    }

    //개발 품목 진행 상태 단건 조회
    @GetMapping("/{development-id}/development-states/{state-id}")
    @ResponseBody
    @Operation(summary = "개발 품목 단건 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<DevelopmentStateReponse> getDevelopmentState(
            @PathVariable(value = "development-id") @Parameter(description = "개발품목 ID") Long id,
            @PathVariable(value = "state-id") @Parameter(description = "개발품목 진행 상태 ID") Long stateId,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        DevelopmentStateReponse response = developmentService.getDevelopmentState(id, stateId);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is view development state : " + id + "from getDevelopmentState");
        return new ResponseEntity<>(response, OK);
    }

    //개발 품목 진행 상태 수정
    @PutMapping("/{development-id}/development-states/{state-id}")
    @ResponseBody
    @Operation(summary = "개발 품목 진행 상태 수정")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<DevelopmentStateReponse> modifyDevelopmentState(
            @PathVariable(value = "development-id") @Parameter(description = "개발품목 ID") Long id,
            @PathVariable(value = "state-id") @Parameter(description = "개발품목 진행 상태 ID") Long stateId,
            @RequestBody @Valid DevelopmentStateRequest request,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        DevelopmentStateReponse response = developmentService.modifyDevelopmentState(id, stateId, request);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is modify development state : " + id + "from modifyDevelopmentState");
        return new ResponseEntity<>(response, OK);
    }

    //개발 품목 진행 상태 삭제
    @DeleteMapping("/{development-id}/development-states/{state-id}")
    @ResponseBody
    @Operation(summary = "개발 품목 진행 상태 삭제")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity deleteDevelopmentState(
            @PathVariable(value = "development-id") @Parameter(description = "개발품목 ID") Long id,
            @PathVariable(value = "state-id") @Parameter(description = "개발품목 진행 상태 ID") Long stateId,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        developmentService.deleteDevelopmentState(id, stateId);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is delete development state : " + stateId + "from deleteDevelopmentState");
        return new ResponseEntity(NO_CONTENT);
    }

    // 개발 품목 진행 상태 파일 추가
    @PutMapping(value = "/{development-id}/development-states/{state-id}/development-state-file",  consumes = MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @Operation(summary = "개발 품목 진행 상태 파일 추가")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<DevelopmentStateReponse> createFileDevelopmentState(
            @PathVariable(value = "development-id") @Parameter(description = "개발 품목 id") Long id,
            @PathVariable(value = "state-id") @Parameter(description = "개발 품목 진행 상태 id") Long stateId,
            @RequestPart MultipartFile file,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException, IOException, BadRequestException {
        DevelopmentStateReponse response = developmentService.createStatusFile(id, stateId, file);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is created the " + response + " from createFileDevelopmentState.");
        return new ResponseEntity<>(response, OK);
    }
}
