package com.mes.mesBackend.entity.enumeration;

public enum DevelopmentStatusType {
    /*
     * 개발 품목 등록 진행 메인 프로세스
     * ORDER
     * PLAN
     * DESIGN
     * CHECK
     * ACTION
     * COMPLETE_REPORT: 완료보고
     * ETC: 기타
     * [COMPLETE_REPORT: 완료보고, ETC: 기타]
     */

    ORDER, PLAN, DESIGN, CHECK, ACTION, COMPLETE_REPORT, ETC
}
