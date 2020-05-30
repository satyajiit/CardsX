package com.argonlabs.cardsx.utils

import java.util.*

object MaterialColorPalette {
    private const val RED_500 = -0xbbcca
    private const val PINK_500 = -0x16e19d
    private const val PURPLE_500 = -0x63d850
    private const val DEEP_PURPLE_500 = -0x98c549
    private const val INDIGO_500 = -0xc0ae4b
    private const val BLUE_500 = -0xde690d
    private const val LIGHT_BLUE_500 = -0xfc560c
    private const val CYAN_500 = -0xff432c
    private const val TEAL_500 = -0xff6978
    private const val GREEN_500 = -0xb350b0
    private const val LIGHT_GREEN_500 = -0x743cb6
    private const val AMBER_500 = -0x3ef9
    private const val ORANGE_500 = -0x6800
    private const val DEEP_ORANGE_500 = -0xa8de
    private const val BROWN_500 = -0x86aab8
    private const val GREY_500 = -0x616162
    private const val BLUE_GREY_500 = -0x9f8275
    private var MATERIAL_PALETTES: MutableList<Int> = ArrayList()
    private val RANDOM = Random()
    @JvmStatic
    val randomColor: Int
        get() = MATERIAL_PALETTES[RANDOM.nextInt(MATERIAL_PALETTES.size)]

    init {
        MATERIAL_PALETTES.add(RED_500)
        MATERIAL_PALETTES.add(PINK_500)
        MATERIAL_PALETTES.add(PURPLE_500)
        MATERIAL_PALETTES.add(DEEP_PURPLE_500)
        MATERIAL_PALETTES.add(INDIGO_500)
        MATERIAL_PALETTES.add(BLUE_500)
        MATERIAL_PALETTES.add(LIGHT_BLUE_500)
        MATERIAL_PALETTES.add(CYAN_500)
        MATERIAL_PALETTES.add(TEAL_500)
        MATERIAL_PALETTES.add(GREEN_500)
        MATERIAL_PALETTES.add(LIGHT_GREEN_500)
        MATERIAL_PALETTES.add(AMBER_500)
        MATERIAL_PALETTES.add(ORANGE_500)
        MATERIAL_PALETTES.add(DEEP_ORANGE_500)
        MATERIAL_PALETTES.add(BROWN_500)
        MATERIAL_PALETTES.add(GREY_500)
        MATERIAL_PALETTES.add(BLUE_GREY_500)
    }
}