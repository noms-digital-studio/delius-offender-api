package uk.gov.justice.digital.delius.integration.secure;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
@RunWith(SpringJUnit4ClassRunner.class)
public class OffendersResource_getOffenderNsisByCrn {

    private static final String OFFENDERS_PATH = "/offenders/crn/%s/convictions/%s/nsis?";

    private static final String QUERY_PARAM_NAME = "nsiCodes";

    private static final Long KNOWN_CONVICTION_ID = 2500295345L;

    private static final Long KNOWN_CONVICTION_ID_NO_BREACH = 2500295343L;

    private static final String KNOWN_OFFENDER = "X320741";

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.token.good}")
    private String validOauthToken;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void getGetOffenderNsisByCrnAndConvictionId() {
        final StringBuilder sb = new StringBuilder(String.format(OFFENDERS_PATH, KNOWN_OFFENDER, KNOWN_CONVICTION_ID))
            .append(QUERY_PARAM_NAME).append("=").append("BRE").append("&").append(QUERY_PARAM_NAME).append("=").append("BRES");
        final var nsiWrapper = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(sb.toString())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(NsiWrapper.class);

        assertThat(nsiWrapper.getNsis()).hasSize(2);
    }

    @Test
    public void getGetOffenderNsisByCrnAndKnownConvictionIdButNoMatchToFilter() {
        final StringBuilder sb = new StringBuilder(String.format(OFFENDERS_PATH, KNOWN_OFFENDER, KNOWN_CONVICTION_ID))
            .append(QUERY_PARAM_NAME).append("=").append("XXX");
        final var nsiWrapper = given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(sb.toString())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(NsiWrapper.class);

        assertThat(nsiWrapper.getNsis()).hasSize(0);
    }

    @Test
    public void getGetOffenderNsisByCrnAndConvictionIdWithNoNsiBreaches() {
        final StringBuilder sb = new StringBuilder(String.format(OFFENDERS_PATH, KNOWN_OFFENDER, KNOWN_CONVICTION_ID_NO_BREACH))
            .append(QUERY_PARAM_NAME).append("=").append("BRE").append("&").append(QUERY_PARAM_NAME).append("=").append("BRES");
        given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(sb.toString())
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void getGetOffenderNsisByCrnAndConvictionIdLimitCode() {

        final StringBuilder sb = new StringBuilder(String.format(OFFENDERS_PATH, KNOWN_OFFENDER, KNOWN_CONVICTION_ID))
            .append(QUERY_PARAM_NAME).append("=").append("BRES");

        final var nsiWrapper = given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(sb.toString())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(NsiWrapper.class);

        assertThat(nsiWrapper.getNsis()).hasSize(1);
    }

    @Test
    public void getGetOffenderNsisByCrnAndUnknownConvictionId_Returns404() {
        final StringBuilder sb = new StringBuilder(String.format(OFFENDERS_PATH, KNOWN_OFFENDER, "0"))
            .append(QUERY_PARAM_NAME).append("=").append("BRE").append("&").append(QUERY_PARAM_NAME).append("=").append("BRES");

        given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(sb.toString())
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void getOffenderNsisByCrn_offenderNotFound_returnsNotFound() {
        final StringBuilder sb = new StringBuilder(String.format(OFFENDERS_PATH, "X777777", KNOWN_CONVICTION_ID))
            .append(QUERY_PARAM_NAME).append("=").append("BRE").append("&").append(QUERY_PARAM_NAME).append("=").append("BRES");

        given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(sb.toString())
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

}
