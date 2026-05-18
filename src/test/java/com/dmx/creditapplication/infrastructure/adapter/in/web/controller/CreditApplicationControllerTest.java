package com.dmx.creditapplication.infrastructure.adapter.in.web.controller;

import com.dmx.creditapplication.domain.exception.ConflictException;
import com.dmx.creditapplication.domain.exception.ResourceNotFoundException;
import com.dmx.creditapplication.domain.model.entity.CreditApplication;
import com.dmx.creditapplication.domain.model.enums.CreditStatus;
import com.dmx.creditapplication.domain.model.enums.Currency;
import com.dmx.creditapplication.domain.model.pagination.PageResult;
import com.dmx.creditapplication.domain.port.in.command.ChangeCreditApplicationStatusCommand;
import com.dmx.creditapplication.domain.port.in.command.CreateCreditApplicationCommand;
import com.dmx.creditapplication.domain.port.in.command.SearchCreditApplicationsQuery;
import com.dmx.creditapplication.domain.port.in.usecases.ChangeCreditApplicationStatusUseCase;
import com.dmx.creditapplication.domain.port.in.usecases.CreateCreditApplicationUseCase;
import com.dmx.creditapplication.domain.port.in.usecases.GetCreditApplicationUseCase;
import com.dmx.creditapplication.domain.port.in.usecases.SearchCreditApplicationsUseCase;
import com.dmx.creditapplication.infrastructure.adapter.in.web.dto.*;
import com.dmx.creditapplication.infrastructure.adapter.in.web.mapper.CreditApplicationWebMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CreditApplicationController.class)
public class CreditApplicationControllerTest {

  @MockitoBean
  private CreateCreditApplicationUseCase createUseCase;

  @MockitoBean
  private GetCreditApplicationUseCase getUseCase;

  @MockitoBean
  private SearchCreditApplicationsUseCase searchUseCase;

  @MockitoBean
  private ChangeCreditApplicationStatusUseCase changeStatusUseCase;

  @MockitoBean
  private CreditApplicationWebMapper mapper;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Nested
  class CreateCreditApplication {

    @Nested
    class Validation {

