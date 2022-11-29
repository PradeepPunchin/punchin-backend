package com.punchin.entity;

import com.punchin.enums.Platform;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "session", indexes = {
        @Index(name = "idx_session_id", columnList = "id"),
        @Index(name = "idx_session_authtoken", columnList = "authToken"),
})
@Data
public class Session{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;

    @ManyToOne
    private User user;

    private String authToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isMobile = false;

    @Size(max = 2000)
    private String deviceId;

    private Long lastActiveTime;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;
}
