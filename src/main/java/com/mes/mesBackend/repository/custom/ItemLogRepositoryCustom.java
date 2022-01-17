package com.mes.mesBackend.repository.custom;

import com.mes.mesBackend.entity.ItemLog;

import java.time.LocalDate;
import java.util.List;

public interface ItemLogRepositoryCustom {
    //일자별 품목 변동 사항 전체 조회 / 검색조건 : 수불일자, 창고, 생성기간
    List<ItemLog> findAllCondition(Long warehouseId, LocalDate startDate, LocalDate endDate, boolean isOut);

    //일자별 품목 변동 사항 단일 조회
    ItemLog findByItemIdAndwareHouseIdAndOutsourcingYn(Long itemId, Long warehouseId, boolean isOut);
}