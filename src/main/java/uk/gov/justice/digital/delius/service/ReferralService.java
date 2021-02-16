package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;

@Service
public class ReferralService {
    private final WebClient webClient;

    @Autowired
    public ReferralService(@Qualifier("deliusApiWebClient") final WebClient webClient) {
        this.webClient = webClient;
    }


    @Transactional
    public void createReferralSent(final String crn,
                                   final ReferralSentRequest referralSent) {

        var contact = NewContact.builder()
            .offenderCrn(crn)
            .type(referralSent.getReferralType())
//            .outcome()
            .provider(referralSent.getProviderCode())
            .team(referralSent.getTeamCode())
            .staff(referralSent.getStaffCode())
//            .officeLocation(referralSent.get)
            .date(referralSent.getDate())
//            .startTime()
//            .endTime()
//            .alert()
//            .sensitive()
            .notes(referralSent.getNotes()).build();
//            .description()
//            .eventId()
//            .requirementId();

        webClient.post()
            .uri("/v1/contact")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(contact)
            .retrieve()
            .toEntity(String.class)
            .block();
    }
}
