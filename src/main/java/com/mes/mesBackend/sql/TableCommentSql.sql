alter table BAD_ITEMS comment = '불량항목 정보';
alter table BAD_ITEM_DETAILS comment = '불량항목 세부 정보';
alter table BOM_ITEM_DETAILS comment = 'BOM 품목 세부 정보';
alter table BOM_MASTERS comment = 'BOM 마스터 정보';
alter table BUSINESS_TYPES comment = '업태';
alter table CHECK_TYPES comment = '점검 유형';
alter table CLIENTS comment = '거래처정보';
alter table CLIENT_TYPES comment = '거래처 타입';
alter table CODE_MASTERS comment = '주코드 정보';
alter table CONTRACTS comment = '수주정보';
alter table CONTRACT_ITEMS comment = '수주 품목 정보';
alter table COUNTRY_CODES comment = '국가코드';
alter table CURRENCIES comment = '화폐';
alter table DEPARTMENTS comment = '부서';
alter table DETAIL_NAVS comment = '디테일 네비게이션';
alter table DEVELOPMENTS comment = '개발정보';
alter table DEVELOPMENT_FILES comment = '개발정보 파일';
alter table DEVELOPMENT_FILE_TYPES comment = '개발정보 파일타입';
alter table DEVELOPMENT_STATES comment = '개발정보 진행상태';
alter table USERS comment = '직원(작업자)';
alter table EQUIPMENTS comment = '설비 정보';
alter table ESTIMATES comment = '견적정보';
alter table ESTIMATE_ITEM_DETAILS comment = '품목견적서 세부 정보';
alter table FACTORIES comment = '공장정보';
alter table GAUGE_TYPES comment = 'GAUGE 유형';
alter table HEADERS comment = '헤더';
alter table HOLIDAYS comment = '휴일';
alter table INSTRUCTION_STATUSES comment = '지시상태';
alter table INVOICES comment = 'Invoice';
alter table ITEMS comment = '품목정보';
alter table ITEM_ACCOUNTS comment = '품목계정';
alter table ITEM_CHECK_DETAILS comment = '품목별 검사항목 정보 세부 정보';
alter table ITEM_FILES comment = '품목정보 파일';
alter table ITEM_FORMS comment = '품목형태';
alter table ITEM_GROUPS comment = '품목 그룹 정보';
alter table ITEM_INVENTORY_AMOUNTS comment = '창고별 품목 재고';
alter table LOT_TYPES comment = 'Lot 유형';
alter table MAIN_NAVS comment = '메인 네비게이션';
alter table MANAGERS comment = '담당자';
alter table MEASURES comment = '계측기 정보';
alter table OUT_SOURCING_INPUT comment = '외주입고 정보';
alter table OUT_SOURCING_PRODUCTION_MATERIAL_OUTPUT_INFO comment = '외주 생산 원재료 출고 대상 정보';
alter table OUT_SOURCING_PRODUCTION_REQUESTS comment = '외주 생산 의뢰 정보';
alter table PI comment = 'P/I';
alter table PRODUCE_ORDERS comment = '제조오더 정보';
alter table PRODUCE_ORDER_DETAIL comment = '제조오더 세부내역';
alter table ROUTINGS comment = '라우팅 정보';
alter table ROUTING_DETAILS comment = '라우팅 상세 정보';
alter table SHIPMENTS comment = '출하정보';
alter table SHIPMENT_ITEMS comment = '출하정보 품목정보';
alter table SHIPMENT_RETURNS comment = '출하반품';
alter table STOCK_INSPECT_INFOS comment = '재고조사 정보';
alter table STOCK_INSPECT_REQUESTS comment = '재고실사 의뢰 정보';
alter table STOCK_INSPECT_REQUEST_DETAILS comment = '재고조사 의뢰 상세 정보';
alter table SUB_CODE_MASTERS comment = '부코드 정보';
alter table SUB_ITEMS comment = '대체품목 정보';
alter table SUB_NAVS comment = '서브 네비게이션';
alter table TEST_CRITERIA comment = '검사기준';
alter table TEST_PROCESSES comment = '검사방법';
alter table TEST_TYPES comment = '검사타입';
alter table UNITS comment = '단위 정보';
alter table USE_TYPES comment = '용도 유형';
alter table WARE_HOUSES comment = '창고 정보';
alter table WARE_HOUSE_TYPES comment = '창고유형';
alter table WORK_CENTERS comment = '작업장 정보';
alter table WORK_CENTER_CHECKS comment = '작업장별 점검항목 정보';
alter table WORK_CENTER_CHECK_DETAILS comment = '작업장별 세부 점검항목 디테일 정보';
alter table WORK_DOCUMENTS comment = '작업표준서 정보';
alter table WORK_LINES comment = '작업라인 정보';
alter table WORK_ORDERS comment = '작업지시 정보';
alter table WORK_ORDER_DETAILS comment = '작업지시 정보';
alter table WORK_PLACES comment = '사업장';
alter table WORK_PLACE_BUSINESS_TYPES comment = '사업장,업태 매핑 테이블';
alter table WORK_PROCESSES comment = '작업공정 정보';
alter table INPUT_TEST_REQUESTS comment = '부품수입검사 정보';
alter table PURCHASE_CURRENTS comment = '구매현황';
alter table PURCHASE_ORDERS comment = '구매발주 정보';
alter table PURCHASE_ORDER_DETAILS comment = '구매발주 세부정보';
alter table INCONGRUITIES comment = '부적합 정보';
alter table PRODUCT_TESTS comment = '검사의뢰 정보';
alter table PRODUCT_TEST_DETAILS comment = '제품검사 상세정보';
alter table EQUIPMENT_BREAKDOWNS comment = '설비고장수리 내역 정보';
alter table EQUIPMENT_BREAKDOWN_DETAILS comment = '수리항목,부품,작업자 정보';
alter table EQUIPMENT_CHECKS comment = '설비 점검 실적 정보';
alter table EQUIPMENT_CHECK_DETAILS comment ='설비 점검 세부 항목';
alter table MEASURE_CALIBRATIONS comment ='계측기 검교정 실적 정보';
alter table LOT_MASTERS comment = 'Lot 마스터 정보';

