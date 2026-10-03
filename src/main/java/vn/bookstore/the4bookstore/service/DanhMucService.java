package vn.bookstore.the4bookstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bookstore.the4bookstore.entity.DanhMuc;
import vn.bookstore.the4bookstore.repository.DanhMucRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DanhMucService {

    private final DanhMucRepository danhMucRepository;

    public List<DanhMuc> getAllActive() {
        List<DanhMuc> active = danhMucRepository.findAll().stream()
                .filter(dm -> Boolean.TRUE.equals(dm.getTrangThai()))
                .toList();
        // Fallback: nếu chưa set trangThai thì lấy tất cả
        return active.isEmpty() ? danhMucRepository.findAll() : active;
    }

    public Page<DanhMuc> getAllActivePaged(int page, int size) {
        List<DanhMuc> all = getAllActive();
        if (all.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(Math.max(0, page), size), 0);
        }
        int total = all.size();
        int safePage = Math.max(0, page);
        int maxPage = (total - 1) / size;
        if (safePage > maxPage) {
            safePage = maxPage;
        }
        int fromIndex = safePage * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<DanhMuc> subList = all.subList(fromIndex, toIndex);
        return new PageImpl<>(subList, PageRequest.of(safePage, size), total);
    }

    public List<DanhMuc> getAll() {
        return danhMucRepository.findAll();
    }

    public Optional<DanhMuc> getById(Integer id) {
        return danhMucRepository.findById(id);
    }

    @Transactional
    public DanhMuc save(DanhMuc danhMuc) {
        return danhMucRepository.save(danhMuc);
    }

    @Transactional
    public void delete(Integer id) {
        danhMucRepository.deleteById(id);
    }
}

