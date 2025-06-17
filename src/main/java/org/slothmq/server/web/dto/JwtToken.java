package org.slothmq.server.web.dto;

public record JwtToken(String token, long exp) {
}
