package uk.gov.justice.digital.delius.util;

import com.google.common.collect.ImmutableList;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalSentence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Address;
import uk.gov.justice.digital.delius.jpa.standard.entity.AddressAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.AddressAssessmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.ApprovedPremisesReferral;
import uk.gov.justice.digital.delius.jpa.standard.entity.ApprovedPremisesReferralDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Assessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.AssessmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Borough;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocation;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocationDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceSubType;
import uk.gov.justice.digital.delius.jpa.standard.entity.CircumstanceType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.DisposalType;
import uk.gov.justice.digital.delius.jpa.standard.entity.District;
import uk.gov.justice.digital.delius.jpa.standard.entity.Document;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.EventDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Explanation;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReportDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.LocalDeliveryUnit;
import uk.gov.justice.digital.delius.jpa.standard.entity.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.OfficeLocation;
import uk.gov.justice.digital.delius.jpa.standard.entity.Officer;
import uk.gov.justice.digital.delius.jpa.standard.entity.OrderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.Organisation;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstance;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstanceDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalContact;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalContactDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.RAssessmentType;
import uk.gov.justice.digital.delius.jpa.standard.entity.RCourtReportType;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.entity.RReferralType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Recall;
import uk.gov.justice.digital.delius.jpa.standard.entity.RecallReason;
import uk.gov.justice.digital.delius.jpa.standard.entity.Referral;
import uk.gov.justice.digital.delius.jpa.standard.entity.ReferralDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.Release;
import uk.gov.justice.digital.delius.jpa.standard.entity.ReportManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.entity.UPWAppointmentDocument;
import uk.gov.justice.digital.delius.jpa.standard.entity.UpwAppointment;
import uk.gov.justice.digital.delius.jpa.standard.entity.UpwDetails;
import uk.gov.justice.digital.delius.jpa.standard.entity.UpwProject;
import uk.gov.justice.digital.delius.jpa.standard.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;

@SuppressWarnings("SameParameterValue")
public class EntityHelper {
    public static InstitutionalReportDocument anInstitutionalReportDocument() {
        final var document = new InstitutionalReportDocument();
        populateBasics(document);
        document.setInstitutionalReport(anInstitutionalReport());
        return document;
    }

    public static InstitutionalReportDocument anInstitutionalReportDocument(final Long eventId) {
        final var document = anInstitutionalReportDocument();
        document.getInstitutionalReport().getCustody().getDisposal().getEvent().setEventId(eventId);
        return document;
    }

    private static InstitutionalReport anInstitutionalReport() {
        return InstitutionalReport
                .builder()
                .custody(Custody
                        .builder()
                        .disposal(Disposal
                                .builder()
                                .event(anEvent())
                                .build())
                        .build())
                .dateRequested(LocalDateTime.now())
                .institutionalReportType(StandardReference
                        .builder()
                        .codeValue("PAR")
                        .codeDescription("Parole Assessment Report")
                        .build())
                .institution(anInstitution())
                .build();
    }

    public static RInstitution anInstitution() {
        return RInstitution
                .builder()
                .code("MDIHMP")
                .institutionName("Moorland (HMP & YOI)")
                .description("Moorland (HMP & YOI)")
                .establishment("Y")
                .nomisCdeCode("MDI")
                .build();
    }

    public static Offender anOffenderWithPreviousConvictionsDocument() {
        final var offender = OffenderHelper.anOffender();
        offender.setPrevConvictionDocumentName("precons.pdf");
        offender.setPreviousConvictionDate(LocalDate.now());
        offender.setPreviousConvictionsAlfrescoDocumentId("123");
        offender.setPreviousConvictionsCreatedByUser(User
                .builder()
                .forename("createdforename")
                .surname("createdsurname")
                .build());
        offender.setPreviousConvictionsCreatedDatetime(LocalDateTime.now());
        return offender;
    }

    public static Offender anOffender() {
        return anOffender(List.of(anActiveOffenderManager()), List.of(anActivePrisonOffenderManager()));
    }

    public static Offender anOffender(final List<OffenderManager> offenderManagers, final List<PrisonOffenderManager> prisonOffenderManagers) {
        return OffenderHelper
                .anOffender()
                .toBuilder()
                .offenderManagers(offenderManagers)
                .prisonOffenderManagers(prisonOffenderManagers)
                .build();
    }

