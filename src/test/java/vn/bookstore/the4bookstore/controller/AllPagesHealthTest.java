package vn.bookstore.the4bookstore.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AllPagesHealthTest {

    @Autowired
    private MockMvc mockMvc;

    // 1. PUBLIC ROUTES
    @Test
    void testPublicPages() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/san-pham")).andExpect(status().isOk());
        mockMvc.perform(get("/san-pham/14")).andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print()).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    // 2. CUSTOMER ROUTES
    @Test
    @WithMockUser(username = "khachhang_an", roles = {"KHACHHANG", "USER"})
    void testCustomerPages() throws Exception {
        mockMvc.perform(get("/gio-hang")).andExpect(status().isOk());
        mockMvc.perform(get("/san-voucher")).andExpect(status().isOk());
        mockMvc.perform(get("/profile")).andExpect(status().isOk());
        mockMvc.perform(get("/profile/addresses")).andExpect(status().isOk());
        mockMvc.perform(get("/profile/favorites")).andExpect(status().isOk());
        mockMvc.perform(get("/don-hang")).andExpect(status().isOk());
    }

    // 3. VENDOR ROUTES
    @Test
    @WithMockUser(username = "vendor_kimdong", roles = {"VENDOR"})
    void testVendorPages() throws Exception {
        mockMvc.perform(get("/vendor/dashboard")).andExpect(status().isOk());
        mockMvc.perform(get("/vendor/products")).andExpect(status().isOk());
        mockMvc.perform(get("/vendor/orders")).andExpect(status().isOk());
        mockMvc.perform(get("/vendor/promotions")).andExpect(status().isOk());
        mockMvc.perform(get("/vendor/stock-requests")).andExpect(status().isOk());
        mockMvc.perform(get("/vendor/inventory")).andExpect(status().isOk());
    }

    // 4. MANAGER & ADMIN ROUTES
    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testManagerPages() throws Exception {
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/shops")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/orders")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/users")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/vouchers")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/categories")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/authors")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/disputes")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/shipping")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/platform-fees")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/import-export")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/kho")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminPages() throws Exception {
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/shops")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/orders")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/users")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/vouchers")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/categories")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/authors")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/disputes")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/shipping")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/platform-fees")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/import-export")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/kho")).andExpect(status().is3xxRedirection());
    }
}
