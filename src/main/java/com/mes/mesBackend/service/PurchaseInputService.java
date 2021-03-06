package com.mes.mesBackend.service;

import com.mes.mesBackend.dto.request.PurchaseInputRequest;
import com.mes.mesBackend.dto.response.LabelPrintResponse;
import com.mes.mesBackend.dto.response.PurchaseInputDetailResponse;
import com.mes.mesBackend.dto.response.PurchaseInputResponse;
import com.mes.mesBackend.dto.response.PurchaseStatusCheckResponse;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;

// 9-5. 구매입고 등록
public interface PurchaseInputService {
    // 구매입고 리스트 조회, 검색조건: 입고기간 fromDate~toDate, 입고창고, 거래처, 품명|품번
    List<PurchaseInputResponse> getPurchaseInputs(
            LocalDate fromDate,
            LocalDate toDate,
            Long wareHouseId,
            Long clientId,
            String itemNoOrItemName
    );
    // ================================ 구매입고 LOT 정보 ================================
    // 구매입고 LOT 생성
    PurchaseInputDetailResponse createPurchaseInputDetail(Long purchaseRequestId, PurchaseInputRequest purchaseInputRequest) throws NotFoundException, BadRequestException;
    // 구매입고 LOT 전체 조회
    List<PurchaseInputDetailResponse> getPurchaseInputDetails(Long purchaseRequestId) throws NotFoundException;
    // 구메입고 LOT 단일 조회
    PurchaseInputDetailResponse getPurchaseInputDetailResponse(Long purchaseRequestId, Long purchaseInputId) throws NotFoundException;
    // 구매입고 LOT 수정
    PurchaseInputDetailResponse updatePurchaseInputDetail(Long purchaseRequestId, Long purchaseInputId, PurchaseInputRequest.updateRequest purchaseInputUpdateRequest) throws NotFoundException, BadRequestException;
    // 구매입고 LOT 삭제
    void deletePurchaseInputDetail(Long purchaseRequestId, Long purchaseInputId) throws NotFoundException, BadRequestException;

    // 구매입고 LOT 단일 조회 및 예외
//    PurchaseInput getPurchaseInputOrThrow(Long id) throws NotFoundException;

    // =========================================== 9-4. 구매현황 조회 ===========================================
    // 구매현황 리스트 조회
    // 검색조건: 거래처 id, 품명|품목, 입고기간 fromDate~toDate
    List<PurchaseStatusCheckResponse> getPurchaseStatusChecks(Long clientId, String itemNoAndItemName, LocalDate fromDate, LocalDate toDate) throws NotFoundException;

    // 금일기준 입고된 자재목록
    List<LabelPrintResponse> getTodayPurchaseInputs(LocalDate fromDate, LocalDate toDate);
}
