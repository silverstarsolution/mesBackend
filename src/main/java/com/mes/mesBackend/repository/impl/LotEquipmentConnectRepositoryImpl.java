package com.mes.mesBackend.repository.impl;

import com.mes.mesBackend.dto.response.PopWorkOrderStates;
import com.mes.mesBackend.dto.response.WorkOrderDetailResponse;
import com.mes.mesBackend.entity.*;
import com.mes.mesBackend.repository.custom.LotEquipmentConnectRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.mes.mesBackend.entity.enumeration.LotConnectDivision.FAMILY;
import static com.mes.mesBackend.entity.enumeration.LotMasterDivision.EQUIPMENT_LOT;
import static com.mes.mesBackend.entity.enumeration.LotMasterDivision.REAL_LOT;

@RequiredArgsConstructor
public class LotEquipmentConnectRepositoryImpl implements LotEquipmentConnectRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    final QLotEquipmentConnect lotEquipmentConnect = QLotEquipmentConnect.lotEquipmentConnect;
    final QLotMaster lotMaster = QLotMaster.lotMaster;
    final QEquipment equipment = QEquipment.equipment;
    final QWorkProcess workProcess = QWorkProcess.workProcess;
    final QLotConnect lotConnect = QLotConnect.lotConnect;
    final QWorkOrderUserLog workOrderUserLog = QWorkOrderUserLog.workOrderUserLog;

    // 설비 lot id 로 조회
    @Override
    public Optional<LotEquipmentConnect> findByChildId(Long childLotId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(lotEquipmentConnect)
                        .leftJoin(lotMaster).on(lotMaster.id.eq(lotEquipmentConnect.childLot.id))
                        .where(lotMaster.id.eq(childLotId))
                        .limit(1)
                        .fetchOne()
        );
    }

    // dummyLot 로 조회
    @Override
    public List<PopWorkOrderStates> findPopWorkOrderStates(Long dummyLotId) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                PopWorkOrderStates.class,
                                lotEquipmentConnect.childLot.id.as("lotMasterId"),  // 설비로트
                                equipment.equipmentName.as("equipmentName"),
                                lotEquipmentConnect.processStatus.as("processStatus"),
                                lotEquipmentConnect.modifiedDate.as("updateDateTime"),
                                lotEquipmentConnect.childLot.createdAmount.as("createdAmount"),
                                lotEquipmentConnect.childLot.inputEquipment.id.as("fillingEquipmentCode"),
                                lotEquipmentConnect.childLot.stockAmount.as("stockAmount")
                        )
                )
                .from(lotEquipmentConnect)
                .leftJoin(lotMaster).on(lotMaster.id.eq(lotEquipmentConnect.childLot.id))
                .leftJoin(equipment).on(equipment.id.eq(lotMaster.equipment.id))
                .where(
                        lotEquipmentConnect.parentLot.id.eq(dummyLotId)
                )
                .fetch();
    }

    // 해당 작업지시로 생성된 realLot 모두 조회
    @Override
    public List<LotMaster> findChildLotByChildLotOfParentLotCreatedDateDesc(Long dummyLotId) {
        return jpaQueryFactory
                .select(lotConnect.childLot)
                .from(lotConnect)
                .leftJoin(lotEquipmentConnect).on(lotEquipmentConnect.id.eq(lotConnect.parentLot.id))
                .where(
                        lotEquipmentConnect.parentLot.id.eq(dummyLotId),
                        lotConnect.childLot.deleteYn.isFalse(),
                        lotConnect.childLot.lotMasterDivision.eq(REAL_LOT),
                        lotConnect.division.eq(FAMILY)
                )
                .orderBy(lotConnect.childLot.createdDate.desc())
                .fetch();
    }

    // dummyLot 로 조회
    @Override
    public List<WorkOrderDetailResponse> findWorkOrderDetailResponseByDummyLotId(Long dummyLotId) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                WorkOrderDetailResponse.class,
                                lotMaster.id.as("equipmentLotId"),
                                workOrderUserLog.user.korName.as("userKorName"),
                                lotMaster.createdDate.as("workDateTime"),
                                lotMaster.createdAmount.as("productionAmount"),
                                lotMaster.createdAmount.subtract(lotMaster.badItemAmount).as("stockAmount"),
                                lotMaster.badItemAmount.as("badItemAmount"),
                                lotMaster.workProcess.workProcessName.as("workProcess"),
                                lotMaster.workProcess.id.as("workProcessId"),
                                lotMaster.recycleAmount.as("recycleAmount")
                        )
                )
                .from(lotEquipmentConnect)
                .leftJoin(workOrderUserLog).on(workOrderUserLog.equipmentLotMaster.id.eq(lotEquipmentConnect.childLot.id))
                .leftJoin(lotMaster).on(lotMaster.id.eq(lotEquipmentConnect.childLot.id))
                .leftJoin(workProcess).on(workProcess.id.eq(lotMaster.workProcess.id))
                .where(
                        lotEquipmentConnect.parentLot.id.eq(dummyLotId),
                        lotMaster.lotMasterDivision.eq(EQUIPMENT_LOT),
                        lotMaster.deleteYn.isFalse()
                )
                .fetch();
    }

    @Override
    public Optional<LotMaster> findEquipmentLotByIdAndStockAmount(Long equipmentId, int stockAmount) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(lotMaster)
                        .from(lotMaster)
                        .where(
                                lotMaster.id.eq(equipmentId),
                                lotMaster.deleteYn.isFalse(),
                                lotMaster.stockAmount.ne(stockAmount),
                                lotMaster.lotMasterDivision.eq(EQUIPMENT_LOT)
                        )
                        .fetchOne()
        );
    }

    // 설비 lot 로 분할 된 lot 모두 조회
    @Override
    public List<LotMaster> findRealLotByEquipmentLot(Long equipmentLotId) {
        return jpaQueryFactory
                .select(lotConnect.childLot)
                .from(lotConnect)
                .where(
                        lotConnect.parentLot.childLot.id.eq(equipmentLotId),
                        lotConnect.parentLot.childLot.lotMasterDivision.eq(EQUIPMENT_LOT),
                        lotConnect.division.eq(FAMILY),
                        lotConnect.childLot.deleteYn.isFalse(),
                        lotConnect.childLot.lotMasterDivision.eq(REAL_LOT)
                )
                .fetch();
    }
}