    public static Event anEvent() {
        return anEvent(100L);
    }

    public static Event anEvent(final Long eventId) {
        return anEvent(eventId, 777L);
    }

    public static Event anEvent(final Long eventId, final Long offenderId) {
        return Event
                .builder()
                .eventId(eventId)
                .offenderId(offenderId)
                .eventNumber("1")
                .cpsAlfrescoDocumentId("123")
                .cpsCreatedByUser(User
                        .builder()
                        .forename("createdforename")
                        .surname("createdsurname")
                        .build())
                .cpsDocumentName("cps.pdf")
                .cpsDate(LocalDate.now())
                .cpsCreatedDatetime(LocalDateTime.now())
                .cpsSoftDeleted(false)
                .softDeleted(false)
                .activeFlag(true)
                .orderManagers(List.of(anOrderManager()))
                .additionalOffences(List.of())
                .build();
    }

    public static Event aCustodyEvent() {
        return aCustodyEvent(100L, 99L, new ArrayList<>());
    }
    public static Event aCustodyEvent(LocalDate sentenceStartDate) {
        final var event = aCustodyEvent();
        final var disposal = event.getDisposal().toBuilder().startDate(sentenceStartDate).build();
        return event.toBuilder().disposal(disposal).build();
    }

    public static Event aCustodyEvent(final StandardReference custodialStatus) {
        return aCustodyEvent(100L, 99L, new ArrayList<>(), custodialStatus);
    }

    public static Event aCustodyEvent(final Long eventId, final List<KeyDate> keyDates) {
        return aCustodyEvent(eventId, 99L, keyDates);
    }

    public static Event aCustodyEvent(final Long eventId, LocalDate sentenceStartDate) {
        final var event = aCustodyEvent(eventId);
        final var disposal = event.getDisposal().toBuilder().startDate(sentenceStartDate).build();
        return event.toBuilder().disposal(disposal).build();
    }

    public static Event aCustodyEvent(final Long eventId) {
        return aCustodyEvent(eventId, 99L, List.of());
    }

    public static Event aCustodyEvent(final Long eventId, final Long offenderId, final List<KeyDate> keyDates) {
        final var disposal = aDisposal(eventId);
        return anEvent(eventId, offenderId)
                .toBuilder()
                .disposal(aCustodialDisposal(keyDates, disposal, StandardReference.builder().codeValue("D").codeDescription("In Custody").build()))
                .build();
    }

    private static Event aCustodyEvent(final Long eventId, final Long offenderId, final List<KeyDate> keyDates, final StandardReference custodialStatus) {
        final var disposal = aDisposal(eventId);
        return anEvent(eventId, offenderId)
                .toBuilder()
                .disposal(aCustodialDisposal(keyDates, disposal, custodialStatus))
                .build();
    }

    private static Disposal aCustodialDisposal(final List<KeyDate> keyDates, final Disposal disposal, final StandardReference custodialStatus) {
        return disposal
                .toBuilder()
                .disposalType(DisposalType
                        .builder()
                        .sentenceType("NC")
                        .build())
                .startDate(LocalDate.now())
                .custody(aCustody(disposal, keyDates, custodialStatus))
                .build();
    }

    private static Custody aCustody(final Disposal disposal, final List<KeyDate> keyDates, final StandardReference custodialStatus) {
        return Custody
                .builder()
                .disposal(disposal)
                .custodyId(9999L)
                .keyDates(new ArrayList<>(keyDates))
                .institution(aPrisonInstitution())
                .custodialStatus(custodialStatus)
                .build();
    }

    public static OffenderDocument anOffenderDocument() {
        final var offenderDocument = new OffenderDocument();

        populateBasics(offenderDocument);
        return offenderDocument;
    }

    public static CourtReportDocument aCourtReportDocument() {
        final var document = new CourtReportDocument();
        populateBasics(document);
        document.setCourtReport(aCourtReport());
        return document;
    }

    public static CourtReportDocument aCourtReportDocument(final Long eventId) {
        final var document = aCourtReportDocument();
        document.getCourtReport().getCourtAppearance().getEvent().setEventId(eventId);
        return document;
    }

    public static Court aCourt(final String code) {
        return Court.builder().code(code).courtId(99L).courtName("Sheffield Crown Court").build();
    }

