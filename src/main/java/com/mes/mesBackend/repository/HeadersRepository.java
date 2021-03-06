package com.mes.mesBackend.repository;

import com.mes.mesBackend.entity.Header;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeadersRepository extends JpaRepository<Header, Long> {
    List<Header> findAllByControllerNameOrderBySeq(String ControllerName);
}