package com.mes.mesBackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mes.mesBackend.entity.enumeration.ProductionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.mes.mesBackend.helper.Constants.ASIA_SEOUL;
import static com.mes.mesBackend.helper.Constants.YYYY_MM_DD;

@Getter
@Setter
@Schema(description = "수주")
@JsonInclude(NON_NULL)
public class ContractResponse {
    @Schema(description = "고유아이디")
    Long id;

    @Schema(description = "수주번호")
    String contractNo;

    @Schema(description = "거래처")
    ClientResponse.CodeAndName client;

    @Schema(description = "수주일자")
    @JsonFormat(pattern = YYYY_MM_DD, timezone = ASIA_SEOUL)
    LocalDate contractDate;

    @Schema(description = "고객발주일자")
    @JsonFormat(pattern = YYYY_MM_DD, timezone = ASIA_SEOUL)
    LocalDate clientOrderDate;

    @Schema(description = "생산유형 [MASS: 양산, SAMPLE: 샘플]")
    ProductionType productionType;

    @Schema(description = "고객발주번호")
    String clientOrderNo;

    @Schema(description = "담당자")
    UserResponse.idAndKorName user;

    @Schema(description = "화폐")
    CurrencyResponse.idAndUnit currency;

    @Schema(description = "부가세적용")
    boolean surtax;

    @Schema(description = "출고창고")
    WareHouseResponse.idAndName outputWareHouse;

    @Schema(description = "납기일자")
    @JsonFormat(pattern = YYYY_MM_DD, timezone = ASIA_SEOUL)
    LocalDate periodDate;

    @Schema(description = "변경사유")
    String changeReason;

    @Schema(description = "결제완료")
    boolean paymentYn;

    @Schema(description = "결제조건")
    PayTypeResponse payType;

    @Schema(description = "Forwader")
    String forwader;

    @Schema(description = "운송조건")
    String transportCondition;

    @Schema(description = "Shipment Service")
    String shipmentService;

    @Schema(description = "Shipment WK")
    String shipmentWk;

    @Schema(description = "비고")
    String note;

    @Schema(description = "마감일자")
    @JsonFormat(pattern = YYYY_MM_DD, timezone = ASIA_SEOUL)
    LocalDate deadlineDate;

    @Schema(description = "마감 여부")
    Boolean isPeriod;

    public ContractResponse setIsPeriod() {
        if (deadlineDate != null) {
            LocalDate now = LocalDate.now();
            setIsPeriod(deadlineDate.isEqual(now) || deadlineDate.isBefore(now));
        } else {
            setIsPeriod(null);
        }
        return this;
    }

    @Getter
    @Setter
    @Schema(description = "수주")
    public static class idAndClientOrderNo {
        @Schema(description = "고유아이디")
        Long id;

        @Schema(description = "고객발주번호")
        String clientOrderNo;
    }

    @Getter
    @Setter
    @Schema(description = "수주")
    public static class toProduceOrder {
        @Schema(description = "고유아이디")
        Long id;

        @Schema(description = "수주번호")
        String contractNo;

        @Schema(description = "거래처")
        String cName;

        @Schema(description = "납기일자")
        @JsonFormat(pattern = YYYY_MM_DD, timezone = ASIA_SEOUL)
        LocalDate periodDate;
    }
}
