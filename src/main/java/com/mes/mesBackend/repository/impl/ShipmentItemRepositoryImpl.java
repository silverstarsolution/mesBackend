package com.mes.mesBackend.repository.impl;

import com.mes.mesBackend.dto.response.ShipmentItemResponse;
import com.mes.mesBackend.entity.*;
import com.mes.mesBackend.repository.custom.ShipmentItemRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ShipmentItemRepositoryImpl implements ShipmentItemRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    final QContract contract = QContract.contract;
    final QShipmentItem shipmentItem = QShipmentItem.shipmentItem;
    final QShipment shipment = QShipment.shipment;
    final QContractItem contractItem = QContractItem.contractItem;
    final QUnit unit = QUnit.unit;
    final QItem item = QItem.item;
    final QLotMaster lotMaster = QLotMaster.lotMaster;
    final QClient client = QClient.client;
    final QShipmentLot shipmentLot = QShipmentLot.shipmentLot;

    // shipmentItem 에 해당되는 제일 처음 등록된 contract 조회
    @Override
    public Optional<Contract> findContractsByShipmentId(Long shipmentId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(shipmentItem.contractItem.contract)
                        .from(shipmentItem)
                        .leftJoin(contractItem).on(contractItem.id.eq(shipmentItem.contractItem.id))
                        .leftJoin(contract).on(contract.id.eq(contractItem.contract.id))
                        .where(
                                shipmentItem.shipment.id.eq(shipmentId),
                                shipmentItem.deleteYn.isFalse()
                        )
                        .orderBy(shipmentItem.createdDate.desc())
                        .limit(1)
                        .fetchOne()
        );
    }

    // 출하 품목정보 단일 조회
    @Override
    public Optional<ShipmentItemResponse> findShipmentItemResponseByShipmentItemId(Long shipmentId, Long shipmentItemId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(
                                Projections.fields(
                                        ShipmentItemResponse.class,
                                        shipmentItem.id.as("id"),
                                        contract.id.as("contractId"),
                                        contract.contractNo.as("contractNo"),
                                        contractItem.id.as("contractItemId"),
                                        item.itemNo.as("itemNo"),
                                        item.itemName.as("itemName"),
                                        item.standard.as("itemStandard"),
                                        unit.unitCodeName.as("contractUnit"),
                                        shipmentItem.note.as("note"),
                                        contractItem.amount.as("contractItemAmount"),
                                        item.inputUnitPrice.as("itemInputUnitPrice")
                                )
                        )
                        .from(shipmentItem)
                        .innerJoin(shipment).on(shipment.id.eq(shipmentItem.shipment.id))
                        .leftJoin(contractItem).on(contractItem.id.eq(shipmentItem.contractItem.id))
                        .leftJoin(contract).on(contract.id.eq(contractItem.contract.id))
                        .leftJoin(item).on(item.id.eq(contractItem.item.id))
                        .leftJoin(unit).on(unit.id.eq(item.unit.id))
                        .where(
                                isShipmentIdEq(shipmentId),
                                isShipmentItemIdEq(shipmentItemId),
                                isShipmentItemDeleteYnFalse()
                        )
                        .fetchOne()
        );
    }

    // 출하 품목정보 전체 조회
    @Override
    public List<ShipmentItemResponse> findShipmentResponsesByShipmentId(Long shipmentId) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                ShipmentItemResponse.class,
                                shipmentItem.id.as("id"),
                                contract.id.as("contractId"),
                                contract.contractNo.as("contractNo"),
                                contractItem.id.as("contractItemId"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName"),
                                item.standard.as("itemStandard"),
                                unit.unitCodeName.as("contractUnit"),
                                shipmentItem.note.as("note"),
                                contractItem.amount.as("contractItemAmount"),
                                item.inputUnitPrice.as("itemInputUnitPrice")
                        )
                )
                .from(shipmentItem)
                .innerJoin(shipment).on(shipment.id.eq(shipmentItem.shipment.id))
                .leftJoin(contractItem).on(contractItem.id.eq(shipmentItem.contractItem.id))
                .leftJoin(contract).on(contract.id.eq(contractItem.contract.id))
                .leftJoin(item).on(item.id.eq(contractItem.item.id))
                .leftJoin(unit).on(unit.id.eq(item.unit.id))
                .where(
                        isShipmentIdEq(shipmentId),
                        isShipmentItemDeleteYnFalse()
                )
                .fetch();
    }

    // 출하에 수주품목이 있는지
    @Override
    public boolean existsByContractItemInShipment(Long shipmentId, Long contractItemId) {
        Integer fetchOne = jpaQueryFactory
                .selectOne()
                .from(shipmentItem)
                .where(
                        shipmentItem.shipment.id.eq(shipmentId),
                        shipmentItem.contractItem.id.eq(contractItemId),
                        shipmentItem.deleteYn.isFalse()
                )
                .fetchFirst();
        return fetchOne != null;
    }

    // 출하 등록 가능 품목 조회
    @Override
    public List<ShipmentItemResponse> getPossibleShipmentItem(Long clientId){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                ShipmentItemResponse.class,
                                contract.id.as("contractId"),
                                contract.contractNo.as("contractNo"),
                                contractItem.id.as("contractItemId"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName"),
                                item.standard.as("itemStandard"),
                                unit.unitCodeName.as("contractUnit"),
                                contractItem.amount.as("contractItemAmount"),
                                item.inputUnitPrice.as("itemInputUnitPrice")
                        )
                )
                .from(contractItem)
                .innerJoin(item).on(item.id.eq(contractItem.item.id))
                .innerJoin(unit).on(unit.id.eq(item.unit.id))
                .innerJoin(contract).on(contract.id.eq(contractItem.contract.id))
                .innerJoin(client).on(client.id.eq(contract.client.id))
                .where(
                        client.id.eq(clientId),
                        contractItem.deleteYn.isFalse()
                )
                .fetch();
    }

    // shipmentLot 에 해당하는 lotMaster 가 있는지
    // 있으면 true
    @Override
    public boolean existsLotMasterByShipmentLot(Long lotMasterId) {
        Integer fetchOne = jpaQueryFactory
                .selectOne()
                .from(shipmentLot)
                .where(
                        shipmentLot.deleteYn.isFalse(),
                        shipmentLot.lotMaster.id.eq(lotMasterId)
                )
                .fetchFirst();

        return fetchOne != null;
    }

    // shipment id eq
    private BooleanExpression isShipmentIdEq(Long shipmentId) {
        return shipment.id.eq(shipmentId);
    }
    // shipmentItem id eq
    private BooleanExpression isShipmentItemIdEq(Long shipmentItemId) {
        return shipmentItem.id.eq(shipmentItemId);
    }
    // shipment deleteYn false 삭제여부
    private BooleanExpression isShipmentDeleteYnFalse() {
        return shipment.deleteYn.isFalse();
    }
    // shipmentItem deleteYn false 삭제여부
    private BooleanExpression isShipmentItemDeleteYnFalse() {
        return shipmentItem.deleteYn.isFalse();
    }
}
