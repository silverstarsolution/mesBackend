package com.mes.mesBackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mes.mesBackend.entity.enumeration.ContractType;
import com.mes.mesBackend.entity.enumeration.InstructionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;


@Getter
@Setter
@Schema(description = "작업지시 제조오더 정보")
@JsonInclude(NON_NULL)
public class WorkOrderProduceOrderResponse {
    @Schema(description = "고유아이디")
    Long id;

    @Schema(description = "제조오더번호")
    String produceOrderNo;

    @Schema(description = "품번")
    String itemNo;

    @Schema(description = "모델명")
    String itemName;

    @Schema(description = "착수예정일")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    LocalDate expectedStartedDate;

    @Schema(description = "오더수량")
    int orderAmount;

    @Schema(description = "단위")
    String unitCodeName;

    @Schema(description = "지시상태")
    InstructionStatus instructionStatus = InstructionStatus.SCHEDULE;

    @Schema(description = "수주유형")
    ContractType contractType;

    @Schema(description = "수주처")
    String contractClient;

    @Schema(description = "수주번호")
    String contractNo;

    @Schema(description = "납기일자")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    LocalDate periodDate;

    @Schema(description = "비고")
    String note;
}
