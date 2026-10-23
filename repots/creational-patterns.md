# Creational patterns in the banking app

## Builder

[`TransactionCommand.Builder`](../src/main/java/com/example/tmps_lab/dto/TransactionCommand.java) assembles and validates commands. HTTP requests can still be deserialized directly into `TransactionCommand`; code that creates commands internally can use the builder.

```java
TransactionCommand command = TransactionCommand.builder()
    .type(TransactionType.TRANSFER)
    .fromAccountId(sourceId)
    .toAccountId(destinationId)
    .amount(50)
    .build();
```

## Prototype

[`TransactionTemplate`](../src/main/java/com/example/tmps_lab/model/TransactionTemplate.java) holds reusable transaction instructions. `copy()` duplicates a template, and `copyWithAmount()` makes a variant without changing the original. A template never carries a transaction ID. [`TransactionService.repeat`](../src/main/java/com/example/tmps_lab/service/TransactionService.java) copies instructions from a saved transaction and processes them as a new transaction with a new ID. The endpoint is `POST /transactions/{id}/repeat` with a body such as `{"amount": 50}`.

## Factory Method

[`TransactionProcessor.createTransaction`](../src/main/java/com/example/tmps_lab/service/processor/TransactionProcessor.java) is the factory method. Each processor implements it to create its matching product: `DepositTransaction`, `WithdrawalTransaction`, or `TransferTransaction`. They share the [`Transaction`](../src/main/java/com/example/tmps_lab/model/Transaction.java) interface, so the dispatcher and repository can handle them uniformly. The controller maps each product to the same five-field HTTP response shape.
