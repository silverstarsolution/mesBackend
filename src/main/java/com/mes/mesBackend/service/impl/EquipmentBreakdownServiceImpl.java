package com.mes.mesBackend.service.impl;

import com.mes.mesBackend.dto.request.EquipmentBreakdownRequest;
import com.mes.mesBackend.dto.request.RepairPartRequest;
import com.mes.mesBackend.dto.response.*;
import com.mes.mesBackend.entity.*;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.helper.S3Uploader;
import com.mes.mesBackend.mapper.ModelMapper;
import com.mes.mesBackend.repository.*;
import com.mes.mesBackend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 17-2. 설비 고장 수리내역 등록
@Service
@RequiredArgsConstructor
public class EquipmentBreakdownServiceImpl implements EquipmentBreakdownService {
    private final EquipmentBreakdownRepository equipmentBreakdownRepo;
    private final EquipmentBreakdownFileRepository equipmentBreakdownFileRepo;
    private final RepairItemRepository repairItemRepo;
    private final RepairPartRepository repairPartRepo;
    private final RepairWorkerRepository repairWorkerRepo;
    private final EquipmentService equipmentService;
    private final WorkCenterService workCenterService;
    private final RepairCodeService repairCodeService;
    private final UserService userService;
    private final ModelMapper mapper;
    private final S3Uploader s3Uploader;

    // 설비고장 생성
    @Override
    public EquipmentBreakdownResponse createEquipmentBreakdown(EquipmentBreakdownRequest equipmentBreakdownRequest) throws NotFoundException {
        EquipmentBreakdown equipmentBreakdown = mapper.toEntity(equipmentBreakdownRequest, EquipmentBreakdown.class);

        Equipment equipment = equipmentService.getEquipmentOrThrow(equipmentBreakdownRequest.getEquipmentId());
        WorkCenter workCenter = workCenterService.getWorkCenterOrThrow(equipmentBreakdownRequest.getWorkCenterId());

        equipmentBreakdown.add(equipment, workCenter);

        equipmentBreakdownRepo.save(equipmentBreakdown);
        return getEquipmentBreakdown(equipmentBreakdown.getId());
    }

    // 설비고장 리스트 검색 조회, 검색조건: 작업장 id, 설비유형, 작업기간 fromDate~toDate
    @Override
    public List<EquipmentBreakdownResponse> getEquipmentBreakdowns(Long workCenterId, Long workLineId, LocalDate fromDate, LocalDate toDate) {
        List<EquipmentBreakdownResponse> equipmentBreakdownResponses = equipmentBreakdownRepo.findEquipmentBreakdownResponsesByCondition(workCenterId, workLineId, fromDate, toDate);
        for (EquipmentBreakdownResponse response : equipmentBreakdownResponses) {
            List<EquipmentBreakdownFileResponse> beforeFiles = equipmentBreakdownRepo.findBeforeFileResponsesByEquipmentBreakdownId(response.getId());
            List<EquipmentBreakdownFileResponse> afterFiles = equipmentBreakdownRepo.findAfterFileResponsesByEquipmentBreakdownId(response.getId());
            response.setBeforeFiles(beforeFiles);
            response.setAfterFiles(afterFiles);
        }
        return equipmentBreakdownResponses;
    }

    // 설비고장 단일조회
    @Override
    public EquipmentBreakdownResponse getEquipmentBreakdown(Long equipmentBreakdownId) throws NotFoundException {
        EquipmentBreakdownResponse breakdownResponse = equipmentBreakdownRepo.findEquipmentBreakdownResponseById(equipmentBreakdownId)
                .orElseThrow(() -> new NotFoundException("equipmentBreakdown does not exist. input id: " + equipmentBreakdownId));
        List<EquipmentBreakdownFileResponse> beforeFiles = equipmentBreakdownRepo.findBeforeFileResponsesByEquipmentBreakdownId(breakdownResponse.getId());
        List<EquipmentBreakdownFileResponse> afterFiles = equipmentBreakdownRepo.findAfterFileResponsesByEquipmentBreakdownId(breakdownResponse.getId());
        breakdownResponse.setBeforeFiles(beforeFiles);
        breakdownResponse.setAfterFiles(afterFiles);
        return breakdownResponse;
    }

    // 설비고장 수정
    @Override
    public EquipmentBreakdownResponse updateEquipmentBreakdown(Long equipmentBreakdownId, EquipmentBreakdownRequest equipmentBreakdownRequest) throws NotFoundException {
        EquipmentBreakdown findEquipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);

        Equipment newEquipment = equipmentService.getEquipmentOrThrow(equipmentBreakdownRequest.getEquipmentId());
        WorkCenter newWorkCenter = workCenterService.getWorkCenterOrThrow(equipmentBreakdownRequest.getWorkCenterId());

