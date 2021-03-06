package com.mes.mesBackend.service.impl;

import com.mes.mesBackend.dto.request.BomItemRequest;
import com.mes.mesBackend.dto.request.BomMasterRequest;
import com.mes.mesBackend.dto.response.BomItemDetailResponse;
import com.mes.mesBackend.dto.response.BomItemResponse;
import com.mes.mesBackend.dto.response.BomMasterResponse;
import com.mes.mesBackend.entity.*;
import com.mes.mesBackend.entity.enumeration.GoodsType;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.mapper.ModelMapper;
import com.mes.mesBackend.repository.BomItemDetailRepository;
import com.mes.mesBackend.repository.BomMasterRepository;
import com.mes.mesBackend.repository.OutSourcingProductionRequestRepository;
import com.mes.mesBackend.repository.OutsourcingInputRepository;
import com.mes.mesBackend.service.BomMasterService;
import com.mes.mesBackend.service.ClientService;
import com.mes.mesBackend.service.ItemService;
import com.mes.mesBackend.service.WorkProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.mes.mesBackend.entity.enumeration.GoodsType.RAW_MATERIAL;
import static com.mes.mesBackend.entity.enumeration.GoodsType.SUB_MATERIAL;
import static com.mes.mesBackend.helper.Constants.DECIMAL_POINT_2;

@Service
@RequiredArgsConstructor
public class BomMasterServiceImpl implements BomMasterService {
    private final BomMasterRepository bomMasterRepository;
    private final ModelMapper mapper;
    private final ItemService itemService;
    private final ClientService clientService;
    private final WorkProcessService workProcessService;
    private final BomItemDetailRepository bomItemDetailRepository;
    private final OutSourcingProductionRequestRepository outSourcingProductionRequestRepository;

    // BOM 마스터 생성
    @Override
    public BomMasterResponse createBomMaster(BomMasterRequest bomMasterRequest) throws NotFoundException, BadRequestException {
        Item item = itemService.getItemOrThrow(bomMasterRequest.getItem());

        // 입력받은 item 이 bomMaster 에 이미 등록되어 있는지 채크
        throwIfNotDuplicateItemInBomMasters(item);
        // 입력받은 item 의 품목계정이 원부자재면 등록 불가능
        throwIfGoodsTypeNe(item.getItemAccount().getGoodsType());

        BomMaster bomMaster = mapper.toEntity(bomMasterRequest, BomMaster.class);
        WorkProcess workProcess = workProcessService.getWorkProcessOrThrow(bomMasterRequest.getWorkProcessId());

        bomMaster.addJoin(item, workProcess);

        BomMaster save = bomMasterRepository.save(bomMaster);
        return getBomMaster(save.getId());
    }

    // BOM 마스처 단일 조회
    @Override
    public BomMasterResponse getBomMaster(Long bomMasterId) throws NotFoundException {
        BomMaster bomMaster = getBomMasterOrThrow(bomMasterId);
        BomMasterResponse response = new BomMasterResponse();
        return response.setResponse(bomMaster);
    }

    // BOM 마스처 단일 조회 및 에외
    private BomMaster getBomMasterOrThrow(Long bomMasterId) throws NotFoundException {
        return bomMasterRepository.findByIdAndDeleteYnFalse(bomMasterId)
                .orElseThrow(() -> new NotFoundException("bom master does exist. input id: " + bomMasterId));
    }

    // BOM 마스터 조회 검색조건: 품목계정, 품목그룹, 품번|품명
    @Override
    public List<BomMasterResponse> getBomMasters(
            Long itemAccountId,
            Long itemGroupId,
            String itemNoAndItemName
    ) {
        List<BomMaster> bomMasters = bomMasterRepository.findAllByCondition(itemAccountId, itemGroupId, itemNoAndItemName);
        List<BomMasterResponse> responses = new ArrayList<>();
        for (BomMaster bomMaster : bomMasters) {
            BomMasterResponse response = new BomMasterResponse();
            response.setResponse(bomMaster);
            responses.add(response);
        }
        return responses;
    }
    // BOM 마스터 페이징 조회 검색조건: 품목계정, 품목그룹, 품번|품명
//    @Override
//    public Page<BomMasterResponse> getBomMasters(
//            Long itemAccountId,
//            Long itemGroupId,
//            String itemNoAndItemName,
//            Pageable pageable
//    ) {
//        Page<BomMaster> bomMasters = bomMasterRepository.findAllByCondition(itemAccountId, itemGroupId, itemNoAndItemName, pageable);
//        return mapper.toPageResponses(bomMasters, BomMasterResponse.class);
//    }

