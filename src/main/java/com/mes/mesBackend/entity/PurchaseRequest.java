package com.mes.mesBackend.entity;

import com.mes.mesBackend.entity.enumeration.OrderState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

import static com.mes.mesBackend.entity.enumeration.OrderState.COMPLETION;
import static com.mes.mesBackend.entity.enumeration.OrderState.SCHEDULE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

/*
 * 9-1. 구매요청 등록
 * 9-2. 구매발주 등록 디테일 정보
 * */
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Entity(name = "PURCHASE_REQUESTS")
@Data
public class PurchaseRequest extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "ID", columnDefinition = "bigint COMMENT '구매요청 고유아이디'")
    private Long id;

    // 제조오더번호
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PRODUCE_ORDER", columnDefinition = "bigint COMMENT '제조오더번호'", nullable = false)
    private ProduceOrder produceOrder;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "ITEM", columnDefinition = "bigint COMMENT '품목'", nullable = false)
    private Item item;

    // 요청일자
    @Column(name = "REQUEST_DATE", columnDefinition = "datetime COMMENT '요청일자'", nullable = false)
    private LocalDate requestDate;

    // 요청수량
    @Column(name = "REQUEST_AMOUNT", columnDefinition = "int COMMENT '요청수량'", nullable = false)
    private int requestAmount;

    // 발주수량
    // default value 0
    @Column(name = "ORDER_AMOUNT", columnDefinition = "int COMMENT '발주수량'", nullable = false)
    private int orderAmount;

    // 구매납기일자
    @Column(name = "PURCHASE_PERIOD_DATE", columnDefinition = "datetime COMMENT '구매납기일자'", nullable = false)
    private LocalDate purchasePeriodDate;

    // 비고
    @Column(name = "NOTE", columnDefinition = "varchar(255) COMMENT '비고'")
    private String note;

    // 삭제여부
    @Column(name = "DELETE_YN", columnDefinition = "bit(1) COMMENT '삭제여부'", nullable = false)
    private boolean deleteYn = false;

    // 취소수량
    @Column(name = "CANCEL_AMOUNT", columnDefinition = "int COMMENT '취소수량'")
    private int cancelAmount;

    // 지시상태
    @Enumerated(STRING)
    @Column(name = "ORDER_STATE", columnDefinition = "varchar(255) COMMENT '지시상태'")
    private OrderState ordersState = OrderState.SCHEDULE;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PURCHASE_ORDER", columnDefinition = "bigint COMMENT '구매발주'")
    private PurchaseOrder purchaseOrder;

    // 9-5. 입고일시
    @Column(name = "INPUT_DATE", columnDefinition = "datetime COMMENT '입고일시'")
    private LocalDate inputDate;    // 구매입고의 가장 최근 createdDate

    @Column(name = "STOCK_UNIT_REQUEST_AMOUNT", columnDefinition = "int COMMENT '재고단위요청수량'")
    private int stockUnitRequestAmount;

    @Column(name = "STOCK_UNIT_ORDER_AMOUNT", columnDefinition = "int COMMENT '재고단위발주수량'")
    private int stockUnitOrderAmount;

    @Column(name = "INPUT_TEST_YN", columnDefinition = "bit(1) COMMENT '수입검사여부'")
    private boolean inputTestYn;

    public void update(
            PurchaseRequest newPurchaseRequest,
            ProduceOrder newProduceOrder,
            Item newItem
    ) {
        setProduceOrder(newProduceOrder);
        setItem(newItem);
        setRequestDate(newPurchaseRequest.requestDate);
        setRequestAmount(newPurchaseRequest.requestAmount);
        setPurchasePeriodDate(newPurchaseRequest.purchasePeriodDate);
        setNote(newPurchaseRequest.note);
        setStockUnitRequestAmount(newPurchaseRequest.stockUnitRequestAmount);
        setStockUnitOrderAmount(newPurchaseRequest.stockUnitOrderAmount);
        setInputTestYn(newPurchaseRequest.inputTestYn);
        if (purchaseOrder != null) {
            setOrderAmount(requestAmount);
        }
    }

    public void mapping(
            ProduceOrder produceOrder,
            Item item
    ) {
        setProduceOrder(produceOrder);
        setItem(item);
    }

    public void delete() {
        setDeleteYn(true);
    }

    public void deleteFromPurchaseOrderAndOrderStateChangedSchedule() {
        setPurchaseOrder(null);
//        orderState 변경 ONGOING -> SCHEDULE
        setOrdersState(SCHEDULE);
    }

    public void putPurchaseOrderAndOrderStateChangedOngoing(PurchaseOrder purchaseOrder) {
        setPurchaseOrder(purchaseOrder);
        setOrdersState(SCHEDULE);
    }

    public void putOrderStateChangedCompletion() {
        setOrdersState(COMPLETION);
    }
}
