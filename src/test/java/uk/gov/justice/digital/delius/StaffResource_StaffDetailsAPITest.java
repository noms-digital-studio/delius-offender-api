package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.StaffDetails;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev-seed")
public class StaffResource_StaffDetailsAPITest {

    @LocalServerPort
    int port;

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
    public void canRetrieveStaffDetailsByStaffCode() {

        val staffDetails = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffCode/SH0001")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        assertThat(staffDetails).isNotNull();
    }

    @Test
    public void retrievingStaffDetailsReturn404WhenStaffDoesNotExist() {

        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffCode/XXXXX")
                .then()
                .statusCode(404);
    }

    @Test
    public void canRetrieveStaffDetailsByUsername() {

        val staffDetails = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/username/SheilaHancockNPS")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        assertThat(staffDetails).isNotNull();
    }

    @Test
    public void canRetrieveStaffDetailsByUsernameIgnoresCase() {

        val staffDetails = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/username/sheilahancocknps")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(StaffDetails.class);

        assertThat(staffDetails).isNotNull();
    }

    @Test
    public void retrievingStaffDetailsByUsernameReturn404WhenUserExistsButStaffDoesNot() {

        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/username/NoStaffUserNPS")
                .then()
                .statusCode(404);
    }

    @Test
    public void retrievingStaffDetailsByUsernameReturn404WhenUserDoesNotExist() {

        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffCode/NOTSheliaHancock")
                .then()
                .statusCode(404);
    }
}
