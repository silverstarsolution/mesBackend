package com.mes.mesBackend.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/*
 * 작업지시 정보
 * 지시번호 (2107080001-0004)
 * 작업공정 (조립)                        -> WorkProcess
 * 작업라인 (외주라인(리버텍))               -> WorkLine
 * 지시수량 (20)
 * 생산담당자 ()                          -> Manager
 * 단위 (개)                             -> Unit
 * 준비시간(분) (0)
 * UPH (1)
 * 소요시간(분) (1200)
 * 작업예정일 (2021.7.19)
 * 예정시간 (09:00)
 * 지시상태 (완료)
 * 검사의뢰 (자동검사)                      -> TestType
 * 검사유형 (출하검사)                      -> TestProcess
 * 최종공정 (아니오)
 * 생산수량 (아니오)
 * 투입인원 (1)
 * 비고 ()
 * */
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "WORK_ORDER_INFOS")
@Data
public class WorkOrderInfo extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) @Column(name = "ID")
    private Long id;

    @Column(name = "ORDER_NO", nullable = false, unique = true)
    private String orderNo;             // 지시번호

    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "WORK_PROCESS", nullable = false)
    private WorkProcess workProcess;        // 작업공정

    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "WORK_LINE", nullable = false)
    private WorkLine workLine;              // 작업라인

    @Column(name = "ORDER_AMOUNT", nullable = false)
    private int orderAmount;                // 지시수량

    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "MANAGER")
    private Manager manager;                // 생산담당자

    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "UNIT", nullable = false)
    private Unit unit;                      // 단위

    @Column(name = "READY_TIME")
    private Long readyTime;                 // 준비시간

    @Column(name = "UHP")
    private int uph;                        // UPH

    @Column(name = "COST_TIME", nullable = false)
    private Long costTime;                  // 소요시간

    @Column(name = "EXPECTED_DATE", nullable = false)
    private LocalDate expectedDate;         // 작업예정일

    @Column(name = "EXPECTED_TIME", nullable = false)
    private Long expectedTime;              // 예정시간

    @Column(name = "ORDER_STATE",nullable = false)
    private String orderState;              // 지시상태

    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "TEST_TYPE", nullable = false)
    private TestType testType;              // 검사의뢰

    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "TEST_PROCESS")
    private TestProcess testProcess;        // 검사유형

    @Column(name = "LAST_PROCESS_YN", nullable = false)
    private boolean lastProcessYn;          // 최종공정

    @Column(name = "PRODUCTION_AMOUNT")
    private int productionAmount;           // 생산수량

    @Column(name = "INPUT_PEOPLE")
    private int inputPeople;                // 투입인원

    @Column(name = "NOTE")
    private String note;                    // 비고

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "WORK_ORDER")
    private WorkOrder workOrder;            // 작업지시 제조오더
}
