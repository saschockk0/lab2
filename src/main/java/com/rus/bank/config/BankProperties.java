package com.rus.bank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank")
public class BankProperties {

    private final Transaction transaction = new Transaction();

    public Transaction getTransaction() {
        return transaction;
    }

    public static class Transaction {
        private int defaultTimeoutSeconds = 30;
        private int maxAmountDigits = 19;

        public int getDefaultTimeoutSeconds() {
            return defaultTimeoutSeconds;
        }

        public void setDefaultTimeoutSeconds(int defaultTimeoutSeconds) {
            this.defaultTimeoutSeconds = defaultTimeoutSeconds;
        }

        public int getMaxAmountDigits() {
            return maxAmountDigits;
        }

        public void setMaxAmountDigits(int maxAmountDigits) {
            this.maxAmountDigits = maxAmountDigits;
        }
    }
}


