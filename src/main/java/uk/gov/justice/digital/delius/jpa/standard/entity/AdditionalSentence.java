package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "ADDITIONAL_SENTENCE")
public class AdditionalSentence {
    @Id
    @SequenceGenerator(name = "ADDITIONAL_SENTENCE_SEQ", sequenceName = "ADDITIONAL_SENTENCE_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ADDITIONAL_SENTENCE_SEQ")
    @Column(name = "ADDITIONAL_SENTENCE_ID")
    private Long additionalSentenceId;

    @ManyToOne
    @JoinColumn(name = "ADDITIONAL_SENTENCE_TYPE_ID")
    private StandardReference additionalSentenceType;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "LENGTH")
    private Long length;

    @Column(name = "NOTES")
    @Lob
    private String notes;

    @Column(name = "SOFT_DELETED")
    private boolean softDeleted;

    @Column(name = "EVENT_ID")
    private Long eventID;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;
}
