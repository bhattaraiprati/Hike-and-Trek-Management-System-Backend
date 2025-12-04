package com.example.treksathi.record;

public record EsewaPaymentRequest(

         String amount,
         String tax_amount,
         String total_amount,
         String transaction_uuid,
         String product_code,
         String product_service_charge,
         String product_delivery_charge,
         String success_url,
         String failure_url,
         String signed_field_names,
         String signature,
         int eventId
) {
}
