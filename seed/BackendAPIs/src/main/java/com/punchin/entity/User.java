package com.punchin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.punchin.enums.RoleEnum;
import com.punchin.enums.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "users")
@EqualsAndHashCode(callSuper = true)
public class User extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;

    /**
     * The user id.
     */
    @Column(unique = true)
    private String userId;

    /**
     * The first name.
     */
    @NotNull(message = "{validation.firstname.notnull}")
    private String firstName;

    /**
     * The last name.
     */
    private String lastName;

    /**
     * The status.
     */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /**
     * The is account locked.
     */
    private boolean isAccountLocked;

    /**
     * The password.
     */
    @JsonIgnore
    private String password;

    /**
     * The roles.
     */
    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    private String state;
    private String city;
    private String address;
    private String mobileNumber;
    private String profilePicUrl;
    private String aadharCardUrl;
}
