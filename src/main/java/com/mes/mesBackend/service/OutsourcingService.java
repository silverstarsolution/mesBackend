package com.mes.mesBackend.service;

import com.mes.mesBackend.dto.request.*;
import com.mes.mesBackend.dto.response.*;
import com.mes.mesBackend.entity.LotMaster;
import com.mes.mesBackend.entity.OutsourcingReturn;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OutsourcingService {
    //외주생산의뢰 등록
    OutsourcingProductionResponse createOutsourcingProduction(OutsourcingProductionRequestRequest outsourcingProductionRequestRequest);

    //외주생산의뢰 리스트조회
    List<OutsourcingProductionResponse> getOutsourcingProductions(Long clientId, Long itemNo, LocalDate startDate, LocalDate endDate);

    //외주생산의뢰 조회
    Optional<OutsourcingProductionResponse> getOutsourcingProduction(Long id);

    //외주생산의뢰 수정
    OutsourcingProductionResponse modifyOutsourcingProduction(Long id, OutsourcingProductionRequestRequest outsourcingProduction);

    //외주생산의뢰 삭제
    void deleteOutsourcingProduction(Long id);

    //외주생산 원재료 출고 대상 등록
    Optional<OutsourcingMaterialReleaseResponse> createOutsourcingMaterial(Long id, OutsourcingMaterialReleaseRequest outsourcingMaterialReleaseRequest);

    //외주생산 원재료 출고 대상 리스트 조회
    List<OutsourcingMaterialReleaseResponse> getOutsourcingMeterials(Long productionId);

    //외주생산 원재료 출고 대상 단일 조회
    OutsourcingMaterialReleaseResponse getOutsourcingMaterial(Long requestId, Long materialId) throws NotFoundException;

    //외주생산 원재료 출고 대상 수정
    OutsourcingMaterialReleaseResponse modifyOutsourcingMaterial(Long requestId, Long materialId, OutsourcingMaterialReleaseRequest request) throws NotFoundException;

    //외주생산 원재료 출고 대상 삭제
    void deleteOutsourcingMaterial(Long requestId, Long id);

    //외주 입고정보 등록
    OutsourcingInputResponse createOutsourcingInput(OutsourcingInputRequest request) throws NotFoundException;

    //외주 입고정보 리스트조회
    List<OutsourcingInputResponse> getOutsourcingInputList(Long clientId, Long itemId, LocalDate startDate, LocalDate endDate);

    //외주 입고정보 조회
    OutsourcingInputResponse getOutsourcingInput(Long id) throws NotFoundException;

    //외주 입고정보 수정
    OutsourcingInputResponse modifyOutsourcingInput(Long inputId, OutsourcingInputRequest request) throws NotFoundException;

    //외주 입고정보 삭제
    void deleteOutsourcingInput(Long id) throws NotFoundException;

    //외주 입고 LOT정보 등록
    OutsourcingInputLOTResponse createOutsourcingInputLOT(Long id, OutsourcingInputLOTRequest request) throws NotFoundException, BadRequestException;

    //외주 입고 LOT정보 리스트조회
    List<OutsourcingInputLOTResponse> getOutsourcingInputLOTList(Long inputId);

    //외주 입고 LOT정보 조회
    OutsourcingInputLOTResponse getOutsourcingInputLOT(Long inputId, Long id) throws NotFoundException;

    //외주 입고 LOT정보 수정
    OutsourcingInputLOTResponse modifyOutsourcingInputLOT(Long inputId, Long id, OutsourcingInputLOTRequest request) throws NotFoundException;

    //외주 입고 LOT정보 삭제
    void deleteOutsourcingInputLOT(Long inputId, Long id) throws NotFoundException;

    //외주 반품 등록
    OutsourcingReturnResponse createOutsourcingReturn(OutsourcingReturnRequest request) throws NotFoundException, BadRequestException;

    //외주 반품 리스트조회
    List<OutsourcingReturnResponse> getOutsourcingReturnList(Long clientId, Long itemId, LocalDate startDate, LocalDate endDate);

    //외주 반품 조회
    OutsourcingReturnResponse getOutsourcingReturn(Long returnId) throws NotFoundException;

    //외주 반품 수정
    OutsourcingReturnResponse modifyOutsourcingReturn(Long returnId, OutsourcingReturnRequest request) throws NotFoundException, BadRequestException;

    //외주 반품 삭제
    void deleteOutsourcingReturn(Long id) throws NotFoundException;

    //외주 현황 조회
    List<OutsourcingStatusResponse> getOutsourcingStatusList(Long clientId, Long itemId, LocalDate startDate, LocalDate endDate);
}
