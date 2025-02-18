package wooteco.subway.domain.section;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import wooteco.subway.domain.path.Fare;
import wooteco.subway.domain.section.DistanceFare;

public class DistanceFareTest {

    @ParameterizedTest(name = "{0}km일 때 요금은 {1}원이다")
    @MethodSource("provideDistanceAndFare")
    void calculate(double distance, int expected) {
        Function<Double, Fare> fareCalculator = DistanceFare.fareCalculator();
        Fare fare = fareCalculator.apply(distance);

        assertThat(fare.getValue()).isEqualTo(expected);
    }

    private static Stream<Arguments> provideDistanceAndFare() {
        return Stream.of(
                Arguments.of(9, 1250),
                Arguments.of(10, 1250),
                Arguments.of(11, 1350),
                Arguments.of(15.1, 1450),
                Arguments.of(16, 1450),
                Arguments.of(49, 2050),
                Arguments.of(50, 2050),
                Arguments.of(51, 2150),
                Arguments.of(58, 2150),
                Arguments.of(59, 2250)
        );
    }
}
