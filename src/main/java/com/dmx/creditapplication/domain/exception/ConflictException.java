package com.dmx.creditapplication.domain.exception;

public class ConflictException extends BusinessException {

  public ConflictException(String message) {
    super(ErrorCode.CONFLICT, message);
  }

}
