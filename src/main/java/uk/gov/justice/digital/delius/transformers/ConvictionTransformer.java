package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import uk.gov.justice.digital.delius.data.api.AdditionalSentence;
import uk.gov.justice.digital.delius.data.api.Appointments;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.CustodyRelatedKeyDates;
import uk.gov.justice.digital.delius.data.api.CustodyRelatedKeyDates.CustodyRelatedKeyDatesBuilder;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.data.api.Sentence;
import uk.gov.justice.digital.delius.data.api.UnpaidWork;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.DisposalType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.UpwAppointment;
import uk.gov.justice.digital.delius.jpa.standard.entity.UpwDetails;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

public class ConvictionTransformer {

    enum KeyDateTypes {
        LICENCE_EXPIRY_DATE("LED", CustodyRelatedKeyDatesBuilder::licenceExpiryDate),
        AUTOMATIC_CONDITIONAL_RELEASE_DATE("ACR", CustodyRelatedKeyDatesBuilder::conditionalReleaseDate),
        PAROLE_ELIGIBILITY_DATE("PED", CustodyRelatedKeyDatesBuilder::paroleEligibilityDate),
        SENTENCE_EXPIRY_DATE("SED", CustodyRelatedKeyDatesBuilder::sentenceExpiryDate),
        EXPECTED_RELEASE_DATE("EXP", CustodyRelatedKeyDatesBuilder::expectedReleaseDate),
        HDC_EXPECTED_DATE("HDE", CustodyRelatedKeyDatesBuilder::hdcEligibilityDate),
        POST_SENTENCE_SUPERVISION_END_DATE("PSSED", CustodyRelatedKeyDatesBuilder::postSentenceSupervisionEndDate),
        POM_HANDOVER_START_DATE("POM1", CustodyRelatedKeyDatesBuilder::expectedPrisonOffenderManagerHandoverStartDate),
        RO_HANDOVER_DATE("POM2", CustodyRelatedKeyDatesBuilder::expectedPrisonOffenderManagerHandoverDate);

        private final BiConsumer<CustodyRelatedKeyDatesBuilder, LocalDate> consumer;
        private final String code;

        KeyDateTypes(String code, BiConsumer<CustodyRelatedKeyDatesBuilder, LocalDate> consumer) {
            this.code = code;
            this.consumer = consumer;
        }

        void set(CustodyRelatedKeyDatesBuilder custodyRelatedKeyDates, LocalDate date) {
            consumer.accept(custodyRelatedKeyDates, date);
        }
        public static List<String> custodyRelatedKeyDates() {
            return Stream
                    .of(values())
                    .map(KeyDateTypes::getCode)
                    .collect(toList());
        }

        static KeyDateTypes of(String code) {
            return Stream
                    .of(values())
                    .filter(keyDate -> keyDate.code.equals(code))
                    .findAny()
                    .orElseThrow();
        }

        public String getCode() {
            return code;
        }
    }

    public ConvictionTransformer() {
    }

    public static Conviction convictionOf(Event event) {
        final var courtAppearances = Optional.ofNullable(event.getCourtAppearances());
        return Conviction.builder()
                .active(event.isActiveFlag())
                .convictionDate(event.getConvictionDate())
                .referralDate(event.getReferralDate())
                .convictionId(event.getEventId())
                .index(event.getEventNumber())
                .offences(offencesOf(event))
                .sentence(Optional.ofNullable(event.getDisposal())
                    .map(disposal -> sentenceOf(disposal, event.getAdditionalSentences()))
                    .orElse(null))
                .custody(Optional
                        .ofNullable(event.getDisposal())
                        .flatMap(disposal -> Optional.ofNullable(disposal.getCustody()).map(ConvictionTransformer::custodyOf))
                        .orElse(null))
                .inBreach(event.isInBreach())
                .latestCourtAppearanceOutcome(courtAppearances.map(ConvictionTransformer::outcomeOf).orElse(null))
                .responsibleCourt(Optional.ofNullable(event.getCourt()).map(CourtTransformer::courtOf).orElse(null))
                .courtAppearance(courtAppearances.map(CourtAppearanceBasicTransformer::latestOrSentencingCourtAppearanceOf).orElse(null))
                .awaitingPsr(courtAppearances.map(CourtAppearanceBasicTransformer::awaitingPsrOf).orElse(false))
                .notes(event.getNotes())
                .build();
    }

    private static AdditionalSentence additionalSentenceOf(uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalSentence additionalSentence) {
        return AdditionalSentence.builder()
            .additionalSentenceId(additionalSentence.getAdditionalSentenceId())
            .type(KeyValueTransformer.keyValueOf(additionalSentence.getAdditionalSentenceType()))
            .amount(additionalSentence.getAmount())
            .length(additionalSentence.getLength())
            .notes(additionalSentence.getNotes())
            .build();
    }

    private static KeyValue outcomeOf(List<CourtAppearance> courtAppearances) {
        return courtAppearances
                .stream()
                .filter(courtAppearance -> courtAppearance.getOutcome() != null).max(Comparator.comparing(CourtAppearance::getAppearanceDate))
                .map(courtAppearance -> outcomeOf(courtAppearance.getOutcome()))
                .orElse(null);
    }
    private static KeyValue outcomeOf(StandardReference outcome) {
        return KeyValue
                .builder()
                .description(outcome.getCodeDescription())
                .code(outcome.getCodeValue())
                .build();
    }