    public static CourtReport aCourtReport(LocalDateTime requestedDate, LocalDateTime requiredDate, LocalDateTime completedDate, RCourtReportType courtReportType, List<ReportManager> reportManagers) {
        return CourtReport
            .builder()
            .courtReportId(1L)
            .offenderId(1L)
            .dateRequested(requestedDate)
            .dateRequired(requiredDate)
            .completedDate(completedDate)
            .allocationDate(LocalDateTime.now())
            .sentToCourtDate(LocalDateTime.now())
            .receivedByCourtDate(LocalDateTime.now())
            .courtReportType(courtReportType)
            .courtAppearance(CourtAppearance
                .builder()
                .court(Court
                    .builder()
                    .courtName("Sheffield Magistrates Court")
                    .build())
                .event(Event
                    .builder()
                    .eventId(1L)
                    .build())
                .build())
            .reportManagers(reportManagers)
            .build();
    }

    public static CourtReport aCourtReport(LocalDateTime requestedDate, LocalDateTime requiredDate, LocalDateTime completedDate, RCourtReportType courtReportType) {
        return aCourtReport(requestedDate, requiredDate, completedDate, courtReportType, List.of(aReportManager(true)));
    }

    private static CourtReport aCourtReport() {
        final var courtReportType = RCourtReportType
                                        .builder()
                                        .description("Pre-Sentence Report - Standard")
                                        .code("CJS")
                                        .build();
        return aCourtReport(LocalDateTime.now(), LocalDateTime.now().plusDays(1), null, courtReportType);
    }

    public static ReportManager aReportManager(final boolean active) {
        return ReportManager.builder()
            .active(active)
            .staff(aStaff())
            .build();
    }

    private static void populateBasics(final Document document) {
        document.setAlfrescoId("123");
        document.setCreatedByProbationAreaId(1L);
        document.setCreatedByUser(User
                .builder()
                .forename("createdforename")
                .surname("createdsurname")
                .build());
        document.setCreatedDate(LocalDateTime.now());
        document.setLastUpdatedByUser(User
                .builder()
                .forename("updatedforename")
                .surname("updatedsurname")
                .build());
        document.setDocumentName("DocumentName.pdf");
        document.setLastSaved(LocalDateTime.now());
        document.setPrimaryKeyId(100L);
    }

    public static EventDocument anEventDocument(final Long eventId) {
        final var document = new EventDocument();
        populateBasics(document);
        final var event = anEvent();
        event.setEventId(eventId);
        document.setEvent(event);
        return document;
    }

    public static AddressAssessmentDocument anAddressAssessmentDocument() {
        final var document = new AddressAssessmentDocument();
        populateBasics(document);
        document.setAddressAssessment(anAddressAssessment());
        return document;
    }

    private static AddressAssessment anAddressAssessment() {
        return AddressAssessment
                .builder()
                .assessmentDate(LocalDateTime.now())
                .build();
    }

    public static ApprovedPremisesReferralDocument anApprovedPremisesReferralDocument(final Long eventId) {
        final var document = new ApprovedPremisesReferralDocument();
        populateBasics(document);
        document.setApprovedPremisesReferral(anApprovedPremisesReferral(eventId));
        return document;
    }

    private static ApprovedPremisesReferral anApprovedPremisesReferral(final Long eventId) {
        return ApprovedPremisesReferral
                .builder()
                .referralDate(LocalDateTime.now())
                .event(anEvent(eventId))
                .build();
    }

    public static AssessmentDocument anAssessmentDocument(final Long eventId) {
        final var document = new AssessmentDocument();
        populateBasics(document);
        document.setAssessment(anAssessment(eventId));
        return document;
    }

    private static Assessment anAssessment(final Long eventId) {
        return Assessment
                .builder()
                .assessmentDate(LocalDateTime.now())
                .assessmentType(RAssessmentType
                        .builder()
                        .description("Drug testing")
                        .build())
                .referral(aReferral(eventId))
                .build();
    }

    public static CaseAllocationDocument aCaseAllocationDocument(final Long eventId) {
        final var document = new CaseAllocationDocument();
        populateBasics(document);
        document.setCaseAllocation(aCaseAllocation(eventId));
        return document;
    }

    private static CaseAllocation aCaseAllocation(final Long eventId) {
        return CaseAllocation
                .builder()
                .event(anEvent(eventId))
                .build();
    }

