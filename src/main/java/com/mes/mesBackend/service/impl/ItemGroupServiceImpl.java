package com.mes.mesBackend.service.impl;

import com.mes.mesBackend.dto.request.CodeRequest;
import com.mes.mesBackend.dto.request.ItemGroupRequest;
import com.mes.mesBackend.dto.response.CodeResponse;
import com.mes.mesBackend.dto.response.ItemGroupResponse;
import com.mes.mesBackend.entity.ItemGroup;
import com.mes.mesBackend.entity.ItemGroupCode;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.mapper.ModelMapper;
import com.mes.mesBackend.repository.ItemGroupCodeRepository;
import com.mes.mesBackend.repository.ItemGroupRepository;
import com.mes.mesBackend.service.ItemGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

// 품목그룹
@Service
public class ItemGroupServiceImpl implements ItemGroupService {
    @Autowired
    ItemGroupCodeRepository itemGroupCodeRepository;
    @Autowired
    ItemGroupRepository itemGroupRepository;
    @Autowired
    ModelMapper mapper;

    // 품목그룹 생성
    @Override
    public ItemGroupResponse createItemGroup(ItemGroupRequest itemGroupRequest) throws NotFoundException {
        ItemGroupCode groupCode = getItemGroupCodeOrThrow(itemGroupRequest.getItemGroupCode());
        ItemGroup itemGroup = mapper.toEntity(itemGroupRequest, ItemGroup.class);
        itemGroup.add(groupCode);
        itemGroupRepository.save(itemGroup);
        return mapper.toResponse(itemGroup, ItemGroupResponse.class);
    }

    // 품목그룹 단일 조회
    @Override
    public ItemGroupResponse getItemGroup(Long id) throws NotFoundException {
        ItemGroup itemGroup = getItemGroupOrThrow(id);
        return mapper.toResponse(itemGroup, ItemGroupResponse.class);
    }

    // 품목그룹 페이징 조회
    @Override
    public Page<ItemGroupResponse> getItemGroups(Pageable pageable) {
        Page<ItemGroup> itemGroups = itemGroupRepository.findAllByDeleteYnFalse(pageable);
        return mapper.toPageResponses(itemGroups, ItemGroupResponse.class);
    }

    // 품목그룹 수정
    @Override
    public ItemGroupResponse updateItemGroup(Long id, ItemGroupRequest itemGroupRequest) throws NotFoundException {
        ItemGroup findItemGroup = getItemGroupOrThrow(id);
        ItemGroupCode newGroupCode = getItemGroupCodeOrThrow(itemGroupRequest.getItemGroupCode());
        ItemGroup newItemGroup = mapper.toEntity(itemGroupRequest, ItemGroup.class);
        findItemGroup.put(newItemGroup, newGroupCode);
        itemGroupRepository.save(findItemGroup);
        return mapper.toResponse(findItemGroup, ItemGroupResponse.class);
    }

    // 품목그룹 삭제
    @Override
    public void deleteItemGroup(Long id) throws NotFoundException {
        ItemGroup itemGroup = getItemGroupOrThrow(id);
        itemGroup.delete();
        itemGroupRepository.save(itemGroup);
    }

    // 품목그룹 조회 및 예외
    @Override
    public ItemGroup getItemGroupOrThrow(Long id) throws NotFoundException {
        return itemGroupRepository.findByIdAndDeleteYnFalse(id)
                .orElseThrow(() -> new NotFoundException("itemGroup does not exist. input id: " + id));
    }



    // 그룹코드 생성
    @Override
    public CodeResponse createItemGroupCode(CodeRequest codeRequest) {
        ItemGroupCode itemGroupCode = mapper.toEntity(codeRequest, ItemGroupCode.class);
        itemGroupCodeRepository.save(itemGroupCode);
        return mapper.toResponse(itemGroupCode, CodeResponse.class);
    }

    // 그룹코드 단일 조회
    @Override
    public CodeResponse getItemGroupCode(Long id) throws NotFoundException {
        return mapper.toResponse(getItemGroupCodeOrThrow(id), CodeResponse.class);
    }

    // 그룹코드 리스트 조회
    @Override
    public List<CodeResponse> getItemGroupCodes() {
        return mapper.toListResponses(itemGroupCodeRepository.findAll(), CodeResponse.class);
    }

    // 그룹코드 삭제
    @Override
    public void deleteItemGroupCode(Long id) throws NotFoundException, BadRequestException {
        throwIfItemGroupCodeExist(id);
        itemGroupCodeRepository.deleteById(id);
    }

    // 그룹코드 조회 및 예외
    private ItemGroupCode getItemGroupCodeOrThrow(Long id) throws NotFoundException {
        return itemGroupCodeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("itemGroupCode does not exist. input id: " + id));
    }

    // itemGroupCode 삭제 시 itemGroup에 해당하는 itemGroupCode가 있으면 예외
    private void throwIfItemGroupCodeExist(Long itemGroupCodeId) throws NotFoundException, BadRequestException {
        ItemGroupCode itemGroupCode = getItemGroupCodeOrThrow(itemGroupCodeId);
        boolean existByItemGroupCode = itemGroupRepository.existsByItemGroupCodeAndDeleteYnFalse(itemGroupCode);
        if (existByItemGroupCode)
            throw new BadRequestException("code exist in the itemGroup. input code id: " + itemGroupCodeId);
    }
}
