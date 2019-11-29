package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderManagerTransformer;
import uk.gov.justice.digital.delius.transformers.OffenderTransformer;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class OffenderService {

    private final OffenderRepository offenderRepository;
    private final OffenderTransformer offenderTransformer;
    private final OffenderManagerTransformer offenderManagerTransformer;
    private final ConvictionService convictionService;

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByOffenderId(Long offenderId) {

        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(offenderId);

        return maybeOffender.map(offenderTransformer::fullOffenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByCrn(String crn) {

        Optional<Offender> maybeOffender = offenderRepository.findByCrn(crn);

        return maybeOffender.map(offenderTransformer::fullOffenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetail> getOffenderByNomsNumber(String nomsNumber) {

        Optional<Offender> maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);

        return maybeOffender.map(offenderTransformer::fullOffenderOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetailSummary> getOffenderSummaryByOffenderId(Long offenderId) {

        Optional<Offender> maybeOffender = offenderRepository.findByOffenderId(offenderId);

        return maybeOffender.map(offenderTransformer::offenderSummaryOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetailSummary> getOffenderSummaryByCrn(String crn) {

        Optional<Offender> maybeOffender = offenderRepository.findByCrn(crn);

        return maybeOffender.map(offenderTransformer::offenderSummaryOf);
    }

    @Transactional(readOnly = true)
    public Optional<OffenderDetailSummary> getOffenderSummaryByNomsNumber(String nomsNumber) {

        Optional<Offender> maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);

        return maybeOffender.map(offenderTransformer::offenderSummaryOf);
    }

    public Optional<String> crnOf(Long offenderId) {
        return offenderRepository.findByOffenderId(offenderId).map(Offender::getCrn);
    }

    public Optional<String> crnOf(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(Offender::getCrn);
    }

    public Optional<Long> offenderIdOfCrn(String crn) {
        return offenderRepository.findByCrn(crn).map(Offender::getOffenderId);
    }

    public Optional<Long> offenderIdOfNomsNumber(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(Offender::getOffenderId);
    }

    public List<BigDecimal> allOffenderIds(int pageSize, int page) {

        int lower = (page * pageSize) - pageSize + 1;
        int upper = page * pageSize;

        List<BigDecimal> offenderIds = offenderRepository.listOffenderIds(lower, upper);

        if (offenderIds == null) {
            log.error("Call to offenderRepository.listOffenderIds {}, {} returned a null list", pageSize, page);
        } else if (offenderIds.contains(null)) {
            log.error("Call to offenderRepository.listOffenderIds {}, {} returned a list containing null", pageSize, page);
        }

        return offenderIds;
    }

    public Long getOffenderCount() {
        return offenderRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<List<OffenderManager>> getOffenderManagersForOffenderId(Long offenderId) {
        return offenderRepository.findByOffenderId(offenderId).map(
                offender -> offenderTransformer.offenderManagersOf(offender.getOffenderManagers()));

    }

    @Transactional(readOnly = true)
    public Optional<List<OffenderManager>> getOffenderManagersForNomsNumber(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(
                offender -> offenderTransformer.offenderManagersOf(offender.getOffenderManagers()));

    }

    @Transactional(readOnly = true)
    public Optional<List<OffenderManager>> getOffenderManagersForCrn(String crn) {
        return offenderRepository.findByCrn(crn).map(
                offender -> offenderTransformer.offenderManagersOf(offender.getOffenderManagers()));

    }

    @Transactional(readOnly = true)
    public Optional<List<ResponsibleOfficer>> getResponsibleOfficersForNomsNumber(String nomsNumber, boolean current) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(
                offender -> offenderTransformer.responsibleOfficersOf(offender, current));

    }

    @Transactional(readOnly = true)
    public Optional<List<CommunityOrPrisonOffenderManager>> getAllOffenderManagersForNomsNumber(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(
                offender -> combine(
                        offender.getOffenderManagers()
                                .stream()
                                .filter(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager::isActive)
                                .map(offenderManagerTransformer::offenderManagerOf)
                                .collect(Collectors.toList()),
                        offender.getPrisonOffenderManagers()
                                .stream()
                                .filter(uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager::isActive)
                                .map(offenderManagerTransformer::offenderManagerOf)
                                .collect(Collectors.toList())
                ) );
    }

     private static <T> List<T> combine(List<T> first, List<T> second) {
        return Stream.of(first, second)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    // TODO DT-337 Flesh out this stub
    @Transactional(readOnly = true)
    public OffenderLatestRecall getOffenderLatestRecall(Long offenderId) {
        Offender offender = offenderRepository.findByOffenderId(offenderId)
                .orElseThrow(() -> new NotFoundException("Offender not found"));
        uk.gov.justice.digital.delius.jpa.standard.entity.Event activeCustodialEvent = convictionService.getActiveCustodialEvent(offender.getOffenderId());
        return null;
    }
}
