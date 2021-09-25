package com.example.infleranspringrestapi.accounts;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTestJunit4 {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Test(expected = UsernameNotFoundException.class)
    public void findByUserByEmail() {
        String username = "random@email.com";
        accountService.loadUserByUsername(username);
    }

    @Test
    public void findByUserByEmail_TryCatch() {
        String username = "random@email.com";

        try {
            accountService.loadUserByUsername(username);
            fail("supposed to be failed");
        } catch (UsernameNotFoundException e) {
            assertThat(e.getMessage()).containsSequence(username);
        }
    }

    @Test
    public void findByUserEmail_Rule() {
        String username = "random@email.com";
        expectedException.expect(UsernameNotFoundException.class);
        expectedException.expectMessage(Matchers.containsString(username));

        accountService.loadUserByUsername(username);
    }

    @Test
    public void findByUsernameFail_assertThrows() {
        String username = "random@email.com";

        //when & then
        UsernameNotFoundException exception = Assertions.assertThrows(
                UsernameNotFoundException.class, () -> accountService.loadUserByUsername(username));
        assertThat(exception.getMessage()).containsSequence(username);
    }
}
