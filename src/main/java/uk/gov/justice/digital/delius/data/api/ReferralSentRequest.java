package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.OffsetDateTime;

@Data
@With
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReferralSentRequest {

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime sentAt;

    @NotEmpty
    @ApiModelProperty(required = true)
    private String serviceCategory;

    @Positive
    @NotNull
    @ApiModelProperty(required = true)
    private Long sentenceId;

    private String notes;
}
