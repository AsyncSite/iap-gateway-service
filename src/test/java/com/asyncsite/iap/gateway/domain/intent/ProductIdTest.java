package com.asyncsite.iap.gateway.domain.intent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProductId Value Object 테스트")
class ProductIdTest {

    @Test
    @DisplayName("of() - 유효한 Product ID 문자열로 Value Object를 생성한다")
    void of_ShouldCreateFromValidString() {
        // given
        String validProductId = "insight_1000_points";

        // when
        ProductId productId = ProductId.of(validProductId);

        // then
        assertThat(productId.getValue()).isEqualTo(validProductId);
    }

    @Test
    @DisplayName("of() - null 값으로는 생성할 수 없다")
    void of_ShouldThrowException_WhenNull() {
        // when & then
        assertThatThrownBy(() -> ProductId.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("of() - 빈 문자열로는 생성할 수 없다")
    void of_ShouldThrowException_WhenEmpty() {
        // when & then
        assertThatThrownBy(() -> ProductId.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("equals() - 같은 값을 가진 ProductId는 동등하다")
    void equals_ShouldReturnTrue_WhenSameValue() {
        // given
        String productId = "insight_1000_points";
        ProductId pid1 = ProductId.of(productId);
        ProductId pid2 = ProductId.of(productId);

        // when & then
        assertThat(pid1).isEqualTo(pid2);
        assertThat(pid1.hashCode()).isEqualTo(pid2.hashCode());
    }

    @Test
    @DisplayName("equals() - 다른 값을 가진 ProductId는 동등하지 않다")
    void equals_ShouldReturnFalse_WhenDifferentValue() {
        // given
        ProductId pid1 = ProductId.of("insight_1000_points");
        ProductId pid2 = ProductId.of("insight_5000_points");

        // when & then
        assertThat(pid1).isNotEqualTo(pid2);
    }
}
