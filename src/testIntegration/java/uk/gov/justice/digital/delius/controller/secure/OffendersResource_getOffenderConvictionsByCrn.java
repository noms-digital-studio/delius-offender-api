package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.data.api.Sentence;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class OffendersResource_getOffenderConvictionsByCrn extends IntegrationTestBase {
    @Test
    void canGetOffenderConvictionsByCrn() {
        final var convictions = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/convictions")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Conviction[].class);

        final var conviction = Stream.of(convictions).filter(Conviction::getActive).findAny().orElseThrow();
        assertThat(conviction.isAwaitingPsr()).isTrue();
        final var offence = conviction.getOffences().stream().filter(Offence::getMainOffence).findAny().orElseThrow();
        assertThat(offence.getDetail().getCode()).isEqualTo("00102");
        final var sentenceType = conviction.getSentence().getSentenceType();
        assertThat(sentenceType.getCode()).isEqualTo("SC");
        assertThat(sentenceType.getDescription()).isEqualTo("CJA - Indeterminate Public Prot.");

        // Should have mapped court appearance from sentencing appearance & responsible court detail
        assertThat(conviction)
            .hasFieldOrPropertyWithValue("courtAppearance.appearanceType.code", "S")
            .hasFieldOrPropertyWithValue("courtAppearance.appearanceType.description", "Sentence")
            .hasFieldOrPropertyWithValue("courtAppearance.courtName", "Sheffield Magistrates Court")
            .hasFieldOrPropertyWithValue("responsibleCourt.courtName", "Sheffield Magistrates Court")
            // Should have mapped additional sentences
            .extracting(Conviction::getSentence)
            .extracting(Sentence::getAdditionalSentences).asList()
            .hasSize(1)
            .first()
            .hasFieldOrPropertyWithValue("additionalSentenceId", 2500002005L)
            .hasFieldOrPropertyWithValue("type.description", "Disqualified from Driving")
            .hasFieldOrPropertyWithValue("type.code", "DISQ")
            .hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(100))
            .hasFieldOrPropertyWithValue("length", 6L)
            .hasFieldOrPropertyWithValue("notes", "Additional Sentence 1");
    }

    @Test
    void canGetOffenderConvictionByCrnAndConvictionId() {
        final var conviction = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/convictions/2500295343")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Conviction.class);

        final var offence = conviction.getOffences().stream().filter(Offence::getMainOffence).findAny().orElseThrow();
        assertThat(offence.getDetail().getCode()).isEqualTo("05600");
    }

    @Test
    void convictionsHaveAssociatedUnpaidWorkData() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/convictions")
                .then()
                .statusCode(200)
                .body("[2].sentence.sentenceId", notNullValue())
                .body("[2].sentence.unpaidWork.minutesOrdered", equalTo( 3600))
                .body("[2].sentence.unpaidWork.minutesCompleted", equalTo(360))
                .body("[2].sentence.unpaidWork.appointments.total", equalTo(5))
                .body("[2].sentence.unpaidWork.appointments.attended", equalTo(2))
                .body("[2].sentence.unpaidWork.appointments.acceptableAbsences", equalTo(1))
                .body("[2].sentence.unpaidWork.appointments.unacceptableAbsences", equalTo(1))
                .body("[2].sentence.unpaidWork.appointments.noOutcomeRecorded", equalTo(1));
    }

    @Test
    void getOffenderConvictionsByCrn_offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X777777/convictions")
                .then()
                .statusCode(404);
    }

    @Test
    void canGetActiveConvictionsOnly(){
        final var convictions = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/convictions?activeOnly=true")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Conviction[].class);

        final var inactiveConviction = Stream.of(convictions).filter(c -> c.getActive() == false).findAny();
        assertThat(inactiveConviction.isPresent()).isFalse();
    }
}
