# Validator Service

Plain Java RMI service that validates invoices for the DIPS pipeline.

## Remote contract

```java
ValidationResult validate(Invoice invoice) throws RemoteException
```

## Validations

- EIN: exactly 13 digits
- Date: not in the future and not older than 365 days
- Items: at least one item
- Quantity and unitPrice: positive
- Currency: BAM, EUR, USD, CHF, GBP
- Total amount: > 0 and < 1,000,000
- Email format: validated with Apache Commons Validator

## Configuration

Copy `.env.example` to `.env`.

## Run

```bash
./gradlew run
```
