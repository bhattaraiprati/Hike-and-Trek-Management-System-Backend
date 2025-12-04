package com.example.treksathi.dto.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EsewaStatusResponse {
    @JsonProperty("status")
    private String status;

    @JsonProperty("transaction_uuid")
    private String transactionUuid;

    @JsonProperty("total_amount")
    private String totalAmount;

    @JsonProperty("product_code")
    private String productCode;
}
