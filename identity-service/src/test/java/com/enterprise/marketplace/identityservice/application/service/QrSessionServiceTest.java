package com.enterprise.marketplace.identityservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.identityservice.application.dto.ConfirmQrRequest;
import com.enterprise.marketplace.identityservice.application.dto.CreateQrRequest;
import com.enterprise.marketplace.identityservice.application.dto.QrCreateResponse;
import com.enterprise.marketplace.identityservice.application.dto.QrStatusResponse;
import com.enterprise.marketplace.identityservice.config.AuthProperties;
import com.enterprise.marketplace.identityservice.domain.QrSessionStatus;
import com.enterprise.marketplace.identityservice.domain.UserType;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AccessTokenData;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AuthSessionStore;
import com.enterprise.marketplace.identityservice.infrastructure.redis.QrSessionData;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QrSessionServiceTest {

    @Mock
    private AuthSessionStore authRedisStore;

    private final AuthProperties authProperties = new AuthProperties();

    @InjectMocks
    private QrSessionService qrSessionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(qrSessionService, "authProperties", authProperties);
    }

    @Test
    void createQrSessionReturnsPendingPayload() {
        CreateQrRequest request = new CreateQrRequest();
        request.setDeviceId("web-1");

        QrCreateResponse response = qrSessionService.createQrSession(request);

        assertThat(response.getStatus()).isEqualTo(QrSessionStatus.PENDING.name());
        assertThat(response.getQrPayload()).startsWith("karatkart://qr-login?sessionId=");
        assertThat(response.getQrSessionId()).isNotBlank();
        verify(authRedisStore).saveQrSession(any(QrSessionData.class));
    }

    @Test
    void confirmQrSessionBindsAccessToken() {
        UUID userId = UUID.randomUUID();
        QrSessionData session = QrSessionData.builder()
                .qrSessionId("qr-1")
                .status(QrSessionStatus.PENDING.name())
                .build();
        AccessTokenData access = AccessTokenData.builder()
                .token("access-9")
                .userId(userId)
                .userType(UserType.INDIVIDUAL)
                .build();
        when(authRedisStore.getQrSession("qr-1")).thenReturn(Optional.of(session));
        when(authRedisStore.getAccessToken("access-9")).thenReturn(Optional.of(access));

        ConfirmQrRequest request = new ConfirmQrRequest();
        request.setAccessToken("access-9");

        QrStatusResponse response = qrSessionService.confirmQrSession("qr-1", request);

        assertThat(response.getStatus()).isEqualTo(QrSessionStatus.CONFIRMED.name());
        assertThat(response.getAccessToken()).isEqualTo("access-9");
        assertThat(response.getUserId()).isEqualTo(userId);
        ArgumentCaptor<QrSessionData> captor = ArgumentCaptor.forClass(QrSessionData.class);
        verify(authRedisStore).saveQrSession(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QrSessionStatus.CONFIRMED.name());
    }
}
