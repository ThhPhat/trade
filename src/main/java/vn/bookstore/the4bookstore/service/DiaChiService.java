package vn.bookstore.the4bookstore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.DiaChiGiaoHang;
import vn.bookstore.the4bookstore.entity.KhachHang;
import vn.bookstore.the4bookstore.repository.DiaChiGiaoHangRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DiaChiService {

    private final DiaChiGiaoHangRepository diaChiRepository;

    public DiaChiService(DiaChiGiaoHangRepository diaChiRepository) {
        this.diaChiRepository = diaChiRepository;
    }

    @Transactional(readOnly = true)
    public List<DiaChiGiaoHang> getAddresses(KhachHang khachHang) {
        return diaChiRepository.findByKhachHangOrderByLaMacDinhDescNgayTaoDesc(khachHang);
    }

    @Transactional(readOnly = true)
    public Optional<DiaChiGiaoHang> getDefaultAddress(KhachHang khachHang) {
        return diaChiRepository.findByKhachHangAndLaMacDinhTrue(khachHang);
    }

    public DiaChiGiaoHang addAddress(KhachHang khachHang, DiaChiGiaoHang diaChi) {
        diaChi.setKhachHang(khachHang);
        List<DiaChiGiaoHang> existing = diaChiRepository.findByKhachHangOrderByLaMacDinhDescNgayTaoDesc(khachHang);
        if (existing.isEmpty() || Boolean.TRUE.equals(diaChi.getLaMacDinh())) {
            // Đặt làm mặc định
            existing.forEach(d -> {
                d.setLaMacDinh(false);
                diaChiRepository.save(d);
            });
            diaChi.setLaMacDinh(true);
        }
        return diaChiRepository.save(diaChi);
    }

    public DiaChiGiaoHang updateAddress(KhachHang khachHang, Integer maDiaChi, DiaChiGiaoHang updated) {
        DiaChiGiaoHang current = diaChiRepository.findById(maDiaChi)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ: " + maDiaChi));
        if (!current.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
            throw new SecurityException("Không có quyền chỉnh sửa địa chỉ này");
        }

        current.setTenNguoiNhan(updated.getTenNguoiNhan());
        current.setSoDienThoai(updated.getSoDienThoai());
        current.setDiaChiChiTiet(updated.getDiaChiChiTiet());
        current.setPhuongXa(updated.getPhuongXa());
        current.setQuanHuyen(updated.getQuanHuyen());
        current.setTinhThanh(updated.getTinhThanh());

        if (Boolean.TRUE.equals(updated.getLaMacDinh())) {
            setDefaultAddress(khachHang, maDiaChi);
        }

        return diaChiRepository.save(current);
    }

    public void setDefaultAddress(KhachHang khachHang, Integer maDiaChi) {
        List<DiaChiGiaoHang> list = diaChiRepository.findByKhachHangOrderByLaMacDinhDescNgayTaoDesc(khachHang);
        for (DiaChiGiaoHang d : list) {
            d.setLaMacDinh(d.getMaDiaChi().equals(maDiaChi));
            diaChiRepository.save(d);
        }
    }

    public void deleteAddress(KhachHang khachHang, Integer maDiaChi) {
        DiaChiGiaoHang current = diaChiRepository.findById(maDiaChi)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ: " + maDiaChi));
        if (!current.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
            throw new SecurityException("Không có quyền xóa địa chỉ này");
        }
        diaChiRepository.delete(current);
    }
}
