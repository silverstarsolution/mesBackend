package com.mes.mesBackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PUBLIC;

/*
* 9-5. 구매입고 등록
* */
@AllArgsConstructor
@NoArgsConstructor(access = PUBLIC)
@Entity(name = "PURCHASE_INPUTS")
@Data
public class PurchaseInput extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "ID", columnDefinition = "bigint COMMENT '구매입고 고유아이디'")
    private Long id;

    // 구매요청 테이블
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PURCHASE_REQUEST", columnDefinition = "bigint COMMENT '구매요청'")
    private PurchaseRequest purchaseRequest;

    // 입고수량
    @Column(name = "INPUT_AMOUNT", columnDefinition = "int COMMENT '입고수량'")
    private int inputAmount;

    // 제조일자
    @Column(name = "MANUFACTURE_DATE", columnDefinition = "datetime COMMENT '제조일자'")
    private LocalDate manufactureDate;

    // 유효일자
    @Column(name = "VALID_DATE", columnDefinition = "datetime COMMENT '유효일자'")
    private LocalDate validDate;

    // 긴급여부
    @Column(name = "INPUT_TEST_YN", columnDefinition = "bit(1) COMMENT '수입검사여부'")
    private boolean inputTestYn = false;

    // 시험성적서
    @Column(name = "TEST_REPORT_YN", columnDefinition = "bit(1) COMMENT '시험성적서'")
    private boolean testReportYn = false;

    // COC
    @Column(name = "COC", columnDefinition = "bit(1) COMMENT 'COC'")
    private boolean coc = false;

    // 삭제여부
    @Column(name = "DELETE_YN", columnDefinition = "bit(1) COMMENT '삭제여부'", nullable = false)
    private boolean deleteYn = false;

    @Column(name = "CLIENT_LOT_NO", columnDefinition = "varchar(255) COMMENT '거래처 LOT 번호'")
    private String clientLotNo;

    public void put(PurchaseInput newPurchaseInput) {
        setInputAmount(newPurchaseInput.inputAmount);
        setValidDate(newPurchaseInput.validDate);
        setManufactureDate(newPurchaseInput.manufactureDate);
        setInputTestYn(newPurchaseInput.inputTestYn);
        setTestReportYn(newPurchaseInput.testReportYn);
        setCoc(newPurchaseInput.coc);
        setClientLotNo(newPurchaseInput.clientLotNo);
    }

    public void delete() {
        setDeleteYn(true);
    }
}
