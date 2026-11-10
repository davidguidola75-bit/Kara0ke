package com.example.data.repository

import com.example.data.model.KaraokeSong
import com.example.data.model.LrcParser

object SampleSongs {

    private val BESAME_MUCHO_LRC = """
        [00:00.00]♪ Intro instrumental ♪
        [00:05.50]Bésame, bésame mucho
        [00:13.20]Como si fuera esta noche la última vez
        [00:23.00]Bésame, bésame mucho
        [00:30.50]Que tengo miedo a perderte, perderte después
        [00:40.00]Quiero tenerte muy cerca
        [00:44.20]Mirarme en tus ojos, verte junto a mí
        [00:52.50]Piensa que tal vez mañana
        [00:57.00]Yo ya estaré lejos, muy lejos de ti
        [01:05.00]Bésame, bésame mucho
        [01:12.80]Como si fuera esta noche la última vez
        [01:22.50]Bésame, bésame mucho
        [01:30.00]Que tengo miedo a perderte, perderte después
        [01:39.00]♪ Solo de trompeta y guitarra ♪
        [01:55.00]Bésame mucho...
    """.trimIndent()

    private val LA_BAMBA_LRC = """
        [00:00.00]♪ Intro de guitarra - ¡Arriba! ♪
        [00:06.00]Para bailar la bamba
        [00:10.50]Para bailar la bamba se necesita una poca de gracia
        [00:15.80]Una poca de gracia pa' mí, pa' ti, ay arriba, ay arriba
        [00:21.00]Ay arriba y arriba, por ti seré
        [00:25.50]Por ti seré, por ti seré
        [00:29.80]Yo no soy marinero
        [00:33.20]Yo no soy marinero, soy capitán
        [00:37.50]Soy capitán, soy capitán
        [00:41.00]Bamba, bamba, bamba, bamba
        [00:45.00]Bamba, bamba, ¡bamba!
        [00:49.00]♪ Riff de Rock & Roll ♪
        [00:59.00]Para bailar la bamba
        [01:03.50]Para bailar la bamba se necesita una poca de gracia
        [01:09.00]Una poca de gracia pa' mí, pa' ti, ay arriba, ay arriba
    """.trimIndent()

    private val DE_MUSICA_LIGERA_LRC = """
        [00:00.00]♪ Riff de bajo y batería Soda Stereo ♪
        [00:10.00]Ella durmió al calor de las masas
        [00:17.50]Y yo desperté queriendo soñarla
        [00:24.00]Algún tiempo atrás pensé en escribirle
        [00:31.00]Que nunca sorteé las trampas del amor
        [00:38.00]De aquel amor de música ligera
        [00:45.00]Nada nos libra, nada más queda
        [00:52.50]No le enviaré cenizas de rosas
        [00:59.80]Ni pienso evitar un roce secreto
        [01:06.50]De aquel amor de música ligera
        [01:13.50]Nada nos libra, nada más queda
        [01:21.00]¡Nada más queda!
        [01:28.00]♪ Solo distorsionado de guitarra eléctrica ♪
        [01:45.00]¡Gracias totales!
    """.trimIndent()

    private val BOHEMIAN_RHAPSODY_LRC = """
        [00:00.00]Is this the real life? Is this just fantasy?
        [00:08.50]Caught in a landslide, no escape from reality
        [00:18.00]Open your eyes, look up to the skies and see
        [00:29.50]I'm just a poor boy, I need no sympathy
        [00:36.50]Because I'm easy come, easy go, little high, little low
        [00:46.00]Any way the wind blows doesn't really matter to me, to me
        [00:58.00]Mama, just killed a man
        [01:05.50]Put a gun against his head, pulled my trigger, now he's dead
        [01:17.00]Mama, life had just begun
        [01:24.00]But now I've gone and thrown it all away
        [01:33.50]Mama, ooh, didn't mean to make you cry
        [01:42.00]If I'm not back again this time tomorrow
        [01:47.50]Carry on, carry on, as if nothing really matters
    """.trimIndent()

    private val STAND_BY_ME_LRC = """
        [00:00.00]♪ Bassline groove and triangle ♪
        [00:07.50]When the night has come
        [00:13.00]And the land is dark
        [00:17.50]And the moon is the only light we'll see
        [00:24.50]No, I won't be afraid, no, I won't be afraid
        [00:32.00]Just as long as you stand, stand by me
        [00:39.00]So darling, darling, stand by me, oh stand by me
        [00:48.00]Oh stand, stand by me, stand by me
        [00:56.00]If the sky that we look upon should tumble and fall
        [01:04.50]Or the mountain should crumble to the sea
        [01:12.00]I won't cry, I won't cry, no, I won't shed a tear
        [01:19.50]Just as long as you stand, stand by me
    """.trimIndent()

    fun getDemoSongs(): List<KaraokeSong> {
        return listOf(
            KaraokeSong(
                id = "demo_besame_mucho",
                title = "Bésame Mucho",
                artist = "Consuelo Velázquez",
                album = "Joyas del Bolero",
                durationMs = 125_000L,
                rawLrc = BESAME_MUCHO_LRC,
                parsedLyrics = LrcParser.parse(BESAME_MUCHO_LRC),
                isVideo = false,
                isLocal = false,
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4" // High reliability CDN test stream
            ),
            KaraokeSong(
                id = "demo_la_bamba",
                title = "La Bamba",
                artist = "Ritchie Valens",
                album = "Rock Classics",
                durationMs = 85_000L,
                rawLrc = LA_BAMBA_LRC,
                parsedLyrics = LrcParser.parse(LA_BAMBA_LRC),
                isVideo = true,
                isLocal = false,
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            ),
            KaraokeSong(
                id = "demo_de_musica_ligera",
                title = "De Música Ligera",
                artist = "Soda Stereo",
                album = "Canción Animal",
                durationMs = 115_000L,
                rawLrc = DE_MUSICA_LIGERA_LRC,
                parsedLyrics = LrcParser.parse(DE_MUSICA_LIGERA_LRC),
                isVideo = false,
                isLocal = false,
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            ),
            KaraokeSong(
                id = "demo_bohemian_rhapsody",
                title = "Bohemian Rhapsody",
                artist = "Queen",
                album = "A Night at the Opera",
                durationMs = 120_000L,
                rawLrc = BOHEMIAN_RHAPSODY_LRC,
                parsedLyrics = LrcParser.parse(BOHEMIAN_RHAPSODY_LRC),
                isVideo = true,
                isLocal = false,
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
            ),
            KaraokeSong(
                id = "demo_stand_by_me",
                title = "Stand By Me",
                artist = "Ben E. King",
                album = "Don't Play That Song!",
                durationMs = 95_000L,
                rawLrc = STAND_BY_ME_LRC,
                parsedLyrics = LrcParser.parse(STAND_BY_ME_LRC),
                isVideo = false,
                isLocal = false,
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
            )
        )
    }
}
