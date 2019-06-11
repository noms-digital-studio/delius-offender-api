package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(of = "staffId")
@ToString(exclude = {"offenderManagers", "prisonOffenderManagers"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "STAFF")
public class Staff {

    @Id
    @Column(name = "STAFF_ID")
    private Long staffId;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "FORENAME")
    private String forename;

    @Column(name = "FORENAME2")
    private String forname2;

    @Column(name = "OFFICER_CODE")
    private String officerCode;

    @OneToMany
    @JoinColumn(name = "ALLOCATION_STAFF_ID")
    List<OffenderManager> offenderManagers;

    @OneToMany
    @JoinColumn(name = "ALLOCATION_STAFF_ID")
    List<PrisonOffenderManager> prisonOffenderManagers;
}
