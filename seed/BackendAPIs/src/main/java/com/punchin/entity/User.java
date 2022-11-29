package com.punchin.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.punchin.enums.UserStatus;
import com.punchin.utility.UserSequenceIdGenerator;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "users")
public class User extends BasicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "user_id_generator")
    @GenericGenerator(name = "user_id_generator", strategy = "com.punchin.utility.UserSequenceIdGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1"),
            @Parameter(name = UserSequenceIdGenerator.VALUE_PREFIX_PARAMETER, value = ""),
            @Parameter(name = UserSequenceIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%05d") })
    private Long id;

    /** The user name. */
    private String userName;

    /** The first name. */
    @NotNull(message = "{validation.firstname.notnull}")
    private String firstName;

    /** The middle name. */
    private String middleName;

    /** The last name. */
    private String lastName;

    /** The status. */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /** The is account locked. */
    private boolean isAccountLocked;

    /** The password. */
    @JsonIgnore
    private String password;

    /** The email. */
    @Email(message = "{validation.email}")
    private String email;


    /** The roles. */
    @ManyToOne
    private Roles role;
}