        EquipmentBreakdown newEquipmentBreakdown = mapper.toEntity(equipmentBreakdownRequest, EquipmentBreakdown.class);

        findEquipmentBreakdown.update(newEquipmentBreakdown, newEquipment, newWorkCenter);
        equipmentBreakdownRepo.save(findEquipmentBreakdown);
        return getEquipmentBreakdown(findEquipmentBreakdown.getId());
    }

    // 설비고장 삭제
    @Override
    public void deleteEquipmentBreakdown(Long equipmentBreakdownId) throws NotFoundException {
        EquipmentBreakdown equipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);
        equipmentBreakdown.delete();
        equipmentBreakdownRepo.save(equipmentBreakdown);
    }

    // 설비고장 파일 생성
    // 수리전 파일명: equipment-breakdown/설비고장 id/before/파일명(현재날짜)
    // 수리후 파일명: equipment-breakdown/설비고장 id/after/파일명(현재날짜)
    @Override
    public EquipmentBreakdownResponse createFilesToEquipmentBreakdown(Long equipmentBreakdownId, boolean fileDivision, List<MultipartFile> files) throws NotFoundException, BadRequestException, IOException {
        EquipmentBreakdown equipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);
        String beforeFileName = "equipment-breakdown/" + equipmentBreakdownId + "/before/";
        String afterFileName = "equipment-breakdown/" + equipmentBreakdownId + "/after/";
        List<EquipmentBreakdownFile> fileList = new ArrayList<>();


        // fileDivision false 면 수리전, true 면 수리 후
        if (fileDivision) {
            for (MultipartFile afterFile : files) {
                EquipmentBreakdownFile file = new EquipmentBreakdownFile();
                String afterFileUploadName = s3Uploader.upload(afterFile, afterFileName);
                file.putAfterFile(equipmentBreakdown, afterFileUploadName);
                fileList.add(file);
            }
        } else {
            for (MultipartFile beforeFile : files) {
                EquipmentBreakdownFile file = new EquipmentBreakdownFile();
                String beforeFileUploadName = s3Uploader.upload(beforeFile, beforeFileName);
                file.putBeforeFile(equipmentBreakdown, beforeFileUploadName);
                fileList.add(file);
            }
        }
        equipmentBreakdownFileRepo.saveAll(fileList);
        return getEquipmentBreakdown(equipmentBreakdownId);
    }

    // 설비고장 파일 삭제
    @Override
    public void deleteFileToEquipmentBreakdown(Long equipmentBreakdownId, Long fileId) throws NotFoundException {
        EquipmentBreakdown equipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);
        EquipmentBreakdownFile breakdownFile = equipmentBreakdownFileRepo.findByIdAndEquipmentBreakdownAndDeleteYnFalse(fileId, equipmentBreakdown)
                .orElseThrow(() -> new NotFoundException("file does not exist. input id:" + fileId));
        breakdownFile.delete();
        equipmentBreakdownFileRepo.save(breakdownFile);
    }

    // 설비고장 단일 조회 및 예외
    private EquipmentBreakdown getEquipmentBreakdownOrThrow(Long id) throws NotFoundException {
        return equipmentBreakdownRepo.findByIdAndDeleteYnFalse(id)
                .orElseThrow(() -> new NotFoundException("equipmentBreakdown does not exist. input id: " + id));
    }

    // 설비고장 파일 리스트 조회
    @Override
    public List<EquipmentBreakdownFileResponse> getFilesToEquipmentBreakdown(Long equipmentBreakdownId, boolean fileDivision) throws NotFoundException {
        EquipmentBreakdown equipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);
        if (fileDivision) return equipmentBreakdownRepo.findAfterFileResponsesByEquipmentBreakdownId(equipmentBreakdown.getId());   // 수리 후 파일
        else return equipmentBreakdownRepo.findBeforeFileResponsesByEquipmentBreakdownId(equipmentBreakdown.getId());   // 수리 전 파일
    }

    // ============================================== 수리항목 ==============================================
    // 수리항목 생성
    @Override
    public RepairItemResponse createRepairItem(Long equipmentBreakdownId, Long repairCodeId) throws NotFoundException {
        RepairItem repairItem = new RepairItem();
        EquipmentBreakdown equipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);
        RepairCode repairCode = repairCodeService.getRepairCodeOrThrow(repairCodeId);
        repairItem.add(equipmentBreakdown, repairCode);
        repairItemRepo.save(repairItem);
        return mapper.toResponse(repairItem, RepairItemResponse.class);
    }

    // 수리항목 전체 조회
    @Override
    public List<RepairItemResponse> getRepairItemResponses(Long equipmentBreakdownId) throws NotFoundException {
        EquipmentBreakdown equipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);
        List<RepairItem> repairItems = repairItemRepo.findAllByEquipmentBreakdownAndDeleteYnFalseOrderByCreatedDate(equipmentBreakdown);
        return mapper.toListResponses(repairItems, RepairItemResponse.class);
    }

    // 수리항목 단일 조회
    @Override
    public RepairItemResponse getRepairItemResponse(Long equipmentBreakdownId, Long repairItemId) throws NotFoundException {
        RepairItem repairItem = getRepairItemOrThrow(equipmentBreakdownId, repairItemId);
        return mapper.toResponse(repairItem, RepairItemResponse.class);
    }

    // 수리항목 수정
    @Override
    public RepairItemResponse updateRepairItem(Long equipmentBreakdownId, Long repairItemId, Long repairCodeId) throws NotFoundException {
        RepairItem findRepairItem = getRepairItemOrThrow(equipmentBreakdownId, repairItemId);
        RepairCode repairCode = repairCodeService.getRepairCodeOrThrow(repairCodeId);
        findRepairItem.update(repairCode);
        repairItemRepo.save(findRepairItem);
        return mapper.toResponse(findRepairItem, RepairItemResponse.class);
    }

    // 수리항목 삭제
    @Override
    public void deleteRepairItem(Long equipmentBreakdownId, Long repairItemId) throws NotFoundException {
        RepairItem repairItem = getRepairItemOrThrow(equipmentBreakdownId, repairItemId);
        repairItem.delete();
        repairItemRepo.save(repairItem);
    }

    // 수리항목 단일 조회 및 예외
    private RepairItem getRepairItemOrThrow(Long equipmentBreakdownId, Long repairItemId) throws NotFoundException {
        EquipmentBreakdown equipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);
        return repairItemRepo.findByIdAndEquipmentBreakdownAndDeleteYnFalse(repairItemId, equipmentBreakdown)
                .orElseThrow(() -> new NotFoundException("repairItem does not exist. " +
                        "input equipmentBreakdownId: " + equipmentBreakdownId + ", " +
                        "input repairItemId: " + repairItemId)
                );
    }

    // ============================================== 수리부품정보 ==============================================
    // 수리부품 생성
    @Override
    public RepairPartResponse createRepairPart(Long equipmentBreakdownId, Long repairItemId, RepairPartRequest repairPartRequest) throws NotFoundException {
        RepairItem repairItem = getRepairItemOrThrow(equipmentBreakdownId, repairItemId);
        RepairPart repairPart = mapper.toEntity(repairPartRequest, RepairPart.class);
        repairPart.add(repairItem);
        repairPartRepo.save(repairPart);
        return mapper.toResponse(repairPart, RepairPartResponse.class);
    }

    // 수리부품 리스트 조회
    @Override
    public List<RepairPartResponse> getRepairPartResponses(Long equipmentBreakdownId, Long repairItemId) throws NotFoundException {
        RepairItem repairItem = getRepairItemOrThrow(equipmentBreakdownId, repairItemId);
        List<RepairPart> repairParts = repairPartRepo.findAllByRepairItemAndDeleteYnFalseOrderByCreatedDate(repairItem);
        return mapper.toListResponses(repairParts, RepairPartResponse.class);
    }

    // 수리부품 단일 조회
    @Override
    public RepairPartResponse getRepairPartResponse(Long equipmentBreakdownId, Long repairItemId, Long repairPartId) throws NotFoundException {
        RepairPart repairPart = getRepairPartOrThrow(equipmentBreakdownId, repairItemId, repairPartId);
        return mapper.toResponse(repairPart, RepairPartResponse.class);
    }

    // 수리부품 수정
    @Override
    public RepairPartResponse updateRepairPart(Long equipmentBreakdownId, Long repairItemId, Long repairPartId, RepairPartRequest repairPartRequest) throws NotFoundException {
        RepairPart findRepairPart = getRepairPartOrThrow(equipmentBreakdownId, repairItemId, repairPartId);
        RepairPart newRepairPart = mapper.toEntity(repairPartRequest, RepairPart.class);
        findRepairPart.update(newRepairPart);
        repairPartRepo.save(findRepairPart);
        return mapper.toResponse(findRepairPart, RepairPartResponse.class);
    }

    // 수리부품 삭제
    @Override
    public void deleteRepairPart(Long equipmentBreakdownId, Long repairItemId, Long repairPartId) throws NotFoundException {
        RepairPart repairPart = getRepairPartOrThrow(equipmentBreakdownId, repairItemId, repairPartId);
        repairPart.delete();
        repairPartRepo.save(repairPart);
    }

    // 수리부품 단일 조회 및 예외
    private RepairPart getRepairPartOrThrow(Long equipmentBreakdownId, Long repairItemId, Long repairPartId) throws NotFoundException {
        RepairItem repairItem = getRepairItemOrThrow(equipmentBreakdownId, repairItemId);
        return repairPartRepo.findByIdAndRepairItemAndDeleteYnFalse(repairPartId, repairItem)
                .orElseThrow(() -> new NotFoundException("repairPart does not exist. " +
                        "input equipmentBreakdownId: " + equipmentBreakdownId + ", " +
                        "input repairItemId: " + repairItemId + ", " +
                        "input repairPartId: " + repairPartId)
                );
    }

    // ============================================== 수리작업자 정보 ==============================================
    // 수리작업자 생성
    @Override
    public RepairWorkerResponse createRepairWorker(Long equipmentBreakdownId, Long userId) throws NotFoundException {
        RepairWorker repairWorker = new RepairWorker();
        EquipmentBreakdown equipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);
        User user = userService.getUserOrThrow(userId);
        repairWorker.add(equipmentBreakdown, user);
        repairWorkerRepo.save(repairWorker);
        return mapper.toResponse(repairWorker, RepairWorkerResponse.class);
    }

    // 수리작업자 전체 조회
    @Override
    public List<RepairWorkerResponse> getRepairWorkerResponses(Long equipmentBreakdownId) throws NotFoundException {
        EquipmentBreakdown equipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);
        List<RepairWorker> repairWorkers = repairWorkerRepo.findAllByEquipmentBreakdownAndDeleteYnFalseOrderByCreatedDate(equipmentBreakdown);
        return mapper.toListResponses(repairWorkers, RepairWorkerResponse.class);
    }

    // 수리작업자 단일 조회
    @Override
    public RepairWorkerResponse getRepairWorkerResponse(Long equipmentBreakdownId, Long repairWorkerId) throws NotFoundException {
        RepairWorker repairWorker = getRepairWorkerOrThrow(equipmentBreakdownId, repairWorkerId);
        return mapper.toResponse(repairWorker, RepairWorkerResponse.class);
    }

    // 수리작업자 수정
    @Override
    public RepairWorkerResponse updateRepairWorker(Long equipmentBreakdownId, Long repairWorkerId, Long userId) throws NotFoundException {
        RepairWorker findRepairWorker = getRepairWorkerOrThrow(equipmentBreakdownId, repairWorkerId);
        User newUser = userService.getUserOrThrow(userId);
        findRepairWorker.update(newUser);
        repairWorkerRepo.save(findRepairWorker);
        return mapper.toResponse(findRepairWorker, RepairWorkerResponse.class);
    }

    // 수리작업자 삭제
    @Override
    public void deleteRepairWorker(Long equipmentBreakdownId, Long repairWorkerId) throws NotFoundException {
        RepairWorker findRepairWorker = getRepairWorkerOrThrow(equipmentBreakdownId, repairWorkerId);
        findRepairWorker.delete();
        repairWorkerRepo.save(findRepairWorker);
    }

    // 수리작업자 단일 조회 및 예외
    private RepairWorker getRepairWorkerOrThrow(Long equipmentBreakdownId, Long repairWorkerId) throws NotFoundException {
        EquipmentBreakdown equipmentBreakdown = getEquipmentBreakdownOrThrow(equipmentBreakdownId);
        return repairWorkerRepo.findByIdAndEquipmentBreakdownAndDeleteYnFalse(repairWorkerId, equipmentBreakdown)
                .orElseThrow(() -> new NotFoundException("repairWorker does not exist. " +
                        "input equipmentBreakdownId: " + equipmentBreakdownId + ", " +
                        "input repairWorkerId: " + repairWorkerId)
                );
    }

    // ============================================== 17-3. 설비 수리내역 조회 ==============================================
    // 설비 수리내역 리스트 조회, 검색조건: 작업장 id, 설비유형, 수리항목, 작업기간 fromDate~toDate
    @Override
    public List<EquipmentRepairHistoryResponse> getEquipmentRepairHistories(
            Long workCenterId,
            Long workLineId,
            Long repairCodeId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return equipmentBreakdownRepo.findEquipmentRepairHistoryResponseByCondition(workCenterId, workLineId, repairCodeId, fromDate, toDate);
    }
    // ============================================== 17-4. 설비 수리부품 내역 조회 ==============================================
    // 설비 수리부품 내역 조회, 검색조건: 작업장 id, 설비유형(작업라인 id), 수리항목(수리코드 id), 작업기간 fromDate~toDate
    @Override
    public List<EquipmentRepairPartResponse> getEquipmentRepairParts(
            Long workCenterId,
            Long workLineId,
            Long repairCodeId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return equipmentBreakdownRepo.findEquipmentRepairPartResopnsesByCondition(workCenterId, workLineId, repairCodeId, fromDate, toDate);
    }
}
