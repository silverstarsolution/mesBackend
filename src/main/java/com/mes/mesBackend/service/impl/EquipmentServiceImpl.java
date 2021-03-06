package com.mes.mesBackend.service.impl;

import com.mes.mesBackend.dto.request.EquipmentRequest;
import com.mes.mesBackend.dto.response.EquipmentResponse;
import com.mes.mesBackend.dto.response.PopEquipmentResponse;
import com.mes.mesBackend.entity.Client;
import com.mes.mesBackend.entity.Equipment;
import com.mes.mesBackend.entity.WorkLine;
import com.mes.mesBackend.entity.WorkProcess;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.mapper.ModelMapper;
import com.mes.mesBackend.repository.EquipmentRepository;
import com.mes.mesBackend.service.ClientService;
import com.mes.mesBackend.service.EquipmentService;
import com.mes.mesBackend.service.WorkLineService;
import com.mes.mesBackend.service.WorkProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 3-5-1. 설비등록
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final WorkLineService workLineService;
    private final ModelMapper mapper;
    private final ClientService clientService;
    private final WorkProcessService workProcessService;

    // 설비 생성
    @Override
    public EquipmentResponse createEquipment(EquipmentRequest equipmentRequest) throws NotFoundException {
        Client client = equipmentRequest.getClient() != null ? clientService.getClientOrThrow(equipmentRequest.getClient()) : null;
        WorkLine workLine = equipmentRequest.getWorkLine() != null ? workLineService.getWorkLineOrThrow(equipmentRequest.getWorkLine()) : null;
        WorkProcess workProcess = equipmentRequest.getWorkProcessId() != null ? workProcessService.getWorkProcessOrThrow(equipmentRequest.getWorkProcessId()) : null;
        Equipment equipment = mapper.toEntity(equipmentRequest, Equipment.class);
        equipment.addJoin(client, workLine, workProcess);
        equipmentRepository.save(equipment);
        return getEquipment(equipment.getId());
    }

    // 설비 단일 조회
    @Override
    public EquipmentResponse getEquipment(Long id) throws NotFoundException {
        Equipment equipment = getEquipmentOrThrow(id);
        EquipmentResponse response = new EquipmentResponse();
        return response.setResponse(equipment);
    }

    // 설비 전체 조회
    @Override
    public List<EquipmentResponse> getEquipments(String equipmentName, Integer checkCycle) {
        List<Equipment> equipments = equipmentRepository.findByCondition(equipmentName, checkCycle);
        List<EquipmentResponse> responses = new ArrayList<>();
        for (Equipment equipment : equipments) {
            EquipmentResponse response = new EquipmentResponse();
            response.setResponse(equipment);
            responses.add(response);
        }
        return responses;
    }
//    public Page<EquipmentResponse> getEquipments(Pageable pageable) {
//        Page<Equipment> equipments = equipmentRepository.findAllByDeleteYnFalse(pageable);
//        return mapper.toPageResponses(equipments, EquipmentResponse.class);
//    }

    // 설비 수정
    @Override
    public EquipmentResponse updateEquipment(Long id, EquipmentRequest equipmentRequest) throws NotFoundException {
        Equipment findEquipment = getEquipmentOrThrow(id);
        Client newClient = equipmentRequest.getClient() != null ? clientService.getClientOrThrow(equipmentRequest.getClient()) : null;
        WorkLine newWorkLine = equipmentRequest.getWorkLine() != null ? workLineService.getWorkLineOrThrow(equipmentRequest.getWorkLine()) : null;
        WorkProcess newWorkProcess = equipmentRequest.getWorkProcessId() != null ? workProcessService.getWorkProcessOrThrow(equipmentRequest.getWorkProcessId()) : null;
        Equipment newEquipment = mapper.toEntity(equipmentRequest, Equipment.class);
        findEquipment.update(newEquipment, newClient, newWorkLine, newWorkProcess);
        equipmentRepository.save(findEquipment);
        return getEquipment(findEquipment.getId());
    }

    // 설비 삭제
    @Override
    public void deleteEquipment(Long id) throws NotFoundException {
        Equipment equipment = getEquipmentOrThrow(id);
        equipment.delete();
        equipmentRepository.save(equipment);
    }

    // 설비 단일 조회 및 예외
    @Override
    public Equipment getEquipmentOrThrow(Long id) throws NotFoundException {
        return equipmentRepository.findByIdAndDeleteYnFalse(id)
                .orElseThrow(() -> new NotFoundException("equipment does not exist. input id: " + id));
    }

    //공정 id로 설비 조회
    public List<PopEquipmentResponse> getEquipmentsByWorkProcess(Long workProcessId) throws NotFoundException {
        return equipmentRepository.findPopEquipmentResponseByWorkProcess(workProcessId, null);
    }
}
