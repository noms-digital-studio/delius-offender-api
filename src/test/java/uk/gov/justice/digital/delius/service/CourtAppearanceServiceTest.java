package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtAppearanceRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtAppearanceServiceTest {

    private CourtAppearanceService courtAppearanceService;

    @Mock
    private CourtAppearanceRepository courtAppearanceRepository;

    @BeforeEach
    public void setUp() {
        courtAppearanceService = new CourtAppearanceService(courtAppearanceRepository);
        when(courtAppearanceRepository.findByOffenderId(1L))
            .thenReturn(
                ImmutableList.of(
                    uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance.builder()
                        .courtAppearanceId(1L)
                        .softDeleted(1L)
                        .appearanceDate(LocalDateTime.now())
                        .offenderId(1L)
                        .event(Event
                                .builder()
                                .eventId(50L)
                                .mainOffence(aMainOffence(1L))
                                .build())
                        .court(aCourt())
                        .courtReports(ImmutableList.of(
                            uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder()
                                .courtReportId(1L)
                                .build()
                        )).build(),
                    uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance.builder()
                        .courtAppearanceId(2L)
                        .appearanceDate(LocalDateTime.now())
                        .offenderId(1L)
                            .event(Event
                                    .builder()
                                    .eventId(50L)
                                    .mainOffence(aMainOffence(100L))
                                    .additionalOffences(ImmutableList.of(anAdditionalOffence(200L), anAdditionalOffence(201L)))
                                    .build())
                            .court(aCourt())
                        .courtReports(ImmutableList.of(
                            uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder()
                                .courtReportId(1L)
                                .build()
                        )).build()
                )
            );

    }

    private AdditionalOffence anAdditionalOffence(long id) {
        return AdditionalOffence.builder()
            .additionalOffenceId(id)
            .build();
    }

    private MainOffence aMainOffence(long id) {
        return MainOffence.builder()
            .mainOffenceId(id)
            .build();
    }

    @Test
    public void softDeletedRecordsAreExcluded() {

        List<CourtAppearance> courtAppearances = courtAppearanceService.courtAppearancesFor(1L);

        assertThat(courtAppearances).extracting("courtAppearanceId")
            .containsOnly(2L);

        assertThat(courtAppearances.get(0).getOffenceIds()).containsOnly("M100", "A200", "A201");

    }

    private Court aCourt() {
        return Court.builder()
            .courtId(1L)
            .build();
    }

}