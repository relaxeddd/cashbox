package ru.cashbox.android.model.auth;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class User {
    private Long id;
    private String username;
    @SerializedName("fullname")
    private String fullName;
    private Boolean activate;
    private String pin;
}
