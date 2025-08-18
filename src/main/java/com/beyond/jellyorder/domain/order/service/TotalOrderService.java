package com.beyond.jellyorder.domain.order.service;

import com.beyond.jellyorder.domain.option.subOption.repository.SubOptionRepository;
import com.beyond.jellyorder.domain.order.dto.OrderOptionResDto;
import com.beyond.jellyorder.domain.order.dto.TotalOrderByTableResDto;
import com.beyond.jellyorder.domain.order.dto.TotalOrderDetailResDto;
import com.beyond.jellyorder.domain.order.dto.TotalOrderLineResDto;
import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import com.beyond.jellyorder.domain.order.repository.OrderMenuRepository;
import com.beyond.jellyorder.domain.order.repository.TotalOrderRepository;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TotalOrderService {

    private final StoreTableRepository storeTableRepository;
    private final TotalOrderRepository totalOrderRepository;
    private final OrderMenuRepository orderMenuRepository;

    /** 테이블 ID로 '열려있는 최신' TotalOrder 주문내역 조회 */
    public TotalOrderByTableResDto getOpenTotalOrderByTable(UUID storeTableId) {
        StoreTable table = storeTableRepository.findById(storeTableId)
                .orElseThrow(() -> new EntityNotFoundException("테이블을 찾을 수 없습니다."));

        TotalOrder totalOrder = totalOrderRepository
                .findTopByStoreTableOrderByOrderedAtDesc(table)
                .filter(to -> to.getEndedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("열려있는 주문이 없습니다."));

        return buildDetail(totalOrder);
    }

    /** totalOrderId로 주문내역 조회(히스토리/결제완료건 포함) */
    public TotalOrderByTableResDto getTotalOrder(UUID totalOrderId) {
        TotalOrder totalOrder = totalOrderRepository.findById(totalOrderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
        return buildDetail(totalOrder);
    }

    private TotalOrderByTableResDto buildDetail(TotalOrder to) {
        // 옵션까지 fetch-join으로 로드
        List<OrderMenu> orderMenus = orderMenuRepository.findAllWithOptionsByTotalOrderId(to.getId());

        List<TotalOrderByTableResDto.Line> lines = orderMenus.stream()
                .map(om -> {
                    // 메뉴 단가: 스냅샷(menuPrice)이 있으면 우선 사용
                    int unitPrice = (om.getMenuPrice() != null ? om.getMenuPrice() : om.getMenu().getPrice());
                    int baseTotal = unitPrice * om.getQuantity();

                    // 옵션 라인 매핑
                    List<TotalOrderByTableResDto.OptionLine> optionLines = om.getOrderMenuOptionList().stream()
                            .map(omo -> {
                                int lineTotal = omo.getOptionPrice();
                                String mainName = (omo.getSubOption() != null && omo.getSubOption().getMainOption() != null)
                                        ? omo.getSubOption().getMainOption().getName()
                                        : null;

                                return TotalOrderByTableResDto.OptionLine.builder()
                                        .mainOptionName(mainName)
                                        .subOptionName(omo.getOptionName())   // 주문 시점 스냅샷
                                        .price(omo.getOptionPrice())          // 주문 시점 스냅샷
                                        .lineTotal(lineTotal)
                                        .build();
                            })
                            .toList();

                    int optionTotal = optionLines.stream().mapToInt(TotalOrderByTableResDto.OptionLine::getLineTotal).sum();

                    return TotalOrderByTableResDto.Line.builder()
                            .orderMenuId(om.getId())
                            .menuId(om.getMenu().getId())
                            .menuName(om.getMenuName() != null ? om.getMenuName() : om.getMenu().getName()) // 스냅샷 우선
                            .menuUnitPrice(unitPrice)
                            .quantity(om.getQuantity())
                            .linePrice(baseTotal + optionTotal) // 메뉴 + 옵션 합
                            .options(optionLines)
                            .build();
                })
                .toList();

        // 총액/총수량
        int calcTotalPrice = lines.stream().mapToInt(TotalOrderByTableResDto.Line::getLinePrice).sum();
        int calcTotalCount = to.getCount() != null ? to.getCount()
                : orderMenus.stream().mapToInt(OrderMenu::getQuantity).sum();

        return TotalOrderByTableResDto.builder()
                .totalOrderId(to.getId())
                .storeTableId(to.getStoreTable().getId())
                .orderedAt(to.getOrderedAt())
                .totalPrice(to.getTotalPrice() != null ? to.getTotalPrice() : calcTotalPrice)
                .totalCount(calcTotalCount)
                .lines(lines)
                .build();
    }
}