package com.dips.validator;

import com.dips.validator.config.AppConfig;
import com.dips.validator.rmi.ValidatorService;
import com.dips.validator.rmi.ValidatorServiceImpl;
import com.dips.validator.validation.InvoiceValidator;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorApplication {
  private static final Logger logger = LoggerFactory.getLogger(ValidatorApplication.class);

  public static void main(String[] args) throws Exception {
    AppConfig config = AppConfig.load();
    System.setProperty("java.rmi.server.hostname", config.rmiHost());

    Registry registry = startOrGetRegistry(config.rmiPort());
    ValidatorService service =
        new ValidatorServiceImpl(new InvoiceValidator(config.validationZone()));

    bind(registry, config.rmiBindName(), service);

    logger.info(
        "Validator RMI service is ready at {}:{} name={}",
        config.rmiHost(),
        config.rmiPort(),
        config.rmiBindName());

    Thread.currentThread().join();
  }

  private static Registry startOrGetRegistry(int port) throws RemoteException {
    try {
      return LocateRegistry.createRegistry(port);
    } catch (RemoteException ex) {
      return LocateRegistry.getRegistry(port);
    }
  }

  private static void bind(Registry registry, String bindName, ValidatorService service)
      throws RemoteException {
    try {
      registry.bind(bindName, service);
    } catch (AlreadyBoundException ex) {
      registry.rebind(bindName, service);
    }
  }
}
