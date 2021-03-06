package com.mes.mesBackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static com.mes.mesBackend.exception.Message.*;

@Getter
@Setter
@Schema(description = "BOM 품목")
public class BomItemRequest {

    @Schema(description = "레벨")
    @Min(value = ONE_VALUE, message = NOT_ZERO)
    @NotNull(message = NOT_NULL)
    int level;

    @Schema(description = "품목 id")
    @Min(value = ONE_VALUE, message = NOT_ZERO)
    @NotNull(message = NOT_NULL)
    Long item;

    @Schema(description = "수량")
    @NotNull(message = NOT_NULL)
    float amount;

//    @Schema(description = "구매처(거래처 id)")
//    @Min(value = ONE_VALUE, message = NOT_ZERO)
//    Long toBuy;

    @Schema(description = "공정 id")
    @Min(value = ONE_VALUE, message = NOT_ZERO)
    @NotNull(message = NOT_NULL)
    Long workProcess;

    @Schema(description = "사용여부")
    @NotNull(message = NOT_NULL)
    boolean useYn;

    @Schema(description = "비고")
    String note;
}
