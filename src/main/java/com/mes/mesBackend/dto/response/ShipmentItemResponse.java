package com.mes.mesBackend.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@Schema(description = "출하 품목")
@JsonInclude(NON_NULL)
public class ShipmentItemResponse {
    @Schema(description = "출하 품목정보 고유아이디")
    Long id;

    @Schema(description = "수주 고유아이디")
    Long contractId;

    @Schema(description = "수주번호")
    String contractNo;

    @Schema(description = "수주품목 고유아이디")
    Long contractItemId;

    @Schema(description = "품번")
    String itemNo;

    @Schema(description = "품명")
    String itemName;

    @Schema(description = "규격")
    String itemStandard;

    @Schema(description = "수주단위")
    String contractUnit;

    @Schema(description = "수주미출하수량")
    int notShippedAmount;           //  contractItem 의 수주수량 - lot에 등록된 재고수량

    @Schema(description = "출하수량")
    int shipmentAmount;

    @Schema(description = "출하금액")
    int shipmentPrice;               // 출하금액 = 출하수량 * 수주 품목 단가

    @Schema(description = "출하금액(원화)")
    int shipmentPriceWon;            // 출하금액(원화) = 출하수량 * 수주 품목 단가

    @Schema(description = "비고")
    String note;

    @JsonIgnore
    int contractItemAmount;     // 수주품목의 수주수량
    @JsonIgnore
    int itemInputUnitPrice;     // 품목단가

    public ShipmentItemResponse converter(int shipmentAmount) {
        setNotShippedAmount(contractItemAmount - shipmentAmount);
        setShipmentAmount(shipmentAmount);
        setShipmentPrice(shipmentAmount * itemInputUnitPrice);
        setShipmentPriceWon(shipmentAmount * itemInputUnitPrice);
        return this;
    }
}
