package boombim.domain.user.domain.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // 이름
    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String profile;


    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Builder
    public User(String id, String email, String name,
                String profile, Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profile = profile;
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
