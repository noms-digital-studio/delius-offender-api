package uk.gov.justice.digital.delius.service;

import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Court;
import uk.gov.justice.digital.delius.data.api.NewCourtDto;
import uk.gov.justice.digital.delius.data.api.UpdateCourtDto;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtRepository;
import uk.gov.justice.digital.delius.transformers.CourtTransformer;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CourtService {
    private final CourtRepository courtRepository;
    private final LookupSupplier lookupSupplier;
    private final FeatureSwitches featureSwitches;

    @Transactional
    public Either<BadData, Court> updateCourt(String code, UpdateCourtDto court) {
        final var maybeExistingCourt = getMostLikelyCourt(code);
        if (maybeExistingCourt.isEmpty()) {
            return error(new CourtDoesNotExist(code));
        }

        final var maybeCourtType = lookupSupplier.courtTypeByCode(court.getCourtTypeCode());
        if (maybeCourtType.isEmpty()) {
            return error(new CourtTypeDoesNotExist(court.getCourtTypeCode()));
        }

        final var courtEntity = maybeExistingCourt.get();

        if (isAllowedToUpdate(code)) {
            courtEntity.setBuildingName(court.getBuildingName());
            courtEntity.setCourtName(court.getCourtName());
            courtEntity.setCountry(court.getCountry());
            courtEntity.setCourtType(maybeCourtType.get());
            courtEntity.setCounty(court.getCounty());
            courtEntity.setFax(court.getFax());
            courtEntity.setLocality(court.getLocality());
            courtEntity.setPostcode(court.getPostcode());
            courtEntity.setStreet(court.getStreet());
            courtEntity.setTelephoneNumber(court.getTelephoneNumber());
            courtEntity.setTown(court.getTown());
            courtEntity.setSelectable(court.isActive() ? "Y" : "N");
        } else {
            log.warn("This Court Update feature for {} is currently switched off", code);
        }
        return ok(CourtTransformer.courtOf(courtEntity));
    }

    private boolean isAllowedToUpdate(String code) {
        return code.matches(featureSwitches.getRegisters().getCourtCodeAllowedPattern());
    }

    @Transactional
    public Either<BadData, Court> createNewCourt(NewCourtDto court) {
        final var maybeExistingCourt = getMostLikelyCourt(court.code());
        if (maybeExistingCourt.isPresent()) {
            return error(new CourtAlreadyExists(court.code()));
        }

        final var maybeCourtType = lookupSupplier.courtTypeByCode(court.courtTypeCode());
        if (maybeCourtType.isEmpty()) {
            return error(new CourtTypeDoesNotExist(court.courtTypeCode()));
        }

        final var maybeProbationArea = lookupSupplier.probationAreaByCode(court.probationAreaCode());
        if (maybeProbationArea.isEmpty()) {
            return error(new ProbationDoesNotExist(court.probationAreaCode()));
        }

        final var courtEntity = uk.gov.justice.digital.delius.jpa.standard.entity.Court
            .builder()
            .code(court.code())
            .buildingName(court.buildingName())
            .courtName(court.courtName())
            .country(court.country())
            .courtType(maybeCourtType.get())
            .county(court.county())
            .fax(court.fax())
            .locality(court.locality())
            .postcode(court.postcode())
            .probationArea(maybeProbationArea.get())
            .street(court.street())
            .telephoneNumber(court.telephoneNumber())
            .town(court.town())
            .selectable(court.active() ? "Y" : "N")
            .build();

        if (isAllowedToUpdate(court.code())) {
            courtRepository.save(courtEntity);
        } else {
            log.warn("This Court Creation feature for {} is currently switched off", court.code());
        }
        return ok(CourtTransformer.courtOf(courtEntity));
    }

    @Transactional(readOnly = true)
    public Court getCourt(String code) {
        return getMostLikelyCourt(code).map(CourtTransformer::courtOf).orElseThrow(() -> new NotFoundException(String
            .format("Court %s not found", code)));
    }

    private Optional<uk.gov.justice.digital.delius.jpa.standard.entity.Court> getMostLikelyCourt(String code) {
        // this diabolic code is needed since the Delius test environment has duplicates for a number of courts (court code has no unique constraint)
        // this is not an issue for prod and I guess the UI constrains the code to be unique
        return courtRepository.findByCode(code).stream().reduce((current, mostLikely) -> {
            if (mostLikely == null) {
                return current;
            } else if (current.getSelectable().equals("Y")) {
                return current;
            }
            return mostLikely;
        });
    }

    public List<Court> getCourts() {
        return courtRepository
            .findAll()
            .stream()
            // filter out duplicates (see note above)
            .filter(court -> court.getCourtId().equals(getMostLikelyCourt(court.getCode()).orElseThrow().getCourtId()))
            .map(CourtTransformer::courtOf)
            .toList();
    }

    public interface BadData {
        String message();
    }

    public interface NotFound extends BadData {
    }

    public static record CourtAlreadyExists(String courtCode) implements BadData {
        public String message() {
            return String.format("Court %s already exists", courtCode);
        }
    }

    public static record CourtDoesNotExist(String courtCode) implements NotFound {
        public String message() {
            return String.format("Court %s does not exist", courtCode);
        }
    }

    public static record CourtTypeDoesNotExist(String courtTypeCourt) implements BadData {
        public String message() {
            return String.format("Court type %s does not exist", courtTypeCourt);
        }
    }

    public static record ProbationDoesNotExist(String probationAreaCode) implements BadData {
        public String message() {
            return String.format("Probation area %s does not exist", probationAreaCode);
        }
    }

    static <L, R> Either<L, R> error(L error) {
        return Either.left(error);
    }
    static <L, R> Either<L, R> ok(R value) {
        return Either.right(value);
    }
}
