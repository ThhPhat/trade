package vn.bookstore.the4bookstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.NhaXuatBan;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.repository.NhaXuatBanRepository;
import vn.bookstore.the4bookstore.repository.SanPhamRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NhaXuatBanService {

    private final NhaXuatBanRepository nhaXuatBanRepository;
    private final SanPhamRepository sanPhamRepository;

    public List<NhaXuatBan> getAll() {
        return nhaXuatBanRepository.findAll();
    }

    public Optional<NhaXuatBan> getById(Integer id) {
        return nhaXuatBanRepository.findById(id);
    }

    @Transactional
    public NhaXuatBan save(NhaXuatBan nxb) {
        return nhaXuatBanRepository.save(nxb);
    }

    @Transactional
    public void delete(Integer id) {
        long bookCount = sanPhamRepository.countByNhaXuatBan_MaNXB(id);
        if (bookCount > 0) {
            throw new IllegalStateException("Không thể xóa nhà xuất bản này vì đang có " + bookCount + " đầu sách liên kết!");
        }
        nhaXuatBanRepository.deleteById(id);
    }

    public List<SanPham> getBooksByPublisher(Integer publisherId) {
        return sanPhamRepository.findByNhaXuatBan_MaNXB(publisherId);
    }

    public long countBooksByPublisher(Integer publisherId) {
        return sanPhamRepository.countByNhaXuatBan_MaNXB(publisherId);
    }

    public List<NhaXuatBan> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return nhaXuatBanRepository.findAll();
        }
        return nhaXuatBanRepository.findByTenNXBContainingIgnoreCase(keyword.trim());
    }
}
