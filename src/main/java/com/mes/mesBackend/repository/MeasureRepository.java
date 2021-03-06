package com.mes.mesBackend.repository;

import com.mes.mesBackend.entity.Measure;
import com.mes.mesBackend.repository.custom.JpaCustomRepository;
import com.mes.mesBackend.repository.custom.MeasureRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasureRepository extends JpaCustomRepository<Measure, Long> , MeasureRepositoryCustom {

}