package ru.nsu.shift.crm.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.nsu.shift.crm.dto.BestPeriodDto;
import ru.nsu.shift.crm.dto.SellerDto;
import ru.nsu.shift.crm.enums.Period;
import ru.nsu.shift.crm.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/best-seller
     *
     * Returns the seller with the highest total transaction amount for the specified period.
     *
     * @param period        DAY | MONTH | QUARTER | YEAR (required)
     * @param referenceDate format: dd-MM-yyyy (optional, defaults to today)
     *
     * Example: GET /api/analytics/best-seller?period=MONTH&referenceDate=01-03-2024
     */
    @GetMapping("/best-seller")
    public ResponseEntity<SellerDto> getMostProductiveSeller(
            @RequestParam Period period,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate referenceDate) {
        return ResponseEntity.ok(analyticsService.getMostProductiveSeller(period, referenceDate));
    }

    /**
     * GET /api/analytics/sellers-below-threshold
     *
     * Returns sellers whose total transaction amount in the given date range is below the threshold.
     *
     * @param startDate format: dd-MM-yyyy (inclusive)
     * @param endDate   format: dd-MM-yyyy (inclusive)
     * @param threshold maximum total amount (exclusive)
     *
     * Example: GET /api/analytics/sellers-below-threshold?startDate=01-01-2024&endDate=31-03-2024&threshold=5000
     */
    @GetMapping("/sellers-below-threshold")
    public ResponseEntity<List<SellerDto>> getSellersWithTotalBelow(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate,
            @RequestParam BigDecimal threshold) {
        return ResponseEntity.ok(analyticsService.getSellersWithTotalBelow(startDate, endDate, threshold));
    }

    /**
     * GET /api/analytics/best-period/{sellerId}
     *
     * (Optional) Returns the best (densest) transaction period for a given seller.
     *
     * Example: GET /api/analytics/best-period/1
     */
    @GetMapping("/best-period/{sellerId}")
    public ResponseEntity<BestPeriodDto> getBestPeriodForSeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(analyticsService.getBestPeriodForSeller(sellerId));
    }
}
