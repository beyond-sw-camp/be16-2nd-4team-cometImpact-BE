package com.beyond.jellyorder.domain.storetable.service;

import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.repository.MenuRepository;
import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import com.beyond.jellyorder.domain.order.repository.OrderMenuRepository;
import com.beyond.jellyorder.domain.order.repository.TotalOrderRepository;
import com.beyond.jellyorder.domain.order.repository.UnitOrderRepository;
import com.beyond.jellyorder.domain.storetable.dto.orderTableStatus.*;
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
    private final MenuRepository menuRepository;

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

    @Transactional(readOnly = true)
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

    /**
     * 필수 로직 변환 작업
     * 추후 redis 재고 도입으로 인한 리팩토링 필요함.
     */
    public void updateOrderTable(List<OrderTableUpdateReqDTO> dtoList) {
        for (OrderTableUpdateReqDTO dto : dtoList) {
            UnitOrder unitOrder = unitOrderRepository.findById(dto.getUnitOrderId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 단건 주문이 없습니다."));

            // 조건1. 주문 전체 삭제 시 -> 재고 수량 증가 및 상태값 변경
            if (dto.getIsDeleteAll()) {
                for (OrderMenu om : unitOrder.getOrderMenus()) {
                    Menu menu = om.getMenu();
                    if (menu.getSalesLimit() != null && menu.getSalesLimit() != -1L) {

                        // RDB 재고 증가, 하루 판매 감소 로직.
                        // <<!- 추후 redis 도입 필요->>
                        menu.increaseSalesLimit(Long.valueOf(om.getQuantity()));
                        menu.decreaseSalesToday(om.getQuantity());
                    }
                }
                unitOrder.updateOrderStatus(OrderStatus.CANCEL);
                break;
            }

            // 조건2. 증가 상품이 있을 경우
            if (!dto.getAddMenuList().isEmpty()) {
                for (MenuDetail menuDetail : dto.getAddMenuList()) {
                    Optional<OrderMenu> orderMenuOpt = orderMenuRepository.findByMenuIdAndUnitOrderId(menuDetail.getId(), unitOrder.getId());

                    // 기존 OrderMenu의 상품이 존재할 시 증가 로직
                    if (orderMenuOpt.isPresent()) {
                        OrderMenu orderMenu = orderMenuOpt.get();
                        Integer increaseQuantity = menuDetail.getQuantity();

                        if (orderMenu.getMenu().getSalesLimit() < increaseQuantity) {
                            throw new IllegalArgumentException("해당 품목의 재고가 부족합니다.");
                        }
                        orderMenu.updateQuantity(orderMenu.getQuantity() + increaseQuantity);
                        orderMenu.getMenu().decreaseSalesLimit(Long.valueOf(increaseQuantity));
                        orderMenu.getMenu().increaseSalesToday(increaseQuantity);
                    } else {
                        Menu menu = menuRepository.findById(menuDetail.getId())
                                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴를 찾을 수 없습니다."));

                        // 재고 체크
                        if (menu.getSalesLimit() < menuDetail.getQuantity()) {
                            throw new IllegalArgumentException("해당 품목의 재고가 부족합니다.");
                        }

                        OrderMenu newOrderMenu = OrderMenu.builder()
                                .unitOrder(unitOrder)
                                .menu(menu)
                                .quantity(menuDetail.getQuantity())
                                .build();

                        orderMenuRepository.save(newOrderMenu);

                        // 재고 차감 및 오늘 판매량 증가
                        menu.decreaseSalesLimit(Long.valueOf(menuDetail.getQuantity()));
                        menu.increaseSalesToday(menuDetail.getQuantity());
                    }

                }
            }


            // 조건3. 삭제 상품이 있을 경우
            if (!dto.getDeleteMenuList().isEmpty()) {
                for (MenuDetail menuDetail : dto.getDeleteMenuList()) {
                    OrderMenu orderMenu = orderMenuRepository.findByMenuIdAndUnitOrderId(menuDetail.getId(), unitOrder.getId())
                            .orElseThrow(() -> new IllegalArgumentException("해당 주문 메뉴를 찾을 수 없습니다."));

                    Integer decreaseQuantity = menuDetail.getQuantity();

                    // 현재 주문 수량보다 많은 수량을 삭제하려는 경우 예외 처리
                    if (orderMenu.getQuantity() < decreaseQuantity) {
                        throw new IllegalArgumentException("삭제 수량이 현재 주문 수량보다 많습니다.");
                    }

                    // 수량 업데이트 (남은 수량이 0이면 삭제)
                    int remainingQuantity = orderMenu.getQuantity() - decreaseQuantity;
                    if (remainingQuantity > 0) {
                        orderMenu.updateQuantity(remainingQuantity);
                    } else {
                        orderMenuRepository.delete(orderMenu);
                    }

                    // 재고 복구 및 오늘 판매량 감소
                    orderMenu.getMenu().increaseSalesLimit(Long.valueOf(decreaseQuantity));
                    orderMenu.getMenu().decreaseSalesToday(decreaseQuantity);
                }
            }

        }
    }


}
