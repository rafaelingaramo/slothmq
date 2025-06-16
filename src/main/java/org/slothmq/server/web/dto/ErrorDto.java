package org.slothmq.server.web.dto;

public record ErrorDto(int httpStatus, String error) {

}
