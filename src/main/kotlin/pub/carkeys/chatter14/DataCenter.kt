/*
 * Copyright 2022 James Keesey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pub.carkeys.chatter14

/**
 * Defines all the known data centers and their servers.
 */
class DataCenter(val name: String, val region: Region, val servers: Set<String>) {
    enum class Region {
        EUROPE,
        JAPAN,
        NORTH_AMERICA,
    }

    @Suppress("SpellCheckingInspection")
    companion object {
        private val aether = DataCenter(
            name = "Aether", region = Region.NORTH_AMERICA, servers = setOf(
                "Adamantoise",
                "Cactuar",
                "Faerie",
                "Gilgamesh",
                "Jenova",
                "Midgardsormr",
                "Sargatanas",
                "Siren",
            )
        )
        private val chaos = DataCenter(
            name = "Chaos", region = Region.EUROPE, servers = setOf(
                "Cerberus",
                "Louisoix",
                "Moogle",
                "Omega",
                "Ragnarok",
                "Spriggan",
            )
        )
        private val crystal = DataCenter(
            name = "Crystal", region = Region.NORTH_AMERICA, servers = setOf(
                "Balmung",
                "Brynhildr",
                "Coeurl",
                "Diabolos",
                "Goblin",
                "Malboro",
                "Mateus",
                "Zalera",
            )
        )
        private val elemental = DataCenter(
            name = "Elemental", region = Region.JAPAN, servers = setOf(
                "Aegis",
                "Atomos",
                "Carbuncle",
                "Garuda",
                "Gungir",
                "Kujata",
                "Ramuh",
                "Tonberry",
                "Typhon",
                "Unicorn",
            )
        )
        private val gaia = DataCenter(
            name = "Gaia", region = Region.JAPAN, servers = setOf(
                "Alexander",
                "Bahamut",
                "Durandal",
                "Fenrir",
                "Ifrit",
                "Ridill",
                "Tiamat",
                "Ultima",
                "Valefor",
                "Yojimbo",
                "Zeromus",
            )
        )
        private val light = DataCenter(
            name = "Light", region = Region.EUROPE, servers = setOf(
                "Lich",
                "Odin",
                "Phoenix",
                "Shiva",
                "Twintania",
                "Zodiark",
            )
        )
        private val mana = DataCenter(
            name = "Mana", region = Region.JAPAN, servers = setOf(
                "Anima",
                "Asura",
                "Belias",
                "Chocobo",
                "Hades",
                "Ixion",
                "Mandragora",
                "Masamune",
                "Pandaemonium",
                "Shinryu",
                "Titan",
            )
        )
        private val primal = DataCenter(
            name = "Primal", region = Region.NORTH_AMERICA, servers = setOf(
                "Behemoth",
                "Excalibur",
                "Exodus",
                "Famfrit",
                "Hyperion",
                "Lamia",
                "Leviathan",
                "Ultros",
            )
        )

        val DEFAULT = crystal

        private val mutableCenters = mapOf(
            aether.name to aether,
            chaos.name to chaos,
            crystal.name to crystal,
            elemental.name to elemental,
            gaia.name to gaia,
            light.name to light,
            mana.name to mana,
            primal.name to primal,
        )

        fun addCenter(dataCenter: DataCenter) {

        }

        val centers: Map<String, DataCenter> = mutableCenters
    }
}