package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.Sentence;
import uk.gov.justice.digital.delius.data.api.UnpaidWork;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.DisposalType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.UpwAppointment;
import uk.gov.justice.digital.delius.jpa.standard.entity.UpwDetails;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.service.ReferenceDataService.REFERENCE_DATA_PSR_ADJOURNED_CODE;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;

class ConvictionTransformerTest {
    @Test
    void convictionIdMappedFromEventId() {
        assertThat(ConvictionTransformer.convictionOf(
            anEvent()
                .toBuilder()
                .eventId(99L)
                .build()).getConvictionId()
        ).isEqualTo(99L);

    }

    @Test
    void offencesCollatedFromMainAndAdditionalOffences() {

        assertThat(ConvictionTransformer.convictionOf(
            anEvent()
                .toBuilder()
                .eventId(99L)
                .mainOffence(aMainOffence())
                .additionalOffences(ImmutableList.of(anAdditionalOffence(), anAdditionalOffence()))
                .build()).getOffences()
        ).hasSize(3);
    }

    @Test
    void activeMappedForZeroOneActiveFlag() {
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().activeFlag(true).build()).getActive())).isTrue();
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().activeFlag(false).build()).getActive())).isFalse();

    }

    @Test
    void inBreachMappedForZeroOneInBreach() {
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().inBreach(true).build()).getInBreach())).isTrue();
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().inBreach(false).build()).getInBreach())).isFalse();

    }

    @Test
    void sentenceIsMappedWhenEvenHasDisposal() {
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(null).build()).getSentence())).isNull();
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(aDisposal()).build()).getSentence())).isNotNull();
    }

    @Test
    void sentenceIdIsMappedWhenEventHasDisposal() {
        Sentence sentence = ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(disposalBuilder().disposalId(1234L).build()).build()).getSentence();
        assertThat(sentence.getSentenceId()).isEqualTo(1234L);
    }

    @Test
    void enteredSentenceEndDateOverridesExpectedSentenceEndDate() {
        final var expectedEndDate = LocalDate.now().minusDays(1);
        final var enteredEndDate = LocalDate.now().minusDays(2);
        final var disposal = disposalBuilder().enteredSentenceEndDate(enteredEndDate).expectedSentenceEndDate(expectedEndDate).build();
        assertThat(ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(disposal).build()).getSentence().getExpectedSentenceEndDate()).isEqualTo(enteredEndDate);
    }

    @Test
    void expectedSentenceEndDateIsMappedIfEnteredSentenceEndDateNotPresent() {
        final var expectedEndDate = LocalDate.now().minusDays(1);
        final var disposal = disposalBuilder().expectedSentenceEndDate(expectedEndDate).build();
        assertThat(ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(disposal).build()).getSentence().getExpectedSentenceEndDate()).isEqualTo(expectedEndDate);
    }

    @Test
    void expectedSentenceEndDateMappingCanHandleNulls() {
        assertThat(ConvictionTransformer.convictionOf(anEvent().toBuilder().disposal(aDisposal()).build()).getSentence().getExpectedSentenceEndDate()).isNull();
    }



    @Test
    void responsibleCourtMappedFromCourt() {
        assertThat(ConvictionTransformer.convictionOf(
            anEvent()
                .toBuilder()
                .court(EntityHelper.aCourt("some-court"))
                .build()))
            .isNotNull()
            .hasFieldOrPropertyWithValue("responsibleCourt.courtId", 99L)
            .hasFieldOrPropertyWithValue("responsibleCourt.courtName", "Sheffield Crown Court");
    }

    @Test
    void indexMappedFromEventNumberString() {
        assertThat((ConvictionTransformer.convictionOf(anEvent().toBuilder().eventNumber("5").build()).getIndex())).isEqualTo("5");
    }

    @Test
    void custodyNotSetWhenDisposalNotPresent() {
        assertThat(
            ConvictionTransformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .disposal(null)
                    .build()
            ).getCustody()
        ).isNull();
    }

    @Test
    void custodyNotSetWhenCustodyNotPresentInDisposal() {
        assertThat(
            ConvictionTransformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .disposal(Disposal
                        .builder()
                        .custody(null)
                        .disposalType(aDisposalType())
                        .build())
                    .build()
            ).getCustody()
        ).isNull();
    }

    @Test
    void custodySetWhenCustodyPresentInDisposal() {
        assertThat(
            ConvictionTransformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .disposal(Disposal
                        .builder()
                        .disposalType(aDisposalType())
                        .custody(aCustody())
                        .build())
                    .build()
            ).getCustody()
        ).isNotNull();
    }

    @Test
    void bookingNumberCopiedFromCustodyPrisonerNumber() {
        assertThat(
            ConvictionTransformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .disposal(Disposal
                        .builder()
                        .disposalType(aDisposalType())
                        .custody(
                            aCustody()
                                .toBuilder()
                                .prisonerNumber("V74111")
                                .build()
                        )
                        .build())
                    .build()
            ).getCustody().getBookingNumber()
        ).isEqualTo("V74111");
    }

    @Test
    void institutionCopiedFromCustodyWhenPresent() {
        assertThat(
            ConvictionTransformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .disposal(Disposal
                        .builder()
                        .custody(
                            aCustody()
                                .toBuilder()
                                .prisonerNumber("V74111")
                                .institution(anInstitution())
                                .build()
                        )
                        .disposalType(aDisposalType())
                        .build())
                    .build()
            ).getCustody().getInstitution()
        ).isNotNull();
    }

    @Test
    void institutionNotCopiedFromCustodyWhenNotPresent() {

        assertThat(
            ConvictionTransformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .disposal(Disposal
                        .builder()
                        .custody(
                            aCustody()
                                .toBuilder()
                                .prisonerNumber("V74111")
                                .institution(null)
                                .build()
                        )
                        .disposalType(aDisposalType())
                        .build())
                    .build()
            ).getCustody().getInstitution()
        ).isNull();
    }


    @Test
    void unpaidWorkMappedWherePresent() {

        Event event = EntityHelper.anEvent().toBuilder()
            .disposal(Disposal.builder()
                .disposalType(aDisposalType())
                .unpaidWorkDetails(UpwDetails.builder()
                    .upwLengthMinutes(120L)
                    .status(StandardReference.builder()
                        .standardReferenceListId(3503L)
                        .codeValue("IGNORED")
                        .codeDescription("Being worked")
                        .build())
                    .appointments(Arrays.asList(
                        UpwAppointment.builder()
                            .attended("Y")
                            .complied("Y")
                            .minutesCredited(60L)
                            .build(),
                        UpwAppointment.builder()
                            .attended("Y")
                            .complied("Y")
                            .minutesCredited(10L)
                            .build(),
                        UpwAppointment.builder()
                            .attended("Y")
                            .complied("Y")
                            .minutesCredited(20L)
                            .build(),
                        UpwAppointment.builder()
                            .attended("N")
                            .complied("Y")
                            .build(),
                        UpwAppointment.builder()
                            .attended("N")
                            .complied("N")
                            .build(),
                        UpwAppointment.builder()
                            .attended(null)
                            .complied(null)
                            .build()
                    ))
                    .build())
                .build())
            .build();
        UnpaidWork unpaidWork = ConvictionTransformer.convictionOf(event)
            .getSentence()
            .getUnpaidWork();

        assertThat(unpaidWork).isNotNull();
        assertThat(unpaidWork.getMinutesOrdered()).isEqualTo(120L);
        assertThat(unpaidWork.getMinutesCompleted()).isEqualTo(90L);
        assertThat(unpaidWork.getAppointments().getTotal()).isEqualTo(6);
        assertThat(unpaidWork.getAppointments().getAcceptableAbsences()).isEqualTo(1);
        assertThat(unpaidWork.getAppointments().getUnacceptableAbsences()).isEqualTo(1);
        assertThat(unpaidWork.getAppointments().getNoOutcomeRecorded()).isEqualTo(1);
        assertThat(unpaidWork.getStatus()).isEqualTo("Being worked");

    }

    @Test
    void unpaidWorkIsNullWhereNonePresent() {
        Event event = EntityHelper.anEvent().toBuilder()
            .disposal(Disposal.builder()
                .unpaidWorkDetails(null)
                .disposalType(aDisposalType())
                .build())
            .build();
        Conviction conviction = ConvictionTransformer.convictionOf(event);
        UnpaidWork unpaidWork = conviction.getSentence().getUnpaidWork();
        assertThat(unpaidWork).isNull();
    }

    @Test
    void sentenceStartDateCopiedWhenPresent() {
        Event event = EntityHelper.anEvent().toBuilder()
            .disposal(Disposal.builder()
                .startDate(LocalDate.of(2020, 2, 22))
                .disposalType(aDisposalType())
                .build())
            .build();
        final var conviction = ConvictionTransformer.convictionOf(event);
        assertThat(conviction.getSentence().getStartDate()).isEqualTo(LocalDate.of(2020, 2, 22));
    }

    @Test
    void sentenceTerminationDetailsCopiedWhenPresent() {

        final StandardReference standardReference = StandardReference.builder()
            .standardReferenceListId(3758L)
            .codeValue("DT02")
            .codeDescription("Auto Terminated")
            .build();

        Event event = EntityHelper.anEvent().toBuilder()
            .disposal(Disposal.builder()
                .terminationDate(LocalDate.of(2020, 2, 22))
                .terminationReason(standardReference)
                .disposalType(aDisposalType())
                .build())
            .build();
        final var conviction = ConvictionTransformer.convictionOf(event);
        assertThat(conviction.getSentence().getTerminationDate()).isEqualTo(LocalDate.of(2020, 2, 22));
        assertThat(conviction.getSentence().getTerminationReason()).isEqualTo("Auto Terminated");
    }

    @Test
    void sentenceType(){
        Event event = EntityHelper.anEvent().toBuilder()
            .disposal(Disposal.builder()
                .disposalType(DisposalType.builder().sentenceType("SC").description("SC Description").build())
                .build())
            .build();
        final var conviction = ConvictionTransformer.convictionOf(event);
        assertThat(conviction.getSentence().getSentenceType().getCode()).isEqualTo("SC");
        assertThat(conviction.getSentence().getSentenceType().getDescription()).isEqualTo("SC Description");
    }

    @Nested
    class CustodyRelatedKeyDatesOf {
        @Test
        void willSetNothingIfNoneExist() {
            final var keyDates = ConvictionTransformer.custodyOf(aCustody().toBuilder().keyDates(List.of()).build())
                .getKeyDates();

            assertThat(keyDates.getConditionalReleaseDate()).isNull();
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverDate()).isNull();
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverStartDate()).isNull();
            assertThat(keyDates.getExpectedReleaseDate()).isNull();
            assertThat(keyDates.getHdcEligibilityDate()).isNull();
            assertThat(keyDates.getLicenceExpiryDate()).isNull();
            assertThat(keyDates.getParoleEligibilityDate()).isNull();
            assertThat(keyDates.getPostSentenceSupervisionEndDate()).isNull();
            assertThat(keyDates.getSentenceExpiryDate()).isNull();
        }

        @Test
        void willSetNothingIfNoneOfTheOnesWeAreInterestedInExist() {
            final var keyDates = ConvictionTransformer.custodyOf(aCustody().toBuilder()
                .keyDates(List.of(aKeyDate("XX", "Whatever", LocalDate
                    .now()))).build())
                .getKeyDates();

            assertThat(keyDates.getConditionalReleaseDate()).isNull();
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverDate()).isNull();
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverStartDate()).isNull();
            assertThat(keyDates.getExpectedReleaseDate()).isNull();
            assertThat(keyDates.getHdcEligibilityDate()).isNull();
            assertThat(keyDates.getLicenceExpiryDate()).isNull();
            assertThat(keyDates.getParoleEligibilityDate()).isNull();
            assertThat(keyDates.getPostSentenceSupervisionEndDate()).isNull();
            assertThat(keyDates.getSentenceExpiryDate()).isNull();
        }

        @Test
        void willSetAllIfAllAreSet() {
            final var keyDates = ConvictionTransformer.custodyOf(aCustody().toBuilder()
                .keyDates(List.of(
                    aKeyDate("LED", "LicenceExpiryDate", LocalDate.of(2030, 1, 1)),
                    aKeyDate("POM2", "ExpectedPrisonOffenderManagerHandoverDate", LocalDate.of(2030, 1, 2)),
                    aKeyDate("POM1", "ExpectedPrisonOffenderManagerHandoverStartDate", LocalDate.of(2030, 1, 3)),
                    aKeyDate("ACR", "ConditionalReleaseDate", LocalDate.of(2030, 1, 4)),
                    aKeyDate("EXP", "ExpectedReleaseDate", LocalDate.of(2030, 1, 5)),
                    aKeyDate("HDE", "HdcEligibilityDate", LocalDate.of(2030, 1, 6)),
                    aKeyDate("PSSED", "PostSentenceSupervisionEndDate", LocalDate.of(2030, 1, 7)),
                    aKeyDate("PED", "ParoleEligibilityDate", LocalDate.of(2030, 1, 8)),
                    aKeyDate("SED", "SentenceExpiryDate", LocalDate.of(2030, 1, 9)),
                    aKeyDate("XX", "Whatever", LocalDate.now())
                )).build())
                .getKeyDates();

            assertThat(keyDates.getLicenceExpiryDate()).isEqualTo(LocalDate.of(2030, 1, 1));
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverDate()).isEqualTo(LocalDate.of(2030, 1, 2));
            assertThat(keyDates.getExpectedPrisonOffenderManagerHandoverStartDate()).isEqualTo(LocalDate.of(2030, 1, 3));
            assertThat(keyDates.getConditionalReleaseDate()).isEqualTo(LocalDate.of(2030, 1, 4));
            assertThat(keyDates.getExpectedReleaseDate()).isEqualTo(LocalDate.of(2030, 1, 5));
            assertThat(keyDates.getHdcEligibilityDate()).isEqualTo(LocalDate.of(2030, 1, 6));
            assertThat(keyDates.getPostSentenceSupervisionEndDate()).isEqualTo(LocalDate.of(2030, 1, 7));
            assertThat(keyDates.getParoleEligibilityDate()).isEqualTo(LocalDate.of(2030, 1, 8));
            assertThat(keyDates.getSentenceExpiryDate()).isEqualTo(LocalDate.of(2030, 1, 9));
        }
    }

    @Nested
    class CourtAppearanceRelated {
        @Test
        void outcomeMappedFromLastCourtAppearance() {
            assertThat(ConvictionTransformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .courtAppearances(ImmutableList.of(
                        aCourtAppearanceWithNoOutcome(LocalDateTime.now()),
                        aCourtAppearance("Final Review", "Y", LocalDateTime.now().minusDays(1)),
                        aCourtAppearance("Adjourned", "X", LocalDateTime.now().minusDays(2))
                    ))
                    .build()).getLatestCourtAppearanceOutcome())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "Y")
                .hasFieldOrPropertyWithValue("description", "Final Review");
        }

        @Test
        void outcomeIndicatesAwaitingPsr() {
            assertThat(ConvictionTransformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .courtAppearances(ImmutableList.of(
                        aCourtAppearanceWithNoOutcome(LocalDateTime.now()),
                        aCourtAppearance("Final Review", "Y", LocalDateTime.now().minusDays(1)),
                        aCourtAppearance("PSR Adjourned", REFERENCE_DATA_PSR_ADJOURNED_CODE, LocalDateTime.now().minusDays(2))
                    ))
                    .build()))
                .isNotNull()
                .hasFieldOrPropertyWithValue("awaitingPsr", true);
        }

        @Test
        void courtMappedFromLatestAppearance() {
            assertThat(ConvictionTransformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .courtAppearances(List.of(
                        aCourtAppearanceWithNoOutcome(LocalDateTime.of(2015, 6, 10, 12, 0)),
                        aCourtAppearanceWithNoOutcome(LocalDateTime.of(2015, 6, 11, 12, 0))
                    ))
                    .build()))
                .isNotNull()
                .hasFieldOrPropertyWithValue("courtAppearance.appearanceDate", LocalDateTime.of(2015, 6, 11, 12, 0));
        }

        private CourtAppearance aCourtAppearance(String outcomeDescription, String outcomeCode, LocalDateTime appearanceDate) {
            return EntityHelper.aCourtAppearanceWithOutcome(outcomeCode,outcomeDescription)
                .toBuilder()
                .appearanceDate(appearanceDate)
                .build();
        }

        private CourtAppearance aCourtAppearanceWithNoOutcome(LocalDateTime appearanceDate) {
            return EntityHelper.aCourtAppearanceWithOutOutcome()
                .toBuilder()
                .appearanceDate(appearanceDate)
                .build();
        }
    }

    @Test
    void notesMappedFromEvent() {
        assertThat(ConvictionTransformer.convictionOf(
            anEvent()
                .toBuilder()
                .notes("Some notes")
                .build()).getNotes()
        ).isEqualTo("Some notes");
    }

    @Nested
    class AdditionalSentences {
        @Test
        void mappedFromEvent() {
            final var additionalSentence = EntityHelper.anAdditionalSentence();
            final var source =  anEvent().toBuilder()
                .disposal(aDisposal())
                .additionalSentences(List.of(additionalSentence))
                .build();
            final var observed = ConvictionTransformer.convictionOf(source);
            assertThat(observed.getSentence().getAdditionalSentences())
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("additionalSentenceId", 100L)
                .hasFieldOrPropertyWithValue("type.description", "Disqualified from Driving")
                .hasFieldOrPropertyWithValue("type.code", "DISQ")
                .hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(100))
                .hasFieldOrPropertyWithValue("length", 6L)
                .hasFieldOrPropertyWithValue("notes", "Additional Sentence 1");
        }

        @Test
        void ignoredWhenNull() {
            final var source = anEvent().toBuilder().disposal(aDisposal()).build();
            final var observed = ConvictionTransformer.convictionOf(source);
            assertThat(observed.getSentence().getAdditionalSentences()).isNull();
        }

        @Test
        void mappedToEmptyWhenEmpty() {
            final var source =  anEvent().toBuilder().disposal(aDisposal()).additionalSentences(List.of()).build();
            final var observed = ConvictionTransformer.convictionOf(source);
            assertThat(observed.getSentence().getAdditionalSentences()).isEmpty();
        }

        @Test
        void ignoredWhenNoSentence() {
            final var additionalSentence = EntityHelper.anAdditionalSentence();
            final var source =  anEvent().toBuilder().additionalSentences(List.of(additionalSentence)).build();
            final var observed = ConvictionTransformer.convictionOf(source);
            assertThat(observed.getSentence()).isNull();
        }
    }



    private AdditionalOffence anAdditionalOffence() {
        return AdditionalOffence
            .builder()
            .offence(anOffence())
            .build();
    }

    private MainOffence aMainOffence() {
        return MainOffence
            .builder()
            .offence(anOffence())
            .build();
    }

    private Offence anOffence() {
        return Offence
            .builder()
            .ogrsOffenceCategory(StandardReference.builder().build())
            .build();
    }

    private Event anEvent() {
        return Event
            .builder()
            .additionalOffences(ImmutableList.of())
            .courtAppearances(ImmutableList.of())
            .build();
    }

    private Disposal aDisposal() {
        return disposalBuilder()
            .build();
    }

    private Disposal.DisposalBuilder disposalBuilder() {
        return Disposal.builder()
            .disposalId(1L)
            .event(anEvent())
            .offenderId(1L)
            .softDeleted(0L)
            .disposalType(aDisposalType());
    }

    private DisposalType aDisposalType() {
        return DisposalType.builder().build();
    }

    private Custody aCustody() {
        return Custody.builder().disposal(aDisposal()).build();
    }

    private RInstitution anInstitution() {
        return RInstitution.builder().build();
    }

}
