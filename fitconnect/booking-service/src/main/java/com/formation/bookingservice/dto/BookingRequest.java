package com.formation.bookingservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookingRequest {

    @NotNull(message = "userId est obligatoire")
    private Long userId;

    @NotBlank(message = "userEmail est obligatoire")
    @Email(message = "userEmail doit etre une adresse email valide")
    private String userEmail;

    @NotBlank(message = "userName est obligatoire")
    private String userName;

    @NotNull(message = "classId est obligatoire")
    private Long classId;

    @NotNull(message = "numberOfSpots est obligatoire")
    @Min(value = 1, message = "Il faut reserver au moins 1 place")
    @Max(value = 4, message = "4 places maximum par reservation")
    private Integer numberOfSpots;

    public BookingRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Integer getNumberOfSpots() {
        return numberOfSpots;
    }

    public void setNumberOfSpots(Integer numberOfSpots) {
        this.numberOfSpots = numberOfSpots;
    }
}
