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

//    /**
//     * 필수 로직 변환 작업
//     * 추후 redis 재고 도입으로 인한 리팩토링 필요함.
//     */
//    @Transactional
//    public void updateOrderTable(List<OrderTableUpdateReqDTO> dtoList) {
//        for (OrderTableUpdateReqDTO dto : dtoList) {
//            UnitOrder unitOrder = unitOrderRepository.findById(dto.getUnitOrderId())
//                    .orElseThrow(() -> new EntityNotFoundException("해당 단건 주문이 없습니다."));
//
//            if (!unitOrder.getStatus().equals(OrderStatus.COMPLETE)) {
//                throw new IllegalArgumentException("주문 완료되지 않은 단건 주문입니다.");
//            }
//
//            // 1) 현재 라인(+옵션) 로드
//            List<OrderMenu> currentRows = orderMenuRepository.findAllByUnitOrderIdWithOptions(unitOrder.getId());
//
//            // 2) 재고/오늘판매량 '복구' 후 기존 라인 삭제 (하드 딜리트)
//            for (OrderMenu row : currentRows) {
//                int qty = row.getQuantity();
//                Menu menu = row.getMenu();
//                menu.increaseSalesLimit((long) qty);
//                menu.decreaseSalesToday(qty);
//            }
//            // 자식 먼저 삭제(스키마/연관관계에 따라 cascade면 생략 가능)
//            List<OrderMenuOption> currentOptions = currentRows.stream()
//                    .filter(r -> r.getOrderMenuOptions() != null)
//                    .flatMap(r -> r.getOrderMenuOptions().stream())
//                    .toList();
//            if (!currentOptions.isEmpty()) {
//                orderMenuOptionRepository.deleteAllInBatch(currentOptions);
//            }
//            if (!currentRows.isEmpty()) {
//                orderMenuRepository.deleteAllInBatch(currentRows);
//            }
//
//            // 3) 요청 본문대로 신규 라인 생성
//            if (dto.getMenuDetailList() == null) continue; // 빈 주문으로 교체하는 경우
//
//            List<OrderMenu> created = new ArrayList<>();
//
//            for (MenuDetail md : dto.getMenuDetailList()) {
//                if (md.getQuantity() == null || md.getQuantity() <= 0) {
//                    throw new IllegalArgumentException("메뉴 수량은 1 이상이어야 합니다.");
//                }
//
//                Menu menu = menuRepository.findById(md.getMenuId())
//                        .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));
//
//                // 재고 체크
//                int addQty = md.getQuantity();
//                if (menu.getSalesLimit() < addQty) {
//                    throw new IllegalArgumentException("해당 품목의 재고가 부족합니다.");
//                }
//
//                // order_menu 생성
//                OrderMenu newRow = OrderMenu.builder()
//                        .unitOrder(unitOrder)
//                        .menu(menu)
//                        .quantity(addQty)
//                        .build();
//                orderMenuRepository.save(newRow);
//
//                // 옵션이 여러 번 동일 id로 들어오면 합쳐서 저장(멀티셋)
//                if (md.getOptionDetailList() != null && !md.getOptionDetailList().isEmpty()) {
//                    Map<UUID, Integer> optionAgg = new HashMap<>();
//                    for (MenuOptionDetail opt : md.getOptionDetailList()) {
//                        optionAgg.merge(opt.getMenuOptionId(), 1, Integer::sum);
//                    }
//
//                    for (Map.Entry<UUID, Integer> e : optionAgg.entrySet()) {
//                        OptionItem item = optionItemRepository.findById(e.getKey())
//                                .orElseThrow(() -> new IllegalArgumentException("옵션 아이템을 찾을 수 없습니다."));
//
//                        OrderMenuOption omo = OrderMenuOption.builder()
//                                .orderMenu(newRow)
//                                .optionItem(item)
//                                .quantity(e.getValue())
//                                // 스냅샷 컬럼을 쓰는 경우에만
//                                .optionNameSnapshot(item.getName())
//                                .priceDeltaSnapshot(item.getPriceDelta())
//                                .build();
//                        orderMenuOptionRepository.save(omo);
//                    }
//                }
//
//                // 재고/오늘판매량 반영
//                menu.decreaseSalesLimit((long) addQty);
//                menu.increaseSalesToday(addQty);
//
//                created.add(newRow);
//            }
//
//            // 4) unitOrder에 신규 라인 갈아끼우기 (양방향 컬렉션 유지 시)
//            if (unitOrder.getOrderMenus() != null) {
//                unitOrder.getOrderMenus().clear();
//                unitOrder.getOrderMenus().addAll(created);
//            }
//            // 단방향이거나 cascade 설정이면 위 블록은 생략 가능
//        }
//    }



}
