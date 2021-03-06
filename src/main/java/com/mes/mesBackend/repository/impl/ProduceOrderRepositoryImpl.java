package com.mes.mesBackend.repository.impl;

import com.mes.mesBackend.dto.response.ProduceOrderDetailResponse;
import com.mes.mesBackend.dto.response.ProduceOrderResponse;
import com.mes.mesBackend.dto.response.ProductionPerformanceResponse;
import com.mes.mesBackend.entity.*;
import com.mes.mesBackend.entity.enumeration.OrderState;
import com.mes.mesBackend.repository.custom.ProduceOrderRepositoryCustom;
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

@RequiredArgsConstructor
public class ProduceOrderRepositoryImpl implements ProduceOrderRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    final QProduceOrder produceOrder = QProduceOrder.produceOrder;
    final QBomMaster bomMaster = QBomMaster.bomMaster;
    final QBomItemDetail bomItemDetail = QBomItemDetail.bomItemDetail;
    final QItem item = QItem.item;
    final QWorkProcess workProcess = QWorkProcess.workProcess;
    final QUnit unit = QUnit.unit;
    final QItemAccount itemAccount = QItemAccount.itemAccount;
    final QContractItem contractItem = QContractItem.contractItem;
    final QWorkOrderDetail workOrderDetail = QWorkOrderDetail.workOrderDetail;
    final QContract contract = QContract.contract;
    final QClient client = QClient.client;

    // 제조 오더 리스트 조회, 검색조건 : 품목그룹 id, 품명|품번, 지시상태, 제조오더번호, 수주번호, 착수예정일 fromDate~toDate, 자재납기일자(보류)
    @Override
    @Transactional(readOnly = true)
    public List<ProduceOrderResponse> findAllByCondition(
            Long itemGroupId,
            String itemNoAndName,
            OrderState orderState,
            String produceOrderNo,
            String contractNo,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                ProduceOrderResponse.class,
                                produceOrder.id.as("id"),
                                produceOrder.produceOrderNo.as("produceOrderNo"),
                                contractItem.id.as("contractItemId"),
                                item.itemNo.as("contractItemItemNo"),
                                item.itemName.as("contractItemItemName"),
                                contractItem.contractType.as("contractItemContractType"),
                                contractItem.amount.as("contractItemAmount"),
                                contract.id.as("contractId"),
                                contract.contractNo.as("contractNo"),
                                client.clientName.as("contractCName"),
                                contract.periodDate.as("contractPeriodDate"),
                                produceOrder.expectedStartedDate.as("expectedStartedDate"),
                                produceOrder.expectedCompletedDate.as("expectedCompletedDate"),
                                produceOrder.orderState.as("orderState"),
                                produceOrder.rate.as("rate"),
                                produceOrder.note.as("note")
                        )
                )
                .from(produceOrder)
                .leftJoin(contractItem).on(contractItem.id.eq(produceOrder.contractItem.id))
                .leftJoin(item).on(item.id.eq(contractItem.item.id))
                .leftJoin(contract).on(contract.id.eq(produceOrder.contract.id))
                .leftJoin(client).on(client.id.eq(contract.client.id))
                .where(
                        isItemGroupEq(itemGroupId),
                        isItemNoAndItemNameContain(itemNoAndName),
                        isInstructionStatus(orderState),
                        isProduceOrderNoContain(produceOrderNo),
                        isContractNoContain(contractNo),
                        isExpectedCompletedDateBetween(fromDate, toDate),
                        isDeleteYnFalse()
                )
                .orderBy(produceOrder.createdDate.desc())
                .fetch();
    }

    // 제조오더 response 단일 조회
    @Override
    public Optional<ProduceOrderResponse> findResponseByProduceOrderId(Long id) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(
                                Projections.fields(
                                        ProduceOrderResponse.class,
                                        produceOrder.id.as("id"),
                                        produceOrder.produceOrderNo.as("produceOrderNo"),
                                        contractItem.id.as("contractItemId"),
                                        item.itemNo.as("contractItemItemNo"),
                                        item.itemName.as("contractItemItemName"),
                                        contractItem.contractType.as("contractItemContractType"),
                                        contractItem.amount.as("contractItemAmount"),
                                        contract.id.as("contractId"),
                                        contract.contractNo.as("contractNo"),
                                        client.clientName.as("contractCName"),
                                        contract.periodDate.as("contractPeriodDate"),
                                        produceOrder.expectedStartedDate.as("expectedStartedDate"),
                                        produceOrder.expectedCompletedDate.as("expectedCompletedDate"),
                                        produceOrder.orderState.as("orderState"),
                                        produceOrder.rate.as("rate"),
                                        produceOrder.note.as("note")
                                )
                        )
                        .from(produceOrder)
                        .leftJoin(contractItem).on(contractItem.id.eq(produceOrder.contractItem.id))
                        .leftJoin(item).on(item.id.eq(contractItem.item.id))
                        .leftJoin(contract).on(contract.id.eq(produceOrder.contract.id))
                        .leftJoin(client).on(client.id.eq(contract.client.id))
                        .where(
                                produceOrder.id.eq(id),
                                isDeleteYnFalse()
                        )
                        .fetchOne()
        );
    }

    // where: produceOrder.contractItem.item = bomMaster.item
    // produceOrder.contractItem.item = bomMaster.item 랑 같은걸 찾으면 ? list로
    @Override
    @Transactional(readOnly = true)
    public List<ProduceOrderDetailResponse> findAllProduceOrderDetail(Long itemId) {
        return jpaQueryFactory
                .select(
                        Projections.fields(ProduceOrderDetailResponse.class,
                                bomItemDetail.id.as("bomItemDetailId"),
                                item.id.as("itemId"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName"),
                                itemAccount.account.as("itemAccount"),
                                bomItemDetail.amount.as("bomAmount"),
                                workProcess.workProcessName.as("workProcess"),
                                unit.unitCodeName.as("orderUnit"),
                                bomItemDetail.level.as("level"),
                                itemAccount.goodsType.as("goodsType")
                        )
                )
                .from(bomItemDetail)
                .leftJoin(bomMaster).on(bomMaster.id.eq(bomItemDetail.bomMaster.id))
                .leftJoin(item).on(item.id.eq(bomItemDetail.item.id))
                .leftJoin(workProcess).on(workProcess.id.eq(bomItemDetail.workProcess.id))
                .leftJoin(unit).on(unit.id.eq(item.unit.id))
                .leftJoin(itemAccount).on(itemAccount.id.eq(item.itemAccount.id))
                .where(
                        bomMaster.item.id.eq(itemId)
                )
                .orderBy(workProcess.orders.asc())
                .fetch();
    }
    @Transactional(readOnly = true)
    public ProduceOrder findByIdforShortage(Long id){
        return jpaQueryFactory
                .selectFrom(produceOrder)
                .where(
                        produceOrder.id.eq(id),
                        produceOrder.deleteYn.eq(false)
                )
                .fetchOne();
    }

    final QWorkOrderDetail subWorkOrderDetail = QWorkOrderDetail.workOrderDetail;
    // 생산실적 조회
    @Override
    public List<ProductionPerformanceResponse> findProductionPerformanceResponseByCondition(
            LocalDate fromDate,
            LocalDate toDate,
            Long itemGroupId,
            String itemNoOrItemName
    ) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                ProductionPerformanceResponse.class,
                                produceOrder.id.as("id"),
                                contract.contractNo.as("contractNo"),
                                produceOrder.produceOrderNo.as("produceOrderNo"),
                                client.clientName.as("clientName"),
                                item.itemName.as("itemName"),
                                contract.periodDate.as("periodDate"),
                                contractItem.amount.as("contractItemAmount"),
                                contract.user.korName.as("korName"),
                                item.inputUnitPrice.as("unitPrice"),
                                item.inputUnitPrice.multiply(contractItem.amount).as("price")
                        )
                )
                .from(produceOrder)
                .leftJoin(contractItem).on(contractItem.id.eq(produceOrder.contractItem.id))
                .leftJoin(contract).on(contract.id.eq(contractItem.contract.id))
                .leftJoin(client).on(client.id.eq(contract.client.id))
                .leftJoin(item).on(item.id.eq(contractItem.item.id))
                .leftJoin(workOrderDetail).on(workOrderDetail.produceOrder.id.eq(produceOrder.id))
                .where(
                        produceOrder.deleteYn.isFalse(),
                        isItemGroupIdEq(itemGroupId),
                        isItemNameAneItemNoCon(itemNoOrItemName)
//                        isProduceOrderCreatedDateBetween(fromDate, toDate)
                )
                .groupBy(produceOrder.id)
                .orderBy(produceOrder.createdDate.desc())
                .fetch();
    }

    private BooleanExpression isProduceOrderCreatedDateBetween(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return produceOrder.createdDate.between(fromDate.atStartOfDay(), LocalDateTime.of(toDate, LocalTime.MAX).withNano(0));
        } else if (fromDate != null) {
            return produceOrder.createdDate.after(fromDate.atStartOfDay());
        } else if (toDate != null) {
            return produceOrder.createdDate.before(LocalDateTime.of(toDate, LocalTime.MAX).withNano(0));
        } else {
            return null;
        }
    }

    private BooleanExpression isItemGroupIdEq(Long itemGroupId) {
        return itemGroupId != null ? item.itemGroup.id.eq(itemGroupId) : null;
    }

    private BooleanExpression isItemNameAneItemNoCon(String itemNameOrItemName) {
        return itemNameOrItemName != null ? item.itemNo.contains(itemNameOrItemName)
                .or(item.itemName.contains(itemNameOrItemName)) : null;
    }

    /*
     * > contract.item.itemGroup -> NullPointerException
     * QueryPath 를 이용해서 쿼리를 빌드할 때 QueryDSL은 기본적으로 현재 엔티티의 속성값에 대해서만 참조 가능.
     * 즉, Direct Properties 만 BuildPath 에 포함할 수 있으며 Deep Initialize 가 필요한 부분은
     * @QueryInit 어노테이션을 적용해야함.
     * */
    private BooleanExpression isItemGroupEq(Long itemGroupId) {
        return itemGroupId != null ? produceOrder.contractItem.item.itemGroup.id.eq(itemGroupId) : null;
    }
    // 품명|품번
    private BooleanExpression isItemNoAndItemNameContain(String itemNoAndName) {
        return itemNoAndName != null ? produceOrder.contractItem.item.itemNo.contains(itemNoAndName)
                .or(produceOrder.contractItem.item.itemName.contains(itemNoAndName)) : null;
    }
    // 지시상태
    private BooleanExpression isInstructionStatus(OrderState orderState) {
        return orderState != null ? produceOrder.orderState.eq(orderState) : null;
    }
    // 제조오더 번호
    private BooleanExpression isProduceOrderNoContain(String produceOrderNo) {
        return produceOrderNo != null ? produceOrder.produceOrderNo.contains(produceOrderNo) : null;
    }
    // 수주번호
    private BooleanExpression isContractNoContain(String contractNo) {
        return contractNo != null ? produceOrder.contract.contractNo.contains(contractNo) : null;
    }
    // 착수예정일 fromDate~toDate
    private BooleanExpression isExpectedCompletedDateBetween(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return produceOrder.expectedStartedDate.between(fromDate, toDate);
        } else if (fromDate != null) {
            return produceOrder.expectedStartedDate.after(fromDate).or(produceOrder.expectedStartedDate.eq(fromDate));
        } else if (toDate != null) {
            return produceOrder.expectedStartedDate.before(toDate).or(produceOrder.expectedStartedDate.eq(toDate));
        } else {
            return null;
        }
    }
    // 삭제여부
    private BooleanExpression isDeleteYnFalse() {
        return produceOrder.deleteYn.isFalse();
    }
}
