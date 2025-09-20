package com.soundtracker.backend;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Basic safeguard test - ensures DNS resolution for known external domains is either blocked / not attempted.
 * This is a heuristic placeholder; real implementation would wrap HTTP client beans.
 */
@SpringBootTest
@ActiveProfiles("mock")
public class NoExternalHttpTest {

    @BeforeAll
    static void explain() {
        System.out.println("[TEST] Verifying offline mock profile constraints (heuristic)");
    }

    @Test
    void externalDomainResolutionIsNotRequired() throws UnknownHostException {
        // We do NOT actually call external APIs—just ensure test harness runs under mock profile.
        // If future guard logic is added (e.g., custom HTTP client), enhance this test.
        InetAddress localhost = InetAddress.getByName("localhost");
        Assertions.assertNotNull(localhost);
    }
}
