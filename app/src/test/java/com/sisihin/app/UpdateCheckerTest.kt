package com.sisihin.app

import com.sisihin.app.data.update.UpdateChecker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun patchLebihBaru() {
        assertTrue(UpdateChecker.isNewerVersion("1.0.1", "1.0.0"))
    }

    @Test
    fun sepuluhLebihBaruDariDua_bukanPerbandinganString() {
        assertTrue(UpdateChecker.isNewerVersion("1.0.10", "1.0.2"))
    }

    @Test
    fun minorLebihBaru() {
        assertTrue(UpdateChecker.isNewerVersion("1.1.0", "1.0.9"))
    }

    @Test
    fun awalanVDiabaikan() {
        assertTrue(UpdateChecker.isNewerVersion("v2.0.0", "1.9.9"))
        assertTrue(UpdateChecker.isNewerVersion("V2.0.0", "v1.9.9"))
    }

    @Test
    fun versiSamaBukanLebihBaru() {
        assertFalse(UpdateChecker.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(UpdateChecker.isNewerVersion("v1.0.0", "1.0.0"))
    }

    @Test
    fun versiLamaBukanLebihBaru() {
        assertFalse(UpdateChecker.isNewerVersion("1.0.0", "1.0.1"))
        assertFalse(UpdateChecker.isNewerVersion("1.9.9", "2.0.0"))
    }

    @Test
    fun segmenKurangDianggapNol() {
        // versionName aplikasi saat ini "1.1" (dua segmen).
        assertFalse(UpdateChecker.isNewerVersion("1.1.0", "1.1"))
        assertFalse(UpdateChecker.isNewerVersion("1.1", "1.1.0"))
        assertTrue(UpdateChecker.isNewerVersion("1.1.1", "1.1"))
        assertTrue(UpdateChecker.isNewerVersion("1.2", "1.1.9"))
    }
}
