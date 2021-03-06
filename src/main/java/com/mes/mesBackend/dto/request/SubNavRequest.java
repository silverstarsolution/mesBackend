package com.mes.mesBackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.mes.mesBackend.exception.Message.NOT_NULL;

@Setter
@Getter
@Schema(description = "서브 네비게이션")
public class SubNavRequest {

    @Schema(description = "네비게이션 이름")
    @NotBlank(message = NOT_NULL)
    String name;

    @Schema(description = "유저레벨")
    @NotNull(message = NOT_NULL)
    int level;

    @Schema(description = "출력순번")
    @NotNull(message = NOT_NULL)
    int orders;

    @Schema(description = "경로주소")
    @NotBlank(message = NOT_NULL)
    String path;

    @Schema(description = "사용여부")
    @NotNull(message = NOT_NULL)
    boolean useYn;
}
