package com.banking.api.services;

import com.banking.api.models.Currency;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;

public class CurrencyConverterService {


    private final static Map<Pair<Currency, Currency>, BigDecimal> conversionRates =
            Collections.unmodifiableMap(new HashMap<Pair<Currency, Currency>, BigDecimal>() {{
                put(new Pair<>(Currency.USD, Currency.EUR), BigDecimal.valueOf(0.90));
                put(new Pair<>(Currency.USD, Currency.USD), BigDecimal.valueOf(1D));
                put(new Pair<>(Currency.EUR, Currency.EUR), BigDecimal.valueOf(1D));
                put(new Pair<>(Currency.EUR, Currency.USD), BigDecimal.valueOf(1.11));
            }});

    public BigDecimal exchange(BigDecimal amount, Currency amountCurrency, Currency targetCurrency) {
        return amount.multiply(conversionRates.get(new Pair<>(amountCurrency, targetCurrency)));
    }
}
