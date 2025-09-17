package com.example.financeapp.data.repository

import com.example.financeapp.data.dbManager.DbManagerPlanning
import com.example.financeapp.data.model.PlanningClass

class PlanningRepository(private val dbManagerPlanning: DbManagerPlanning): AbstractRepository(dbManagerPlanning) {

    fun getPlannings(): ArrayList<PlanningClass>{
        return dbManagerPlanning.readDbData(query_)
    }

    fun createPlannings(
        planning: PlanningClass
    ){
        val values = dbManagerPlanning.createValues(planning)
        dbManagerPlanning.insertToDb(values)
    }

    fun updatePlanning(
        planning: PlanningClass
    ){
        val values = dbManagerPlanning.createValues(planning)
        dbManagerPlanning.updateInDb(planning.id, values)
    }

}