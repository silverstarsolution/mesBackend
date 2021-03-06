package com.mes.mesBackend.service;

import com.mes.mesBackend.dto.request.ShipmentReturnRequest;
import com.mes.mesBackend.dto.response.ShipmentReturnLotResponse;
import com.mes.mesBackend.dto.response.ShipmentReturnResponse;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

// 4-6. 출하반품 등록
public interface ShipmentReturnService {
    // 출하반품 생성
    ShipmentReturnResponse createShipmentReturn(ShipmentReturnRequest shipmentReturnRequest) throws NotFoundException, BadRequestException;
    // 출하반품 리스트 검색 조회, 검색조건: 거래처 id, 품번|품명, 반품기간 fromDate~toDate
    List<ShipmentReturnResponse> getShipmentReturns(Long clientId, String itemNoAndItemName, LocalDate fromDate, LocalDate toDate);
    // 출하반품 수정
    ShipmentReturnResponse updateShipmentReturn(Long id, ShipmentReturnRequest shipmentReturnRequest) throws NotFoundException, BadRequestException;
    // 출하반품 삭제
    void deleteShipmentReturn(Long id) throws NotFoundException;
    // clientId 로 shipmentIds 조회
    Page<ShipmentReturnLotResponse> getShipmentLots(Long clientId, LocalDate fromDate, LocalDate toDate, Pageable pageable) throws NotFoundException;
    // 바코드 번호로 조회
    ShipmentReturnLotResponse getShipmentLotByBarcodeNo(String barcodeNo) throws NotFoundException, BadRequestException;
}
