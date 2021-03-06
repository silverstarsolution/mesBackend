package com.mes.mesBackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mes.mesBackend.entity.Item;
import com.mes.mesBackend.entity.enumeration.OrderState;
import com.mes.mesBackend.entity.enumeration.WorkProcessDivision;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.mes.mesBackend.helper.Constants.ASIA_SEOUL;
import static com.mes.mesBackend.helper.Constants.YYYY_MM_DD;

@Getter
@Setter
@Schema(description = "작지상태")
@JsonInclude(NON_NULL)
public class WorkOrderStateResponse {
    @Schema(description = "고유아이디")
    Long id;

    @Schema(description = "작지번호")
    String orderNo;

    @Schema(description = "작업공정")
    String workProcess;

    @Schema(description = "작업라인")
    String workLine;

    @Schema(description = "지시상태")
    OrderState orderState;

    @Schema(description = "품번")
    String itemNo;

    @Schema(description = "품명")
    String itemName;

    @Schema(description = "품목계정")
    String itemAccount;

    @Schema(description = "납기일자")
    @JsonFormat(pattern = YYYY_MM_DD, timezone = ASIA_SEOUL)
    LocalDate periodDate;

    @Schema(description = "지시수량")
    int orderAmount;

    @Schema(description = "생산수량")
    int productionAmount;

    @Schema(description = "수주번호")
    String contractNo;

    @JsonIgnore
    WorkProcessDivision workProcessDivision;

    @JsonIgnore
    Long itemId;

    @JsonIgnore
    Long workProcessId;

//    @JsonIgnore
    Long itemAccountId;

    public WorkOrderStateResponse setItems(Item item) {
        setItemNo(item.getItemNo());
        setItemName(item.getItemName());
        setItemAccount(item.getItemAccount().getAccount());
        setItemAccountId(item.getItemAccount().getId());
        return this;
    }
}
