package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalSentence {
    @ApiModelProperty(name = "Unique id of this additional sentence", example = "2500000001")
    private Long additionalSentenceId;

    @ApiModelProperty(name = "The type of this additional sentence")
    private KeyValue type;

    @ApiModelProperty(name = "The value associated with this additional sentence without units, explanation is often found in the notes", example = "100")
    private BigDecimal amount;

    @ApiModelProperty(name = "The length of this additional sentence without units, explanation is often found in the notes", example = "14")
    private Long length;

    @ApiModelProperty(name = "Notes about this additional sentence", example = "Some additional sentence notes")
    private String notes;
}
