package com.mes.mesBackend.repository.impl;

import com.mes.mesBackend.dto.response.LotLogResponse;
import com.mes.mesBackend.entity.*;
import com.mes.mesBackend.entity.enumeration.WorkProcessDivision;
import com.mes.mesBackend.repository.custom.LotLogRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.mes.mesBackend.entity.enumeration.OrderState.COMPLETION;

@RequiredArgsConstructor
public class LotLogRepositoryImpl implements LotLogRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    final QLotLog lotLog = QLotLog.lotLog;
    final QWorkOrderDetail workOrderDetail = QWorkOrderDetail.workOrderDetail;
    final QLotMaster lotMaster = QLotMaster.lotMaster;
    final QWorkProcess workProcess = QWorkProcess.workProcess;
    final QProduceOrder produceOrder = QProduceOrder.produceOrder;
    final QContractItem contractItem = QContractItem.contractItem;

    // 작업지시 id 로 createdDate 가 제일 최근인 workProcess 를 가져옴
    @Override
    public Optional<LotLog> findLotLogByWorkOrderIdAndWorkProcessId(Long workOrderId, Long workProcessId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(lotLog)
                        .where(
                                lotLog.workOrderDetail.id.eq(workOrderId),
                                lotLog.workProcess.id.eq(workProcessId)
                        )
                        .orderBy(lotLog.createdDate.desc())
                        .fetchFirst()
        );
    }

    // 작업지시에 해당하는 모든 불량수량 가져옴
    @Override
    public List<Integer> findBadItemAmountByWorkOrderId(Long workOrderId) {
        return jpaQueryFactory
                .select(lotMaster.badItemAmount)
                .from(lotLog)
                .innerJoin(workOrderDetail).on(workOrderDetail.id.eq(lotLog.workOrderDetail.id))
                .innerJoin(lotMaster).on(lotMaster.id.eq(lotLog.lotMaster.id))
                .where(
                        workOrderDetail.id.eq(workOrderId)
                )
                .fetch();
    }

    // 작업지시에 해당하는 모든 생성수량 가져옴
