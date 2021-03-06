package com.mes.mesBackend.service.impl;

import com.mes.mesBackend.dto.request.WorkCenterRequest;
import com.mes.mesBackend.dto.response.WorkCenterResponse;
import com.mes.mesBackend.entity.Client;
import com.mes.mesBackend.entity.WorkCenter;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.mapper.ModelMapper;
import com.mes.mesBackend.repository.WorkCenterRepository;
import com.mes.mesBackend.service.WorkCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkCenterServiceImpl implements WorkCenterService {
    private final WorkCenterRepository workCenterRepository;
    private final ClientServiceImpl clientService;
    private final ModelMapper mapper;

    // 작업장 생성
    @Override
    public WorkCenterResponse createWorkCenter(WorkCenterRequest workCenterRequest) throws NotFoundException {
        Client outCompany = workCenterRequest.getOutCompany() != null ? clientService.getClientOrThrow(workCenterRequest.getOutCompany()) : null;
        WorkCenter workCenter = mapper.toEntity(workCenterRequest, WorkCenter.class);
        // workcenter, client 추가
        workCenter.addWorkCenterCodeAndClient(outCompany);
        WorkCenter saveWorkCenter = workCenterRepository.save(workCenter);
        return mapper.toResponse(saveWorkCenter, WorkCenterResponse.class);
    }

    // 단일조회
    @Override
    public WorkCenterResponse getWorkCenter(Long workCenterId) throws NotFoundException{
        WorkCenter workCenter = getWorkCenterOrThrow(workCenterId);
        return mapper.toResponse(workCenter, WorkCenterResponse.class);
    }

    // 전체조회
    @Override
    public List<WorkCenterResponse> getWorkCenters() {
        List<WorkCenter> workCenters = workCenterRepository.findAllByDeleteYnFalseOrderByCreatedDateDesc();
        return mapper.toListResponses(workCenters, WorkCenterResponse.class);
    }

    // 페이징조회
//    @Override
//    public Page<WorkCenterResponse> getWorkCenters(Pageable pageable) {
//        Page<WorkCenter> workCenters = workCenterRepository.findAllByDeleteYnFalse(pageable);
//        return mapper.toPageResponses(workCenters, WorkCenterResponse.class);
//    }

    // 수정
    @Override
    public WorkCenterResponse updateWorkCenter(Long workCenterId, WorkCenterRequest workCenterRequest) throws NotFoundException {
        WorkCenter findWorkCenter = getWorkCenterOrThrow(workCenterId);

        Client newOutCompany = workCenterRequest.getOutCompany() != null ? clientService.getClientOrThrow(workCenterRequest.getOutCompany()) : null;

        WorkCenter newWorkCenter = mapper.toEntity(workCenterRequest, WorkCenter.class);

        findWorkCenter.put(newWorkCenter, newOutCompany);
        workCenterRepository.save(findWorkCenter);
        return mapper.toResponse(findWorkCenter, WorkCenterResponse.class);
    }

    // 삭제
    @Override
    public void deleteWorkCenter(Long workCenterId) throws NotFoundException {
        WorkCenter workCenter = getWorkCenterOrThrow(workCenterId);
        workCenter.delete();
        workCenterRepository.save(workCenter);
    }

//    // 코드생성
//    @Override
//    public CodeResponse createWorkCenterCode(CodeRequest codeRequest) {
//        WorkCenterCode workCenterCode = mapper.toEntity(codeRequest, WorkCenterCode.class);
//        WorkCenterCode saveCode = workCenterCodeRepository.save(workCenterCode);
//        return mapper.toResponse(saveCode, CodeResponse.class);
//    }
//
//    // 코드 리스트조회
//    @Override
//    public List<CodeResponse> getWorkCenterCodes() {
//        return mapper.toListResponses(workCenterCodeRepository.findAll(), CodeResponse.class);
//    }
//
//    // 코드삭제
//    @Override
//    public void deleteWorkCenterCode(Long id) throws NotFoundException, BadRequestException {
//        throwIfWorkCenterCodeExists(id);
//        workCenterCodeRepository.deleteById(id);
//    }
//
//    // 조회 및 예외처리
    @Override
    public WorkCenter getWorkCenterOrThrow(Long id) throws NotFoundException {
        if (id == 0) {
            return null;
        }
        return workCenterRepository.findByIdAndDeleteYnFalse(id)
                .orElseThrow(() -> new NotFoundException("workCenter does not found. input id: " + id));
    }
//
//    // 단일조회
//    @Override
//    public CodeResponse getWorkCenterCode(Long id) throws NotFoundException {
//        WorkCenterCode workCenterCode = getWorkCenterCodeOrThrow(id);
//        return mapper.toResponse(workCenterCode, CodeResponse.class);
//    }
//
//    // 코드 단일조회 및 예외
//    private WorkCenterCode getWorkCenterCodeOrThrow(Long id) throws NotFoundException {
//        return workCenterCodeRepository.findById(id).orElseThrow(
//                () -> new NotFoundException("workCenterCode does not exists. input id: " + id));
//    }
//
//    // 코드 삭제 시 workCenter에 해당하는 코드가 있으면 예외
//    private void throwIfWorkCenterCodeExists(Long id) throws NotFoundException, BadRequestException {
//        WorkCenterCode workCenterCode = getWorkCenterCodeOrThrow(id);
//        boolean existsByWorkCenter = workCenterRepository.existsAllByWorkCenterCodeAndDeleteYnFalse(workCenterCode);
//        if (existsByWorkCenter) throw new BadRequestException("code exists in the workCenter. input code id: " + id);
//    }
}
