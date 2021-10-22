package com.mes.mesBackend.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "ITEM_INVENTORY_AMOUNTS")
@Data
public class ItemInventoryAmount extends BaseTimeEntity {
    /*
    * 창고별 품목 재고
    * 품목
    * 창고
    * 개수
    * 로트번호 --> 미구현
    * */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", columnDefinition = "bigint COMMENT '창고별 품목 재고 고유아이디'")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM", columnDefinition = "bigint COMMENT '품목정보'")
    private Item item;                  // 품목

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WARE_HOUSE", columnDefinition = "bigint COMMENT '창고정보'")
    private WareHouse wareHouse;        // 창고

    @Column(name = "AMOUNT", columnDefinition = "bigint COMMENT '창고별 품목 개수'")
    private int amount;                 // 개수

    @Column(name = "LOT_NO", columnDefinition = "bigint COMMENT 'LOT번호'")
    private String lotNo;               // lot번호
}
