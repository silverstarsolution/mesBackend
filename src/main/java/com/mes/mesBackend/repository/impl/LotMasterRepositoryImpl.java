package com.mes.mesBackend.repository.impl;

import com.mes.mesBackend.dto.response.*;
import com.mes.mesBackend.entity.*;
import com.mes.mesBackend.entity.enumeration.EnrollmentType;
import com.mes.mesBackend.entity.enumeration.GoodsType;
import com.mes.mesBackend.entity.enumeration.LotMasterDivision;
import com.mes.mesBackend.entity.enumeration.WorkProcessDivision;
import com.mes.mesBackend.repository.custom.JpaCustomRepository;
import com.mes.mesBackend.repository.custom.LotMasterRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.mes.mesBackend.entity.enumeration.EnrollmentType.*;
import static com.mes.mesBackend.entity.enumeration.GoodsType.PRODUCT;
import static com.mes.mesBackend.entity.enumeration.LotConnectDivision.EXHAUST;
import static com.mes.mesBackend.entity.enumeration.LotConnectDivision.FAMILY;
import static com.mes.mesBackend.entity.enumeration.LotMasterDivision.DUMMY_LOT;
import static com.mes.mesBackend.entity.enumeration.LotMasterDivision.REAL_LOT;
import static com.mes.mesBackend.entity.enumeration.WorkProcessDivision.PACKAGING;

