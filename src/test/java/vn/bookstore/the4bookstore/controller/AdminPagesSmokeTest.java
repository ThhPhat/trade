package vn.bookstore.the4bookstore.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPagesSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString("Danh Sách Sản Phẩm Mới Trên Sàn")
                ))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString("Top Sách Bán Chạy Nhất")
                ))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString("Đơn Hàng Gần Đây Trên Sàn")
                ))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString("Tháng 10")
                ));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminOrders() throws Exception {
        mockMvc.perform(get("/admin/orders"))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminVouchers() throws Exception {
        mockMvc.perform(get("/admin/vouchers"))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminUsers() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andDo(print());
    }

    @Test
    void testLoginWithManager() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/process-login")
                .param("username", "manager")
                .param("password", "123456")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/admin/dashboard"));
    }

    @Test
    void testLoginWithEmail() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/process-login")
                .param("username", "admin@the4bookstore.vn")
                .param("password", "123456")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/admin/dashboard"));
    }

    @Autowired
    private vn.bookstore.the4bookstore.repository.DonHangRepository donHangRepository;

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testManagerImportExport() throws Exception {
        mockMvc.perform(get("/admin/import-export"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        mockMvc.perform(get("/admin/import-export/export/orders"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testUpdateOrderStatusConstraint() {
        var optOrder = donHangRepository.findAll().stream().findFirst();
        if (optOrder.isPresent()) {
            var order = optOrder.get();
            String originalStatus = order.getTrangThai();
            try {
                order.setTrangThai("DaGiao");
                donHangRepository.saveAndFlush(order);

                order.setTrangThai("DaLayHang");
                donHangRepository.saveAndFlush(order);

                order.setTrangThai("YeuCauHuy");
                donHangRepository.saveAndFlush(order);

                order.setTrangThai("TraHangHoanTien");
                donHangRepository.saveAndFlush(order);
            } finally {
                order.setTrangThai(originalStatus);
                donHangRepository.saveAndFlush(order);
            }
        }
    }
}
