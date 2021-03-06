package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class ConvictionAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jwt jwt;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @Test
    public void canGetConvictionsByCrn() {

        Conviction[] convictions = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/X320741/convictions")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Conviction[].class);

        assertThat(convictions).extracting("convictionId").containsExactlyInAnyOrder(2500297061L, 2500295345L, 2500295343L, 10002L, 10003L);
        assertThat(convictions).filteredOn("convictionId", 2500295343L).extracting("sentence.expectedSentenceEndDate")
                .containsExactly(LocalDate.of(2019, 10, 15));
        assertThat(convictions).filteredOn("convictionId", 2500295345L).extracting("sentence.expectedSentenceEndDate")
                .containsExactly(LocalDate.of(2024, 9, 03));
    }

    @Test
    public void convictionByCrnMustHaveValidJwt() {
        given()
            .when()
            .get("offenders/crn/CRN1/convictions")
            .then()
            .statusCode(401);
    }

    private Conviction aConviction(Long id) {
        return Conviction.builder()
            .convictionId(id)
            .build();
    }

    private String aValidToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }
}
