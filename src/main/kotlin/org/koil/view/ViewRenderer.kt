package org.koil.view

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.ModelAndView
import kotlin.streams.toList

interface ViewRenderer<MODEL> {
    fun render(model: MODEL, httpStatus: HttpStatus = HttpStatus.OK): ModelAndView =
        ModelAndView(template, mapOf("model" to model))
            .apply {
                status = httpStatus
            }

    val template: String
}

abstract class PaginatedViewModel<T>(val page: Page<T>) {
    private val current = page.pageable

    fun hasPrevious(): Boolean = page.hasPrevious()
    fun hasNext(): Boolean = page.hasNext()

    fun pageNumber(): String = (page.number + 1).toString()
    fun totalPages(): String = page.totalPages.toString()
    fun numberOfElements(): String = page.numberOfElements.toString()

    fun firstPage(): String = createQueryString(current.first())
    fun previousPage(): String = createQueryString(page.previousOrFirstPageable())

    fun nextPage(): String = createQueryString(page.nextPageable())
    fun lastPage(): String = createQueryString(current.withPage(page.totalPages - 1))

    fun sortBy(property: String): String {
        val pageable = page.pageable
        val existingDirection = pageable.sort.getOrderFor(property)?.direction
        val direction = if (existingDirection == Sort.Direction.DESC || existingDirection == null) {
            Sort.Direction.ASC
        } else {
            Sort.Direction.DESC
        }

        val sort = Sort.by(direction, property)
        val updated = PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)
        return createQueryString(updated)
    }

    fun sortDirection(property: String): String {
        return page.pageable.sort
            .getOrderFor(property)
            ?.direction
            ?.let { "($it)" }
            ?.lowercase()
            ?: ""
    }

    private fun createQueryString(pageable: Pageable): String {
        var query = "?"
        if (pageable.sort.isSorted) {
            query += pageable.sort.get().map {
                "&sort=${it.property},${it.direction}"
            }.toList().reduce { acc, s -> "$acc$s" }
        }

        query += "&page=${pageable.pageNumber}"
        query += "&size=${pageable.pageSize}"

        return query
    }
}
