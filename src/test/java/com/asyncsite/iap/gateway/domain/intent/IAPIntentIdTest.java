package com.asyncsite.iap.gateway.domain.intent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IAPIntentId Value Object 테스트")
class IAPIntentIdTest {

    @Test
    @DisplayName("generate() - 고유한 Intent ID를 생성한다")
    void generate_ShouldCreateUniqueId() {
        // when
        IAPIntentId id1 = IAPIntentId.generate();
        IAPIntentId id2 = IAPIntentId.generate();

        // then
        assertThat(id1.getValue()).isNotEqualTo(id2.getValue());
        assertThat(id1.getValue()).startsWith("iap_intent_");
        assertThat(id2.getValue()).startsWith("iap_intent_");
    }

    @Test
    @DisplayName("of() - 유효한 Intent ID 문자열로 Value Object를 생성한다")
    void of_ShouldCreateFromValidString() {
        // given
        String validId = "iap_intent_1234567890_abc123";

        // when
        IAPIntentId intentId = IAPIntentId.of(validId);

        // then
        assertThat(intentId.getValue()).isEqualTo(validId);
    }

    @Test
    @DisplayName("of() - null 값으로는 생성할 수 없다")
    void of_ShouldThrowException_WhenNull() {
        // when & then
        assertThatThrownBy(() -> IAPIntentId.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("of() - 빈 문자열로는 생성할 수 없다")
    void of_ShouldThrowException_WhenEmpty() {
        // when & then
        assertThatThrownBy(() -> IAPIntentId.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("of() - iap_intent_ prefix가 없으면 생성할 수 없다")
    void of_ShouldThrowException_WhenInvalidFormat() {
        // when & then
        assertThatThrownBy(() -> IAPIntentId.of("invalid_id"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid IAPIntentId format");
    }

    @Test
    @DisplayName("equals() - 같은 값을 가진 IntentId는 동등하다")
    void equals_ShouldReturnTrue_WhenSameValue() {
        // given
        String value = "iap_intent_1234567890_abc123";
        IAPIntentId id1 = IAPIntentId.of(value);
        IAPIntentId id2 = IAPIntentId.of(value);

        // when & then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("equals() - 다른 값을 가진 IntentId는 동등하지 않다")
    void equals_ShouldReturnFalse_WhenDifferentValue() {
        // given
        IAPIntentId id1 = IAPIntentId.of("iap_intent_1234567890_abc123");
        IAPIntentId id2 = IAPIntentId.of("iap_intent_9876543210_xyz789");

        // when & then
        assertThat(id1).isNotEqualTo(id2);
    }
}
