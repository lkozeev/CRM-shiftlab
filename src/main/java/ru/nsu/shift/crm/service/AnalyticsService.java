package ru.nsu.shift.crm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.shift.crm.dto.BestPeriodDto;
import ru.nsu.shift.crm.dto.SellerDto;
import ru.nsu.shift.crm.entity.Seller;
import ru.nsu.shift.crm.entity.Transaction;
import ru.nsu.shift.crm.enums.DateRange;
import ru.nsu.shift.crm.enums.Period;
import ru.nsu.shift.crm.exception.ResourceNotFoundException;
import ru.nsu.shift.crm.repository.TransactionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final TransactionRepository transactionRepository;
    private final SellerService sellerService;

    /**
     * Returns the most productive seller (highest total transaction amount)
     * for the given period relative to the reference date.
     *
     * @param period        DAY | MONTH | QUARTER | YEAR
     * @param referenceDate the date within the target period (defaults to today)
     */
    @Transactional(readOnly = true)
    public SellerDto getMostProductiveSeller(Period period, LocalDate referenceDate) {
        LocalDate ref = referenceDate != null ? referenceDate : LocalDate.now();
        DateRange range = period.calculateRange(ref);

        return transactionRepository.findTopSellersByTotalAmount(range.start(), range.end(), PageRequest.of(0, 1))
			.stream()
			.findFirst()
			.map(SellerService::convertToDto)
			.orElseThrow(() -> new ResourceNotFoundException("No transactions found in the specified period [" + range.start() + " - " + range.end() + "]"));
    }

    /**
     * Returns sellers whose total transaction amount in [startDate, endDate] is below the threshold.
     */
    @Transactional(readOnly = true)
    public List<SellerDto> getSellersWithTotalBelow(LocalDate startDate, LocalDate endDate, BigDecimal threshold) {
        validateDateRange(startDate, endDate);
        DateRange range = new DateRange(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        return transactionRepository.findSellersWithTotalBelow(range.start(), range.end(), threshold).stream()
			.map(SellerService::convertToDto)
			.toList();
    }
    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("startDate (" + start + ") must not be after endDate (" + end + ")");
        }
    }

	/**
     * Finds the best calendar period for a seller based on transaction density (transactions/day).
     * Normalizes different period lengths (DAY, MONTH, QUARTER, YEAR) for a fair comparison.
     * Ties are broken by the highest absolute transaction count.
     */
    @Transactional(readOnly = true)
    public BestPeriodDto getBestPeriodForSeller(Long sellerId) {
        Seller seller = sellerService.findSellerById(sellerId);
        List<Transaction> transactions = transactionRepository.findBySellerIdOrderByTransactionDateAsc(sellerId);

        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("No transactions found for seller with id " + sellerId);
        }

        record PeriodStats(Period period, LocalDate start, LocalDate end, long count, double density) {}

        // тут происходит бред сумасшедшего, сказывается (https://github.com/lkozeev/Diophantine-equation-solver)
        List<PeriodStats> stats = Arrays.stream(Period.values())
			.flatMap(period -> transactions.stream()
				.collect(Collectors.groupingBy(
					t -> period.calculateRange(t.getTransactionDate().toLocalDate()),
					Collectors.counting()
				))
				.entrySet().stream()
				.map(entry -> {
					LocalDate start = entry.getKey().start().toLocalDate();
					LocalDate end = entry.getKey().end().toLocalDate();
					long count = entry.getValue();
					long days = ChronoUnit.DAYS.between(start, end) + 1;

					double density = (double) count / days;
					return new PeriodStats(period, start, end, count, density);
				})
			)
			.toList();
        
        PeriodStats best = stats.stream()
			.max(Comparator.comparingDouble(PeriodStats::density)
				.thenComparingLong(PeriodStats::count))
			.orElseThrow(() -> new ResourceNotFoundException("No periods found for seller with id " + sellerId));
        
        return BestPeriodDto.builder()
			.sellerId(seller.getId())
			.sellerName(seller.getName())
			.startDate(best.start)
			.endDate(best.end)
			.transactionCount(best.count)
			.periodType(best.period.name())
			.build();
    } // Комментарий на счет выбранного алгоритма, формулировка задания достаточно туманна для различной её трактовки, поэтому я выбрал следующую ;)
    // Искать не лучший период в произвольном промежутке времени (хоть и с заданным окном), а привязать еще и расположение границ, например месяц,
    // именно диапазон времени [1.x, 1.x+1) (с первого числа и до последнего).
}