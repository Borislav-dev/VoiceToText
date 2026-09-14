package org.example.project.domain.usecase

class SearchInTextUseCase {
    operator fun invoke(text: String, query: String): List<IntRange> {
        if (query.isBlank()) return emptyList()
        val matches = mutableListOf<IntRange>()
        var index = text.indexOf(query, ignoreCase = true)
        while (index != -1) {
            matches.add(index until (index + query.length))
            index = text.indexOf(query, index + 1, ignoreCase = true)
        }
        return matches
    }
}