      @Test
      void should_return_bad_request_when_rfc_is_invalid() throws Exception {

        // Arrange
        CreateCreditApplicationRequest request =
            new CreateCreditApplicationRequest()
                .customerName("Juan Perez")
                .customerEmail("juan@test.com")
                .customerRfc("RFC_INVALIDO_MUY_LARGO")
                .requestedAmount(1000.0)
                .currency(Currencies.MXN)
                .termMonths(12)
                .annualInterestRate(0.12);

        // Act & Assert
        mockMvc.perform(post("/credit-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(createUseCase);
      }

      @Test
      void should_return_bad_request_when_requested_amount_is_less_than_minimum()
          throws Exception {

        // Arrange
        CreateCreditApplicationRequest request =
            new CreateCreditApplicationRequest()
                .customerName("Juan Perez")
                .customerEmail("juan@test.com")
                .customerRfc("XAXX010101000")
                .requestedAmount(0.0)
                .currency(Currencies.MXN)
                .termMonths(12)
                .annualInterestRate(0.12);

        // Act & Assert
        mockMvc.perform(post("/credit-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(createUseCase);
      }

      @Test
      void should_return_bad_request_when_term_months_is_less_than_minimum()
          throws Exception {

        // Arrange
        CreateCreditApplicationRequest request =
            new CreateCreditApplicationRequest()
                .customerName("Juan Perez")
                .customerEmail("juan@test.com")
                .customerRfc("XAXX010101000")
                .requestedAmount(1000.0)
                .currency(Currencies.MXN)
                .termMonths(1)
                .annualInterestRate(0.12);

        // Act & Assert
        mockMvc.perform(post("/credit-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(createUseCase);
      }

    }

    @Test
    void should_create_credit_application() throws Exception {
      // Arrange
      UUID creditId = UUID.randomUUID();

      CreateCreditApplicationRequest request =
          new CreateCreditApplicationRequest()
              .customerName("Juan Perez")
              .customerEmail("juan@test.com")
              .customerRfc("XAXX010101000")
              .requestedAmount(1000.0)
              .currency(Currencies.MXN)
              .termMonths(12)
              .annualInterestRate(0.12);

      CreateCreditApplicationCommand command =
          new CreateCreditApplicationCommand(
              "Juan Perez",
              "juan@test.com",
              "XAXX010101000",
              new BigDecimal("1000.0"),
              Currency.MXN,
              12,
              new BigDecimal("0.12")
          );

      CreditApplication credit =CreditApplication.restore(
          creditId,
          "Juan Perez",
          "juan@test.com",
          "XAXX010101000",
          new BigDecimal("1000.0"),
          Currency.MXN,
          12,
          new BigDecimal("0.12"),
          null,
          null,
          null,
          null,
          null,
          CreditStatus.CREATED,
          null,
          null,
          null
      );

      CreditApplicationResponse response =
          new CreditApplicationResponse();

      response.setId(creditId);

      when(mapper.toCommand(any(CreateCreditApplicationRequest.class)))
          .thenReturn(command);

      when(createUseCase.create(command))
          .thenReturn(credit);

      when(mapper.toResponse(credit))
          .thenReturn(response);

      // Act & Assert
      mockMvc.perform(post("/credit-applications")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(header().string(
              "Location",
              "http://localhost/credit-applications/" + creditId
          ));

      verify(mapper, times(1))
          .toCommand(any(CreateCreditApplicationRequest.class));

      verify(createUseCase, times(1))
          .create(command);

      verify(mapper, times(1))
          .toResponse(credit);
    }
  }

  @Nested
  class Get {
    @Test
    void should_return_credit_application_when_exists() throws Exception {
      // Arrange
      UUID creditId = UUID.randomUUID();

      CreditApplication credit =
          CreditApplication.restore(
              creditId,
              "Juan Perez",
              "juan@test.com",
              "XAXX010101000",
              new BigDecimal("1000.0"),
              Currency.MXN,
              12,
              new BigDecimal("0.12"),
              null,
              null,
              null,
              null,
              null,
              CreditStatus.CREATED,
              null,
              null,
              null
          );

      CreditApplicationResponse response =
          new CreditApplicationResponse()
          .id(creditId);

      when(getUseCase.get(creditId))
          .thenReturn(credit);

      when(mapper.toResponse(credit))
          .thenReturn(response);

      // Act & Assert
      mockMvc.perform(get("/credit-applications/{id}", creditId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id")
              .value(creditId.toString()));

      verify(getUseCase, times(1))
          .get(creditId);

      verify(mapper, times(1))
          .toResponse(credit);
    }

    @Test
    void should_return_not_found_when_credit_application_does_not_exist()
        throws Exception {

      // Arrange
      UUID creditId = UUID.randomUUID();

      when(getUseCase.get(creditId))
          .thenThrow(
              new ResourceNotFoundException(
                  "CreditApplication",
                  creditId
              )
          );

      // Act & Assert
      mockMvc.perform(get("/credit-applications/{id}", creditId))
          .andExpect(status().isNotFound());

      verify(getUseCase, times(1))
          .get(creditId);

      verifyNoInteractions(mapper);
    }
  }

  @Nested
  class PaginatedSearch {

    @Test
    void should_return_credit_applications_using_default_pagination() throws Exception {
      // Arrange
      var query = new SearchCreditApplicationsQuery(
          0,20, null, null, null, null
      );

      UUID creditId = UUID.randomUUID();
      var credit = defaultCredit(creditId);

      var pagedResult = PageResult.<CreditApplication>builder()
          .content(List.of(credit))
          .metadata(new com.dmx.creditapplication.domain.model.pagination.PageMetadata(
              0,
              20,
              1,
              1,
              true
          ))
          .build();

      var creditResponse = new CreditApplicationResponse()
          .id(creditId);

      var pagedResponse = new PagedCreditApplicationResponse()
          .content(List.of(creditResponse))
          .metadata(new PageMetadata()
              .page(0)
              .size(20)
              .totalElements(1L)
              .totalPages(1)
              .isLast(true)
          );

      when(mapper.toQuery(
          anyInt(),
          anyInt(),
          nullable(String.class),
          nullable(
              com.dmx.creditapplication.infrastructure.adapter.in.web.dto.CreditStatus.class
          ),
          nullable(Double.class),
          nullable(Double.class)
      )).thenReturn(query);

      when(searchUseCase.execute(any(SearchCreditApplicationsQuery.class)))
          .thenReturn(pagedResult);

      when(mapper.toPagedResponse(
          ArgumentMatchers.<PageResult<CreditApplication>>any()
      )).thenReturn(pagedResponse);

      // Act & Assert
      mockMvc.perform(get("/credit-applications"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.metadata.page")
              .value(0))
          .andExpect(jsonPath("$.metadata.size")
              .value(20))
          .andExpect(jsonPath("$.content[0].id")
              .value(creditId.toString()));

      verify(mapper, times(1)).toQuery(
          0,20,null,null,null,null
      );
      verify(searchUseCase, times(1))
          .execute(query);
      verify(mapper, times(1))
          .toPagedResponse(pagedResult);
    }

    @Test
    void should_return_credit_applications_with_filters() throws Exception {
      // Arrange

      var page = 1;
      var size = 5;
      var customerRfc = "XAXX010101000";
      var creditStatus = CreditStatus.CREATED;
      var minAmount = BigDecimal.valueOf(1000);
      var maxAmount = BigDecimal.valueOf(5000);

      var query = new SearchCreditApplicationsQuery(
          page,size, customerRfc, creditStatus, minAmount, maxAmount
      );
      var creditId = UUID.randomUUID();
      var credit = defaultCredit(creditId);

      var pagedResult = PageResult.<CreditApplication>builder()
          .content(List.of(credit))
          .metadata(new com.dmx.creditapplication.domain.model.pagination.PageMetadata(
              page,
              size,
              8,
              2,
              true
          ))
          .build();

      var creditResponse = new CreditApplicationResponse()
          .id(creditId)
          .customerRfc(customerRfc)
          .status(com.dmx.creditapplication.infrastructure.adapter.in.web.dto.CreditStatus.CREATED);

      var pagedResponse = new PagedCreditApplicationResponse()
          .content(List.of(creditResponse))
          .metadata(new PageMetadata()
              .page(page)
              .size(size)
              .totalElements(8L)
              .totalPages(2)
              .isLast(true)
          );

      when(mapper.toQuery(
          page,
          size,
          customerRfc,
          com.dmx.creditapplication.infrastructure.adapter.in.web.dto.CreditStatus.CREATED,
          minAmount.doubleValue(),
          maxAmount.doubleValue())
      ).thenReturn(query);

      when(searchUseCase.execute(query))
          .thenReturn(pagedResult);

      when(mapper.toPagedResponse(pagedResult))
          .thenReturn(pagedResponse);

      mockMvc.perform(get("/credit-applications")
          .param("page", String.valueOf(page))
          .param("size", String.valueOf(size))
          .param("customerRfc", customerRfc)
          .param("status", "CREATED")
          .param("minAmount", minAmount.toPlainString())
          .param("maxAmount", maxAmount.toPlainString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.metadata.page")
              .value(page))
          .andExpect(jsonPath("$.metadata.size")
              .value(size))
          .andExpect(jsonPath("$.content[0].id")
              .value(creditId.toString()))
          .andExpect(jsonPath("$.content[0].customerRfc")
              .value(customerRfc));

      verify(mapper, times(1)).toQuery(
          page,
          size,
          customerRfc,
          com.dmx.creditapplication.infrastructure.adapter.in.web.dto.CreditStatus.CREATED,
          minAmount.doubleValue(),
          maxAmount.doubleValue()
      );
      verify(searchUseCase, times(1))
          .execute(query);
      verify(mapper, times(1))
          .toPagedResponse(pagedResult);
    }

    @Nested
    class Validations {

      @Test
      void should_return_bad_request_when_page_is_negative()
          throws Exception {
        // Arrange
        int invalidPage = -1;

        // Act & Assert
        mockMvc.perform(get("/credit-applications")
                .param("page", String.valueOf(invalidPage)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(mapper);
        verifyNoInteractions(searchUseCase);
      }

      @Test
      void should_return_bad_request_when_size_is_less_than_one() throws Exception {
        // Arrange
        int size = -1;

        // Act & Assert
        mockMvc.perform(get("/credit-applications")
                .param("page", String.valueOf(size)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(mapper);
        verifyNoInteractions(searchUseCase);
      }

    }

  }

  @Nested
  class ChangeStatus {

    @Test
    void should_change_credit_application_status() throws Exception {
      // Arrange
      var creditId = UUID.randomUUID();

      var updateRequest = new UpdateCreditStatusRequest()
          .status(com.dmx.creditapplication.infrastructure.adapter.in.web.dto.CreditStatus.UNDER_REVIEW)
          .reason("testing");

      var command = new ChangeCreditApplicationStatusCommand(
          creditId,
          CreditStatus.UNDER_REVIEW,
          "testing"
      );

      var creditApplication = defaultCredit(creditId);
      var creditResponse = new CreditApplicationResponse().id(creditId);

      when(mapper.toCommand(creditId, updateRequest)).thenReturn(command);
      when(changeStatusUseCase.execute(command)).thenReturn(creditApplication);
      when(mapper.toResponse(creditApplication)).thenReturn(creditResponse);

      // Act & Assert
      mockMvc.perform(patch("/credit-applications/{creditId}/status", creditId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id")
              .value(creditId.toString()));

      verify(mapper, times(1)).toCommand(creditId, updateRequest);
      verify(changeStatusUseCase, times(1)).execute(command);
      verify(mapper, times(1)).toResponse(creditApplication);
    }

    @Test
    void should_return_bad_request_when_request_is_invalid() throws Exception {
      // Arrange
      var creditId = UUID.randomUUID();

      var updateRequest = new UpdateCreditStatusRequest()
          .reason("testing");

      // Act & Assert
      mockMvc.perform(patch("/credit-applications/{creditId}/status", creditId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(mapper);
      verifyNoInteractions(changeStatusUseCase);
      verifyNoInteractions(mapper);
    }

    @Test
    void should_return_not_found_when_credit_application_does_not_exist() throws Exception {
      // Arrange
      var creditId = UUID.randomUUID();

      var updateRequest = new UpdateCreditStatusRequest()
          .status(com.dmx.creditapplication.infrastructure.adapter.in.web.dto.CreditStatus.UNDER_REVIEW)
          .reason("testing");

      var command = new ChangeCreditApplicationStatusCommand(
          creditId,
          CreditStatus.UNDER_REVIEW,
          "testing");

      when(mapper.toCommand(
          eq(creditId),
          any(UpdateCreditStatusRequest.class)
      )).thenReturn(command);
      when(changeStatusUseCase.execute(any(ChangeCreditApplicationStatusCommand.class)))
          .thenThrow(
              new ResourceNotFoundException(
                  "CreditApplication",
                  creditId
              )
          );

      // Act & Assert
      mockMvc.perform(patch("/credit-applications/{creditId}/status", creditId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isNotFound());


      verify(mapper, times(1)).toCommand(creditId, updateRequest);
      verify(changeStatusUseCase, times(1)).execute(any(ChangeCreditApplicationStatusCommand.class));
      verifyNoMoreInteractions(mapper);
    }

    @Test
    void should_return_conflict_when_status_transition_is_invalid() throws Exception {
      // Arrange
      var creditId = UUID.randomUUID();

      var updateRequest = new UpdateCreditStatusRequest()
          .status(com.dmx.creditapplication.infrastructure.adapter.in.web.dto.CreditStatus.UNDER_REVIEW)
          .reason("testing");

      var command = new ChangeCreditApplicationStatusCommand(
          creditId,
          CreditStatus.APPROVED,
          "testing");

      when(mapper.toCommand(
          eq(creditId),
          any(UpdateCreditStatusRequest.class)
      )).thenReturn(command);
      when(changeStatusUseCase.execute(any(ChangeCreditApplicationStatusCommand.class)))
          .thenThrow(
              new ConflictException(
                  "Invalid status transition from " + CreditStatus.CREATED +
                      " to " + CreditStatus.APPROVED
              )
          );

      // Act & Assert
      mockMvc.perform(patch("/credit-applications/{creditId}/status", creditId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isConflict());


      verify(mapper, times(1)).toCommand(creditId, updateRequest);
      verify(changeStatusUseCase, times(1)).execute(any(ChangeCreditApplicationStatusCommand.class));
      verifyNoMoreInteractions(mapper);
    }

  }


  private CreditApplication defaultCredit(UUID id) {
    return CreditApplication.restore(
        id,
        "Juan Perez",
        "juan@test.com",
        "XAXX010101000",
        new BigDecimal("1000.0"),
        Currency.MXN,
        12,
        new BigDecimal("0.12"),
        null,
        null,
        null,
        null,
        null,
        CreditStatus.CREATED,
        null,
        null,
        null
    );
  }

}
