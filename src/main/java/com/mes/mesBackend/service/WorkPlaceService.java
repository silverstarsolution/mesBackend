package com.mes.mesBackend.service;

import com.mes.mesBackend.dto.request.WorkPlaceRequest;
import com.mes.mesBackend.dto.response.WorkPlaceResponse;
import com.mes.mesBackend.entity.WorkPlace;
import com.mes.mesBackend.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 사업장
public interface WorkPlaceService {
    // 사업장 생성
    WorkPlaceResponse createWorkPlace(WorkPlaceRequest workPlaceRequest) throws NotFoundException;

    // 사업장 단일 조회
    WorkPlaceResponse getWorkPlace(Long id) throws NotFoundException;

    // 사업장 페이징 조회
    Page<WorkPlaceResponse> getWorkPlaces(Pageable pageable);

    // 사업장 수정
    WorkPlaceResponse updateWorkPlace(Long id, WorkPlaceRequest workPlaceRequest) throws NotFoundException;

    // 사업장 삭제
    void deleteWorkPlace(Long id) throws NotFoundException;

    WorkPlace findByIdAndDeleteYnFalse(Long id) throws NotFoundException;
}
