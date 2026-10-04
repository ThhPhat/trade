package vn.bookstore.the4bookstore.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import vn.bookstore.the4bookstore.dto.MonthlyRevenueDTO;
import vn.bookstore.the4bookstore.dto.TopSellingBookDTO;
import vn.bookstore.the4bookstore.repository.ChiTietDonHangRepository;
import vn.bookstore.the4bookstore.repository.DonHangRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final DonHangRepository donHangRepository;

    private final ChiTietDonHangRepository chiTietDonHangRepository;

    public ReportService(DonHangRepository donHangRepository,
                         ChiTietDonHangRepository chiTietDonHangRepository) {
        this.donHangRepository = donHangRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
    }

    @PersistenceContext
    private EntityManager entityManager;

    public BigDecimal getRevenueByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        Long revenue = donHangRepository.getRevenueByDateRange(startOfDay, endOfDay);
        return revenue != null ? new BigDecimal(revenue) : BigDecimal.ZERO;
    }

    public List<MonthlyRevenueDTO> getRevenueByMonth() {
        String sql = "CALL sp_DoanhThuTheoThang()";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        List<MonthlyRevenueDTO> dtos = new ArrayList<>();
        for (Object[] row : results) {
            Integer year = ((Number) row[0]).intValue();
            Integer month = ((Number) row[1]).intValue();
            BigDecimal revenue = new BigDecimal(((Number) row[2]).longValue());
            String monthStr = String.format("%04d-%02d", year, month);
            dtos.add(new MonthlyRevenueDTO(monthStr, revenue));
        }
        return dtos;
    }

    public List<TopSellingBookDTO> getTopSellingBooks() {
        String sql = "CALL sp_TopSachBanChay(10)";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        List<TopSellingBookDTO> dtos = new ArrayList<>();
        for (Object[] row : results) {
            Long maSP = ((Number) row[0]).longValue();
            String tenSP = (String) row[1];
            Long soLuongBan = ((Number) row[2]).longValue();
            Integer giaBan = (row.length > 3 && row[3] != null) ? ((Number) row[3]).intValue() : null;
            Long doanhThuDongGop = (row.length > 4 && row[4] != null) ? ((Number) row[4]).longValue() : null;
            dtos.add(new TopSellingBookDTO(maSP, tenSP, soLuongBan, giaBan, doanhThuDongGop));
        }
        return dtos;
    }

    public Long getTodayOrderCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        Long count = donHangRepository.getOrderCountByDateRange(startOfDay, endOfDay);
        return count != null ? count : 0L;
    }

    public Long getThisMonthOrderCount() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonth = startOfMonth.plusMonths(1);

        Long count = donHangRepository.getOrderCountByDateRange(startOfMonth, nextMonth);
        return count != null ? count : 0L;
    }

    public Long getTotalBooksSold() {
        Long total = chiTietDonHangRepository.getTotalBooksSold();
        return total != null ? total : 0L;
    }
}
