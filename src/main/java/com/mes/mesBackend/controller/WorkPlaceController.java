package com.mes.mesBackend.controller;

import com.mes.mesBackend.dto.request.WorkPlaceRequest;
import com.mes.mesBackend.dto.response.WorkPlaceResponse;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.logger.CustomLogger;
import com.mes.mesBackend.logger.LogService;
import com.mes.mesBackend.logger.MongoLogger;
import com.mes.mesBackend.service.WorkPlaceService;
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

import javax.validation.Valid;
import java.util.List;

import static com.mes.mesBackend.helper.Constants.MONGO_TEMPLATE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Tag(name = "work-place", description = "사업장 API")
@RequestMapping(value = "/work-places")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = AUTHORIZATION)
public class WorkPlaceController {
    private final WorkPlaceService workPlaceService;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(WorkPlaceController.class);
    private CustomLogger cLogger;

    // 사업장 생성
    @PostMapping
    @ResponseBody
    @Operation(summary = "사업장 생성")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<WorkPlaceResponse> createWorkPlace(
            @RequestBody @Valid WorkPlaceRequest workPlaceRequest,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        WorkPlaceResponse workPlace = workPlaceService.createWorkPlace(workPlaceRequest);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is created the " + workPlace.getId() + " from createWorkPlace.");
        return new ResponseEntity<>(workPlace, OK);
    }

    // 사업장 단일 조회
    @GetMapping("/{id}")
    @ResponseBody
    @Operation(summary = "사업장 단일 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
            }
    )
    public ResponseEntity<WorkPlaceResponse> getWorkPlace(
            @PathVariable Long id,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        WorkPlaceResponse workPlace = workPlaceService.getWorkPlace(id);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the " + workPlace.getId() + " from getWorkPlace.");
        return new ResponseEntity<>(workPlace, OK);
    }

    // 사업장 전체 조회
    @GetMapping
    @ResponseBody
    @Operation(summary = "사업장 전체 조회")
    public ResponseEntity<List<WorkPlaceResponse>> getWorkPlaces(
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) {
        List<WorkPlaceResponse> workPlaces = workPlaceService.getWorkPlaces();
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is viewed the list of from getWorkPlaces.");
        return new ResponseEntity<>(workPlaces, OK);
    }

    // 사업장 수정
    @PatchMapping("/{id}")
    @ResponseBody()
    @Operation(summary = "사업장 수정")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "not found resource"),
                    @ApiResponse(responseCode = "400", description = "bad request")
            }
    )
    public ResponseEntity<WorkPlaceResponse> updateWorkPlace(
            @PathVariable Long id,
            @RequestBody @Valid WorkPlaceRequest workPlaceRequest,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        WorkPlaceResponse workPlace = workPlaceService.updateWorkPlace(id, workPlaceRequest);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is modified the " + workPlace.getId() + " from updateWorkPlace.");
        return new ResponseEntity<>(workPlace, OK);
    }

    @DeleteMapping("/{id}")
    @ResponseBody()
    @Operation(summary = "사업장 삭제")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "no content"),
                    @ApiResponse(responseCode = "404", description = "not found resource")
            }
    )
    public ResponseEntity deleteWorkPlace(
            @PathVariable Long id,
            @RequestHeader(value = AUTHORIZATION, required = false) @Parameter(hidden = true) String tokenHeader
    ) throws NotFoundException {
        workPlaceService.deleteWorkPlace(id);
        cLogger = new MongoLogger(logger, MONGO_TEMPLATE);
        cLogger.info(logService.getUserCodeFromHeader(tokenHeader) + " is deleted the " + id + " from deleteWorkPlace.");
        return new ResponseEntity(NO_CONTENT);
    }

    // 사업장 페이징 조회
//    @GetMapping
//    @ResponseBody()
//    @Operation(summary = "사업장 페이징 조회")
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
//    public ResponseEntity<Page<WorkPlaceResponse>> getWorkPlaces(
//            @PageableDefault @Parameter(hidden = true) Pageable pageable
//    ) {
//        return new ResponseEntity<>(workPlaceService.getWorkPlaces(pageable), OK);
//    }
}
