package com.thv.sport.system.dto.request.order;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String recipient;
    private String phoneNumber;
    private String locationDetail;
    @Email(message = "Invalid email")
    private String email;
    private String note;
    private String promotion;
    //true: COD, false: Stripe
    private Boolean checkoutType;
}
