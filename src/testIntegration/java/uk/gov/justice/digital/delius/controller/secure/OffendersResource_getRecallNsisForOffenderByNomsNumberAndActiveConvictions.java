package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getRecallNsisForOffenderByNomsNumberAndActiveConvictions extends IntegrationTestBase {
    private static final String GET_NSI_PATH = "/offenders/nomsNumber/%s/convictions/active/nsis/recall";
    private static final Long KNOWN_NSI_ID_1 = 2500018997L;
    private static final Long KNOWN_NSI_ID_2 = 2500018998L;
    private static final String KNOWN_NOMS_NUMBER_FOR_NSI = "G9542VP";

    @Test
    @DisplayName("must have ROLE_COMMUNITY")
    void mustHaveROLE_COMMUNITY() {
        final var path = String.format(GET_NSI_PATH, KNOWN_NOMS_NUMBER_FOR_NSI);

        given()
            .auth()
            .oauth2(createJwt("ROLE_BANANAS"))
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("will return all recalls only")
    void willReturnAllRecallsOnly() {
        final var path = String.format(GET_NSI_PATH, KNOWN_NOMS_NUMBER_FOR_NSI);

        final var recallNoOutcome = withArgs(KNOWN_NSI_ID_1);
        final var recallWithOutcome = withArgs(KNOWN_NSI_ID_2);


        final var nsi = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .assertThat()
            .statusCode(200)
            .body("nsis.size()", equalTo(2))
            .root("nsis.find { it.nsiId == %d }")
            .body("nsiType.code", recallNoOutcome, equalTo("REC"))
            .body("nsiOutcome", recallNoOutcome, nullValue())
            .body("nsiStatus.description", recallNoOutcome, equalTo("Recall Initiated"))

            .body("nsiType.code", recallWithOutcome, equalTo("REC"))
            .body("nsiOutcome.code", recallWithOutcome, equalTo("REC01"))
            .body("nsiStatus.description", recallWithOutcome, equalTo("PPCS Recall Decision Received"));
    }

    @Test
    @DisplayName("will get 404 when offender not found")
    void willGet404WhenOffenderNotFound() {
        final var path = String.format(GET_NSI_PATH, "UNKNOWN_CRN");

        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(404);
    }
}
