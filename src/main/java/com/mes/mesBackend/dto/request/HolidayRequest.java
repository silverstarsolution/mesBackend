package com.mes.mesBackend.dto.request;

import com.mes.mesBackend.entity.enumeration.HolidayType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static com.mes.mesBackend.exception.Message.NOT_NULL;

@Getter
@Setter
@Schema(description = "휴일 등록")
public class HolidayRequest {
    @NotNull(message = NOT_NULL)
    @Schema(description = "휴일일자")
    LocalDate date;

    @Schema(description = "요일")
    String day;

    @Schema(description = "휴일 유형 [EVERY_YEAR: 매년반복, NONE: 비반복, EVERY_WEEK: 주단위반복]")
    HolidayType type = HolidayType.EVERY_YEAR;

    @Schema(description = "비고")
    String note;

    @Schema(description = "사용여부")
    boolean useYn;
}
