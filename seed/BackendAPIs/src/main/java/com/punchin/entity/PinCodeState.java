package com.punchin.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class PinCodeState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;
    private String pinCode;
    private String location;
    private String division;
    private String district;
    private String state;

}
