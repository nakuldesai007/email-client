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
                        "email-client.imap.host=imap.gmail.com",
                        "email-client.imap.port=993",
                        "email-client.imap.username=nakuldesai007@gmail.com",
                        "email-client.imap.password=aeyuydrkwnzdydvq",
                        "email-client.crypto.master-key=I6eTnrSdxciZMsxCbRGIhEW52znNEWtW3tdfJZlKpQQ=",
                        "email-client.crypto.salt=0CHc4CDlRF3BY9GYRK0+vg=="
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailClientProperties.class);

                    EmailClientProperties properties = context.getBean(EmailClientProperties.class);

                    assertThat(properties.getImap().getHost()).isEqualTo("imap.gmail.com");
                    assertThat(properties.getImap().getPort()).isEqualTo(993);
                    assertThat(properties.getImap().getUsername()).isEqualTo("nakuldesai007@gmail.com");
                    assertThat(properties.getImap().getPassword()).isEqualTo("aeyuydrkwnzdydvq");
                    assertThat(properties.getImap().isSsl()).isTrue();
                    assertThat(properties.getCrypto().getMasterKey()).isEqualTo("I6eTnrSdxciZMsxCbRGIhEW52znNEWtW3tdfJZlKpQQ=");
                    assertThat(properties.getCrypto().getSalt()).isEqualTo("0CHc4CDlRF3BY9GYRK0+vg==");
                });
    }

    @Test
    void bindsPropertiesWhenProvidedViaEnvironmentStyleVariables() {
        contextRunner
                .withInitializer(context -> {
                    Map<String, Object> env = new HashMap<>();
                    env.put("EMAIL_CLIENT_IMAP_HOST", "imap.gmail.com");
                    env.put("EMAIL_CLIENT_IMAP_PORT", "993");
                    env.put("EMAIL_CLIENT_IMAP_USER", "nakuldesai007@gmail.com");
                    env.put("EMAIL_CLIENT_IMAP_USERNAME", "nakuldesai007@gmail.com");
                    env.put("EMAIL_CLIENT_IMAP_PASSWORD", "aeyuydrkwnzdydvq");
                    env.put("EMAIL_CLIENT_CRYPTO_MASTER_KEY", "I6eTnrSdxciZMsxCbRGIhEW52znNEWtW3tdfJZlKpQQ=");
                    env.put("EMAIL_CLIENT_CRYPTO_SALT", "0CHc4CDlRF3BY9GYRK0+vg==");

                    MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
                    SystemEnvironmentPropertySource propertySource = new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, env);
                    if (propertySources.contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                        propertySources.replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, propertySource);
                    } else {
                        propertySources.addFirst(propertySource);
                    }
                })
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailClientProperties.class);

                    EmailClientProperties properties = context.getBean(EmailClientProperties.class);

                    assertThat(properties.getImap().getHost()).isEqualTo("imap.gmail.com");
                    assertThat(properties.getImap().getPort()).isEqualTo(993);
                    assertThat(properties.getImap().getUsername()).isEqualTo("nakuldesai007@gmail.com");
                    assertThat(properties.getImap().getPassword()).isEqualTo("aeyuydrkwnzdydvq");
                    assertThat(properties.getCrypto().getMasterKey()).isEqualTo("I6eTnrSdxciZMsxCbRGIhEW52znNEWtW3tdfJZlKpQQ=");
                    assertThat(properties.getCrypto().getSalt()).isEqualTo("0CHc4CDlRF3BY9GYRK0+vg==");
                });
    }

    @Test
    void failsValidationWhenRequiredImapSettingsMissing() {
        contextRunner
                .withPropertyValues(
                        "email-client.imap.host=",
                        "email-client.imap.port=993",
                        "email-client.crypto.master-key=I6eTnrSdxciZMsxCbRGIhEW52znNEWtW3tdfJZlKpQQ=",
                        "email-client.crypto.salt=0CHc4CDlRF3BY9GYRK0+vg=="
                )
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

