package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.util.EntityHelper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PersonalContactTransformerTest {
    @Test
    public void transformsPersonalContact() {
        final var source = EntityHelper.aPersonalContact();
        final var observed = PersonalContactTransformer.personalContactOf(source);

        assertThat(observed)
            .usingRecursiveComparison()
            .ignoringFields("gender", "relationshipType", "title")
            .isEqualTo(source);

        assertThat(observed)
            .hasFieldOrPropertyWithValue("gender", source.getGender().getCodeDescription())
            .hasFieldOrPropertyWithValue("relationshipType.code", source.getRelationshipType().getCodeValue())
            .hasFieldOrPropertyWithValue("relationshipType.description", source.getRelationshipType().getCodeDescription())
            .hasFieldOrPropertyWithValue("title", source.getTitle().getCodeDescription());
    }
}
