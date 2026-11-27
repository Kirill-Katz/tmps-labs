package com.example.tmps_lab;

import com.example.tmps_lab.controller.AccountController;
import com.example.tmps_lab.controller.TransactionController;
import com.example.tmps_lab.dto.TransactionCommand;
import com.example.tmps_lab.model.Account;
import com.example.tmps_lab.model.Transaction;
import com.example.tmps_lab.model.TransactionType;
import com.example.tmps_lab.repository.InMemoryAccountRepository;
import com.example.tmps_lab.repository.InMemoryTransactionRepository;
import com.example.tmps_lab.repository.LoggingTransactionRepository;
import com.example.tmps_lab.service.AccountService;
import com.example.tmps_lab.service.BankingFacade;
import com.example.tmps_lab.service.TransactionDispatcher;
import com.example.tmps_lab.service.TransactionService;
import com.example.tmps_lab.service.processor.DepositProcessor;
import com.example.tmps_lab.service.processor.TransferProcessor;
import com.example.tmps_lab.service.processor.WithdrawProcessor;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BankingFacadeTests {
    private final InMemoryAccountRepository accountStorage = new InMemoryAccountRepository();
    private final InMemoryTransactionRepository transactionStorage = new InMemoryTransactionRepository();
    private final LoggingTransactionRepository decorated = new LoggingTransactionRepository(transactionStorage);
    private final BankingFacade banking = new BankingFacade(
        new AccountService(accountStorage),
        new TransactionService(new TransactionDispatcher(List.of(
            new DepositProcessor(decorated, accountStorage),
            new WithdrawProcessor(decorated, accountStorage),
            new TransferProcessor(decorated, accountStorage)
        ), event -> {}), decorated)
    );

    @Test
    void facadeCoordinatesAccountsCommandsAndRepeatedTransactionsThroughDecorator() {
        Account source = banking.createAccount();
        Account destination = banking.createAccount();
        banking.deposit(source.id(), 100);
        Transaction transfer = banking.transfer(source.id(), destination.id(), 40);
        banking.withdraw(destination.id(), 10);
        Transaction repeated = banking.repeatTransaction(transfer.id(), 15);

        assertEquals(45, banking.getAccount(source.id()).balance());
        assertEquals(45, banking.getAccount(destination.id()).balance());
        assertEquals(transfer, banking.getTransaction(transfer.id()));
        assertEquals(repeated, transactionStorage.findById(repeated.id()));
        assertNotEquals(transfer.id(), repeated.id());
        assertEquals(15, repeated.amount());
        assertEquals(40, transfer.amount());
        assertEquals(TransactionType.TRANSFER, repeated.type());
        assertEquals(source.id(), repeated.fromAccountId());
        assertEquals(destination.id(), repeated.toAccountId());
    }

    @Test
    void facadePreservesMissingResultsAndRejectsInvalidOperationsWithoutBalanceChanges() {
        Account account = banking.createAccount();
        banking.deposit(account.id(), 20);
        UUID unknown = UUID.randomUUID();

        assertNull(banking.getAccount(unknown));
        assertNull(banking.getTransaction(unknown));
        assertThrows(IllegalArgumentException.class, () -> banking.repeatTransaction(unknown, 10));
        assertThrows(IllegalArgumentException.class, () -> banking.withdraw(account.id(), 30));
        assertThrows(IllegalArgumentException.class, () -> banking.deposit(account.id(), 0));
        assertThrows(IllegalArgumentException.class, () -> banking.transfer(account.id(), account.id(), 5));
        assertThrows(IllegalArgumentException.class, () -> banking.createTransaction(
            new TransactionCommand(TransactionType.DEPOSIT, null, null, 5)));
        assertEquals(20, banking.getAccount(account.id()).balance());
    }

    @Test
    void controllersKeepExistingHttpRoutesAndResponseFields() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(
            new AccountController(banking), new TransactionController(banking)).build();
        Account account = banking.createAccount();
        Transaction original = banking.deposit(account.id(), 10);

        mvc.perform(post("/accounts"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.balance").value(0));
        mvc.perform(get("/accounts/{id}", account.id()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(10));
        mvc.perform(post("/transactions").contentType("application/json")
                .content("""
                    {"type":"DEPOSIT","toAccountId":"%s","amount":5}
                    """.formatted(account.id())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.type").value("DEPOSIT"))
            .andExpect(jsonPath("$.toAccountId").value(account.id().toString()))
            .andExpect(jsonPath("$.amount").value(5));
        mvc.perform(get("/transactions/{id}", original.id()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(original.id().toString()));
        mvc.perform(post("/transactions/{id}/repeat", original.id())
                .contentType("application/json").content("{\"amount\":7}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.amount").value(7));
        assertEquals(22, banking.getAccount(account.id()).balance());
    }
}
