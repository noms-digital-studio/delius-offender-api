package uk.gov.justice.digital.delius.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateResponse;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.Requirement;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.transformers.AppointmentTransformer;
import uk.gov.justice.digital.delius.utils.DateConverter;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static uk.gov.justice.digital.delius.transformers.AppointmentCreateRequestTransformer.appointmentOf;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalTime;

@Service
public class AppointmentService {

    private final ContactTypeRepository contactTypeRepository;
    private final ContactRepository contactRepository;
    private final RequirementService requirementService;
    private final DeliusApiClient deliusApiClient;
    private final DeliusIntegrationContextConfig deliusIntegrationContextConfig;

    @Autowired
    public AppointmentService(ContactTypeRepository contactTypeRepository,
                              ContactRepository contactRepository,
                              RequirementService requirementService,
                              DeliusApiClient deliusApiClient,
                              DeliusIntegrationContextConfig deliusIntegrationContextConfig) {
        this.contactTypeRepository = contactTypeRepository;
        this.contactRepository = contactRepository;
        this.requirementService = requirementService;
        this.deliusApiClient = deliusApiClient;
        this.deliusIntegrationContextConfig = deliusIntegrationContextConfig;
    }

    public List<Appointment> appointmentsFor(Long offenderId, AppointmentFilter filter) {
        return AppointmentTransformer.appointmentsOf(
                contactRepository.findAll(
                        filter.toBuilder().offenderId(offenderId).build(),
                        Sort.by(DESC, "contactDate")));
    }

    public AppointmentCreateResponse createAppointment(String crn, Long sentenceId, AppointmentCreateRequest request) {
        this.assertAppointmentType(request.getContactType());

        NewContact newContact = makeNewContact(crn, sentenceId, request);
        ContactDto contactDto = deliusApiClient.createNewContract(newContact);

        return makeResponse(contactDto);
    }

    public AppointmentCreateResponse createAppointment(String crn, Long sentenceId, String contextName, ContextlessAppointmentCreateRequest contextualRequest) {

        IntegrationContext context = getContext(contextName);
        Requirement requirement = requirementService.getRequirement(crn, sentenceId, context.getRequirementRehabilitationActivityType());
        AppointmentCreateRequest request = appointmentOf(contextualRequest, requirement.getRequirementId(), context);

        return createAppointment(crn, sentenceId, request);
    }

    private void assertAppointmentType(String contactTypeCode) {
        final var type = this.contactTypeRepository.findByCode(contactTypeCode)
            .orElseThrow(() -> new BadRequestException(String.format("contact type '%s' does not exist", contactTypeCode)));

        if (!type.getAttendanceContact().equals("Y")) {
            throw new BadRequestException(String.format("contact type '%s' is not an appointment type", contactTypeCode));
        }
    }

    private NewContact makeNewContact(String crn, Long sentenceId, AppointmentCreateRequest request) {
        return NewContact.builder()
            .offenderCrn(crn)
            .type(request.getContactType())
            .provider(request.getProviderCode())
            .team(request.getTeamCode())
            .staff(request.getStaffCode())
            .officeLocation(request.getOfficeLocationCode())
            .date(toLondonLocalDate(request.getAppointmentStart()))
            .startTime(toLondonLocalTime(request.getAppointmentStart()))
            .endTime(toLondonLocalTime(request.getAppointmentEnd()))
            .notes(request.getNotes())
            .eventId(sentenceId)
            .requirementId(request.getRequirementId())
            .build();
    }

    private AppointmentCreateResponse makeResponse(ContactDto contactDto) {
        var appointmentStart = DateConverter.toOffsetDateTime(contactDto.getDate().atTime(contactDto.getStartTime()));
        var appointmentEnd = DateConverter.toOffsetDateTime(contactDto.getDate().atTime(contactDto.getEndTime()));
        return new AppointmentCreateResponse(contactDto.getId(), appointmentStart, appointmentEnd, contactDto.getType(), contactDto.getTypeDescription());
    }

    IntegrationContext getContext(String name) {
        var context = deliusIntegrationContextConfig.getIntegrationContexts().get(name);
        return Optional.ofNullable(context).orElseThrow(
            () -> new IllegalArgumentException("IntegrationContext does not exist for: " + name)
        );
    }

}
