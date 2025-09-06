package com.beyond.jellyorder.domain.sales.repository;

import com.beyond.jellyorder.domain.sales.entity.Sales;
import com.beyond.jellyorder.domain.sales.entity.SalesStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SalesRepository extends JpaRepository<Sales, UUID> {
    Optional<Sales> findByTotalOrderId(UUID totalOrderId);

    // SalesRepository
    @Query("""
  select coalesce(sum(s.totalAmount), 0),
         coalesce(sum(s.settlementAmount), 0),
         count(s)
    from Sales s
    join s.totalOrder t
    join t.storeTable st
   where st.store.id = :storeId
     and s.status = :completed
     and s.storeOpenClose.id = :openCloseId
""")
    Object[] summarizeByOpenClose(@Param("storeId") UUID storeId,
                                  @Param("openCloseId") UUID openCloseId,
                                  @Param("completed") SalesStatus completed);

}
