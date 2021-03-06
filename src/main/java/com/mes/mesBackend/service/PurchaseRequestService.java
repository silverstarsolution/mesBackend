package com.mes.mesBackend.service;

import com.mes.mesBackend.dto.request.PurchaseRequestRequest;
import com.mes.mesBackend.dto.response.ProduceRequestBomDetail;
import com.mes.mesBackend.dto.response.PurchaseRequestResponse;
import com.mes.mesBackend.entity.PurchaseRequest;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;
// 9-1. 구매요청 등록
public interface PurchaseRequestService {
    // 구매요청 생성
    PurchaseRequestResponse createPurchaseRequest(PurchaseRequestRequest purchaseRequestRequest) throws NotFoundException, BadRequestException;
    // 구매요청 단일조회
    PurchaseRequestResponse getPurchaseRequestResponseOrThrow(Long id) throws NotFoundException;
    // 구매요청 리스트 조회, 검색조건: 요청기간, 제조오더번호, 품목그룹, 품번|품명, 제조사 품번, 완료포함(check)
    List<PurchaseRequestResponse> getPurchaseRequests(LocalDate fromDate, LocalDate toDate, String produceOrderNo, Long itemGroupId, String itemNoAndName, String manufacturerPartNo, Boolean orderCompletion, Boolean purchaseOrderYn, Long purchaseOrderClientId);
    // 구매요청 수정
    PurchaseRequestResponse updatePurchaseRequest(Long id, PurchaseRequestRequest purchaseRequestRequest) throws NotFoundException, BadRequestException;
    // 구매요청 삭제
    void deletePurchaseRequest(Long id) throws NotFoundException, BadRequestException;
    // 구매요청 단일 조회 및 예외
    PurchaseRequest getPurchaseRequestOrThrow(Long id) throws NotFoundException;
    // 수주품목에 해당하는 원부자재
    List<ProduceRequestBomDetail> getProduceOrderBomDetails(Long produceOrderId) throws NotFoundException;
    // 같은 제조오더에 같은 품목이 존재하는지 체크
    Boolean checkPurchaseRequestInItem(Long produceOrderId, Long itemId) throws NotFoundException;
}
