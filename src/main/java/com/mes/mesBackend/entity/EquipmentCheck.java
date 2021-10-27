package com.mes.mesBackend.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/*
 * 17-1. 설비 점검 실적 등록
 * 검색: 공장,설비유형,점검유형,작업기간
 * 등록일자
 * 설비
 * 설비명
 * 작업장
 * 작업공정
 * 작업라인
 * 세부:점검항목,점검내용,판정기준,판정방법,상한값,하한값,결과값,등록유형
 * */
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "EQUIPMENT_CHECKS")
@Data
public class EquipmentCheck extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", columnDefinition = "bigint COMMENT '설비점검 고유아이디'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EQUIPMENT", columnDefinition = "bigint COMMENT '설비 정보'")
    private Equipment equipment;        // 설비 정보

    @Column(name = "REGISTER_DATE", columnDefinition = "date COMMENT '등록일자'")
    private LocalDate registerDate;     // 등록일자
}