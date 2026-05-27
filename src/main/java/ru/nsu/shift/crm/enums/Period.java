package ru.nsu.shift.crm.enums;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.IsoFields;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

public enum Period {
    DAY {
        @Override
        public DateRange calculateRange(LocalDate ref) {
            return new DateRange(ref.atStartOfDay(), ref.atTime(LocalTime.MAX));
        }
    },
    MONTH {
        @Override
        public DateRange calculateRange(LocalDate ref) {
            return new DateRange(
                ref.with(firstDayOfMonth()).atStartOfDay(),
                ref.with(lastDayOfMonth()).atTime(LocalTime.MAX)
            );
        }
    },
    QUARTER {
        @Override
        public DateRange calculateRange(LocalDate ref) {
            int currentQuarter = ref.get(IsoFields.QUARTER_OF_YEAR);
            LocalDate firstDay = LocalDate.of(ref.getYear(), (currentQuarter - 1) * 3 + 1, 1);
            return new DateRange(
                firstDay.atStartOfDay(),
                firstDay.plusMonths(2).with(lastDayOfMonth()).atTime(LocalTime.MAX)
            );
        }
    },
    YEAR {
        @Override
        public DateRange calculateRange(LocalDate ref) {
            return new DateRange(
                ref.with(firstDayOfYear()).atStartOfDay(),
                ref.with(lastDayOfYear()).atTime(LocalTime.MAX)
            );
        }
    };

    public abstract DateRange calculateRange(LocalDate ref);
}