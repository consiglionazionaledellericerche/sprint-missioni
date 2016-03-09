package it.cnr.si.missioni.repository;

import javax.persistence.LockModeType;

import it.cnr.si.missioni.domain.custom.persistence.DatiIstituto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for the AutoPropria entity.
 */
public interface DatiIstitutoRepository extends
		JpaRepository<DatiIstituto, String> {
	@Lock(LockModeType.WRITE)
	@Query("select a from DatiIstituto a where a.istituto = ?1 and a.anno = ?2")
	DatiIstituto getDatiIstitutoAndLock(String istituto, Integer anno);

	@Query("select a from DatiIstituto a where a.istituto = ?1 and a.anno = ?2")
	DatiIstituto getDatiIstituto(String istituto, Integer anno);
}