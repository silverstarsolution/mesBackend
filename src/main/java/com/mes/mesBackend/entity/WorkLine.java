package com.mes.mesBackend.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
/*
 * 작업라인 등록
 * 검색조건 : 공장
 * 라인코드(01),
 * 작업라인명(조립라인,몰딩라인,외주라인),
 * 작업장(1작업장(본사),외주작업장(리버텍)) -> 작업장 참조(WORK_CENTERS),
 * 원자재창고(kitting창고(본사)) -> WARE_HOUSE 참조 / 일단 빼고 작업,
 * 작업공정(조립,몰딩,조립) -> WORK_PROCESS,
 * pop시작 FORMID(POP1003),
 * 외주사(리버텍,신성테크) -> 작업장 참조(WORK_CENTERS) / 일단 빼고 작업,
 * 일 가동시간(8,8), 사용
 */
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "WORK_LINES")
@Data
public class WorkLine extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", columnDefinition = "bigint COMMENT '작업라인 등록 고유아이디'")
    private Long id;

    @Column(name = "WORK_LINE_NAME", nullable = false, columnDefinition = "varchar(255) COMMENT '작업라인명'")
    private String workLineName;    // 작업라인명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORK_CENTER", columnDefinition = "bigint COMMENT '작업장'")
    private WorkCenter workCenter;  // 작업장

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "WARE_HOUSE", columnDefinition = "bigint COMMENT '원자재 창고'")
//    private WareHouse wareHouse;        // 원자재 창고

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORK_PROCESS", columnDefinition = "bigint COMMENT '작업공정'")
    private WorkProcess workProcess;        // 작업공정

    @Column(name = "POP_START_FORMID", columnDefinition = "varchar(255) COMMENT 'POP 시작 FORMID'")
    private String popStartFormid;      // POP 시작 FORMID

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "CLIENT", columnDefinition = "bigint COMMENT '외주사'")
//    private Client client;              // 외주사

    @Column(name = "TIME", nullable = false, columnDefinition = "int COMMENT '일 가동시간'")
    private int time;       // 일 가동시간

    @Column(name = "USE_YN", columnDefinition = "bit(1) COMMENT '사용여부'")
    private boolean useYn = true;   // 사용여부

    @Column(name = "DELETE_YN", columnDefinition = "bit(1) COMMENT '삭제여부'")
    private boolean deleteYn = false;  // 삭제여부


    public void addMapping(
            WorkCenter workCenter,
            WorkProcess workProcess
    ) {
        setWorkCenter(workCenter);
        setWorkProcess(workProcess);
    }

    public void put(
            WorkLine newWorkLine,
            WorkCenter newWorkCenter,
            WorkProcess newWorkProcess
    ) {
        addMapping(newWorkCenter,newWorkProcess);
        setWorkLineName(newWorkLine.workLineName);
        setPopStartFormid(newWorkLine.popStartFormid);
        setTime(newWorkLine.time);
        setUseYn(newWorkLine.useYn);
    }

    public void delete() {
        setDeleteYn(true);
    }
}
