package com.serviceplus.metadata.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.serviceplus.metadata.entity.ServiceDefinition;
import com.serviceplus.metadata.catalogue.repository.PublicServiceProjection;
import com.serviceplus.metadata.catalogue.repository.PublicServiceSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface IServiceDefinitionRepository extends JpaRepository<ServiceDefinition, Object> {

    // One publication predicate shared by summary, page and count. Tenant presence is
    // not national publication approval: anonymous gateway exposure remains blocked.
    String PUBLIC_SERVICE_SOURCE = """
            FROM schm_sp.service_definition sd
            JOIN schm_sp.service_log sl ON sl.service_id = sd.service_id
            WHERE sl.service_status = 4
              AND sl.launch_flag = 'Y'
              AND sl.tenant_id ~ '[^[:space:]]'
            """;

    @Query(value = """
            WITH published AS (
                SELECT sd.clc_id AS "stateId",
                    CASE UPPER(BTRIM(sd.definition_json -> 'category' ->> 'value'))
                        WHEN '166' THEN 'G2B' WHEN 'G2B' THEN 'G2B'
                        WHEN '164' THEN 'G2C' WHEN 'G2C' THEN 'G2C'
                        WHEN '163' THEN 'G2G' WHEN 'G2G' THEN 'G2G'
                        WHEN '165' THEN 'G2E' WHEN 'G2E' THEN 'G2E'
                        ELSE CASE UPPER(BTRIM(sd.definition_json -> 'category' ->> 'label'))
                            WHEN 'G2B' THEN 'G2B' WHEN 'G2C' THEN 'G2C'
                            WHEN 'G2G' THEN 'G2G' WHEN 'G2E' THEN 'G2E'
                            ELSE 'Others'
                        END
                    END AS category
            """ + PUBLIC_SERVICE_SOURCE + """
            )
            SELECT "stateId", COUNT(*) AS "totalServices",
                COUNT(*) FILTER (WHERE category = 'G2B') AS g2b,
                COUNT(*) FILTER (WHERE category = 'G2C') AS g2c,
                COUNT(*) FILTER (WHERE category = 'G2G') AS g2g,
                COUNT(*) FILTER (WHERE category = 'G2E') AS g2e,
                COUNT(*) FILTER (WHERE category = 'Others') AS others
            FROM published
            GROUP BY "stateId"
            ORDER BY "stateId" ASC
            """, nativeQuery = true)
    List<PublicServiceSummaryProjection> findPublicServiceSummary();

    @Query(value = """
            SELECT sd.service_id AS "serviceId", sd.clc_id AS "stateId",
                sd.service_name AS "serviceName", sl.launched_date AS "launchedDate"
            """ + PUBLIC_SERVICE_SOURCE + """
              AND sd.clc_id = :stateId
            ORDER BY sd.service_id ASC
            """, countQuery = "SELECT COUNT(*) " + PUBLIC_SERVICE_SOURCE + " AND sd.clc_id = :stateId",
            nativeQuery = true)
    Page<PublicServiceProjection> findPublicServices(@Param("stateId") Integer stateId, Pageable pageable);

	ServiceDefinition findByServiceIdAndUserId(Integer serviceId, Long userId);

	List<ServiceDefinition> findByUserId(Long userId);
	
	@Query("SELECT DISTINCT sd FROM ServiceLog sl JOIN ServiceDefinition sd ON sl.serviceId = sd.serviceId WHERE sl.tenantId = :tenantId AND sl.serviceStatus IN :statuses")
	List<ServiceDefinition> findActiveServicesAndStatus(@Param("tenantId") String tenantId, @Param("statuses") List<Integer> statuses);
	
	public Optional<List<ServiceDefinition>> findByBaseServiceId(Integer baseServiceId);
	
	boolean existsByServiceNameIgnoreCase(String serviceName);

	boolean existsByServiceAbbreviation(String serviceAbbreviation);

	boolean existsByServiceNameIgnoreCaseAndServiceIdNot(String serviceName, Integer serviceId);

	boolean existsByServiceAbbreviationAndServiceIdNot(String serviceAbbreviation, Integer serviceId);

    List<ServiceDefinition> findByServiceId(Integer serviceId);

    void deleteByServiceId(Integer serviceId);
    
    @Query(value = """
            SELECT sd.definition_json ->> 'registerToAvail'
            FROM schm_sp.service_definition sd
            WHERE sd.service_id = :serviceId
            """, nativeQuery = true)
    String findRegisterToAvailByServiceId(
            @Param("serviceId") Integer serviceId);

	List<ServiceDefinition> findByDepartmentId(Long entityId);

	List<ServiceDefinition> findByServiceIdIn(List<Integer> serviceIds);

	@Query("""
		    SELECT sd FROM ServiceDefinition sd
		    WHERE (:state IS NULL OR :state = 0 OR sd.stateId = :state)
		    AND (:dept IS NULL OR sd.departmentId = :dept)
		    AND EXISTS (
		        SELECT 1 FROM ServiceLog sl
		        WHERE sl.serviceId = sd.serviceId
		        AND sl.serviceStatus IN :statuses
		    )
		    """)
		Page<ServiceDefinition> findCataloguePage(
		        @Param("state") Integer state,
		        @Param("dept") Long dept,
		        @Param("statuses") List<Integer> statuses,
		        Pageable pageable
		);

    @Query(
            value = """
        SELECT sd FROM ServiceDefinition sd
        INNER JOIN ServiceLog sl ON sl.serviceId = sd.serviceId
        WHERE sl.serviceStatus = 4
        AND (
            :search IS NULL
            OR :search = ''
            OR sd.serviceName ILIKE CONCAT('%', :search, '%')
            OR sd.serviceAbbreviation ILIKE CONCAT('%', :search, '%')
        )
        ORDER BY sd.serviceId DESC
        """,
            countQuery = """
        SELECT COUNT(sd) FROM ServiceDefinition sd
        INNER JOIN ServiceLog sl ON sl.serviceId = sd.serviceId
        WHERE sl.serviceStatus = 4
        AND (
            :search IS NULL
            OR :search = ''
            OR sd.serviceName ILIKE CONCAT('%', :search, '%')
            OR sd.serviceAbbreviation ILIKE CONCAT('%', :search, '%')
        )
        """
    )
    Page<ServiceDefinition> findLaunchedServices(
            @Param("search") String search,
            Pageable pageable
    );


    @Query(
            value = """
        SELECT sd FROM ServiceDefinition sd
        INNER JOIN ServiceLog sl ON sl.serviceId = sd.serviceId
        WHERE sl.tenantId = :tenantId
        AND sl.serviceStatus = 4
        AND (sd.departmentId = :departmentId)
        AND (
            :search IS NULL
            OR :search = ''
            OR sd.serviceName ILIKE CONCAT('%', :search, '%')
            OR sd.serviceAbbreviation ILIKE CONCAT('%', :search, '%')
        )
        ORDER BY sd.serviceId DESC
        """,
            countQuery = """
        SELECT COUNT(sd) FROM ServiceDefinition sd
        INNER JOIN ServiceLog sl ON sl.serviceId = sd.serviceId
        WHERE sl.tenantId = :tenantId
        AND sl.serviceStatus = 4
        AND (sd.departmentId = :departmentId)
        AND (
            :search IS NULL
            OR :search = ''
            OR sd.serviceName ILIKE CONCAT('%', :search, '%')
            OR sd.serviceAbbreviation ILIKE CONCAT('%', :search, '%')
        )
        """
    )
    Page<ServiceDefinition> findLaunchedServicesByTenant(
            @Param("tenantId") String tenantId,
            @Param("departmentId") Integer departmentId,

            @Param("search") String search,
            Pageable pageable
    );
}
