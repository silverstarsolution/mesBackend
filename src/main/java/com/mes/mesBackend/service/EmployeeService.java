package com.mes.mesBackend.service;

import com.mes.mesBackend.dto.request.EmployeeRequest;
import com.mes.mesBackend.dto.response.EmployeeResponse;
import com.mes.mesBackend.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    // 직원(작업자) 생성
    EmployeeResponse createEmployee(EmployeeRequest employeeRequest) throws NotFoundException;

    // 직원(작업자) 단일 조회
    EmployeeResponse getEmployee(Long id) throws NotFoundException;

    // 직원(작업자) 페이징 조회
    Page<EmployeeResponse> getEmployees(Pageable pageable);

    // 직원(작업자) 수정
    EmployeeResponse updateEmployee(Long id, EmployeeRequest employeeRequest) throws NotFoundException;

    // 직원(작업자) 삭제
    void deleteEmployee(Long id) throws NotFoundException;
}
