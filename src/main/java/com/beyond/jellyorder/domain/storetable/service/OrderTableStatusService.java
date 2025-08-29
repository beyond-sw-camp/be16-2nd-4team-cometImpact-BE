package com.beyond.jellyorder.domain.storetable.service;

import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuStatus;
import com.beyond.jellyorder.domain.menu.repository.MenuRepository;
import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import com.beyond.jellyorder.domain.option.subOption.repository.SubOptionRepository;
import com.beyond.jellyorder.domain.order.entity.*;
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
    private final SubOptionRepository subOptionRepository;

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
                            || table.getStatus() == TableStatus.STANDBY) {
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

    // 주문 테이블 상세 조회
    @Transactional(readOnly = true)
    public List<OrderTableDetailResDTO> getTableOrderDetail(UUID totalOrderId) {
        TotalOrder totalOrder = totalOrderRepository.findById(totalOrderId)
                .orElseThrow(() -> new EntityNotFoundException("해당 전체 주문이 없습니다."));

        return totalOrder.getUnitOrderList().stream()
                .map(unitOrder -> {
                    List<OrderMenu> orderMenus =
                            orderMenuRepository.findAllByUnitOrderIdWithOptions(unitOrder.getId());

                    List<OrderMenuDetailPrice> orderMenuList = orderMenus.stream()
                            .map(orderMenu -> {
                                List<OrderMenuOptionDetail> menuOptionList = orderMenu.getOrderMenuOptionList().stream()
                                        .map(OrderMenuOptionDetail::from)
                                        .toList();

                                return OrderMenuDetailPrice.from(orderMenu, menuOptionList);
                            }).toList();

                    return OrderTableDetailResDTO.from(unitOrder, orderMenuList);
                }).toList();
    }

    /**
     * 필수 로직 변환 작업
     * 추후 redis 재고 도입으로 인한 리팩토링 필요함.
     */
    @Transactional
    public void updateOrderTable(List<OrderTableUpdateReqDTO> dtoList) {

        for (OrderTableUpdateReqDTO dto : dtoList) {
            UnitOrder unitOrder = unitOrderRepository.findById(dto.getUnitOrderId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 단건 주문이 없습니다."));
            if (!unitOrder.getStatus().equals(OrderStatus.COMPLETE)) {
                throw new IllegalArgumentException("주문 완료되지 않은 단건 주문입니다.");
            }

            // 1) 기존 주문메뉴(+옵션) 가져오기
            List<OrderMenu> currentOrderMenus = orderMenuRepository.findAllByUnitOrderIdWithOptions(unitOrder.getId());

            // 기존 totalCount 합계 계산
            Integer oldTotalCount = currentOrderMenus.stream()
                    .mapToInt(OrderMenu::getQuantity)
                    .sum();

            // 2-1) 재고/오늘판매량 '복구'
            for (OrderMenu om : currentOrderMenus) {
                int qty = om.getQuantity();
                Menu menu = om.getMenu();
                // 추후 redis도입으로 인한 동시성 이슈 해결
                menu.decreaseSalesToday(qty);
            }

            // 2-2) orderMenuOption(자식)삭제, orderMenu(부모)삭제
            if (!currentOrderMenus.isEmpty()) {
                for (OrderMenu om : currentOrderMenus) {
                    om.getOrderMenuOptionList().clear(); // orphanRemoval = true라면 자식 먼저 DELETE
                }
                orderMenuRepository.deleteAll(currentOrderMenus); // 부모 삭제
            }

            // 추가없이 전체 삭제 시 상태값 'CANCEL' 로 변경.
            if (dto.getMenuDetailList() == null || dto.getMenuDetailList().isEmpty()) {
                // 값들이 null값이면 취소로 변경
                unitOrder.updateOrderStatus(OrderStatus.CANCEL);
                unitOrder.getTotalOrder().updateCount(-oldTotalCount);
                continue;
            }

            // 3) 요청 dto 대로 신규 주문 생성
            List<OrderMenu> newOrderMenuList = new ArrayList<>();
            // 각 menu별 주문 생성
            for (MenuDetail md : dto.getMenuDetailList()) {
                if (md.getQuantity() == null || md.getQuantity() <= 0) {
                    throw new IllegalArgumentException("메뉴 수량은 1 이상이어야 합니다.");
                }

                Menu menu = menuRepository.findById(md.getMenuId())
                        .orElseThrow(() -> new EntityNotFoundException("메뉴를 찾을 수 없습니다."));

                //==재고 체크==// - 추후 redis도입으로 인한 동시성 이슈 해결
                int addQty = md.getQuantity();

                // 1. 품절 여부 확인
                if (!menu.getStockStatus().equals(MenuStatus.ON_SALE)) {
                    throw new IllegalArgumentException("품절 처리된 메뉴를 주문하셨습니다.");
                }

                // 2. 주문 수량 제한 여부 확인
                if (menu.getSalesLimit().equals(-1) || (menu.getSalesLimit() - menu.getSalesToday()) >= addQty) {
                    menu.increaseSalesToday(addQty);
                } else {
                    throw new IllegalArgumentException(menu.getName() + "의 재고가 부족합니다.");
                }

                // order_menu 생성
                OrderMenu newOrderMenu = OrderMenu.builder()
                        .unitOrder(unitOrder)
                        .menu(menu)
                        .quantity(addQty)
                        .build();
                orderMenuRepository.save(newOrderMenu);

                // 메뉴에 대한 옵션 값 저장
                // 옵션이 여러 번 동일 id로 들어와도 "그 수만큼" 개별 행으로 저장
                if (md.getOptionDetailList() != null && !md.getOptionDetailList().isEmpty()) {
                    for (MenuOptionDetail opt : md.getOptionDetailList()) {
                        SubOption sub = subOptionRepository.findById(opt.getMenuOptionId())
                                .orElseThrow(() -> new EntityNotFoundException("옵션(서브옵션)을 찾을 수 없습니다."));

                        OrderMenuOption omo = OrderMenuOption.builder()
                                .orderMenu(newOrderMenu)             // 부모 연결
                                .subOption(sub)
                                .optionName(sub.getName())     // 스냅샷 저장
                                .optionPrice(sub.getPrice())   // 스냅샷 저장
                                .build();

                        // cascade로 함께 저장
                        newOrderMenu.getOrderMenuOptionList().add(omo);
                    }
                }

                newOrderMenuList.add(newOrderMenu);
            }

            int newTotalCount = newOrderMenuList.stream()
                    .mapToInt(OrderMenu::getQuantity)
                    .sum();
            int diff = newTotalCount - oldTotalCount;
            unitOrder.updateUnitCount(newTotalCount);
            unitOrder.getTotalOrder().updateCount(diff);

            // 4) unitOrder에 신규 라인 갈아끼우기
            if (unitOrder.getOrderMenus() != null) {
                unitOrder.getOrderMenus().clear();
                unitOrder.getOrderMenus().addAll(newOrderMenuList);
            }
        }
    }



}
