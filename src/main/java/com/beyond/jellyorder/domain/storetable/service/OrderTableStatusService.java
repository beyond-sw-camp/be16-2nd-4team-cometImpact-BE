package com.beyond.jellyorder.domain.storetable.service;

import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import com.beyond.jellyorder.domain.order.repository.OrderMenuRepository;
import com.beyond.jellyorder.domain.order.repository.TotalOrderRepository;
import com.beyond.jellyorder.domain.order.repository.UnitOrderRepository;
import com.beyond.jellyorder.domain.storetable.dto.orderTableStatus.OrderMenuDetail;
import com.beyond.jellyorder.domain.storetable.dto.orderTableStatus.OrderMenuDetailPrice;
import com.beyond.jellyorder.domain.storetable.dto.orderTableStatus.OrderTableDetailResDTO;
import com.beyond.jellyorder.domain.storetable.dto.orderTableStatus.OrderTableResDTO;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.TableStatus;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import com.beyond.jellyorder.domain.storetable.repository.ZoneRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
//@CacheConfig(cacheManager = "orderTableCacheManager")
public class OrderTableStatusService {

    private final ZoneRepository zoneRepository;
    private final StoreTableRepository storeTableRepository;
    private final TotalOrderRepository totalOrderRepository;
    private final UnitOrderRepository unitOrderRepository;
    private final OrderMenuRepository orderMenuRepository;

    /**
     * 추후 N+1을 해결하는 fetch join, 프로젝션 도입 예정
     */
    public List<OrderTableResDTO> getTablesByZone(UUID zoneId) {
        // 1. zoneId에 해당하는 모든 테이블 조회
        List<StoreTable> tableList = storeTableRepository.findAllByZoneId(zoneId);

        return tableList.stream()
                .map(table -> {
                    Optional<TotalOrder> totalOrderOpt =
                            totalOrderRepository.findTopByStoreTableOrderByOrderedAtDesc(table);

                    // 테이블에 주문이 없거나 테이블 상태가 EATING이 아니면 null값 만 넘김.
                    if (totalOrderOpt.isEmpty()
                            || table.getStatus() != TableStatus.EATING) {
                        return OrderTableResDTO.from(table, null, Collections.emptyList());
                    }

                    TotalOrder totalOrder = totalOrderOpt.get();
                    // 2. CANCEL이 아닌 UnitOrder 조회
                    List<UnitOrder> unitOrderList = unitOrderRepository
                            .findAllByTotalOrderIdAndStatusNot(totalOrder.getId(), OrderStatus.CANCEL);

                    // 3. UnitOrder리스트에서 메뉴 추출
                    List<OrderMenuDetail> orderMenuDetails = new ArrayList<>();
                    unitOrderList.stream().map(unitOrder -> {
                        return unitOrder.getOrderMenus().stream().map(
                                orderMenu -> orderMenuDetails.add(OrderMenuDetail.from(orderMenu))
                        );
                    });

                    // 4. UnitOrder의 OrderMenu 스트림으로 수집
                    List<OrderMenu> orderMenus = unitOrderList.stream()
                            .flatMap(uo -> uo.getOrderMenus().stream())
                            .toList();

                    // 5. flatMap + groupingBy 로 중복 메뉴 개수 계산
                    Map<String, Long> menuCounts = orderMenus.stream()
                            .flatMap(om -> Collections.nCopies(
                                    om.getQuantity(), om.getMenu().getName()).stream())
                            .collect(Collectors.groupingBy(
                                    Function.identity(), Collectors.counting()));

                    // 6. Map 엔트리 → OrderMenuDetail 리스트 변환
                    List<OrderMenuDetail> details = menuCounts.entrySet().stream()
                            .map(e -> OrderMenuDetail.builder()
                                    .name(e.getKey())
                                    .quantity(e.getValue().intValue())
                                    .build())
                            .collect(Collectors.toList());

                    // 7. DTO 변환 및 반환
                    return OrderTableResDTO.from(table, totalOrder, details);
                })
                .toList();
    }

    public List<OrderTableDetailResDTO> getTableOrderDetail(UUID totalOrderId) {
        TotalOrder totalOrder = totalOrderRepository.findById(totalOrderId)
                .orElseThrow(() -> new EntityNotFoundException("해당 전체 주문이 없습니다."));

        return totalOrder.getUnitOrderList().stream()
                .map(
                        unitOrder -> {
                            // OrderMenuDetailPrice 생성 로직
                            List<OrderMenuDetailPrice> orderMenuList = unitOrder.getOrderMenus().stream()
                                    .map(OrderMenuDetailPrice::from).toList();

                            return OrderTableDetailResDTO.from(unitOrder, orderMenuList);
                        }
                ).toList();
    }


}
