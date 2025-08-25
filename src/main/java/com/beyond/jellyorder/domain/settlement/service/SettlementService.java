package com.beyond.jellyorder.domain.settlement.service;

import com.beyond.jellyorder.domain.settlement.dto.SettlementDetailDTO;
import com.beyond.jellyorder.domain.settlement.dto.SettlementSummaryDTO;
import com.beyond.jellyorder.domain.settlement.entity.Bucket;
import com.beyond.jellyorder.domain.settlement.repository.SettlementReportRepository;
import com.beyond.jellyorder.domain.settlement.repository.SettlementDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {
    private final SettlementReportRepository reportRepository;
    private final SettlementDetailRepository detailRepository;

    public List<SettlementSummaryDTO> summary(Bucket bucket, LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = switch (bucket) {
            case DAILY   -> reportRepository.aggregateDaily(from, to);
            case WEEKLY  -> reportRepository.aggregateWeekly(from, to);
            case MONTHLY -> reportRepository.aggregateMonthly(from, to);
        };
        return rows.stream().map(r -> {
            String bucketStr = String.valueOf(r[0]);
            long gross = ((Number) r[1]).longValue();
            long net   = ((Number) r[2]).longValue();
            long fee   = ((Number) r[3]).longValue();
            long cnt   = ((Number) r[4]).longValue();
            return new SettlementSummaryDTO(bucketStr, gross, fee, net, cnt);
        }).toList();
    }

    public Page<SettlementDetailDTO> lines(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return detailRepository.findLineItems(from, to, pageable).map(r ->
                new SettlementDetailDTO(
                        (String)  r[0],                     // paidDate
                        (String)  r[1],                     // paymentMethod
                        (String)  r[2],                     // menuName
                        (Integer) r[3],                     // menuPrice
                        (Integer) r[4],                     // quantity
                        (String)  r[5],                     // optionName
                        (Integer) r[6],                     // optionPrice
                        ((Number) r[7]).longValue()         // lineTotal
                )
        );
    }
}
