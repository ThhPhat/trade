package vn.bookstore.the4bookstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.SanPham;
import vn.bookstore.the4bookstore.entity.TacGia;
import vn.bookstore.the4bookstore.repository.SanPhamRepository;
import vn.bookstore.the4bookstore.repository.SanPhamTacGiaRepository;
import vn.bookstore.the4bookstore.repository.TacGiaRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TacGiaService {

    private final TacGiaRepository tacGiaRepository;
    private final SanPhamRepository sanPhamRepository;
    private final SanPhamTacGiaRepository sanPhamTacGiaRepository;

    public List<TacGia> getAll() {
        return tacGiaRepository.findAll();
    }

    public Optional<TacGia> getById(Integer id) {
        return tacGiaRepository.findById(id);
    }

    @Transactional
    public TacGia save(TacGia tacGia) {
        return tacGiaRepository.save(tacGia);
    }

    @Transactional
    public void delete(Integer id) {
        long bookCount = sanPhamTacGiaRepository.countByTacGia_MaTacGia(id);
        if (bookCount > 0) {
            throw new IllegalStateException("Không thể xóa tác giả này vì đang có " + bookCount + " đầu sách liên kết!");
        }
        tacGiaRepository.deleteById(id);
    }

    public List<SanPham> getBooksByAuthor(Integer authorId) {
        return sanPhamRepository.findBooksByTacGiaId(authorId);
    }

    public long countBooksByAuthor(Integer authorId) {
        return sanPhamTacGiaRepository.countByTacGia_MaTacGia(authorId);
    }

    public List<TacGia> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return tacGiaRepository.findAll();
        }
        return tacGiaRepository.findByTenTacGiaContainingIgnoreCase(keyword.trim());
    }
}