    public static Address anAddress() {
        return Address.builder()
            .addressId(1000L)
            .addressNumber("32")
            .buildingName("HMPPS Digital Studio")
            .streetName("Scotland Street")
            .district("Sheffield City Centre")
            .townCity("Sheffield")
            .county("South Yorkshire")
            .postcode("S3 7BS")
            .telephoneNumber("0123456789")
            .createdDatetime(LocalDateTime.of(2021, 6, 10, 13, 0))
            .lastUpdatedDatetime(LocalDateTime.of(2021, 6, 10, 14, 0))
            .softDeleted(0L)
            .build();
    }

    public static PersonalContact aPersonalContact() {
        return PersonalContact.builder()
            .personalContactId(2500058493L)
            .relationship("Father")
            .startDate(LocalDateTime.of(2019, 9, 13, 0, 0))
            .endDate(LocalDateTime.of(2020, 9, 13, 0, 0))
            .firstName("Smile")
            .otherNames("Danger")
            .surname("Barry")
            .previousSurname("Steve")
            .mobileNumber("0123456789")
            .emailAddress("example@example.com")
            .notes("Some personal contact notes")
            .relationshipType(aStandardReference("RT01", "Drug Worker"))
            .createdDatetime(LocalDateTime.now())
            .lastUpdatedDatetime(LocalDateTime.now())
            .title(aStandardReference("LDY", "Lady"))
            .gender(aStandardReference("F", "Female"))
            .address(anAddress())
            .build();
    }

    public static PersonalContactDocument aPersonalContactDocument() {
        final var document = new PersonalContactDocument();
        populateBasics(document);
        document.setPersonalContact(aPersonalContact());
        return document;
    }

    public static ReferralDocument aReferralDocument(final Long eventId) {
        final var document = new ReferralDocument();
        populateBasics(document);
        document.setReferral(aReferral(eventId));
        return document;
    }

    private static Referral aReferral(final Long eventId) {
        return Referral
                .builder()
                .event(anEvent(eventId))
                .referralDate(LocalDateTime.now())
                .referralType(RReferralType
                        .builder()
                        .description("Mental Health")
                        .build())
                .build();
    }

    public static NsiDocument aNsiDocument(final Long eventId) {
        final var document = new NsiDocument();
        populateBasics(document);
        document.setNsi(aNsi(eventId));
        return document;
    }

    public static NsiDocument aNsiDocument() {
        final var document = new NsiDocument();
        populateBasics(document);
        document.setNsi(aNsi());
        return document;
    }


    private static Nsi aNsi(final Long eventId) {
        return aNsi()
                .toBuilder()
                .event(anEvent(eventId))
                .build();
    }

    public static Requirement aRarRequirement() {
        return Requirement.builder()
            .requirementId(1000L)
            .requirementTypeMainCategory(RequirementTypeMainCategory.builder()
                .code(RequirementTypeMainCategory.REHABILITATION_ACTIVITY_REQUIREMENT_CODE)
                .description("Rehabilitation activity")
                .build())
            .activeFlag(true)
            .softDeleted(false)
            .build();
    }

    public static Nsi aNsi() {
        return Nsi
                .builder()
                .nsiType(NsiType
                        .builder()
                        .description("Custody - Accredited Programme")
                        .build())
                .nsiSubType(StandardReference
                        .builder()
                        .codeDescription("Healthy Sex Programme (HCP)")
                        .build())
                .referralDate(LocalDate.now())
                .nsiManagers(List.of(aNsiManager()))
                .softDeleted(0L)
                .build();
    }

    public static PersonalCircumstanceDocument aPersonalCircumstanceDocument() {
        final var document = new PersonalCircumstanceDocument();
        populateBasics(document);
        document.setPersonalCircumstance(aPersonalCircumstance());
        return document;
    }

    public static PersonalCircumstance aPersonalCircumstance() {
        return PersonalCircumstance.builder()
            .personalCircumstanceId(1000L)
            .offenderId(1001L)
            .notes("Some notes")
            .evidenced("Y")
            .startDate(LocalDate.of(2021, 7, 9))
            .endDate(LocalDate.of(2021, 7, 10))
            .circumstanceType(CircumstanceType.builder().codeValue("CT").codeDescription("AP - Medication in Posession - Assessment").build())
            .circumstanceSubType(CircumstanceSubType.builder().codeValue("CST").codeDescription("MiP approved").build())
            .createdDatetime(LocalDateTime.of(2021, 7, 9, 9, 12))
            .lastUpdatedDatetime(LocalDateTime.of(2021, 7, 9, 9, 32))
            .build();
    }

