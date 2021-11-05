package com.mes.mesBackend.repository;

import com.mes.mesBackend.entity.ItemGroupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemGroupCodeRepository extends JpaRepository<ItemGroupCode, Long> {
}