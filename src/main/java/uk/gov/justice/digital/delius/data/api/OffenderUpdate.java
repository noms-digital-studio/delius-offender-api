package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OffenderUpdate {
    @ApiModelProperty(value = "Offender ID", example = "232423", position = 1)
    private Long offenderId;
    @ApiModelProperty(value = "The datetime the change occurred", example = "2019-11-27T15:12:43.000Z", position = 2)
    private LocalDateTime dateChanged;
    @ApiModelProperty(value = "Type of delta", example = "UPSERT", allowableValues = "UPSERT,DELETE", position = 3)
    private String action;
    @ApiModelProperty(value = "Offender Delta ID", example = "341256", position = 4)
    private Long offenderDeltaId;
    @ApiModelProperty(value = "Source table", example = "OFFENDER", position = 5)
    private String sourceTable;
    @ApiModelProperty(value = "Record number from source table", example = "13256", position = 6)
    private Long sourceRecordId;
    @ApiModelProperty(value = "Status", example = "CREATED", position = 7)
    private String status;
    @ApiModelProperty(value = "A previously failed update", example = "false", position = 8)
    private boolean failedUpdate;

    public OffenderUpdate setAsFailed() {
        return this.toBuilder().failedUpdate(true).build();
    }

}
