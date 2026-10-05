-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: ql_nhasach
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `ql_nhasach`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `ql_nhasach` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `ql_nhasach`;

--
-- Table structure for table `cau_hinh_he_thong`
--

DROP TABLE IF EXISTS `cau_hinh_he_thong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cau_hinh_he_thong` (
  `ma_cau_hinh` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gia_tri` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `mo_ta` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ma_cau_hinh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cau_hinh_he_thong`
--

LOCK TABLES `cau_hinh_he_thong` WRITE;
/*!40000 ALTER TABLE `cau_hinh_he_thong` DISABLE KEYS */;
INSERT INTO `cau_hinh_he_thong` VALUES ('MIN_REVIEW_LENGTH','50','Số ký tự tối thiểu của bài đánh giá'),('PHI_SAN_MAC_DINH','5.0','Tỷ lệ phần trăm phí hoa hồng sàn mặc định (%)');
/*!40000 ALTER TABLE `cau_hinh_he_thong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chi_tiet_don_hang`
--

DROP TABLE IF EXISTS `chi_tiet_don_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chi_tiet_don_hang` (
  `madh` int NOT NULL,
  `masp` int NOT NULL,
  `don_gia` int NOT NULL,
  `so_luong` int NOT NULL,
  PRIMARY KEY (`madh`,`masp`),
  KEY `FK98u87ds1aj1339w293i4er4fx` (`masp`),
  CONSTRAINT `FK98u87ds1aj1339w293i4er4fx` FOREIGN KEY (`masp`) REFERENCES `san_pham` (`masp`),
  CONSTRAINT `FKodihdwuetirdvsli7a1oobwnq` FOREIGN KEY (`madh`) REFERENCES `don_hang` (`madh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chi_tiet_don_hang`
--

LOCK TABLES `chi_tiet_don_hang` WRITE;
/*!40000 ALTER TABLE `chi_tiet_don_hang` DISABLE KEYS */;
INSERT INTO `chi_tiet_don_hang` VALUES (1,1,189000,1),(1,3,150000,1),(1,6,245000,1),(1,7,80000,1),(2,7,80000,1);
/*!40000 ALTER TABLE `chi_tiet_don_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chi_tiet_gio_hang`
--

DROP TABLE IF EXISTS `chi_tiet_gio_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chi_tiet_gio_hang` (
  `ma_gio_hang` int NOT NULL,
  `masp` int NOT NULL,
  `so_luong` int NOT NULL,
  PRIMARY KEY (`ma_gio_hang`,`masp`),
  KEY `FKkmq7k0m84x6j0g74rxs4fw0wg` (`masp`),
  CONSTRAINT `FKdqjc7vkyfiup4ydgmhx8sdwii` FOREIGN KEY (`ma_gio_hang`) REFERENCES `gio_hang` (`ma_gio_hang`),
  CONSTRAINT `FKkmq7k0m84x6j0g74rxs4fw0wg` FOREIGN KEY (`masp`) REFERENCES `san_pham` (`masp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chi_tiet_gio_hang`
--

LOCK TABLES `chi_tiet_gio_hang` WRITE;
/*!40000 ALTER TABLE `chi_tiet_gio_hang` DISABLE KEYS */;
INSERT INTO `chi_tiet_gio_hang` VALUES (1,7,1),(4,14,1);
/*!40000 ALTER TABLE `chi_tiet_gio_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chi_tiet_kiem_ke`
--

DROP TABLE IF EXISTS `chi_tiet_kiem_ke`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chi_tiet_kiem_ke` (
  `ma_phieu_kiem_ke` int NOT NULL,
  `masp` int NOT NULL,
  `ly_do` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `so_luong_he_thong` int NOT NULL,
  `so_luong_thuc_te` int NOT NULL,
  PRIMARY KEY (`ma_phieu_kiem_ke`,`masp`),
  KEY `FKghnffbkpvpnp3ahq0g43y7e0g` (`masp`),
  CONSTRAINT `FKghnffbkpvpnp3ahq0g43y7e0g` FOREIGN KEY (`masp`) REFERENCES `san_pham` (`masp`),
  CONSTRAINT `FKkc6e2jvl6hvhqtm42mr6xfebi` FOREIGN KEY (`ma_phieu_kiem_ke`) REFERENCES `phieu_kiem_ke` (`ma_phieu_kiem_ke`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chi_tiet_kiem_ke`
--

LOCK TABLES `chi_tiet_kiem_ke` WRITE;
/*!40000 ALTER TABLE `chi_tiet_kiem_ke` DISABLE KEYS */;
/*!40000 ALTER TABLE `chi_tiet_kiem_ke` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chi_tiet_phieu_nhap`
--

DROP TABLE IF EXISTS `chi_tiet_phieu_nhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chi_tiet_phieu_nhap` (
  `mapn` int NOT NULL,
  `masp` int NOT NULL,
  `don_gia_nhap` int NOT NULL,
  `so_luong` int NOT NULL,
  PRIMARY KEY (`mapn`,`masp`),
  KEY `FKdjxtx10fxow826rs8ju4rlcvq` (`masp`),
  CONSTRAINT `FKdjxtx10fxow826rs8ju4rlcvq` FOREIGN KEY (`masp`) REFERENCES `san_pham` (`masp`),
  CONSTRAINT `FKkl3e6ypo8mcm8st8axnq3yj4g` FOREIGN KEY (`mapn`) REFERENCES `phieu_nhap` (`mapn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chi_tiet_phieu_nhap`
--

LOCK TABLES `chi_tiet_phieu_nhap` WRITE;
/*!40000 ALTER TABLE `chi_tiet_phieu_nhap` DISABLE KEYS */;
/*!40000 ALTER TABLE `chi_tiet_phieu_nhap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chi_tiet_tra_hang`
--

DROP TABLE IF EXISTS `chi_tiet_tra_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chi_tiet_tra_hang` (
  `ma_phieu_tra` int NOT NULL,
  `masp` int NOT NULL,
  `don_gia` int NOT NULL,
  `so_luong` int NOT NULL,
  PRIMARY KEY (`ma_phieu_tra`,`masp`),
  KEY `FK78org0kyuit4uqye0lwy15bu` (`masp`),
  CONSTRAINT `FK3dy6w1ltkf3jmrrqk34axdsxt` FOREIGN KEY (`ma_phieu_tra`) REFERENCES `phieu_tra_hang` (`ma_phieu_tra`),
  CONSTRAINT `FK78org0kyuit4uqye0lwy15bu` FOREIGN KEY (`masp`) REFERENCES `san_pham` (`masp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chi_tiet_tra_hang`
--

LOCK TABLES `chi_tiet_tra_hang` WRITE;
/*!40000 ALTER TABLE `chi_tiet_tra_hang` DISABLE KEYS */;
/*!40000 ALTER TABLE `chi_tiet_tra_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `danh_gia`
--

DROP TABLE IF EXISTS `danh_gia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `danh_gia` (
  `ma_danh_gia` int NOT NULL AUTO_INCREMENT,
  `ngay_danh_gia` datetime(6) NOT NULL,
  `noi_dung` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `so_sao` int NOT NULL,
  `trang_thai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `madh` int NOT NULL,
  `makh` int NOT NULL,
  `masp` int NOT NULL,
  PRIMARY KEY (`ma_danh_gia`),
  UNIQUE KEY `UKqc20lbrx414frax35ygtqldwh` (`makh`,`masp`,`madh`),
  KEY `FK2x7pqo1qrotk9d8umuemqa3gi` (`madh`),
  KEY `FK6ngu0jah0wdv2nk6bsxcwygh9` (`masp`),
  CONSTRAINT `FK1s2u0uipc62t77xq81xf9lbed` FOREIGN KEY (`makh`) REFERENCES `khach_hang` (`makh`),
  CONSTRAINT `FK2x7pqo1qrotk9d8umuemqa3gi` FOREIGN KEY (`madh`) REFERENCES `don_hang` (`madh`),
  CONSTRAINT `FK6ngu0jah0wdv2nk6bsxcwygh9` FOREIGN KEY (`masp`) REFERENCES `san_pham` (`masp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `danh_gia`
--

LOCK TABLES `danh_gia` WRITE;
/*!40000 ALTER TABLE `danh_gia` DISABLE KEYS */;
/*!40000 ALTER TABLE `danh_gia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `danh_gia_media`
--

DROP TABLE IF EXISTS `danh_gia_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `danh_gia_media` (
  `ma_media` int NOT NULL AUTO_INCREMENT,
  `ma_danh_gia` int NOT NULL,
  `loai_media` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IMAGE',
  `url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`ma_media`),
  KEY `fk_dgm_danhgia` (`ma_danh_gia`),
  CONSTRAINT `fk_dgm_danhgia` FOREIGN KEY (`ma_danh_gia`) REFERENCES `danh_gia` (`ma_danh_gia`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `danh_gia_media`
--

LOCK TABLES `danh_gia_media` WRITE;
/*!40000 ALTER TABLE `danh_gia_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `danh_gia_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `danh_muc`
--

DROP TABLE IF EXISTS `danh_muc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `danh_muc` (
  `ma_danh_muc` int NOT NULL AUTO_INCREMENT,
  `mo_ta` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ten_danh_muc` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `trang_thai` bit(1) NOT NULL,
  PRIMARY KEY (`ma_danh_muc`),
  UNIQUE KEY `UK5cmp5hxtq0cc1p5u3ofiser6v` (`ten_danh_muc`),
  KEY `idx_dm_trangthai` (`trang_thai`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `danh_muc`
--

LOCK TABLES `danh_muc` WRITE;
/*!40000 ALTER TABLE `danh_muc` DISABLE KEYS */;
INSERT INTO `danh_muc` VALUES (1,'Tác phẩm tiểu thuyết trong và ngoài nước','Tiểu Thuyết & Văn Học',_binary ''),(2,'Kỹ năng sống, tư duy và phát triển cá nhân','Phát Triển Bản Thân',_binary ''),(3,'Kinh doanh, đầu tư và tâm lý tài chính','Kinh Tế & Tài Chính',_binary ''),(4,'Lịch sử nhân loại, vũ trụ và khoa học thường thức','Khoa Học & Lịch Sử',_binary ''),(5,'Tiểu thuyết viễn tưởng kinh điển và hiện đại','Khoa Học Viễn Tưởng',_binary ''),(8,'Sổ tay da, sổ ghi chép, sổ kế hoạch','Sổ Tay',_binary ''),(9,'Các loại bút viết, bút máy, bút bi cao cấp','Bút',_binary ''),(10,'Kẹp sách, đèn đọc sách, thước kẻ và phụ kiện','Dụng Cụ Học Tập & Làm Việc',_binary ''),(11,'Túi vải canvas thời trang phong cách mọt sách','Túi Tote',_binary ''),(12,'Mô hình gỗ 3D Book Nook trang trí giá sách','Mô Hình Book Nook',_binary ''),(13,'Trò chơi boardgame trí tuệ và hộp quà tặng độc đáo','Boardgame & Quà Tặng',_binary '');
/*!40000 ALTER TABLE `danh_muc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dia_chi_giao_hang`
--

DROP TABLE IF EXISTS `dia_chi_giao_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dia_chi_giao_hang` (
  `ma_dia_chi` int NOT NULL AUTO_INCREMENT,
  `makh` int NOT NULL,
  `ten_nguoi_nhan` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `so_dien_thoai` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dia_chi_chi_tiet` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phuong_xa` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quan_huyen` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tinh_thanh` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `la_mac_dinh` bit(1) NOT NULL DEFAULT b'0',
  `ngay_tao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ma_dia_chi`),
  KEY `fk_dc_khachhang` (`makh`),
  CONSTRAINT `fk_dc_khachhang` FOREIGN KEY (`makh`) REFERENCES `khach_hang` (`makh`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dia_chi_giao_hang`
--

LOCK TABLES `dia_chi_giao_hang` WRITE;
/*!40000 ALTER TABLE `dia_chi_giao_hang` DISABLE KEYS */;
/*!40000 ALTER TABLE `dia_chi_giao_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `don_hang`
--

DROP TABLE IF EXISTS `don_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `don_hang` (
  `madh` int NOT NULL AUTO_INCREMENT,
  `dia_chi_giao` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ly_do_huy` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ly_do_tu_choi` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ngay_dat` datetime(6) NOT NULL,
  `ngay_hoan_thanh` datetime(6) DEFAULT NULL,
  `ngay_xac_nhan` datetime(6) DEFAULT NULL,
  `so_dien_thoai_giao` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `tien_giam` int NOT NULL,
  `tong_tien` int NOT NULL,
  `trang_thai` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `makh` int NOT NULL,
  `makm` int DEFAULT NULL,
  `ma_shop` int DEFAULT NULL,
  `ma_nvc` int DEFAULT NULL,
  `phi_van_chuyen` int NOT NULL DEFAULT '0',
  `chiet_khau_app_phan_tram` decimal(5,2) NOT NULL DEFAULT '5.00',
  `tien_phi_san` int NOT NULL DEFAULT '0',
  `tien_thuc_nhan_shop` int NOT NULL DEFAULT '0',
  `phuong_thuc_thanh_toan` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'COD',
  `trang_thai_thanh_toan` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ChuaThanhToan',
  `ly_do_tra_hang` text COLLATE utf8mb4_unicode_ci,
  `phan_quyet_tranh_chap` text COLLATE utf8mb4_unicode_ci,
  `nguoi_xu_ly_tranh_chap` int DEFAULT NULL,
  PRIMARY KEY (`madh`),
  KEY `FKiy9wbkgc3iv3ome6new025n9o` (`makh`),
  KEY `FKmailjslygm19yf3a0vxln8tx6` (`makm`),
  KEY `idx_dh_trangthai` (`trang_thai`),
  KEY `idx_dh_ngaydat` (`ngay_dat`),
  KEY `idx_dh_makh` (`makh`),
  KEY `fk_dh_shop` (`ma_shop`),
  KEY `fk_dh_nvc` (`ma_nvc`),
  KEY `fk_dh_manager` (`nguoi_xu_ly_tranh_chap`),
  CONSTRAINT `fk_dh_manager` FOREIGN KEY (`nguoi_xu_ly_tranh_chap`) REFERENCES `tai_khoan` (`ma_tai_khoan`),
  CONSTRAINT `fk_dh_nvc` FOREIGN KEY (`ma_nvc`) REFERENCES `nha_van_chuyen` (`ma_nvc`),
  CONSTRAINT `fk_dh_shop` FOREIGN KEY (`ma_shop`) REFERENCES `shop` (`ma_shop`),
  CONSTRAINT `FKiy9wbkgc3iv3ome6new025n9o` FOREIGN KEY (`makh`) REFERENCES `khach_hang` (`makh`),
  CONSTRAINT `FKmailjslygm19yf3a0vxln8tx6` FOREIGN KEY (`makm`) REFERENCES `khuyen_mai` (`makm`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `don_hang`
--

LOCK TABLES `don_hang` WRITE;
/*!40000 ALTER TABLE `don_hang` DISABLE KEYS */;
INSERT INTO `don_hang` VALUES (1,'462 nguyễn tri phương dĩ an bình dương',NULL,NULL,'2026-09-25 16:51:51.573846','2026-09-26 09:09:40.443940',NULL,'0903943105',0,664000,'DaGiao',7,NULL,1,1,0,5.00,33200,630800,'COD','ChuaThanhToan',NULL,NULL,NULL),(2,'55/4N đường 42',NULL,NULL,'2026-09-26 01:39:17.042475','2026-09-28 00:54:19.446468','2026-09-27 00:14:33.242588','0999999999',0,80000,'DaHuy',3,NULL,1,1,0,5.00,4000,76000,'COD','ChuaThanhToan',NULL,NULL,NULL);
/*!40000 ALTER TABLE `don_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gio_hang`
--

DROP TABLE IF EXISTS `gio_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gio_hang` (
  `ma_gio_hang` int NOT NULL AUTO_INCREMENT,
  `ngay_cap_nhat` datetime(6) NOT NULL,
  `ngay_tao` datetime(6) NOT NULL,
  `makh` int NOT NULL,
  PRIMARY KEY (`ma_gio_hang`),
  KEY `FKr918hcrsa82ly9duc4jsec5n8` (`makh`),
  CONSTRAINT `FKr918hcrsa82ly9duc4jsec5n8` FOREIGN KEY (`makh`) REFERENCES `khach_hang` (`makh`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gio_hang`
--

LOCK TABLES `gio_hang` WRITE;
/*!40000 ALTER TABLE `gio_hang` DISABLE KEYS */;
INSERT INTO `gio_hang` VALUES (1,'2026-09-26 00:55:51.970082','2026-09-25 15:28:14.102613',7),(2,'2026-09-26 01:39:18.757457','2026-09-26 01:19:01.728064',3),(3,'2026-10-05 08:46:59.974413','2026-09-26 03:28:03.804571',8),(4,'2026-10-05 08:13:51.771602','2026-10-05 08:13:51.747607',6);
/*!40000 ALTER TABLE `gio_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hoa_don`
--

DROP TABLE IF EXISTS `hoa_don`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hoa_don` (
  `mahd` int NOT NULL AUTO_INCREMENT,
  `ngay_lap` datetime(6) NOT NULL,
  `tong_tien_thanh_toan` int NOT NULL,
  `trang_thai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `madh` int NOT NULL,
  `manv` int NOT NULL,
  PRIMARY KEY (`mahd`),
  UNIQUE KEY `UKi5dsqpkn4sb6xnm62vxche09x` (`madh`),
  KEY `FKr67k5gttxaonk5trdfwvcgk80` (`manv`),
  CONSTRAINT `FK83d5kek5uoh8e1kralry52seo` FOREIGN KEY (`madh`) REFERENCES `don_hang` (`madh`),
  CONSTRAINT `FKr67k5gttxaonk5trdfwvcgk80` FOREIGN KEY (`manv`) REFERENCES `nhan_vien` (`manv`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hoa_don`
--

LOCK TABLES `hoa_don` WRITE;
/*!40000 ALTER TABLE `hoa_don` DISABLE KEYS */;
/*!40000 ALTER TABLE `hoa_don` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `khach_hang`
--

DROP TABLE IF EXISTS `khach_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khach_hang` (
  `makh` int NOT NULL AUTO_INCREMENT,
  `dia_chi` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ho_ten` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ngay_dang_ky` datetime(6) NOT NULL,
  `so_dien_thoai` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ma_tai_khoan` int DEFAULT NULL,
  `anh_dai_dien` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`makh`),
  UNIQUE KEY `UK6j1oks4nrqpqnl0b6cnp85vrd` (`so_dien_thoai`),
  UNIQUE KEY `UKiv6nhi0meph4iaotgx5h0yg63` (`ma_tai_khoan`),
  CONSTRAINT `FKchhnalcpr9cvc1leppvfftoh5` FOREIGN KEY (`ma_tai_khoan`) REFERENCES `tai_khoan` (`ma_tai_khoan`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khach_hang`
--

LOCK TABLES `khach_hang` WRITE;
/*!40000 ALTER TABLE `khach_hang` DISABLE KEYS */;
INSERT INTO `khach_hang` VALUES (3,NULL,'khachhang@gmail.com','khachhang','2026-09-23 14:14:58.897340','0999999999',1,NULL),(6,NULL,'thangphatnekea21333@gmail.com','Phat Thanh','2026-09-24 07:49:20.483861','0960083842',16,'https://lh3.googleusercontent.com/a/ACg8ocKSeYPIRBzeR0Bf5Xd8GpYHen1ZwkCbACw6psb1cSGC4ockK2Y=s96-c'),(7,'123 Võ V?n Ngân, TP. Th? ??c, TP.HCM','user@the4bookstore.vn','Ng??i Dùng Test','2026-09-25 14:59:02.906244','0901234567',17,'https://lh3.googleusercontent.com/a/ACg8ocJDaIAqcV6CJeOAwiGReqZcrPiCifrMs8pK3_3qsLwyWDemdA=s96-c'),(8,NULL,'admin@gmail.com','admin','2026-09-26 01:18:02.028991',NULL,4,NULL);
/*!40000 ALTER TABLE `khach_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kho`
--

DROP TABLE IF EXISTS `kho`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kho` (
  `ma_kho` int NOT NULL AUTO_INCREMENT,
  `dia_chi` varchar(255) DEFAULT NULL,
  `ghi_chu` varchar(500) DEFAULT NULL,
  `ngay_tao` datetime(6) NOT NULL,
  `so_dien_thoai` varchar(20) DEFAULT NULL,
  `ten_kho` varchar(100) NOT NULL,
  `trang_thai` varchar(20) NOT NULL,
  PRIMARY KEY (`ma_kho`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kho`
--

LOCK TABLES `kho` WRITE;
/*!40000 ALTER TABLE `kho` DISABLE KEYS */;
INSERT INTO `kho` VALUES (1,'1 vo van ngan','tét','2026-09-26 01:02:31.840711','0909090909','abc','HoatDong');
/*!40000 ALTER TABLE `kho` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kho_hang`
--

DROP TABLE IF EXISTS `kho_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kho_hang` (
  `ma_kho_hang` int NOT NULL AUTO_INCREMENT,
  `muc_toi_da` int NOT NULL,
  `muc_toi_thieu` int NOT NULL,
  `ngay_cap_nhat` datetime(6) NOT NULL,
  `so_luong_ton` int NOT NULL,
  `ma_kho` int NOT NULL,
  `masp` int NOT NULL,
  PRIMARY KEY (`ma_kho_hang`),
  UNIQUE KEY `UKsjpgc3qiiuvu0ajw9vmm39mgn` (`ma_kho`,`masp`),
  KEY `FKl48mosg36hr2h8c41w0q24pit` (`masp`),
  CONSTRAINT `FK4kal9sxy7w4k142gc8dh5fdtq` FOREIGN KEY (`ma_kho`) REFERENCES `kho` (`ma_kho`),
  CONSTRAINT `FKl48mosg36hr2h8c41w0q24pit` FOREIGN KEY (`masp`) REFERENCES `san_pham` (`masp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kho_hang`
--

LOCK TABLES `kho_hang` WRITE;
/*!40000 ALTER TABLE `kho_hang` DISABLE KEYS */;
/*!40000 ALTER TABLE `kho_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `khuyen_mai`
--

DROP TABLE IF EXISTS `khuyen_mai`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khuyen_mai` (
  `makm` int NOT NULL AUTO_INCREMENT,
  `don_toi_thieu` int DEFAULT NULL,
  `gia_tri_giam` int NOT NULL,
  `giam_toi_da` int DEFAULT NULL,
  `loai_giam` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ma_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ngay_bat_dau` datetime(6) NOT NULL,
  `ngay_ket_thuc` datetime(6) NOT NULL,
  `so_luong_da_dung` int NOT NULL,
  `so_luong_toi_da` int DEFAULT NULL,
  `tenkm` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `trang_thai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ap_dung_cho` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `danh_sach_danh_muc_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `danh_sach_san_pham_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `doi_tuong_khach_hang` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gioi_han_moi_khach_hang` int NOT NULL,
  `hien_thi_cong_khai` bit(1) NOT NULL,
  `loai_tru_san_pham_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mo_ta` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `loai_khuyen_mai` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GIAM_GIA_SAN_PHAM',
  `pham_vi` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TOAN_SAN',
  `ma_shop` int DEFAULT NULL,
  PRIMARY KEY (`makm`),
  UNIQUE KEY `UKq8resr8h2u6wfhtgxq4itxssf` (`ma_code`),
  KEY `fk_km_shop` (`ma_shop`),
  CONSTRAINT `fk_km_shop` FOREIGN KEY (`ma_shop`) REFERENCES `shop` (`ma_shop`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khuyen_mai`
--

LOCK TABLES `khuyen_mai` WRITE;
/*!40000 ALTER TABLE `khuyen_mai` DISABLE KEYS */;
INSERT INTO `khuyen_mai` VALUES (7,NULL,20,50000,'PhanTram','KM-UGW9SZ2G','2026-10-02 07:19:00.000000','2026-11-02 07:19:00.000000',0,NULL,'Khai Trương','HoatDong','ALL',NULL,NULL,'ALL',1,_binary '',NULL,NULL,'GIAM_GIA_SAN_PHAM','TOAN_SAN',NULL);
/*!40000 ALTER TABLE `khuyen_mai` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nha_cung_cap`
--

DROP TABLE IF EXISTS `nha_cung_cap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nha_cung_cap` (
  `mancc` int NOT NULL AUTO_INCREMENT,
  `dia_chi` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ma_so_thue` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `so_dien_thoai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tenncc` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `trang_thai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`mancc`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nha_cung_cap`
--

LOCK TABLES `nha_cung_cap` WRITE;
/*!40000 ALTER TABLE `nha_cung_cap` DISABLE KEYS */;
INSERT INTO `nha_cung_cap` VALUES (1,'TP.HCM','contact@phuongnam.com',NULL,'0283833333','Công ty Sách Phương Nam','HoatDong');
/*!40000 ALTER TABLE `nha_cung_cap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nha_van_chuyen`
--

DROP TABLE IF EXISTS `nha_van_chuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nha_van_chuyen` (
  `ma_nvc` int NOT NULL AUTO_INCREMENT,
  `ten_nvc` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phi_co_ban` int NOT NULL DEFAULT '25000',
  `thoi_gian_du_kien` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '2 - 3 ngày',
  `trang_thai` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'HoatDong',
  PRIMARY KEY (`ma_nvc`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nha_van_chuyen`
--

LOCK TABLES `nha_van_chuyen` WRITE;
/*!40000 ALTER TABLE `nha_van_chuyen` DISABLE KEYS */;
INSERT INTO `nha_van_chuyen` VALUES (1,'Giao Hàng Nhanh (GHN)',25000,'2 - 3 ngày','HoatDong'),(2,'Giao Hàng Tiết Kiệm (GHTK)',22000,'3 - 4 ngày','HoatDong'),(3,'Viettel Post',28000,'1 - 2 ngày','HoatDong'),(4,'Hỏa Tốc 2H (Nội thành)',45000,'2 giờ','HoatDong');
/*!40000 ALTER TABLE `nha_van_chuyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nha_xuat_ban`
--

DROP TABLE IF EXISTS `nha_xuat_ban`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nha_xuat_ban` (
  `manxb` int NOT NULL AUTO_INCREMENT,
  `dia_chi` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `so_dien_thoai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tennxb` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`manxb`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nha_xuat_ban`
--

LOCK TABLES `nha_xuat_ban` WRITE;
/*!40000 ALTER TABLE `nha_xuat_ban` DISABLE KEYS */;
INSERT INTO `nha_xuat_ban` VALUES (1,'Hà Nội','nxbhnv@gmail.com','0243822222','Nhà Xuất Bản Hội Nhà Văn'),(2,'TP. Hồ Chí Minh','hopthu@nxbtre.com.vn','02839316289','Nhà Xuất Bản Trẻ'),(3,'Hà Nội','cskh_online@nxbkimdong.com.vn','02439434730','Nhà Xuất Bản Kim Đồng'),(4,'Hà Nội','thegioi@hn.vnn.vn','02438253841','Nhà Xuất Bản Thế Giới'),(5,'Hà Nội','phunuvn@gmail.com','02439710723','Nhà Xuất Bản Phụ Nữ Việt Nam');
/*!40000 ALTER TABLE `nha_xuat_ban` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhan_vien`
--

DROP TABLE IF EXISTS `nhan_vien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhan_vien` (
  `manv` int NOT NULL AUTO_INCREMENT,
  `chuc_vu` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `dia_chi` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ho_ten` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ngay_vao_lam` date DEFAULT NULL,
  `so_dien_thoai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `trang_thai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ma_tai_khoan` int DEFAULT NULL,
  PRIMARY KEY (`manv`),
  UNIQUE KEY `UKetdcxme7ynbys36hi28u9tk4e` (`so_dien_thoai`),
  UNIQUE KEY `UKs931jur9i7px0jt36iev3osli` (`ma_tai_khoan`),
  CONSTRAINT `FKdpk3u6xuawsiksnkklx1pfeyw` FOREIGN KEY (`ma_tai_khoan`) REFERENCES `tai_khoan` (`ma_tai_khoan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhan_vien`
--

LOCK TABLES `nhan_vien` WRITE;
/*!40000 ALTER TABLE `nhan_vien` DISABLE KEYS */;
/*!40000 ALTER TABLE `nhan_vien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_token`
--

DROP TABLE IF EXISTS `password_reset_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiry_date` datetime(6) NOT NULL,
  `ngay_tao` datetime(6) NOT NULL,
  `token` varchar(100) NOT NULL,
  `used` bit(1) NOT NULL,
  `ma_tai_khoan` int NOT NULL,
  `otp_code` varchar(10) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKg0guo4k8krgpwuagos61oc06j` (`token`),
  KEY `FK9ym08qkf9wlr5jc1kclfmx34t` (`ma_tai_khoan`),
  CONSTRAINT `FK9ym08qkf9wlr5jc1kclfmx34t` FOREIGN KEY (`ma_tai_khoan`) REFERENCES `tai_khoan` (`ma_tai_khoan`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_token`
--

LOCK TABLES `password_reset_token` WRITE;
/*!40000 ALTER TABLE `password_reset_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `password_reset_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieu_kiem_ke`
--

DROP TABLE IF EXISTS `phieu_kiem_ke`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieu_kiem_ke` (
  `ma_phieu_kiem_ke` int NOT NULL AUTO_INCREMENT,
  `ghi_chu` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ngay_kiem_ke` datetime(6) NOT NULL,
  `ngay_phe_duyet` datetime(6) DEFAULT NULL,
  `trang_thai` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `manv` int NOT NULL,
  `manvphe_duyet` int DEFAULT NULL,
  `ma_kho` int NOT NULL,
  PRIMARY KEY (`ma_phieu_kiem_ke`),
  KEY `FKivhhrnwvje1osre9o5o97ihxj` (`manv`),
  KEY `FKmaqnbjq6sj13w1q9ttu5pby52` (`manvphe_duyet`),
  KEY `FKgpc5xjviuff0encds3s0sitay` (`ma_kho`),
  KEY `idx_pkk_ngaykiemke` (`ngay_kiem_ke`),
  KEY `idx_pkk_trangthai` (`trang_thai`),
  CONSTRAINT `FKgpc5xjviuff0encds3s0sitay` FOREIGN KEY (`ma_kho`) REFERENCES `kho` (`ma_kho`),
  CONSTRAINT `FKivhhrnwvje1osre9o5o97ihxj` FOREIGN KEY (`manv`) REFERENCES `nhan_vien` (`manv`),
  CONSTRAINT `FKmaqnbjq6sj13w1q9ttu5pby52` FOREIGN KEY (`manvphe_duyet`) REFERENCES `nhan_vien` (`manv`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieu_kiem_ke`
--

LOCK TABLES `phieu_kiem_ke` WRITE;
/*!40000 ALTER TABLE `phieu_kiem_ke` DISABLE KEYS */;
/*!40000 ALTER TABLE `phieu_kiem_ke` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieu_nhap`
--

DROP TABLE IF EXISTS `phieu_nhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieu_nhap` (
  `mapn` int NOT NULL AUTO_INCREMENT,
  `ghi_chu` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ngay_nhap` datetime(6) NOT NULL,
  `tong_tien` int NOT NULL,
  `trang_thai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `mancc` int NOT NULL,
  `manv` int NOT NULL,
  `ma_kho` int NOT NULL,
  PRIMARY KEY (`mapn`),
  KEY `FKbw2u0ber2va865kg948efhlm5` (`mancc`),
  KEY `FKlavt7ihi8dug436gh1vq69w1y` (`manv`),
  KEY `FKqtsuyyf14bh4lge186fqfh79h` (`ma_kho`),
  KEY `idx_pn_ngaynhap` (`ngay_nhap`),
  KEY `idx_pn_trangthai` (`trang_thai`),
  CONSTRAINT `FKbw2u0ber2va865kg948efhlm5` FOREIGN KEY (`mancc`) REFERENCES `nha_cung_cap` (`mancc`),
  CONSTRAINT `FKlavt7ihi8dug436gh1vq69w1y` FOREIGN KEY (`manv`) REFERENCES `nhan_vien` (`manv`),
  CONSTRAINT `FKqtsuyyf14bh4lge186fqfh79h` FOREIGN KEY (`ma_kho`) REFERENCES `kho` (`ma_kho`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieu_nhap`
--

LOCK TABLES `phieu_nhap` WRITE;
/*!40000 ALTER TABLE `phieu_nhap` DISABLE KEYS */;
/*!40000 ALTER TABLE `phieu_nhap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieu_tra_hang`
--

DROP TABLE IF EXISTS `phieu_tra_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieu_tra_hang` (
  `ma_phieu_tra` int NOT NULL AUTO_INCREMENT,
  `ghi_chu` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ly_do` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ngay_tra` datetime(6) NOT NULL,
  `trang_thai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `mancc` int NOT NULL,
  `manv` int NOT NULL,
  PRIMARY KEY (`ma_phieu_tra`),
  KEY `FK4l2i3xyg39ixq625uf8pght9o` (`mancc`),
  KEY `FKjat51cayratbgicvq12d7r800` (`manv`),
  CONSTRAINT `FK4l2i3xyg39ixq625uf8pght9o` FOREIGN KEY (`mancc`) REFERENCES `nha_cung_cap` (`mancc`),
  CONSTRAINT `FKjat51cayratbgicvq12d7r800` FOREIGN KEY (`manv`) REFERENCES `nhan_vien` (`manv`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieu_tra_hang`
--

LOCK TABLES `phieu_tra_hang` WRITE;
/*!40000 ALTER TABLE `phieu_tra_hang` DISABLE KEYS */;
/*!40000 ALTER TABLE `phieu_tra_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `san_pham`
--

DROP TABLE IF EXISTS `san_pham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `san_pham` (
  `masp` int NOT NULL AUTO_INCREMENT,
  `isbn` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gia_ban` int NOT NULL,
  `loaisp` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `mo_ta` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `muc_ton_toi_thieu` int NOT NULL,
  `ngay_tao` datetime(6) NOT NULL,
  `so_luong_ton` int NOT NULL,
  `tensp` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `trang_thai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ma_danh_muc` int NOT NULL,
  `mancc` int DEFAULT NULL,
  `manxb` int DEFAULT NULL,
  `ma_shop` int NOT NULL DEFAULT '1',
  `trang_thai_khoa` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BinhThuong',
  `so_luong_da_ban` int NOT NULL DEFAULT '0',
  `hinh_anh` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`masp`),
  UNIQUE KEY `UK400ysr2y7n5m2ehwyqnxb741v` (`isbn`),
  KEY `FKqss6n6gtx6lhb7flcka9un18t` (`ma_danh_muc`),
  KEY `FKhp2k7qqhwp3hb66f900uc5gg1` (`mancc`),
  KEY `FKa0fvb13fnkejnd3vtmyicq5x7` (`manxb`),
  KEY `idx_sp_loaisp_trangthai` (`loaisp`,`trang_thai`),
  KEY `idx_sp_danhmuc_trangthai` (`ma_danh_muc`,`trang_thai`),
  KEY `idx_sp_tensp` (`tensp`),
  KEY `idx_sp_ngaytao` (`ngay_tao`),
  KEY `idx_sp_giaban` (`gia_ban`),
  KEY `fk_sp_shop` (`ma_shop`),
  CONSTRAINT `fk_sp_shop` FOREIGN KEY (`ma_shop`) REFERENCES `shop` (`ma_shop`),
  CONSTRAINT `FKa0fvb13fnkejnd3vtmyicq5x7` FOREIGN KEY (`manxb`) REFERENCES `nha_xuat_ban` (`manxb`),
  CONSTRAINT `FKhp2k7qqhwp3hb66f900uc5gg1` FOREIGN KEY (`mancc`) REFERENCES `nha_cung_cap` (`mancc`),
  CONSTRAINT `FKqss6n6gtx6lhb7flcka9un18t` FOREIGN KEY (`ma_danh_muc`) REFERENCES `danh_muc` (`ma_danh_muc`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `san_pham`
--

LOCK TABLES `san_pham` WRITE;
/*!40000 ALTER TABLE `san_pham` DISABLE KEYS */;
INSERT INTO `san_pham` VALUES (1,'978-0525559474',189000,'Sach','https://cdn.penguin.co.in/wp-content/uploads/2023/06/9781786892720-scaled.jpg',5,'2026-09-22 08:25:29.489818',24,'The Midnight Library','DangBan',1,1,1,1,'BinhThuong',35,NULL),(2,'978-0735211292',220000,'Sach','https://cdn.hstatic.net/products/200001055148/thay-doi-ti-hon-hieu-qua-bat-ngo-tb-2026_05f680cdf929490fa3727819b130b408.jpg',5,'2026-09-22 08:25:29.500201',0,'Atomic Habits - Thay Đổi Tí Hon','HetHang',2,1,1,1,'BinhThuong',11,NULL),(3,'978-0857197689',150000,'Sach','https://cdn1.fahasa.com/media/catalog/product/i/m/image_220008_1.jpg',5,'2026-09-22 08:25:29.507098',2,'Tâm Lý Học Về Tiền','DangBan',3,1,1,1,'BinhThuong',24,NULL),(4,'978-0062316097',265000,'Sach','https://lh3.googleusercontent.com/aida-public/AB6AXuCG70bn_Qqg9AKCp8J9XwZoQ0H8otME2a1VxaIQ9mbLAQk60DgJssT5k7D_fsGf6ttO0j-TROb1NzE4QR1_sbRLD856RV8UIFgynUd7SujhVFdhLBJkAPHxPynAmPFKFoKuLSY2tyJX9T_1SDen13Mk4MN2urDBWhtknhDsTK8-O2oLxA8yjhXNZy0YUWK2U5gHXvRhAIj2d-YivtCOD_hFsb8Qu8Z00zBpmbfcRC4at7Dd35UbsRB0',5,'2026-09-22 08:25:29.512098',38,'Sapiens: Lược Sử Loài Người','DangBan',4,1,1,1,'BinhThuong',0,NULL),(5,'978-6047781234',108000,'Sach','https://lh3.googleusercontent.com/aida-public/AB6AXuAJxhxEUoGYZXYaOldMaT7d3dcCRrYvCD-3AWG8q4nOBMKeBIhtPi1dKb9NiDZHTXWMxMULBn3rsm5dU8_IVOjwK8Bnkg8xf-IGsBbsQoLtc5D6EWqcU1KfQ1LzI9jMW8JHZBBy9sHF_7t1onS8pagaubUOfOqYifnPkQMRgKeMgWgWQ71XMqmHdC9fJzuMiB3aqbLZt8bE5qj4HVtt62KzVUKztkjut685dS6dOCMbTJ_WPnz-9N7y',5,'2026-09-22 08:25:29.516615',45,'Cây Cam Ngọt Của Tôi','DangBan',1,1,1,3,'BinhThuong',0,NULL),(6,'978-0441013593',245000,'Sach','https://lh3.googleusercontent.com/aida-public/AB6AXuCG70bn_Qqg9AKCp8J9XwZoQ0H8otME2a1VxaIQ9mbLAQk60DgJssT5k7D_fsGf6ttO0j-TROb1NzE4QR1_sbRLD856RV8UIFgynUd7SujhVFdhLBJkAPHxPynAmPFKFoKuLSY2tyJX9T_1SDen13Mk4MN2urDBWhtknhDsTK8-O2oLxA8yjhXNZy0YUWK2U5gHXvRhAIj2d-YivtCOD_hFsb8Qu8Z00zBpmbfcRC4at7Dd35UbsRB0',5,'2026-09-22 08:25:29.521417',17,'Dune - Xứ Cát','DangBan',5,1,1,1,'BinhThuong',18,NULL),(7,'978-604-1-19841-8',80000,'Sach','https://www.nxbtre.com.vn/Images/Book/nxbtre_full_09422022_034212.jpg',0,'2026-09-23 14:00:30.844593',24,'Cho tôi xin một vé đi tuổi thơ','DangBan',1,1,1,1,'BinhThuong',12,NULL),(8,'VPP-001',85000,'VanPhongPham','https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=80',5,'2026-09-24 12:23:26.717083',40,'Sổ Tay Bìa Da Vintage Classic','DangBan',8,1,NULL,1,'BinhThuong',0,NULL),(9,'VPP-002',210000,'VanPhongPham','https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500&auto=format&fit=crop&q=80',5,'2026-09-24 12:23:27.808937',25,'Bút Máy Thư Pháp Pilot Kakuno','DangBan',9,1,NULL,1,'BinhThuong',0,NULL),(10,'VPP-003',45000,'VanPhongPham','https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500&auto=format&fit=crop&q=80',5,'2026-09-24 12:23:29.036350',60,'Bộ Bookmark Kim Loại Mạ Vàng Cổ Điển','DangBan',10,1,NULL,1,'BinhThuong',0,NULL),(11,'VPP-004',135000,'VanPhongPham','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500&auto=format&fit=crop&q=80',5,'2026-09-24 12:23:30.057715',15,'Đèn Kẹp Đọc Sách Bảo Vệ Mắt LED','DangBan',10,1,NULL,1,'BinhThuong',0,NULL),(12,'QT-001',120000,'QuaTang','https://images.unsplash.com/photo-1544816155-12df9643f363?w=500&auto=format&fit=crop&q=80',5,'2026-09-24 12:23:31.364502',35,'Túi Canvas The4BookStore Vintage Tote','DangBan',11,1,NULL,1,'BinhThuong',0,NULL),(13,'QT-002',450000,'QuaTang','https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500&auto=format&fit=crop&q=80',5,'2026-09-24 12:23:32.400093',10,'Mô Hình Book Nook Gỗ 3D Hẻm Xéo','DangBan',12,1,NULL,1,'BinhThuong',0,NULL),(14,'QT-003',590000,'QuaTang','https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=500&auto=format&fit=crop&q=80',5,'2026-09-24 12:23:33.463374',8,'Bộ Boardgame Catan Bản Tiếng Việt','DangBan',13,1,NULL,1,'BinhThuong',0,NULL),(15,'QT-004',320000,'QuaTang','https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=500&auto=format&fit=crop&q=80',5,'2026-09-24 12:23:34.887879',18,'Hộp Quà Tặng Người Yêu Sách Reader Box','DangBan',13,1,NULL,1,'BinhThuong',0,NULL);
/*!40000 ALTER TABLE `san_pham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `san_pham_da_xem`
--

DROP TABLE IF EXISTS `san_pham_da_xem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `san_pham_da_xem` (
  `makh` int NOT NULL,
  `masp` int NOT NULL,
  `thoi_gian_xem` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`makh`,`masp`),
  KEY `fk_dx_sanpham` (`masp`),
  CONSTRAINT `fk_dx_khachhang` FOREIGN KEY (`makh`) REFERENCES `khach_hang` (`makh`) ON DELETE CASCADE,
  CONSTRAINT `fk_dx_sanpham` FOREIGN KEY (`masp`) REFERENCES `san_pham` (`masp`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `san_pham_da_xem`
--

LOCK TABLES `san_pham_da_xem` WRITE;
/*!40000 ALTER TABLE `san_pham_da_xem` DISABLE KEYS */;
INSERT INTO `san_pham_da_xem` VALUES (6,14,'2026-10-05 08:34:00'),(8,15,'2026-10-05 08:12:19');
/*!40000 ALTER TABLE `san_pham_da_xem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `san_pham_tac_gia`
--

DROP TABLE IF EXISTS `san_pham_tac_gia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `san_pham_tac_gia` (
  `masp` int NOT NULL,
  `ma_tac_gia` int NOT NULL,
  `thu_tu_tac_gia` int DEFAULT NULL,
  PRIMARY KEY (`masp`,`ma_tac_gia`),
  KEY `FKd0vf47jmbbc5xd458vmmt4ly4` (`ma_tac_gia`),
  CONSTRAINT `FKd0vf47jmbbc5xd458vmmt4ly4` FOREIGN KEY (`ma_tac_gia`) REFERENCES `tac_gia` (`ma_tac_gia`),
  CONSTRAINT `FKrn0c9qwvmpesdr9s0akt6lame` FOREIGN KEY (`masp`) REFERENCES `san_pham` (`masp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `san_pham_tac_gia`
--

LOCK TABLES `san_pham_tac_gia` WRITE;
/*!40000 ALTER TABLE `san_pham_tac_gia` DISABLE KEYS */;
INSERT INTO `san_pham_tac_gia` VALUES (1,1,1),(2,2,1),(3,3,1),(4,4,1),(5,5,1),(6,6,1);
/*!40000 ALTER TABLE `san_pham_tac_gia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `san_pham_yeu_thich`
--

DROP TABLE IF EXISTS `san_pham_yeu_thich`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `san_pham_yeu_thich` (
  `makh` int NOT NULL,
  `masp` int NOT NULL,
  `ngay_thich` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`makh`,`masp`),
  KEY `fk_yt_sanpham` (`masp`),
  CONSTRAINT `fk_yt_khachhang` FOREIGN KEY (`makh`) REFERENCES `khach_hang` (`makh`) ON DELETE CASCADE,
  CONSTRAINT `fk_yt_sanpham` FOREIGN KEY (`masp`) REFERENCES `san_pham` (`masp`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `san_pham_yeu_thich`
--

LOCK TABLES `san_pham_yeu_thich` WRITE;
/*!40000 ALTER TABLE `san_pham_yeu_thich` DISABLE KEYS */;
/*!40000 ALTER TABLE `san_pham_yeu_thich` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shop`
--

DROP TABLE IF EXISTS `shop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop` (
  `ma_shop` int NOT NULL AUTO_INCREMENT,
  `ma_tai_khoan` int NOT NULL,
  `ten_shop` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `logo` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `banner` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mo_ta` text COLLATE utf8mb4_unicode_ci,
  `dia_chi_shop` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `so_dien_thoai` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email_shop` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `chiet_khau_phan_tram` decimal(5,2) DEFAULT NULL,
  `trang_thai` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ChoDuyet',
  `ngay_tao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ngay_cap_nhat` datetime DEFAULT NULL,
  PRIMARY KEY (`ma_shop`),
  UNIQUE KEY `ma_tai_khoan` (`ma_tai_khoan`),
  UNIQUE KEY `ten_shop` (`ten_shop`),
  UNIQUE KEY `slug` (`slug`),
  CONSTRAINT `fk_shop_taikhoan` FOREIGN KEY (`ma_tai_khoan`) REFERENCES `tai_khoan` (`ma_tai_khoan`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop`
--

LOCK TABLES `shop` WRITE;
/*!40000 ALTER TABLE `shop` DISABLE KEYS */;
INSERT INTO `shop` VALUES (1,4,'The4BookStore Official','the4bookstore-official','https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=150','https://images.unsplash.com/photo-1507842229451-7f01be45c06b?w=1200','Gian hàng chính thức của hệ thống The4BookStore.','Số 1 Võ Văn Ngân, TP. Thủ Đức, TP. Hồ Chí Minh','0903943105','official@the4bookstore.vn',5.00,'HoatDong','2026-10-05 13:11:19','2026-10-05 08:52:44'),(2,22,'Nhà Sách Trí Tuệ','nha-sach-tri-tue','https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=150','https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=1200','Chuyên cung cấp sách kỹ năng sống và sách kinh tế chọn lọc.','123 Cầu Giấy, Hà Nội','0912345678','trituebook@gmail.com',5.00,'HoatDong','2026-10-05 13:11:31',NULL),(3,16,'FAHASA','fahasa','https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=150','https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=150','Thử nghiệm','462/18 Nguyễn Tri Phương','0967238940','thangphatnekea21333@gmail.com',5.00,'HoatDong','2026-10-05 06:59:49','2026-10-05 11:36:48'),(5,1,'Hieu Sach Hoa Sen','hieu-sach-hoa-sen','https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=150','https://images.unsplash.com/photo-1507842229451-7f01be45c06b?w=1200','Gian hang thu nghiem cho duyet','123 Duong Sach, Q.1','0987654321','hoasen@test.com',5.00,'HoatDong','2026-10-05 07:21:01','2026-10-05 07:21:02');
/*!40000 ALTER TABLE `shop` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tac_gia`
--

DROP TABLE IF EXISTS `tac_gia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tac_gia` (
  `ma_tac_gia` int NOT NULL AUTO_INCREMENT,
  `mo_ta` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ten_tac_gia` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`ma_tac_gia`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tac_gia`
--

LOCK TABLES `tac_gia` WRITE;
/*!40000 ALTER TABLE `tac_gia` DISABLE KEYS */;
INSERT INTO `tac_gia` VALUES (1,'Tiểu thuyết gia và nhà báo nổi tiếng người Anh, tác giả cuốn sách best-seller quốc tế The Midnight Library.','Matt Haig'),(2,'Chuyên gia hàng đầu thế giới về hình thành thói quen và tối ưu hóa năng suất cá nhân, tác giả Atomic Habits.','James Clear'),(3,'Đối tác tại The Collaborative Fund, cựu nhà phân tích tài chính tại The Motley Fool và The Wall Street Journal, tác giả Tâm Lý Học Về Tiền.','Morgan Housel'),(4,'Giáo sư khoa Lịch sử tại Đại học Hebrew Jerusalem, tác giả bộ sách Sapiens Lược sử loài người kinh điển.','Yuval Noah Harari'),(5,'Nhà văn lỗi lạc người Brazil, tác giả kiệt tác văn học kinh điển Cây Cam Ngọt Của Tôi.','José Mauro de Vasconcelos'),(6,'Đại văn hào người Mỹ, tác giả thiên sử thi khoa học viễn tưởng Dune (Xứ Cát).','Frank Herbert'),(7,'Nhà văn nổi tiếng của bao thế hệ độc giả Việt Nam với các tác phẩm như Cho Tôi Xin Một Vé Đi Tuổi Thơ, Mắt Biếc.','Nguyễn Nhật Ánh'),(8,'Tác giả của cuốn sách bán chạy Tuổi Trẻ Đáng Giá Bao Nhiêu, blogger và người truyền cảm hứng sống.','Rosie Nguyễn');
/*!40000 ALTER TABLE `tac_gia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tai_khoan`
--

DROP TABLE IF EXISTS `tai_khoan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tai_khoan` (
  `ma_tai_khoan` int NOT NULL AUTO_INCREMENT,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `mat_khau_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ngay_tao` datetime(6) NOT NULL,
  `ten_dang_nhap` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `trang_thai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `vai_tro` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `auth_provider` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ma_tai_khoan`),
  UNIQUE KEY `UKd0golrlr34gkql6so1i4gbuw5` (`email`),
  UNIQUE KEY `UKgkh4qh51gkiu8ccu1ybn1q7h7` (`ten_dang_nhap`),
  KEY `idx_tk_vaitro_trangthai` (`vai_tro`,`trang_thai`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tai_khoan`
--

LOCK TABLES `tai_khoan` WRITE;
/*!40000 ALTER TABLE `tai_khoan` DISABLE KEYS */;
INSERT INTO `tai_khoan` VALUES (1,'hoasen@the4bookstore.vn','.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','2026-09-21 17:59:56.441901','vendor_hoasen','HoatDong','VENDOR',NULL,NULL),(4,'admin@the4bookstore.vn','.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','2026-09-21 17:59:56.500059','admin','HoatDong','ADMIN',NULL,NULL),(5,'manager@the4bookstore.vn','.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','2026-09-21 17:59:56.503747','manager','HoatDong','MANAGER',NULL,NULL),(16,'fahasa@the4bookstore.vn','.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','2026-09-24 07:49:19.934471','vendor_fahasa','HoatDong','VENDOR','GOOGLE','107848872169038249996'),(17,'user@the4bookstore.vn','.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','2026-09-25 14:59:02.302868','user','HoatDong','USER','GOOGLE','114426590934798871845'),(22,'vendor@the4bookstore.vn','.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','2026-10-05 13:11:31.000000','vendor_demo','HoatDong','VENDOR','LOCAL',NULL);
/*!40000 ALTER TABLE `tai_khoan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thanh_toan`
--

DROP TABLE IF EXISTS `thanh_toan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thanh_toan` (
  `ma_thanh_toan` int NOT NULL AUTO_INCREMENT,
  `ma_giao_dich` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ngay_thanh_toan` datetime(6) DEFAULT NULL,
  `noi_dung` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phuong_thuc` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `so_tien` int NOT NULL,
  `trang_thai` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `madh` int NOT NULL,
  PRIMARY KEY (`ma_thanh_toan`),
  KEY `FKfo8a50ev7l24cccqm3v0hfwbv` (`madh`),
  CONSTRAINT `FKfo8a50ev7l24cccqm3v0hfwbv` FOREIGN KEY (`madh`) REFERENCES `don_hang` (`madh`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thanh_toan`
--

LOCK TABLES `thanh_toan` WRITE;
/*!40000 ALTER TABLE `thanh_toan` DISABLE KEYS */;
INSERT INTO `thanh_toan` VALUES (1,NULL,NULL,'Thanh toán đơn hàng #1','ChuyenKhoan',664000,'ChoThanhToan',1),(2,NULL,NULL,'Thanh toán đơn hàng #2','ChuyenKhoan',80000,'ChoThanhToan',2);
/*!40000 ALTER TABLE `thanh_toan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voucher_da_luu`
--

DROP TABLE IF EXISTS `voucher_da_luu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucher_da_luu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ngay_luu` datetime(6) NOT NULL,
  `ngay_su_dung` datetime(6) DEFAULT NULL,
  `trang_thai` varchar(20) NOT NULL,
  `makh` int NOT NULL,
  `makm` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4kymrlvsuv0h6qohr71r47ssl` (`makh`,`makm`),
  KEY `FK3bdg6al7dgcfa0qfr2eqsqlbv` (`makm`),
  CONSTRAINT `FK3bdg6al7dgcfa0qfr2eqsqlbv` FOREIGN KEY (`makm`) REFERENCES `khuyen_mai` (`makm`),
  CONSTRAINT `FKotvljc8mltd7trbstorbnn3j4` FOREIGN KEY (`makh`) REFERENCES `khach_hang` (`makh`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voucher_da_luu`
--

LOCK TABLES `voucher_da_luu` WRITE;
/*!40000 ALTER TABLE `voucher_da_luu` DISABLE KEYS */;
INSERT INTO `voucher_da_luu` VALUES (2,'2026-10-02 10:49:10.450496',NULL,'ChuaDung',7,7);
/*!40000 ALTER TABLE `voucher_da_luu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'ql_nhasach'
--
/*!50003 DROP FUNCTION IF EXISTS `fn_DemDonHang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_DemDonHang`(startDate DATETIME, endDate DATETIME) RETURNS bigint
    READS SQL DATA
BEGIN
    DECLARE cnt BIGINT DEFAULT 0;
    
    SELECT COUNT(madh) INTO cnt
    FROM don_hang
    WHERE ngay_dat >= startDate AND ngay_dat <= endDate;
    RETURN cnt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_DemSachCuaShop` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_DemSachCuaShop`(p_ma_shop INT) RETURNS int
    READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO v_count
    FROM san_pham
    WHERE ma_shop = p_ma_shop
      AND (trang_thai_khoa IS NULL OR trang_thai_khoa != 'DaKhoa');
      
    RETURN v_count;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_DemSachTheoNXB` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_DemSachTheoNXB`(p_manxb INT) RETURNS bigint
    READS SQL DATA
BEGIN
    DECLARE cnt BIGINT DEFAULT 0;
    
    SELECT COUNT(masp) INTO cnt
    FROM san_pham
    WHERE manxb = p_manxb;
    RETURN cnt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_DemSachTheoTacGia` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_DemSachTheoTacGia`(p_matacgia INT) RETURNS bigint
    READS SQL DATA
BEGIN
    DECLARE cnt BIGINT DEFAULT 0;
    
    SELECT COUNT(masp) INTO cnt
    FROM san_pham_tac_gia
    WHERE ma_tac_gia = p_matacgia;
    RETURN cnt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_DemSanPhamHetHang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_DemSanPhamHetHang`() RETURNS bigint
    READS SQL DATA
BEGIN
    DECLARE cnt BIGINT DEFAULT 0;
    
    SELECT COUNT(masp) INTO cnt
    FROM san_pham
    WHERE so_luong_ton <= 0;
    RETURN cnt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_DemSanPhamSapHet` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_DemSanPhamSapHet`() RETURNS bigint
    READS SQL DATA
BEGIN
    DECLARE cnt BIGINT DEFAULT 0;
    
    SELECT COUNT(masp) INTO cnt
    FROM san_pham
    WHERE so_luong_ton > 0 AND so_luong_ton <= 10;
    RETURN cnt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_KiemTraOTPConHieuLuc` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_KiemTraOTPConHieuLuc`(p_email VARCHAR(255)) RETURNS tinyint
    READS SQL DATA
BEGIN
    DECLARE is_valid TINYINT DEFAULT 0;
    
    SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END INTO is_valid
    FROM tai_khoan
    WHERE email = p_email 
      AND otp_expiry_time > NOW();
    RETURN is_valid;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_TinhDiemDanhGiaSanPham` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_TinhDiemDanhGiaSanPham`(p_masp INT) RETURNS decimal(2,1)
    READS SQL DATA
BEGIN
    DECLARE v_diem DECIMAL(2,1) DEFAULT 5.0;
    
    SELECT COALESCE(ROUND(AVG(so_sao), 1), 5.0) INTO v_diem
    FROM danh_gia
    WHERE masp = p_masp;
    
    RETURN v_diem;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_TinhDoanhThu` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_TinhDoanhThu`(startDate DATETIME, endDate DATETIME) RETURNS bigint
    READS SQL DATA
BEGIN
    DECLARE total BIGINT DEFAULT 0;
    
    SELECT COALESCE(SUM(tong_tien), 0) INTO total
    FROM don_hang
    WHERE trang_thai = 'DaGiao' 
      AND ngay_hoan_thanh >= startDate 
      AND ngay_hoan_thanh <= endDate;
    RETURN total;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_TinhDoanhThuKhuyenMai` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_TinhDoanhThuKhuyenMai`(p_makm VARCHAR(50)) RETURNS bigint
    READS SQL DATA
BEGIN
    DECLARE total BIGINT DEFAULT 0;
    
    SELECT COALESCE(SUM(tong_tien), 0) INTO total
    FROM don_hang
    WHERE makm = p_makm AND trang_thai = 'DaGiao';
    RETURN total;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_TinhPhiSan` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_TinhPhiSan`(p_tong_tien INT, p_ma_shop INT) RETURNS int
    READS SQL DATA
    DETERMINISTIC
BEGIN
    DECLARE v_chiet_khau DECIMAL(5,2) DEFAULT 5.00;
    
    
    SELECT COALESCE(chiet_khau_phan_tram, 5.00) INTO v_chiet_khau
    FROM shop 
    WHERE ma_shop = p_ma_shop;
    
    IF v_chiet_khau IS NULL THEN
        SET v_chiet_khau = 5.00;
    END IF;
    
    RETURN ROUND(p_tong_tien * (v_chiet_khau / 100.0));
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_TinhTienGiamKhuyenMai` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_TinhTienGiamKhuyenMai`(p_makm VARCHAR(50)) RETURNS bigint
    READS SQL DATA
BEGIN
    DECLARE total_discount BIGINT DEFAULT 0;
    SELECT COALESCE(SUM(tien_giam), 0) INTO total_discount
    FROM don_hang
    WHERE makm = p_makm AND trang_thai = 'DaGiao';
    RETURN total_discount;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_TongSachDaBan` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_TongSachDaBan`() RETURNS bigint
    READS SQL DATA
BEGIN
    DECLARE total BIGINT DEFAULT 0;
    
    SELECT COALESCE(SUM(ct.so_luong), 0) INTO total
    FROM chi_tiet_don_hang ct
    JOIN don_hang dh ON ct.madh = dh.madh
    WHERE dh.trang_thai = 'DaGiao';
    RETURN total;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `CreateIdxIfNotExists` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CreateIdxIfNotExists`(
    IN p_idx_name VARCHAR(255),
    IN p_tbl_name VARCHAR(255),
    IN p_sql VARCHAR(1000)
)
BEGIN
    DECLARE idx_exists INT;
    SELECT COUNT(1) INTO idx_exists
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = DATABASE()
      AND table_name = p_tbl_name
      AND index_name = p_idx_name;

    IF idx_exists = 0 THEN
        SET @stmt = p_sql;
        PREPARE s FROM @stmt;
        EXECUTE s;
        DEALLOCATE PREPARE s;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_CapNhatGioHang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_CapNhatGioHang`(IN p_makh INT, IN p_masp INT, IN p_soluongmoi INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    IF p_soluongmoi <= 0 THEN
        
        DELETE FROM gio_hang 
        WHERE khach_hang_id = p_makh AND san_pham_id = p_masp;
    ELSE
        
        UPDATE gio_hang 
        SET so_luong = p_soluongmoi 
        WHERE khach_hang_id = p_makh AND san_pham_id = p_masp;
    END IF;
    
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_CapNhatTrangThaiDonHang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_CapNhatTrangThaiDonHang`(
    IN p_madh INT,
    IN p_trangthaimoi VARCHAR(50),
    IN p_lydo VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF p_trangthaimoi = 'TU_CHOI' THEN
        UPDATE don_hang 
        SET trang_thai = p_trangthaimoi, ly_do_tu_choi = p_lydo 
        WHERE madh = p_madh;
    ELSEIF p_trangthaimoi = 'DA_HUY' THEN
        UPDATE don_hang 
        SET trang_thai = p_trangthaimoi, ly_do_huy = p_lydo 
        WHERE madh = p_madh;
    ELSE
        UPDATE don_hang 
        SET trang_thai = p_trangthaimoi 
        WHERE madh = p_madh;
    END IF;

    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_DoanhThuShopTheoThang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_DoanhThuShopTheoThang`(IN p_ma_shop INT)
BEGIN
    SELECT 
        YEAR(dh.ngay_dat) AS nam,
        MONTH(dh.ngay_dat) AS thang,
        COUNT(dh.madh) AS tong_don_hang,
        COALESCE(SUM(dh.tong_tien), 0) AS tong_doanh_thu,
        COALESCE(SUM(dh.tien_phi_san), 0) AS tong_phi_san,
        COALESCE(SUM(dh.tien_thuc_nhan_shop), 0) AS thuc_nhan_shop
    FROM don_hang dh
    WHERE dh.ma_shop = p_ma_shop
      AND dh.trang_thai IN ('DaGiao', 'DA_GIAO')
    GROUP BY YEAR(dh.ngay_dat), MONTH(dh.ngay_dat)
    ORDER BY nam DESC, thang DESC;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_DoanhThuTheoThang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_DoanhThuTheoThang`()
BEGIN
    
    SELECT YEAR(dh.ngay_hoan_thanh) as nam, 
           MONTH(dh.ngay_hoan_thanh) as thang, 
           SUM(dh.tong_tien) as doanh_thu 
    FROM don_hang dh 
    WHERE dh.trang_thai = 'DaGiao' 
      AND dh.ngay_hoan_thanh IS NOT NULL 
    GROUP BY YEAR(dh.ngay_hoan_thanh), MONTH(dh.ngay_hoan_thanh) 
    ORDER BY nam DESC, thang DESC 
    LIMIT 12;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_HuyDonHang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_HuyDonHang`(
    IN p_madh INT,
    IN p_makh INT,
    IN p_lydohuy VARCHAR(255)
)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_masp INT;
    DECLARE v_soluong INT;
    DECLARE v_trangthai VARCHAR(50);
    
    DECLARE cur_ctdh CURSOR FOR 
        SELECT masp, so_luong FROM chi_tiet_don_hang WHERE madh = p_madh;
        
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    
    SELECT trang_thai INTO v_trangthai 
    FROM don_hang 
    WHERE madh = p_madh AND makh = p_makh 
    FOR UPDATE;
    
    IF v_trangthai != 'DA_HUY' AND v_trangthai != 'DA_GIAO' THEN
        
        OPEN cur_ctdh;
        read_loop: LOOP
            FETCH cur_ctdh INTO v_masp, v_soluong;
            IF done THEN
                LEAVE read_loop;
            END IF;
            
            UPDATE san_pham 
            SET so_luong_ton = so_luong_ton + v_soluong 
            WHERE masp = v_masp;
        END LOOP;
        CLOSE cur_ctdh;
        
        
        UPDATE don_hang 
        SET trang_thai = 'DA_HUY', ly_do_huy = p_lydohuy 
        WHERE madh = p_madh AND makh = p_makh;
    END IF;

    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_KhoaShopVaSanPham` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_KhoaShopVaSanPham`(
    IN p_ma_shop INT,
    IN p_ly_do TEXT,
    OUT p_so_luong_khoa INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    
    UPDATE shop 
    SET trang_thai = 'BiKhoa', 
        ngay_cap_nhat = NOW() 
    WHERE ma_shop = p_ma_shop;
    
    
    UPDATE san_pham 
    SET trang_thai_khoa = 'DaKhoa'
    WHERE ma_shop = p_ma_shop;
    
    SELECT ROW_COUNT() INTO p_so_luong_khoa;
    
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_LayDanhMucTheoLoaiSP` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_LayDanhMucTheoLoaiSP`(IN p_loaisp INT)
BEGIN
    
    SELECT madanhmuc, tendanhmuc, loaisp
    FROM danh_muc
    WHERE loaisp = p_loaisp;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_ResetPassword` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_ResetPassword`(IN p_email VARCHAR(100), IN p_otp VARCHAR(10), IN p_newpasshash VARCHAR(255))
BEGIN
    DECLARE v_valid INT DEFAULT 0;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    
    SELECT COUNT(*) INTO v_valid 
    FROM password_reset_token 
    WHERE email = p_email 
      AND otp = p_otp 
      AND da_xoa = 0 
      AND da_su_dung = 0 
      AND thoi_gian_het_han > NOW();
    
    IF v_valid > 0 THEN
        
        UPDATE tai_khoan 
        SET mat_khau = p_newpasshash 
        WHERE email = p_email;
        
        
        UPDATE password_reset_token 
        SET da_su_dung = 1 
        WHERE email = p_email AND otp = p_otp;
    ELSE
        
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'OTP khÃ´ng há»£p lá»‡, Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng hoáº·c Ä‘Ã£ háº¿t háº¡n';
    END IF;
    
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_TaoTokenResetPassword` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_TaoTokenResetPassword`(IN p_email VARCHAR(100), OUT p_otp VARCHAR(10))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    
    SET p_otp = LPAD(FLOOR(RAND() * 999999.99), 6, '0');
    
    
    UPDATE password_reset_token 
    SET da_xoa = 1 
    WHERE email = p_email AND da_xoa = 0;
    
    
    INSERT INTO password_reset_token (email, otp, thoi_gian_het_han, da_xoa, da_su_dung) 
    VALUES (p_email, p_otp, DATE_ADD(NOW(), INTERVAL 15 MINUTE), 0, 0);
    
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_ThemVaoGioHang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_ThemVaoGioHang`(
    IN p_makh INT,
    IN p_masp INT,
    IN p_soluong INT
)
BEGIN
    DECLARE v_count INT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    
    SELECT COUNT(*) INTO v_count 
    FROM gio_hang 
    WHERE makh = p_makh AND masp = p_masp;

    IF v_count > 0 THEN
        
        UPDATE gio_hang 
        SET so_luong = so_luong + p_soluong 
        WHERE makh = p_makh AND masp = p_masp;
    ELSE
        
        INSERT INTO gio_hang (makh, masp, so_luong) 
        VALUES (p_makh, p_masp, p_soluong);
    END IF;

    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_TimKiemKhuyenMai` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_TimKiemKhuyenMai`(IN p_keyword VARCHAR(255), IN p_loaigiam VARCHAR(50))
BEGIN
    
    SELECT makm, tenkm, loai_giam, gia_tri_giam, ngay_bat_dau, ngay_ket_thuc, trang_thai
    FROM khuyen_mai
    WHERE (p_keyword IS NULL OR tenkm LIKE CONCAT('%', p_keyword, '%') OR makm LIKE CONCAT('%', p_keyword, '%'))
      AND (p_loaigiam IS NULL OR loai_giam = p_loaigiam);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_TopSachBanChay` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_TopSachBanChay`(IN p_limit INT)
BEGIN
    
    SELECT s.masp, s.tensp, SUM(ct.so_luong) as tong_da_ban, s.gia_ban, SUM(ct.so_luong * ct.don_gia) as doanh_thu_dong_gop
      FROM san_pham s
      JOIN chi_tiet_don_hang ct ON s.masp = ct.masp
    JOIN don_hang dh ON ct.madh = dh.madh
    WHERE dh.trang_thai = 'DaGiao'
    GROUP BY s.masp, s.tensp, s.gia_ban
    ORDER BY tong_da_ban DESC
    LIMIT p_limit;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_VendorXacNhanDonHang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_VendorXacNhanDonHang`(
    IN p_madh INT,
    IN p_ma_shop INT,
    OUT p_status VARCHAR(20),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_trang_thai VARCHAR(50);
    DECLARE done INT DEFAULT 0;
    DECLARE v_masp INT;
    DECLARE v_so_luong INT;
    DECLARE v_ton_kho INT;
    
    
    DECLARE cur_ctdh CURSOR FOR 
        SELECT masp, so_luong FROM chi_tiet_don_hang WHERE madh = p_madh;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status = 'ERROR';
        SET p_message = 'Lỗi hệ thống CSDL trong quá trình xử lý giao dịch';
    END;

    START TRANSACTION;
    
    
    SELECT COUNT(*), trang_thai INTO v_count, v_trang_thai
    FROM don_hang 
    WHERE madh = p_madh AND (ma_shop = p_ma_shop OR p_ma_shop IS NULL)
    GROUP BY trang_thai
    LIMIT 1;

    IF v_count = 0 THEN
        SET p_status = 'NOT_FOUND';
        SET p_message = 'Đơn hàng không tồn tại hoặc không thuộc quyền quản lý của gian hàng';
        ROLLBACK;
    ELSEIF v_trang_thai != 'ChoXacNhan' AND v_trang_thai != 'CHO_XAC_NHAN' AND v_trang_thai != 'DonHangMoi' THEN
        SET p_status = 'INVALID_STATE';
        SET p_message = CONCAT('Đơn hàng đang ở trạng thái [', v_trang_thai, '], không thể xác nhận lại');
        ROLLBACK;
    ELSE
        
        SET done = 0;
        OPEN cur_ctdh;
        check_loop: LOOP
            FETCH cur_ctdh INTO v_masp, v_so_luong;
            IF done = 1 THEN
                LEAVE check_loop;
            END IF;
            
            SELECT so_luong_ton INTO v_ton_kho FROM san_pham WHERE masp = v_masp FOR UPDATE;
            IF v_ton_kho < v_so_luong THEN
                SET p_status = 'OUT_OF_STOCK';
                SET p_message = CONCAT('Sản phẩm mã #', v_masp, ' không đủ tồn kho (hiện còn ', v_ton_kho, ')');
                LEAVE check_loop;
            END IF;
        END LOOP;
        CLOSE cur_ctdh;
        
        
        IF p_status IS NULL OR p_status != 'OUT_OF_STOCK' THEN
            SET done = 0;
            OPEN cur_ctdh;
            update_loop: LOOP
                FETCH cur_ctdh INTO v_masp, v_so_luong;
                IF done = 1 THEN
                    LEAVE update_loop;
                END IF;
                
                UPDATE san_pham 
                SET so_luong_ton = so_luong_ton - v_so_luong,
                    so_luong_da_ban = so_luong_da_ban + v_so_luong
                WHERE masp = v_masp;
            END LOOP;
            CLOSE cur_ctdh;
            
            
            UPDATE don_hang 
            SET trang_thai = 'DaXacNhan' 
            WHERE madh = p_madh;
            
            SET p_status = 'SUCCESS';
            SET p_message = 'Đã xác nhận đơn hàng thành công và cập nhật tồn kho';
            COMMIT;
        ELSE
            ROLLBACK;
        END IF;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_XacNhanPhieuKiemKe` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_XacNhanPhieuKiemKe`(IN p_mapk INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_masp INT;
    DECLARE v_soluong_thucte INT;
    
    DECLARE cur CURSOR FOR 
        SELECT san_pham_id, so_luong_thuc_te 
        FROM chi_tiet_kiem_ke 
        WHERE phieu_kiem_ke_id = p_mapk;
        
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_masp, v_soluong_thucte;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        
        
        SET @chenh_lech = (SELECT so_luong_ton FROM kho_hang WHERE san_pham_id = v_masp LIMIT 1);
        IF @chenh_lech IS NOT NULL THEN
            SET @chenh_lech = v_soluong_thucte - @chenh_lech;
            UPDATE san_pham SET so_luong_ton = so_luong_ton + @chenh_lech WHERE masp = v_masp;
        END IF;

        UPDATE kho_hang 
        SET so_luong_ton = v_soluong_thucte 
        WHERE san_pham_id = v_masp;
    END LOOP;
    CLOSE cur;
    
    
    UPDATE phieu_kiem_ke 
    SET trang_thai = 'DaDuyet' 
    WHERE id = p_mapk;
    
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_XacNhanPhieuNhap` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_XacNhanPhieuNhap`(IN p_mapn INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_masp INT;
    DECLARE v_soluong INT;
    
    DECLARE cur CURSOR FOR 
        SELECT san_pham_id, so_luong 
        FROM chi_tiet_phieu_nhap 
        WHERE phieu_nhap_id = p_mapn;
        
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_masp, v_soluong;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        
        UPDATE kho_hang 
        SET so_luong_ton = so_luong_ton + v_soluong 
        WHERE san_pham_id = v_masp;
        
        
        UPDATE san_pham
        SET so_luong_ton = so_luong_ton + v_soluong
        WHERE masp = v_masp;
    END LOOP;
    CLOSE cur;
    
    
    UPDATE phieu_nhap 
    SET trang_thai = 'DaNhap' 
    WHERE id = p_mapn;
    
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_XoaKhoiGioHang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_XoaKhoiGioHang`(
    IN p_makh INT,
    IN p_masp INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    DELETE FROM gio_hang 
    WHERE makh = p_makh AND masp = p_masp;

    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_XoaPhieuNhap` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_XoaPhieuNhap`(
    IN p_mapn INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    
    DELETE FROM chi_tiet_phieu_nhap WHERE mapn = p_mapn;
    
    
    DELETE FROM phieu_nhap WHERE mapn = p_mapn;

    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_XoaToanBoGioHang` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_XoaToanBoGioHang`(IN p_makh INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    
    DELETE FROM gio_hang 
    WHERE khach_hang_id = p_makh;
    
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-05 19:51:14
