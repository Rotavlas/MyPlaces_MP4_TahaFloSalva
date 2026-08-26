package com.lamanu.myplaces.domain.model

/**
 * Palette d'emojis proposee dans le formulaire. Seule la chaine [emoji] est persistee :
 * ajouter une entree ici n'impacte donc pas le schema de la base.
 */
enum class MoodCategory(val label: String) {
    FEELING("Ressenti"),
    NATURE("Nature"),
    FOOD("Gourmandise"),
    ACTIVITY("Activite"),
    PLACE("Lieu"),
}

data class Mood(
    val emoji: String,
    val label: String,
    val category: MoodCategory,
)

object Moods {
    val ALL: List<Mood> = listOf(
        Mood("\uD83D\uDE0D", "Coup de coeur", MoodCategory.FEELING),
        Mood("\uD83D\uDE04", "Joyeux", MoodCategory.FEELING),
        Mood("\uD83D\uDE0C", "Apaise", MoodCategory.FEELING),
        Mood("\uD83E\uDD29", "Emerveille", MoodCategory.FEELING),
        Mood("\uD83D\uDE22", "Triste", MoodCategory.FEELING),
        Mood("\uD83D\uDE20", "Enerve", MoodCategory.FEELING),
        Mood("\uD83D\uDE31", "Surpris", MoodCategory.FEELING),

        Mood("\uD83C\uDF32", "Foret", MoodCategory.NATURE),
        Mood("\uD83C\uDF0A", "Mer", MoodCategory.NATURE),
        Mood("\u26F0\uFE0F", "Montagne", MoodCategory.NATURE),
        Mood("\uD83C\uDF3B", "Campagne", MoodCategory.NATURE),
        Mood("\uD83C\uDF05", "Lever de soleil", MoodCategory.NATURE),

        Mood("\uD83C\uDF55", "Restaurant", MoodCategory.FOOD),
        Mood("\u2615", "Cafe", MoodCategory.FOOD),
        Mood("\uD83C\uDF7A", "Bar", MoodCategory.FOOD),
        Mood("\uD83C\uDF70", "Patisserie", MoodCategory.FOOD),

        Mood("\uD83C\uDFC3", "Sport", MoodCategory.ACTIVITY),
        Mood("\uD83C\uDFB8", "Concert", MoodCategory.ACTIVITY),
        Mood("\uD83C\uDFA8", "Culture", MoodCategory.ACTIVITY),
        Mood("\uD83D\uDCDA", "Etude", MoodCategory.ACTIVITY),

        Mood("\uD83C\uDFE0", "Chez moi", MoodCategory.PLACE),
        Mood("\uD83C\uDFDB\uFE0F", "Monument", MoodCategory.PLACE),
        Mood("\uD83C\uDFD6\uFE0F", "Vacances", MoodCategory.PLACE),
        Mood("\uD83D\uDCBC", "Travail", MoodCategory.PLACE),
    )

    val DEFAULT: Mood = ALL.first()

    fun byCategory(): Map<MoodCategory, List<Mood>> = ALL.groupBy { it.category }
}
