package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "OFFENDER")
public class OffenderPrimaryIdentifiers {
    @Id
    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "CRN")
    private String crn;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "CURRENT_DISPOSAL")
    private Long currentDisposal;

    @OneToMany(mappedBy = "offenderId")
    private List<Event> events;
}
