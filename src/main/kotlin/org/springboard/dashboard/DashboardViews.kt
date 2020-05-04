package org.springboard.dashboard

import org.springboard.user.EnrichedUserDetails
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

data class DashboardIndexViewModel(val user: EnrichedUserDetails)

interface IDashboardViews {
    fun renderIndex(model: DashboardIndexViewModel): ModelAndView
}

@Component
class DashboardViews : IDashboardViews {
    override fun renderIndex(model: DashboardIndexViewModel): ModelAndView {
        return ModelAndView("pages/dashboard/index", mapOf("model" to model))
    }
}