@RequiredArgsConstructor
public class LotMasterRepositoryImpl implements LotMasterRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    final QItemAccountCode itemAccountCode = QItemAccountCode.itemAccountCode;
    final QItemAccount itemAccount = QItemAccount.itemAccount;
    final QItem item = QItem.item;
    final QLotMaster lotMaster = QLotMaster.lotMaster;
    final QItemGroup itemGroup = QItemGroup.itemGroup;
    final QWareHouse wareHouse = QWareHouse.wareHouse;
    final QClient client = QClient.client;
    final QLotType lotType = QLotType.lotType1;
    final QOutSourcingInput outSourcingInput = QOutSourcingInput.outSourcingInput;
    final QWorkProcess workProcess = QWorkProcess.workProcess;
    final QUnit unit = QUnit.unit;
    final QLotLog lotLog = QLotLog.lotLog;
    final QWorkOrderDetail workOrderDetail = QWorkOrderDetail.workOrderDetail;
    final QLotEquipmentConnect lotEquipmentConnect = QLotEquipmentConnect.lotEquipmentConnect;
    final QLotConnect lotConnect = QLotConnect.lotConnect;
    final QEquipment equipment = QEquipment.equipment;

    // id ??? itemAccountCode ??? code ??????
    @Override
    @Transactional(readOnly = true)
    public ItemAccountCode findCodeByItemId(Long itemId) {
        return jpaQueryFactory
                .select(itemAccountCode)
                .from(itemAccountCode)
                .innerJoin(itemAccount).on(itemAccount.id.eq(itemAccountCode.itemAccount.id))
                .innerJoin(item).on(item.itemAccountCode.id.eq(itemAccountCode.id))
                .where(
                        item.id.eq(itemId),
                        item.deleteYn.isFalse()
                )
                .fetchOne();
    }

    // ???????????? ???????????? ????????? ?????????
    // ??????????????? 1~6??? ??????????????? format ??? ?????? And ??????????????? 9???
    // ????????? ????????? ?????? ??? ??? ?????? ??? ?????? ?????????  +1
    @Override
    @Transactional(readOnly = true)
    public Optional<String> findLotNoByLotNoLengthAndLotNoDateAndCode(int length, String date, String code) {
        return Optional.ofNullable(jpaQueryFactory
                .select(lotMaster.lotNo)
                .from(lotMaster)
                .where(
                        isLotNoLengthEq(length),
                        isLotNoDateContain(date),
                        isCodeContain(code),
                        isDeleteYnFalse()
                )
                .orderBy(lotMaster.lotNo.desc())
                .limit(1)
                .fetchOne());
    }

    @Transactional(readOnly = true)
    public Optional<String> findLotNoByGoodsType(GoodsType goodsType, LocalDate startDate, LocalDate endDate){
        return Optional.ofNullable(jpaQueryFactory
                .select(lotMaster.lotNo)
                .from(lotMaster)
                .where(
//                        lotMaster.goodsType.eq(goodsType),
                        lotMaster.createdDate.between(startDate.atStartOfDay(), endDate.atStartOfDay())
                )
                .orderBy(lotMaster.createdDate.desc())
                .limit(1)
                .fetchOne());
    }

    @Transactional(readOnly = true)
    public Optional<String> findLotNoByAccountCodeAndDate(GoodsType goodsType, LocalDate now){
        return Optional.ofNullable(jpaQueryFactory
                .select(lotMaster.lotNo)
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(itemAccount).on(itemAccount.id.eq(item.itemAccount.id))
                .where(
                        lotMaster.createdDate.between(now.atStartOfDay(), LocalDateTime.of(now, LocalTime.MAX).withNano(0)),
                        itemAccount.goodsType.eq(goodsType),
                        lotMaster.lotMasterDivision.eq(REAL_LOT),
                        lotMaster.deleteYn.isFalse()
                )
                .orderBy(lotMaster.createdDate.desc())
                .limit(1)
                .fetchOne());
    }

    // ??????????????? ?????? ?????? ?????? ???????????? ????????? LOT NO
    @Override
    public Optional<String> findLotNoByAccountCodeAndMonth(GoodsType goodsType, LocalDate now) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(lotMaster.lotNo)
                        .from(lotMaster)
                        .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                        .leftJoin(itemAccount).on(itemAccount.id.eq(item.itemAccount.id))
                        .where(
                                lotMaster.createdDate.between(now.atStartOfDay(), LocalDateTime.of(now, LocalTime.MAX).withNano(0)),
                                lotMaster.deleteYn.isFalse(),
                                lotMaster.lotMasterDivision.eq(REAL_LOT),
                                itemAccount.goodsType.eq(goodsType)
                        )
                        .orderBy(lotMaster.createdDate.desc())
                        .limit(1)
                        .fetchOne()
        );
    }

    // LOT ????????? ??????, ????????????: ???????????? id, LOT ??????, ??????|??????, ?????? id, ????????????, ????????????, LOT ??????, ???????????????, ????????????
    @Override
    @Transactional(readOnly = true)
    public List<LotMasterResponse> findLotMastersByCondition(
            Long itemGroupId,
            String lotNo,
            String itemNoAndItemName,
            Long wareHouseId,
            EnrollmentType enrollmentType,
            Boolean stockYn,
            Long lotTypeId,
            Boolean testingYn,
            WorkProcessDivision workProcessDivision
    ) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                LotMasterResponse.class,
                                lotMaster.id.as("id"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName"),
                                wareHouse.wareHouseName.as("warehouse"),
                                lotMaster.lotNo.as("lotNo"),
                                lotMaster.serialNo.as("serialNo"),
                                lotType.lotType.as("lotType"),
                                lotMaster.enrollmentType.as("enrollmentType"),
//                                lotMaster.process.as("process"),
                                lotMaster.processYn.as("processYn"),
                                lotMaster.stockAmount.as("stockAmount"),
                                lotMaster.createdAmount.as("createdAmount"),
                                lotMaster.badItemAmount.as("badItemAmount"),
                                lotMaster.inputAmount.as("inputAmount"),
                                lotMaster.changeAmount.as("changeAmount"),
                                lotMaster.transferAmount.as("transferAmount"),
                                lotMaster.inspectAmount.as("inspectAmount"),
                                lotMaster.shipmentAmount.as("shipmentAmount"),
                                lotMaster.badItemReturnAmount.as("badItemReturnAmount"),
                                lotMaster.stockReturnAmount.as("stockReturnAmount"),
                                lotMaster.checkRequestAmount.as("checkRequestAmount"),
                                lotMaster.checkAmount.as("checkAmount"),
                                lotMaster.qualityLevel.as("qualityLevel"),
                                lotMaster.createdDate.as("createdDate"),
                                item.itemAccountCode.detail.as("itemAccountCode")
                        )
                )
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(wareHouse).on(wareHouse.id.eq(lotMaster.wareHouse.id))
                .leftJoin(lotType).on(lotType.id.eq(lotMaster.lotType.id))
                .leftJoin(itemGroup).on(itemGroup.id.eq(item.itemGroup.id))
                .leftJoin(workProcess).on(workProcess.id.eq(lotMaster.workProcess.id))
                .where(
                        isItemGroupEq(itemGroupId),
                        isLotNoContain(lotNo),
                        isItemNoAndItemNameContain(itemNoAndItemName),
                        isWarehouseEq(wareHouseId),
                        isEnrollmentTypeEq(enrollmentType),
                        isStockYn(stockYn),
                        isLotTypeEq(lotTypeId),
                        isCheckAmountYn(testingYn),
                        isWorkProcessDivisionEq(workProcessDivision),
                        lotMaster.lotMasterDivision.eq(REAL_LOT),
                        isDeleteYnFalse()
                )
                .orderBy(lotMaster.createdDate.desc())
                .fetch();
    }

    private BooleanExpression isWorkProcessDivisionEq(WorkProcessDivision workProcessDivision) {
        return workProcessDivision != null ? workProcess.workProcessDivision.eq(workProcessDivision) : null;
    }

    //????????????????????? LOT????????? ??????
    @Override
    @Transactional(readOnly = true)
    public List<OutsourcingInputLOTResponse> findLotMastersByOutsourcing(Long input) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                OutsourcingInputLOTResponse.class,
                                outSourcingInput.id.as("id"),
                                lotMaster.id.as("lotId"),
                                lotType.lotType.as("lotType"),
                                lotMaster.lotNo.as("lotNo"),
                                lotMaster.createdAmount.as("inputAmount"),
                                lotMaster.item.testType.as("testRequestType")
                        )
                )
                .from(lotMaster)
                .innerJoin(lotType).on(lotType.id.eq(lotMaster.lotType.id))
                .innerJoin(outSourcingInput).on(outSourcingInput.id.eq(lotMaster.outSourcingInput.id))
                .where(
                        outSourcingInput.id.eq(input),
                        isDeleteYnFalse()
                )
                .fetch();
    }

    //id??? LOT????????? ??????
    @Transactional(readOnly = true)
    public OutsourcingInputLOTResponse findLotMasterById(Long id){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                OutsourcingInputLOTResponse.class,
                                outSourcingInput.id.as("id"),
                                lotMaster.id.as("lotId"),
                                lotType.lotType.as("lotType"),
                                lotMaster.lotNo.as("lotNo"),
                                lotMaster.stockAmount.as("inputAmount"),
                                lotMaster.item.testType.as("testRequestType")
                        )
                )
                .from(lotMaster)
                .innerJoin(lotType).on(lotType.id.eq(lotMaster.lotType.id))
                .innerJoin(outSourcingInput).on(outSourcingInput.id.eq(lotMaster.outSourcingInput.id))
                .where(
                        lotMaster.id.eq(id),
                        isDeleteYnFalse()
                )
                .fetchOne();
    }

    //????????????????????? id??? LOT????????? ??????
    @Transactional(readOnly = true)
    public OutsourcingInputLOTResponse findLotMasterByInputAndId(OutSourcingInput input, Long id){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                OutsourcingInputLOTResponse.class,
                                outSourcingInput.id.as("id"),
                                lotMaster.id.as("lotId"),
                                lotType.lotType.as("lotType"),
                                lotMaster.lotNo.as("lotNo"),
                                lotMaster.stockAmount.as("inputAmount"),
                                lotMaster.item.testType.as("testRequestType")
                        )
                )
                .from(lotMaster)
                .innerJoin(lotType).on(lotType.id.eq(lotMaster.lotType.id))
                .innerJoin(outSourcingInput).on(outSourcingInput.id.eq(lotMaster.outSourcingInput.id))
                .where(
                        lotMaster.id.eq(id),
                        outSourcingInput.eq(input),
                        isDeleteYnFalse()
                )
                .fetchOne();
    }

    //???????????? ??????
    @Transactional(readOnly = true)
    public List<MaterialStockReponse> findStockByItemAccountAndItemAndItemAccountCode(
            Long itemAccountId, Long itemId, Long itemGroupId, Long warehouseId){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                MaterialStockReponse.class,
                                lotMaster.item.itemNo.as("itemNo"),
                                lotMaster.item.itemName.as("itemName"),
                                item.manufacturer.clientCode.as("manufacturerCode"),
                                lotMaster.wareHouse.id.as("warehouseId"),
                                lotMaster.stockAmount.sum().as("amount")
                        )
                )
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(itemAccount).on(itemAccount.id.eq(item.itemAccount.id))
                .leftJoin(itemAccountCode).on(itemAccountCode.id.eq(item.itemAccountCode.id))
                .leftJoin(client).on(client.id.eq(item.manufacturer.id))
                .leftJoin(wareHouse).on(wareHouse.id.eq(lotMaster.wareHouse.id))
                .where(
                        isWarehouseEq(warehouseId),
                        isItemGroupEq(itemGroupId),
                        isItemAccountEq(itemAccountId),
                        isDeleteYnFalse()
                )
                .groupBy(item,wareHouse,lotMaster.enrollmentType)
                .orderBy(item.id.asc())
                .fetch();
    }

    // ?????? LOT ?????? ?????? ??? LOT ?????? ?????? API
    @Override
    public List<LotMasterResponse.idAndLotNo> findLotMastersByShipmentLotCondition(Long itemId, int notShippedAmount) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                LotMasterResponse.idAndLotNo.class,
                                lotMaster.id.as("id"),
                                lotMaster.lotNo.as("lotNo"),
                                lotMaster.item.id.as("itemId"),
                                lotMaster.item.itemNo.as("itemNo"),
                                lotMaster.item.itemName.as("itemName"),
                                lotMaster.workProcess.id.as("workProcessId"),
                                lotMaster.workProcess.workProcessDivision.as("workProcessDivision"),
                                lotMaster.stockAmount.as("stockAmount")
                        )
                )
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(workProcess).on(workProcess.id.eq(lotMaster.workProcess.id))
                .where(
                        item.id.eq(itemId),
                        workProcess.workProcessDivision.eq(PACKAGING),
                        lotMaster.stockAmount.goe(1),
                        lotMaster.stockAmount.loe(notShippedAmount),
                        lotMaster.deleteYn.isFalse(),
                        lotMaster.lotMasterDivision.eq(REAL_LOT)

                )
                .fetch();
    }

    @Transactional(readOnly = true)
    //ITEM?????? ?????? ???????????? ??????
    public List<MaterialStockReponse> findStockAmountByItemId(Long itemId, Long warehouseId){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                MaterialStockReponse.class,
                                lotMaster.item.id.as("itemId"),
                                lotMaster.wareHouse.id.as("warehouseId"),
                                lotMaster.stockAmount.sum().as("amount"),
                                item.inputUnitPrice.as("inputUnitPrice"),
                                lotMaster.outSourcingInput.id.as("outsourcingId"),
                                lotMaster.workProcess.id.as("workProcessId")
                        )
                )
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(wareHouse).on(wareHouse.id.eq(lotMaster.wareHouse.id))
                .where(
                        isWarehouseEq(warehouseId),
                        item.id.eq(itemId),
                        lotMaster.deleteYn.isFalse(),
                        lotMaster.stockAmount.gt(0)
                )
                .groupBy(lotMaster.wareHouse, lotMaster.item, lotMaster.outSourcingInput)
                .fetch();
    }

    //????????? ?????? ?????? ????????????
    @Transactional(readOnly = true)
    public List<PopRecycleResponse> findBadAmountByWorkProcess(Long workProcessId){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                PopRecycleResponse.class,
                                item.id.as("id"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName"),
                                lotMaster.badItemAmount.sum().as("badAmount"),
                                lotMaster.recycleAmount.sum().as("recycleAmount")
                        )
                )
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(workProcess).on(workProcess.id.eq(lotMaster.workProcess.id))
                .innerJoin(lotLog).on(lotLog.lotMaster.id.eq(lotMaster.id))
                .innerJoin(workOrderDetail).on(workOrderDetail.id.eq(lotLog.workOrderDetail.id))
                .where(
                        lotMaster.workProcess.id.eq(workProcessId),
                        //workOrderDetail.orderState.eq(COMPLETION),
                        lotMaster.lotMasterDivision.eq(DUMMY_LOT),
                        lotMaster.deleteYn.eq(false),
                        lotMaster.useYn.eq(true)
                )
                .groupBy(lotMaster.item.id)
                .fetch();
    }

    //????????? ?????? ?????? ?????? ??????
    @Transactional(readOnly = true)
    public PopRecycleResponse findBadAmountByWorkProcess(Long workProcessId, Long itemId){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                PopRecycleResponse.class,
                                item.id.as("id"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName"),
                                lotMaster.badItemAmount.sum().as("badAmount"),
                                lotMaster.recycleAmount.sum().as("recycleAmount")
                        )
                )
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(workProcess).on(workProcess.id.eq(lotMaster.workProcess.id))
                .where(
                        lotMaster.workProcess.id.eq(workProcessId),
                        lotMaster.item.id.eq(itemId),
                        lotMaster.lotMasterDivision.eq(DUMMY_LOT)
                )
                .groupBy(lotMaster.item.id)
                .fetchOne();
    }

    //????????? ????????? LOT??????
    @Transactional(readOnly = true)
    public List<LotMaster> findBadLotByItemIdAndWorkProcess(Long itemId, Long workProcessId, LotMasterDivision division){
        return jpaQueryFactory
                .selectFrom(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(workProcess).on(workProcess.id.eq(lotMaster.workProcess.id))
                .where(
                        lotMaster.item.id.eq(itemId),
                        lotMaster.workProcess.id.eq(workProcessId),
                        lotMaster.badItemAmount.gt(0),
                        lotMaster.deleteYn.eq(false),
                        lotMaster.useYn.eq(true),
                        lotMaster.lotMasterDivision.eq(division)
                )
                .orderBy(lotMaster.createdDate.desc())
                .fetch();
    }

    // ?????? id ??? ???????????? lotMaster ?????? 1 ??????
    @Override
    public List<PopBomDetailLotMasterResponse> findAllByItemIdAndLotNo(Long itemId, String lotNo) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                PopBomDetailLotMasterResponse.class,
                                lotMaster.id.as("lotMasterId"),
                                lotMaster.lotNo.as("lotNo"),
                                lotMaster.stockAmount.as("stockAmount"),
                                unit.unitCode.as("unitCodeName"),
                                unit.exhaustYn.as("exhaustYn")
                        )
                )
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(unit).on(unit.id.eq(item.unit.id))
                .where(
                        lotMaster.item.id.eq(itemId),
                        lotMaster.deleteYn.isFalse(),
                        isLotNoContain(lotNo),
                        lotMaster.stockAmount.goe(1),
                        lotMaster.lotMasterDivision.eq(REAL_LOT)
                )
                .fetch();
    }

    // ??????, ????????? ??????????????? ?????? ??????
    public List<LabelPrintResponse> findPrintsByWorkProcessAndEquipment(Long workProcessId, Long equipmentId){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                LabelPrintResponse.class,
                                lotMaster.id.as("lotMasterId"),
                                lotMaster.lotNo.as("lotNo"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName"),
                                lotMaster.stockAmount.as("amount"),
                                lotMaster.labelPrintYn.as("labelPrintYn")
                        )
                )
                .from(lotMaster)
                .innerJoin(item).on(item.id.eq(lotMaster.item.id))
                //.leftJoin(lotLog).on(lotLog.lotMaster.id.eq(lotMaster.id))
                //.innerJoin(workOrderDetail).on(workOrderDetail.id.eq(lotLog.workOrderDetail.id))
                .innerJoin(workProcess).on(workProcess.id.eq(lotMaster.workProcess.id))
                .innerJoin(equipment).on(equipment.id.eq(lotMaster.equipment.id))
                .where(
                        //workOrderDetail.orderState.eq(COMPLETION),
                        lotMaster.lotMasterDivision.eq(REAL_LOT),
                        lotMaster.deleteYn.eq(false),
                        lotMaster.stockAmount.gt(0),
                        lotMaster.useYn.eq(true),
                        lotMaster.workProcess.id.eq(workProcessId),
                        lotMaster.equipment.id.eq(equipmentId)
                )
                .fetch();
    }

    // ??????????????? ????????????, lotDivision ??? dummny ??? ??? ?????????
    @Override
    public Optional<String> findDummyNoByDivision(LotMasterDivision lotMasterDivision, LocalDate startDate) {
        return Optional.ofNullable(jpaQueryFactory
                .select(lotMaster.lotNo)
                .from(lotMaster)
                .where(
                        lotMaster.createdDate.between(startDate.atStartOfDay(), LocalDateTime.of(startDate, LocalTime.MAX).withNano(0)),
                        lotMaster.lotMasterDivision.eq(lotMasterDivision)
                )
                .orderBy(lotMaster.createdDate.desc())
                .limit(1)
                .fetchOne());
    }

    @Override
    public Optional<BadItemWorkOrderResponse.subDto> findLotMaterByDummyLotIdAndWorkProcessId(Long dummyLotId, Long workProcessId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(
                                Projections.fields(
                                        BadItemWorkOrderResponse.subDto.class,
                                        lotMaster.badItemAmount.as("badAmount"),
                                        lotMaster.createdAmount.as("createAmount"),
                                        item.itemNo.as("itemNo"),
                                        item.itemName.as("itemName"),
                                        item.unit.unitCode.as("unitCode")
                                )
                        )
                        .from(lotEquipmentConnect)
                        .leftJoin(lotMaster).on(lotMaster.id.eq(lotEquipmentConnect.parentLot.id))
                        .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                        .leftJoin(workProcess).on(workProcess.id.eq(lotMaster.workProcess.id))
                        .where(
                                lotMaster.id.eq(dummyLotId),
                                workProcess.id.eq(workProcessId),
                                lotMaster.lotMasterDivision.eq(DUMMY_LOT),
                                lotMaster.deleteYn.isFalse()
                        )
                        .limit(1)
                        .fetchOne()
        );
    }

    // ??????????????? ????????????, ????????????, inputEquipment ??? ?????????
    @Override
    public Optional<LotMaster> findByTodayAndWorkProcessDivisionEqAndInputEquipmentEq(LocalDate now, WorkProcessDivision workProcessDivision, Long inputEquipmentId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(lotMaster)
                        .where(
                                lotMaster.createdDate.between(now.atStartOfDay(), LocalDateTime.of(now, LocalTime.MAX).withNano(0)),
                                lotMaster.workProcess.workProcessDivision.eq(workProcessDivision),
                                lotMaster.inputEquipment.id.eq(inputEquipmentId),
                                lotMaster.deleteYn.isFalse(),
                                lotMaster.exhaustYn.isFalse(),
                                lotMaster.stockAmount.goe(1),
                                lotMaster.lotMasterDivision.eq(REAL_LOT)
                        )
                        .fetchOne()
        );
    }

    //??????????????? LOT ??????
    @Transactional(readOnly = true)
    public Optional<LotMaster> findByOutsourcingInput(Long outsourcingInputId){
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(lotMaster)
                        .leftJoin(outSourcingInput).on(outSourcingInput.id.eq(lotMaster.outSourcingInput.id))
                        .where(
                                outSourcingInput.id.eq(outsourcingInputId),
                                isDeleteYnFalse(),
                                lotMaster.enrollmentType.eq(OUTSOURCING_INPUT)
                        )
                        .fetchOne()
        );
    }

    // LOT ????????? ??????, ????????????: ???????????? id, LOT ??????, ??????|??????, ?????? id, ????????????, ????????????, LOT ??????, ???????????????, ????????????
    // ????????????
    private BooleanExpression isItemGroupEq(Long itemGroupId) {
        return itemGroupId != null ? itemGroup.id.eq(itemGroupId) : null;
    }
    // lot ??????
    private BooleanExpression isLotNoContain(String lotNo) {
        return lotNo != null ? lotMaster.lotNo.contains(lotNo) : null;
    }

    //????????????
    private BooleanExpression isItemAccountEq(Long itemAccountId) {
        return itemAccountId != null ? itemAccount.id.eq(itemAccountId) : null;
    }

    //????????????
    private BooleanExpression isItemAccountCodeEq(Long itemAccountCodeId) {
        return itemAccountCodeId != null ? itemAccountCode.id.eq(itemAccountCodeId) : null;
    }
    // ??????|??????
    private BooleanExpression isItemNoAndItemNameContain(String itemNoAndName) {
        return itemNoAndName != null ? item.itemNo.contains(itemNoAndName).or(item.itemName.contains(itemNoAndName)) : null;
    }
    // ?????? id
    private BooleanExpression isWarehouseEq(Long warehouseId) {
        return warehouseId != null ? wareHouse.id.eq(warehouseId) : null;
    }
    // ????????????
    private BooleanExpression isEnrollmentTypeEq(EnrollmentType enrollmentType) {
        return enrollmentType != null ? lotMaster.enrollmentType.eq(enrollmentType) : null;
    }
    // ????????????
    // null
    // true: ???????????? 1 ??????
    // false: ???????????? 0
    private BooleanExpression isStockYn(Boolean stockYn) {
        return stockYn != null ? (stockYn ? lotMaster.stockAmount.goe(1) : lotMaster.stockAmount.lt(1)) : null;
    }
    // LOT ??????
    private BooleanExpression isLotTypeEq(Long lotTypeId) {
        return lotTypeId != null ? lotMaster.lotType.id.eq(lotTypeId) : null;
    }
    // ???????????????
    // null
    // true: checkAmount 1 ?????? && ??????????????? ?????????????????? ??????
    // false: checkAmount 0
    private BooleanExpression isCheckAmountYn(Boolean checkAmountYn) {
        return checkAmountYn != null ?
                (checkAmountYn ? lotMaster.checkAmount.goe(1).and(lotMaster.checkAmount.lt(lotMaster.stockAmount)) : lotMaster.checkAmount.lt(1))
                : null;
    }

    // ================================================== 7-2. Lot Tracking ==============================================

    // lotTracking ????????????: LOT ??????(?????????), ????????????(?????????), ??????|??????
    // ????????? true: ????????? LOT ??? ????????? LOT ??????
    // ????????? LOT NO ??? ????????? LotConnect ??????
    @Override
    public List<LotEquipmentConnect> findExhaustLotByLotNoAndTrackTypeTrue(String lotNo) {
        return jpaQueryFactory
                .select(lotEquipmentConnect)
                .from(lotConnect)
                .leftJoin(lotMaster).on(lotMaster.id.eq(lotConnect.childLot.id))
                .leftJoin(lotEquipmentConnect).on(lotEquipmentConnect.id.eq(lotConnect.parentLot.id))
                .where(
                        lotMaster.lotNo.eq(lotNo),                   // LOT ?????? ?????????
                        lotConnect.division.eq(EXHAUST),             // ????????? ???????????? ??????
                        lotMaster.deleteYn.isFalse(),                // ????????? LOT ?????? ??????
                        lotMaster.lotMasterDivision.eq(REAL_LOT)     // lotMaster ?????? ????????????
                )
                .fetch();
    }

    // ?????? ???????????? return ?????? equipmentLotId ??? lotConnect ??? division ??? ???????????? ????????? ???????
    @Override
    public List<LotTrackingResponse> findLotTrackingResponseByTrackingTypeTrue(Long lotEquipmentConnectId, String itemNoAndItemName) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                LotTrackingResponse.class,
                                lotMaster.id.as("lotMasterId"),
                                lotMaster.lotNo.as("lotNo"),
                                lotMaster.item.itemNo.as("itemNo"),
                                lotMaster.item.itemName.as("itemName"),
                                lotMaster.enrollmentType.as("enrollmentType"),
                                lotMaster.createdDate.as("createdDate")
                        )
                )
                .from(lotConnect)
                .leftJoin(lotMaster).on(lotMaster.id.eq(lotConnect.childLot.id))
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .where(
                        lotConnect.parentLot.childLot.id.eq(lotEquipmentConnectId),      // lotEquipmentConnect ?????????
                        lotConnect.division.eq(FAMILY),                         // ????????? ????????????
                        lotMaster.deleteYn.isFalse(),                           // ????????? LOT ?????? ??????
                        isItemNoAndItemNameContain(itemNoAndItemName),          // ??????|??????,
                        lotMaster.lotMasterDivision.eq(REAL_LOT)                // lotMaster ?????? ????????????
                )
                .fetch();
    }

    // lotTracking ????????????: LOT ??????(?????????), ????????????(?????????), ??????|??????
    // ????????? false: ????????? LOT ??? ???????????? ?????? ????????? LOT
    @Override
    public List<LotTrackingResponse> findLotTrackingResponseByTrackingTypeFalse(Long equipmentLotId, String itemNoAndItemName) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                LotTrackingResponse.class,
                                lotMaster.id.as("lotMasterId"),
                                lotMaster.lotNo.as("lotNo"),
                                lotMaster.item.itemNo.as("itemNo"),
                                lotMaster.item.itemName.as("itemName"),
                                lotMaster.enrollmentType.as("enrollmentType"),
                                lotMaster.createdDate.as("createdDate"),
                                lotConnect.amount.as("inputAmount")
                        )
                )
                .from(lotConnect)
                .leftJoin(lotMaster).on(lotMaster.id.eq(lotConnect.childLot.id))
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .where(
                        lotConnect.parentLot.childLot.id.eq(equipmentLotId),
                        lotConnect.division.eq(EXHAUST),
                        lotMaster.deleteYn.isFalse(),
                        isItemNoAndItemNameContain(itemNoAndItemName)
                )
                .fetch();
    }

    // ?????? lotNo ??? ?????? LOT id ?????? ??????
    @Override
    public Optional<LotMaster> findEquipmentLotMasterByRealLotNo(String realLotNo) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(lotConnect.parentLot.childLot)
                        .from(lotConnect)
                        .where(
                                lotConnect.childLot.lotMasterDivision.eq(REAL_LOT),  // lotMaster ?????? ????????????
                                lotConnect.division.eq(FAMILY),                    // ????????? ??????
                                lotConnect.parentLot.childLot.deleteYn.isFalse(),
                                lotConnect.childLot.lotNo.eq(realLotNo)               // ????????????
                        )
                        .limit(1)
                        .fetchOne()
        );
    }

    // ???????????? ??? ???????????? ??????
    // lotMaster ??? realLot ??? stockAmount ??? 0 ????????????, ?????????????????? ??????????????? ??????????????? ????????? ????????? 5???
    @Override
    public List<ItemInventoryStatusResponse> findItemInventoryStatusResponseByGoodsType(GoodsType goodsType) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                ItemInventoryStatusResponse.class,
                                item.id.as("itemId"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName"),
                                lotMaster.stockAmount.sum().as("stockAmount")
                        )
                )
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(itemAccount).on(itemAccount.id.eq(item.itemAccount.id))
                .where(
                        itemAccount.goodsType.eq(goodsType),
                        lotMaster.deleteYn.isFalse(),
                        lotMaster.stockAmount.ne(0),
                        lotMaster.lotMasterDivision.eq(REAL_LOT)
                )
                .orderBy(lotMaster.stockAmount.desc())
                .groupBy(item.id)
                .limit(5)
                .fetch();
    }

    // ?????????????????? - ?????? ??????
    @Override
    public List<ItemResponse.noAndName>findSalesRelatedStatusResponseByProductItems(LocalDate fromDate, LocalDate toDate) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                ItemResponse.noAndName.class,
                                item.id.as("id"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName")
                        )
                )
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(itemAccount).on(itemAccount.id.eq(item.itemAccount.id))
                .where(
                        lotMaster.deleteYn.isFalse(),
                        lotMaster.createdDate.between(fromDate.atStartOfDay(), LocalDateTime.of(toDate, LocalTime.MAX).withNano(0)),
                        lotMaster.lotMasterDivision.eq(REAL_LOT),
                        itemAccount.goodsType.eq(PRODUCT),
                        lotMaster.enrollmentType.eq(PRODUCTION)
                )
                .groupBy(item.id)
                .orderBy(lotMaster.createdAmount.sum().desc())
                .limit(5)
                .fetch();
    }

    // ??? ?????? ?????? ??? ?????? ??????
    @Override
    public Optional<Integer> findCreatedAmountByWeekDate(LocalDate fromDate, LocalDate toDate, Long itemId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(lotMaster.createdAmount.sum())
                        .from(lotMaster)
                        .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                        .leftJoin(itemAccount).on(itemAccount.id.eq(item.itemAccount.id))
                        .where(
                                lotMaster.deleteYn.isFalse(),
                                lotMaster.createdDate.between(fromDate.atStartOfDay(), LocalDateTime.of(toDate, LocalTime.MAX).withNano(0)),
                                lotMaster.lotMasterDivision.eq(REAL_LOT),
                                itemAccount.goodsType.eq(PRODUCT),
                                lotMaster.enrollmentType.eq(PRODUCTION),
                                lotMaster.item.id.eq(itemId)
                        )
                        .fetchOne()
        );
    }

    //????????? LOT????????? ??????
    @Override
    public List<RecycleLotResponse> findRecycleLots(LocalDate fromDate, LocalDate toDate){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                RecycleLotResponse.class,
                                lotMaster.id.as("id"),
                                lotMaster.lotNo.as("lotNo"),
                                item.itemName.as("itemName"),
                                item.itemNo.as("itemNo"),
                                lotMaster.stockAmount.as("stockAmount"),
                                lotMaster.workProcess.workProcessName.as("workProcess"),
                                lotMaster.labelPrintYn.as("labelPrintYn")
                        )
                )
                .from(lotMaster)
                .leftJoin(item).on(item.id.eq(lotMaster.item.id))
                .leftJoin(workProcess).on(workProcess.id.eq(lotMaster.workProcess.id))
                .where(
                        lotMaster.deleteYn.isFalse(),
                        dateNull(fromDate, toDate),
                        lotMaster.enrollmentType.eq(RECYCLE)
                )
                .fetch();
    }

    private BooleanExpression isLotNoLengthEq(int length) {
        return lotMaster.lotNo.length().eq(length);
    }

    private BooleanExpression isLotNoDateContain(String date) {
        return lotMaster.lotNo.contains(date);
    }

    private BooleanExpression isDeleteYnFalse() {
        return lotMaster.deleteYn.isFalse();
    }

    private BooleanExpression isCodeContain(String code) {
        return lotMaster.lotNo.contains(code);
    }

    private  BooleanExpression dateNull(LocalDate startDate, LocalDate endDate){
        if (startDate != null && endDate != null) {
            return lotMaster.createdDate.between(startDate.atStartOfDay(), LocalDateTime.of(endDate, LocalTime.MAX).withNano(0));
        } else if (startDate != null) {
            return lotMaster.createdDate.after(startDate.atStartOfDay());
        } else if (endDate != null) {
            return lotMaster.createdDate.before(LocalDateTime.of(endDate, LocalTime.MAX).withNano(0));
        } else {
            return null;
        }
    }
}
