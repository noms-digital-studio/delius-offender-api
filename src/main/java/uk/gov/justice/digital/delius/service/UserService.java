package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.jpa.national.entity.ProbationArea;
import uk.gov.justice.digital.delius.ldap.repository.LdapRepository;
import uk.gov.justice.digital.delius.ldap.repository.entity.NDeliusUser;
import uk.gov.justice.digital.delius.service.wrapper.UserRepositoryWrapper;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class UserService {
    private final UserRepositoryWrapper userRepositoryWrapper;
    private final LdapRepository ldapRepository;
    private final TelemetryClient telemetryClient;

    @Autowired
    public UserService(final UserRepositoryWrapper userRepositoryWrapper, final LdapRepository ldapRepository, final TelemetryClient telemetryClient) {
        this.userRepositoryWrapper = userRepositoryWrapper;
        this.ldapRepository = ldapRepository;
        this.telemetryClient = telemetryClient;
    }

    @Transactional(readOnly = true)
    public AccessLimitation accessLimitationOf(final String subject, final OffenderDetail offenderDetail) {
        final var accessLimitationBuilder = AccessLimitation.builder();

        if (offenderDetail.getCurrentExclusion() || offenderDetail.getCurrentRestriction()) {
            final var user = userRepositoryWrapper.getUser(subject);

            if (offenderDetail.getCurrentExclusion()) {
                final var userExcluded = user.isExcludedFrom(offenderDetail.getOffenderId());
                accessLimitationBuilder.userExcluded(userExcluded);
                if (userExcluded) {
                    accessLimitationBuilder.exclusionMessage(offenderDetail.getExclusionMessage());
                }
            }

            if (offenderDetail.getCurrentRestriction()) {
                final var userRestricted = !user.isRestrictedUserFor(offenderDetail.getOffenderId());
                accessLimitationBuilder.userRestricted(userRestricted);
                if (userRestricted) {
                    accessLimitationBuilder.restrictionMessage(offenderDetail.getRestrictionMessage());
                }
            }
        }

        return accessLimitationBuilder.build();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Transactional
    public List<User> getUsersList(final String surname, final Optional<String> forename) {

        return forename.map(f -> userRepositoryWrapper.findBySurnameIgnoreCaseAndForenameIgnoreCase(surname, f))
                .orElse(userRepositoryWrapper.findBySurnameIgnoreCase(surname))
                .stream()
                .map(user -> User.builder()
                        .userId(user.getUserId())
                        .distinguishedName(user.getDistinguishedName())
                        .endDate(user.getEndDate())
                        .externalProviderEmployeeFlag(user.getExternalProviderEmployeeFlag())
                        .externalProviderId(user.getExternalProviderId())
                        .forename(user.getForename())
                        .forename2(user.getForename2())
                        .surname(user.getSurname())
                        .organisationId(user.getOrganisationId())
                        .privateFlag(user.getPrivateFlag())
                        .scProviderId(user.getScProviderId())
                        .staffId(user.getStaffId())
                        .probationAreaCodes(probationAreaCodesOf(user.getProbationAreas()))
                        .build()).collect(toList());
    }

    public Map<String, UserDetails> getUserDetailsMap(final Set<String> usernames) {
        Map<String, UserDetails> userDetailsMapByUsername = new HashMap<>();

        usernames.stream().forEach(username-> {
                    NDeliusUser deliusUser = ldapRepository.getDeliusUser(username).get();
                    UserDetails userDetails = UserDetails
                            .builder()
                            .roles(deliusUser.getRoles().stream().map(role -> UserRole.builder().name(role.getCn()).build()).collect(toList()))
                            .firstName(deliusUser.getGivenname())
                            .surname(deliusUser.getSn())
                            .email(deliusUser.getMail())
                            .enabled(deliusUser.isEnabled())
                            .build();
                    userDetailsMapByUsername.put(username, userDetails);
                });
        return userDetailsMapByUsername;
    }

    private List<String> probationAreaCodesOf(final List<ProbationArea> probationAreas) {
        return Optional.ofNullable(probationAreas).map(
                pas -> pas.stream().map(ProbationArea::getCode).collect(toList())).orElse(Collections.emptyList());
    }

    public Optional<UserDetails> getUserDetails(final String username) {
        final var oracleUser = userRepositoryWrapper.getUser(username);
        return ldapRepository.getDeliusUser(username).map(user ->
                UserDetails
                        .builder()
                        .roles(user.getRoles().stream().map(role -> UserRole.builder().name(role.getCn()).build()).collect(toList()))
                        .firstName(user.getGivenname())
                        .surname(user.getSn())
                        .email(user.getMail())
                        .enabled(user.isEnabled())
                        .userId(oracleUser.getUserId())
                        .build());
    }

    public boolean authenticateUser(final String user, final String password) {
        return ldapRepository.authenticateUser(user, password);
    }

    public boolean changePassword(final String username, final String password) {
        return ldapRepository.changePassword(username, password);
    }

    public void addRole(final String username, final String roleId) {
        var allRoles = ldapRepository.getAllRoles();
        if (!allRoles.contains(roleId)) {
            log.info("Could not add role with id: '{}' in {}", roleId, allRoles);
            throw new BadRequestException(String.format("Could not find role with id: '%s'", roleId));
        }
        try {
            ldapRepository.addRole(username, roleId);
        } catch (NameNotFoundException e) {
            throw new NotFoundException(String.format("Could not find user with username: '%s'", username));
        }
        telemetryClient.trackEvent("RoleAssigned", Map.of("username", username, "roleId", roleId), null);
    }
}
