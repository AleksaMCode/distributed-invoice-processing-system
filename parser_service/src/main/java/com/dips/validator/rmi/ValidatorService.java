package com.dips.validator.rmi;

import com.dips.validator.model.Invoice;
import com.dips.validator.model.ValidationResult;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ValidatorService extends Remote {
  ValidationResult validate(Invoice invoice) throws RemoteException;
}
