package com.example.game.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Stream
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ArrowSkin(
    val id: String,
    val name: String,
    val subtitle: String,
    val price: Int,
    val gradientColors: List<Color>,
    val headGlowColor: Color,
    val accentColor: Color,
    val unblockedColors: List<Color>,
    val iconVector: ImageVector
)

object ArrowSkinCatalog {
    val CLASSIC_CYAN = ArrowSkin(
        id = "classic_cyan",
        name = "Classic Cyan",
        subtitle = "Standard issue precision laser vector",
        price = 0,
        gradientColors = listOf(Color(0xFF1E40AF), Color(0xFF0284C7), Color(0xFF06B6D4)),
        headGlowColor = Color(0xFF38BDF8),
        accentColor = Color(0xFF00D2FF),
        unblockedColors = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF6EE7B7)),
        iconVector = Icons.AutoMirrored.Filled.ArrowForward
    )

    val NEON_EMERALD = ArrowSkin(
        id = "neon_emerald",
        name = "Matrix Emerald",
        subtitle = "High-voltage bio-luminescent pulse",
        price = 750,
        gradientColors = listOf(Color(0xFF065F46), Color(0xFF059669), Color(0xFF10B981)),
        headGlowColor = Color(0xFF34D399),
        accentColor = Color(0xFF6EE7B7),
        unblockedColors = listOf(Color(0xFF0284C7), Color(0xFF06B6D4), Color(0xFF67E8F9)),
        iconVector = Icons.Default.NearMe
    )

    val SOLAR_FLARE = ArrowSkin(
        id = "solar_flare",
        name = "Solar Flare",
        subtitle = "Forged in the heart of thermonuclear stars",
        price = 1500,
        gradientColors = listOf(Color(0xFFC2410C), Color(0xFFEA580C), Color(0xFFF59E0B)),
        headGlowColor = Color(0xFFFDE047),
        accentColor = Color(0xFFF59E0B),
        unblockedColors = listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFFA7F3D0)),
        iconVector = Icons.Default.Flare
    )

    val CRIMSON_PHANTOM = ArrowSkin(
        id = "crimson_phantom",
        name = "Crimson Phantom",
        subtitle = "Stealth aerodynamic velocity projectile",
        price = 2500,
        gradientColors = listOf(Color(0xFF881337), Color(0xFFBE123C), Color(0xFFE11D48)),
        headGlowColor = Color(0xFFFB7185),
        accentColor = Color(0xFFF43F5E),
        unblockedColors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0xFFFEF08A)),
        iconVector = Icons.Default.Flight
    )

    val CYBER_SYNTH = ArrowSkin(
        id = "cyber_synth",
        name = "Cyber Synthwave",
        subtitle = "Retro-futuristic magenta laser beam",
        price = 3200,
        gradientColors = listOf(Color(0xFF831843), Color(0xFFDB2777), Color(0xFF06B6D4)),
        headGlowColor = Color(0xFFF472B6),
        accentColor = Color(0xFFEC4899),
        unblockedColors = listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFFA7F3D0)),
        iconVector = Icons.Default.Stream
    )

    val QUANTUM_VIOLET = ArrowSkin(
        id = "quantum_violet",
        name = "Quantum Violet",
        subtitle = "Dimensional rift particle stream",
        price = 4200,
        gradientColors = listOf(Color(0xFF581C87), Color(0xFF7E22CE), Color(0xFFA855F7)),
        headGlowColor = Color(0xFFC084FC),
        accentColor = Color(0xFF9333EA),
        unblockedColors = listOf(Color(0xFF06B6D4), Color(0xFF22D3EE), Color(0xFFA5F3FC)),
        iconVector = Icons.Default.Bolt
    )

    val OBSIDIAN_VOID = ArrowSkin(
        id = "obsidian_void",
        name = "Obsidian Void",
        subtitle = "Deep cosmic stealth with silver iridescence",
        price = 5200,
        gradientColors = listOf(Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B)),
        headGlowColor = Color(0xFF94A3B8),
        accentColor = Color(0xFFCBD5E1),
        unblockedColors = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399)),
        iconVector = Icons.Default.DarkMode
    )

    val APEX_GOLD = ArrowSkin(
        id = "apex_gold",
        name = "Apex Golden",
        subtitle = "Master tier gilded luxury arrows",
        price = 6500,
        gradientColors = listOf(Color(0xFF78350F), Color(0xFFB45309), Color(0xFFF59E0B)),
        headGlowColor = Color(0xFFFEF08A),
        accentColor = Color(0xFFFFD700),
        unblockedColors = listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFFA7F3D0)),
        iconVector = Icons.Default.Diamond
    )

    val FROST_CRYSTAL = ArrowSkin(
        id = "frost_crystal",
        name = "Frost Cryo",
        subtitle = "Sub-zero absolute glacier vector",
        price = 7800,
        gradientColors = listOf(Color(0xFF0C4A6E), Color(0xFF0284C7), Color(0xFFBAE6FD)),
        headGlowColor = Color(0xFFE0F2FE),
        accentColor = Color(0xFF7DD3FC),
        unblockedColors = listOf(Color(0xFF059669), Color(0xFF34D399), Color(0xFF6EE7B7)),
        iconVector = Icons.Default.AcUnit
    )

    val COSMIC_NEBULA = ArrowSkin(
        id = "cosmic_nebula",
        name = "Cosmic Nebula",
        subtitle = "Interstellar astral cluster with radiant starlight",
        price = 9200,
        gradientColors = listOf(Color(0xFF3B0764), Color(0xFF701A75), Color(0xFFC026D3)),
        headGlowColor = Color(0xFFF0ABFC),
        accentColor = Color(0xFFE879F9),
        unblockedColors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD)),
        iconVector = Icons.Default.Stars
    )

    val DRAGON_MAGMA = ArrowSkin(
        id = "dragon_magma",
        name = "Dragon Magma",
        subtitle = "Molten volcanic flame kinetic vector",
        price = 11000,
        gradientColors = listOf(Color(0xFF7F1D1D), Color(0xFFDC2626), Color(0xFFF97316)),
        headGlowColor = Color(0xFFFDBA74),
        accentColor = Color(0xFFFB923C),
        unblockedColors = listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFFA7F3D0)),
        iconVector = Icons.Default.LocalFireDepartment
    )

    val CELESTIAL_PRISM = ArrowSkin(
        id = "celestial_prism",
        name = "Celestial Prism",
        subtitle = "Supreme rainbow spectrum of the mind",
        price = 15000,
        gradientColors = listOf(Color(0xFF4338CA), Color(0xFF0D9488), Color(0xFFF59E0B)),
        headGlowColor = Color(0xFFFFFFFF),
        accentColor = Color(0xFFFDE047),
        unblockedColors = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF6EE7B7)),
        iconVector = Icons.Default.AutoAwesome
    )

    val allSkins = listOf(
        CLASSIC_CYAN,
        NEON_EMERALD,
        SOLAR_FLARE,
        CRIMSON_PHANTOM,
        CYBER_SYNTH,
        QUANTUM_VIOLET,
        OBSIDIAN_VOID,
        APEX_GOLD,
        FROST_CRYSTAL,
        COSMIC_NEBULA,
        DRAGON_MAGMA,
        CELESTIAL_PRISM
    )

    fun getSkin(id: String): ArrowSkin {
        return allSkins.find { it.id == id } ?: CLASSIC_CYAN
    }
}
