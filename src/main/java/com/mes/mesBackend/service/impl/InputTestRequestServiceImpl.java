package com.mes.mesBackend.service.impl;

import com.mes.mesBackend.dto.request.InputTestRequestRequest;
import com.mes.mesBackend.dto.response.InputTestRequestResponse;
import com.mes.mesBackend.entity.InputTestRequest;
import com.mes.mesBackend.entity.LotMaster;
import com.mes.mesBackend.entity.enumeration.TestType;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.mapper.ModelMapper;
import com.mes.mesBackend.repository.InputTestRequestRepository;
import com.mes.mesBackend.repository.LotMasterRepository;
import com.mes.mesBackend.service.InputTestRequestService;
import com.mes.mesBackend.service.LotMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// 14-1. 검사의뢰 등록
@Service
@RequiredArgsConstructor
public class InputTestRequestServiceImpl implements InputTestRequestService {
    private final LotMasterService lotMasterService;
    private final InputTestRequestRepository inputTestRequestRepo;
    private final ModelMapper modelMapper;
    private final LotMasterRepository lotMasterRepo;

    /*
    * 검사의뢰 생성
    * lotMaster.checkRequestAmount 검사요청수량 변경
    * 검사유형: 추후 non 측과 협의 후 변경되어야 함
    * 예외: 입고된 갯수만큼만 요청수량을 등록 할 수 있음
    * */
    @Override
    public InputTestRequestResponse createInputTestRequest(InputTestRequestRequest inputTestRequestRequest) throws NotFoundException, BadRequestException {
        ifThrowRequestAmountGreaterThanInputAmount(inputTestRequestRequest.getLotId(), inputTestRequestRequest.getRequestAmount());       // 요청수량 재고수량 비교
        LotMaster lotMaster = lotMasterService.getLotMasterOrThrow(inputTestRequestRequest.getLotId());
        InputTestRequest inputTest = modelMapper.toEntity(inputTestRequestRequest, InputTestRequest.class);
        inputTest.create(lotMaster);                // 상태값: SCHEDULE
        inputTestRequestRepo.save(inputTest);       // lotMaster, 요청유형, 요청수량, 검사유형, 상태값 생성
        lotMaster.setCheckRequestAmount(inputTestRequestRequest.getRequestAmount());    // lotMaster 검사요청수량 변경
        lotMasterRepo.save(lotMaster);
        return getInputTestRequestResponse(inputTest.getId());
    }


    // 검사의뢰 리스트 검색 조회,
    // 검색조건: 창고 id, LOT 유형 id, 품명|품목, 검사유형, 품목그룹, 요청유형, 의뢰기간
    @Override
    public List<InputTestRequestResponse> getInputTestRequests(
            Long warehouseId,
            Long lotTypeId,
            String itemNoAndName,
            TestType testType,
            Long itemGroupId,
            TestType requestType,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return inputTestRequestRepo.findAllByCondition(warehouseId, lotTypeId, itemNoAndName, testType, itemGroupId, requestType, fromDate, toDate);
    }

    // 검사의뢰 단일 조회
    @Override
    public InputTestRequestResponse getInputTestRequestResponse(Long id) throws NotFoundException {
        return inputTestRequestRepo.findResponseByIdAndDeleteYnFalse(id)
                .orElseThrow(() -> new NotFoundException("inputTestRequest does not exist. input id: " + id));
    }

    // 검사의뢰 수정
    @Override
    public InputTestRequestResponse updateInputTestRequest(Long id, InputTestRequestRequest inputTestRequestRequest) throws NotFoundException, BadRequestException {
        ifThrowRequestAmountGreaterThanInputAmount(inputTestRequestRequest.getLotId(), inputTestRequestRequest.getRequestAmount());     // 요청수량 재고수량 비교
        InputTestRequest findInputTestRequest = getInputTestRequestOrThrow(id);
        InputTestRequest newInputTestRequest = modelMapper.toEntity(inputTestRequestRequest, InputTestRequest.class);
        LotMaster newLotMaster = lotMasterService.getLotMasterOrThrow(inputTestRequestRequest.getLotId());
        findInputTestRequest.update(newLotMaster, newInputTestRequest);
        inputTestRequestRepo.save(findInputTestRequest);
        newLotMaster.setCheckRequestAmount(inputTestRequestRequest.getRequestAmount());     // lotMaster 검사요청수량 변경
        lotMasterRepo.save(newLotMaster);
        return getInputTestRequestResponse(findInputTestRequest.getId());
    }

    // 검사의뢰 삭제
    @Override
    public void deleteInputTestRequest(Long id) throws NotFoundException {
        InputTestRequest inputTestRequest = getInputTestRequestOrThrow(id);
        inputTestRequest.delete();
        inputTestRequestRepo.save(inputTestRequest);
    }

    // 검사의뢰 단일 조회 및 예외
    private InputTestRequest getInputTestRequestOrThrow(Long id) throws NotFoundException {
        return inputTestRequestRepo.findByIdAndDeleteYnFalse(id)
                .orElseThrow(() -> new NotFoundException("inputTestRequest does not exist. input id: " + id));
    }

    // 입고된 갯수만큼만 요청수량을 등록 할 수 있음.
    // 요청수량 재고수량 비교
    private void ifThrowRequestAmountGreaterThanInputAmount(Long lotId, int requestAmount) throws BadRequestException {
        Integer stockAmountFromLotMaster = inputTestRequestRepo.findLotMasterInputAmountByLotMasterId(lotId);
        if (requestAmount > stockAmountFromLotMaster)
            throw new BadRequestException("input requestAmount must not be greater than inputAmount. " +
                    "input requestAmount: " + requestAmount + ", inputAmount: " + stockAmountFromLotMaster);
    }
}