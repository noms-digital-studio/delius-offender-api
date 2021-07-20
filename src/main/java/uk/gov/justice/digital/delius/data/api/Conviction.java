package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Conviction {
    @ApiModelProperty(name = "Unique id of this conviction", example = "2500000001")
    private Long convictionId;

    @ApiModelProperty(name = "Index of this conviction", example = "1")
    private String index;

    @ApiModelProperty(name = "Active conviction flag", example = "true")
    private Boolean active;

    @ApiModelProperty(name = "Conviction in breach flag", example = "true")
    private Boolean inBreach;

    @ApiModelProperty(name = "Conviction is awaiting pre-sentence report", example = "true")
    private boolean awaitingPsr;

    @ApiModelProperty(name = "Date of this conviction", example = "2021-06-10")
    private LocalDate convictionDate;

    @ApiModelProperty(name = "Referral date of this conviction", example = "2021-06-10")
    private LocalDate referralDate;

    @ApiModelProperty(name = "Main & additional offences that resulted in this conviction")
    private List<Offence> offences;

    @ApiModelProperty(name = "Sentence given with this conviction")
    private Sentence sentence;

    @ApiModelProperty(name = "Outcome of the latest court appearance for this conviction")
    private KeyValue latestCourtAppearanceOutcome;

    @ApiModelProperty(name = "Custody details")
    private Custody custody;

    @ApiModelProperty(name = "Court associated to this conviction")
    private Court responsibleCourt;

    @ApiModelProperty(name = "Sentencing court appearance or the latest court appearance otherwise")
    private CourtAppearanceBasic courtAppearance;

    @ApiModelProperty(name = "Notes about this conviction", example = "Some conviction notes")
    private String notes;
}
