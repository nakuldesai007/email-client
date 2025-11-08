package com.emailclient.backend.email;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmailClientPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void bindsImapAndCryptoSettingsFromConfiguration() {
        contextRunner
                .withPropertyValues(
                        "email-client.imap.host=imap.test.example",
                        "email-client.imap.port=993",
                        "email-client.imap.username=test-imap-user@example.com",
                        "email-client.imap.password=test-imap-password",
                        "email-client.crypto.master-key=MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=",
                        "email-client.crypto.salt=c2FtcGxlLXRlc3Qtc2FsdCEh")
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailClientProperties.class);

                    EmailClientProperties properties = context.getBean(EmailClientProperties.class);

                    assertThat(properties.getImap().getHost()).isEqualTo("imap.test.example");
                    assertThat(properties.getImap().getPort()).isEqualTo(993);
                    assertThat(properties.getImap().getUsername()).isEqualTo("test-imap-user@example.com");
                    assertThat(properties.getImap().getPassword()).isEqualTo("test-imap-password");
                    assertThat(properties.getImap().isSsl()).isTrue();
                    assertThat(properties.getCrypto().getMasterKey())
                            .isEqualTo("MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=");
                    assertThat(properties.getCrypto().getSalt()).isEqualTo("c2FtcGxlLXRlc3Qtc2FsdCEh");
                });
    }

    @Test
    void bindsPropertiesWhenProvidedViaEnvironmentStyleVariables() {
        contextRunner
                .withInitializer(context -> {
                    Map<String, Object> env = new HashMap<>();
                    env.put("EMAIL_CLIENT_IMAP_HOST", "imap.test.example");
                    env.put("EMAIL_CLIENT_IMAP_PORT", "993");
                    env.put("EMAIL_CLIENT_IMAP_USER", "test-imap-user@example.com");
                    env.put("EMAIL_CLIENT_IMAP_USERNAME", "test-imap-user@example.com");
                    env.put("EMAIL_CLIENT_IMAP_PASSWORD", "test-imap-password");
                    env.put("EMAIL_CLIENT_CRYPTO_MASTER_KEY", "MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=");
                    env.put("EMAIL_CLIENT_CRYPTO_SALT", "c2FtcGxlLXRlc3Qtc2FsdCEh");

                    MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
                    SystemEnvironmentPropertySource propertySource = new SystemEnvironmentPropertySource(
                            StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, env);
                    if (propertySources.contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                        propertySources.replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                                propertySource);
                    } else {
                        propertySources.addFirst(propertySource);
                    }
                })
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailClientProperties.class);

                    EmailClientProperties properties = context.getBean(EmailClientProperties.class);

                    assertThat(properties.getImap().getHost()).isEqualTo("imap.test.example");
                    assertThat(properties.getImap().getPort()).isEqualTo(993);
                    assertThat(properties.getImap().getUsername()).isEqualTo("test-imap-user@example.com");
                    assertThat(properties.getImap().getPassword()).isEqualTo("test-imap-password");
                    assertThat(properties.getCrypto().getMasterKey())
                            .isEqualTo("MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=");
                    assertThat(properties.getCrypto().getSalt()).isEqualTo("c2FtcGxlLXRlc3Qtc2FsdCEh");
                });
    }

    @Test
    void failsValidationWhenRequiredImapSettingsMissing() {
        contextRunner
                .withPropertyValues(
                        "email-client.imap.host=",
                        "email-client.imap.port=993",
                        "email-client.crypto.master-key=MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=",
                        "email-client.crypto.salt=c2FtcGxlLXRlc3Qtc2FsdCEh")
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailClientProperties.class);
                    EmailClientProperties properties = context.getBean(EmailClientProperties.class);
                    Validator validator = context.getBean(Validator.class);

                    var violations = validator.validate(properties.getImap());
                    assertThat(violations)
                            .as("Expected validation errors when IMAP host is blank")
                            .anyMatch(v -> "host".equals(v.getPropertyPath().toString()));
                });
    }

    @Configuration
    @EnableConfigurationProperties(EmailClientProperties.class)
    static class TestConfiguration {
    }
}
