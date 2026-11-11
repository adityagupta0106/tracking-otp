package com.serviceplus.metadata.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceplus.metadata.catalogue.controller.PublicServiceCatalogueController;
import com.serviceplus.metadata.catalogue.service.ServiceCatalogueService;
import com.serviceplus.metadata.entity.ServiceDefinition;
import com.serviceplus.metadata.entity.ServiceLog;
import com.serviceplus.metadata.exception.GlobalExceptionHandler;
import com.serviceplus.metadata.repository.IServiceDefinitionRepository;

import jakarta.persistence.EntityManager;

/** Real PostgreSQL JSONB tests, only against the explicitly named isolated local test database. */
@EnabledIfEnvironmentVariable(named = "PUBLIC_CATALOGUE_TEST_JDBC_URL",
        matches = "jdbc:postgresql://127[.]0[.]0[.]1:[0-9]+/public_catalogue_test")
class PublicServiceCatalogueRepositoryTest {
    private static SessionFactory factory;
    private EntityManager em;
    private IServiceDefinitionRepository repository;

    @BeforeAll
    static void start() {
        factory = new Configuration()
                .addAnnotatedClass(ServiceDefinition.class).addAnnotatedClass(ServiceLog.class)
                .setProperty("hibernate.connection.url", System.getenv("PUBLIC_CATALOGUE_TEST_JDBC_URL"))
                .setProperty("hibernate.connection.username", "catalogue_test")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.hbm2ddl.create_namespaces", "true")
                .buildSessionFactory();
        // Match Scripts/Table/create.sql, not Hibernate's java.util.Date default.
        try (var manager = factory.createEntityManager()) {
            manager.getTransaction().begin();
            manager.createNativeQuery("""
                    ALTER TABLE schm_sp.service_log ALTER COLUMN launched_date
                    TYPE timestamp with time zone USING launched_date AT TIME ZONE 'UTC'
                    """).executeUpdate();
            manager.getTransaction().commit();
        }
    }

    @AfterAll
    static void stop() {
        if (factory != null) factory.close();
    }

    @BeforeEach
    void begin() {
        em = factory.createEntityManager();
        em.getTransaction().begin();
        repository = new JpaRepositoryFactory(em).getRepository(IServiceDefinitionRepository.class);
    }

    @AfterEach
    void rollback() {
        if (em != null) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }

    @Test
    void categoriesUseMasterValueThenLabelAndMissingOrUnknownBelongToOthers() {
        List<String> categories = List.of(
                "{\"value\":\"166\",\"label\":\"conflicting\"}",
                "{\"value\":164,\"label\":\"G2B\"}",
                "{\"value\":\" G2G \"}",
                "{\"label\":\" g2e \"}",
                "{\"label\":\"Others\"}",
                "null", "{}", "{\"label\":\"unknown\"}",
                "{\"value\":\"invalid\",\"label\":\"G2C\"}");
        for (int i = 0; i < categories.size(); i++) {
            insert(i + 1, 32, "{\"category\":" + categories.get(i) + "}", 4, "Y", "tenant-a", null);
        }
        insert(20, 33, "{}", 4, "Y", "tenant-b", null);
        var summary = repository.findPublicServiceSummary();
        assertThat(summary).hasSize(2);
        var row = summary.getFirst();
        assertThat(row.getStateId()).isEqualTo(32);
        assertThat(row.getTotalServices()).isEqualTo(9);
        assertThat(row.getG2b()).isEqualTo(1);
        assertThat(row.getG2c()).isEqualTo(2);
        assertThat(row.getG2g()).isEqualTo(1);
        assertThat(row.getG2e()).isEqualTo(1);
        assertThat(row.getOthers()).isEqualTo(4);
        assertThat(row.getG2b() + row.getG2c() + row.getG2g() + row.getG2e() + row.getOthers()).isEqualTo(row.getTotalServices());
        for (var group : summary) {
            assertThat(repository.findPublicServices(group.getStateId(), PageRequest.of(0, 1)).getTotalElements())
                    .isEqualTo(group.getTotalServices());
        }
    }

