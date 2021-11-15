package com.mes.mesBackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "점검유형")
public class CheckTypeResponse {
    @Schema(description = "고유아이디")
    Long id;

    @Schema(description = "점검유형")
    String checkType;
}
