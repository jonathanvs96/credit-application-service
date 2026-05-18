package com.dmx.creditapplication.domain.exception;

public class ResourceNotFoundException extends BusinessException {
  public ResourceNotFoundException(){
    super(ErrorCode.RESOURCE_NOT_FOUND, "The requested resource was not found");
  }

  public ResourceNotFoundException(String message){
    super(ErrorCode.RESOURCE_NOT_FOUND, message);
  }

  public ResourceNotFoundException(
      String resource,
      Object id) {
    super(
        ErrorCode.RESOURCE_NOT_FOUND,
        "%s with id '%s' was not found"
            .formatted(resource, id)
    );
  }
}

