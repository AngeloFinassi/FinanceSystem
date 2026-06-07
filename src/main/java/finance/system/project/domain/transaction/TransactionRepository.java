package finance.system.project.domain.transaction;

import finance.system.project.domain.category.CategoryEntity;
import finance.system.project.domain.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    Optional<TransactionEntity> findByIdAndUser(UUID id, UserEntity user);

    @Query("""
        SELECT t FROM TransactionEntity t
        LEFT JOIN FETCH t.account
        LEFT JOIN FETCH t.category
        WHERE t.user = :user
          AND (:type     IS NULL OR t.type            = :type)
          AND (:accountId  IS NULL OR t.account.id    = :accountId)
          AND (:categoryId IS NULL OR t.category.id   = :categoryId)
          AND (:startDate  IS NULL OR t.transactionDate >= :startDate)
          AND (:endDate    IS NULL OR t.transactionDate <= :endDate)
          AND (:isPaid     IS NULL OR t.isPaid         = :isPaid)
        ORDER BY t.transactionDate DESC
        """)
    Page<TransactionEntity> findAllWithFilters(
            @Param("user")       UserEntity user,
            @Param("type")       TransactionType type,
            @Param("accountId")  UUID accountId,
            @Param("categoryId") UUID categoryId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate,
            @Param("isPaid")     Boolean isPaid,
            Pageable pageable);

    // used for budget service to calculate spent amount for a category/month/year
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t
        WHERE t.user     = :user
          AND t.category = :category
          AND t.type     = :type
          AND MONTH(t.transactionDate) = :month
          AND YEAR(t.transactionDate)  = :year
        """)
    Optional<BigDecimal> sumByUserAndCategoryAndTypeAndMonthAndYear(
            @Param("user")     UserEntity user,
            @Param("category") CategoryEntity category,
            @Param("type")     TransactionType type,
            @Param("month")    int month,
            @Param("year")     int year);
}