package vn.bookstore.the4bookstore.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvatarUtilsTest {

    @Test
    void testExtractInitial_VietnameseNames() {
        assertEquals("D", AvatarUtils.extractInitial("Nguyễn Duy"));
        assertEquals("T", AvatarUtils.extractInitial("Cao Tường"));
        assertEquals("T", AvatarUtils.extractInitial("Nguyễn Minh Thư"));
        assertEquals("L", AvatarUtils.extractInitial("Hoàng Long"));
    }

    @Test
    void testExtractInitial_SingleWordNames() {
        assertEquals("A", AvatarUtils.extractInitial("admin"));
        assertEquals("Q", AvatarUtils.extractInitial("quanly1"));
        assertEquals("I", AvatarUtils.extractInitial("Izhary"));
        assertEquals("U", AvatarUtils.extractInitial("User"));
    }

    @Test
    void testExtractInitial_SpecialAndEmpty() {
        assertEquals("U", AvatarUtils.extractInitial(null));
        assertEquals("U", AvatarUtils.extractInitial("   "));
        assertEquals("U", AvatarUtils.extractInitial("---"));
    }

    @Test
    void testGenerateInitialAvatarSvg() {
        String svgUri = AvatarUtils.generateInitialAvatarSvg("Cao Tường");
        assertNotNull(svgUri);
        assertTrue(svgUri.startsWith("data:image/svg+xml;utf8,"));
        assertTrue(svgUri.contains("%3E"));
    }

    @Test
    void testGenerateDefaultAvatarSvg() {
        String svgUri = AvatarUtils.generateDefaultAvatarSvg();
        assertNotNull(svgUri);
        assertTrue(svgUri.startsWith("data:image/svg+xml;base64,"));
    }

    @Test
    void testHasCustomAvatar() {
        assertFalse(AvatarUtils.hasCustomAvatar(null));
        assertFalse(AvatarUtils.hasCustomAvatar(""));
        assertFalse(AvatarUtils.hasCustomAvatar("/images/manager-avatar.png"));
        assertFalse(AvatarUtils.hasCustomAvatar("data:image/svg+xml;utf8,..."));
        assertFalse(AvatarUtils.hasCustomAvatar("data:image/svg+xml;base64,..."));
        assertTrue(AvatarUtils.hasCustomAvatar("https://lh3.googleusercontent.com/photo.jpg"));
        assertTrue(AvatarUtils.hasCustomAvatar("/uploads/avatars/avatar_1.png"));
    }
}