    public static UPWAppointmentDocument aUPWAppointmentDocument(final Long eventId) {
        final var document = new UPWAppointmentDocument();
        populateBasics(document);
        document.setUpwAppointment(upwAppointment(eventId));
        return document;
    }


    private static UpwAppointment upwAppointment(final Long eventId) {
        return UpwAppointment
                .builder()
                .appointmentDate(LocalDateTime.now())
                .upwDetails(UpwDetails
                        .builder()
                        .disposal(aDisposal(eventId))
                        .build())
                .upwProject(UpwProject
                        .builder()
                        .name("Grass cutting")
                        .build())
                .build();
    }

    private static Disposal aDisposal(final Long eventId) {
        return Disposal
                .builder()
                .disposalType(
                        DisposalType
                                .builder()
                                .sentenceType("SC")
                                .build())
                .event(anEvent(eventId))
                .startDate(LocalDate.now())
                .build();
    }

    public static ContactDocument aContactDocument(final Long eventId) {
        final var document = new ContactDocument();
        populateBasics(document);
        document.setContact(aContact(eventId));
        return document;
    }

    public static ContactDocument aContactDocument() {
        final var document = new ContactDocument();
        populateBasics(document);
        document.setContact(aContact());
        return document;
    }

    public static Contact aContact(final Long eventId) {
        return Contact
                .builder()
                .event(anEvent(eventId))
                .contactDate(LocalDate.now())
                .contactStartTime(LocalTime.now())
                .contactType(ContactType
                        .builder()
                        .description("Case Conference - MAPPA")
                        .build())
                .build();
    }

    public static Contact aContact() {
        return Contact
                .builder()
                .contactId(1L)
                .contactDate(LocalDate.now())
                .contactStartTime(LocalTime.now())
                .contactOutcomeType(aContactOutcomeType())
                .contactType(aContactType())
                .explanation(anExplanation())
                .probationArea(aProbationArea())
                .team(aTeam())
                .staff(aStaff())
                .build();

    }

    public static KeyDate aKeyDate(final String typeCode, final String description, final LocalDate date) {
        return KeyDate
                .builder()
                .keyDate(date)
                .keyDateType(StandardReference
                        .builder()
                        .codeDescription(description)
                        .codeValue(typeCode)
                        .build())
                .build();
    }

    public static KeyDate aKeyDate(final Long keyDateId, final String typeCode) {
        return KeyDate
                .builder()
                .keyDateId(keyDateId)
                .keyDate(LocalDate.now())
                .keyDateType(StandardReference
                        .builder()
                        .codeDescription("description")
                        .codeValue(typeCode)
                        .build())
                .build();
    }

    public static KeyDate aKeyDate(final String typeCode, final String typeDescription) {
        return KeyDate
                .builder()
                .keyDateId(99L)
                .keyDate(LocalDate.now())
                .keyDateType(StandardReference
                        .builder()
                        .codeDescription(typeDescription)
                        .codeValue(typeCode)
                        .build())
                .build();
    }

    public static Staff aStaff() {
        return aStaff("A1234");
    }

    public static Staff aStaff(final String officerCode) {
        return Staff
                .builder()
                .user(aUser())
                .officerCode(officerCode)
                .forename("John")
                .surname("Smith")
                .teams(ImmutableList.of())
                .probationArea(aProbationArea())
                .build();
    }

    public static OfficeLocation anOfficeLocation() {
        return OfficeLocation
                .builder()
                .code("ASP_ASH")
                .description("Ashley House Approved Premises")
                .buildingName("Ashley House")
                .buildingNumber("14")
                .streetName("Somerset Street")
                .townCity("Bristol")
                .county("Somerset")
                .postcode("BS2 8NB")
                .build();
    }

    public static Team aTeam(final String teamCode) {
        return Team
                .builder()
                .code(teamCode)
                .description("Team 1")
                .district(aDistrict())
                .localDeliveryUnit(LocalDeliveryUnit.builder()
                        .code("LL")
                        .description("LDU description")
                        .build())
                .build();
    }