    // BOM 마스터 수정
    @Override
    public BomMasterResponse updateBomMaster(Long bomMasterId, BomMasterRequest bomMasterRequest) throws NotFoundException, BadRequestException {
        BomMaster findBomMaster = getBomMasterOrThrow(bomMasterId);
        Item newItem = itemService.getItemOrThrow(bomMasterRequest.getItem());
        WorkProcess newWorkProcess = workProcessService.getWorkProcessOrThrow(bomMasterRequest.getWorkProcessId());

        // Bom 에 대한 상세 정보가 존재하면 품목정보는 수정 불가능
        if (!findBomMaster.getItem().getId().equals(newItem.getId())) throwIfBomMasterDetailExistIsNotItemUpdate(findBomMaster);
        // Bom 에 대한 상세 정보가 존재하면 작업공정 수정 불가능
        if (!findBomMaster.getWorkProcess().getId().equals(newWorkProcess.getId())) throwIfBomMasterDetailExistIsNotItemUpdateWorkProcess(findBomMaster);

        BomMaster newBomMaster = mapper.toEntity(bomMasterRequest, BomMaster.class);
        findBomMaster.update(newBomMaster, newItem, newWorkProcess);

        bomMasterRepository.save(findBomMaster);
        return getBomMaster(findBomMaster.getId());
    }

    // BOM 마스터 삭제
    @Override
    public void deleteBomMaster(Long bomMasterId) throws NotFoundException, BadRequestException {
        BomMaster bomMaster = getBomMasterOrThrow(bomMasterId);
        // bomMaster 에 해당되는 BomItemDetail 이 있는지 체크(존재하면 삭제 불가능)
        throwIfBomMasterExistInBomItemDetail(bomMaster);
        // 해당 품목이 외주입고에 등록되어 있으면 삭제 불가능
        throwIfOutsourcingProductionRequestItemEqNotDelete(bomMaster.getItem().getId());
        bomMaster.delete();
        bomMasterRepository.save(bomMaster);
    }

    // 해당 품목이 외주입고에 등록되어 있으면 삭제 불가능
    private void throwIfOutsourcingProductionRequestItemEqNotDelete(Long itemId) throws BadRequestException {
        boolean b = outSourcingProductionRequestRepository.existsItemByOutsourcingRequest(itemId);
        if (b) throw new BadRequestException("해당 BOM 은 외주입고에 등록되어 있으므로 삭제가 불가능 합니다. ");
    }

    // BOM 품목 생성
    @Override
    public BomItemResponse createBomItem(Long bomMasterId, BomItemRequest bomItemRequest) throws NotFoundException, BadRequestException {
        BomMaster bomMaster = getBomMasterOrThrow(bomMasterId);

        // 같은 품목 2개 등록 불가
        throwIfBomItemDetailItemEq(bomMasterId, bomItemRequest.getItem());
        Item item = itemService.getItemOrThrow(bomItemRequest.getItem());
        WorkProcess workProcess = bomItemRequest.getWorkProcess() != null ?
                workProcessService.getWorkProcessOrThrow(bomItemRequest.getWorkProcess()) : null;
        BomItemDetail bomItemDetail = mapper.toEntity(bomItemRequest, BomItemDetail.class);

        bomItemDetail.addJoin(bomMaster, item, workProcess);

        bomItemDetailRepository.save(bomItemDetail);
        return mapper.toResponse(bomItemDetail, BomItemResponse.class);
    }

    // 같은 품목 2개 등록 불가
    private void throwIfBomItemDetailItemEq(Long bomMasterId, Long itemId) throws BadRequestException {
        boolean b = bomItemDetailRepository.existsByBomItemDetailItem(bomMasterId, itemId);
        if (b) throw new BadRequestException("해당 품목은 이미 등록되어 있으므로 중복 등록이 불가능 합니다. 확인 후 다시 시도해주세요.");
    }

    // BOM 품목 리스트 조회
    @Override
    public List<BomItemDetailResponse> getBomItems(Long bomMasterId, String itemNoOrItemName) throws NotFoundException {
        BomMaster bomMaster = getBomMasterOrThrow(bomMasterId);
        List<BomItemDetailResponse> responses = bomItemDetailRepository.findAllByCondition(bomMaster.getId(), itemNoOrItemName);
        responses.forEach(m -> m.setPrice(String.format(DECIMAL_POINT_2, m.getAmount() * m.getItemInputUnitPrice())));
        return responses;
    }

