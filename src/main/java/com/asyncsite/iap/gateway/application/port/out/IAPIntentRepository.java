package com.asyncsite.iap.gateway.application.port.out;

import com.asyncsite.iap.gateway.domain.intent.IAPIntent;
import com.asyncsite.iap.gateway.domain.intent.IAPIntentId;
import com.asyncsite.iap.gateway.domain.intent.UserEmail;

import java.util.List;
import java.util.Optional;

/**
 * IAP Intent Repository Port (Output Port)
 */
public interface IAPIntentRepository {

    IAPIntent save(IAPIntent intent);

    Optional<IAPIntent> findById(IAPIntentId intentId);

    List<IAPIntent> findByUserEmail(UserEmail userEmail);

    int expireOldIntents();
}
