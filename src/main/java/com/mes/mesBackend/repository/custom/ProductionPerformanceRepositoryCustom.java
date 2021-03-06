package com.mes.mesBackend.repository.custom;

import com.mes.mesBackend.dto.response.ProductionPerformanceResponse;
import com.mes.mesBackend.entity.ProductionPerformance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// 8-6. 생산실적 관리
public interface ProductionPerformanceRepositoryCustom {
    // 생산실적 리스트 조회, 검색조건: 조회기간 fromDate~toDate, 품목그룹 id, 품명|품번
    List<ProductionPerformanceResponse> findProductionPerformanceResponsesByCondition(LocalDate fromDate, LocalDate toDate, Long itemGroupId, String itemNoOrItemName);
    Optional<ProductionPerformance> findByProduceOrderId(Long produceOrderId);
}