    public static Team aTeam() {
        return aTeam("TEAM-1");
    }

    public static Borough aBorough() {
        return aBorough("BB");
    }

    public static Borough aBorough(final String code) {
        return Borough.builder()
                .code(code)
                .description("Borough description")
                .build();
    }

    public static District aDistrict() {
        return District.builder()
                .code("XX")
                .description("DD description")
                .borough(aBorough())
                .build();
    }

    public static LocalDeliveryUnit aLocalDeliveryUnit() {
        return aLocalDeliveryUnit("LDU");
    }

    public static LocalDeliveryUnit aLocalDeliveryUnit(final String code) {
        return LocalDeliveryUnit
                .builder()
                .code(code)
                .description("LDU description")
                .build();
    }

    public static User aUser() {
        return User.builder()
                .distinguishedName("XX")
                .build();
    }

    public static OffenderManager anActiveOffenderManager() {
        return anActiveOffenderManager("AA");
    }

    public static OffenderManager anActiveOffenderManager(final String staffCode) {
        return anOffenderManager(aStaff(staffCode), aTeam())
                .toBuilder()
                .activeFlag(1L)
                .endDate(null)
                .responsibleOfficers(mutableListOf(aResponsibleOfficer()))
                .probationArea(aPrisonProbationArea())
                .build();
    }

    static <E> List<E> mutableListOf(E ... e1) {
        return new ArrayList<>(Arrays.asList(e1));
    }

    public static OffenderManager anInactiveOffenderManager(final String staffCode) {
        return anOffenderManager(aStaff(staffCode), aTeam())
                .toBuilder()
                .activeFlag(0L)
                .endDate(LocalDate.now())
                .build();
    }

    public static OffenderManager anEndDatedActiveOffenderManager(final String staffCode) {
        return anOffenderManager(aStaff(staffCode), aTeam())
                .toBuilder()
                .activeFlag(1L)
                .endDate(LocalDate.now())
                .build();
    }

    public static OffenderManager anOffenderManager(final Staff staff, final Team team) {
        return OffenderManager.builder()
                .activeFlag(1L)
                .allocationDate(LocalDate.now())
                .officer(Officer.builder().surname("Jones").build())
                .probationArea(ProbationArea.builder().code("A").description("B").privateSector(1L).build())
                .staff(staff)
                .team(team)
                .allocationDate(LocalDate.now())
                .allocationReason(StandardReference
                        .builder()
                        .codeValue("AUT")
                        .codeDescription("Automatic allocation")
                        .build())
                .probationArea(aProbationArea())
                .emailAddress("no-one@nowhere.com")
                .telephoneNumber("020 1111 2222")
                .build();
    }

    public static PrisonOffenderManager anActivePrisonOffenderManager() {
        return anActivePrisonOffenderManager("AA");
    }

    public static PrisonOffenderManager anActivePrisonOffenderManager(final String staffCode) {
        return aPrisonOffenderManager(aStaff(staffCode), aTeam())
                .toBuilder()
                .activeFlag(1L)
                .endDate(null)
                .responsibleOfficers(mutableListOf(aResponsibleOfficer()))
                .build();
    }

    public static PrisonOffenderManager anInactivePrisonOffenderManager(final String staffCode) {
        return aPrisonOffenderManager(aStaff(staffCode), aTeam())
                .toBuilder()
                .activeFlag(0L)
                .endDate(LocalDate.now())
                .build();
    }

    public static PrisonOffenderManager anEndDatedActivePrisonOffenderManager(final String staffCode) {
        return aPrisonOffenderManager(aStaff(staffCode), aTeam())
                .toBuilder()
                .activeFlag(1L)
                .endDate(LocalDate.now())
                .build();
    }

    public static PrisonOffenderManager aPrisonOffenderManager(final Staff staff, final Team team) {
        return PrisonOffenderManager.builder()
                .activeFlag(1L)
                .allocationDate(LocalDate.now())
                .allocationReason(StandardReference
                        .builder()
                        .codeValue("AUT")
                        .codeDescription("Automatic allocation")
                        .build())
                .probationArea(ProbationArea.builder().code("A").description("B").privateSector(1L).build())
                .staff(staff)
                .team(team)
                .probationArea(aPrisonProbationArea())
                .build();
    }

