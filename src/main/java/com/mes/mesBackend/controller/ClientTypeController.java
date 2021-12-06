package com.mes.mesBackend.controller;


import com.mes.mesBackend.dto.request.ClientTypeRequest;
import com.mes.mesBackend.dto.response.ClientTypeResponse;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.ClientTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "client-type", description = "거래처 유형 API")
@RequestMapping(value = "/client-types")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class ClientTypeController {

    @Autowired
    ClientTypeService clientTypeService;
    @Autowired
    LogService logService;

    private Logger logger = LoggerFactory.getLogger(ClientTypeController.class);
    private CustomLogger cLogger;


    // 거래처 유형 생성
    @PostMapping
    @ResponseBody()
    @Operation(summary = "거래처유형 생성")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<ClientTypeResponse> createClientType(
            @RequestBody @Valid ClientTypeRequest clientTypeRequest,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        ClientTypeResponse clientType = clientTypeService.createClientType(clientTypeRequest);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + "is created the " + clientType.getId() + " from createClientType.");
        return new ResponseEntity<>(clientType, HttpStatus.OK);
    }

    // 거래처 유형 단일 조회
    @GetMapping("/{id}")
    @ResponseBody()
    @Operation(summary = "거래처유형 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
            }
    )
    public ResponseEntity<ClientTypeResponse> getClientType(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        ClientTypeResponse clientType = clientTypeService.getClientType(id);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the " + clientType.getId() + " from getClientType.");
        return new ResponseEntity<>(clientType, HttpStatus.OK);
    }

    // 거래처 유형 전체 조회
    @Operation(summary = "거래처유형 전체 조회")
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ClientTypeResponse>> getClientTypes(
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        List<ClientTypeResponse> clientTypes = clientTypeService.getClientTypes();
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getClientTypes.");
        return new ResponseEntity<>(clientTypes, HttpStatus.OK);
    }

    // 거래처 유형 수정
    @PatchMapping("/{id}")
    @ResponseBody()
    @Operation(summary = "거래처유형 수정")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<ClientTypeResponse> updateClientType(
            @PathVariable Long id,
            @RequestBody @Valid ClientTypeRequest clientTypeRequest,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        ClientTypeResponse clientType = clientTypeService.updateClientType(id, clientTypeRequest);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is modified the " + clientType.getId() + " from updateClientType.");
        return new ResponseEntity<>(clientType, HttpStatus.OK);
    }

    // 거래처유형 삭제
    @DeleteMapping("/{id}")
    @ResponseBody()
    @Operation(summary = "거래처유형 삭제")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "no content"),
                    @ApiResponse(responseCode = "404", description = "not found resource")
            }
    )
    public ResponseEntity<Void> deleteClientType(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        clientTypeService.deleteClientType(id);
        cLogger = new MongoLogger(logger, "mongoTemplate");
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is deleted the " + id + " from deleteClientType.");
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

//    @GetMapping
//    @ResponseBody()
//    @Operation(summary = "거래처유형 페이징 조회")
//    @Parameters(
//            value = {
//                    @Parameter(
//                            name = "page", description = "0 부터 시작되는 페이지 (0..N)",
//                            in = ParameterIn.QUERY,
//                            schema = @Schema(type = "integer", defaultValue = "0")
//                    ),
//                    @Parameter(
//                            name = "size", description = "페이지의 사이즈",
//                            in = ParameterIn.QUERY,
//                            schema = @Schema(type = "integer", defaultValue = "20")
//                    ),
//                    @Parameter(
//                            name = "sort", in = ParameterIn.QUERY,
//                            description = "정렬할 대상과 정렬 방식, 데이터 형식: property(,asc|desc). + 디폴트 정렬순서는 오름차순, 다중정렬 가능",
//                            array = @ArraySchema(schema = @Schema(type = "string", defaultValue = "id,desc"))
//                    )
//            }
//    )
//    public ResponseEntity<Page<ClientTypeResponse>> getClientTypes(
//            @PageableDefault @Parameter(hidden = true) Pageable pageable
//    ) {
//        return new ResponseEntity<>(clientTypeService.getClientTypes(pageable), HttpStatus.OK);
//    }

}
