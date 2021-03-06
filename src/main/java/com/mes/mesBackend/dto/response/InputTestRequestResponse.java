package com.mes.mesBackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mes.mesBackend.entity.enumeration.InputTestDivision;
import com.mes.mesBackend.entity.enumeration.InspectionType;
import com.mes.mesBackend.entity.enumeration.TestType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.mes.mesBackend.entity.enumeration.InputTestDivision.*;
import static com.mes.mesBackend.helper.Constants.*;

@Getter
@Setter
@Schema(description = "14-1.검사의뢰 등록")
@JsonInclude(NON_NULL)
public class InputTestRequestResponse {
    @Schema(description = "고유아이디")
    Long id;

    @Schema(description = "LOT 고유아이디")
    Long lotId;

    @Schema(description = "LOT 번호")
    String lotNo;

    @Schema(description = "구매입고 입고번호")
    Long purchaseInputNo;   // purchaseInputId

    @Schema(description = "외주입고 입고번호")
    Long outsourcingInputNo;   // purchaseInputId

    @Schema(description = "품목 고유아이디")
    Long itemId;

    @Schema(description = "품번")
    String itemNo;

    @Schema(description = "품명")
    String itemName;

    @Schema(description = "제조사 품번")
    String itemManufacturerPartNo;

//    @Schema(description = "고객사 품번")
//    String itemClientPartNo;      1월 17일 팀장님이 필드 삭제하라고 하심

    @Schema(description = "제조사")
    String manufacturerName;

//    @Schema(description = "고객사")
//    String clientName;            1월 17일 팀장님이 필드 삭제하라고 하심

    @Schema(description = "창고")
    String warehouse;

    @Schema(description = "품목형태")
    String itemForm;        // 품목형태

//    @Schema(description = "검사방법")
//    String testProcess;

    @Schema(description = "검사방법")
    InspectionType inspectionType;  // inputTestRequest.inspectionType

    @Schema(description = "검사기준")
    String testCriteria;

    @Schema(description = "수입검사여부")
    Boolean inputTestYn;

    @Schema(description = "시험성적서")
    Boolean testReportYn;

    @Schema(description = "COC")
    Boolean coc;

    @Schema(description = "요청일시")
    @JsonFormat(pattern = YYYY_MM_DD_HH_MM, timezone = ASIA_SEOUL)
    LocalDateTime requestDate;

    @Schema(description = "요청유형")
    TestType testType;   // item.testType

    @Schema(description = "요청수량")
    int requestAmount;

    @Schema(description = "검사수량")
    int testAmount;

//    @Schema(description = "검사요청")
//    TestType testType;

    @Schema(description = "검사완료요청일")
    @JsonFormat(pattern = YYYY_MM_DD, timezone = ASIA_SEOUL)
    LocalDate testCompletionRequestDate;

//    @Schema(description = "지시번호")
//    String workOrderNo;

    public InputTestRequestResponse division(InputTestDivision inputTestDivision) {
        if (inputTestDivision.equals(PART)) {
            setOutsourcingInputNo(null);
            setTestCompletionRequestDate(null);
//            setWorkOrderNo(null);
        } else if (inputTestDivision.equals(OUT_SOURCING)){
            setPurchaseInputNo(null);
            setTestCompletionRequestDate(null);
//            setWorkOrderNo(null);
            setCoc(null);
            setTestReportYn(null);
            setInputTestYn(null);
        } else if (inputTestDivision.equals(PRODUCT)) {
            setOutsourcingInputNo(null);
            setPurchaseInputNo(null);
//            setTestProcess(null);
            setTestCriteria(null);
            setCoc(null);
            setTestReportYn(null);
            setInputTestYn(null);
        }
        return this;
    }
}
