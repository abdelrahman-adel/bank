package com.bank.account.event;

import com.bank.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerEventListenerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private CustomerEventListener customerEventListener;

    @Test
    void whenHandleCustomerDeletedEvent_withValidId_shouldCallService() {
        // Arrange
        Long customerId = 123L;
        doNothing().when(accountService).deleteAccountsByCustomerId(customerId);

        // Act
        customerEventListener.handleCustomerDeletedEvent(customerId);

        // Assert
        verify(accountService).deleteAccountsByCustomerId(customerId);
    }

    @Test
    void whenHandleCustomerDeletedEvent_andServiceThrowsException_shouldHandleGracefully() {
        // Arrange
        Long customerId = 456L;
        doThrow(new RuntimeException("Database is down")).when(accountService).deleteAccountsByCustomerId(customerId);

        // Act & Assert
        // The listener should catch the exception and not re-throw it.
        assertDoesNotThrow(() -> customerEventListener.handleCustomerDeletedEvent(customerId));

        // Verify the service method was still called
        verify(accountService).deleteAccountsByCustomerId(customerId);
    }
}