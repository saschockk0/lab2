package com.danil.library.security;

public enum SessionStatus {
    ACTIVE,
    ROTATED,   // refresh уже был использован и заменён на новый
    REVOKED,   // принудительно отозван (на будущее)
    EXPIRED
}
