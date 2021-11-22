package com.mes.mesBackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "설비")
public class EquipmentResponse {
    @Schema(description = "고유아이디")
    Long id;

    @Schema(description = "설비코드")
    String equipmentCode;

    @Schema(description = "설비명")
    String equipmentName;

    @Schema(description = "설비유형")
    String equipmentType;

    @Schema(description = "규격&모델")
    String model;

    @Schema(description = "구매처")
    ClientResponse.idAndName client;

    @Schema(description = "구매일자")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    LocalDateTime purchaseDate;

    @Schema(description = "구입금액")
    int purchaseAmount;

    @Schema(description = "생산업체명")
    String maker;

    @Schema(description = "시리얼번호")
    String serialNo;

    @Schema(description = "생산개시일자")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    LocalDateTime startDate;

    @Schema(description = "작업라인, 작업장, 작업공정")
    WorkLineResponse.workLineAndWorkCenterAndWorkProcess workLine;

    @Schema(description = "점검주기")
    int checkCycle;

    @Schema(description = "사용")
    boolean useYn = true;
}