    private static List<Offence> offencesOf(Event event) {
        return ImmutableList.<Offence>builder()
                .addAll(Optional.ofNullable(event.getMainOffence()).map(mainOffence -> ImmutableList.of(OffenceTransformer
                        .offenceOf(mainOffence))).orElse(ImmutableList.of()) )
                .addAll(OffenceTransformer.offencesOf(event.getAdditionalOffences()))
                .build();
    }

    private static Sentence sentenceOf(Disposal disposal, List<uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalSentence> additionalSentences) {
        return Sentence.builder()
                .sentenceId(disposal.getDisposalId())
                .defaultLength(disposal.getLength())
                .effectiveLength(disposal.getEffectiveLength())
                .lengthInDays(disposal.getLengthInDays())
                .originalLength(disposal.getEntryLength())
                .originalLengthUnits(Optional.ofNullable(disposal.getEntryLengthUnits())
                        .map(StandardReference::getCodeDescription)
                        .orElse(null))
                .secondLength(disposal.getLength2())
                .secondLengthUnits(Optional.ofNullable(disposal.getEntryLength2Units())
                        .map(StandardReference::getCodeDescription)
                        .orElse(null))
                .description(Optional.ofNullable(disposal.getDisposalType())
                        .map(DisposalType::getDescription)
                        .orElse(null))
                .unpaidWork(Optional.ofNullable(disposal.getUnpaidWorkDetails())
                        .map(ConvictionTransformer::unpaidWorkOf)
                        .orElse(null))
                .startDate(disposal.getStartDate())
                .terminationDate(disposal.getTerminationDate())
                .terminationReason(Optional.ofNullable(disposal.getTerminationReason())
                        .map(StandardReference::getCodeDescription)
                        .orElse(null))
                .expectedSentenceEndDate(Optional.ofNullable(disposal.getEnteredSentenceEndDate())
                        .orElse(disposal.getExpectedSentenceEndDate()))
                .sentenceType(sentenceTypeOf(disposal.getDisposalType()))
                .additionalSentences(Optional.ofNullable(additionalSentences)
                    .map(x -> x.stream().map(ConvictionTransformer::additionalSentenceOf).collect(toList()))
                    .orElse(null))
                .build();

    }

    private static KeyValue sentenceTypeOf(DisposalType disposalType) {
        return new KeyValue(disposalType.getSentenceType(), disposalType.getDescription());
    }

    private static UnpaidWork unpaidWorkOf(UpwDetails upwDetails) {
        return UnpaidWork.builder()
                .minutesOrdered(upwDetails.getUpwLengthMinutes())
                .minutesCompleted(upwDetails.getAppointments().stream()
                        .filter(l -> Objects.nonNull(l.getMinutesCredited()))
                        .mapToLong(UpwAppointment::getMinutesCredited)
                        .sum()
                )
                .appointments(appointmentsOf(upwDetails.getAppointments()))
                .status(Optional.ofNullable(upwDetails.getStatus())
                    .map(StandardReference::getCodeDescription)
                    .orElse(null))
                .build();
    }

    private static Appointments appointmentsOf(List<UpwAppointment> appointments) {
        return Appointments.builder()
                .total((long) appointments.size())
                .attended(appointments.stream()
                        .filter(upwAppointment -> "Y".equals(upwAppointment.getAttended()))
                        .count())
                .acceptableAbsences(appointments.stream()
                        .filter(upwAppointment -> "N".equals(upwAppointment.getAttended()) && "Y".equals(upwAppointment.getComplied()))
                        .count())
                .unacceptableAbsences(appointments.stream()
                        .filter(upwAppointment -> "N".equals(upwAppointment.getAttended()) && "N".equals(upwAppointment.getComplied()))
                        .count())
                .noOutcomeRecorded(appointments.stream()
                        .filter(upwAppointment -> isNull(upwAppointment.getAttended()) && isNull(upwAppointment.getComplied()))
                        .count())
                .build();
    }

    public static Custody custodyOf(uk.gov.justice.digital.delius.jpa.standard.entity.Custody custody) {
        return Custody.builder().bookingNumber(custody.getPrisonerNumber())
                .institution(Optional.ofNullable(custody.getInstitution()).map(InstitutionTransformer::institutionOf)
                    .orElse(null))
                .status(KeyValueTransformer.keyValueOf(custody.getCustodialStatus()))
                .sentenceStartDate(custody.getDisposal().getStartDate())
                .keyDates(Optional.ofNullable(custody.getKeyDates()).map(ConvictionTransformer::custodyRelatedKeyDatesOf)
                    .orElse(CustodyRelatedKeyDates.builder().build())).build();
    }

    private static CustodyRelatedKeyDates custodyRelatedKeyDatesOf(List<KeyDate> keyDates) {
        final var allCustodyRelatedKeyDates = KeyDateTypes.custodyRelatedKeyDates();
        final var custodyRelatedKeyDates = CustodyRelatedKeyDates.builder();

        keyDates
                .stream()
                .filter(keyDate -> allCustodyRelatedKeyDates.contains(keyDate
                        .getKeyDateType()
                        .getCodeValue()))
                .forEach(keyDate -> {
                    var keyDateType = KeyDateTypes.of(keyDate
                            .getKeyDateType()
                            .getCodeValue());
                    keyDateType.set(custodyRelatedKeyDates, keyDate.getKeyDate());
                });
        return custodyRelatedKeyDates.build();
    }

}