    @Test
    void publicationPredicateExcludesAllOtherStatusesFlagsAndMissingTenantsInBothQueries() {
        for (int status = 0; status <= 5; status++) {
            insert(100 + status, 32, "{}", status, "Y", "tenant-a", null);
        }
        insert(200, 32, "{}", 4, "N", "tenant-a", null);
        insert(201, 32, "{}", 4, "Y", "", null);
        insert(202, 32, "{}", 4, "Y", "   ", null);
        insert(203, 32, "{}", 4, "y", "tenant-a", null);
        insert(204, 32, "{}", 4, "Y", "\t\n", null);
        var page = repository.findPublicServices(32, PageRequest.of(0, 1));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getServiceId()).isEqualTo(104);
        assertThat(page.getContent().getFirst().getLaunchedDate()).isNull();
        assertThat(repository.findPublicServiceSummary().getFirst().getTotalServices()).isEqualTo(1);
    }

    @Test
    void databasePaginationSortsByIdAndPreservesTimestampAndNullsWithoutHydratingUnknownJson() {
        insert(3, 32, "{\"unmappedLegacyField\":true}", 4, "Y", "tenant-a", null);
        insert(1, 32, "{}", 4, "Y", "tenant-a", "2026-10-13 16:18:08.279496+05:30");
        insert(2, 32, "{}", 4, "Y", "tenant-b", null);
        insert(4, 33, "{}", 4, "Y", "tenant-b", null);
        var first = repository.findPublicServices(32, PageRequest.of(0, 1));
        assertThat(first.getTotalElements()).isEqualTo(3);
        assertThat(first.getTotalPages()).isEqualTo(3);
        assertThat(first.hasNext()).isTrue();
        assertThat(first.getContent().getFirst().getServiceId()).isEqualTo(1);
        assertThat(first.getContent().getFirst().getLaunchedDate())
                .isEqualTo(OffsetDateTime.parse("2026-10-13T16:18:08.279496+05:30").toInstant());
        var last = repository.findPublicServices(32, PageRequest.of(2, 1));
        assertThat(last.getContent().getFirst().getServiceId()).isEqualTo(3);
        assertThat(last.getContent().getFirst().getLaunchedDate()).isNull();
        assertThat(last.hasNext()).isFalse();
        assertThat(repository.findPublicServices(32, PageRequest.of(3, 1))).isEmpty();
        assertThat(repository.findPublicServiceSummary().stream().mapToLong(row -> row.getTotalServices()).sum()).isEqualTo(4);
    }

    @Test
    void emptyStatesAndEmptyDatabaseHaveZeroResults() {
        assertThat(repository.findPublicServiceSummary()).isEmpty();
        var page = repository.findPublicServices(999, PageRequest.of(0, 10));
        assertThat(page).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
    }

    @Test
    void currentServiceIdsAreCountedSeparatelyEvenWhenTheyShareABaseId() {
        insert(1, 32, "{}", 4, "Y", "tenant-a", null);
        insert(2, 32, "{}", 4, "Y", "tenant-a", null);
        assertThat(repository.findPublicServiceSummary().getFirst().getTotalServices()).isEqualTo(2);
        assertThat(repository.findPublicServices(32, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
    }

    @Test
    void httpResponsesUseTheRealPostgresQueriesAndReconcile() throws Exception {
        insert(410001, 32, "{\"category\":{\"label\":\"G2C\",\"value\":\"164\"}}", 4, "Y", "tenant-a", "2026-10-13 16:18:08.279496+05:30");
        insert(410002, 32, "{}", 4, "N", "tenant-a", null);
        var service = new ServiceCatalogueService(null, null, repository, null, null, new ObjectMapper(),
                null, null, null, null, null, null, null);
        var mvc = MockMvcBuilders.standaloneSetup(new PublicServiceCatalogueController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/catalogue/public/service-summary"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.grandTotal").value(1))
                .andExpect(jsonPath("$.data[0].g2c").value(1))
                .andExpect(jsonPath("$.data[0].stateName").value(org.hamcrest.Matchers.nullValue()));
        mvc.perform(get("/catalogue/public/services").param("stateId", "32"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalRecords").value(1))
                .andExpect(jsonPath("$.data[0].serviceId").value(410001))
                .andExpect(jsonPath("$.data[0].launchedDate").value("2026-10-13T10:48:08.279496Z"))
                .andExpect(jsonPath("$.data[0].length()").value(5))
                .andExpect(jsonPath("$.data[0].stateName").value(org.hamcrest.Matchers.nullValue()));
    }

    private void insert(int id, int clc, String json, int status, String flag, String tenant, String date) {
        em.createNativeQuery("""
                INSERT INTO schm_sp.service_definition
                (service_id,base_service_id,service_name,service_abbreviation,definition_json,clc_id,
                 department_id,user_id,version_no,minor_version_no,c_date)
                VALUES (:id,1,'Example','EX',CAST(:json AS jsonb),:clc,1,1,1,0,CURRENT_TIMESTAMP)
                """).setParameter("id", id).setParameter("json", json).setParameter("clc", clc).executeUpdate();
        em.createNativeQuery("""
                INSERT INTO schm_sp.service_log
                (service_id,base_service_id,service_status,launch_flag,tenant_id,launched_date,
                 tab_list,version_no,minor_version_no,launch_doc_file_id)
                VALUES (:id,1,:status,:flag,:tenant,CAST(:date AS timestamp with time zone),'',1,0,'test')
                """).setParameter("id", id).setParameter("status", status).setParameter("flag", flag)
                .setParameter("tenant", tenant).setParameter("date", date).executeUpdate();
    }
}
