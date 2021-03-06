package com.mes.mesBackend.repository;

import com.mes.mesBackend.entity.WareHouse;
import com.mes.mesBackend.repository.custom.JpaCustomRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WareHouseRepository extends JpaCustomRepository<WareHouse, Long> {
    Optional<WareHouse> findByWorkProcessYnIsTrueAndDeleteYnFalse();
    boolean existsByWorkProcessYnTrueAndDeleteYnFalse();
}