    // BOM 품목 수정
    @Override
    public BomItemResponse updateBomItem(Long bomMasterId, Long bomItemId, BomItemRequest bomItemRequest) throws NotFoundException {
        BomItemDetail findBomItemDetail = getBomItemDetailOrThrow(bomMasterId, bomItemId);

//        Client newToBuy = bomItemRequest.getToBuy() != null ? clientService.getClientOrThrow(bomItemRequest.getToBuy()) : null;
        Item newItem = itemService.getItemOrThrow(bomItemRequest.getItem());
        WorkProcess newWorkProcess = bomItemRequest.getWorkProcess() != null ? workProcessService.getWorkProcessOrThrow(bomItemRequest.getWorkProcess()) : null;

        BomItemDetail newBomItemDetail = mapper.toEntity(bomItemRequest, BomItemDetail.class);

        findBomItemDetail.update(newItem, newWorkProcess, newBomItemDetail);
        bomItemDetailRepository.save(findBomItemDetail);
        return mapper.toResponse(findBomItemDetail, BomItemResponse.class);
    }

    // BOM 품목 삭제
    @Override
    public void deleteBomItem(Long bomMasterId, Long bomItemId) throws NotFoundException {
        BomItemDetail bomItemDetail = getBomItemDetailOrThrow(bomMasterId, bomItemId);
        bomItemDetail.delete();
        bomItemDetailRepository.save(bomItemDetail);
    }

    // BOM 품목 단일 조회
    @Override
    public BomItemResponse getBomItem(Long bomMasterId, Long bomItemDetailId) throws NotFoundException {
        BomItemDetail bomItemDetail = getBomItemDetailOrThrow(bomMasterId, bomItemDetailId);
        return mapper.toResponse(bomItemDetail, BomItemResponse.class);
    }

    // BOM 품목 조회 및 예외
    private BomItemDetail getBomItemDetailOrThrow(Long bomMasterId, Long bomItemDetailId) throws NotFoundException {
        BomMaster bomMaster = getBomMasterOrThrow(bomMasterId);
        return bomItemDetailRepository.findByBomMasterAndIdAndDeleteYnFalse(bomMaster, bomItemDetailId)
                .orElseThrow(() -> new NotFoundException("bomItemDetail does not exist. input id:" + bomItemDetailId));
    }

    // 입력받은 item 이 bomMaster 에 이미 등록되어 있는지 채크
    private void throwIfNotDuplicateItemInBomMasters(Item item) throws BadRequestException {
        boolean b = bomMasterRepository.existsByItemInBomMasters(item.getId());
        if (b) throw new BadRequestException("입략한 품목이 이미 Bom 에 등록되어 있으므로 중복 등록이 불가능 합니다.");
    }

    // 입력받은 item 의 품목계정이 원부자재면 등록 불가능
    private void throwIfGoodsTypeNe(GoodsType goodsType) throws BadRequestException {
        if (goodsType.equals(RAW_MATERIAL) || goodsType.equals(SUB_MATERIAL)) {
            throw new BadRequestException("원부자재 품목은 등록할수 없습니다. 반제품, 완제품만 Bom 등록이 가능합니다. 확인 후 다시 시도해주세요.");
        }
    }

    // bomMaster 에 해당되는 BomItemDetail 이 있는지 체크(존재하면 삭제 불가능)
    private void throwIfBomMasterExistInBomItemDetail(BomMaster bomMaster) throws BadRequestException {
        boolean exists = bomItemDetailRepository.existsByBomMasterAndDeleteYnFalse(bomMaster);
        if (exists) throw new BadRequestException("해당 BOM 에 등록되어있는 상세 정보가 존재하므로 삭제가 불가능 합니다. 상세 정보 삭제 후 다시 시도해주세요.");
    }

    // Bom 에 대한 상세 정보가 존재하면 품목정보는 수정 불가능
    private void throwIfBomMasterDetailExistIsNotItemUpdate(BomMaster bomMaster) throws BadRequestException {
        boolean exists = bomItemDetailRepository.existsByBomMasterAndDeleteYnFalse(bomMaster);
        if (exists) throw new BadRequestException("Bom 에 대한 상세정보가 등록되어 있으므로 품목정보 수정이 불가능합니다. 확인 후 다시 시도해주세요.");
    }
    // Bom 에 대한 상세 정보가 존재하면 작업공정 수정 불가능
    private void throwIfBomMasterDetailExistIsNotItemUpdateWorkProcess(BomMaster bomMaster) throws BadRequestException {
        boolean exists = bomItemDetailRepository.existsByBomMasterAndDeleteYnFalse(bomMaster);
        if (exists) throw new BadRequestException("Bom 에 대한 상세정보가 등록되어 있으므로 작업공정 수정이 불가능합니다. 확인 후 다시 시도해주세요.");
    }
}
