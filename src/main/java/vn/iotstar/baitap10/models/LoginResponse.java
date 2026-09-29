package vn.iotstar.baitap10.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginResponse {
    private String token;
    private long expiresIn;

    public String getToken() {
        return token;
    }
}