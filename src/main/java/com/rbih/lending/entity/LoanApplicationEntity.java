package com.rbih.lending.entity;

import com.rbih.lending.domain.enums.ApplicationStatus;
import com.rbih.lending.domain.enums.EmploymentType;
import com.rbih.lending.domain.enums.LoanPurpose;
import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.enums.RiskBand;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "loan_applications")
public class LoanApplicationEntity {

    @Id
    private UUID id;

    private String applicantName;
    private int applicantAge;
    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyIncome;
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;
    private int creditScore;

    @Column(precision = 19, scale = 2)
    private BigDecimal loanAmount;
    private int tenureMonths;
    @Enumerated(EnumType.STRING)
    private LoanPurpose purpose;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    @Enumerated(EnumType.STRING)
    private RiskBand riskBand;
    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;
    @Column(precision = 19, scale = 2)
    private BigDecimal emi;
    @Column(precision = 19, scale = 2)
    private BigDecimal totalPayable;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<RejectionReason> rejectionReasons = new ArrayList<>();

    private Instant createdAt;

    protected LoanApplicationEntity() {
        // required by JPA
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public LoanApplicationEntity(UUID id, String applicantName, int applicantAge, BigDecimal monthlyIncome,
                                 EmploymentType employmentType, int creditScore, BigDecimal loanAmount,
                                 int tenureMonths, LoanPurpose purpose, ApplicationStatus status, RiskBand riskBand,
                                 BigDecimal interestRate, BigDecimal emi, BigDecimal totalPayable,
                                 List<RejectionReason> rejectionReasons, Instant createdAt) {
        this.id = id;
        this.applicantName = applicantName;
        this.applicantAge = applicantAge;
        this.monthlyIncome = monthlyIncome;
        this.employmentType = employmentType;
        this.creditScore = creditScore;
        this.loanAmount = loanAmount;
        this.tenureMonths = tenureMonths;
        this.purpose = purpose;
        this.status = status;
        this.riskBand = riskBand;
        this.interestRate = interestRate;
        this.emi = emi;
        this.totalPayable = totalPayable;
        this.rejectionReasons = rejectionReasons == null ? new ArrayList<>() : new ArrayList<>(rejectionReasons);
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getApplicantName() { return applicantName; }
    public int getApplicantAge() { return applicantAge; }
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public int getCreditScore() { return creditScore; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public int getTenureMonths() { return tenureMonths; }
    public LoanPurpose getPurpose() { return purpose; }
    public ApplicationStatus getStatus() { return status; }
    public RiskBand getRiskBand() { return riskBand; }
    public BigDecimal getInterestRate() { return interestRate; }
    public BigDecimal getEmi() { return emi; }
    public BigDecimal getTotalPayable() { return totalPayable; }
    public List<RejectionReason> getRejectionReasons() { return rejectionReasons; }
    public Instant getCreatedAt() { return createdAt; }
}
