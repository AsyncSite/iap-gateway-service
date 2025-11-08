package com.asyncsite.iap.gateway.domain.intent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserEmail Value Object 테스트")
class UserEmailTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "test@example.com",
        "user.name@domain.co.kr",
        "admin+tag@company.org"
    })
    @DisplayName("of() - 유효한 이메일로 Value Object를 생성한다")
    void of_ShouldCreateFromValidEmail(String validEmail) {
        // when
        UserEmail userEmail = UserEmail.of(validEmail);

        // then
        assertThat(userEmail.getValue()).isEqualTo(validEmail);
    }

    @Test
    @DisplayName("of() - null 값으로는 생성할 수 없다")
    void of_ShouldThrowException_WhenNull() {
        // when & then
        assertThatThrownBy(() -> UserEmail.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("of() - 빈 문자열로는 생성할 수 없다")
    void of_ShouldThrowException_WhenEmpty() {
        // when & then
        assertThatThrownBy(() -> UserEmail.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "@example.com",
        "test@",
        "test @example.com"
    })
    @DisplayName("of() - 유효하지 않은 이메일 형식으로는 생성할 수 없다")
    void of_ShouldThrowException_WhenInvalidFormat(String invalidEmail) {
        // when & then
        assertThatThrownBy(() -> UserEmail.of(invalidEmail))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("equals() - 같은 이메일을 가진 UserEmail은 동등하다")
    void equals_ShouldReturnTrue_WhenSameEmail() {
        // given
        String email = "test@example.com";
        UserEmail email1 = UserEmail.of(email);
        UserEmail email2 = UserEmail.of(email);

        // when & then
        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }

    @Test
    @DisplayName("equals() - 다른 이메일을 가진 UserEmail은 동등하지 않다")
    void equals_ShouldReturnFalse_WhenDifferentEmail() {
        // given
        UserEmail email1 = UserEmail.of("test1@example.com");
        UserEmail email2 = UserEmail.of("test2@example.com");

        // when & then
        assertThat(email1).isNotEqualTo(email2);
    }
}
