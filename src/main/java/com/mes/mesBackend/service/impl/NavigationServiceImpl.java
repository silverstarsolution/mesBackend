package com.mes.mesBackend.service.impl;

import com.mes.mesBackend.dto.request.DetailNavRequest;
import com.mes.mesBackend.dto.request.MainNavRequest;
import com.mes.mesBackend.dto.request.SubNavRequest;
import com.mes.mesBackend.dto.response.DetailNavResponse;
import com.mes.mesBackend.dto.response.MainNavResponse;
import com.mes.mesBackend.dto.response.SubNavResponse;
import com.mes.mesBackend.entity.DetailNavigation;
import com.mes.mesBackend.entity.MainNavigation;
import com.mes.mesBackend.entity.SubNavigation;
import com.mes.mesBackend.mapper.ModelMapper;
import com.mes.mesBackend.repository.DetailNavigationRepository;
import com.mes.mesBackend.repository.MainNavigationRepository;
import com.mes.mesBackend.repository.SubNavigationRepository;
import com.mes.mesBackend.service.NavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NavigationServiceImpl implements NavigationService {

    @Autowired MainNavigationRepository mainNavigationRepository;
    @Autowired SubNavigationRepository subNavigationRepository;
    @Autowired DetailNavigationRepository detailNavigationRepository;
    @Autowired
    ModelMapper modelMapper;

    // 메인네비게이션바 조회
    public List<MainNavResponse> getMainNavigations() {
        List<MainNavigation> mainNavigations = mainNavigationRepository.findAllByUseYnTrueOrderByOrdersAsc();
        return modelMapper.toListResponses(mainNavigations, MainNavResponse.class);
    }

    // 메인네비게이션바 생성
    public MainNavResponse createMainNavigation(MainNavRequest mainNavRequest) {
        MainNavigation mainNavigation = modelMapper.toEntity(mainNavRequest, MainNavigation.class);
        MainNavigation saveMainNavigation = mainNavigationRepository.save(mainNavigation);
        return modelMapper.toResponse(saveMainNavigation, MainNavResponse.class);
    }

    // 메인네비게이션바 수정
    public MainNavResponse updateMainNavigation(Long id, MainNavRequest mainNavRequest) {
        MainNavigation findMainNavigation = mainNavigationRepository.findByIdAndUseYnTrue(id);
        // update Navigation
        MainNavigation newMainNavigation = modelMapper.toEntity(mainNavRequest, MainNavigation.class);
        findMainNavigation.put(newMainNavigation);
        MainNavigation saveMainNavigation = mainNavigationRepository.save(findMainNavigation);
        return modelMapper.toResponse(saveMainNavigation, MainNavResponse.class);
    }

    // 메인네비게이션바 삭제
    public void deleteMainNavigation(Long id) {
        MainNavigation findMainNavigation = mainNavigationRepository.findByIdAndUseYnTrue(id);
        findMainNavigation.setUseYn(false);
    }

    // 서브네비게이션바 전체 조회
    public List<SubNavResponse> getSubNavigations(Long mainNavId) {
        MainNavigation mainNavigation = mainNavigationRepository.findByIdAndUseYnTrue(mainNavId);
        List<SubNavigation> findSubNavigations = subNavigationRepository.findAllByMainNavigationAndUseYnTrueOrderByOrdersAsc(mainNavigation);
        return modelMapper.toListResponses(findSubNavigations, SubNavResponse.class);
    }

    // 서브네이게이션바 생성
    public SubNavResponse createSubNavigation(Long mainNavId, SubNavRequest subNavRequest) {
        SubNavigation subNavigation = modelMapper.toEntity(subNavRequest, SubNavigation.class);
        MainNavigation mainNavigation = mainNavigationRepository.findByIdAndUseYnTrue(mainNavId);
        subNavigation.setMainNavigation(mainNavigation);
        SubNavigation saveSubNavigation = subNavigationRepository.save(subNavigation);
        return modelMapper.toResponse(saveSubNavigation, SubNavResponse.class);
    }

    // 서브네이게이션바 수정
    public SubNavResponse updateSubNavigation(Long id, SubNavRequest subNavRequest) {
        SubNavigation findSubNavigation = subNavigationRepository.findByIdAndUseYnTrue(id);
        SubNavigation subNavigation = modelMapper.toEntity(subNavRequest, SubNavigation.class);
        findSubNavigation.put(subNavigation);
        SubNavigation saveSubNavigation = subNavigationRepository.save(findSubNavigation);
        return modelMapper.toResponse(saveSubNavigation, SubNavResponse.class);
    }

    // 서브네이게이션 삭제
    public void deleteSubNavigation(Long id) {
        SubNavigation findSubNavigation = subNavigationRepository.findByIdAndUseYnTrue(id);
        findSubNavigation.setUseYn(false);
        subNavigationRepository.save(findSubNavigation);
    }

    // 디테일네비게이션 조회
    public List<DetailNavResponse> getDetailNavigations(Long subNavId) {
        SubNavigation subNavigation = subNavigationRepository.findByIdAndUseYnTrue(subNavId);
        List<DetailNavigation> detailNavigations = detailNavigationRepository.findAllBySubNavigationAndUseYnTrueOrderByOrdersAsc(subNavigation);
        return modelMapper.toListResponses(detailNavigations, DetailNavResponse.class);
    }

    // 디테일네비게이션 생성
    public DetailNavResponse createDetailNavigation(Long subNavId, DetailNavRequest detailNavRequest) {
        // subId가 값이 있으면 subNavgation에 값을 저장하고,

        DetailNavigation detailNavigation = modelMapper.toEntity(detailNavRequest, DetailNavigation.class);
        detailNavigationRepository.save(detailNavigation);
        if (subNavId != null) {
            SubNavigation subNavigation = subNavigationRepository.findByIdAndUseYnTrue(subNavId);
            detailNavigation.setSubNavigation(subNavigation);
            detailNavigationRepository.save(detailNavigation);
        }
        return modelMapper.toResponse(detailNavigation, DetailNavResponse.class);
    }

    // 디테일네비게이션 수정
    public DetailNavResponse updateDetailNavigation(Long detailNavId, DetailNavRequest detailNavRequest) {
        DetailNavigation findDetailNavigation = detailNavigationRepository.findByIdAndUseYnTrue(detailNavId);
        DetailNavigation detailavigation = modelMapper.toEntity(detailNavRequest, DetailNavigation.class);
        findDetailNavigation.put(detailavigation);
        DetailNavigation saveDetailNavigation = detailNavigationRepository.save(findDetailNavigation);
        return modelMapper.toResponse(saveDetailNavigation, DetailNavResponse.class);
    }

    // 디테일네비게이션 삭제
    public void deleteDetailNavigation(Long detailNavId) {
        DetailNavigation detailNavigation = detailNavigationRepository.findByIdAndUseYnTrue(detailNavId);
        detailNavigation.setUseYn(false);
        detailNavigationRepository.save(detailNavigation);
    }
}
