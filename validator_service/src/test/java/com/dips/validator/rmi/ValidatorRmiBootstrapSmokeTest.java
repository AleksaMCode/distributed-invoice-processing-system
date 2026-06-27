package com.dips.validator.rmi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dips.validator.model.Client;
import com.dips.validator.model.Invoice;
import com.dips.validator.model.Item;
import com.dips.validator.model.ValidationResult;
import com.dips.validator.validation.InvoiceValidator;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ValidatorRmiBootstrapSmokeTest {
  private static final String BIND_NAME = "ValidatorServiceSmoke";

  private Registry registry;
  private ValidatorServiceImpl serviceImpl;

  @AfterEach
  void tearDown() throws Exception {
    if (registry != null) {
      try {
        registry.unbind(BIND_NAME);
      } catch (Exception ignored) {
      }
    }

    if (serviceImpl != null) {
      UnicastRemoteObject.unexportObject(serviceImpl, true);
    }

    if (registry != null) {
      UnicastRemoteObject.unexportObject(registry, true);
    }
  }

  @Test
  void bootstrapRegistryBindLookupAndValidateSuccessfully() throws Exception {
    int port = findFreePort();
    registry = LocateRegistry.createRegistry(port);
    serviceImpl = new ValidatorServiceImpl(new InvoiceValidator(ZoneId.of("Europe/Paris")));
    registry.rebind(BIND_NAME, serviceImpl);

    Registry clientRegistry = LocateRegistry.getRegistry("127.0.0.1", port);
    ValidatorService stub = (ValidatorService) clientRegistry.lookup(BIND_NAME);

    Invoice invoice =
        new Invoice(
            "INV-2026-001",
            new Client("DIPS", "1234567890123", "invoice@dips.com"),
            LocalDate.now(ZoneId.of("Europe/Paris")).minusDays(1),
            "EUR",
            List.of(
                new Item("Web development", 10, new BigDecimal("50.00")),
                new Item("Hosting", 1, new BigDecimal("120.00"))));

    ValidationResult result = stub.validate(invoice);

    assertNotNull(result);
    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
    assertFalse(result.validatorTimestamp().isBlank());
  }

  private int findFreePort() throws Exception {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }
}
