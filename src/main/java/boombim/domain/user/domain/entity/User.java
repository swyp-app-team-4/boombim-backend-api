package boombim.domain.user.domain.entity;

import boombim.domain.oauth2.domain.entity.SocialProvider;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@DynamicUpdate
public class User {
    @Id
    @Column(unique = true, nullable = false)
    private String id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false)
    private SocialProvider socialProvider;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Builder
    public User(String id, String email, String name, String profile,
                SocialProvider socialProvider, Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profile = profile;
        this.socialProvider = socialProvider;
        this.role = role;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateEmailAndProfile(String email, String profile) {
        this.email = email;
        this.profile = profile;
    }
}