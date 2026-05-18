package com.dmx.creditapplication.domain.exception;

public enum ErrorCode {

  VALIDATION_ERROR(400),

  RESOURCE_NOT_FOUND(404),

  BUSINESS_RULE_VIOLATION(422),

  CONFLICT(409),

  INTERNAL_ERROR(500);

  private final int httpStatus;

  ErrorCode(int httpStatus) {
    this.httpStatus = httpStatus;
  }

  public int getHttpStatus() {
    return httpStatus;
  }
}