alter table PURCHASE_RETURNS comment = '구매입고 반품등록';
alter table PURCHASE_INPUTS comment = '구매입고 등록';
alter table PURCHASE_REQUESTS comment = '구매요청 등록';
alter table KITTINGS comment = 'kitting 등록';
alter table PRODUCT_RESULTS comment = '생산실적 관리';
alter table END_TIMES comment = '마감일자';

alter table GRID_OPTIONS comment = '그리드 옵션';
alter table ITEM_CHECKS comment = '품목별 검사항목';
alter table ITEM_GROUP_CODES comment = '품목 그룹코드';
alter table WORK_CENTER_CODES comment = '작업장 코드';
alter table WORK_LINE_CODES comment = '작업라인 코드';
alter table WORK_PROCESS_CODES comment = '작업공정 코드';



# 테이블명,테이블정보,column정보,column이름,type,null 정보 조회 쿼리
select t.table_name as "테이블 명",
       t.table_comment as "테이블 정보",
       c.column_comment as "Column 정보",
       c.column_name as "Column 이름",
       c.column_type as "Type",
       c.is_nullable as "Null",
       REPLACE(REPLACE(REPLACE(c.column_key,'PRI','PK'),'MUL','FK'),'UNI',' ') as "Column_Key"
  from information_schema.columns c
  join information_schema.tables t ON t.table_name = c.table_name
 where t.table_schema = 'mes-dev'
 order by c.table_name asc;

# 테이블명,테이블정보,end_point 조회 쿼리
select
       t.TABLE_NAME as "테이블 명",
       t.TABLE_COMMENT as "테이블 정보",
       REPLACE(CONCAT('/',LOWER(t.TABLE_NAME)),'_','-') as "end point"
from
     information_schema.tables t
where
      t.table_schema = 'mes-dev';