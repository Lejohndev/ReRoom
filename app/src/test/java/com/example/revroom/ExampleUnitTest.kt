package com.example.revroom

import com.example.revroom.core.utils.StylePreviewAssetBuilder
import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testStylePreviewAssetBuilder_validInputs() {
        val path1 = StylePreviewAssetBuilder.buildAssetPath("master_bedroom", "Indochine")
        assertEquals("file:///android_asset/style_previews/master_bedroom/indochine.webp", path1)

        val path2 = StylePreviewAssetBuilder.buildAssetPath("living_room", "Japandi")
        assertEquals("file:///android_asset/style_previews/living_room/japandi.webp", path2)

        val path3 = StylePreviewAssetBuilder.buildAssetPath("kitchen", "Industrial")
        assertEquals("file:///android_asset/style_previews/kitchen/industrial.webp", path3)
    }

    @Test
    fun testStylePreviewAssetBuilder_invalidInputs() {
        // Invalid style
        val path1 = StylePreviewAssetBuilder.buildAssetPath("master_bedroom", "UnknownStyle")
        assertNull(path1)

        // Invalid room type
        val path2 = StylePreviewAssetBuilder.buildAssetPath("unsupported_room", "Japandi")
        assertNull(path2)

        // Null room type
        val path3 = StylePreviewAssetBuilder.buildAssetPath(null, "Japandi")
        assertNull(path3)
    }
}