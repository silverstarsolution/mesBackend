package com.mes.mesBackend.service;

import com.mes.mesBackend.dto.request.ProduceOrderRequest;
import com.mes.mesBackend.dto.response.ClientResponse;
import com.mes.mesBackend.dto.response.ProduceOrderDetailResponse;
import com.mes.mesBackend.dto.response.ProduceOrderResponse;
import com.mes.mesBackend.entity.ProduceOrder;
import com.mes.mesBackend.entity.enumeration.OrderState;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;

// 6-1. 제조오더 등록
public interface ProduceOrderService {
    // 제조 오더 생성
    ProduceOrderResponse createProduceOrder(ProduceOrderRequest produceOrderRequest) throws NotFoundException;
    // 제조 오더 단일 조회
    ProduceOrderResponse getProduceOrder(Long produceOrderId) throws NotFoundException;
    // 제조 오더 리스트 조회, 검색조건 : 품목그룹 id, 품명|품번, 지시상태, 제조오더번호, 수주번호, 착수예정일 fromDate~toDate, 자재납기일자(보류)
    List<ProduceOrderResponse> getProduceOrders(
            Long itemGroupId, String
            itemNoAndName,
            OrderState orderState,
            String produceOrderNo,
            String contractNo,
            LocalDate fromDate,
            LocalDate toDate
    );
    // 제조 오더 수정
    ProduceOrderResponse updateProduceOrder(Long produceOrderId, ProduceOrderRequest produceOrderRequest) throws NotFoundException, BadRequestException;
    // 제조 오더 삭제
    void deleteProduceOrder(Long produceOrderId) throws NotFoundException, BadRequestException;
    // 제조 오더 디테일 리스트 조회
    List<ProduceOrderDetailResponse> getProduceOrderDetails(Long produceOrderId) throws NotFoundException;
    // 제조 오더 단일 조회 및 예외
    ProduceOrder getProduceOrderOrThrow(Long id) throws NotFoundException;
    // 수주 등록된 제조사 list 조회 api
    List<ClientResponse.CodeAndName> getContractClients();
}