    public static ProbationArea aProbationArea() {
        return ProbationArea
                .builder()
                .code("NO2")
                .probationAreaId(1L)
                .description("NPS North East")
                .privateSector(0L)
                .organisation(Organisation.builder().build())
                .providerTeams(new ArrayList<>())
                .teams(new ArrayList<>(asList(aTeam())))
                .build();
    }

    public static ProbationArea aPrisonProbationArea() {
        return aProbationArea()
                .toBuilder()
                .code("WWI")
                .probationAreaId(1L)
                .description("HMP Wandsworth")
                .institution(aPrisonInstitution())
                .teams(new ArrayList<>(asList(aTeam())))
                .build();
    }

    public static RInstitution aPrisonInstitution() {
        return RInstitution
                .builder()
                .institutionName("HMP Wandsworth")
                .establishment("Yes")
                .privateFlag(0L)
                .code("WWIHMP")
                .nomisCdeCode("WWI")
                .description("HMP Wandsworth")
                .build();
    }

    public static ResponsibleOfficer aResponsibleOfficer() {
        return ResponsibleOfficer
                .builder()
                .offenderId(1L)
                .startDateTime(LocalDateTime.now())
                .responsibleOfficerId(1L)
                .build();
    }

    public static ContactType aContactType() {
        return ContactType
                .builder()
                .code("EPOMEX")
                .description("Prison Offender Manager - External Transfer")
                .alertFlag("N")
                .cjaOrderLevel("Y")
                .legacyOrderLevel("Y")
                .build();
    }

    public static OrderManager anOrderManager() {
        return OrderManager
                .builder()
                .team(aTeam())
                .staff(aStaff())
                .activeFlag(1L)
                .probationArea(aProbationArea())
                .build();
    }

    public static Release aRelease() {
        return Release
                .builder()
                .actualReleaseDate(LocalDateTime.now())
                .institution(EntityHelper.aPrisonInstitution())
                .releaseId(99L)
                .releaseType(StandardReference.builder().build())
                .notes("Released for geed behaviour")
                .softDeleted(0L)
                .build();
    }

    public static Recall aRecall() {
        return Recall
                .builder()
                .notes("Naughty")
                .reason(RecallReason.builder().build())
                .recallDate(LocalDateTime.now())
                .releaseId(99L)
                .softDeleted(0L)
                .build();
    }

    public static NsiManager aNsiManager() {
        return NsiManager
                .builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .probationArea(aProbationArea())
                .staff(aStaff())
                .team(aTeam())
                .nsiManagerId(99L)
                .build();
    }

    public static ContactOutcomeType aContactOutcomeType() {
        return ContactOutcomeType.builder()
            .contactOutcomeTypeId(100L)
            .code("CO1")
            .description("Some contact outcome type")
            .build();
    }

    public static CourtAppearance aCourtAppearanceWithOutcome(String outcomeCode, String outcomeDescription) {
        return CourtAppearance.builder()
            .courtAppearanceId(1L)
            .appearanceDate(LocalDateTime.of(2015, 6, 10, 12, 0))
            .outcome(aStandardReference(outcomeCode, outcomeDescription))
            .court(aCourt("some-court"))
            .offender(anOffender())
            .build();
    }

    public static CourtAppearance aCourtAppearanceWithOutOutcome() {
        return CourtAppearance.builder()
            .courtAppearanceId(2L)
            .appearanceDate(LocalDateTime.of(2015, 6, 11, 12, 0))
            .court(aCourt("some-court"))
            .offender(anOffender())
            .build();
    }

    public static StandardReference aStandardReference(String codeValue, String codeDescription) {
        return StandardReference.builder()
            .codeValue(codeValue)
            .codeDescription(codeDescription)
            .build();
    }

    public static AdditionalSentence anAdditionalSentence() {
        return AdditionalSentence.builder()
            .additionalSentenceId(100L)
            .additionalSentenceType(aStandardReference("DISQ", "Disqualified from Driving"))
            .amount(BigDecimal.valueOf(100))
            .length(6L)
            .notes("Additional Sentence 1")
            .softDeleted(false)
            .eventID(200L)
            .offenderId(300L)
            .build();
    }

    private static Explanation anExplanation() {
        return Explanation
            .builder()
            .explanationId(101L)
            .code("E1")
            .description("Some explanation")
            .build();
    }
}
