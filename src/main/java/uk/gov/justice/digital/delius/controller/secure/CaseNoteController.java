package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.service.CaseNoteService;

@RestController
@Slf4j
@Api(description = "Case note resources", tags = "case notes")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_DELIUS_CASE_NOTES')")
@AllArgsConstructor
public class CaseNoteController {

    private final CaseNoteService caseNoteService;

    @RequestMapping(value = "/nomisCaseNotes/{nomisId}/{caseNotesId}", method = RequestMethod.PUT)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 201, message = "Created", response = String.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @ApiOperation(value = "Adds case note to delius")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertCaseNotesToDelius(@PathVariable("nomisId") final String nomisId,
                                        @PathVariable("caseNotesId") final Long caseNotesId,
                                        final @RequestBody String caseNote) {
        log.info("Call to upsertCaseNotesToDelius for noteid {}", caseNotesId);
        caseNoteService.upsertCaseNotesToDelius(nomisId, caseNotesId, caseNote);
    }
}
