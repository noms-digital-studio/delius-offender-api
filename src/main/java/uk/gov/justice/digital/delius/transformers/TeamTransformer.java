package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Team;
import uk.gov.justice.digital.delius.jpa.standard.entity.Borough;
import uk.gov.justice.digital.delius.jpa.standard.entity.District;
import uk.gov.justice.digital.delius.jpa.standard.entity.LocalDeliveryUnit;

@Component
public class TeamTransformer {
    public Team teamOf(uk.gov.justice.digital.delius.jpa.standard.entity.Team team) {
        return Team.builder().code(team.getCode()).description(team.getDescription())
                .telephone(team.getTelephone())
                .borough(keyValueOf(team.getDistrict().getBorough()))
                .district(keyValueOf(team.getDistrict()))
                // Delius exposes districts as local delivery units.
                .localDeliveryUnit(keyValueOf(team.getDistrict()))
                // Delius repurposes LDUs to represent a logical grouping of teams which it describes as "team types"
                .teamType(keyValueOf(team.getLocalDeliveryUnit()))
                .build();
    }

    private KeyValue keyValueOf(LocalDeliveryUnit localDeliveryUnit) {
        return KeyValue
            .builder()
            .code(localDeliveryUnit.getCode())
            .description(localDeliveryUnit.getDescription())
            .build();
    }

    private KeyValue keyValueOf(District district) {
        return KeyValue
            .builder()
            .code(district.getCode())
            .description(district.getDescription())
            .build();
    }

    private KeyValue keyValueOf(Borough borough) {
        return KeyValue
            .builder()
            .code(borough.getCode())
            .description(borough.getDescription())
            .build();
    }

}