//    @Override
//    public List<Integer> findCreatedAmountByWorkOrderId(Long workOrderId) {
//        return jpaQueryFactory
//                .select(lotMaster.createdAmount)
//                .from(lotLog)
//                .innerJoin(workOrderDetail).on(workOrderDetail.id.eq(lotLog.workOrderDetail.id))
//                .innerJoin(lotMaster).on(lotMaster.id.eq(lotLog.lotMaster.id))
//                .where(
//                        workOrderDetail.id.eq(workOrderId)
//                )
//                .fetch();
//    }

    // 작업지시에 해당하는 lotMaster Id 모두 가져옴
    @Override
    public List<Long> findLotMasterIdByWorkOrderId(Long workOrderId) {
        return jpaQueryFactory
                .select(lotMaster.id)
                .from(lotLog)
                .innerJoin(lotMaster).on(lotMaster.id.eq(lotLog.lotMaster.id))
                .innerJoin(workOrderDetail).on(workOrderDetail.id.eq(lotLog.workOrderDetail.id))
                .where(
                        workOrderDetail.id.eq(workOrderId)
                )
                .fetch();
    }

    // Lot log 조회, 검색조건: 작업공정 id, 작업지시 id, lotMaster id
    @Override
    public List<LotLogResponse> findLotLogResponsesByCondition(Long workProcessId, Long workOrderId, Long lotMasterId) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                LotLogResponse.class,
                                lotLog.id.as("lotLogId"),
                                lotLog.createdDate.as("createdDate"),
                                workProcess.id.as("workProcessId"),
                                workProcess.workProcessName.as("workProcessName"),
                                workOrderDetail.id.as("workOrderId"),
                                workOrderDetail.orderNo.as("workOrderNo"),
                                lotMaster.id.as("lotMasterId"),
                                lotMaster.lotNo.as("lotNo")
                        )
                )
                .from(lotLog)
                .innerJoin(workProcess).on(workProcess.id.eq(lotLog.workProcess.id))
                .innerJoin(workOrderDetail).on(workOrderDetail.id.eq(lotLog.workOrderDetail.id))
                .innerJoin(lotMaster).on(lotMaster.id.eq(lotLog.lotMaster.id))
                .where(
                        isWorkProcessIdEq(workProcessId),
                        isWorkOrderDetailIdEq(workOrderId),
                        isLotMasterIdEq(lotMasterId)
                )
                .fetch();
    }

    // 수주품목과 작업공정에 해당하는 작업지시 가져옴
    @Override
    public Optional<Long> findWorkOrderDetailIdByContractItemAndWorkProcess(Long contractItemId, Long workProcessId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(workOrderDetail.id)
                        .from(workOrderDetail)
                        .leftJoin(produceOrder).on(produceOrder.id.eq(workOrderDetail.produceOrder.id))
                        .leftJoin(contractItem).on(contractItem.id.eq(produceOrder.contract.id))
                        .leftJoin(workProcess).on(workProcess.id.eq(workOrderDetail.workProcess.id))
                        .where(
                                contractItem.id.eq(contractItemId),
                                workProcess.id.eq(workProcessId),
                                workOrderDetail.deleteYn.isFalse()
                        )
                        .fetchOne()
        );
    }

    // 작업공정 구분으로 작업공정 id 가져옴
    @Override
    public Optional<Long> findWorkProcessIdByWorkProcessDivision(WorkProcessDivision workProcessDivision) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(workProcess.id)
                        .from(workProcess)
                        .where(
                                workProcess.workProcessDivision.eq(workProcessDivision),
                                workProcess.deleteYn.isFalse()
                        )
                        .fetchOne()
        );
    }

    // lotMaster id 로 PACKAGING 끝난 작업지시 가져옴
    @Override
    public Optional<String> findWorkOrderIdByLotMasterIdAndWorkProcessDivision(
            Long lotMasterId,
            WorkProcessDivision workProcessDivision
    ) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(lotLog.workOrderDetail.orderNo)
                        .from(lotLog)
                        .where(
                                lotLog.lotMaster.id.eq(lotMasterId),
                                lotLog.workProcess.workProcessDivision.eq(workProcessDivision),
                                lotLog.workOrderDetail.orderState.eq(COMPLETION),
                                lotLog.workOrderDetail.deleteYn.isFalse()
                        )
                        .fetchOne()
        );
    }

    // 검색조건: lotMasterId, workProcessId, return: LotLog
    @Override
    public Optional<LotLog> findByLotMasterIdAndWorkProcessId(Long lotMasterId, Long workProcessId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(lotLog)
                        .where(
                                lotLog.lotMaster.id.eq(lotMasterId),
                                isWorkProcessIdEq(workProcessId)
                        )
                        .fetchOne()
        );
    }

    // 검색조건: workOrderDetailId, 반환: LotLog
    @Override
    public Optional<LotLog> findByWorkOrderDetailId(Long workOrderDetailId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(lotLog)
                        .where(
                                lotLog.workOrderDetail.id.eq(workOrderDetailId)
                        )
                        .orderBy(lotLog.createdDate.desc())
//                        .limit(1)
                        .fetchOne()
        );
    }

    // 조건: lorMasterId, 작업공정, 오늘
    @Override
    public Optional<LotLog> findByLotMasterIdAndWorkProcessDivision(Long lotMasterId, WorkProcessDivision workProcessDivision, LocalDate now) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(lotLog)
                        .from(lotLog)
                        .where(
                                lotLog.lotMaster.id.eq(lotMasterId),
                                lotLog.workProcess.workProcessDivision.eq(workProcessDivision),
                                lotLog.lotMaster.createdDate.between(now.atStartOfDay(), LocalDateTime.of(now, LocalTime.MAX).withNano(0)),
                                lotMaster.deleteYn.isFalse()
                        )
                        .fetchOne()
        );
    }

    // 검색조건: lotMasterId, workProcessDivision, 반환: LotLog
    @Override
    public Optional<LotLog> findByLotMasterIdAndWorkProcessDivision(Long lotMasterId, WorkProcessDivision workProcessDivision) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(lotLog)
                        .where(
                                lotLog.lotMaster.id.eq(lotMasterId),
                                lotLog.lotMaster.workProcess.workProcessDivision.eq(workProcessDivision)
                        )
                        .fetchOne()
        );
    }

    private BooleanExpression isWorkProcessIdEq(Long workProcessId) {
        return workProcessId != null ? workProcess.id.eq(workProcessId) : null;
    }

    private BooleanExpression isWorkOrderDetailIdEq(Long workOrderDetailId) {
        return workOrderDetailId != null ? workOrderDetail.id.eq(workOrderDetailId) : null;
    }

    private BooleanExpression isLotMasterIdEq(Long lotMasterId) {
        return lotMasterId != null ? lotMaster.id.eq(lotMasterId) : null;
    }
}
