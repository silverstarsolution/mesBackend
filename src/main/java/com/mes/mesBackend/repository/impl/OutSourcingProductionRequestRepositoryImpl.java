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

    public List<OutsourcingProductionResponse> findAllByCondition(Long clientId, String itemNoAndItemName, LocalDate startDate, LocalDate endDate){
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                OutsourcingProductionResponse.class,
                                request.id.as("id"),
                                client.clientName.as("clientName"),
                                item.id.as("itemId"),
                                item.itemNo.as("itemNo"),
                                item.itemName.as("itemName"),
                                request.productionDate.as("productionDate"),
                                request.productionAmount.as("productionAmount"),
                                request.materialRequestDate.as("materialRequestDate"),
                                request.periodDate.as("periodDate"),
                                request.inputTestYn.as("inputTestYn"),
                                request.note.as("note")
                        )
                )
                .from(request)
                .leftJoin(item).on(item.id.eq(request.item.id))
                .leftJoin(client).on(client.id.eq(item.manufacturer.id))
                .where(
                        clientNull(clientId),
                        isItemNoAndItemNameContain(itemNoAndItemName),
                        dateNull(startDate, endDate),
                        request.deleteYn.eq(false)
                )
                .orderBy(request.createdDate.desc())
                .fetch();
    }

    public Optional<OutsourcingProductionResponse> findRequestByIdAndDeleteYnAndUseYn(Long id){
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(
                                Projections.fields(
                                        OutsourcingProductionResponse.class,
                                        request.id.as("id"),
                                        client.clientName.as("clientName"),
                                        item.id.as("itemId"),
                                        item.itemNo.as("itemNo"),
                                        item.itemName.as("itemName"),
                                        request.productionDate.as("productionDate"),
                                        request.productionAmount.as("productionAmount"),
                                        request.materialRequestDate.as("materialRequestDate"),
                                        request.periodDate.as("periodDate"),
                                        request.inputTestYn.as("inputTestYn"),
                                        request.note.as("note")
                                )
                        )
                        .from(request)
                        .leftJoin(item).on(item.id.eq(request.item.id))
                        .leftJoin(client).on(client.id.eq(item.manufacturer.id))
                        .where(
                                request.id.eq(id),
                                request.deleteYn.eq(false)
                        )
                        .fetchOne()
        );
    }

    // 해당 품목이 외주생산의뢰에 등록되어 있는지
    @Override
    public boolean existsItemByOutsourcingRequest(Long itemId) {
        Integer fetchOne = jpaQueryFactory
                .selectOne()
                .from(request)
                .where(
                        request.deleteYn.isFalse(),
                        request.item.id.eq(itemId)
                )
                .fetchFirst();
        return fetchOne != null;
    }

    private BooleanExpression clientNull(Long clientId){
//        return clientId != null ? request.bomMaster.item.manufacturer.id.eq(clientId) : null;
        return clientId != null ? client.id.eq(clientId) : null;
    }

    private BooleanExpression itemNull(Long itemId){
        return itemId != null ? item.id.eq(itemId) : null;
    }

    private BooleanExpression isItemNoAndItemNameContain(String itemNoAndName) {
        return itemNoAndName != null ? item.itemNo.contains(itemNoAndName).or(item.itemName.contains(itemNoAndName)) : null;
    }

    private  BooleanExpression dateNull(LocalDate startDate, LocalDate endDate){
        if (startDate != null && endDate != null) {
            return request.productionDate.between(startDate, endDate);
        } else if (startDate != null) {
            return request.productionDate.after(startDate).or(request.productionDate.eq(startDate));
        } else if (endDate != null) {
            return request.productionDate.before(endDate).or(request.productionDate.eq(endDate));
        } else {
            return null;
        }
    }
}
