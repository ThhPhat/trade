package vn.bookstore.the4bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cau_hinh_he_thong")
public class CauHinhHeThong {

    @Id
    @Column(name = "ma_cau_hinh", length = 50)
    private String maCauHinh;

    @Column(name = "gia_tri", nullable = false)
    private String giaTri;

    @Column(name = "mo_ta")
    private String moTa;

    public CauHinhHeThong() {
    }

    public CauHinhHeThong(String maCauHinh, String giaTri, String moTa) {
        this.maCauHinh = maCauHinh;
        this.giaTri = giaTri;
        this.moTa = moTa;
    }

    public String getMaCauHinh() {
        return maCauHinh;
    }

    public void setMaCauHinh(String maCauHinh) {
        this.maCauHinh = maCauHinh;
    }

    public String getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(String giaTri) {
        this.giaTri = giaTri;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
