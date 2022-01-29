package com.mes.mesBackend.service;

import com.mes.mesBackend.dto.response.PopWorkOrderResponse;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;

import java.util.List;

// pop
public interface PopService {
    // 작업지시 정보 리스트 api, 조건: 작업자, 작업공정
    List<PopWorkOrderResponse> getPopWorkOrders(Long workProcessId) throws NotFoundException;

    // 작업지시 상태 변경
    /*
     *  path: 작업지시 고유번호
     *  request: 품목고유번호, 작업수량
     *  return: LOT 고유아이디
     * */
    Long createCreateWorkOrder(Long workOrderId, Long itemId, String userCode, int productAmount) throws NotFoundException, BadRequestException;
    // 작업지시 상세 정보
    // 위에 해당 작업지시로 bomItemDetail 항목들 가져오기(품번, 품명, 계정, bom 수량, 예약수량)
//    List<PopWorkOrderDetailResponse> getPopWorkOrderDetails(Long lotMasterId, Long workOrderId) throws NotFoundException;
}
