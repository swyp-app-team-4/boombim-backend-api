package boombimapi.domain.congestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Immutable
@Table(name = "official_congestion_demographics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OfficialCongestionDemographics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "official_congestion_id", nullable = false)
    private OfficialCongestion officialCongestion;

    @Column(name = "category", length = 30, nullable = false)
    private String category;

    @Column(name = "sub_category", length = 30, nullable = false)
    private String subCategory;

    @Column(name = "rate", nullable = false)
    private Double rate;

    @Builder
    public OfficialCongestionDemographics(
        OfficialCongestion officialCongestion,
        String category,
        String subCategory,
        Double rate
    ) {
        this.officialCongestion = officialCongestion;
        this.category = category;
        this.subCategory = subCategory;
        this.rate = rate;
    }

    public static OfficialCongestionDemographics of(
        OfficialCongestion officialCongestion,
        String category,
        String subCategory,
        Double rate
    ) {
        return OfficialCongestionDemographics.builder()
            .officialCongestion(officialCongestion)
            .category(category)
            .subCategory(subCategory)
            .rate(rate)
            .build();
    }
}
