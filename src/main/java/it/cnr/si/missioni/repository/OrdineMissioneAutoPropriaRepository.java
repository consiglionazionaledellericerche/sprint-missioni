/*
 *  Copyright (C) 2023  Consiglio Nazionale delle Ricerche
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Affero General Public License as
 *      published by the Free Software Foundation, either version 3 of the
 *      License, or (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Affero General Public License for more details.
 *
 *      You should have received a copy of the GNU Affero General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package it.cnr.si.missioni.repository;

import it.cnr.si.missioni.domain.custom.persistence.OrdineMissione;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissioneAutoPropria;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for the AutoPropria entity.
 */
public interface OrdineMissioneAutoPropriaRepository extends
        JpaRepository<OrdineMissioneAutoPropria, Long> {

    @Query("select a from OrdineMissioneAutoPropria a where a.ordineMissione = ?1 and a.stato != 'ANN'")
    OrdineMissioneAutoPropria getAutoPropria(OrdineMissione ordineMissione);
    
    @Query("select a from OrdineMissioneAutoPropria a where a.ordineMissione = ?1")
    Optional<OrdineMissioneAutoPropria> getAutoPropria(OrdineMissione ordineMissione, boolean all);

}
