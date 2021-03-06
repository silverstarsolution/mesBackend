package com.mes.mesBackend.dto.request;

import com.mes.mesBackend.entity.enumeration.DevelopStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.mes.mesBackend.entity.enumeration.DevelopStatus.BEFORE;
import static com.mes.mesBackend.exception.Message.*;
import static com.mes.mesBackend.helper.Constants.YYYY_MM_DD;

@Getter
@Setter
@Schema(description = "BOM 마스터")
public class BomMasterRequest {

    @Schema(description = "품목 id")
    @Min(value = ONE_VALUE, message = NOT_ZERO)
    @NotNull(message = NOT_NULL)
    Long item;

    @Schema(description = "BOM 번호")
    @Min(value = ONE_VALUE, message = NOT_ZERO)
    @NotNull(message = NOT_NULL)
    int bomNo;

    @Schema(description = "유효시작일")
    @NotNull(message = NOT_NULL)
    @DateTimeFormat(pattern = YYYY_MM_DD)
    LocalDate startDate;

    @Schema(description = "유효종료일")
    @NotNull(message = NOT_NULL)
    @DateTimeFormat(pattern = YYYY_MM_DD)
    LocalDate endDate;

    @Schema(description = "개발상태 [BEFORE : 미개발, PROCEEDING : 개발중, COMPLETION : 개발완료]")
    @NotNull(message = NOT_NULL)
    DevelopStatus developStatus = BEFORE;

    @Schema(description = "비고")
    String note;

    @Schema(description = "승인일시")
    @DateTimeFormat(pattern = YYYY_MM_DD)
    LocalDate approvalDate;

    @Schema(description = "사용")
    @NotNull(message = NOT_NULL)
    Boolean useYn;

    @Schema(description = "작업공정 고유아이디")
    @NotNull(message = NOT_NULL)
    @Min(value = ONE_VALUE, message = NOT_ZERO)
    Long workProcessId;
}
