package com.mes.mesBackend.repository.impl;


import com.mes.mesBackend.dto.response.OutsourcingProductionResponse;
import com.mes.mesBackend.entity.*;
import com.mes.mesBackend.repository.custom.OutsourcingRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class OutSourcingProductionRequestRepositoryImpl implements OutsourcingRepositoryCustom {
    // 검색조건: 외주처 명, 생산품목, 의뢰기간

    private final JPAQueryFactory jpaQueryFactory;
    final QOutSourcingProductionRequest request = QOutSourcingProductionRequest.outSourcingProductionRequest;
    final QBomMaster master = QBomMaster.bomMaster;
    final QItem item = QItem.item;
    final QClient client = QClient.client;

    public List<OutsourcingProductionResponse> findAllByCondition(Long clientId, Long itemId, LocalDate startDate, LocalDate endDate){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                OutsourcingProductionResponse.class,
                                client.clientName.as("clientName"),
                                request.id.as("id"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName"),
                                master.bomNo.as("bomNo"),
                                request.productionDate.as("productionDate"),
                                request.productionAmount.as("productionAmount"),
                                request.materialRequestDate.as("materialRequestDate"),
                                request.periodDate.as("periodDate"),
                                request.inputTestYn.as("inputTestYn"),
                                request.note.as("note")
                        )
                )
                .from(request)
                .leftJoin(master).on(master.id.eq(request.bomMaster.id))
                .leftJoin(item).on(item.id.eq(master.item.id))
                .leftJoin(client).on(client.id.eq(item.manufacturer.id))
                .where(
                        clientNull(clientId),
                        itemNull(itemId),
                        dateNull(startDate, endDate),
                        request.useYn.eq(true),
                        request.deleteYn.eq(false)
                )
                .fetch();
    }

    public Optional<OutsourcingProductionResponse> findRequestByIdAndDeleteYnAndUseYn(Long id){
        return Optional.ofNullable(
                jpaQueryFactory
                .select(
                        Projections.fields(
                            OutsourcingProductionResponse.class,
                            client.clientName.as("clientName"),
                            request.id.as("id"),
                            item.itemNo.as("itemNo"),
                            item.itemName.as("itemName"),
                            master.bomNo.as("bomNo"),
                            request.productionDate.as("productionDate"),
                            request.productionAmount.as("productionAmount"),
                            request.materialRequestDate.as("materialRequestDate"),
                            request.periodDate.as("periodDate"),
                            request.inputTestYn.as("inputTestYn"),
                            request.note.as("note")
                        )
                )
                .from(request)
                .leftJoin(master).on(master.id.eq(request.bomMaster.id))
                .leftJoin(item).on(item.id.eq(master.item.id))
                .leftJoin(client).on(client.id.eq(item.manufacturer.id))
                .where(
                        request.id.eq(id),
                        request.useYn.eq(true),
                        request.deleteYn.eq(false)
                )
                .fetchOne()
        );
    }

    private BooleanExpression clientNull(Long clientId){
//        return clientId != null ? request.bomMaster.item.manufacturer.id.eq(clientId) : null;
        return clientId != null ? client.id.eq(clientId) : null;
    }

    private BooleanExpression itemNull(Long itemId){
        return itemId != null ? item.id.eq(itemId) : null;
    }

    private  BooleanExpression dateNull(LocalDate startDate, LocalDate endDate){
        return startDate != null ? request.productionDate.between(startDate, endDate) : null;
    }
}