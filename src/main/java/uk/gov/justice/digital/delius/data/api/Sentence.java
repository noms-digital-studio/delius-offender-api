package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sentence {
    private Long sentenceId;
    private String description;
    private Long originalLength;
    private String originalLengthUnits;
    private Long secondLength;
    private String secondLengthUnits;
    private Long defaultLength;
    private Long effectiveLength;
    private Long lengthInDays;
    @ApiModelProperty(value = "The expected end date of the sentence")
    private LocalDate expectedSentenceEndDate;
    @ApiModelProperty(value = "Unpaid Work to date associated with this sentence")
    private UnpaidWork unpaidWork;
    @ApiModelProperty(value = "Date sentence started")
    private LocalDate startDate;
    private LocalDate terminationDate;
    private String terminationReason;
    @ApiModelProperty(value = "Sentence type and description")
    private KeyValue sentenceType;

    @ApiModelProperty(name = "Additional sentences if present")
    private List<AdditionalSentence> additionalSentences;
}
